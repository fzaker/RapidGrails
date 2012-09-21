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
        if (!attrs[attr])
            return false;
        return Boolean.parseBoolean(attrs[attr])
    }

    def static getDomainClassProperties(domainClass) {
        def excludedProperties = ["id", "version"]

        def props = []
        domainClass.persistentProperties.each { GrailsDomainClassProperty p ->
            if (!excludedProperties.contains(p.name)) {
                props << p
            }
        }
        props
    }
}
