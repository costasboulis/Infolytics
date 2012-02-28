function message(message, obj, div, goback){
	jQuery('div.'+div).html(message+"&nbsp;<img src='../images/delete2.png' border='0' style='cursor:pointer' width='12' height='12' onclick='closeDiv(\""+div+"\", \""+goback+"\")'/>");
	
	if (goback=="1")  jQuery('div.goback').fadeOut("slow");
	
	jQuery('div.'+div).fadeIn();
	obj.style.visibility="";
}

function closeDiv(div, goback){
	jQuery('div.'+div).fadeOut("slow");
	if (goback=="1") jQuery('div.goback').fadeIn();
}