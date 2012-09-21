package rapidgrails

class RapidGrailsTagLib {
    static namespace = "rg"

    def grid = { attrs, body ->
        out << rg.jqgrid(attrs, body)
    }
}
