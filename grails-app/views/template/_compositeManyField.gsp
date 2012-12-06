<div class="fieldcontain">
    <label for="${compositeProperty}">
        <g:message code="${className}.${compositeProperty}.label" default="${g.message(code: "${compositeProperty}.label", default: compositeProperty.capitalize())}"/>
    </label>

    <g:render template="/template/compositeList" model="${[instance:instance, compositeProperty: compositeProperty]}" plugin="rapid-grails" />
</div>
