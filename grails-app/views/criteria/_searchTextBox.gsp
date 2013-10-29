<g:if test="${hidden}">
    <input type='hidden' id='${name}' unary='${unary}' name='${name}' thirdParam='${thirdParam}' op='${operator}' group='${group}' value='${value ?: ""}'/>
</g:if>
<g:else>
    <div class='fieldcontain'>
        <label for='${name}'>${label}</label>
        <g:if test="${from}">
            <g:set var="okey" value="${optionKey?:"id"}"/>
            <g:if test="${okey=="nill"}">
                <g:set var="okey" value=""/>
            </g:if>
            <g:select id='${name}' name='${name}' op='${operator}' from='${from}' thirdParam='${thirdParam}' valueMessagePrefix="${valueMessagePrefix}" noSelection="${noSelection}" optionKey="${okey}" group='${group}' value='${value ?: ""}'/>
        </g:if>
        <g:elseif test="${datePicker}">
            <rg:datePicker id="${idPrefix?:""}${name}" name="${name}" precision="day" thirdParam='${thirdParam}' op='${operator}' group='${group}'  value="${value}"/>
        </g:elseif>
        <g:else>
            <input type='text' id='${name}' name='${name}' op='${operator}'  thirdParam='${thirdParam}' group='${group}' value='${value ?: ""}'/>
        </g:else>
    </div>
</g:else>