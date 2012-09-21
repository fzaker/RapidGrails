package rapidgrails.search

import rapidgrails.TaglibHelper

class CriteriaTagLib {
    static namespace = "rg"

    def criteria = { attrs, body ->
        def gridParams = request.getAttribute("gridParams")
        if (gridParams == null) {
            out << "<div id='criteria_${attrs.id ?: ""}'>"
            out << "<fieldset class='form'>"

            request.setAttribute("group", "criteria_${attrs.id ?: ""}")
            out << body.call()
            request.removeAttribute("group")

            out << "</fieldset>"
            out << "</div>"
        } else {
            def r = body().trim()
            if (r && r.endsWith(","))
                r = r[0..-2]
            if (r)
                r = "[${r}]"
            r = r.replace("\r\n", "")
            gridParams.filter = r
        }
    }

    def eq = { attrs, body ->
        searchTextBox(attrs, "eq")
    }

    def like = { attrs, body ->
        searchTextBox(attrs, "like")
    }

    def and = { attrs, body ->
        closureOperator(attrs, body, "and")
    }

    def or = { attrs, body ->
        closureOperator(attrs, body, "or")
    }

    def filterGrid = {attrs, body ->
        def group = request.getAttribute("group")
        def labelMsg = attrs.label ?: "Search"
        def label = g.message(code: labelMsg, default: labelMsg)
        out << g.javascript(src: 'criteria.js', plugin: 'rapid-grails')
        out << "<input type='button' onclick='loadGrid(\"${group}\", \"${attrs.grid}\")' value='${label}'>"
        out << "</input>"
    }

    private void closureOperator(attrs, body, operator) {
        def gridParams = request.getAttribute("gridParams")
        if (gridParams == null) {
            out << "<span op='${operator}'>"
            out << body()
            out << "</span>"
        } else {
            String data = body().trim()
            if (data && data.endsWith(","))
                data = data[0..-2]
            if (!data)
                out << ""
            out << "{op:${operator},data:[${data}]},"
        }
    }

    private void searchTextBox(attrs, operator) {
        def gridParams = request.getAttribute("gridParams")
        if (gridParams == null) {
            def group = request.getAttribute("group")
            def labelMsg = attrs.label ?: attrs.name
            def label = g.message(code: labelMsg, default: labelMsg)

            def hidden = TaglibHelper.getBooleanAttribute(attrs, "hidden")
            if (!hidden) {
                out << "<div class='fieldcontain'>"
                out << "<label for='${attrs.name}'>${label}</label>"
            }
            out << "<input type='${hidden ? "hidden" : "text"}' id='${attrs.name}' name='${attrs.name}' op='${operator}' group='${group}' value='${attrs.value ?: ""}' />"
            if (!hidden) {
                out << "</div>"
            }
        } else {
            if (attrs.value)
                out << "{op:'${operator}', field:'${attrs.name}', val:'${attrs.value}'},"
            else
                out << ""
        }
    }
}
