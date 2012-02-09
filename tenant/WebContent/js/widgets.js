$(document).ready(function(){
	
	$("#dialogPreview").dialog({  
        bgiframe: true,
        autoOpen: false,
        width: 964,
        height: 'auto',
        modal: true,
        resizable:false,
        closeOnEscape: false,
            close:function(event, id) {
                jQuery("#spanPreview").html('');
            }
        
    });
	
	$("#dialogCode").dialog({  
        bgiframe: true,
        autoOpen: false,
        width: 964,
        height: 480,
        modal: true,
        resizable:false,
        closeOnEscape: false,
            close:function(event, id) {
                jQuery("#widgetCode").html('');
            }
        
    });
	
	jQuery("#tblWidgets").jqGrid({  
	    url:widgetsUrl1 + '?nd='+new Date().getTime()+"&id="+$("#hiddenTenantId").val(),    
	    datatype: "xml",
	    height:450,
	    width:946, 
	    colNames:['Name', 'Type', 'Description', '', '', '', ''],
	    colModel:[
	        {name:'NAME',index:'NAME',sortable:false,width:80},
	        {name:'TYPE',index:'TYPE',sortable:false,width:100},
	        {name:'DESCRIPTION',index:'DESCRIPTION',sortable:false},
	        {name:'edit',index:'edit',align:"center",width:20,sortable:false},
	        {name:'preview',index:'preview',align:"center",width:20,sortable:false},
	        {name:'code',index:'code',align:"center",width:20,sortable:false},
	        {name:'delete',index:'delete',align:"center",width:20,sortable:false}
	    ],
	    rownumbers:true,
	    sortname: 'NAME',
	    viewrecords: true,                        
	    sortorder: "asc",
	    sortable:false,
	    caption: "Widget List",
	    multiselect:false
	});
	
	 $("#dialogAdd").dialog({  
         bgiframe: true,
         autoOpen: false,
         width: 600,
         height: 350,
         modal: true,
         resizable:false,
         closeOnEscape: false,
             close:function(event, id) {
                 
             }
     });
	 
});


function addWidget(obj) {
	
	obj.style.visibility="hidden";
	var bValid = true;
	var params = "";
	var tenantId = $("#hiddenTenantId").val();
	$("#widgetName").removeClass("ui-state-error");
	$("#widgetDesc").removeClass("ui-state-error");
	
	var name = jQuery("#widgetName").val();
	var desc = jQuery("#widgetDesc").val();
	
	//start validations      
	if ($("#widgetName").val() == "") {
		$("#widgetName").addClass("ui-state-error");
		bValid = false;       
	}
	
	if (bValid) {
		params+= "id="+tenantId;
		params+="&name="+encodeURIComponent(name);
		params+="&description="+encodeURIComponent(desc);
		
		jQuery.ajax({
	        url:widgetsUrl2,
	        data: params,
	        cache:false,
	        dataType:"json",
	        type:"POST",
	        success:function(json){
	            if(json.answer=="true"){
	            	obj.style.visibility="";
		        	$('#dialogAdd').dialog('close');
	                jQuery("#tblWidgets").trigger("reloadGrid");
	            } else {
	            	$("#widgetName").addClass("ui-state-error");
		        	$("#widgetDesc").addClass("ui-state-error");
	            }
	        }
	    });
		obj.style.visibility="";
	} else {
		obj.style.visibility="";
	}
	
}


function deleteWidget(id, name) {
	confirm ("Are you sure for the deletion of the widget "+name+"?");
	var params = "";
	var tenantId = $("#hiddenTenantId").val();

	params+= "tenantId="+tenantId;
	params+="&id="+id;
	
	jQuery.ajax({
        url:widgetsUrl3,
        data: params,
        cache:false,
        dataType:"json",
        type:"POST",
        success:function(json){
            if(json.answer=="true"){
                jQuery("#tblWidgets").trigger("reloadGrid");
            } 
        }
    });
	
}

function dialogCode(id){
    $.getJSON('get_widget_code.do?id='+id+"&tenantId="+$("#hiddenTenantId").val(), function(json) {
        if (json.answer == "true") {
        	var html = json.code;
        	html = htmlEncode(html);
        	$("#widgetCode").html(html);
        	$("#widgetCode").snippet("html",{style:"bright"});
        	$('#dialogCode').dialog('open');
        }
    });
}

function dialogPreview(id){
	$.getJSON('preview_widget.do?id='+id+"&tenantId="+$("#hiddenTenantId").val(), function(json) {
        if (json.answer == "true") {
        	$("#spanPreview").html(json.preview+"<br/>");
        	$('#dialogPreview').dialog('open');
        }
    });
  
}

function htmlEncode(value){
  return $('<div/>').text(value).html();
}