/**
 * Author: Farshid Zaker
 * Shayen Information Techology
 * 10/12/11 - 12:10 PM
 */

import org.codehaus.groovy.grails.scaffolding.DefaultGrailsTemplateGenerator
import org.codehaus.groovy.grails.plugins.PluginManagerHolder

class RapidGrailsTemplateGenerator extends DefaultGrailsTemplateGenerator {
    static final RapidGrailsTemplateGenerator instance = new RapidGrailsTemplateGenerator()
    def pluginManager
    def rapidGrailsRenderEditorTemplate

    // uses the type to render the appropriate editor
    def rapidGrailsRenderEditor = { property ->
        def domainClass = property.domainClass
        def cp
        if (PluginManagerHolder.pluginManager.hasGrailsPlugin('hibernate')) {
            cp = domainClass.constrainedProperties[property.name]
        }

        if (!rapidGrailsRenderEditorTemplate) {
            // create template once for performance
            def templateText = getTemplateText("rapidGrailsRenderEditor.template")
            rapidGrailsRenderEditorTemplate = engine.createTemplate(templateText)
        }

        def binding = [property: property,
                domainClass: domainClass,
                cp: cp,
                domainInstance:getPropertyName(domainClass)]
        return rapidGrailsRenderEditorTemplate.make(binding).toString()
    }
}
