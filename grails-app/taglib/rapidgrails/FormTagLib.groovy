package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
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
        body()
        def modify = request.getAttribute("modify")

        out << "<form>"
        out << "<div class=\"form-fields\"><div class=\"form-fields-part\">"

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
                    if (modify.readonlyFields?.contains(p.name)) {
                        out << f.field(bean: attrs.bean, property: p.name, "input-ng-model": "${domainClass.propertyName}Instance.${p.name}", "input-readonly": "true")
                    }
                    else
                        out << f.field(bean: attrs.bean, property: p.name, "input-ng-model": "${domainClass.propertyName}Instance.${p.name}")
                    count++
                }
                if (count >= 10) {
                    count = 0
                    newColumn = true
                }
            }
        }
        out << "</div></div>"
        out << "</form>"
        def angular = TaglibHelper.getBooleanAttribute(attrs, "angular", true)
        if (angular)
            out << """
                <script type="text/javascript">
                    function getFresh${domainClass.propertyName}Instance() {
                        return ${domainClass.newInstance() as JSON};
                    }
                    function ${domainClass.propertyName}Controller(\$scope, \$http) {
                        //\$scope.${domainClass.propertyName}Instance = getFresh${domainClass.propertyName}Instance();

                        \$scope.open${domainClass.propertyName.capitalize()}CreateDialog = function() {
                            \$scope.${domainClass.propertyName}Instance = getFresh${domainClass.propertyName}Instance();

                            ${request.getAttribute("interceptCreateDialog")}
                            if (!\$scope.\$\$phase)
                                \$scope.\$apply();

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
                    }
                </script>
            """

        request.removeAttribute("interceptCreateDialog")
        request.removeAttribute("modify")
    }

    def interceptCreateDialog = { attrs, body ->
        request.setAttribute("interceptCreateDialog", body())
    }

    def modify = { attrs, body ->
        def modify = request.getAttribute("modify")
        modify.hiddenReferences = []
        modify.readonlyFields = []
        modify.ignoredFields = []
        body()
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
        def index = (attrs.index == "_clone") ? null : Integer.valueOf(attrs.index)
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(parent.class.name)
        GrailsDomainClassProperty compositeProperty = domainClass.propertyMap[attrs.compositeProperty]
        def compositeDomainClass = compositeProperty.referencedDomainClass
        def props = TaglibHelper.getDomainClassProperties(compositeDomainClass)

        def compositeInstance
        if (index != null)
            compositeInstance = parent."${attrs.compositeProperty}"[index]
        else
            compositeInstance = compositeDomainClass.newInstance()

        out << g.hiddenField(name: 'id', value: compositeInstance.id).replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << g.hiddenField(name: 'deleted', value: compositeInstance.deleted).replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << g.hiddenField(name: 'new', value: compositeInstance?.id == null ? 'true' : 'false').replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")

        def excludedProperties = ["deleted", "indx"]
        def excludedTypes = []
        if (compositeInstance.belongsTo instanceof Map)
            compositeInstance.belongsTo.each { excludedProperties << it.key}
        else if (compositeInstance.belongsTo instanceof List)
            excludedTypes = compositeInstance.belongsTo

        props.each { p ->
            if ((!excludedProperties.contains(p.name)) && (!excludedTypes.contains(p.type))) {
                def label = message(code: "${p.domainClass.propertyName}.${p.name}.label", default: message(code: "${p.name}.label", default: p.naturalName))
                def field = f.input(bean: compositeInstance, property: p.name, class: "compositionField", placeholder: label)
                out << field.replaceAll("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
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
}
