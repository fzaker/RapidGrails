<g:if test="${hidden}">
    <input type='hidden' id='${name}' name='${name}' op='${operator}' group='${group}' value='${value ?: ""}'/>
</g:if>
<g:else>
    <div class='fieldcontain'>
        <label for='${name}'>${label}</label>
        <g:if test="${from}">
            <g:select id='${name}' name='${name}' op='${operator}' from='${from}' noSelection="${noSelection}" optionKey="${optionKey?:"id"}" group='${group}' value='${value ?: ""}'/>
        </g:if>
        <g:elseif test="${datePicker}">
            <rg:datePicker name="${name}" precision="day" op='${operator}' group='${group}'  value="${value}"/>
        </g:elseif>
        <g:else>
            <input type='text' id='${name}' name='${name}' op='${operator}' group='${group}' value='${value ?: ""}'/>
        </g:else>
    </div>
</g:else>