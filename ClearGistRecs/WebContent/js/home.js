$(document).ready(function() {
	var options = initChartOptions("spline", "chart-container", "Requests");
    jQuery.ajax({
    cache:false,
    url:homeUrl1+ '?nd='+new Date().getTime()+"&id="+$("#hiddenTenantId").val()+"&metric=REC_SERVED"+"&period=LAST_30DAYS",
    dataType:"json",
    type:"POST",
    success:function(json){
        if (json.answer == "true") {
                //set performance span
	        	document.getElementById("spanPerformance").innerHTML ='';
	            document.getElementById("spanPerformance").innerHTML =json.spanPerf;
                
                for (var p = 0; p < json.period.length; p++) {
                    options.xAxis.categories.push(json.period[p]);
                }
 
            	var series = {
                    data: []
                };
                series.name = json.name;
                series.color = json.color;
                series.type = json.type;

                for (var j = 0; j < json.data.length; j++) {
                    series.data.push(json.data[j]);
                }
                
                options.series.push(series);
                
                options.title.text = json.title;

                options.yAxis = {
                    title: {
                        text: json.name
                    },
                    allowDecimals: false,
                    min:0
                }

                var chart = new Highcharts.Chart(options);

               jQuery("#chart-container text tspan").each(function (s, E) {
                    if (jQuery(E).text().toLowerCase() == "highcharts.com") {
                        jQuery(E).text("");
                    }
               });
               
            } else {
                if (json.answer == "false") {}
            }
        },
        error: function (s, i, json) {
            alert(i + "," + json)
        }
    });

});

function selMetricList(obj){
	if ($("#selMetric").val() == "widg_purch" || $("#selMetric").val() == "widg_aov" || $("#selMetric").val() == "clicked" || $("#selMetric").val() == "unique") {
		$("#"+obj).show('slow');
	} else {
		$("#"+obj).hide('slow');
	}
	
    
	getStats();
}

function changePeriod(period) {
	$("#hiddenPeriod").val(period);
	$("#daySpan").removeClass("red");
	$("#monthSpan").removeClass("red");
	$("#yearSpan").removeClass("red");
	
	if (period=="LAST_30DAYS")
		$("#daySpan").addClass("red");
	else if (period=="LAST_6MONTHS")
		$("#monthSpan").addClass("red");
	else
		$("#yearSpan").addClass("red");

	
	getStats();
}

function getStats() {
	
	document.getElementById("spanPerformance").innerHTML ='';
    document.getElementById("spanPerformance").innerHTML = '<img src="../images/loader-stats.gif" style="padding-top: 10px;" />';
    
	var metric = $("#selMetric").val();
	var period = $("#hiddenPeriod").val();

	var options = initChartOptions("spline", "chart-container", "Requests");

    jQuery.ajax({
    cache:false,
    url:homeUrl1+ '?nd='+new Date().getTime()+"&id="+$("#hiddenTenantId").val()+"&metric="+metric+"&period="+period,
    dataType:"json",
    type:"POST",
    success:function(json){
        if (json.answer == "true") {
                //set performance span
                document.getElementById("spanPerformance").innerHTML ='';
                document.getElementById("spanPerformance").innerHTML =json.spanPerf;
                
                for (var p = 0; p < json.period.length; p++) {
                    options.xAxis.categories.push(json.period[p]);
                }
 
            	var series = {
                    data: []
                };
                series.name = json.name;
                series.color = json.color;
                series.type = json.type;

                for (var j = 0; j < json.data.length; j++) {
                    series.data.push(json.data[j]);
                }
                
                options.series.push(series);
                
                options.title.text = json.title;

                options.yAxis = {
            		title: {
                        text: json.name
                    },
                    allowDecimals: true,
                    min:0
                }

                var chart = new Highcharts.Chart(options);

               jQuery("#chart-container text tspan").each(function (s, E) {
                    if (jQuery(E).text().toLowerCase() == "highcharts.com") {
                        jQuery(E).text("");
                    }
               });
               
            } else {
                if (json.answer == "false") {}
            }
        },
        error: function (s, i, json) {
            alert(i + "," + json)
        }
    });
}

function initChartOptions (chartType, container, axisDesc) {
    
    //column and spline
        if (chartType == "column" || chartType == "spline") {
            var options = {
                chart: {
                    renderTo: container,
                    zoomType: 'xy',
                    defaultSeriesType: chartType
                },
                title: {
                    text: ''
                },
                subtitle: {
                    text: 'Source: '+$("#hiddenTenantSite").val()
                },
                xAxis: {
                    categories: []
                },
                /*yAxis: {
                    title: {
                        text: 
                    }
                },*/
              legend: {
                 layout: 'vertical',
                 align: 'right',
                 x: 0,
                 verticalAlign: 'top',
                 y: 48,
                 floating: true
              },
              plotOptions: {
                 column: {
                    stacking: 'normal'
                 }
              },
                series: []
            };
            
        //pie
        } else if (chartType == "pie") {
            var options = {
                colors: ["#DDDF0D", "#7798BF", "#55BF3B", "#DF5353", "#aaeeee", "#ff0066", "#eeaaee", 
      "#55BF3B", "#DF5353", "#7798BF", "#aaeeee"],
                chart: {
                    renderTo: container,
                    defaultSeriesType: chartType
                },
                title: {
                    text: ''
                },
                subtitle: {
                    color: '#DDD',
                    text: 'Source: '
                },
              tooltip: {
                 formatter: function() {
                    return '<b>'+ this.series.name +'</b><br/>'+ 
                       this.point.name +': '+ this.y;
                 }
              },
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                           /*enabled: true,*/
                           color: '#CCC',
                           formatter: function() {
                              return '<b>'+ this.point.name +'</b>: '+ this.y;
                           }
                        },
                        marker: {
                            lineColor: '#333'
                         }
                    }
                },
                series: []
            };
        } else if (chartType == "mixed") {
            var options = {
                chart: {
                    renderTo: container,
                    zoomType: 'xy'
                },
                title: {
                    text: ''
                },
                subtitle: {
                    text: 'Source: '
                },
                xAxis: {
                    categories: []
                },
                yAxis: [{ // Primary yAxis
                    labels: {
                    /*formatter: function() {
                       return this.value;
                    },*/
                    style: {
                       color: '#4572A7'
                    }
                    },
                    title: {
                    text: 'First',
                    style: {
                       color: '#4572A7'
                    }
                    }

                    }, { // Secondary yAxis
                    gridLineWidth: 0,
                    title: {
                    text: 'Second',
                    style: {
                       color: '#AA4643'
                    }
                    },
                    opposite: true,
                    labels: {
                    /*formatter: function() {
                       return this.value;
                    },*/
                    style: {
                       color: '#AA4643'
                    }

                    }
                }],
              legend: {
                 enabled:false
              },
                series: []
            };
            
        //mixed ends
        } else if (chartType == "area") {
            var options = {
                chart: {
                    renderTo: container,
                    zoomType: 'xy',
                    defaultSeriesType: chartType
                },
                title: {
                    text: ''
                },
                subtitle: {
                    text: 'Source: '
                },
                xAxis: {
                    categories: []
                },
                yAxis: [{ // Primary yAxis
                         labels: {
                            formatter: function() {
                               return this.value;
                            },
                            style: {
                               color: '#0099FF'
                            }
                         },
                         title: {
                            text: 'first',
                            style: {
                               color: '#0099FF'
                            }
                         }
                         
                      }
                      ],
              legend: {
                 enabled:false
              },
                series: []
            };
            
        //area ends
        }

    return options;
}
