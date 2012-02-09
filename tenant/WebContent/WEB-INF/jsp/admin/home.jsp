<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
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
<script type="text/javascript">
	var chart;
	$(document)
			.ready(
					function() {
						chart = new Highcharts.Chart(
								{
									chart : {
										renderTo : 'chart-container',
										zoomType : 'xy'
									},
									title : {
										text : 'Average Monthly Temperature and Rainfall in Tokyo'
									},
									subtitle : {
										text : 'Source: WorldClimate.com'
									},
									xAxis : [ {
										categories : [ 'Jan', 'Feb', 'Mar',
												'Apr', 'May', 'Jun', 'Jul',
												'Aug', 'Sep', 'Oct', 'Nov',
												'Dec' ]
									} ],
									yAxis : [ { // Primary yAxis
										labels : {
											formatter : function() {
												return this.value + '°C';
											},
											style : {
												color : '#89A54E'
											}
										},
										title : {
											text : 'Temperature',
											style : {
												color : '#89A54E'
											}
										}
									}, { // Secondary yAxis
										title : {
											text : 'Rainfall',
											style : {
												color : '#4572A7'
											}
										},
										labels : {
											formatter : function() {
												return this.value + ' mm';
											},
											style : {
												color : '#4572A7'
											}
										},
										opposite : true
									} ],
									tooltip : {
										formatter : function() {
											return ''
													+ this.x
													+ ': '
													+ this.y
													+ (this.series.name == 'Rainfall' ? ' mm'
															: '°C');
										}
									},
									legend : {
										layout : 'vertical',
										align : 'left',
										x : 120,
										verticalAlign : 'top',
										y : 100,
										floating : true,
										backgroundColor : Highcharts.theme.legendBackgroundColor
												|| '#FFFFFF'
									},
									series : [
											{
												name : 'Rainfall',
												color : '#4572A7',
												type : 'column',
												yAxis : 1,
												data : [ 49.9, 71.5, 106.4,
														129.2, 144.0, 176.0,
														135.6, 148.5, 216.4,
														194.1, 95.6, 54.4 ]

											},
											{
												name : 'Temperature',
												color : '#89A54E',
												type : 'spline',
												data : [ 7.0, 6.9, 9.5, 14.5,
														18.2, 21.5, 25.2, 26.5,
														23.3, 18.3, 13.9, 9.6 ]
											} ]
								});

						jQuery("#chart-container text tspan")
								.each(
										function(s, E) {
											if (jQuery(E).text().toLowerCase() == "highcharts.com") {
												jQuery(E).text("")
											}
										});

					});
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
								class="spanMainHeaderLeft">Dashboard</span></td>
							<td align="right" valign="bottom"><span
								class="spanMainHeaderRight">View data
									from:&nbsp;&nbsp;Last 30 Days&nbsp;&nbsp;|&nbsp;&nbsp;<span
									class="red">Last 6 months</span>&nbsp;&nbsp;|&nbsp;&nbsp;Last
									12 months
							</span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<div id="chart-container"
						style="height: 340px; clear: both; min-width: 946px;"></div>
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
							<td width="50%">
								<table width="100%">
									<tr>
										<td width="50%">
											<table width="100%" cellpadding="3" cellspacing="2">
												<tr>
													<td width="50%"><span class="spanKeyValue">75.12%</span></td>
													<td width="50%"><span class="spanKeyValueDesc">ctr
															this week<br /> <font color="#e42e21">-3.7%</font> this
															period
													</span></td>
												</tr>
											</table>
										</td>
										<td width="50%">
											<table width="100%" cellpadding="3" cellspacing="2">
												<tr>
													<td width="50%"><span class="spanKeyValue">15.13%</span></td>
													<td width="50%"><span class="spanKeyValueDesc">ctr
															this week<br /> <font color="#3cdd03">+8.2%</font> this
															period
													</span></td>
												</tr>
											</table>
										</td>
									</tr>
									<tr>
										<td width="50%">
											<table width="100%" cellpadding="3" cellspacing="2">
												<tr>
													<td width="50%"><span class="spanKeyValue">12.05%</span></td>
													<td width="50%"><span class="spanKeyValueDesc">ctr
															this week<br /> <font color="#3cdd03">+2.9%</font> this
															period
													</span></td>
												</tr>
											</table>
										</td>
										<td width="50%">
											<table width="100%" cellpadding="3" cellspacing="2">
												<tr>
													<td width="50%"><span class="spanKeyValue">23.01%</span></td>
													<td width="50%"><span class="spanKeyValueDesc">ctr
															this week<br /> <font color="#3cdd03">+2.9%</font> this
															period
													</span></td>
												</tr>
											</table>
										</td>
									</tr>
									<tr>
										<td width="50%">
											<table width="100%" cellpadding="3" cellspacing="2">
												<tr>
													<td width="50%"><span class="spanKeyValue">18.05%</span></td>
													<td width="50%"><span class="spanKeyValueDesc">ctr
															this week<br /> <font color="#e42e21">-5.9%</font> this
															period
													</span></td>
												</tr>
											</table>
										</td>
										<td width="50%">
											<table width="100%" cellpadding="3" cellspacing="2">
												<tr>
													<td width="50%"><span class="spanKeyValue">29.01%</span></td>
													<td width="50%"><span class="spanKeyValueDesc">ctr
															this week<br /> <font color="#3cdd03">+3.5%</font> this
															period
													</span></td>
												</tr>
											</table>
										</td>
									</tr>
								</table>
							</td>
							<td width="50%">
								<table width="100%" cellpadding="8" cellspacing="7">
									<tr>
										<td width=20%"><span class="spanKeyValue"><font
												color="#3cdd03">22.13%</font></span></td>
										<td><span class="spanKeyValueDesc">performance
												data here 1 </span></td>
									</tr>
									<tr>
										<td width="20%"><span class="spanKeyValue"><font
												color="#e42e21">31.21%</font></span></td>
										<td><span class="spanKeyValueDesc">performance
												data here 2 </span></td>
									</tr>
									<tr>
										<td width="20%"><span class="spanKeyValue"><font
												color="#3cdd03">22.13%</font></span></td>
										<td><span class="spanKeyValueDesc">performance
												data here 3 </span></td>
									</tr>
								</table>
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