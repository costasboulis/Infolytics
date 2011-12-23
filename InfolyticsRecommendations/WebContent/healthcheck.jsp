<!--
     This trivial example is a template for how to create a health check page. 
     A real health check is as complex as necessary to determine the health 
     of your application.  An http response of 200 (OK) is considered healthy;
     anything else will be considered unhealthy and taken out of the environment's
     load balancer. 
-->
<html>
  <head>
    <title>Health check</title>
  </head>
  <body>
    <h1>Application is healthy!</h1>
    <p><%= new java.util.Date() %></p>
  </body>
</html>