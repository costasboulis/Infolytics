<%
	response.setContentType("application/xml;charset=utf-8");
	String xml = (String)request.getAttribute("catalogXml");
	out.println(xml);
%>