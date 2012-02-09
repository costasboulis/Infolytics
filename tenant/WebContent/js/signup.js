$(document).ready(function(){
    $(".inline").colorbox({inline:true, width:"50%"});
});

function signup(obj){
	obj.style.visibility="hidden";
	var bValid = true;
	var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	var reg2 = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
	jQuery("div.error:first").html("");
	
	var minLength = 6; // Minimum password length
	var errorMessage = "";
	
	//start validations      
	if ($("#firstname").val() == "") {
		errorMessage+="&nbsp;- First Name is required<br />";
		bValid = false;       
	}

	if ($("#lastname").val() == "") {
		errorMessage+="&nbsp;- Last Name is required<br />";
		bValid = false;                        
	}

	if(reg.test($("#email").val()) == false) {
		errorMessage+="&nbsp;- Email is required<br />";
		bValid = false;                        
	}

	if ($("#company").val() == "") {
		errorMessage+="&nbsp;- Company Name is required<br />";
		bValid = false;                        
	}

	if (reg2.test($("#siteUrl").val()) == false) {
		errorMessage+="&nbsp;- A valid Site Url is required (please include http:// at the beginning)<br />";
		bValid = false;                        
	}

	if ($("#username").val() == false) {
		errorMessage+="&nbsp;- Username is required<br />";
		bValid = false;                        
	}
	
	if ($("#password1").val() == false) {
		errorMessage+="&nbsp;- Password is required<br />";
		bValid = false;                        
	}
	
	if($("#password2").val() == false) {
		errorMessage+="&nbsp;- Password Verification is required<br />";
		bValid = false;                        
	}
	
	var params = "username="+$("#username").val();
	jQuery.ajax({
        url:checkUserUrl,
        data: params,
        cache:false,
        dataType:"json",
        type:"POST",
        success:function(json){
            if(json.answer=="true"){
            	errorMessage+="&nbsp;- Username already exists! Please choose another one and retry<br />";
    			bValid = false; 
    			window.scrollTo(0,0);
    			jQuery("div.error:first").html(errorMessage);
    			jQuery("div.error:first").fadeIn();
    			obj.style.visibility="";
            } else {
            	if (bValid) {
            		if ($("#password1").val().length < minLength) {
            			errorMessage+="&nbsp;- Your password must be at least " + minLength + " characters long<br />";
            			bValid = false;
            		}
            		
            		if ($("#password1").val() != $("#password2").val()) {
            			errorMessage+="&nbsp;- You did not enter the same new password twice<br />";
            			bValid = false;                        
            		}
            		
            		if (bValid) {
            			document.forms["frmSignUp"].submit();
            		} else {
            			window.scrollTo(0,0);
            			jQuery("div.error:first").html(errorMessage);
            			jQuery("div.error:first").fadeIn();
            			obj.style.visibility="";
            		}
            		
            	} else {
            		window.scrollTo(0,0);
        			jQuery("div.error:first").html(errorMessage);
        			jQuery("div.error:first").fadeIn();
        			obj.style.visibility="";
            	}
            }
        }
    });
	
	

}