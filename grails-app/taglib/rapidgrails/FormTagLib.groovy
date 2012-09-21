package rapidgrails

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class FormTagLib {
    static namespace = "rg"

    def form = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        def props = TaglibHelper.getDomainClassProperties(domainClass)

        out << """<script type="text/javascript">
        \$(function() {
        \$( "#dialog-form" ).dialog({
			autoOpen: false,
			width: 350,
			modal: true,
			resizable: false,
			buttons: {
				"Create an account": function() {
					allFields.removeClass( "ui-state-error" );
                    \$( this ).dialog( "close" );
				},
				Cancel: function() {
					\$( this ).dialog( "close" );
				}
			},
			close: function() {
				allFields.val( "" ).removeClass( "ui-state-error" );
			}
		});
		});
		\$("#createButton").button().click(function() {
				\$("#dialog-form").dialog("open");
	    });
		</script>"""

        out << """<div id="dialog-form" title="Create new user">
	    <p class="validateTips">All form fields are required.</p>
        <form>
        <fieldset>
        """

        props.each { p ->
            out << """<label for="${p.name}">${g.message(code:domainClass.propertyName + "." + p.name + ".label", default:p.naturalName)}</label>"""
            def editor = RapidGrailsTemplateGenerator.instance.rapidGrailsRenderEditor(p)
            groovyPagesTemplateEngine.createTemplate(editor, "abcq.gsp").make().writeTo(out)
        }

        out << """</fieldset>
        </form>
        </div>"""
    }

}
