var refreshId;

$(document).ready(function(){
	
	jQuery("#tblCatalog").jqGrid({  
	    url:catalogUrl1 + '?nd='+new Date().getTime()+"&id="+$("#hiddenTenantToken").val(),    
	    datatype: "xml",
	    height:500,
	    width:946, 
	    hoverrows: false,
	    colNames:['ID', 'Image', 'Item', 'Category', 'Price', 'Description'],
	    colModel:[
	        {name:'CUSTID',index:'CUSTID',align:"center",width:30},
	        {name:'IMAGE',index:'IMAGE',sortable:false,align:"center",width:40},
	        {name:'NAME',index:'NAME',width:60},
	        {name:'CATEGORY',index:'CATEGORY',width:60},
	        {name:'PRICE',index:'PRICE',align:"center",width:30},
	        /*{name:'STOCK',index:'STOCK',align:"center",width:60},*/
	        {name:'DESCRIPTION',index:'DESCRIPTION',sortable:false}
	        
	        /*{name:'URL',index:'URL',align:"center",width:60}*/
	    ],
	    rownumbers:true,
	    sortname: 'NAME',
	    viewrecords: true,                        
	    sortorder: "asc",
	    sortable:true,
	    caption: "Current Catalog",
	    pager:jQuery("#divCatalogGridPager"),
	    multiselect:false,
	    
	    beforeSelectRow: function(rowid, e) { 
	        return false; 
	    } 


	
	});

	 $("#dialogPhoto").dialog({  
         bgiframe: true,
         autoOpen: false,
         width: 400,
         height: 300,
         modal: true,
         resizable:false,
         closeOnEscape: false,
             close:function(event, id) {
                 jQuery("#spanPhoto").html('');
             }
         
     }); 
	 $("#dialogUpload").dialog({  
         bgiframe: true,
         autoOpen: false,
         width: 600,
         height: 200,
         modal: true,
         resizable:false,
         closeOnEscape: false,
             close:function(event, id) {
                 
             }
         
     });
	 
	 var status= $("#hiddenCatalogStatus").val();

	 if (status=="SYNCING") 
		 refreshId = setInterval(function() { checkCatalogStatus(); }, 7000);

});

function checkCatalogStatus(){
	var tenantId = $("#hiddenTenantId").val();
    $.getJSON('check_catalog_status.do?tenantId='+tenantId, function(json) {
        if (json.answer == "true") {
    	 	$("#spanCatalogStatus").html('');
            /*$("#spanCatalogStatus").html('<img src="../images/syncin.gif" border="0"/>');*/
            if (json.status == "SYNCING") {
            $("#spanCatalogStatus").html('Uploading new catalog&nbsp;&nbsp;&nbsp;<img src="../images/ajax-loader.gif" width="10" />');
            /*} else if (json.status == "INSYNC") {
                $("#spanCatalogStatus").html('<font color="#3cdd03">OK</font>');*/
            } else if (json.status == "FAILED") {
                $("#spanCatalogStatus").html('Latest catalog upload was <font color="#E42E21"><b>FAILED</b></font>.');
            /*} else {
            	$("#spanCatalogStatus").html('<font color="#E42E21">AWAITING</font>');*/
            }
            
            if (json.status != "SYNCING") {
            	clearInterval(refreshId);
                $("#spanCatalogStatusDesc").html("&nbsp;&nbsp;<a href=\"/\" class=\"x-small-button-2\" onclick=\"$('#dialogUpload').dialog('open'); return false;\"><span class=\"x-small-button-span\">Upload Catalog</span></a>");
                jQuery("#tblCatalog").jqGrid('setGridParam',{url:catalogUrl1+"&id="+$("#hiddenTenantToken").val()}).trigger("reloadGrid");
                $("#hiddenCatalogStatus").val(json.status);
            }
            
        }
    });
}

function searchCatalog(){
	var empty = /^\s+$/;
	var searchParams = "";
	var selKey = jQuery("#selSearchcat").val();
	var selValue = jQuery("#catSearchKey").val();
	
	if(selValue==""){
		searchParams = "?time="+new Date().getTime()+"&id="+$("#hiddenTenantToken").val();
		jQuery("#tblCatalog").jqGrid('setGridParam',{url:catalogUrl1+searchParams}).trigger("reloadGrid");
	} else {
	
		if(!empty.test(selValue) && selValue!=""){
			searchParams+="&selKey="+encodeURIComponent(selKey);
			searchParams+="&selValue="+encodeURIComponent(selValue);
		}
		
		if(searchParams!=""){
			searchParams = "?time="+new Date().getTime()+searchParams+"&id="+$("#hiddenTenantToken").val();
			jQuery("#tblCatalog").jqGrid('setGridParam',{url:catalogUrl1+searchParams}).trigger("reloadGrid");
			
	
		}
	
	}
	
	
}//searchGrid

function clearCatalog(obj){
	searchParams = "?time="+new Date().getTime()+"&id="+$("#hiddenTenantToken").val();
	jQuery("#tblCatalog").jqGrid('setGridParam',{url:catalogUrl1+searchParams}).trigger("reloadGrid");
}//clearGrid


function imageDisplay (image) {	
	jQuery("#spanPhoto").html("<img src='"+image+"' border='0' width='200' />");
    jQuery("#dialogPhoto").dialog("open");
}

function uploadCatalog (obj) {	
	
	var bValid = true;
	$("#file").removeClass("ui-state-error");
	
	if ($("#file").val() == "") {
		bValid = false;       
		$("#file").addClass("ui-state-error");
	}
	if (bValid) {
		obj.style.visibility="hidden";
		document.forms["frmUploadCatalog"].submit();
	}
}