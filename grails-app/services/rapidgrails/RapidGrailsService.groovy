/**
 * Author: Farshid Zaker
 * Shayen Information Techology
 * 10/12/11 - 12:10 PM
 */

package rapidgrails

import org.codehaus.groovy.grails.commons.GrailsClass
import grails.converters.JSON

class RapidGrailsService {
    def grailsApplication

    static transactional = true

    def findDomainClass(domainClassName) {
        def dc = grailsApplication.getDomainClass(domainClassName)
        def foundDomainClasses = []
        grailsApplication.domainClasses.each { GrailsClass domainClass ->
            if ((domainClassName == domainClass.name) || (domainClassName == domainClass.shortName))
                foundDomainClasses << domainClass
        }
        foundDomainClasses
    }
}
