<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="date" class="java.util.Date" />


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Personalization Services</title>
<meta name="keywords"
	content="recommendation, personalization, recommendation engine, amazon, product recommendations, shopping, ecommerce" />
<meta name="description"
	content="Personalize your customer&#039;s experience from start to finish with the cleargist Recommendation Engine." />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/front.css" rel="stylesheet" type="text/css" />
<link href="css/colorbox.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/jquery-1.5.1.min.js"></script>
<script src="js/jquery.colorbox-min.js"></script>
<script type="text/javascript" src="js/signup.js"></script>
<script type="text/javascript">var checkUserUrl = "<c:out value='${contexturl}' />check_username.do";</script>
</head>

<body>

	<%@ include file="header_menu.jsp"%>


	<!--main quotes-->
	<div class="container" align="center">
		<div class="main" align="left">

			<div id="promo">
				<h2 class="title">
					Sign up and start gaining <span class="mark">more revenue!</span>
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

	<!--contact-->
	<div class="container" align="center">
		<div class="contact" align="left">
			<div class="error"></div>
			<div class="success"></div>
			
			<form:form modelAttribute="tenant" name="frmSignUp" id="frmSignUp" action="signup.do" method="post">
			<%-- <form name="frmSignUp" id="frmSignUp" action="signupresults.do"
				method="post"> --%>
				<table>
					<tr>
						<td width="70%">


							<table>

								<tr>
									<td valign="top" width="150">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">FIRST NAME</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="firstName" id="firstname"
														name="firstname" maxlength="100"/>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="top" width="150">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">LAST NAME</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="lastName" id="lastname"
														name="lastname" maxlength="100"/>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">PHONE</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="phone" id="phone"
														name="phone" maxlength="100"/>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">EMAIL</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="email" id="email"
														name="email" maxlength="100"/>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">COMPANY NAME</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="company" id="company"
														name="company" maxlength="100"/>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">SITE URL</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="url" id="siteUrl"
															name="siteUrl" maxlength="100" value="http://"/>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">USERNAME</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:input path="username" id="username"
															name="username" maxlength="100"/>
															<br /> <span class="grey">This
															is what you'll use to sign in to our dashboard</span>
												</div>
											</div>
										</div>
									</td>
								</tr>


								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">PASSWORD</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<form:password path="password" id="password1"
															name="password1" maxlength="100" />
													<br /> <span class="grey">Password
														must be 6 characters or longer.</span>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td valign="top" width="230">
										<div class="box">
											<div class="boxIn">
												<h2 class="title">PASSWORD VERIFICATION</h2>
											</div>
										</div>
									</td>
									<td width="20">&nbsp;</td>
									<td valign="top">
										<div class="box">
											<div class="boxIn">
												<div class="content">
													<input type="password" maxlength="100" id="password2"
														name="password2" value="" /><br /> <span class="grey">Enter
														your password again for verification.</span>
												</div>
											</div>
										</div>
									</td>
								</tr>

								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="middle" colspan="3" align="right">By clicking
										Create my account you agree to the <a class='inline'
										href="#inline_content"><u>Terms of Service</u></a> and <a
										class='inline' href="#inline_content2"><u>Privacy policies</u></a>.
									</td>
								</tr>


								<tr>
									<td colspan="3">&nbsp;</td>
								</tr>

								<tr>
									<td valign="middle" colspan="3" align="right"><a href="/"
										id="signUp" onclick="signup(this);return false;"><span
											id="signUpSpan">Sign Up</span></a></td>
								</tr>

							</table>

						</td>
						<td width="30%" valign="top">

							<table>

								<tr>
									<td><img src="images/register.png" border="0"
										class="personImg" /></td>
								</tr>

							</table>

						</td>
					</tr>

				</table>

			</form:form>

		</div>
	</div>

	<div style='display: none'>

		<div id='inline_content'
			style='padding: 10px; background: #fff; font-size: 16px;'>
			<p>
				<b>Terms of Service</b>
			</p>
			<br />
			<p></p>
		</div>

		<div id='inline_content2'
			style='padding: 10px; background: #fff; font-size: 16px;'>
			<p>
				<b>Privacy Policies</b>
			</p>
			<br />
			<p></p>
		</div>

	</div>

	<%@ include file="footer.jsp"%>

</body>
</html>