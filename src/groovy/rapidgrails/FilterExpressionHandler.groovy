package rapidgrails

import grails.converters.JSON

class FilterExpressionHandler {
    def static equalExpression(name, value) {
        "[{p:'${name}',v:'${value}',o:'eq'}]"
    }
}
