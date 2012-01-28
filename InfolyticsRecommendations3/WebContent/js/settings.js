function updatePerSettings(obj){
	obj.style.visibility="hidden";
	var bValid = true;
	var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	var reg2 = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
	jQuery("div.error3").html("");
	jQuery("div.success3").html("");
	var errorMessage = "";
	$("#firstName").removeClass("ui-state-error");
	$("#lastName").removeClass("ui-state-error");
	$("#email").removeClass("ui-state-error");
	$("#company").removeClass("ui-state-error");
	$("#siteUrl").removeClass("ui-state-error");
	
	//start validations      
	if ($("#firstName").val() == "") {
		errorMessage+="First Name ";
		$("#firstName").addClass("ui-state-error");
		bValid = false;       
	}

	if ($("#lastName").val() == "") {
		errorMessage+="Last Name ";
		$("#lastName").addClass("ui-state-error");
		bValid = false;                        
	}

	if(reg.test($("#email").val()) == false) {
		errorMessage+="Email ";
		$("#email").addClass("ui-state-error");
		bValid = false;                        
	}

	if ($("#company").val() == "") {
		errorMessage+="Company ";
		$("#company").addClass("ui-state-error");
		bValid = false;                        
	}

	if (reg2.test($("#url").val()) == false) {
		errorMessage+="URL ";
		$("#url").addClass("ui-state-error");
		bValid = false;                        
	}
	
	if (bValid) {
		var params = "id="+$("#hiddenTenantId").val()+"&firstName="+$("#firstName").val()+"&lastName="+$("#lastName").val()+"&email="+$("#email").val()+"&company="+$("#company").val()+"&url="+$("#url").val();
		jQuery.ajax({
	        url:settingsUrl1,
	        data: params,
	        cache:false,
	        dataType:"json",
	        data:params,
	        type:"POST",
	        success:function(json){
	        	if(json.answer!="true"){
	            	errorMessage+="Ops! An network error occured. Please try again or contact us.";
	    			bValid = false; 
	    			message ("Ops! Check the following: "+errorMessage, obj, "error3", '0');
	            } else {
	            	message ("Settings updated successfully!", obj, "success3", '0');
	            }
	        }
	    });
	} else {
		message ("Ops! Check the following: "+errorMessage, obj, "error3", '0');
	}
	
}

function updateLogSettings(obj){
	obj.style.visibility="hidden";
	var bValid = true;
	jQuery("div.error4").html("");
	jQuery("div.success4").html("");
	var errorMessage = "";
	var minLength = 6; // Minimum password length
	$("#password1").removeClass("ui-state-error");
	$("#password2").removeClass("ui-state-error");
	
	if ($("#password1").val() == "") {
		errorMessage+="Password (at least " + minLength + " chars long) ";
		$("#password1").addClass("ui-state-error");
		bValid = false;                        
	}
	
	if ($("#password2").val() == "") {
		errorMessage+="Password Verification ";
		$("#password2").addClass("ui-state-error");
		bValid = false;                        
	}
	
	if ($("#password1").val().length < minLength) {
		bValid = false;
		$("#password1").addClass("ui-state-error");
	}

	
	if ($("#password1").val() != $("#password2").val()) {
		$("#password1").addClass("ui-state-error");
		$("#password2").addClass("ui-state-error");
		bValid = false;                        
	}
		
	if (bValid) {
		var params = "id="+$("#hiddenTenantId").val()+"&password1="+$("#password1").val();
		jQuery.ajax({
	        url:settingsUrl2,
	        data: params,
	        cache:false,
	        dataType:"json",
	        data:params,
	        type:"POST",
	        success:function(json){
	            if(json.answer!="true"){
	            	errorMessage+="Ops! An network error occured. Please try again or contact us.";
	    			bValid = false; 
	    			message ("Ops! Check the following: "+errorMessage, obj, "error4", '0');
	            } else {
	            	message ("Settings updated successfully!", obj, "success4", '0');
	            }
	        }
	    });
	} else {
		message ("Ops! Check the following: "+errorMessage, obj, "error4", '0');
	}
	
}