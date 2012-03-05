<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
	
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Cleargist // Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/jquery-1.5.1.min.js"></script>
<script type="text/javascript">
	
	
function CGEvent(event, itemId, token, session, userId){
    $.ajax({
        url: 'http://cleargist.elasticbeanstalk.com/activity.do',
        dataType: 'jsonp',
        data: {
            event : event,
            userId : userId,
            itemId : itemId,
            token : token,
            session : session,
        }
    }); 
}


	$(document).ready(function() {

	//get items
	/* jQuery.ajax({
	    url: '	',
	    dataType: 'jsonp',
	    data: {
	        widgetId : '1c0bb730-4e0a-4c32-885f-9843a50f4b9b',
	        tenantId : 'd28ab9a8-5683-41a6-bf29-0f7d06ca2572',
	        itemId : 'test',
	        userId : '100'
	    }
	     success: function(data) {
	        if (data!=null) {
	            jQuery('#CGWUl').html(data.lis);
	        }
	    } 
	}); */
	
	/* jQuery.ajax({
        url: 'scrape_page.do',
        dataType: 'jsonp',
        data: {
            url : "http://www.fashionplus.gr/proion/22255/Female_Shop__rologia__Roloi_B10437.html",
            token : "103",
            catalog : "fashionplus"
        }
	}); */

    
    jQuery.getJSON('get_items.do?callback=?','widgetType='+jQuery('#CGWType').val()+'&token='+jQuery('#CGWToken').val()+'&itemId=3156&noOfItems='+jQuery('#CGWItems').val()+'&userId=&session=', function(data) {
        if (data!=null) {
            var count = 0;
            jQuery.each(data.items, function(key, val) {
            	//$("#CGWli"+count+" a").attr("href", val.link);
            	$("#CGWli"+count+" a")[0].setAttribute("onclick","CGEvent(\"REC_CLICK\", \"3156\", \""+jQuery('#CGWToken').val()+"\", \"\", \"\");closeProductPopup();getProduct(\"\",\"3156\")"); 
            	$("#CGWli"+count+" img").attr("src", val.image);
            	$("#CGWli"+count+" .CGWName").append(val.name);
            	$("#CGWli"+count+" .CGWCategory").append(val.name);
            	$("#CGWli"+count+" .CGWPrice").append("&euro;"+val.price);
            	count++;
            });
            jQuery('#CGW').css('display', '');
        }
    })
    
    /* $.ajax({
        url: 'activity.do',
        dataType: 'jsonp',
        data: {
            event : "ADD_TO_CART",
            userId : "",
            itemId : "25932",
            tenantId : "963f85a9-340c-4549-8859-aa98382dcc0e",
            session : "",
        }
    }); */
    
    
	/* $.ajax({
        url: 'activity.do',
        dataType: 'jsonp',
        data: {
            event : "ADD_TO_CART",
            userId : "",
            itemId : "19",
            tenantId : "963f85a9-340c-4549-8859-aa98382dcc0e",
            session : ""
        }
    }); */
	
});
</script>

</head>

<body>

        <!--header-->
        <div class="wrapper">    
            <div class="header-wrapper">
                <div class="container">
                    <div id="header">
                        <div id="divLogo"><a href="login.do"><img src="images/logo_admin.png" border="0" /></a></div>           
                        <div id="divInfo">
                            <div class="content">
                                Welcome Guest. Please login with your username and password.
                            </div>
                        </div>
                        <div id="divLang">
                        <ul class="languages">
                            <li><a href="/"><img src="images/en_flag.png" alt="english" border="0" /></a></li>
                            <li style="margin-left:8px;"><a href="/el/"><img src="images/el_flag_b.png" alt="greek" border="0" /></a></li>
                        </ul>
                         <br clear="right" />
                        <div id="divRightMenu">
                            <ul class="rightMenu">
                                <li><a href="admin/forgot_pass.do">Forgot Password?</a></li>
                                <li><a href="http://www.cleargist.com/contact.php" target="_blank">Contact</a></li>
                                <li><a href="http://www.cleargist.com/" target="_blank">About</a></li>
                                <li><a href="admin/login.do">Sign In</a></li>
                            </ul>
                        </div>
                        </div>
                 
                    </div>
                </div>
            </div>
            <!--main-->  
            <div class="main-wrapper">
                <div class="container">
                    <div id="loginHeader">
                            <span>A/B Test</span><br />
                            <div id="menu_seperator_top"></div>
                            <br clear="all" />
                            <div class="error"></div>
                    </div>
                    
                    
                    
                    <div id="loginBox">
                        <div class="content">

						<div id="CGW"
							style="font-family: Arial, Helvetica, sans-serif; width: 620px; border: 1px solid rgb(255, 255, 255); overflow: hidden; display: none;"
							align="center">
							<input id="CGWType" name="CGWType" value="Most_popular_overall"
								type="hidden"><input id="CGWToken" name="CGWToken"
								value="103" type="hidden"><input id="CGWTId"
								name="CGWTId" value="963f85a9-340c-4549-8859-aa98382dcc0e"
								type="hidden"><input id="CGWItems" name="CGWItems"
								value="4" type="hidden">
							<div id="CGWHeader"
								style="color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; background-color: rgb(255, 255, 255); padding-top: 10px; padding-bottom: 10px;">
								<span id="CGWHeaderTxt">Δες επίσης...</span>
							</div>
							<div id="CGWMain"
								style="background-color: rgb(255, 255, 255); width: 620px; padding-top: 10px; padding-bottom: 10px; float: right; position: relative;">
								<ul id="CGWUl"
									style="list-style: none outside none; position: relative; width: 85%;">
									<li
										style="overflow: hidden; position: relative; float: left; padding: 0px 14px;"
										id="CGWli0"><a href="" style="text-decoration: none"
										><span style="display: block;" class="CGWImg"><img
												style="width: 100px; height: auto;" src="" border="0"
												height="auto" width="100"></span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWName">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWCategory">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: block;"
											class="CGWPrice">&nbsp;</span></a></li>
									<li
										style="overflow: hidden; position: relative; float: left; padding: 0px 14px;"
										id="CGWli1"><a href="" style="text-decoration: none"
										><span style="display: block;" class="CGWImg"><img
												style="width: 100px; height: auto;" src="" border="0"
												height="auto" width="100"></span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWName">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWCategory">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: block;"
											class="CGWPrice">&nbsp;</span></a></li>
									<li
										style="overflow: hidden; position: relative; float: left; padding: 0px 14px;"
										id="CGWli2"><a href="" style="text-decoration: none"
										><span style="display: block;" class="CGWImg"><img
												style="width: 100px; height: auto;" src="" border="0"
												height="auto" width="100"></span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWName">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWCategory">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: block;"
											class="CGWPrice">&nbsp;</span></a></li>
									<li
										style="overflow: hidden; position: relative; float: left; padding: 0px 14px;"
										id="CGWli3"><a href="" style="text-decoration: none"
										><span style="display: block;" class="CGWImg"><img
												style="width: 100px; height: auto;" src="" border="0"
												height="auto" width="100"></span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWName">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: none;"
											class="CGWCategory">&nbsp;</span><span
											style="font-family: Arial, Helvetica, sans-serif; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; display: block;"
											class="CGWPrice">&nbsp;</span></a></li>
								</ul>
							</div>
							<br clear="all">
							<div id="CGWFooter"
								style="color: rgb(102, 102, 102); font-size: 10px; text-align: center; background-color: rgb(255, 255, 255); width: 620px; padding-top: 5px; padding-bottom: 10px; display: block;">
								powered by <br>
								<a href="http://www.cleargist.com" target="_blank"><img
									src="http://cleargist.elasticbeanstalk.com/images/logo_small.png"
									border="0"></a>
							</div>
						</div>
					</div>
                    </div>
                </div>
            </div>

        </div>
     
    </body>
</html>



