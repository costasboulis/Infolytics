<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="../css/admin.css" rel="stylesheet" type="text/css" />
</head>

<body>

        <!--header-->
        <div class="wrapper">    
            <div class="header-wrapper">
                <div class="container">
                    <div id="header">
                        <div id="divLogo"><a href="login.do"><img src="../images/logo_admin.png" border="0" /></a></div>           
                        <div id="divInfo">
                            <div class="content">
                                Welcome Guest. Please login with your username and password.
                            </div>
                        </div>
                        <div id="divLang">
                        <ul class="languages">
                            <li><a href="/"><img src="../images/en_flag.png" alt="english" border="0" /></a></li>
                            <li style="margin-left:8px;"><a href="/el/"><img src="../images/el_flag_b.png" alt="greek" border="0" /></a></li>
                        </ul>
                         <br clear="right" />
                        <div id="divRightMenu">
                            <ul class="rightMenu">
                                <li><a href="forgot_pass.do">Forgot Password?</a></li>
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
                            <span>Access Denied!</span><br />
                            <div id="menu_seperator_top"></div>
                            <br clear="all" />
                            <div class="error"></div>
                    </div>
                    <div id="loginBox">
                        <div class="content">
                                Either your session has expired or you tried to access a page that only cleargist's registered users can access.
                            </div>
                    </div>
                </div>
            </div>

        </div>
     
    </body>
</html>