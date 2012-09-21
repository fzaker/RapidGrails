package rapidgrails.reporting

import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Created by IntelliJ IDEA.
 * User: Zaker
 * Date: 2/5/12
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */

class ReportDataReader {
    def static fromService(service, method, params) {
        def ctx = ApplicationHolder.getApplication().getMainContext()
        def svc = ctx.getBean(service + "Service")
        def serviceResponse = svc."${method ?: "report"}"(params ?: [:])
        serviceResponse
    }
}
