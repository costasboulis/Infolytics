<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Personalization Services</title>
<meta name="keywords"
	content="recommendation, personalization, recommendation engine, amazon, product recommendations, shopping, ecommerce" />
<meta name="description"
	content="Personalize your customer&#039;s experience from start to finish with the Infolytics Recommendation Engine." />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/front.css" rel="stylesheet" type="text/css" />
</head>

<body>

	<%@ include file="header_menu.jsp"%>

	<!--main quotes-->
	<div class="container" align="center">
		<div class="main" align="left">

			<div id="promo">
				<h2 class="title">
					Your request has <span class="mark">NOT</span> been processed. <%= request.getParameter("error") %>.<br /> Please <a href="http://www.cleargist.com/contact.php"><span
						class="mark">CONTACT US</span></a> or try again. 
				</h2>
				<div class="content">
					<div class="quote">
						<p>
							Get in touch by using the information below or contact us using
							the form.<br /> <br /> <b>General Inquires: </b>+30 211
							0139971, info@cleargist.com<br /> <b>Sales: </b>+30 211 0139971,
							info@cleargist.com<br /> <b>Careers: </b>+30 211 0139971,
							info@cleargist.com
						</p>
					</div>
				</div>
			</div>

		</div>
	</div>


	<%@ include file="footer.jsp"%>

</body>
</html>