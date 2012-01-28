$(document).ready(function(){
	
	$("#divRdShowHeader").buttonset();
	$("#divRdShowImages").buttonset();
	$("#divRdShowLogo").buttonset();
	$("#divRdHeaderTrans").buttonset();
	$("#divRdMainTrans").buttonset();
	$("#divRdFooterTrans").buttonset();
	
	$("#divRdShowName").buttonset();
	$("#divRdShowPrice").buttonset();
	$("#divRdShowCategory").buttonset();
	/*$("#divRdShowStock").buttonset();*/

	$('#advancedSettings').hide();
	
	$("#dialogCode").dialog({  
        bgiframe: true,
        autoOpen: false,
        width: 964,
        height: 480,
        modal: true,
        resizable:false,
        closeOnEscape: false,
            close:function(event, id) {
                //jQuery("#widgetCode").html('');
            }
        
    });
	
	$("#borderColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#borderColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenBorderColor').val(hex);
			 previewWidget();
		}
	});
	
	$("#headerBackColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#headerBackColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenHeaderBackColor').val(hex);
			previewWidget();
		}
	});
	
	$("#mainBackColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#mainBackColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenMainColor').val(hex);
			previewWidget();
		}
	});
	
	$("#footerBackColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#footerBackColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenFooterColor').val(hex);
			previewWidget();
		}
	});
	
	$("#headerColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#headerColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenHeaderColor').val(hex);
			previewWidget();
		}
	});
	
	$("#nameColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#nameColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenNameColor').val(hex);
			previewWidget();
		}
	});
	
	$("#priceColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#priceColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenPriceColor').val(hex);
			previewWidget();
		}
	});
	
	$("#categoryColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#categoryColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenCategoryColor').val(hex);
			previewWidget();
		}
	});
	
	/*$("#stockColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#stockColorSelector div').css('backgroundColor', '#' + hex);
			$('#hiddenStockColor').val(hex);
			previewWidget();
		}
	});*/
	
	$("#previewBackColorSelector").ColorPicker({
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$('#previewBackColorSelector div').css('backgroundColor', '#' + hex);
			$('#previewArea').css('backgroundColor', '#' + hex);
			previewWidget();
		}
	});
	
});


function editWidget(obj) {
	obj.style.visibility="hidden";
	var bValid = true;
	jQuery("div.error").html("");
	jQuery("div.success").html("");
	var errorMessage = "";
	var tenantId = $("#hiddenTenantId").val();
	$("#name").removeClass("ui-state-error");
	
	if ($("#name").val() == "") {
		errorMessage+="Title";
		$("#name").addClass("ui-state-error");
		bValid = false;       
	}
	
	if (bValid) {
		
		if ($('#rdShowHeader1').attr('checked'))
            var sHead = "1";
         else
            var sHead = "0";
		
		if ($('#rdShowImages1').attr('checked'))
            var sImag = "1";
         else
            var sImag = "0";
		
		if ($('#rdShowLogo1').attr('checked'))
            var sLogo = "1";
         else
            var sLogo = "0";
		
		if ($('#rdHeaderTrans1').attr('checked'))
            var hTrans = "1";
         else
            var hTrans = "0";
		
		if ($('#rdMainTrans1').attr('checked'))
            var mTrans = "1";
         else
            var mTrans = "0";
		
		if ($('#rdFooterTrans1').attr('checked'))
            var fTrans = "1";
         else
            var fTrans = "0";
		
		/*if ($('#rdShowStock1').attr('checked'))
            var sStoc = "1";
         else
            var sStoc = "0";*/
		
		if ($('#rdShowCategory1').attr('checked'))
            var sCat = "1";
         else
            var sCat = "0";
		
		if ($('#rdShowPrice1').attr('checked'))
            var sPrice = "1";
         else
            var sPrice = "0";
		
		if ($('#rdShowName1').attr('checked'))
            var sName = "1";
         else
            var sName = "0";
		
		var params = "tenantId="+tenantId+"&id="+$("#hiddenWidgetId").val()+"&name="+$("#name").val()+"&description="+$("#description").val()+"&selLayoutType="+$("#selLayoutType").val()+"&selNoOfItems="+$("#selNoOfItems").val()+
		"&selImageSize="+$("#selImageSize").val()+"&selImageSize2="+$("#selImageSize2").val()+"&selTextAreaWidth="+$("#selTextAreaWidth").val()+"&sHead="+sHead+"&sImag="+sImag+"&sLogo="+sLogo+
		"&borderColor="+$("#hiddenBorderColor").val()+"&selBorderWidth="+$("#selBorderWidth").val()+"&headerBackColor="+$("#hiddenHeaderBackColor").val()+"&hTrans="+hTrans+"&mainColor="+$("#hiddenMainColor").val()+
		"&mTrans="+mTrans+"&footerColor="+$("#hiddenFooterColor").val()+"&fTrans="+fTrans+"&selMainFontType="+$("#selMainFontType").val()+"&headerColor="+$("#hiddenHeaderColor").val()+
		"&headerText="+$("#headerText").val()+"&selHeaderFontSize="+$("#selHeaderFontSize").val()+"&selHeaderFontWeight="+$("#selHeaderFontWeight").val()+"&selHeaderFontAlign="+$("#selHeaderFontAlign").val()+
		"&nameColor="+$("#hiddenNameColor").val()+"&selNameFontSize="+$("#selNameFontSize").val()+"&selNameFontWeight="+$("#selNameFontWeight").val()+"&selNameFontAlign="+$("#selNameFontAlign").val()+
		"&priceColor="+$("#hiddenPriceColor").val()+"&selPriceFontSize="+$("#selPriceFontSize").val()+"&selPriceFontWeight="+$("#selPriceFontWeight").val()+"&selPriceFontAlign="+$("#selPriceFontAlign").val()+
		"&categoryColor="+$("#hiddenCategoryColor").val()+"&selCategoryFontSize="+$("#selCategoryFontSize").val()+"&selCategoryFontWeight="+$("#selCategoryFontWeight").val()+"&selCategoryFontAlign="+$("#selCategoryFontAlign").val()+
		"&sName="+sName+"&sPrice="+sPrice+"&sCat="+sCat+"&sStoc="+sStoc+"&selListWidth="+$("#selListWidth").val()+"&selListSpaces="+$("#selListSpaces").val()+"&selListSpaces2="+$("#selListSpaces2").val()+
		"&selListDisplay="+$("#selListDisplay").val()+"&widgetCode="+encodeURIComponent($("#previewAreaHidden").html())+"&previewCode="+encodeURIComponent($("#previewArea").html())+"&selWidgetType="+$("#selWidgetType").val();
		
		jQuery.ajax({
	        url:widgetsUrl,
	        data: params,
	        cache:false,
	        dataType:"json",
	        data:params,
	        type:"POST",
	        success:function(json){
	        	if(json.answer!="true"){
	            	errorMessage+="Ops! An network error occured. Please try again or contact us.";
	    			bValid = false; 
	    			message ("Ops! Check the following: "+errorMessage, obj, "error", '1');
	            } else {
	            	message ("Widget updated successfully!", obj, "success", '1');
	            }
	        }
	    });
	} else {
		message ("Ops! Check the following: "+errorMessage, obj, "error", '1');
	}
	
}

function getItems() {
	var params = "token="+$("#hiddenTenantToken").val()+"&limit="+$("#selNoOfItems").val();
	jQuery.ajax({
        url:widgetsUrl3,
        data: params,
        cache:false,
        dataType:"json",
        data:params,
        type:"POST",
        success:function(json){
            if(json.answer=="true"){
            	$('#clearGistWidgetUl').html(json.lis);
            	previewWidget();
            }
        }
    });
}

function previewWidget() {
	
	var selMainFontType = $("#selMainFontType").val();
	var selTextAreaWidth = $("#selTextAreaWidth").val();
	var headerText = $("#headerText").val();
	var selLayoutType = $("#selLayoutType").val();
	var liFloat = "left";
	var liDisplay = "";
	if (selLayoutType=="vertical") {
		liFloat = '';
		liDisplay = 'block';
	}
	var selImageSize = $("#selImageSize").val();
	var selImageSize2 = $("#selImageSize2").val();
	var selListWidth = $("#selListWidth").val();
	var selListSpaces = $("#selListSpaces").val();
	var selListDisplay = $("#selListDisplay").val();
	var selListSpaces2 = $("#selListSpaces2").val();
	var hiddenHeaderColor = $("#hiddenHeaderColor").val();
	var selHeaderFontSize = $("#selHeaderFontSize").val();
	var selHeaderFontWeight = $("#selHeaderFontWeight").val();
	var selHeaderFontAlign = $("#selHeaderFontAlign").val();
	var hiddenHeaderBackColor = "#"+$("#hiddenHeaderBackColor").val();
	if ($('#rdHeaderTrans1').attr('checked')) hiddenHeaderBackColor = "transparent";
	var hiddenMainColor ="#"+ $("#hiddenMainColor").val();
	if ($('#rdMainTrans1').attr('checked')) hiddenMainColor = "transparent";
	var hiddenFooterColor = "#"+$("#hiddenFooterColor").val();
	if ($('#rdFooterTrans1').attr('checked')) hiddenFooterColor = "transparent";
	var hiddenNameColor = $("#hiddenNameColor").val();
	var selNameFontSize = $("#selNameFontSize").val();
	var selNameFontWeight = $("#selNameFontWeight").val();
	var selNameFontAlign = $("#selNameFontAlign").val();
	var hiddenPriceColor = $("#hiddenPriceColor").val();
	var selPriceFontSize = $("#selPriceFontSize").val();
	var selPriceFontWeight = $("#selPriceFontWeight").val();
	var selPriceFontAlign = $("#selPriceFontAlign").val();
	var hiddenCategoryColor = $("#hiddenCategoryColor").val();
	var selCategoryFontSize = $("#selCategoryFontSize").val();
	var selCategoryFontWeight = $("#selCategoryFontWeight").val();
	var selCategoryFontAlign = $("#selCategoryFontAlign").val();
	/*var hiddenStockColor = $("#hiddenStockColor").val();
	var selStockFontSize = $("#selStockFontSize").val();
	var selStockFontWeight = $("#selStockFontWeight").val();
	var selStockFontAlign = $("#selStockFontAlign").val();*/
	var hiddenBorderColor = $("#hiddenBorderColor").val();
	var selBorderWidth = $("#selBorderWidth").val();
	var liPadding = selListSpaces2 + "px " + selListSpaces + "px " + selListSpaces2 + "px " + selListSpaces + "px";
	var dispHead = "none";
	if ($('#rdShowHeader1').attr('checked')) dispHead = "";
	var dispImg = "none";
	if ($('#rdShowImages1').attr('checked')) dispImg = "block";
	var dispLog = "none";
	if ($('#rdShowLogo1').attr('checked')) dispLog = "block";
	var dispName = "none";
	if ($('#rdShowName1').attr('checked')) dispName = selListDisplay;
	var dispPrice = "none";
	if ($('#rdShowPrice1').attr('checked')) dispPrice = selListDisplay;
	var dispCat = "none";
	if ($('#rdShowCategory1').attr('checked')) dispCat = selListDisplay;
	/*var dispSto = "none";
	if ($('#rdShowStock1').attr('checked')) dispSto = selListDisplay;*/

	
	$('#clearGistWidget').css({ 
		'font-family' : selMainFontType, 
		width : selTextAreaWidth+"px",
		border : selBorderWidth+"px solid #"+ hiddenBorderColor
	});
	
	$('#clearGistWidgetHeader').css({ 
		color : "#"+hiddenHeaderColor,
		'font-size': selHeaderFontSize+"px",
		'font-weight': selHeaderFontWeight,
		'text-align': selHeaderFontAlign,
		'background-color': hiddenHeaderBackColor,
		display: dispHead
	});
	
	$('#clearGistWidgetMain').css({ 
		'background-color': hiddenMainColor,
		width : selTextAreaWidth+"px"
	});
	
	$('#clearGistWidgetMain ul').css({ 
		width : selListWidth+"%"
	});
	
	$('#clearGistWidgetMain ul li').css({ 
		float : liFloat,
		padding: liPadding,
		display: liDisplay
	});
	
	$('#clearGistWidgetMain ul li img').css({ 
		width : selImageSize,
		height : selImageSize2
	});
	
	$('#clearGistWidgetHeaderTxt').html(headerText);
	
	$('.clearGistWidgetImgSpan').css({ 
		display: dispImg
	});
	
	$('.clearGistWidgetNameSpan').css({ 
		'font-family' : selMainFontType, 
		 color : "#"+hiddenNameColor,
		'font-size': selNameFontSize+"px",
		'font-weight': selNameFontWeight,
		'text-align': selNameFontAlign,
		display: dispName
	});
	
	$('.clearGistWidgetCategorySpan').css({ 
		'font-family' : selMainFontType, 
		color : "#"+hiddenCategoryColor,
		'font-size': selCategoryFontSize+"px",
		'font-weight': selCategoryFontWeight,
		'text-align': selCategoryFontAlign,
		display: dispCat
	});
	
	$('.clearGistWidgetPriceSpan').css({ 
		'font-family' : selMainFontType, 
		color : "#"+hiddenPriceColor,
		'font-size': selPriceFontSize+"px",
		'font-weight': selPriceFontWeight,
		'text-align': selPriceFontAlign,
		display: dispPrice
	});
	
	/*$('.clearGistWidgetStockSpan').css({ 
		'font-family' : selMainFontType, 
		color : "#"+hiddenStockColor,
		'font-size': selStockFontSize+"px",
		'font-weight': selStockFontWeight,
		'text-align': selStockFontAlign,
		display: dispSto
	});*/
	
	$('#clearGistWidgetFooter').css({ 
		'background-color': hiddenFooterColor,
		display: dispLog,
		width : selTextAreaWidth+"px"
	});
	
	
	
	//hidden div 
	$('#CGW').css({ 
		'font-family' : selMainFontType, 
		width : selTextAreaWidth+"px",
		border : selBorderWidth+"px solid #"+ hiddenBorderColor
	});
	
	$('#CGWHeader').css({ 
		color : "#"+hiddenHeaderColor,
		'font-size': selHeaderFontSize+"px",
		'font-weight': selHeaderFontWeight,
		'text-align': selHeaderFontAlign,
		'background-color': hiddenHeaderBackColor,
		display: dispHead
	});
	
	$('#CGWMain').css({ 
		'background-color': hiddenMainColor,
		width : selTextAreaWidth+"px"
	});
	
	$('#CGWMain ul').css({ 
		width : selListWidth+"%"
	});
	
	
	$('#CGWHeaderTxt').html(headerText);
	
	$('#CGWFooter').css({ 
		'background-color': hiddenFooterColor,
		display: dispLog,
		width : selTextAreaWidth+"px"
	});
	
	//set code textearea
	var html = $('#previewAreaHidden').html();
	html = htmlEncode(html);
	$("#widgetCode").html(html);
	$("#widgetCode").snippet("html",{style:"bright"});
	
}


function showAdvancedSettings() {
	$('#advancedSettings').fadeToggle("slow", "linear");
}

function clearChanges() {
	confirm('Are you sure you want to clear your changes?');
	window.location.reload();
}

function htmlEncode(value){
  return $('<div/>').text(value).html();
}





