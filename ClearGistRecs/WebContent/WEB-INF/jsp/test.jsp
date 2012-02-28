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

	jQuery.getJSON('http://cleargist.elasticbeanstalk.com/get_items.do?callback=?','&widgetId='+jQuery('#CGWId').val()+'&tenant=963f85a9-340c-4549-8859-aa98382dcc0e&itemId='+id+'&userId=&session=', function(data) {
        if (data!=null && data.lis!="") {
            jQuery('#CGWUl').html(data.lis);
            jQuery('#CGW').css('display', '');
            jQuery('#divProductPopup').css('height', '580px');
            jQuery('#divProductPopupChild').css('height', '570px');
        }
    });
    
    /* jQuery.getJSON('get_items.do?callback=?','widgetId='+jQuery('#CGWId').val()+'&tenantId=963f85a9-340c-4549-8859-aa98382dcc0e&itemId=25932&userId=100', function(data) {
        if (data!=null) {
            jQuery('#CGWUl').html(data.lis);
            jQuery('#CGW').css('display', '');
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
		                      <br /><div id='CGW'  style='overflow-x: hidden; overflow-y: hidden; font-family: Arial, Helvetica, sans-serif; width: 600px; border-top-width: 1px; border-right-width: 1px; border-bottom-width: 1px; border-left-width: 1px; border-top-style: solid; border-right-style: solid; border-bottom-style: solid; border-left-style: solid; border-top-color: rgb(255, 255, 255); border-right-color: rgb(255, 255, 255); border-bottom-color: rgb(255, 255, 255); border-left-color: rgb(255, 255, 255); border-image: initial;' align='center'><input type='hidden' id='CGWId' name='CGWId' value='6203fc3b-0caa-4d4c-92bf-18dcc2809c62'><div id='CGWHeader' style='height: 100%; padding-top: 10px; padding-bottom: 10px; color: rgb(0, 0, 0); font-size: 12px; font-weight: normal; text-align: center; background-color: rgb(255, 255, 255); '><span id='CGWHeaderTxt'>Δες επίσης...</span></div><div id='CGWMain' style='padding-top: 10px; padding-bottom: 10px; float: right; position: relative; background-color: rgb(255, 255, 255); width: 600px; '><ul id='CGWUl' style='list-style-type: none; list-style-position: initial; list-style-image: initial; position: relative; width: 45%; '></ul></div><br clear='all'><div id='CGWFooter' style='color: rgb(102, 102, 102); font-size: 10px; text-align: center; padding-top: 5px; padding-bottom: 10px; background-color: rgb(255, 255, 255); display: block; width: 600px; '>powered by <br><a href='http://www.cleargist.com' target='_blank'><img src='http://cleargist.elasticbeanstalk.com/images/logo_small.png' border='0'></a></div></div>
                           </div>
                    </div>
                </div>
            </div>

        </div>
     
    </body>
</html>



