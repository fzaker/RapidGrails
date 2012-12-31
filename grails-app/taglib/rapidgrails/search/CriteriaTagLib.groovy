package rapidgrails.search

import rapidgrails.TaglibHelper

class CriteriaTagLib {
    static namespace = "rg"

    def criteria = { attrs, body ->
        def gridParams = request.getAttribute("gridParams")
        if (gridParams == null) {
            out << "<div id='criteria_${attrs.id ?: ""}'>"
            out << "<fieldset class='form ${attrs.inline ? 'inline' : ''}'>"

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
            r = r.replace("\r", "").replace("\n","")
            gridParams.filter = r
        }
    }

    def eq = { attrs, body ->
        searchBox(attrs, "eq")
    }

    def inCrit = { attrs, body ->
        searchBox(attrs, "in")
    }
    def ne = { attrs, body ->
        searchBox(attrs, "ne")
    }
    def gt = { attrs, body ->
        searchBox(attrs, "gt")
    }
    def lt = { attrs, body ->
        searchBox(attrs, "lt")
    }

    def like = { attrs, body ->
        searchBox(attrs, "like")
    }
    def alias = {attrs, body ->
        attrs.hidden = "true"
        searchBox(attrs, "createAlias")
    }
    def nest = {attrs, body ->
        closureOperator(attrs, body, attrs.name)
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

    private void searchBox(attrs, operator) {
        def gridParams = request.getAttribute("gridParams")
        if (gridParams == null) {
            def group = request.getAttribute("group")
            def labelMsg = attrs.label ?: attrs.name
            def label = g.message(code: labelMsg, default: labelMsg)
            def name = attrs.name
            def hidden = TaglibHelper.getBooleanAttribute(attrs, "hidden")
            def value = attrs.value
            def from = attrs.from
            def optionKey = attrs.optionkey
            def noSelection = attrs.noSelection
            def datePicker = attrs.datePicker
            out << render(plugin: "rapid-grails", template: "/criteria/searchTextBox", model: [name: name, label: label, group: group, operator: operator, hidden: hidden, value: value, from: from, optionKey: optionKey, noSelection: noSelection, datePicker: datePicker])
        } else {
            if (attrs.value){
                if(attrs.value instanceof String)
                    out << "{op:'${operator}', field:'${attrs.name}', val:'${attrs.value}'},"
                else
                    out << "{op:'${operator}', field:'${attrs.name}', val:${attrs.value}},"
            }
            else
                out << ""
        }
    }
}
