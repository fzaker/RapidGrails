<div class="fieldcontain">
    <label for="${compositeProperty}">
        <g:message code="${instance.class.name}.${compositeProperty}.label"/>
    </label>

    <g:render template="/template/compositeList" model="${[instance:instance, compositeProperty: compositeProperty]}" plugin="rapid-grails" />
</div>
