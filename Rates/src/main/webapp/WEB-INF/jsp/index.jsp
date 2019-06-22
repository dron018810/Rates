<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
	<head>
		
		<script type="text/javascript" src="https://cdn.jsdelivr.net/jquery/latest/jquery.min.js"></script>
		<script type="text/javascript" src="https://cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>
		<script type="text/javascript" src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js"></script>		
		<script src="https://canvasjs.com/assets/script/jquery.canvasjs.min.js"></script>
		<link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css" />
		
		<script>
		
			$(document).ready(function() {	

				var dataPoints = [];
				<c:forEach var="rate" items="${rates}" varStatus="status">
			        dataPoints.push({
			        	x: new Date('${rate.stringDate}'),
						y: ${rate.rate},
						stringDate : '${rate.stringDate}',
			        	rate : ${rate.rate}
					});  
	        	</c:forEach>
	        	
	        	buildResultsTable(dataPoints);
	        	buildChart(dataPoints);

				$("#downloadExchangeRates").click(function() {
					$.ajax({
				        type: "POST",
				        url: "${pageContext.request.contextPath}/downloadExchangeRatesAction",
				        dataType: 'json',
				        data: {
				        	daterange : $('input[name="daterange"]').val()
				        },
				        success: function(data) {
				        	buildResultsTable(data);
				        	buildChart(getDataPointsFromData(data));
				        },
				        error: function(data) {
				            alert("error");
				        }
				    });
					
				});
				
				$("#downloadExcelRates").click(function() {
					
					var ids_array = [];
				    $('input[name="ids"]').each(function() {
				    	ids_array.push($(this).val());
				    });
				    
				    var dates_array = [];
				    $('input[name="dates"]').each(function() {
				    	dates_array.push($(this).val());
				    });
				    
				    var rates_array = [];
				    $('input[name="rates"]').each(function() {
				    	rates_array.push($(this).val());
				    });				    				  
				    
					$.ajax({
				        type: "POST",
				        url: "${pageContext.request.contextPath}/downloadExcelRatesAction",
				        data: {
				        	ids : JSON.stringify(ids_array),
				        	dates : JSON.stringify(dates_array),
				        	rates : JSON.stringify(rates_array)
				        },
				        success: function(data) {
				        	window.location.href = '${pageContext.request.contextPath}/downloadFile/' + data;
				        },
				        error: function(data) {
				            alert("error");
				        }
				    });
				});
			});
			
			function buildResultsTable(data) {
				var trHTML = '';				
				trHTML += '<tr><td><b>Date</b></td><td><b>Exchange rate</b></td></tr>';
				if(data.length > 0) {
					var rateRowRowspan = 1;
					var rateRowRowspanFlag = false;
			        $.each(data, function (index, rate) {
			        	if(rateRowRowspan == 1) {
			        		rateRowRowspanFlag = false;
				        	for(var i = index; i < data.length; i++) {
				        		if(data[i+1] != undefined && data[i].rate == data[i+1].rate) {
					        		rateRowRowspan++;
					        	} else {
					        		break;
					        	}
				        	}
			        	}			       
			        	
			        	trHTML += '<tr>';
			        	var dateRow = '<td>' + rate.stringDate  + '<input type=\'hidden\' name=\'dates\' value=\'' + rate.stringDate + '\'/></td>';			        				        				      
			        	trHTML += dateRow;
			        	if(!rateRowRowspanFlag) {
			        		var rateRow = '<td rowspan=\'' + rateRowRowspan + '\'>' + rate.rate  + '<input type=\'hidden\' name=\'rates\' value=\'' + rate.rate + '\'/></td>';
			        	} else {
			        		var rateRow = '<input type=\'hidden\' name=\'rates\' value=\'' + rate.rate + '\'/>';
			        		rateRowRowspan--;
			        	}
			        	trHTML += rateRow;
			            trHTML += '</tr>';
      					
			            rateRowRowspanFlag = true;
			            
			        });
				}
		        $('#results').html(trHTML);				
			}
			
			function buildChart(dataPoints) {
				var options =  {
					animationEnabled: true,
					theme: "light2",
					title: {
						text: "Rates"
					},
					axisX: {
						valueFormatString: "YYYY-MM-DD ",
					},
					axisY: {
						title: "USD",
						titleFontSize: 24,
						includeZero: false
					},
					data: [{
						type: "spline", 
						yValueFormatString: "$#,###.##",
						dataPoints: dataPoints
					}]
				};
				$("#chartContainer").CanvasJSChart(options);
			}
			
			function getDataPointsFromData(data) {
				var dataPoints = [];
				$.each(data, function (index, rate) {
					dataPoints.push({
						x: new Date(rate.stringDate),
						y: rate.rate
					});
		        });
				return dataPoints;
			}		
		</script>
	
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Rates</title>
        <style type="text/css">

        </style>
    </head>
    <body>
       	<table border="1">
               <tr>
                   <td>
                   	<a href="javascript:;" id="downloadExchangeRates">Download exchange rates</a>      	
                   </td>
                   <td>
                   	<a href="javascript:;" id="downloadExcelRates">Download Excel</a>
                   </td>
               </tr>
               <tr>
               	<td>							                   
                   	<input type="text" name="daterange" value="2019-01-01 - 2019-02-01" />
					<script>
						$(function() {
						  $('input[name="daterange"]').daterangepicker({
						  	onSelect: function(date) {
					            alert();
					        },
						    opens: 'left',
						    locale: {
				    	    	format: 'YYYY-MM-DD'
				    	    }
						  });
						});
						$('input[name="daterange"]').on('apply.daterangepicker', function (ev, picker) {
							$.ajax({
						        type: "POST",
						        url: "${pageContext.request.contextPath}/applyDaterangepickerAction",
						        dataType: 'json',
						        data: {
						        	daterange : $('input[name="daterange"]').val()
						        },
						        success: function(data) {
						        	buildResultsTable(data);
						        	buildChart(getDataPointsFromData(data));
						        },
						        error: function(data) {
						            alert("error");
						        }
						    });
						});
					</script>
				</td>
               </tr>
		</table>
		
		<div id="chartContainer" style="height: 370px; width: 100%;"></div>
		
		
		<table border="1" id="results">		
                
        </table>              
    </body>
</html>