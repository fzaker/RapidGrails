package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import grails.converters.JSON

class PopupTagLib {
    static namespace = "rg"

    def formDialog = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        def includeSubClasses = TaglibHelper.getBooleanAttribute(attrs, "includeSubClasses", false)

        if (!includeSubClasses) {
            def id = attrs.id ?: attrs.name
            def title = attrs.title ?: ""
            out << "<div id='${id}' title='${title}'>"

            request.setAttribute("dialogId", id)

            //out << body()
            out << rg.fields(bean: domainClass.newInstance())
            out << rg.saveButton(domainClass: domainClass)
            out << rg.cancelButton()

            request.removeAttribute("dialogId")

            out << "</div>"
            out << """
                <script type='text/javascript'>
                jQuery(function() {
                    jQuery("#${id}").dialog({autoOpen: false});
                });
                </script>
            """
        }
    }

    def dialog = { attrs, body ->
        def id = attrs.id ?: attrs.name
        def title = attrs.title ?: ""
        out << "<div id='${id}' title='${title}'>"

        request.setAttribute("dialogId", id)
        out << body()
        request.removeAttribute("dialogId")

        out << "</div>"
        out << """
            <script type='text/javascript'>
            jQuery(function() {
                setTimeout(function(){
                    jQuery("#${id}").dialog({autoOpen: false, dialogClass: "form-dialog"});
                },50);
            });
            </script>
        """
    }

    def cancelButton = { attrs, body ->
        def cancelLabel = message(code: "cancel.label", default: "Cancel")
        def dialogId = request.getAttribute("dialogId")
        out << "<input class='btn' type='button' value='${cancelLabel}' onclick='jQuery(\"#${dialogId}\").dialog(\"close\")'/>"
    }

    def saveButton = { attrs, body ->
        def saveLabel = message(code: "save.label", default: "Save")
        def dialogId = request.getAttribute("dialogId")
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        def gridId = attrs.gridId ?: domainClass.propertyName
        def url = g.createLink(controller: attrs.conroller?:"rapidGrails", action: attrs.action?:"save")
        def params=attrs.params?:[:]
        out << "<input class='btn btn-success' type='button' value='${saveLabel}' ng-click='save${domainClass.propertyName.capitalize()}(\"${dialogId}\", \"${gridId}\", \"${url}\", \"${domainClass.fullName}\",${params as JSON})'/>"
    }
}
