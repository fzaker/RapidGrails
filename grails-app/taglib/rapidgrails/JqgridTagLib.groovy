package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import grails.converters.JSON

class JqgridTagLib {
    def static namespace = "rg"

    def jqgridResources = {
        //out << g.javascript(library: 'jquery')
        //out << r.layoutResources()

        //out << jqui.resources(theme: 'cobalt')

        def localeURL = ""
        if (grailsApplication.config.rapidgrails.application.direction == "rtl") {
            localeURL = g.resource(plugin: 'rapid-grails', dir: 'jqgrid/js/i18n', file: 'grid.locale-fa.js')
        } else {
            localeURL = g.resource(plugin: 'rapid-grails', dir: 'jqgrid/js/i18n', file: 'grid.locale-en.js')
        }
        def localeTag = "<script type=\"text/javascript\" src=\"${localeURL}\"></script>"
        out << localeTag

        def jsURL = g.resource(plugin: 'rapid-grails', dir: 'jqgrid/js', file: 'jquery.jqGrid.min.js')
        def scriptTag = "<script type=\"text/javascript\" src=\"${jsURL}\"></script>"
        out << scriptTag

        out << g.javascript(src: 'criteria.js', plugin: 'rapid-grails')

        cssLink(out, 'jqgrid/css', 'ui.jqgrid.css')
        cssLink(out, 'css', 'rapidgrails.css')
    }

    private def cssLink(out, dir, file) {
        def cssURL = resource(plugin: 'rapid-grails', dir: dir, file: file)
        def cssLinkTag = "<link rel=\"stylesheet\" href=\"${cssURL}\"/>"
        out << cssLinkTag
    }

    def jqgrid = {attrs, body ->
        def maxColumnCount = attrs.columns ? attrs.columns.size() : (attrs.maxColumns ? Integer.parseInt(attrs.maxColumns) : 6)
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        def gridName = "${domainClass.shortName}${attrs.idPostfix?:""}Grid"

        def gridParams = [:]
        def commands = []
        request.setAttribute("gridName", gridName)
        request.setAttribute("domainClass", attrs.domainClass.name)
        request.setAttribute("gridParams", gridParams)
        request.setAttribute("commands", commands)
        out << body()
        request.removeAttribute("gridName")
        request.removeAttribute("domainClass")
        request.removeAttribute("gridParams")
        request.removeAttribute("commands")

        def excludedProperties = ["id", "version", "password"]
        def colNames = []
        def colModel = []
        def showFormatter = attrs.showFormatter
        if (!showFormatter)
            showFormatter = "show${domainClass.shortName}${attrs.idPostfix?:""}ItemFormatter"

        def showRowsOptions = ""
        if (attrs.showAllRows && Boolean.parseBoolean(attrs.showAllRows)){
            showRowsOptions = """
                rowList: [],          // disable page size dropdown
                pgbuttons: false,     // disable page control like next, back button
                pgtext: null,         // disable pager text like 'Page 0 of 10'
                viewrecords: false,    // disable current view record text like 'View 1-10 of 100'
                loadonce: true,
            """
        }
        else {
            showRowsOptions = "rowNum:10,"
        }

        if (!attrs.showFirstColumn || Boolean.parseBoolean(attrs.showFirstColumn)) {
            colModel << [name: "id", index: "id", width: attrs.firstColumnWidth?:20, formatter:showFormatter]
            colNames << ""
        }
        def footerRow = Boolean.parseBoolean(attrs.footerRow)
        def footerRowFillCode = ""
        if (footerRow)
            footerRowFillCode = """
        var g = jQuery("#${gridName}");
        var userData = g.jqGrid("getGridParam","userData");
        g.jqGrid("footerData","set",userData,false);
        """

        def columnProperties = [:]
        if (attrs.columnProperties)
            columnProperties = attrs.columnProperties

        if (attrs.columns) {
            attrs.columns.each {
                //colNames << it.name
                colNames << message(code: "${domainClass.propertyName}.${it.name}", default: it.name)
                def width = (columnProperties[it.name]?.width)?:100
                def c = [name: it.name, index: it.name, width: width]
                def property = domainClass.propertyMap[it.name]
                if (it.formatter)
                    c.formatter = it.formatter
                else if (property && (property.type in [java.lang.Double, java.lang.Integer, java.lang.Long, java.lang.Short]))
                    c.formatter = "currency"
                if (it.formatoptions)
                    c.formatoptions = it.formatoptions
                else if (property && (property.type in [java.lang.Double, java.lang.Integer, java.lang.Long, java.lang.Short]))
                    c.formatoptions = [thousandsSeparator: ",", decimalPlaces:0]
                colModel << c
            }
        } else {
            def hasTransients = domainClass.clazz.metaClass.hasProperty(domainClass.clazz, "transients")
            def domainClassProperties = domainClass.constraints.keySet() + (hasTransients ? domainClass.clazz["transients"] : [])
            domainClassProperties.eachWithIndex {it, index->
                if (!excludedProperties.contains(it) && (index < maxColumnCount)) {
                    colNames << message(code: "${domainClass.propertyName}.${it}", default: it)

                    def width = (columnProperties[it]?.width)?:100
                    def c = [name: it, index: it, width: width, classes: "ltr"]
                    def property = domainClass.propertyMap[it]
                    if (property.type in [java.lang.Double, java.lang.Integer, java.lang.Long, java.lang.Short])
                        c.formatter = "currency"
                    if (property && (property.type in [java.lang.Double, java.lang.Integer, java.lang.Long, java.lang.Short]))
                        c.formatoptions = [thousandsSeparator: ",", decimalPlaces:0]
                    colModel << c
                }
            }
        }

        def filter = gridParams.filter?:""

        def url = attrs.url ?: """${g.createLink(controller: "rapidGrails", action: "jsonList")}?maxColumns=${maxColumnCount}&domainClass=${domainClass.fullName}&filter=${filter}"""

        if (attrs.columns) {
            def jsonColumns = ("" + (attrs.columns as JSON)).replaceAll("\"", "'")
            url += "&columns=${jsonColumns}"
        }
        if (attrs.source) {
            def jsonColumns = ("" + (attrs.source as JSON)).replaceAll("\"", "'")
            url += "&source=${jsonColumns}"
        }
        if (attrs.showFirstColumn && !Boolean.parseBoolean(attrs.showFirstColumn)) {
            url += "&showFirstColumn=false"
        }
        def groupby = ""
        if (attrs.groupby) {
            if ((attrs.groupby instanceof Collection) || (attrs.groupby.getClass().isArray()))
                groupby = """grouping:true, groupingView : { groupField : ${attrs.groupby as JSON}, groupColumnShow : ${(attrs.groupby.collect { false }) as JSON}, groupText : ['<b>{0}</b>'] },"""
            else
                groupby = """grouping:true, groupingView : { groupField : ['${attrs.groupby}'], groupColumnShow : [false], groupText : ['<b>{0}</b>'] },"""
        }
        def ondblClickRow = ""
        if (attrs.ondblClickRow) {
            ondblClickRow = "ondblClickRow: function(rowId) { ${attrs.ondblClickRow}(rowId); },"
        }

        def onSelectRow = ""
        if (attrs.onSelectRow || attrs.childGrid) {
            onSelectRow = "onSelectRow: function(rowId) {"
            if(attrs.onSelectRow)
                onSelectRow += "${attrs.onSelectRow}(rowId);"
            if(attrs.childGrid){
                if(attrs.childGrid instanceof Map){
                    attrs.childGrid.each {childGrid,fieldInChild->
                        def criteria = "'[{\\\'op\\\':\\\'eq\\\', \\\'field\\\':\\\'${fieldInChild}.id\\\', \\\'val\\\':\\\'' + rowId + '\\\'}]'"
                        def handler = "loadGridWithCriteria('${childGrid}Grid', ${criteria});"
                        onSelectRow += handler
                    }
                }
                else{
                    def criteria = "'[{\\\'op\\\':\\\'eq\\\', \\\'field\\\':\\\'${attrs.fieldInChild}.id\\\', \\\'val\\\':\\\'' + rowId + '\\\'}]'"
                    def handler = "loadGridWithCriteria('${attrs.childGrid}Grid', ${criteria})"
                    onSelectRow += handler
                }
            }
            onSelectRow += "},"
        }

        def treeParams = ""
        def datatypeDefinition = "datatype: \"json\""
        if (attrs.tree) {
            //datatypeDefinition = "treedatatype: \"json\""
            treeParams = "treeGrid: true, treeGridModel : 'adjacency', ExpandColumn : '${domainClass.constraints.find().key}',"
            url += "&tree=${attrs.tree}"
        }


        def caption
        if (attrs.caption != null)
            caption = attrs.caption
        else {
            //def defaultCaption = message(code: "${domainClass.propertyName}.label")
            def arg = message(code: domainClass.propertyName)//, default: defaultCaption)
            def default2 = message(code: "default.list.label", args: [arg], default: "${domainClass.shortName} List")
            caption = message(code: "${domainClass.propertyName}.list", default: default2)
        }

        def tagBody = """
        <div id="${gridName}Container">
        <table id="${gridName}" ${attrs.width? "style=\"width:${attrs.width}\"" : ""}></table>
        <div id="${domainClass.shortName}${attrs.idPostfix?:""}Pager"></div>
        <script type="text/javascript">
            ${attrs.tree?"""
            function reload${gridName}Node(id,lastExpandedStatus){
                var grid = jQuery("#${gridName}")
                var p=grid[0].p
                var node=p.data[p._index[id]]
                var collapse=false
                if(!node.expanded && lastExpandedStatus)
                    collapse=true
                grid.collapseRow(node)
                grid.collapseNode(node)
                var children=grid.getNodeChildren(node)
                for(var i=0;i<children.length;i++)
                {
                    grid.delTreeNode(children[i].id)
                }
                node.loaded=false
                grid.expandRow(node)
                grid.expandNode(node)
                if(collapse){
                    grid.collapseRow(node)
                    grid.collapseNode(node)
                }
            }
            """:""}
            jQuery("#${gridName}").jqGrid({
                direction: "${grailsApplication.config.rapidgrails.application.direction?:"ltr"}",
                ${treeParams}
                url: "${url}",
                ${datatypeDefinition},
                colNames:${colNames as JSON},
                colModel:${colModel as JSON},
                rowList:[10,20,30,50,100],
                pager: '#${domainClass.shortName}${attrs.idPostfix?:""}Pager',
                ${attrs.sortname?"sortname:'"+attrs.sortname+"',":''}
                //sortname: 'id',
                viewrecords: true,
                ${attrs.sortorder?"sortorder:'"+attrs.sortorder+"',":''}
                //sortorder: "asc",
                autowidth: true,
                //width: 800,
                height: "100%",
                ${showRowsOptions}
                ${footerRow?"footerrow : true,userDataOnFooter : false,":""}
                loadComplete: function(data) {
                    ${footerRowFillCode}
                    ${attrs.loadComplete?attrs.loadComplete+"(data)":""}
                },
                gridComplete:function(data){
                    ${attrs.gridComplete?attrs.gridComplete+"(data)":""}
                },
                ${groupby}
                ${ondblClickRow}
                ${onSelectRow}
                cellLayout: 5,
                altRows: true,
                altclass: "altrow",
                caption: "${caption}"
            });
            jQuery("#${gridName}").jqGrid('navGrid', '#${domainClass.shortName}${attrs.idPostfix?:""}Pager', {edit:false,add:false,del:false,search:false});


        </script>
        </div>
        """

        def initialCommand = ""
        if (!attrs.showCommand || Boolean.parseBoolean(attrs.showCommand))
            initialCommand = """<a href=\\"${g.createLink(controller: domainClass.propertyName, action: "show")}/" + cellvalue + "\\"><img src=\\"${fam.icon(name: 'magnifier')}\\"/></a>"""

        out << """<script type="text/javascript">
        jQuery.extend(\$.fn.fmatter , {
            show${domainClass.shortName}${attrs.idPostfix?:""}ItemFormatter : function(cellvalue, options, rowdata) {
                var r = "${initialCommand}";
        """

        def allCommands = (attrs.commands?:[]) + commands
        allCommands?.each {
            def iconTitle=it.title?:message(code: it.icon)
            if (it.handler) {
                def handler = it.handler.replaceAll("#id#", "\" + cellvalue + \"")
                out << """r = r + "<a style='margin-right:3px;' href=\\"javascript:${handler}\\"><img src=\\"${fam.icon(name: it.icon)}\\" title=\\"${iconTitle}\\"/></a>";"""
            }
            else if (it.loadOverlay) {
                def remoteAddress = "'" + it.loadOverlay.replaceAll("#id#", "\" + cellvalue + \"") + "'"
                def loadCallback=it.loadCallback?","+it.loadCallback:""
                def saveCallback=it.saveCallback?it.saveCallback:"function(){\$('#${gridName}').trigger('reloadGrid')}"
                out << """r = r + "<a style='margin-right:3px;' href=\\"javascript:loadOverlay(${remoteAddress},'${it.saveAction}',${saveCallback}${loadCallback})\\"><img src=\\"${fam.icon(name: it.icon)}\\"  title=\\"${iconTitle}\\"/></a>";"""
            }
            else if (it.childGrid) {
                def criteria = "'[{\\\\\'op\\\\\':\\\\\'eq\\\\\', \\\\\'field\\\\\':\\\\\'${it.fieldInChild}.id\\\\\', \\\\\'val\\\\\':\\\\\'\" + cellvalue + \"\\\\\'}]'"
                def handler = "loadGridWithCriteria('${it.childGrid}Grid', ${criteria})"
                out << """r = r + "<a style='margin-right:3px;' href=\\"javascript:${handler}\\"><img src=\\"${fam.icon(name: it.icon)}\\"  title=\\"${iconTitle}\\"/></a>";"""
            }
            else {
                def param = it.param.replaceAll("#id#", "\" + cellvalue + \"")
                out << """r = r + "<a style='margin-right:3px;' href=\\"${g.createLink(controller: it.controller, action: it.action) + "?" + param}\\"><img src=\\"${fam.icon(name: it.icon)}\\" title=\\"${iconTitle}\\" /></a>";"""
            }
        }

        out << """
                return r;
            }
        });
        </script>"""

        out << tagBody

        attrs.toolbarCommands?.each{
            out << """
            <script type=\"text/javascript\">
                jQuery("#${gridName}").jqGrid('navButtonAdd', '#${domainClass.shortName}${attrs.idPostfix?:""}Pager', {
                    caption:"${it.caption}",
                    buttonicon:"ui-icon-${it.icon?:it.caption}",
                    onClickButton:function(){${it.function}();}
                });
            </script>
            """;
        }
    }

    def commands = { attrs, body ->
        body()
    }

    def deleteCommand = { attrs, body ->
        def commands = request.getAttribute("commands")
        def gridName = request.getAttribute("gridName")
        def domainClass = request.getAttribute("domainClass")
        def deleteUrl = attrs.deleteURL?:createLink(controller: "rapidGrails", action: "delete")

        commands << [handler: "genericDelete('${deleteUrl}', '${gridName}', '${domainClass}', #id#)", icon: "cross"]
    }
}
