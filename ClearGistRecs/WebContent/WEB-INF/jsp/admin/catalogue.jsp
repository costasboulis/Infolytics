<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />
<%
	String selectedMenu = "catalogue";
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


<script type="text/javascript" src="../js/jquery-1.6.2.min.js"></script>
<script type="text/javascript"
	src="../js/jquery-ui-1.8.16.custom.min.js"></script>
<script src="../js/jquery.jqGrid-3.7.2/js/i18n/grid.locale-en.js"
	type="text/javascript"></script>
<script src="../js/jquery.jqGrid-3.7.2/js/jquery.jqGrid.min.js"
	type="text/javascript"></script>
<script type="text/javascript" src="../js/catalog.js"></script>
<script type="text/javascript">
	var catalogUrl1 = "get_catalog_items.do";
</script>
<script type="text/javascript">
	var catalogUrl2 = "check_catalog_status.do";
</script>
</head>

<body>

<!--hidden status-->
<input type="hidden" id="hiddenCatalogStatus" name="hiddenCatalogStatus" value="${tenant.catalogStatus}" />

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
								class="spanMainHeaderLeft">Catalog</span></td>
							<td align="right" valign="bottom"><span
								class="spanCatalogMainHeaderRight"> 
									<span id="spanCatalogStatus">
									<c:choose>
										<c:when test='${tenant.catalogStatus=="SYNCING"}'>
											Uploading new catalog&nbsp;&nbsp;&nbsp;<img src="../images/ajax-loader.gif" width="10" />
										</c:when>
										<%-- <c:when test='${tenant.catalogStatus=="INSYNC"}'>
											<font color="#3cdd03">OK</font>
										</c:when> --%>
										<c:when test='${tenant.catalogStatus=="FAILED"}'>
											Latest catalog upload was <font color="#E42E21"><b>FAILED</b></font>. ${tenant.catalogStatusMessage}.
										</c:when>
										<%-- <c:otherwise>
											<font color="#E42E21">AWAITING</font>
										</c:otherwise> --%>
									</c:choose>
									</span>
								
								<span id="spanCatalogStatusDesc">
								<c:choose>
											<c:when test='${tenant.catalogStatus=="SYNCING"}'></c:when>
											<c:otherwise>&nbsp;&nbsp;<a href="/"
						class="x-small-button-2" onclick="$('#dialogUpload').dialog('open'); return false;"><span
						class="x-small-button-span">Upload Catalog</span></a>
											</c:otherwise>
										</c:choose>
								</span>
							</span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<span class="label-small">Search</span> &nbsp;&nbsp;<select
						name="selSearchcat" id="selSearchcat" class="selSmall"><option
							value="item">Item</option>
						<option value="category">Category</option>
						<option value="description">Description</option></select> &nbsp;&nbsp;<span
						class="label-small"> for </span>&nbsp;&nbsp;
						
						<input
						type="text" name="catSearchKey" id="catSearchKey" maxlength="50"
						class="inputSmall" value="Search Keyword..." onkeyup="searchCatalog();" onblur="if(this.value=='')this.value='Search Keyword...';" onfocus="if(this.value=='Search Keyword...')this.value='';" /> 
						
						<!-- &nbsp;&nbsp;<a href="/"
						class="x-small-button" onclick="searchCatalog(this);return false;"><span
						class="x-small-button-span">Search</span></a> --> <!-- &nbsp;&nbsp;<a href="/"
						onclick="clearCatalog(this);return false;">Clear Search</a>  --><br />
					<br />
					<table id='tblCatalog'></table>
					<div id="divCatalogGridPager"></div>
				</div>
			</div>
		</div>
		<!--main ends-->
		<!--sub-main-->
		<!-- <div class="sub-main-wrapper">
			<div class="container">
				<div id="subMainHeader">
					<table width="100%">
						<tr>
							<td align="left" valign="bottom" width="50%"><span
								class="spanMainHeaderLeft">Sub Menu</span></td>
							<td align="left" valign="bottom" width="50%"><span
								class="spanMainHeaderLeft"></span></td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					
				</div>
			</div>
		</div> -->
		<!--sub-main ends-->
		<br clear="all" />
	</div>
	<!--wrapper ends-->
	<div id="dialogPhoto" title="Item Image">
		<table width="100%" cellpadding="4" cellspacing="3">
			<tr>
				<td align="center" valign="middle"><span id="spanPhoto"></span></td>
			</tr>
		</table>
	</div>
	<div id="dialogUpload" title="Upload a new Catalog">
		<br /><table width="100%" cellpadding="4" cellspacing="3">
			<tr>
				<td align="center" valign="middle">
					<form name="frmUploadCatalog" id="frmUploadCatalog"
						action="uploadCatalog.do" method="post"
						enctype="multipart/form-data">
						<input type="hidden" id="tenantToken" name="tenantToken"
							value="${tenant.token}" /> <input type="hidden" id="tenantId"
							name="tenantId" value="${tenant.id}" /> <span class="label">XML file to upload: </span>&nbsp;<input name="file" id="file" type="file" /> 
							<br /><br /> <a href="#"
							class="x-small-button"
							onclick="uploadCatalog(this);return false;"> <span
							class="x-small-button-span">Upload</span></a>
							<!-- <input type="submit" value="Upload" /> -->
					</form>
				</td>
			</tr>
		</table>
	</div>
	<%@ include file="footer.jsp"%>

</body>
</html>