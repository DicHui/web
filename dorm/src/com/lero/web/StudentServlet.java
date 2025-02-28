package com.lero.web;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lero.dao.DormBuildDao;
import com.lero.dao.StudentDao;
import com.lero.model.DormManager;
import com.lero.model.Student;
import com.lero.util.JDBCUtil;
import com.lero.util.StringTool;

public class StudentServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JDBCUtil dbUtil = new JDBCUtil();
	StudentDao studentDao = new StudentDao();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		HttpSession session = request.getSession();
		Object currentUserType = session.getAttribute("currentUserType");
		String s_studentText = request.getParameter("s_studentText");
		String dormBuildId = request.getParameter("buildToSelect");
		String searchType = request.getParameter("searchType");
		String action = request.getParameter("action");
		Student student = new Student();
		//点击添加学生
		if("preSave".equals(action)) {
			studentPreSave(request, response);
			return;
			//执行修改或添加学生
		} else if("save".equals(action)){
			studentSave(request, response);
			return;
			//删除学生
		} else if("delete".equals(action)){
			studentDelete(request, response);
			return;
			//查询学生
		} else if("list".equals(action)) {
			if(StringTool.isNotEmpty(s_studentText)) {
				if("name".equals(searchType)) {
					student.setName(s_studentText);
				} else if("number".equals(searchType)) {
					student.setStuNumber(s_studentText);
				} else if("dorm".equals(searchType)) {
					student.setDormName(s_studentText);
				}
			}
			if(StringTool.isNotEmpty(dormBuildId)) {
				student.setDormBuildId(Integer.parseInt(dormBuildId));
			}
			session.removeAttribute("s_studentText");
			session.removeAttribute("searchType");
			session.removeAttribute("buildToSelect");
			request.setAttribute("s_studentText", s_studentText);
			request.setAttribute("searchType", searchType);
			request.setAttribute("buildToSelect", dormBuildId);
			//模糊查询
		} else if("search".equals(action)){
			if(StringTool.isNotEmpty(s_studentText)) {
				if("name".equals(searchType)) {
					student.setName(s_studentText);
				} else if("number".equals(searchType)) {
					student.setStuNumber(s_studentText);
				} else if("dorm".equals(searchType)) {
					student.setDormName(s_studentText);
				}
				session.setAttribute("s_studentText", s_studentText);
				session.setAttribute("searchType", searchType);
			} else {
				session.removeAttribute("s_studentText");
				session.removeAttribute("searchType");
			}
			if(StringTool.isNotEmpty(dormBuildId)) {
				student.setDormBuildId(Integer.parseInt(dormBuildId));
				session.setAttribute("buildToSelect", dormBuildId);
			}else {
				session.removeAttribute("buildToSelect");
			}
		} else {
			if("admin".equals((String)currentUserType)) {
				if(StringTool.isNotEmpty(s_studentText)) {
					if("name".equals(searchType)) {
						student.setName(s_studentText);
					} else if("number".equals(searchType)) {
						student.setStuNumber(s_studentText);
					} else if("dorm".equals(searchType)) {
						student.setDormName(s_studentText);
					}
					session.setAttribute("s_studentText", s_studentText);
					session.setAttribute("searchType", searchType);
				}
				if(StringTool.isNotEmpty(dormBuildId)) {
					student.setDormBuildId(Integer.parseInt(dormBuildId));
					session.setAttribute("buildToSelect", dormBuildId);
				}
				if(StringTool.isEmpty(s_studentText) && StringTool.isEmpty(dormBuildId)) {
					Object o1 = session.getAttribute("s_studentText");
					Object o2 = session.getAttribute("searchType");
					Object o3 = session.getAttribute("buildToSelect");
					if(o1!=null) {
						if("name".equals((String)o2)) {
							student.setName((String)o1);
						} else if("number".equals((String)o2)) {
							student.setStuNumber((String)o1);
						} else if("dorm".equals((String)o2)) {
							student.setDormName((String)o1);
						}
					}
					if(o3 != null) {
						student.setDormBuildId(Integer.parseInt((String)o3));
					}
				}
			} else if("dormManager".equals((String)currentUserType)) {
				if(StringTool.isNotEmpty(s_studentText)) {
					if("name".equals(searchType)) {
						student.setName(s_studentText);
					} else if("number".equals(searchType)) {
						student.setStuNumber(s_studentText);
					} else if("dorm".equals(searchType)) {
						student.setDormName(s_studentText);
					}
					session.setAttribute("s_studentText", s_studentText);
					session.setAttribute("searchType", searchType);
				}
				if(StringTool.isEmpty(s_studentText)) {
					Object o1 = session.getAttribute("s_studentText");
					Object o2 = session.getAttribute("searchType");
					if(o1!=null) {
						if("name".equals((String)o2)) {
							student.setName((String)o1);
						} else if("number".equals((String)o2)) {
							student.setStuNumber((String)o1);
						} else if("dorm".equals((String)o2)) {
							student.setDormName((String)o1);
						}
					}
				}
			}
		}
		Connection con = null;
		try {
			con=dbUtil.getConn();
			if("admin".equals((String)currentUserType)) {
				List<Student> studentList = studentDao.studentList(con, student);
				request.setAttribute("dormBuildList", studentDao.dormBuildList(con));
				request.setAttribute("studentList", studentList);
				request.setAttribute("mainPage", "admin/student.jsp");
				request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
			} else if("dormManager".equals((String)currentUserType)) {
				DormManager manager = (DormManager)(session.getAttribute("currentUser"));
				int buildId = manager.getDormBuildId();
				String buildName = DormBuildDao.dormBuildName(con, buildId);
				List<Student> studentList = studentDao.studentListWithBuild(con, student, buildId);
				request.setAttribute("dormBuildName", buildName);
				request.setAttribute("studentList", studentList);
				request.setAttribute("mainPage", "dormManager/student.jsp");
				request.getRequestDispatcher("mainManager.jsp").forward(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeAll(null,null,con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void studentDelete(HttpServletRequest request,
			HttpServletResponse response) {
		String studentId = request.getParameter("studentId");
		Connection con = null;
		try {
			con = dbUtil.getConn();
			studentDao.studentDelete(con, studentId);
			request.getRequestDispatcher("student?action=list").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeAll(null,null,con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void studentSave(HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {
		String studentId = request.getParameter("studentId");
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String dormBuildId = request.getParameter("dormBuildId");
		String dormName = request.getParameter("dormName");
		String name = request.getParameter("name");
		String sex = request.getParameter("sex");
		String tel = request.getParameter("tel");
		Student student = new Student(userName, password, Integer.parseInt(dormBuildId), dormName, name, sex, tel);
		if(StringTool.isNotEmpty(studentId)) {
			student.setStudentId(Integer.parseInt(studentId));
		}
		Connection con = null;
		try {
			con = dbUtil.getConn();
			int saveNum = 0;
			if(StringTool.isNotEmpty(studentId)) {
				saveNum = studentDao.studentUpdate(con, student);
			} else if(studentDao.haveNameByNumber(con, student.getStuNumber())){
				request.setAttribute("student", student);
				request.setAttribute("error", "该学号已存在");
				request.setAttribute("mainPage", "admin/studentSave.jsp");
				request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
				try {
					dbUtil.closeAll(null,null,con);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			} else {
				saveNum = studentDao.studentAdd(con, student);
			}
			if(saveNum > 0) {
				request.getRequestDispatcher("student?action=list").forward(request, response);
			} else {
				request.setAttribute("student", student);
				request.setAttribute("error", "保存失败");
				request.setAttribute("mainPage", "admin/studentSave.jsp");
				request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeAll(null,null,con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void studentPreSave(HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {
		String studentId = request.getParameter("studentId");
		Connection con = null;
		try {
			con = dbUtil.getConn();
			request.setAttribute("dormBuildList", studentDao.dormBuildList(con));
			if (StringTool.isNotEmpty(studentId)) {
				Student student = studentDao.studentShow(con, studentId);
				request.setAttribute("student", student);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeAll(null,null,con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		request.setAttribute("mainPage", "admin/studentSave.jsp");
		request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
	}

	
}
