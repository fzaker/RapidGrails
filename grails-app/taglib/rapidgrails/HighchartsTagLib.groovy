package rapidgrails

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import rapidgrails.reporting.ChartBuilder
import rapidgrails.reporting.ReportDataReader
import rapidgrails.reporting.Chart

class HighchartsTagLib {
    static namespace = "rg"
    def messageSource

    def colors = ['#4572A7', '#AA4643', '#89A54E', '#80699B', '#3D96AE', '#DB843D', '#92A8CD', '#A47D7C', '#B5CA92']

    def highchartsResources = {
        def jsURL = g.resource(plugin: 'rapid-grails', dir: 'highcharts', file: 'highcharts.src.js')
        def scriptTag = "<script type=\"text/javascript\" src=\"${jsURL}\"></script>"
        out << scriptTag

//        def themeURL = g.resource(plugin: 'rapid-grails', dir: 'highcharts/themes', file: 'dark-green.js')
//        def themeScriptTag = "<script type=\"text/javascript\" src=\"${themeURL}\"></script>"
//        out << themeScriptTag
    }

    def chart = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        Closure<?> chartsClosure = GrailsClassUtils.getStaticPropertyValue(domainClass.getClazz(), "charts")
        chartsClosure.setResolveStrategy(Closure.DELEGATE_ONLY)
        def chartBuilder = new ChartBuilder()
        chartsClosure.setDelegate(chartBuilder)
        chartsClosure.call()
        def chartName = attrs.chart
        def chart = chartBuilder.getCharts().find { it.title == attrs.chart }

        "${chart.chartType}Chart"(attrs, body, chart, domainClass)
    }

    def barChart = { attrs, body, chart, domainClass ->
        params.chart=chart
        def serviceResponse = ReportDataReader.fromService(domainClass.propertyName, "report", params)
        def list = serviceResponse.list
        def locale = new Locale("en")
        def series = chart.columns.collect { col->
            [name: messageSource.getMessage("${domainClass.propertyName}.${col.title}", null, col.title, locale), data: list.collect {it."${col.title}"}]
//            [name: "${col.title}", data: list.collect {it."${col.title}"}]
        }
        def xAxis = list.collect {it."${chart.variable}"}
        def background = "backgroundColor:'#F5F5F5',"
        def tagBody = """
            <script type="text/javascript">
                var chart;
                jQuery(document).ready(function() {
                   chart = new Highcharts.Chart({
                      chart: {
                         renderTo: '${attrs.id}',
                         ${background}
                         defaultSeriesType: '${attrs.type ?: "column"}'
                      },
                      title: {
                         text: '${messageSource.getMessage("${domainClass.propertyName}.${chart.title}.chart", null, chart.title, locale)}'
                      },
                      subtitle: {
                         text: '${messageSource.getMessage("${domainClass.propertyName}.${chart.title}.chart.subtitle", null, chart.subtitle, locale)}'
                      },
                      xAxis: {
                         categories: ${xAxis as JSON}
                      },
                      yAxis: {
                         //min: 0,
                         title: {
                            text: '${chart.yTitle}'
                         }
                      },
                      /*legend: {
                         layout: 'vertical',
                         backgroundColor: '#FFFFFF',
                         align: 'left',
                         verticalAlign: 'top',
                         x: 100,
                         y: 70,
                         floating: true,
                         shadow: true
                      },*/
                      tooltip: {
                         formatter: function() {
                            return ''+
                               this.x +': '+ this.y;
                         }
                      },
                      credits: {
                         enabled: false
                      },
                      plotOptions: {
                         column: {
                            pointPadding: 0.2,
                            borderWidth: 0
                         }
                      },
                      series: ${series as JSON}
                   });
                });
                </script>
                <div id="${attrs.id}"></div>
            """
        out << tagBody
    }

    def pieChart = { attrs, body, Chart chart, domainClass ->
        params.chart=chart
        def serviceResponse = ReportDataReader.fromService(domainClass.propertyName, "report", params)
        def list = serviceResponse.list
        def locale = new Locale("en")
        def data
        def clickHandler = ""
        if (!chart.groupBy)
            data = list.collect { [ name: it."${chart.variable}", y: Double.parseDouble(String.format("%.2f", it."${chart.columns[0].title}")) ] }
        else {
            data = []
            def groupedList = list.groupBy {it."${chart.groupBy}"}
            groupedList.eachWithIndex { key, subList, indx ->
                def sum = 0
                def drilledDownData = []
                subList.eachWithIndex { it, indx2 ->
                    sum += it."${chart.columns[0].title}"
                    drilledDownData << [name: it."${chart.variable}", y: Double.parseDouble(String.format("%.2f", it."${chart.columns[0].title}")), color: "${colors[indx2]}"]
                }
                data << [name: key.toString(), y: Double.parseDouble(String.format("%.2f", sum)), drilledDownData: drilledDownData, color: "${colors[indx]}"]
            }

            clickHandler = """
                click: function() {
                    var drilldown = this.drilledDownData;
                    if (drilldown) {
                        setChart${attrs.id}(drilldown);
                    } else {
                        setChart${attrs.id}(data);
                    }
                }
            """
        }

        def background = "backgroundColor:'#F5F5F5',"

        def tagBody = """
                <script type="text/javascript">
                var chart${attrs.id};
                jQuery(document).ready(function() {
                    var data = ${data as JSON};

                    function setChart${attrs.id}(data) {
                        chart${attrs.id}.series[0].remove();
                        chart${attrs.id}.addSeries({
                            type: 'pie',
                            name: 'Browser share',
                            data: data
                        });
                    }

                    chart${attrs.id} = new Highcharts.Chart({
                        chart: {
                            renderTo: '${attrs.id}',
                            plotBackgroundColor: null,
                            plotBorderWidth: null,
                            ${background}
                            plotShadow: false
                        },
                        title: {
                            text: '${messageSource.getMessage("${domainClass.propertyName}.${chart.title}.chart", null, chart.title, locale)}'
                        },
                        subtitle: {
                            text: '${messageSource.getMessage("${domainClass.propertyName}.${chart.title}.chart.subtitle", null, "", locale)}'
                        },
                        tooltip: {
                            formatter: function() {
                                return  this.point.name?this.point.name +': '+ Math.round(this.percentage*100)/100 +' %':'';
                            }
                        },
                        plotOptions: {
                            pie: {
                                allowPointSelect: ${!chart.groupBy},
                                point: {
                                    events: {
                                        ${ chart.groupBy ? clickHandler : ""}
                                    }
                                },
                                cursor: 'pointer',
                                dataLabels: {
                                    enabled: true,
                                    color: '#000000',
                                    connectorColor: '#000000',
                                    formatter: function() {
                                        return  this.point.name? Math.round(this.percentage*100)/100 +' %':'';
                                    }
                                }
                            }
                        },
                        series: [{
                            type: 'pie',
                            name: 'Browser share',
                            data: data
                        }]
                    });
                });
                </script>
                <div id="${attrs.id}"></div>
            """
        out << tagBody
    }

    def pieDonutChart = { attrs, body, chart, domainClass ->
        tagBody = """
                <script type="text/javascript">
                var chart;
                //jQuery(document).ready(function() {
                    var colors = Highcharts.getOptions().colors,
                            categories = ['MSIE', 'Firefox', 'Chrome', 'Safari', 'Opera'],
                            name = 'Browser brands',
                            data = [{
                                y: 55.11,
                                color: colors[0],
                                drilldown: {
                                    name: 'MSIE versions',
                                    categories: ['MSIE 6.0', 'MSIE 7.0', 'MSIE 8.0', 'MSIE 9.0'],
                                    data: [10.85, 7.35, 33.06, 2.81],
                                    color: colors[0]
                                }
                            }, {
                                y: 21.63,
                                color: colors[1],
                                drilldown: {
                                    name: 'Firefox versions',
                                    categories: ['Firefox 2.0', 'Firefox 3.0', 'Firefox 3.5', 'Firefox 3.6', 'Firefox 4.0'],
                                    data: [0.20, 0.83, 1.58, 13.12, 5.43],
                                    color: colors[1]
                                }
                            }, {
                                y: 11.94,
                                color: colors[2],
                                drilldown: {
                                    name: 'Chrome versions',
                                    categories: ['Chrome 5.0', 'Chrome 6.0', 'Chrome 7.0', 'Chrome 8.0', 'Chrome 9.0',
                                            'Chrome 10.0', 'Chrome 11.0', 'Chrome 12.0'],
                                    data: [0.12, 0.19, 0.12, 0.36, 0.32, 9.91, 0.50, 0.22],
                                    color: colors[2]
                                }
                            }, {
                                y: 7.15,
                                color: colors[3],
                                drilldown: {
                                    name: 'Safari versions',
                                    categories: ['Safari 5.0', 'Safari 4.0', 'Safari Win 5.0', 'Safari 4.1', 'Safari/Maxthon',
                                            'Safari 3.1', 'Safari 4.1'],
                                    data: [4.55, 1.42, 0.23, 0.21, 0.20, 0.19, 0.14],
                                    color: colors[3]
                                }
                            }, {
                                y: 2.14,
                                color: colors[4],
                                drilldown: {
                                    name: 'Opera versions',
                                    categories: ['Opera 9.x', 'Opera 10.x', 'Opera 11.x'],
                                    data: [ 0.12, 0.37, 1.65],
                                    color: colors[4]
                                }
                            }];


                    // Build the data arrays
                    var topLevelData = [];
                    var drilledDownData = [];
                    for (var i = 0; i < data.length; i++) {

                        // add browser data
                        topLevelData.push({
                            name: categories[i],
                            y: data[i].y,
                            color: data[i].color
                        });

                        // add version data
                        for (var j = 0; j < data[i].drilldown.data.length; j++) {
                            var brightness = 0.2 - (j / data[i].drilldown.data.length) / 5 ;
                            drilledDownData.push({
                                name: data[i].drilldown.categories[j],
                                y: data[i].drilldown.data[j],
                                color: Highcharts.Color(data[i].color).brighten(brightness).get()
                            });
                        }
                    }

                    // Create the chart
                    chart = new Highcharts.Chart({
                        chart: {
                            renderTo: '${attrs.id}',
                            type: 'pie'
                        },
                        title: {
                            text: '${messageSource.getMessage("${domainClass.propertyName}.${chart.title}.chart", null, chart.title, locale)}'
                        },
                        yAxis: {
                            title: {
                                text: 'Total percent market share'
                            }
                        },
                        plotOptions: {
                            pie: {
                                shadow: false
                            }
                        },
                        tooltip: {
                            formatter: function() {
                                return '<b>'+ this.point.name +'</b>: '+ this.y +' %';
                            }
                        },
                        series: [{
                            name: 'Browsers',
                            data: topLevelData,
                            size: '60%',
                            dataLabels: {
                                formatter: function() {
                                    return this.y > 5 ? this.point.name : null;
                                },
                                color: 'white',
                                distance: -30
                            }
                        }, {
                            name: 'Versions',
                            data: drilledDownData,
                            innerSize: '60%',
                            dataLabels: {
                                formatter: function() {
                                    // display only if larger than 1
                                    return this.y > 1 ? '<b>'+ this.point.name +':</b> '+ this.y +'%'  : null;
                                }
                            }
                        }]
                    });
                //});
                </script>
                <div id="${attrs.id}"></div>
            """

    }
}
