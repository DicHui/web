package com.lero.web;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lero.dao.UserDao;
import com.lero.model.Admin;
import com.lero.model.DormManager;
import com.lero.model.Student;
import com.lero.util.JDBCUtil;

public class LoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JDBCUtil dbUtil = new JDBCUtil();
	UserDao userDao = new UserDao();
	
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
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		String remember = request.getParameter("remember");
		String userType = request.getParameter("userType");
		
		Connection con = null;
		try {
			con=dbUtil.getConn();
			Admin currentAdmin = null;
			DormManager currentDormManager = null;
			Student currentStudent = null;
			//如果类型为管理员
			if("admin".equals(userType)) {
				Admin admin = new Admin(userName, password);
				currentAdmin = userDao.Login(con, admin);
				if(currentAdmin == null) {
					request.setAttribute("admin", admin);
					request.setAttribute("error", "用户名或密码错误！");
					request.getRequestDispatcher("login.jsp").forward(request, response);
				} else {
					if("remember-me".equals(remember)) {
						rememberMe(userName, password, userType,response);
					} else {
						deleteCookie(userName, request, response);
					}
					session.setAttribute("currentUserType", "admin");
					session.setAttribute("currentUser", currentAdmin);
					request.setAttribute("mainPage", "admin/blank.jsp");
					request.getRequestDispatcher("mainAdmin.jsp").forward(request, response);
				}
				//如果类型为宿舍管理员
			} else if("dormManager".equals(userType)) {
				DormManager dormManager = new DormManager(userName, password);
				currentDormManager = userDao.Login(con, dormManager);
				if(currentDormManager == null) {
					request.setAttribute("dormManager", dormManager);
					request.setAttribute("error", "用户名或密码错误！");
					request.getRequestDispatcher("login.jsp").forward(request, response);
				} else {
					if("remember-me".equals(remember)) {
						rememberMe(userName, password, userType,response);
					} else {
						deleteCookie(userName, request, response);
					}
					session.setAttribute("currentUserType", "dormManager");
					session.setAttribute("currentUser", currentDormManager);
					request.setAttribute("mainPage", "dormManager/blank.jsp");
					request.getRequestDispatcher("mainManager.jsp").forward(request, response);
				}
				//如果类型为学生
			} else if("student".equals(userType)) {
				Student student = new Student(userName, password);
				currentStudent = userDao.Login(con, student);
				if(currentStudent == null) {
					request.setAttribute("student", student);
					request.setAttribute("error", "用户名或密码错误！");
					request.getRequestDispatcher("login.jsp").forward(request, response);
				} else {
					if("remember-me".equals(remember)) {
						rememberMe(userName, password, userType,response);
					} else {
						deleteCookie(userName, request, response);
					}
					session.setAttribute("currentUserType", "student");
					session.setAttribute("currentUser", currentStudent);
					request.setAttribute("mainPage", "student/blank.jsp");
					request.getRequestDispatcher("mainStudent.jsp").forward(request, response);
				}
			} 
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				dbUtil.closeAll(null,null,con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//记住我
	private void rememberMe(String userName, String password, String userType, HttpServletResponse response) {
		Cookie user = new Cookie("dormuser", userName+"-"+password+"-"+userType+"-"+"yes");
		user.setMaxAge(1*60*60*24*7);
		response.addCookie(user);
	}
	//不记住我
	private void deleteCookie(String userName, HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies=request.getCookies();
		for(int i=0;cookies!=null && i<cookies.length;i++){
			if(cookies[i].getName().equals("dormuser")){
				if(userName.equals(userName=cookies[i].getValue().split("-")[0])) {
					Cookie cookie = new Cookie(cookies[i].getName(), null);
					cookie.setMaxAge(0);
					response.addCookie(cookie);
					break;
				}
			}
		}
	}
}
