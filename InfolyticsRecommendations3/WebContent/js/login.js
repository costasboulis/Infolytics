function signin(obj){
	
	obj.style.visibility="hidden";
	document.forms["frmSignIn"].submit();
	
	/*var bValid = true;
	jQuery("div.error:first").html("");
	var errorMessage = "";
	
	//start validations      
	if ($("#j_username").val() == "") {
		errorMessage+="&nbsp;- Username is required<br />";
		bValid = false;                        
	}

	if ($("#j_password").val() == "") {
		errorMessage+="&nbsp;- Password is required<br />";
		bValid = false;                        
	}
	
	var params = "username="+$("#j_username").val()+"&password="+$("#j_password").val();
	jQuery.ajax({
        url:checkLoginUrl,
        data: params,
        cache:false,
        dataType:"json",
        data:params,
        type:"POST",
        success:function(json){
            if(json.answer=="false"){
            	errorMessage+="&nbsp;- The username and password did not match. Please try again.<br />";
    			bValid = false; 
    			jQuery("div.error:first").html(errorMessage);
    			jQuery("div.error:first").fadeIn();
    			obj.style.visibility="";
            } else {
            	if (bValid) {
            		document.forms["frmSignIn"].submit();
            	} else {
        			jQuery("div.error:first").html(errorMessage);
        			jQuery("div.error:first").fadeIn();
        			obj.style.visibility="";
        		}
            }
        }
    });*/
}