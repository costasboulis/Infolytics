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
<script type="text/javascript" src="../js/forgotPass.js"></script>
<script type="text/javascript">var checkResetPassUrl = "resetPass.do";</script>
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
						<div class="content">Welcome Guest. Please enter your username and email to reset your password.</div>
					</div>
					<div id="divLang">
						<ul class="languages">
							<li><a href="/"><img src="../images/en_flag.png"
									alt="english" border="0" /></a></li>
							<li style="margin-left: 8px;"><a href="/el/"><img
									src="../images/el_flag_b.png" alt="greek" border="0" /></a></li>
						</ul>
						<br clear="right" />
						<div id="divRightMenu">
							<ul class="rightMenu">
								<li><a href="http://www.cleargist.com/contact.php" target="_blank">Contact</a></li>
								<li><a href="http://www.cleargist.com/" target="_blank">About</a></li>
								<li><a href="login.do">Sign In</a></li>
							</ul>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!--main-->
			<div class="main-wrapper">
				<div class="container">
					<div id="loginHeader">
						<span>Reset your Password</span><br />
						<div id="menu_seperator_top"></div>
						<br clear="all" />
							<div class="error2"></div>
							<div class="success"></div>
					</div>
					<div id="loginBox">
						<input type="text" name="username" id="username"
							maxlength="50" class="inputLarge" value="username"
							onblur="if(this.value==''){this.value='username'}"
							onfocus="if(this.value=='username'){this.value='';}" /><br />
						<input type="text" id="email" name="email"
							maxlength="50" class="inputLarge" value="your email"
							onblur="if(this.value==''){this.value='your email'}"
							onfocus="if(this.value=='your email'){this.value='';}" /><br /> <a
							href="/" class="large-button" onclick="forgotPass(this);return false;"><span
							class="large-button-span">Reset</span></a><br />
					</div>
				</div>
			</div>
	</div>
</body>
</html>