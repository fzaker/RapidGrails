package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import grails.converters.JSON

class FormTagLib {
    static namespace = "rg"
    def fields = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.bean.class.name)
        def props = TaglibHelper.getDomainClassProperties(domainClass)
        def composites = attrs.bean.hasProperty("composites") ? attrs.bean.composites : null

        request.setAttribute("interceptCreateDialog", "")
        request.setAttribute("modify", [:])
        request.setAttribute("bean", attrs.bean)
        out << "<form>"
        out << "<div class='form-validation errors' style='display:none'>${message(code: 'form-errors')}</div>"
        out << "<div class=\"form-fields\"><div class=\"form-fields-part ${attrs.inline?"inline":""}\">"
        out << body()
        def modify = request.getAttribute("modify")
        def template = request.getAttribute("template")
        if (!template) {
//        out << g.hiddenField(name: "id", value: "0", "ng-model": "${domainClass.propertyName}Instance.id")
            def count = 0
            def newColumn = false
            props.each { p ->
                if (!modify.ignoredFields?.contains(p.name)) {
                    if (composites?.contains(p.name))
                        out << compositeRows(bean: attrs.bean, property: p.name, className: p.domainClass.propertyName)
                    else if (modify.hiddenReferences?.contains(p.name))
                        out << g.hiddenField(name: "${p.name}.id", "value": "{{${domainClass.propertyName}Instance.${p.name}.id}}")
                    else {
                        if (newColumn) {
                            out << "</div><div class=\"form-fields-part\">"
                            newColumn = false
                        }
                        def c = domainClass.constraints[p.name]

                        if (modify.readonlyFields?.contains(p.name)) {
                            out << f.field(bean: attrs.bean, property: p.name, "input-ng-model": "${domainClass.propertyName}Instance.${p.name}", "input-readonly": "true")
                        } else if (p.type == Date.class && c?.metaConstraints?.persian) {
                            out << """<div class="fieldcontain"><label for="${p.name}">${message(code: "${domainClass.propertyName}.${p.name}.label")}</label>"""
                            out << rg.datePicker(name: p.name, "input-ng-model": "${domainClass.propertyName}Instance.${p.name}")
                            out << "</div>"
                        } else {
                            def ngModel = "${domainClass.propertyName}Instance.${p.name}"
                            if (p.manyToOne || p.manyToMany || p.oneToOne)
                                ngModel += ".id"
                            def nullable = c.appliedConstraints.find { it.name == 'nullable' }.nullable
                            def fparams = [bean: attrs.bean, property: p.name, "input-ng-model": ngModel, "input-ngmodel": ngModel, required: !nullable]
                            if (!p.manyToOne)
                                fparams."input-valueMessagePrefix" = "${p.domainClass.propertyName}.${p.name}"

                            out << f.field(fparams)

                        }
                        count++
                    }
                    if (count >= 10) {
                        count = 0
                        newColumn = true
                    }
                }
            }
            modify.extraFields.each {
                out << it
            }
        }

        out << "</div></div>"
        out << "</form>"
        def angular = TaglibHelper.getBooleanAttribute(attrs, "angular", true)
        if (angular)
            out << """
                <script type="text/javascript">
                    function getFresh${domainClass.propertyName}Instance() {
                        return {};
                    }
                    function ${domainClass.propertyName}Controller(\$scope, \$http) {
                        \$scope.${domainClass.propertyName}Instance = getFresh${domainClass.propertyName}Instance();

                        \$scope.open${domainClass.propertyName.capitalize()}CreateDialog = function() {
                            \$scope.${domainClass.propertyName}Instance = getFresh${domainClass.propertyName}Instance();

                            ${request.getAttribute("interceptCreateDialog")}
                            if (!\$scope.\$\$phase)
                                \$scope.\$apply();

                            jQuery("#${domainClass.propertyName}").find('.form-validation').hide().html('${message(code: 'form-errors')}');
                            jQuery("#${domainClass.propertyName}").dialog('open');
                        }

                        \$scope.open${domainClass.propertyName.capitalize()}EditDialog = function() {
                            var selectedRow = jQuery('#${domainClass.shortName}Grid').jqGrid('getGridParam','selrow'); // returns id of selected object
                            var url = "${g.createLink(plugin: "rapid-grails", controller: "rapidGrails", action: "jsonInstance")}/" + selectedRow + "?domainClass=${domainClass.fullName}";
                            \$http.get(url).success(function(data, status, headers, config) {
                                \$scope.${domainClass.propertyName}Instance = removeNulls(data);
                                jQuery("#${domainClass.propertyName}").find('.form-validation').hide().html('${message(code: 'form-errors')}');
                                jQuery("#${domainClass.propertyName}").dialog('open');
                            });
                        }

                        \$scope.save${domainClass.propertyName.capitalize()} = function(dialogId, gridId, url, domainClass, params) {
                            if (\$scope.${domainClass.propertyName}Instance.id)
                                url = url + "/" + \$scope.${domainClass.propertyName}Instance.id;
                            sendSaveRequest(dialogId, gridId, url, domainClass, params);
                        }
                """
        props.each { p ->
            if (composites?.contains(p.name)) {
                out << """
                    \$scope.addComposite${p.name}= function() {
                        if(!\$scope.${domainClass.propertyName}Instance.${p.name})
                            \$scope.${domainClass.propertyName}Instance.${p.name}=[]
                        \$scope.${domainClass.propertyName}Instance.${p.name}[\$scope.${domainClass.propertyName}Instance.${p.name}.length]={}
                    }
                """
            }
        }
        out << """
                    }
                </script>
            """

        request.removeAttribute("interceptCreateDialog")
        request.removeAttribute("modify")
        request.removeAttribute("template")
        request.removeAttribute("bean")
    }

    def interceptCreateDialog = { attrs, body ->
        request.setAttribute("interceptCreateDialog", body())
    }
    def template = { attrs, body ->
        request.setAttribute("template", true)
        out << body()
    }
    def formColumn = { attrs, body ->
        out << "</div><div class=\"form-fields-part\">"
        out << body()
    }
    def field = { attrs, body ->
        def bean = request.getAttribute("bean")
        def composites = bean.hasProperty("composites") ? bean.composites : null
        def fieldName = attrs.name
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(bean.class.name)
        def p = domainClass.propertyMap[fieldName]
        if (p) {
            if (composites?.contains(p.name))
                out << compositeRows(bean: bean, property: p.name, className: p.domainClass.propertyName)
            else {

                def c = domainClass.constraints[p.name]

                if (p.type == Date.class && c?.metaConstraints?.persian) {
                    out << """<div class="fieldcontain"><label for="${p.name}">${message(code: "${domainClass.propertyName}.${p.name}.label")}</label>"""
                    out << rg.datePicker(name: p.name, "input-ng-model": "${domainClass.propertyName}Instance.${p.name}")
                    out << "</div>"
                } else {
                    def ngModel = "${domainClass.propertyName}Instance.${p.name}"
                    if (p.manyToOne || p.manyToMany || p.oneToOne)
                        ngModel += ".id"
                    out << f.field(bean: bean, property: p.name, "input-ng-model": ngModel, "input-valueMessagePrefix": "${p.domainClass.propertyName}.${p.name}")
                }
            }
        }

    }
    def modify = { attrs, body ->
        def modify = request.getAttribute("modify")
        modify.hiddenReferences = []
        modify.readonlyFields = []
        modify.ignoredFields = []
        modify.extraFields = []
        body()
    }

    def extraField = { attrs, body ->
        def modify = request.getAttribute("modify")
        def extraFields = modify.extraFields
        extraFields << body()
    }

    def hiddenReference = { attrs, body ->
        def modify = request.getAttribute("modify")
        def hiddenReferences = modify.hiddenReferences
        hiddenReferences << attrs.field
    }
    def ignoreField = { attrs, body ->
        def modify = request.getAttribute("modify")
        def ignoredFields = modify.ignoredFields
        ignoredFields << attrs.field
    }

    def readonlyField = { attrs, body ->
        def modify = request.getAttribute("modify")
        def readonlyFields = modify.readonlyFields
        readonlyFields << attrs.field
    }

    def angularController = { attrs, body ->
        def self = TaglibHelper.getBooleanAttribute(attrs, "self", true)
        def subclasses = TaglibHelper.getBooleanAttribute(attrs, "subClasses", false)

        out << "<script type=\"text/javascript\">"

        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        if (self)
            out << domainClassAngularGetFresh(domainClass)
        if (subclasses) {
            def subClasses = domainClass.subClasses
            subClasses.each {
                out << domainClassAngularGetFresh(it)
            }
        }

        out << "function ${domainClass.propertyName}Controller(\$scope, \$http) {"

        if (self)
            out << domainClassAngularController(domainClass)
        if (subclasses) {
            def subClasses = domainClass.subClasses
            subClasses.each {
                out << domainClassAngularController(it)
            }
        }

        out << "}"

        out << "</script>"
    }

    private domainClassAngularController(domainClass) {
        """
            \$scope.${domainClass.propertyName}Instance = getFresh${domainClass.propertyName}Instance();

            \$scope.open${domainClass.propertyName.capitalize()}CreateDialog = function() {
                \$scope.${domainClass.propertyName}Instance = getFresh${domainClass.propertyName}Instance();
                jQuery("#${domainClass.propertyName}").dialog('open');
            }

            \$scope.open${domainClass.propertyName.capitalize()}EditDialog = function() {
                var selectedRow = jQuery('#${domainClass.shortName}Grid').jqGrid('getGridParam','selrow'); // returns id of selected object
                var url = "${g.createLink(plugin: "rapid-grails", controller: "rapidGrails", action: "jsonInstance")}/" + selectedRow + "?domainClass=${domainClass.fullName}";
                \$http.get(url).success(function(data, status, headers, config) {
                    \$scope.${domainClass.propertyName}Instance = data;
                    jQuery("#${domainClass.propertyName}").dialog('open');
                });
            }

            \$scope.save${domainClass.propertyName.capitalize()} = function(dialogId, gridId, url, domainClass, params) {
                if (\$scope.${domainClass.propertyName}Instance.id)
                    url = url + "/" + \$scope.${domainClass.propertyName}Instance.id;
                sendSaveRequest(dialogId, gridId, url, domainClass, params);
            }
            """
    }

    private domainClassAngularGetFresh(domainClass) {
        """
        function getFresh${domainClass.propertyName}Instance() {
            return ${domainClass.newInstance() as JSON};
        }
        """
    }

    def compositeRows = { attrs, body ->
        out << g.render(template: "/template/compositeManyField", model: [instance: attrs.bean, className: attrs.className, compositeProperty: attrs.property], plugin: "rapid-grails")
    }

    def compositeRow = { attrs, body ->
        def parent = attrs.parent
        def index = (attrs.index == "_clone" || attrs.index == '{{$index}}') ? null : Integer.valueOf(attrs.index)
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(parent.class.name)
        GrailsDomainClassProperty compositeProperty = domainClass.propertyMap[attrs.compositeProperty]
        def compositeDomainClass = compositeProperty.referencedDomainClass
        def props = TaglibHelper.getDomainClassProperties(compositeDomainClass)

        def compositeInstance
        if (index != null)
            compositeInstance = parent."${attrs.compositeProperty}"[index]
        else
            compositeInstance = compositeDomainClass.newInstance()
        out << "<span style='display:none'>"
        out << g.textField(name: 'id', value: compositeInstance.id, "ng-model": "item.id").replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << g.hiddenField(name: 'deleted', value: compositeInstance.deleted).replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << g.hiddenField(name: 'new', value: attrs.index == '{{$index}}' ? 'false' : 'true').replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << '</span>'
        def excludedProperties = ["deleted", "indx"]
        def excludedTypes = [parent.class]
        if (compositeInstance.belongsTo instanceof Map)
            compositeInstance.belongsTo.each { excludedProperties << it.key }
        else if (compositeInstance.belongsTo instanceof List)
            excludedTypes = compositeInstance.belongsTo

        props.each { p ->
            if ((!excludedProperties.contains(p.name)) && (!excludedTypes.contains(p.type))) {
                def label = message(code: "${p.domainClass.propertyName}.${p.name}.label", default: message(code: "${p.name}.label", default: p.naturalName))
                def ngModel = "item.${p.name}"
//                if (p.oneToOne || p.manyToOne || p.manyToMany)
//                    ngModel += ".id"

                def field = f.input(bean: compositeInstance, property: p.name, class: "compositionField", placeholder: label, "ng-model": ngModel,
                        propertyIndex: attrs.index, compositeProperty: attrs.compositeProperty, "input-valueMessagePrefix": "${p.domainClass.propertyName}.${p.name}")
                if (p.oneToOne || p.manyToOne || p.manyToMany)
                    out << field
                else
                    out << field.replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
            }
        }
    }

    def crud = { attrs, body ->
        def domainClass = null
        def propertyName = "forwardingReference"
        def shortName = ""

        out << "<div id=\"list-${propertyName}\" ng-controller=\"${propertyName}Controller\" class=\"content scaffold-list\" role=\"main\">"
        out << rg.grid(domainClass: domainClass)
//                <rg:dialog id="${propertyName}" title="${shortName} Dialog">
//                    <rg:fields bean="${domainClass.newInstance()}"></rg:fields>
//                    <rg:saveButton domainClass="${domainClass}"/>
//                    <rg:cancelButton/>
//                </rg:dialog>
//                <input type="button" ng-click="open${shortName}CreateDialog()" value="Create ${shortName}"/>
//                <input type="button" ng-click="open${shortName}EditDialog()" value="Edit ${shortName}"/>
//            </div>
//        """
    }

    def tree = { attrs, body ->
        def bean = attrs.bean
        GrailsDomainClass domainClass = bean.domainClass
        String fieldName = attrs.field
        GrailsDomainClassProperty field = domainClass.properties.find { it.name == fieldName }
        GrailsDomainClass fieldDomainClass = field.referencedDomainClass;
        def relationFieldName = attrs.relationField
        GrailsDomainClassProperty relationField = fieldDomainClass.properties.find { it.name == relationFieldName }

        if (!attrs.titleProperty)
            attrs.titleProperty = 'name'

        def selectedIds
        if (field.oneToMany)
            selectedIds = bean.properties[fieldName].collect { it.id }.join(',')
        else
            selectedIds = bean.properties[relationFieldName].id.toString()

        out << "<input id=\"${fieldName}\" name=\"${fieldName}\" class=\"combotree\" ${attrs.width ? 'style=\"width:' + attrs.width + ';\"' : ''}  ${field.oneToMany ? 'multiple' : ''} "
        out << "data-options=\"url:'${createLink(controller: "rapidGrails", action: "treeStructure", params: [domainClass: fieldDomainClass.fullName, relationProperty: relationFieldName, titleProperty: attrs.titleProperty, selected: selectedIds])}'\">"
        out << "<script language=\"javascript\">"
        out << "\$('#${fieldName}').combotree({"

        if (attrs.cascadeCheck)
            out << "cascadeCheck:${attrs.cascadeCheck},"
        else
            out << "cascadeCheck:false,"

        if (attrs.onChange)
            out << "onChange:function(param){${attrs.onChange}(param);},"

        if (attrs.onLoadSuccess)
            out << "onLoadSuccess:function(node, param){${attrs.onLoadSuccess}(node, param);}"

        out << "});"
        out << "</script>"
    }

    def treeStructure(domainClass, relationProperty, titleProperty, selectedIds) {

        def openIds = []
        selectedIds.each { selectedId ->
            def id = selectedId
            def currentParentId = domainClass.clazz.createCriteria().list {
                eq("id", id)
            }.first()."${relationProperty.name}Id"
            while (currentParentId) {
                openIds << currentParentId
                id = currentParentId
                currentParentId = domainClass.clazz.createCriteria().list {
                    eq("id", id)
                }.first()."${relationProperty.name}Id"
            }
        }

        def structure
        if (params.id)
            structure = fillRecordChildren([id: params.id.toLong()], domainClass, relationProperty, titleProperty, selectedIds, openIds)
        else
            structure = fillRecordChildren(null, domainClass, relationProperty, titleProperty, selectedIds, openIds)

        return structure as grails.converters.JSON

    }

    def fillRecordChildren(root, domainClass, relationProperty, titleProperty, selectedIds, openIds) {
        def recordList
        if (root)
            recordList = domainClass.clazz.createCriteria().list {
                eq("${relationProperty.name}.id", root.id)
            }.collect { [id: it.id, text: it.properties[titleProperty.name], checked: selectedIds.contains(it.id), state: (openIds.contains(it.id) ? 'open' : 'closed'), children: []] }
        else
            recordList = domainClass.clazz.createCriteria().list {
                isNull(relationProperty.name)
            }.collect { [id: it.id, text: it.properties[titleProperty.name], checked: selectedIds.contains(it.id), state: (openIds.contains(it.id) ? 'open' : 'closed'), children: []] }


        recordList.each {
            it.children = fillRecordChildren(it, domainClass, relationProperty, titleProperty, selectedIds, openIds)
            if (it.children == [])
                it.state = 'open'
        }

        return recordList
    }
}
