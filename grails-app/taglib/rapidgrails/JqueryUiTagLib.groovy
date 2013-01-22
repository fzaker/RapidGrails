package rapidgrails

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import fi.joensuu.joyds1.calendar.JalaliCalendar

class JqueryUiTagLib {
    static namespace = "rg"
    GroovyPagesTemplateEngine groovyPagesTemplateEngine

    def form1 = { attrs, body ->
        DefaultGrailsDomainClass domainClass = grailsApplication.getDomainClass(attrs.domainClass.name)
        def excludedProperties = ["id", "version"]

        def props = []
        domainClass.persistentProperties.each { GrailsDomainClassProperty p ->
            if (!excludedProperties.contains(p.name)) {
                props << p
            }
        }

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

    def escapeForJquery(selector) {
        def r = selector.replace(".", "\\\\.")
        r = r.replace("[", "\\\\[")
        r = r.replace("]", "\\\\]")
        r
    }

    def autocomplete = { attrs, body ->
        String selector = attrs.selector ?: attrs.id
        selector = escapeForJquery(selector);

        def cls = attrs.class ? "class=\"${attrs.class}\"" : ""

        out <<
        """<input id="${attrs.id ?: attrs.name}" ${attrs.placeholder? "placeholder=\"${attrs.placeholder}\"":""} value="${attrs.display?:""}"  ${attrs.style?"style=\"${attrs.style}\"":""} ${cls} />
        <input type="hidden" id="${attrs.id ?: attrs.name}.id" value="${attrs.value?:""}" name="${attrs.name ?: attrs.id}.id" />
        <script type="text/javascript">
            jQuery("#${selector}").autocomplete({
                source: '${g.createLink(controller:"rapidGrails", action:"search", params:[domainClass:"${attrs.domainClass}", like: "${Boolean.parseBoolean(attrs.like)?:false}"])}',
                minLength: 2,
                select: function(event, ui) {jQuery("#${selector}\\\\.id").val(ui.item.id);}
            }).change(function(){
                if(jQuery(this).val()=="")
                    jQuery("#${selector}\\\\.id\").val("");
            });
        </script>
        """
    }

    def datePicker = {attrs, body ->
        def dt = attrs.value
        def control_value = ""
        def y = ""
        def m = ""
        def d = ""
        def h = ""
        def min = ""

        if (dt) {
            def cal = Calendar.getInstance()
            cal.setTime(dt)

            def jc = new JalaliCalendar(cal)
            control_value = String.format("%04d/%02d/%02d", jc.getYear(), jc.getMonth(), jc.getDay())

            y = String.format("%04d", cal.get(Calendar.YEAR))
            m = String.format("%02d", cal.get(Calendar.MONTH) + 1)
            d = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
            h = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))
            min = String.format("%02d", cal.get(Calendar.MINUTE))
        }

        def already = Boolean.parseBoolean(attrs.already)

        def selector = attrs.id?:attrs.name;
        selector = escapeForJquery(selector);

        def cls = attrs.class ? "class=\"${attrs.class}\"" : ""
        def tt=g.javascript(null,"""${already ? "" : "jQuery(document).ready(function() {"}
                    jQuery("#${selector}_control").datepicker({
                        dateFormat: 'yy/mm/dd',
                        changeMonth: true,
                        changeYear: true,
                        ${attrs.yearRange?'yearRange:"'+attrs.yearRange+'",':''}
                        onSelect: function(dateText, inst) {
                            var s = jQuery("#${selector}_control").datepicker("getDate").getTime();
                            var dt = new Date(s);
                            jQuery("#${selector}_year").val(dt.getFullYear());
                            jQuery("#${selector}_month").val(dt.getMonth() + 1);
                            jQuery("#${selector}_day").val(dt.getDate());
                            jQuery('#${selector}_control').change();
                        }
                    });
                    ${attrs.required?"jQuery('#${selector}_control').change(function(){if(\$(this).val()){\$(this).removeClass('invalid')}else{\$(this).addClass('invalid')}});jQuery('#${selector}_control').change();":""}
                ${already ? "" : "});"}""")
        def tagBody = """${tt}

            <input type="hidden" value="date.struct" id="${attrs.id?:attrs.name}" name="${attrs.name}"/>
            <input type="hidden" id="${attrs.id?:attrs.name}_year" name="${attrs.name}_year" value="${y}" />
            <input type="hidden" id="${attrs.id?:attrs.name}_month" name="${attrs.name}_month" value="${m}" />
            <input type="hidden" id="${attrs.id?:attrs.name}_day" name="${attrs.name}_day" value="${d}" />
            <input type="text" readonly="true" id="${attrs.id?:attrs.name}_control" name="${attrs.name}_control" value="${control_value}" ${cls} ${attrs.placeholder? "placeholder=\"${attrs.placeholder}\"":""} ${attrs."input-ng-model"?"ng-model=\"${attrs."input-ng-model"}\"":""}/>
        """

        def hourMinPart = ""
        if (Boolean.parseBoolean(attrs.hm)) {
            hourMinPart = "<select id=\"${attrs.id?:attrs.name}_minute\" name=\"${attrs.name}_minute\" style=\"width: 45px;\">"
            (0..59).each {
                def v = String.format("%02d", it)
                def selected = (v == min)?"selected=\"selected\"":""
                hourMinPart += "<option ${selected} value=\"${v}\">${v}</option>"
            }
            hourMinPart += "</select>"

            hourMinPart += "<select id=\"${attrs.id?:attrs.name}_hour\" name=\"${attrs.name}_hour\" style=\"width:45px;\">"
            (0..23).each {
                def v = String.format("%02d", it)
                def selected = (v == h)?"selected=\"selected\"":""
                hourMinPart += "<option ${selected} value=\"${v}\">${v}</option>"
            }
            hourMinPart += "</select>"
        }

        tagBody = "<div style=\"white-space:nowrap;\">" + hourMinPart + tagBody + "</div>"

        out << tagBody
    }

    def formatJalaliDate = {attrs, body ->
        if (attrs.date) {
            def cal = Calendar.getInstance()
            cal.setTime(attrs.date)

            def jc = new JalaliCalendar(cal)
            if (Boolean.parseBoolean(attrs.hm))
                out << String.format("%02d:%02d ", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            out << String.format("%04d/%02d/%02d", jc.getYear(), jc.getMonth(), jc.getDay())
        }
    }
}
