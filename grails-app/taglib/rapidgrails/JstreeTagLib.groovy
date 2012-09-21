package rapidgrails

class JstreeTagLib {
    static namespace = "rg"

    def jstreeResources = {
        def jsURL = g.resource(plugin: 'rapid-grails', dir: 'jstree', file: 'jquery.jstree.js')
        def scriptTag = "<script type=\"text/javascript\" src=\"${jsURL}\"></script>"
        out << scriptTag

        jsURL = g.resource(plugin: 'rapid-grails', dir: 'jstree', file: 'jstree.radio.js')
        scriptTag = "<script type=\"text/javascript\" src=\"${jsURL}\"></script>"
        out << scriptTag

        cssLink(out, 'jstree/themes/default-rtl', 'style.css')
    }

    private def cssLink(out, dir, file) {
        def cssURL = resource(plugin: 'rapid-grails', dir: dir, file: file)
        def cssLinkTag = "<link rel=\"stylesheet\" href=\"${cssURL}\"/>"
        out << cssLinkTag
    }
}
