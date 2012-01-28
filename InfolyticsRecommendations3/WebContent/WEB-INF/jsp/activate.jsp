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

<meta http-equiv="refresh" content="10;url=http://cleargist.elasticbeanstalk.com/admin/login.do" />

</head>

<body>

	<%@ include file="header_menu.jsp"%>

	<!--main quotes-->
	<div class="container" align="center">
		<div class="main" align="left">

			<div id="promo">
				<h2 class="title">
					You have successfully activated your ClearGist account! Please login to the <a href="/admin/login.do">administration menu</a>, in order to proceed with the 'customer subscription' process. Thank you for choosing ClearGist's Personalization Services.
					<br /><br /><span style="font-size: 16px;">You are redirected to the <a href="/admin/login.do">administration menu</a>... If you are not redirected automatically (after 10 seconds), please click on the following link: <a href="/admin/login.do">http://cleargist.elasticbeanstalk.com/admin/login.do</a>.</span><br />
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