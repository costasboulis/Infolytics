<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />
<%
	String selectedMenu = "home";
%>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="../css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery-1.5.1.min.js"></script>
<script type="text/javascript" src="../js/charts/charts.js"></script>
<script type="text/javascript" src="../js/charts/themes/gray.js"></script>
<script type="text/javascript" src="../js/charts/modules/exporting.js"></script>
<script type="text/javascript" src="../js/home.js"></script>
<script type="text/javascript">
	var homeUrl1 = "get_metric.do";
</script>
</head>

<body>
	
	<!--hidden vars-->
	<input type="hidden" id="hiddenTenantSite" name="hiddenTenantSite" value="${tenant.url}" />
	<input type="hidden" id="hiddenPeriod" name="hiddenPeriod" value="LAST_30DAYS" />
	
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
								class="spanMainHeaderLeft">Dashboard</span></td>
							<td align="right" valign="bottom"><span
								class="spanMainHeaderRight">
								Metric:&nbsp;&nbsp;
								<select name="selMetric" id="selMetric" class="selXSmall" onchange="selMetricList('spanWidget')">
									<option value="REC_SERVED">Total Served</option>
									<!-- <option value="REC_SERVED">Total Empty</option>
									<option value="REC_SERVED">Total Unique</option> -->
									<option value="REC_CLICK">Clicked</option>
									<!-- <option value="REC_SERVED">Clicked Unique</option> -->
									<option value="CTR">CTR</option>
									<option value="PURCHASE">Purchases</option>
									<option value="ITEM_PAGE">Total Item Pages</option>
									<option value="ADD_TO_CART">Add To Cart Requests</option>
									<!-- <option value="REC_SERVED">Widget Purchases</option>
									<option value="REC_SERVED">AOV</option>
									<option value="REC_SERVED">Widget AOV</option> -->
								</select>
								<span id="spanWidget" style="display:none;">&nbsp;&nbsp;Widget:&nbsp;&nbsp;<select name="selWidget" id="selWidget" class="selXSmall" onclick=""></select></span>
								&nbsp;&nbsp;Period:&nbsp;&nbsp;<a href="#" onclick="changePeriod('LAST_30DAYS');return false;"><span id="daySpan" class="red">Last 30 Days</span></a>
								&nbsp;&nbsp;|&nbsp;&nbsp;<a href="#" onclick="changePeriod('LAST_6MONTHS');return false;"><span id="monthSpan">Last 6 months</span></a>
								&nbsp;&nbsp;|&nbsp;&nbsp;<a href="#" onclick="changePeriod('LAST_YEAR');return false;"><span id="yearSpan">Last 12 months</span></a>
							</span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<div id="chart-container"
						style="height: 340px; clear: both; min-width: 946px;" align="center"><img src="../images/loader-stats.gif" style="padding-top: 10px;" /></div>
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
								class="spanMainHeaderLeft">Key Metrics</span></td>
							<td align="left" valign="bottom" width="50%"><span
								class="spanMainHeaderLeft"></span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<table width="100%">
						<tr>
							<td width="100%">
							
							<div id="spanPerformance" align="center"><img src="../images/loader-stats.gif" style="padding-top: 10px;" /></div>
								
							</td>
						</tr>
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