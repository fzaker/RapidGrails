package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class FormTagLib {
    static namespace = "rg"

    def form = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.bean.class.name)
        def props = TaglibHelper.getDomainClassProperties(domainClass)
        def renderFormTag = TaglibHelper.getBooleanAttribute(attrs, "renderFormTag")

        def composites = attrs.bean.composites

        if (renderFormTag)
            out << "<form>"
        props.each { p ->
            def b = attrs.bean
            if (composites.contains(p.name))
                out << compositeForm(bean: attrs.bean, property: p.name)
            else
                out << f.field(bean: attrs.bean, property: p.name)
        }
        if (renderFormTag)
            out << "</form>"
    }

    def compositeForm = { attrs, body ->
        out << g.render(template: "/template/compositeManyField", model: [instance: attrs.bean, compositeProperty: attrs.property], plugin: "rapid-grails")
    }

    def compositeFormRow = { attrs, body->
        def parent = attrs.parent
        def index = (attrs.index == "_clone") ? null : Integer.valueOf(attrs.index)
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(parent.class.name)
        GrailsDomainClassProperty compositeProperty = domainClass.propertyMap[attrs.compositeProperty]
        def compositeDomainClass = compositeProperty.referencedDomainClass
        def props = TaglibHelper.getDomainClassProperties(compositeDomainClass)

        def compositeInstance
        if (index != null)
            compositeInstance = parent."${attrs.compositeProperty}"[index]
        else
            compositeInstance = compositeDomainClass.newInstance()

        out << g.hiddenField(name: 'id', value: compositeInstance.id).replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << g.hiddenField(name: 'deleted', value: compositeInstance.deleted).replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
        out << g.hiddenField(name: 'new', value: compositeInstance?.id == null? 'true': 'false').replace("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")

        def excludedProperties = ["deleted", "indx"]
        def excludedTypes = []
        if (compositeInstance.belongsTo instanceof Map)
            compositeInstance.belongsTo.each { excludedProperties << it.key}
        else if (compositeInstance.belongsTo instanceof List)
            excludedTypes = compositeInstance.belongsTo

        props.each { p ->
            if ((!excludedProperties.contains(p.name)) && (!excludedTypes.contains(p.type))) {
                def field = f.field(bean: compositeInstance, property: p.name)
                out << field.replaceAll("name=\"", "name=\"${attrs.compositeProperty}[${attrs.index}].")
            }
        }
    }
}
