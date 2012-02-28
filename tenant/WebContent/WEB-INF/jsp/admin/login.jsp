<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="../css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="../js/login.js"></script>
<script type="text/javascript">var checkLoginUrl = "verifyUser.do";</script>
</head>

<body>
	<!--header-->
	<div class="wrapper">
		<div class="header-wrapper">
			<div class="container">
				<div id="header">
					<div id="divLogo">
						<a href="login.do"><img src="../images/logo_admin.png" border="0" /></a>
					</div>
					<div id="divInfo">
						<div class="content">Welcome Guest. Please login with your
							username and password.</div>
					</div>
					<div id="divLang">
						<ul class="languages">
							<li><img src="../images/en_flag.png"
									alt="english" border="0" /></li>
							<li style="margin-left: 8px;"><img
									src="../images/el_flag_b.png" alt="greek" border="0" /></li>
						</ul>
						<br clear="right" />
						<div id="divRightMenu">
							<ul class="rightMenu">
								<li><a href="forgot_pass.do">Forgot Password?</a></li>
								<li><a href="http://www.cleargist.com/contact.php" target="_blank">Contact</a></li>
								<li><a href="http://www.cleargist.com/" target="_blank">About</a></li>
							</ul>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!--main-->
		<%-- <form name="frmSignIn" id="frmSignIn"
			action="home.do"> --%>
		<form name='frmSignIn' action="<c:url value='../j_spring_security_check' />" method='post'>
			<div class="main-wrapper">
				<div class="container">
					<div id="loginHeader">
						<span>Sign In</span><br />
						<div id="menu_seperator_top"></div>
						<br clear="all" />
						<c:if test="${not empty error}">
							<div class="error">
								Your login attempt was not successful, please try again.<br /> Caused :
								${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
							</div>
						</c:if>
	
					</div>
					<div id="loginBox">
						<!--<span class="label">Username</span><br />-->
						<input type="text" name="j_username" id="j_username"
							maxlength="50" class="inputLarge" value="username"
							onblur="if(this.value==''){this.value='username'}"
							onfocus="if(this.value=='username'){this.value='';}" /><br />
						<!--<span class="label">Password</span><br />-->
						<input type="password" id="j_password" name="j_password"
							maxlength="50" class="inputLarge" value="password"
							onblur="if(this.value==''){this.value='password'}"
							onfocus="if(this.value=='password'){this.value='';}" /><br /> <a
							href="/" id="signin" onclick="signin(this);return false;"><span
							id="signinSpan">Sign In</span></a><br />
						<div class="content">
							<input type='checkbox' name='_spring_security_remember_me' />
							Remember me on this computer.
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>
</body>
</html>