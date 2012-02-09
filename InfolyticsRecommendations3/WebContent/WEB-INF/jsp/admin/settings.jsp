<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />
<%
	String selectedMenu = "settings";
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="../css/admin.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="../js/common.js"></script>
<script type="text/javascript" src="../js/settings.js"></script>
<script type="text/javascript">var settingsUrl1 = "update_per_settings.do";</script>
<script type="text/javascript">var settingsUrl2 = "update_log_settings.do";</script>
</head>

<body>
	<!--page wrapper-->
	<div class="wrapper">

		<%@ include file="header_menu.jsp"%>

		<!--main-->
		<div class="main-wrapper">
			<div class="container">
				<div id="mainHeader">
					<table width="100%">
						<tr>
							<td align="left" valign="bottom"><span
								class="spanMainHeaderLeft">Personal Settings</span></td>
								<td align="right" valign="bottom"><span
								class="spanMainHeaderRight"><div class="error3"></div><div class="success3"></div>
							</span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<table width="100%" cellpadding="3" cellspacing="2" border="0">
					<tr>
					<td valign="middle" width="20%"><span class="label">First Name:</span></td><td valign="middle"><input type="text" name="firstName" id="firstName" maxlength="50" class="inputMedium" value="${tenant.firstName}" /></td>
					<td valign="middle" width="20%"><span class="label">Last Name:</span></td><td valign="middle"><input type="text" name="lastName" id="lastName" maxlength="50" class="inputMedium" value="${tenant.lastName}" /></td>
					</tr>
					<!-- space -->
					<tr><td colspan="4">&nbsp;</td></tr>
					<!-- space ends-->
					<tr>
					<td valign="middle" width="20%"><span class="label">Company:</span></td><td valign="middle"><input type="text" name="company" id="company" maxlength="50" class="inputMedium" value="${tenant.company}" /></td>
					<td valign="middle" width="20%"><span class="label">Email:</span></td><td valign="middle"><input type="text" name="email" id="email" maxlength="50" class="inputMedium" value="${tenant.email}" /></td>
					</tr>
					<!-- space -->
					<tr><td colspan="4">&nbsp;</td></tr>
					<!-- space ends-->
					<tr>
					<td valign="middle" width="20%"><span class="label">Site URL:</span></td><td valign="middle"><input type="text" name="url" id="url" maxlength="50" class="inputMedium" value="${tenant.url}" /></td>
					<td valign="middle" width="20%"><span class="label">Phone:</span></td><td valign="middle"><input type="text" name="phone" id="phone" maxlength="50" class="inputMedium" value="${tenant.phone}" /></td>
					</tr>
					<!-- space -->
					<tr><td colspan="4">&nbsp;</td></tr>
					<!-- space ends-->
					<tr><td colspan="4" align="center"><a href="/" class="small-button" onclick="updatePerSettings(this);return false;"><span class="small-button-span">Update</span></a></td></tr>
					</table>
				</div>
			</div>
		</div>
		<!--main ends-->
		<!--sub-main-->
		<div class="sub-main-wrapper">
			<div class="container">
				<div id="subMainHeader">
					<table width="100%">
						<tr>
							<td align="left" valign="bottom" width="50%"><span
								class="spanMainHeaderLeft">Login Settings</span></td>
							<td align="right" valign="bottom"><span
								class="spanMainHeaderRight"><div class="error4"></div><div class="success4"></div>
							</span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<table width="100%" cellpadding="3" cellspacing="2" border="0">
					<tr>
					<td valign="middle" width="20%"><span class="label">Username:</span></td><td valign="middle"><span class="labelValue">${tenant.username}</span></td>
					<td valign="middle" width="20%"><span class="label">Password:</span></td><td valign="middle"><span class="labelValue">**********</span></td>
					</tr>
					<!-- space -->
					<tr><td colspan="4">&nbsp;</td></tr>
					<!-- space ends-->
					<tr>
					<td valign="middle" width="20%"><span class="label">Change Password:</span></td><td valign="middle"><input type="password" name="password1" id="password1" maxlength="50" class="inputMedium" value="" /></td>
					<td valign="middle" width="20%"><span class="label">Confirm Password:</span></td><td valign="middle"><input type="password" name="password2" id="password2" maxlength="50" class="inputMedium" value="" /></td>
					</tr>
					<!-- space -->
					<tr><td colspan="4">&nbsp;</td></tr>
					<!-- space ends-->
					<tr><td colspan="4" align="center"><a href="/" class="small-button" onclick="updateLogSettings(this);return false;"><span class="small-button-span">Update</span></a></td></tr>
					</table>
				</div>
			</div>
		</div>
		<!--sub-main ends-->
		<br clear="all" />
	</div>
	<!--wrapper ends-->

	<%@ include file="footer.jsp"%>
	
</body>
</html>