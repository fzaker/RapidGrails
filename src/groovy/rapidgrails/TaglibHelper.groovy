package rapidgrails

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

/**
 * Created with IntelliJ IDEA.
 * User: Zaker
 * Date: 8/6/12
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
class TaglibHelper {
    def static getBooleanAttribute(attrs, attr) {
        return getBooleanAttribute(attrs, attr, false)
    }

    def static getBooleanAttribute(attrs, attr, defaultValue) {
        if (!attrs[attr])
            return defaultValue;
        return Boolean.parseBoolean(attrs[attr])
    }

    def static getDomainClassProperties(domainClass) {
        def excludedProperties = ["id", "version"]

        def props = []
        domainClass.constraints.keySet().each {
            if (!excludedProperties.contains(it)) {
                props << domainClass.propertyMap[it]
            }
        }
        props
    }
}
