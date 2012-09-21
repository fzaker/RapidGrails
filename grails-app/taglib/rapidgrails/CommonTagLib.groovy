package rapidgrails

class CommonTagLib {
    static namespace = "rg"

    def colorPicker = {attrs, body ->
        def tagBody = """
            <p>
                <div id="colorSelector"><div style="background-color: ${com.shayen.afc.util.ColorUtil.getHexString(attrs.value)}"></div></div>
            </p>
            <script type="text/javascript">
            jQuery(document).ready(function() {
                jQuery('#colorSelector').ColorPicker({
                    color: '${com.shayen.afc.util.ColorUtil.getHexString(attrs.value)}',
                    onShow: function (colpkr) {
                        jQuery(colpkr).fadeIn(500);
                        return false;
                    },
                    onHide: function (colpkr) {
                        jQuery(colpkr).fadeOut(500);
                        return false;
                    },
                    onChange: function (hsb, hex, rgb) {
                        jQuery('#colorSelector div').css('backgroundColor', '#' + hex);
                        jQuery('#color').val(rgb.r * 65536 + rgb.g * 256 + rgb.b);
                    }
                });
            });
            </script>
        """
        tagBody += hiddenField(name:attrs.name, value:attrs.value)

        out << tagBody
    }

    def colorViewer = {attrs, body ->
        def tagBody = """
            <p>
                <div id="colorSelector"><div style="background-color: ${com.shayen.afc.util.ColorUtil.getHexString(attrs.value)}"></div></div>
            </p>
        """
        tagBody += hiddenField(name:attrs.name, value:attrs.value)

        out << tagBody
    }

    def checkBoxList = {attrs, body ->
        def from = attrs.from
        def value = attrs.value
        def cname = attrs.name
        def translate = attrs.translate?.toBoolean()
        def isChecked, ht, wd, style, html

        //  sets the style to override height and/or width if either of them
        //  is specified, else the default from the CSS is taken
        style = "style='"
        if(attrs.height)
            style += "height:${attrs.height};"
        if(attrs.width)
            style += "width:${attrs.width};"

        if (style.length() == "style='".length())
            style = ""
        else
            style += "'" // closing single quote

        html = "<ul class='CheckBoxList' " + style + ">"

        out << html

        from.each { obj ->
            // if we wanted to select the checkbox using a click anywhere on the label (also hover effect)
            // but grails does not recognize index suffix in the name as an array:
            //      cname = "${attrs.name}[${idx++}]"
            //      and put this inside the li: <label for='$cname'>...</label>

            isChecked = (value?.contains(obj."${attrs.optionKey}"))? true: false

            out << "<li>" <<
                    checkBox(name:cname, value:obj."${attrs.optionKey}", checked: isChecked) <<
                    "${translate?message(code: obj.toString()):obj}" << "</li>"
        }
        out << "</ul>"
    }
}
