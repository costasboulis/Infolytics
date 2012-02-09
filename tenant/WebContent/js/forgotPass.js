function forgotPass(obj){
	
	obj.style.visibility="hidden";
	
	var bValid = true;
	var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	jQuery("div.error2").html("");
	jQuery("div.success").html("");
	var errorMessage = "User authentication was not successful, please try again.<br />Caused: ";
	
	//start validations      
	if ($("#username").val() == "") {
		errorMessage+="Username is required<br />";
		bValid = false;                        
	}
	
	if(reg.test($("#email").val()) == false) {
		errorMessage+="A valid email is required<br />";
		bValid = false;                        
	}
	
	if (bValid) {
		var params = "username="+$("#username").val()+"&email="+$("#email").val();
		jQuery.ajax({
	        url:checkResetPassUrl,
	        data: params,
	        cache:false,
	        dataType:"json",
	        data:params,
	        type:"POST",
	        success:function(json){
	            if(json.answer=="false"){
	            	errorMessage+="The username and email did not match<br />";
	    			bValid = false; 
	    			jQuery("div.error2").html(errorMessage);
	    			jQuery("div.error2").fadeIn();
	    			obj.style.visibility="";
	            } else {
	            	jQuery("div.success").html("Your password has been successfully reset to a temporary password.<br />To continue, check your email and login with your new password.<br />You are able to change your password at any time from the settings menu.");
	        		jQuery("div.success").fadeIn();
	        		obj.style.visibility="";
	            }
	        }
	    });
	} else {
		jQuery("div.error2").html(errorMessage);
		jQuery("div.error2").fadeIn();
		obj.style.visibility="";
	}
	
	
}