<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<link rel="stylesheet" media="screen" type="text/css" href="../css/colorpicker.css" />
<link href="../css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" href="../css/jquery.snippet.min.css" />

<script type="text/javascript" src="../js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="../js/colorpicker.js"></script>
<script type="text/javascript" src="../js/jquery.snippet.min.js"></script>
<script type="text/javascript" src="../js/common.js"></script>
<script type="text/javascript" src="../js/edit_widget.js"></script>
<script type="text/javascript">
	var widgetsUrl = "edit_widget_async.do";
	var widgetsUrl2 = "edit_widget.do";
	var widgetsUrl3 = "get_widget_items.do";
</script>

</head>

<body onload="getItems();">
	<!--page wrapper-->
	<div class="wrapper">

		<%@ include file="header_menu.jsp"%>
		
		<!-- hidden vars -->
		<input type="hidden" id="hiddenWidgetId" name="hiddenWidgetId" value="${widget.id}" />
		<input type="hidden" id="hiddenBorderColor" name="hiddenBorderColor" value="${widget.borderColor}" />
		<input type="hidden" id="hiddenHeaderBackColor" name="hiddenHeaderBackColor" value="${widget.headerBack}" />
		<input type="hidden" id="hiddenMainColor" name="hiddenMainColor" value="${widget.mainBack}" />
		<input type="hidden" id="hiddenFooterColor" name="hiddenFooterColor" value="${widget.footerBack}" />
		<input type="hidden" id="hiddenHeaderColor" name="hiddenHeaderColor" value="${widget.headerTextColor}" />
		<input type="hidden" id="hiddenNameColor" name="hiddenNameColor" value="${widget.nameTextColor}" />
		<input type="hidden" id="hiddenPriceColor" name="hiddenPriceColor" value="${widget.priceTextColor}" />
		<input type="hidden" id="hiddenCategoryColor" name="hiddenCategoryColor" value="${widget.categoryTextColor}" />
		<%-- <input type="hidden" id="hiddenStockColor" name="hiddenStockColor" value="${widget.stockTextColor}" /> --%>
		
		<!--main-->
		<div class="main-wrapper">
			<div class="container">
				<div id="mainHeader">
					<table width="100%">
						<tr>
							<td align="left" valign="bottom"><span
								class="spanMainHeaderLeft">Edit Widget</span></td>
							<td align="right" valign="bottom"><span class="spanCatalogMainHeaderRight"><div class="error"></div><div class="success"></div>
								<!-- <a href="#" onclick="window.scrollTo(0, document.body.scrollHeight);return false;">Click here to preview your widget (scrolls to the end of the page).</a> -->
								<div class="goback"><a href="widgets.do"><img src='../images/question.png' border='0' style='cursor:pointer' width='8' height='8' />&nbsp;Click here to go back to your widgets list.</a></div>
								</td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<table width="100%" cellpadding="0" cellspacing="0" border="0">
					<tr>
					<td width="50%" valign="top">
						<table width="100%" cellpadding="12" cellspacing="10" border="0">
						<!-- header -->
							<tr><td colspan="2"><span class="label-small-header">Basic Settings</span></td></tr>	
							<!-- header ends-->
							
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Title:</span></td>
							<td valign="middle">
							<input type="text" value="${widget.name}" style="width:240px" class="inputSmall" maxlength="100" id="name" name="name" />	
							</td>
							</tr>
							
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Type:</span></td>
							<td valign="middle">
							<select name="selWidgetType" id="selWidgetType" class="selSmall">
								<c:choose>
									<c:when test='${widget.type=="Most_popular_overall"}'><option value="Most_popular_overall">Most popular overall</option></c:when>
									<c:when test='${widget.type=="Most_popular_in_category"}'><option value="Most_popular_in_category">Most popular in category</option></c:when>
									<c:when test='${widget.type=="Customers_who_viewed_this_also_viewed_that"}'><option value="Customers_who_viewed_this_also_viewed_that">Customers who viewed this, also viewed that</option></c:when>
									<c:when test='${widget.type=="Customers_who_bought_this_also_bought_that"}'><option value="Customers_who_bought_this_also_bought_that">Customers who bought this, also bought that</option></c:when>
									<c:when test='${widget.type=="Recommended_For_You"}'><option value="Recommended_For_You">Recommended For You</option></c:when>
									<c:when test='${widget.type=="Packaged_for_you"}'><option value="Packaged_for_you">Packaged for you</option></c:when>
								</c:choose>
								<c:if test='${widget.type!="Most_popular_overall"}' /><option value="Most_popular_overall">Most popular overall</option>
								<c:if test='${widget.type!="Most_popular_in_category"}' /><option value="Most_popular_in_category">Most popular in category</option>
								<c:if test='${widget.type!="Customers_who_viewed_this_also_viewed_that"}' /><option value="Customers_who_viewed_this_also_viewed_that">Customers who viewed this, also viewed that</option>
								<c:if test='${widget.type!="Customers_who_bought_this_also_bought_that"}' /><option value="Customers_who_bought_this_also_bought_that">Customers who bought this, also bought that</option>
								<c:if test='${widget.type!="Recommended_For_You"}' /><option value="Recommended_For_You">Recommended For You</option>
								<c:if test='${widget.type!="Packaged_for_you"}' /><option value="Packaged_for_you">Packaged for you</option>
							</select>
							</td>
							</tr>

							<tr>
							<td valign="middle" width="35%"><span class="label-small">Description:</span></td>
							<td valign="middle">
							<input type="text" value="${widget.description}" style="width:240px" class="inputSmall" maxlength="100" id="description" name="description" />
							</td>
							</tr>
							
							<tr>
								<td valign="middle" width="35%"><span class="label-small">Font Family:</span></td>
								<td>
								<select name="selMainFontType" id="selMainFontType" class="selSmall" onchange="previewWidget();">
								<%-- <option value="<c:out value="${widget.fontFamily}"/>"><c:out value="${widget.fontFamily}" /></option> --%>
								<c:if test='${widget.fontFamily!="Arial, Helvetica, sans-serif"}' /><option value="Arial, Helvetica, sans-serif">Arial</option>
								<c:if test='${widget.fontFamily!="Arial Black, Gadget, sans-serif"}' /><option value="Arial Black, Gadget, sans-serif">Arial Black</option>
								<c:if test='${widget.fontFamily!="Bookman Old Style, serif"}' /><option value="Bookman Old Style, serif">Bookman Old Style</option>
								<c:if test='${widget.fontFamily!="Comic Sans MS, cursive"}' /><option value="Comic Sans MS, cursive">Comic Sans MS</option>
								<c:if test='${widget.fontFamily!="Courier, monospace"}' /><option value="Courier, monospace">Courier</option>
								<c:if test='${widget.fontFamily!="Courier New, Courier, monospace"}' /><option value="Courier New, Courier, monospace">Courier New</option>
								<c:if test='${widget.fontFamily!="Garamond, serif"}' /><option value="Garamond, serif">Garamond</option>
								<c:if test='${widget.fontFamily!="Georgia, serif"}' /><option value="Georgia, serif">Georgia</option>
								<c:if test='${widget.fontFamily!="Impact, Charcoal, sans-serif"}' /><option value="Impact, Charcoal, sans-serif">Impact</option>
								<c:if test='${widget.fontFamily!="Lucida Console, Monaco, monospace"}' /><option value="Lucida Console, Monaco, monospace">Lucida Console</option>
								<c:if test='${widget.fontFamily!="Lucida Sans Unicode, Lucida Grande, sans-seri"}' /><option value="Lucida Sans Unicode, Lucida Grande, sans-serif">Lucida Sans Unicode</option>
								<c:if test='${widget.fontFamily!="MS Sans Serif, Geneva, sans-serif"}' /><option value="MS Sans Serif, Geneva, sans-serif">MS Sans Serif</option>
								<c:if test='${widget.fontFamily!="MS Serif, New York, sans-serif"}' /><option value="MS Serif, New York, sans-serif">MS Serif</option>
								<c:if test='${widget.fontFamily!="Palatino Linotype, Book Antiqua, Palatino, serif"}' /><option value="Palatino Linotype, Book Antiqua, Palatino, serif">Palatino Linotype</option>
								<c:if test='${widget.fontFamily!="Symbol, sans-serif"}' /><option value="Symbol, sans-serif">Symbol</option>
								<c:if test='${widget.fontFamily!="Tahoma, Geneva, sans-serif"}' /><option value="Tahoma, Geneva, sans-serif">Tahoma</option>
								<c:if test='${widget.fontFamily!="Times New Roman, Times, serif"}' /><option value="Times New Roman, Times, serif">Times New Roman</option>
								<c:if test='${widget.fontFamily!="Trebuchet MS, Helvetica, sans-serif"}' /><option value="Trebuchet MS, Helvetica, sans-serif">Trebuchet MS</option>
								<c:if test='${widget.fontFamily!="Verdana, Geneva, sans-serif"}' /><option value="Verdana, Geneva, sans-serif">Verdana</option>
								<c:if test='${widget.fontFamily!="Webdings, sans-serif"}' /><option value="Webdings, sans-serif">Webdings</option>
								<c:if test='${widget.fontFamily!="Wingdings, Zapf Dingbats, sans-serif"}' /><option value="Wingdings, Zapf Dingbats, sans-serif">Wingdings</option>
								</select>
								</td>
							</tr>

							<tr>
							<td valign="middle" width="35%"><span class="label-small">Header Text:</span></td>
							<td>
										<input
										type="text" name="headerText" id="headerText" maxlength="80"
										class="inputSmall" style="width:240px" value="${widget.headerText}" onkeyup="previewWidget();"/>
                             </td>
							</tr>
						</table>
					</td>
					<td valign="top">
						<table width="100%" cellpadding="12" cellspacing="10" border="0">
							<!-- header -->
							<tr><td colspan="2"><span class="label-small-header">Content and Layout</span></td></tr>	
							<!-- header ends-->
						
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Layout Type:</span></td>
							<td valign="middle">
							<select name="selLayoutType" id="selLayoutType" class="selSmall" onchange="previewWidget();">
							<c:choose>
								<c:when test='${widget.layoutType=="horizontal"}'>
									<option value="horizontal">Horizontal</option>
									<option value="vertical">Vertical</option>
								</c:when>
								<c:otherwise>
									<option value="vertical">Vertical</option>
									<option value="horizontal">Horizontal</option>
								</c:otherwise>
							</c:choose>
							</select>
							</td>
							</tr>
							<!-- space -->
							
							<!-- space ends-->
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Number of Items:</span></td>
							<td valign="middle"><select name="selNoOfItems" id="selNoOfItems" class="selSmallNumber" onchange="getItems();">
							<option value="${widget.noOfItems}" selected="selected">${widget.noOfItems}</option>
							<c:forEach var="i" begin="1" end="20" step="1" varStatus ="noOfItems">
								<c:if test='${widget.noOfItems!=i}'>
								 	<option value="<c:out value="${i}" />"><c:out value="${i}" /></option>
								 </c:if>
							</c:forEach>
							</select></td>
							</tr>
							<!-- space -->
							
							<!-- space ends-->
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Image Size:</span></td>
							<td valign="middle">
							<select name="selImageSize" id="selImageSize" class="selSmallNumber" onchange="previewWidget();">
							<option value="${widget.imageSizeWidth}" selected="selected">${widget.imageSizeWidth}</option>
							<c:if test='${widget.imageSizeWidth!="auto"}'>
							<option value="auto">auto</option>
							</c:if>
							<c:forEach var="j" begin="10" end="400" step="10" varStatus ="imageSizeW">
								 <c:if test='${widget.imageSizeWidth!=j}'>
								 	<option value="<c:out value="${j}" />"><c:out value="${j}" />px</option>
								 </c:if>
							</c:forEach>
							</select>
							&nbsp;&nbsp;<span class="label-small">width</span>&nbsp;&nbsp;&nbsp;&nbsp;
							<select name="selImageSize2" id="selImageSize2" class="selSmallNumber" onchange="previewWidget();">
							<option value="${widget.imageSizeHeight}" selected="selected">${widget.imageSizeHeight}</option>
							<c:if test='${widget.imageSizeHeight!="auto"}'>
							<option value="auto">auto</option>
							</c:if>
							<c:forEach var="k" begin="10" end="400" step="10" varStatus ="imageSizeH">
								<c:if test='${widget.imageSizeHeight!=k}'>
								 	<option value="<c:out value="${k}" />"><c:out value="${k}" />px</option>
								 </c:if>
							</c:forEach>
							</select>
							&nbsp;&nbsp;<span class="label-small">height</span>
							</td>
							</tr>
	
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Widget width:</span></td>
							<td valign="middle"><select name="selTextAreaWidth" id="selTextAreaWidth" class="selSmallNumber" onchange="previewWidget();">
							<option value="${widget.textAreaWidth}" selected="selected">${widget.textAreaWidth}px</option>
							<c:forEach var="l" begin="80" end="1000" step="20" varStatus ="textAreaWidth">
								 <c:if test='${widget.textAreaWidth!=l}'>
								 	<option value="<c:out value="${l}" />"><c:out value="${l}" />px</option>
								 </c:if>
							</c:forEach>
							</select></td>
							</tr>
							
							<tr>
							<td valign="middle" width="35%"><span class="label-small">List width:</span></td>
							<td valign="middle"><select name="selListWidth" id="selListWidth" class="selSmallNumber" onchange="previewWidget();">
							<option value="${widget.listWidth}" selected="selected">${widget.listWidth}%</option>
							<c:forEach var="x" begin="10" end="100" step="5" varStatus ="listWidth">
								 <c:if test='${widget.listWidth!=x}'>
								 	<option value="<c:out value="${x}" />"><c:out value="${x}" />%</option>
								 </c:if>
							</c:forEach>
							</select>&nbsp;&nbsp;<small style="color:#999999">(adjust for list alignment)</small>
							</td>
							</tr>
							
							<tr>
							<td valign="middle" width="35%"><span class="label-small">List spaces:</span></td>
							<td valign="middle"><select name="selListSpaces" id="selListSpaces" class="selSmallNumber" onchange="previewWidget();">
							<option value="${widget.listSpaces}" selected="selected">${widget.listSpaces}px</option>
							<c:forEach var="y" begin="0" end="40" step="1" varStatus ="listSpaces">
								 <c:if test='${widget.listSpaces!=y}'>
								 	<option value="<c:out value="${y}" />"><c:out value="${y}" />px</option>
								 </c:if>
							</c:forEach>
							</select>&nbsp;<small style="color:#999999">(left & right)</small>&nbsp;&nbsp;
							<select name="selListSpaces2" id="selListSpaces2" class="selSmallNumber" onchange="previewWidget();">
							<option value="${widget.listSpaces2}" selected="selected">${widget.listSpaces2}px</option>
							<c:forEach var="z" begin="1" end="40" step="1" varStatus ="listSpaces2">
								 <c:if test='${widget.listSpaces2!=z}'>
								 	<option value="<c:out value="${z}" />"><c:out value="${z}" />px</option>
								 </c:if>
							</c:forEach>
							</select>&nbsp;<small style="color:#999999">(top & bottom)</small></td>
							</tr>
							
						</table>
					</td>
					</tr>
					</table>							


					<!-- ADVANCED SETTINGS  -->
					
					<div id="advancedSettings">
					
					<table width="100%" cellpadding="0" cellspacing="0" border="0">
					<tr>
					<td width="50%" valign="top">
						<table width="100%" cellpadding="12" cellspacing="10" border="0">
							<!-- header -->
							<tr><td colspan="2"><span class="label-small-header">Text Settings</span></td></tr>
							<!-- header ends-->
							
							<tr>
					<td valign="middle" width="35%"><span class="label-small">Header Text Style:</span></td>
					<td>
					<table>
						<tr>
							<td>
								<div id="headerColorSelector" class="color-selector">
									<div style="background-color: #${widget.headerTextColor}"></div>
								</div>
							</td>
							<td>
								<select name="selHeaderFontSize" id="selHeaderFontSize" class="selSmallNumber" onchange="previewWidget();">
								<option value="<c:out value="${widget.headerTextSize}"/>"><c:out value="${widget.headerTextSize}" />px</option>
								<c:forEach var="q" begin="6" end="40" step="1" varStatus="headerFontSize">
										<c:if test='${widget.headerTextSize!=q}'>
										<option value="<c:out value="${q}" />"><c:out value="${q}" />px</option>
										</c:if>
									</c:forEach>
								</select>
	                        </td>
	                        <td>
								<select name="selHeaderFontWeight" id="selHeaderFontWeight" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.headerTextWeight=="normal"}'> 
										<option value="normal">normal</option><option value="lighter">lighter</option><option value="bold">bold</option>
									</c:when>
									<c:when test='${widget.headerTextWeight=="lighter"}'> 
										<option value="lighter">lighter</option><option value="normal">normal</option><option value="bold">bold</option>
									</c:when>
									<c:otherwise>
										<option value="bold">bold</option><option value="normal">normal</option><option value="lighter">lighter</option>
									</c:otherwise>
								</c:choose>
								
								</select>
							</td>
							<td>
								<select name="selHeaderFontAlign" id="selHeaderFontAlign" class="selSmallNumber" onchange="previewWidget();">	
								<c:choose>
									<c:when test='${widget.headerTextAlign=="left"}'> 
										<option value="left">left</option><option value="center">center</option><option value="right">right</option>
									</c:when>
									<c:when test='${widget.headerTextAlign=="center"}'> 
										<option value="center">center</option><option value="left">left</option><option value="right">right</option>
									</c:when>
									<c:otherwise>
										<option value="right">right</option><option value="center">center</option><option value="left">left</option>
									</c:otherwise>
								</c:choose>
								</select>
							</td>
						</tr>
					</table>

					</td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<tr>
					<td valign="middle" width="35%"><span class="label-small">"Name" Text Style:</span></td>
					<td>
					<table>
						<tr>
							<td>
								<div id="nameColorSelector" class="color-selector">
									<div style="background-color: #${widget.nameTextColor}"></div>
								</div>
							</td>
							<td>
								<select name="selNameFontSize" id="selNameFontSize" class="selSmallNumber" onchange="previewWidget();">
								<option value="${widget.nameTextSize}" selected="selected">${widget.nameTextSize}px</option>
								<c:forEach var="b" begin="6" end="40" step="1" varStatus="nameFontSize">
									<c:if test='${widget.nameTextSize!=b}'>
										<option value="<c:out value="${b}" />"><c:out value="${b}" />px</option>
									</c:if>
									</c:forEach>
								</select>
	                        </td>
	                        <td>
								<select name="selNameFontWeight" id="selNameFontWeight" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.nameTextWeight=="normal"}'> 
										<option value="normal">normal</option><option value="lighter">lighter</option><option value="bold">bold</option>
									</c:when>
									<c:when test='${widget.nameTextWeight=="lighter"}'> 
										<option value="lighter">lighter</option><option value="normal">normal</option><option value="bold">bold</option>
									</c:when>
									<c:otherwise>
										<option value="bold">bold</option><option value="normal">normal</option><option value="lighter">lighter</option>
									</c:otherwise>
								</c:choose>
									
								</select>
	                        </td>
	                        <td>
								<select name="selNameFontAlign" id="selNameFontAlign" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.nameTextAlign=="left"}'> 
										<option value="left">left</option><option value="center">center</option><option value="right">right</option>
									</c:when>
									<c:when test='${widget.nameTextAlign=="center"}'> 
										<option value="center">center</option><option value="left">left</option><option value="right">right</option>
									</c:when>
									<c:otherwise>
										<option value="right">right</option><option value="center">center</option><option value="left">left</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
						</tr>
					</table>
					</td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<tr>
					<td valign="middle" width="35%"><span class="label-small">"Price" Text Style:</span></td>
					<td>
					<table>
						<tr>
							<td>
								<div id="priceColorSelector" class="color-selector">
									<div style="background-color: #${widget.priceTextColor}"></div>
								</div>
							</td>
							<td>
								<select name="selPriceFontSize" id="selPriceFontSize" class="selSmallNumber" onchange="previewWidget();">
								<option value="${widget.priceTextSize}" selected="selected">${widget.priceTextSize}px</option>
								<c:forEach var="c" begin="6" end="40" step="1" varStatus="priceFontSize">
									<c:if test='${widget.priceTextSize!=c}'>
										<option value="<c:out value="${c}" />"><c:out value="${c}" />px</option>
									</c:if>
								</c:forEach>
								</select>
	                        </td>
	                        <td>
								<select name="selPriceFontWeight" id="selPriceFontWeight" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.priceTextWeight=="normal"}'> 
										<option value="normal">normal</option><option value="lighter">lighter</option><option value="bold">bold</option>
									</c:when>
									<c:when test='${widget.priceTextWeight=="lighter"}'> 
										<option value="lighter">lighter</option><option value="normal">normal</option><option value="bold">bold</option>
									</c:when>
									<c:otherwise>
										<option value="bold">bold</option><option value="normal">normal</option><option value="lighter">lighter</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
	                        <td>
								<select name="selPriceFontAlign" id="selPriceFontAlign" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.priceTextAlign=="left"}'> 
										<option value="left">left</option><option value="center">center</option><option value="right">right</option>
									</c:when>
									<c:when test='${widget.priceTextAlign=="center"}'> 
										<option value="center">center</option><option value="left">left</option><option value="right">right</option>
									</c:when>
									<c:otherwise>
										<option value="right">right</option><option value="center">center</option><option value="left">left</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
						</tr>
					</table>
					</td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<tr>
					<td valign="middle" width="35%"><span class="label-small">"Category" Text Style:</span></td>
					<td>
					<table>
						<tr>
							<td>
								<div id="categoryColorSelector" class="color-selector">
									<div style="background-color: #${widget.categoryTextColor}"></div>
								</div>
							</td>
							<td>
								<select name="selCategoryFontSize" id="selCategoryFontSize" class="selSmallNumber" onchange="previewWidget();">
								<option value="${widget.categoryTextSize}" selected="selected">${widget.categoryTextSize}px</option>
								<c:forEach var="e" begin="6" end="40" step="1" varStatus="categoryFontSize">
									<c:if test='${widget.categoryTextSize!=e}'>
										<option value="<c:out value="${e}" />"><c:out value="${e}" />px</option>
									</c:if>
								</c:forEach>
								</select>
	                        </td>
	                        <td>
								<select name="selCategoryFontWeight" id="selCategoryFontWeight" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.categoryTextWeight=="normal"}'> 
										<option value="normal">normal</option><option value="lighter">lighter</option><option value="bold">bold</option>
									</c:when>
									<c:when test='${widget.categoryTextWeight=="lighter"}'> 
										<option value="lighter">lighter</option><option value="normal">normal</option><option value="bold">bold</option>
									</c:when>
									<c:otherwise>
										<option value="bold">bold</option><option value="normal">normal</option><option value="lighter">lighter</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
	                        <td>
								<select name="selCategoryFontAlign" id="selCategoryFontAlign" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.categoryTextAlign=="left"}'> 
										<option value="left">left</option><option value="center">center</option><option value="right">right</option>
									</c:when>
									<c:when test='${widget.categoryTextAlign=="center"}'> 
										<option value="center">center</option><option value="left">left</option><option value="right">right</option>
									</c:when>
									<c:otherwise>
										<option value="right">right</option><option value="center">center</option><option value="left">left</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
						</tr>
					</table>
					</td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<%-- <tr>
					<td valign="middle" width="35%"><span class="label-small">"In Stock" Text Style:</span></td>
					<td>
					<table>
						<tr>
							<td>
								<div id="stockColorSelector" class="color-selector">
									<div style="background-color: #${widget.stockTextColor}"></div>
								</div>
							</td>
							<td>
								<select name="selStockFontSize" id="selStockFontSize" class="selSmallNumber" onchange="previewWidget();">
								<option value="${widget.stockTextSize}" selected="selected">${widget.stockTextSize}px</option>
								<c:forEach var="d" begin="6" end="40" step="1" varStatus="stockFontSize">
									<c:if test='${widget.stockTextSize!=d}'>
										<option value="<c:out value="${d}" />"><c:out value="${d}" />px</option>
									</c:if>
								</c:forEach>
								</select>
	                        </td>
	                        <td>
								<select name="selStockFontWeight" id="selStockFontWeight" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.stockTextWeight=="normal"}'> 
										<option value="normal">normal</option><option value="lighter">lighter</option><option value="bold">bold</option>
									</c:when>
									<c:when test='${widget.stockTextWeight=="lighter"}'> 
										<option value="lighter">lighter</option><option value="normal">normal</option><option value="bold">bold</option>
									</c:when>
									<c:otherwise>
										<option value="bold">bold</option><option value="normal">normal</option><option value="lighter">lighter</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
	                        <td>
								<select name="selStockFontAlign" id="selStockFontAlign" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.stockTextAlign=="left"}'> 
										<option value="left">left</option><option value="center">center</option><option value="right">right</option>
									</c:when>
									<c:when test='${widget.stockTextAlign=="center"}'> 
										<option value="center">center</option><option value="left">left</option><option value="right">right</option>
									</c:when>
									<c:otherwise>
										<option value="right">right</option><option value="center">center</option><option value="left">left</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
						</tr>
					</table>
					</td>
					</tr> --%>
					
					<tr>
					<td valign="middle" width="35%"><span class="label-small">List Text Display:</span></td>
					<td>
					<table>
						<tr>
							<td>
								<select name="selListDisplay" id="selListDisplay" class="selSmallNumber" onchange="previewWidget();">
								<c:choose>
									<c:when test='${widget.listDisplay=="block"}'> 
										<option value="block">Block</option><option value="inline">Inline</option>
									</c:when>
									<c:otherwise>
										<option value="inline">Inline</option><option value="block">Block</option>
									</c:otherwise>
								</c:choose>
								</select>
	                        </td>
						</tr>
					</table>
					</td>
					</tr>
					
					
							<!-- space -->
							<tr><td colspan="2">&nbsp;</td></tr>
							<!-- space ends-->
							<!-- header -->
							<tr><td colspan="2"><span class="label-small-header">Background & Borders</span></td></tr>
							
							<!-- header ends-->
							<!-- space -->
							
							<!-- space ends-->
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Border:</span></td>
							<td>
							<table>
								<tr>
									<td>
										<div id="borderColorSelector" class="color-selector">
											<div style="background-color: #${widget.borderColor}"></div>
										</div>
									</td>
									<td>&nbsp;<select name="selBorderWidth" id="selBorderWidth"
										class="selSmallNumber" onchange="previewWidget();">
										<option value="${widget.borderWidth}" selected="selected">${widget.borderWidth}px</option>
											<c:forEach var="m" begin="0" end="3" step="1"
												varStatus="borderWidth">
												<c:if test='${widget.borderWidth!=m}'>
												<option value="<c:out value="${m}" />"><c:out value="${m}" />px</option>
												</c:if>
											</c:forEach>
									</select></td>
								</tr>
							</table>
							</td>
							</tr>
							<!-- space -->
							
							<!-- space ends-->
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Header Background:</span></td>
							<td>
							<table>
								<tr>
									<td>
										<div id="headerBackColorSelector" class="color-selector">
											<div style="background-color: #${widget.headerBack}"></div>
										</div>
									</td>
									<td>
										<div id="divRdHeaderTrans">
											<c:choose>
												<c:when test='${widget.headerBackTrans==1}'> 
													<input type="radio" id="rdHeaderTrans1" name="rdHeaderTrans" value="1" checked="checked"  onclick="previewWidget();" /><label for="rdHeaderTrans1">Transparent</label>
		                                    		<input type="radio" id="rdHeaderTrans0" name="rdHeaderTrans" value="0"  onclick="previewWidget();" /><label for="rdHeaderTrans0">No</label>
												</c:when>
												<c:otherwise>
													<input type="radio" id="rdHeaderTrans1" name="rdHeaderTrans" value="1"  onclick="previewWidget();" /><label for="rdHeaderTrans1">Transparent</label>
		                                    		<input type="radio" id="rdHeaderTrans0" name="rdHeaderTrans" value="0"  onclick="previewWidget();" checked="checked"/><label for="rdHeaderTrans0">No</label>
												</c:otherwise>
											</c:choose>
			                            </div>
			                        </td>
								</tr>
							</table>
							</td>
							</tr>
							
						</table>
					</td>
					<td valign="top">
						<table width="100%" cellpadding="12" cellspacing="10" border="0">
							

							<tr><td colspan="2"><span class="label-small-header">Other Settings</span></td></tr>
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Show header?</span></td>
							<td><div id="divRdShowHeader">
							<c:choose>
								<c:when test='${widget.showHeader==1}'> 
									<input type="radio" id="rdShowHeader1" name="rdShowHeader" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowHeader1">Yes</label>
	                                <input type="radio" id="rdShowHeader0" name="rdShowHeader" value="0"  onclick="previewWidget();" /><label for="rdShowHeader0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowHeader1" name="rdShowHeader" value="1"  onclick="previewWidget();" /><label for="rdShowHeader1">Yes</label>
	                                <input type="radio" id="rdShowHeader0" name="rdShowHeader" value="0" checked="checked"  onclick="previewWidget();" /><label for="rdShowHeader0">No</label>
								</c:otherwise>
							</c:choose>	
                                </div>
                             </td>
							</tr>
							<!-- space -->
							
							<!-- space ends-->
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Show images?</span></td>
							<td><div id="divRdShowImages">
							<c:choose>
								<c:when test='${widget.showImages==1}'> 
									<input type="radio" id="rdShowImages1" name="rdShowImages" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowImages1">Yes</label>
                                    <input type="radio" id="rdShowImages0" name="rdShowImages" value="0"  onclick="previewWidget();" /><label for="rdShowImages0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowImages1" name="rdShowImages" value="1"  onclick="previewWidget();" /><label for="rdShowImages1">Yes</label>
                                    <input type="radio" id="rdShowImages0" name="rdShowImages" value="0"  onclick="previewWidget();" checked="checked" /><label for="rdShowImages0">No</label>
								</c:otherwise>
							</c:choose>
                                    
                                </div>
                             </td>
							</tr>
							<!-- space -->
							
							<!-- space ends-->
							<tr>
							<td valign="middle" width="35%"><span class="label-small">Show Cleargist's logo?</span></td>
							<td><div id="divRdShowLogo">
							<c:choose>
								<c:when test='${widget.showClearGistLogo==1}'> 
									<input type="radio" id="rdShowLogo1" name="rdShowLogo" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowLogo1">Yes</label>
                                    <input type="radio" id="rdShowLogo0" name="rdShowLogo" value="0"  onclick="previewWidget();" /><label for="rdShowLogo0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowLogo1" name="rdShowLogo" value="1"  onclick="previewWidget();" /><label for="rdShowLogo1">Yes</label>
                                    <input type="radio" id="rdShowLogo0" name="rdShowLogo" value="0" checked="checked"  onclick="previewWidget();"/><label for="rdShowLogo0">No</label>
								</c:otherwise>
							</c:choose>
                                </div>
                             </td>
							</tr>
					<tr>
					<td valign="middle" width="35%"><span class="label-small">Show "name"?</span></td>
					<td><div id="divRdShowName">
							<c:choose>
								<c:when test='${widget.showName==1}'> 
									<input type="radio" id="rdShowName1" name="rdShowName" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowName1">Yes</label>
                                  	<input type="radio" id="rdShowName0" name="rdShowName" value="0"  onclick="previewWidget();" /><label for="rdShowName0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowName1" name="rdShowName" value="1"  onclick="previewWidget();"/><label for="rdShowName1">Yes</label>
                                  <input type="radio" id="rdShowName0" name="rdShowName" value="0" checked="checked"  onclick="previewWidget();" /><label for="rdShowName0">No</label>
								</c:otherwise>
							</c:choose>
                                  
                              </div>
                           </td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<tr>
					<td valign="middle" width="35%"><span class="label-small">Show "price"?</span></td>
					<td><div id="divRdShowPrice">
							<c:choose>
								<c:when test='${widget.showPrice==1}'> 
									<input type="radio" id="rdShowPrice1" name="rdShowPrice" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowPrice1">Yes</label>
                                  <input type="radio" id="rdShowPrice0" name="rdShowPrice" value="0"  onclick="previewWidget();" /><label for="rdShowPrice0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowPrice1" name="rdShowPrice" value="1"  onclick="previewWidget();"/><label for="rdShowPrice1">Yes</label>
                                  <input type="radio" id="rdShowPrice0" name="rdShowPrice" value="0" checked="checked"  onclick="previewWidget();" /><label for="rdShowPrice0">No</label>
								</c:otherwise>
							</c:choose>
                                  
                              </div>
                           </td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<tr>
					<td valign="middle" width="35%"><span class="label-small">Show "category"?</span></td>
					<td><div id="divRdShowCategory">
							<c:choose>
								<c:when test='${widget.showCategory==1}'> 
									<input type="radio" id="rdShowCategory1" name="rdShowCategory" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowCategory1">Yes</label>
                                  <input type="radio" id="rdShowCategory0" name="rdShowCategory" value="0"  onclick="previewWidget();" /><label for="rdShowCategory0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowCategory1" name="rdShowCategory" value="1"  onclick="previewWidget();"/><label for="rdShowCategory1">Yes</label>
                                  <input type="radio" id="rdShowCategory0" name="rdShowCategory" value="0" checked="checked"  onclick="previewWidget();" /><label for="rdShowCategory0">No</label>
								</c:otherwise>
							</c:choose>
                                  
                              </div>
                           </td>
					</tr>
					<!-- space -->
					
					<!-- space ends-->
					<%-- <tr>
					<td valign="middle" width="35%"><span class="label-small">Show "in stock"?</span></td>
					<td><div id="divRdShowStock">
							<c:choose>
								<c:when test='${widget.showStock==1}'> 
									<input type="radio" id="rdShowStock1" name="rdShowStock" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdShowStock1">Yes</label>
                                  <input type="radio" id="rdShowStock0" name="rdShowStock" value="0"  onclick="previewWidget();" /><label for="rdShowStock0">No</label>
								</c:when>
								<c:otherwise>
									<input type="radio" id="rdShowStock1" name="rdShowStock" value="1"  onclick="previewWidget();"/><label for="rdShowStock1">Yes</label>
                                  <input type="radio" id="rdShowStock0" name="rdShowStock" value="0" checked="checked"  onclick="previewWidget();" /><label for="rdShowStock0">No</label>
								</c:otherwise>
							</c:choose>
                                  
                              </div>
                           </td>
					</tr> --%>
					
					<tr><td colspan="2">&nbsp;</td></tr>
					
						<tr>
						<td valign="middle" width="35%"><span class="label-small">Main Background:</span></td>
						<td>
						<table>
							<tr>
								<td>
									<div id="mainBackColorSelector" class="color-selector">
										<div style="background-color: #${widget.mainBack}"></div>
									</div>
								</td>
								<td>
									<div id="divRdMainTrans">
										<c:choose>
											<c:when test='${widget.mainBackTrans==1}'> 
												<input type="radio" id="rdMainTrans1" name="rdMainTrans" value="1" checked="checked"  onclick="previewWidget();"/><label for="rdMainTrans1">Transparent</label>
	                                    		<input type="radio" id="rdMainTrans0" name="rdMainTrans" value="0"  onclick="previewWidget();" /><label for="rdMainTrans0">No</label>
											</c:when>
											<c:otherwise>
												<input type="radio" id="rdMainTrans1" name="rdMainTrans" value="1"  onclick="previewWidget();" /><label for="rdMainTrans1">Transparent</label>
	                                    		<input type="radio" id="rdMainTrans0" name="rdMainTrans" value="0" checked="checked"  onclick="previewWidget();"/><label for="rdMainTrans0">No</label>
											</c:otherwise>
										</c:choose>
	                                    
		                            </div>
		                        </td>
							</tr>
						</table>
						</td>
						</tr>
							
						<tr>
						<td valign="middle" width="35%"><span class="label-small">Footer Background:</span></td>
						<td>
						<table>
							<tr>
								<td>
									<div id="footerBackColorSelector" class="color-selector">
										<div style="background-color: #${widget.footerBack}"></div>
									</div>
								</td>
								<td>
									<div id="divRdFooterTrans">
										<c:choose>
											<c:when test='${widget.footerBackTrans==1}'> 
												<input type="radio" id="rdFooterTrans1" name="rdFooterTrans" value="1" checked="checked"  onclick="previewWidget();" /><label for="rdFooterTrans1">Transparent</label>
	                                    		<input type="radio" id="rdFooterTrans0" name="rdFooterTrans" value="0"  onclick="previewWidget();" /><label for="rdFooterTrans0">No</label>
											</c:when>
											<c:otherwise>
												<input type="radio" id="rdFooterTrans1" name="rdFooterTrans" value="1"  onclick="previewWidget();" /><label for="rdFooterTrans1">Transparent</label>
	                                    		<input type="radio" id="rdFooterTrans0" name="rdFooterTrans" value="0" checked="checked"  onclick="previewWidget();"/><label for="rdFooterTrans0">No</label>
											</c:otherwise>
										</c:choose>
	                                    
		                            </div>
		                        </td>
							</tr>
						</table>
						</td>
						</tr>
					
					</table>
					</td>
					</tr>
					</table>
					
					</div>
					
					
					<table width="100%" cellpadding="12" cellspacing="10" border="0">
					<!-- space -->
					
					<!-- space ends-->
					<!-- line -->
					<!-- <tr><td colspan="4"><div id="menu_seperator_top"></div></td></tr> -->
					<tr><td align="center"><a href="/" onclick="showAdvancedSettings();return false;"><img height="8" border="0" width="8" style="cursor:pointer" src="../images/preview.png">&nbsp;Show/Hide Advanced Settings</a></td></tr>
					<tr><td align="center">
					<!-- <a href="/" onclick="clearChanges();">Clear Changes</a>&nbsp;&nbsp;&nbsp; -->
					<a href="/" class="small-button-w" onclick="clearChanges();return false;"><span class="small-button-span-w">Clear</span></a>&nbsp;&nbsp;&nbsp;
					<a href="/" class="small-button-b" onclick="$('#dialogCode').dialog('open');return false;"><span class="small-button-span-b">Get Code</span></a>&nbsp;&nbsp;&nbsp;
					<a href="/" class="small-button" onclick="editWidget(this);return false;"><span class="small-button-span">Save Widget</span></a></td></tr>
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
								class="spanMainHeaderLeft">Preview</span></td>
							<td align="right" valign="bottom" width="50%">
							<table>
								<tr>
									<td valign="bottom">
										<span class="label-small">Set Preview Area's Background Color:&nbsp;&nbsp;</span>
									</td>
									<td>
										<div id="previewBackColorSelector" class="color-selector">
											<div style="background-color: #ffffff"></div>
										</div>
			                        </td>
								</tr>
							</table>
							</td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
<div id="previewArea" align="center">
<br clear="all" />
<div id="clearGistWidget" style="font-family:Arial, Helvetica, sans-serif; width:800px; border: 1px solid #000000;overflow: hidden;" align="center">
<div id="clearGistWidgetHeader" style="color:#333333;font-size:14px;font-weight:bold;text-align:center;background-color:#ffffff;padding-top:10px;padding-bottom:10px;">
<span id="clearGistWidgetHeaderTxt">People also bought...</span></div>
<div id="clearGistWidgetMain" style="background-color:#ffffff;width:800px;padding-top:10px;padding-bottom:10px;float:right;position:relative;">
<ul id="clearGistWidgetUl" style="list-style:none;position:relative;width: 65%;">

<!-- <li style="float:left;overflow: hidden;position:relative;padding: 0 14px 0 14px;">
	<a href="http://www.plaisio.gr/Sound-Vision/Home-Cinema/Televisions/Turbo-X-TXV-215FH-LED-TV-21-5-22L13A.htm">
	<span class="clearGistWidgetImgSpan" style="display:block;"><img src="http://www.plaisio.gr/ProductImages/250x250/1695304.JPG" border="0" width="100" height="auto"/></span>
	<span class="clearGistWidgetNameSpan" style="display:block;color:#E42E21;font-size:12px;font-wight:normal;text-align:center;">LG Monitor 23'</span>
	<span class="clearGistWidgetCategorySpan" style="display:block;color:#000000;font-size:12px;font-weight:normal;text-align:center;">PC & Monitors</span>
	<span class="clearGistWidgetPriceSpan" style="display:block;color:#000000;font-size:12px;font-weight:bold;text-align:center;">&euro;354</span>
	<span class="clearGistWidgetStockSpan" style="display:block;color:#000000;font-size:12px;font-weight:normal;text-align:center;">only 2 left!</span></a>
</li>
<li style="float:left;overflow: hidden;position:relative;padding: 0 14px 0 14px;">
	<a href="http://www.plaisio.gr/Laptop-and-Tablet/Notebook/Laptop/Turbo-X-Steel-D-820-432.htm">
	<span class="clearGistWidgetImgSpan" style="display:block;"><img src="http://www.plaisio.gr/ProductImages/250x250/1736744.JPG" border="0" width="100" height="auto"/></span>
	<span class="clearGistWidgetNameSpan" style="display:block;color:#E42E21;font-size:12px;font-weight:normal;text-align:center;">50 inch Hard Drive</span>
	<span class="clearGistWidgetCategorySpan" style="display:block;color:#000000;font-size:12px;font-weight:normal;text-align:center;">PC & Hardware</span>
	<span class="clearGistWidgetPriceSpan" style="display:block;color:#000000;font-size:12px;font-weight:bold;text-align:center;">&euro;291</span>
	<span class="clearGistWidgetStockSpan" style="display:block;color:#000000;font-size:12px;font-weight:normal;text-align:center;">only 1 left!</span></a>
</li>
<li style="float:left;overflow: hidden;position:relative;padding: 0 14px 0 14px;">
	<a href="http://www.plaisio.gr/Gaming/Consoles/Microsoft-XBOX360/Microsoft-XBOX-360-4GB-Kinect-Holiday-2011-Value-Bundle-R7G-00008.htm">
	<span class="clearGistWidgetImgSpan" style="display:block;"><img src="http://www.plaisio.gr/ProductImages/250x250/1724258.JPG" border="0" width="100" height="auto"/></span>
	<span class="clearGistWidgetNameSpan" style="display:block;color:#E42E21;font-size:12px;font-weight:normal;text-align:center;">Panasonic TV 42'</span>
	<span class="clearGistWidgetCategorySpan" style="display:block;color:#000000;font-size:12px;font-weight:normal;text-align:center;">TV</span>
	<span class="clearGistWidgetPriceSpan" style="display:block;color:#000000;font-size:12px;font-weight:bold;text-align:center;">&euro;1200</span>
	<span class="clearGistWidgetStockSpan" style="display:block;color:#000000;font-size:12px;font-weight:normal;text-align:center;">only 8 left!</span></a>
</li>
<li style="float:left;overflow: hidden;position:relative;padding: 0 14px 0 14px;">
	<a href="http://www.plaisio.gr/Peripherals/PC-Peripherals/Keyboards/Turbo-X-NumPad-WND100-JT-JP0027.htm">
	<span class="clearGistWidgetImgSpan" style="display:block;"><img src="http://www.plaisio.gr/ProductImages/250x250/1685287.JPG" border="0" width="100" height="auto"/></span>
	<span class="clearGistWidgetNameSpan" style="display:block;color:#E42E21;font-size:12px;font-wight:normal;text-align:center;">Samsung Blue Ray</span>
	<span class="clearGistWidgetCategorySpan" style="display:block;color:#000000;font-size:12px;font-wight:normal;text-align:center;">Blue Rays</span>
	<span class="clearGistWidgetPriceSpan" style="display:block;color:#000000;font-size:12px;font-wight:bold;text-align:center;">&euro;123</span>
	<span class="clearGistWidgetStockSpan" style="display:block;color:#000000;font-size:12px;font-wight:normal;text-align:center;">only 5 left!</span></a>
</li> -->
	</ul>
</div>
<br clear="all" />
<div id="clearGistWidgetFooter" style="color:#666666;font-size:10px;text-align:center;background-color:#ffffff;width:800px;padding-top:5px;padding-bottom:10px;">
		powered by <br /><a href="http://www.cleargist.com" target="_blank"><img src="http://cleargist.elasticbeanstalk.com/images/logo_small.png" border="0"/></a>
		</div>
	</div>
</div>
				</div>
			</div>
		</div>
		<!--sub-main ends-->
		
		<!--sub-main-->
		<!-- <div class="sub-main-wrapper">
			<div class="container">
				<div id="subMainHeader">
					<table width="100%">
						<tr>
							<td align="left" valign="bottom" width="50%"><span
								class="spanMainHeaderLeft">Code</span></td>
							<td align="right" valign="bottom"><span class="spanCatalogMainHeaderRight">
								 <div class="goback"><img src='../images/question.png' border='0' style='cursor:pointer' width='8' height='8' />&nbsp;When your done just copy and paste the code into your site.</div>
							</td>
						</tr>
					</table>
					<div id="menu_seperator_top"></div>
				</div>
				<div id="mainBox">
					<pre id="widgetCode"></pre>
				</div>
			</div>
		</div> -->
		<!--sub-main ends-->
		<!--hidden code-->
		<div id="previewAreaHidden" style="display: none;">
			<div id="CGW"
				style="font-family: Arial, Helvetica, sans-serif; width: 800px; border: 1px solid #000000; overflow: hidden; display: none;" align="center" >
				<input type="hidden" id="CGWToken" name="CGWToken" value="${tenant.token}" />
				<input type="hidden" id="CGWType" name="CGWType" value="${widget.type}" />
				<input type="hidden" id="CGWItems" name="CGWItems" value="${widget.noOfItems}" />
				<div id="CGWHeader"
					style="color: #333333; font-size: 14px; font-weight: bold; text-align: center; background-color: #ffffff; padding-top: 10px; padding-bottom: 10px;">
					<span id="CGWHeaderTxt">People also bought...</span>
				</div>
				<div id="CGWMain"
					style="background-color: #ffffff; width: 800px; padding-top: 10px; padding-bottom: 10px; float: right; position: relative;">
					<ul id="CGWUl"
						style="list-style: none; position: relative; width: 65%;">
					</ul>
				</div>
				<br clear="all" />
				<div id="CGWFooter"
					style="color: #666666; font-size: 10px; text-align: center; background-color: #ffffff; width: 800px; padding-top: 5px; padding-bottom: 10px;">
					powered by <br />
					<a href="http://www.cleargist.com" target="_blank"><img
						src="http://cleargist.elasticbeanstalk.com/images/logo_small.png" border="0" width="60" height="8"/></a>
				</div>
			</div>
		</div>
		<!--hidden code ends-->
		
		<div id="dialogCode" title="Copy and paste the following code to your website">
			<pre id="widgetCode"></pre>
		</div>
	
		<br clear="all" />
	</div>
	<!--wrapper ends-->

	<%@ include file="footer.jsp"%>
	
</body>
</html>