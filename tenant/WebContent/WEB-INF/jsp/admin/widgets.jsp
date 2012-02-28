<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />
<%
	String selectedMenu = "widgets";
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="../css/admin.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="screen"
	href="../js/jquery.jqGrid-3.7.2/css/ui.jqgrid.css" />
<link href="../css/jquery-ui-1.8.16.custom.css" rel="stylesheet"
	type="text/css" />
<link rel="stylesheet" type="text/css" href="../css/jquery.snippet.min.css" />
	
<script type="text/javascript" src="../js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.16.custom.min.js"></script>
<script src="../js/jquery.jqGrid-3.7.2/js/i18n/grid.locale-en.js"
	type="text/javascript"></script>
<script src="../js/jquery.jqGrid-3.7.2/js/jquery.jqGrid.min.js"
	type="text/javascript"></script>
<script type="text/javascript" src="../js/jquery.snippet.min.js"></script>
<script type="text/javascript" src="../js/widgets.js"></script>
<script type="text/javascript">
	var widgetsUrl1 = "get_widgets.do";
	var widgetsUrl2 = "add_widget.do";
	var widgetsUrl3 = "delete_widget.do";
</script>
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
								class="spanMainHeaderLeft">My Widgets</span></td>
							<td align="right" valign="bottom"><span
								class="spanCatalogMainHeaderRight">		
								<a href="/"
						class="x-small-button-2" onclick="$('#dialogAdd').dialog('open'); return false;"><span
						class="x-small-button-span">Add Widget</span></a>
								</span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<br />
					<table id='tblWidgets'></table>
				</div>
			</div>
		</div>
		<!--main ends-->
		<br clear="all" />
	</div>
	<!--wrapper ends-->
	<div id="dialogAdd" title="Add a new Widget">
		<br /><table width="100%" cellpadding="4" cellspacing="3">
			<tr>
				<td colspan="2">
					Select a name and description for your widget:
				</td>
			</tr>
			<!-- space -->
			<tr><td colspan="2">&nbsp;</td></tr>
			<!-- space ends-->
			<tr>
				<td valign="middle">
					Name:
				</td>
				<td valign="middle">
					<input type="text" name="widgetName" id="widgetName" maxlength="100" class="inputMedium" value="" />
				</td>
			</tr>
			<!-- space -->
			<tr><td colspan="2">&nbsp;</td></tr>
			<!-- space ends-->
			<tr>
				<td valign="middle">
					Description:
				</td>
				<td valign="middle">
					<input type="text" name="widgetDesc" id="widgetDesc" maxlength="150" class="inputMedium" value="" />
				</td>
			</tr>
			<!-- space -->
			<tr><td colspan="2">&nbsp;</td></tr>
			<!-- space ends-->
			<tr><td colspan="2" align="center">
			<a href="/" class="small-button" onclick="addWidget(this);return false;"><span class="small-button-span">Add Widget</span></a>
			</td>
			</tr>
		</table>
	</div>
	
	<div id="dialogPreview" title="Preview Widget">
		<table width="100%" cellpadding="4" cellspacing="3">
			<tr>
				<td align="center" valign="middle"><span id="spanPreview"></span></td>
			</tr>
		</table>
	</div>
	<div id="dialogCode" title="Copy and paste the following code to your website">
		<pre id="widgetCode"></pre>
	</div>
	<%@ include file="footer.jsp"%>
	
</body>
</html>