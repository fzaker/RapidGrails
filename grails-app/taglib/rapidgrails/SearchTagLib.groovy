package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

class SearchTagLib {
    static namespace = "rg"
    def messageSource

    def searchBox = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        def propertyNames = []
        domainClass.properties.each {
            propertyNames << it.name
        }

        def id = attrs.id ?: attrs.name
        def name = attrs.name ?: attrs.id
        def cls = attrs.class ? "class=\"${attrs.class}\"" : ""

        def tagBody = """
        <input id="${id}" name="${name}" ${cls}></div>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                jQuery("#${id}").searchBox();
            });
        </script>
        """

        out << tagBody
    }
}
