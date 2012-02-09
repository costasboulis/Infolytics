<%
	response.setContentType("application/json;charset=utf-8");

	String responseStr = (String)request.getAttribute("jsonResponse");
	out.println(responseStr);
%>