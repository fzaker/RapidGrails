<g:if test="${autocomplete}">
    <input class="compositionField" placeholder="${placeholder}"  ng-model="item.${property}.label" autocomplete domainclass="${type.name}" property="item.${property}"/>
    <span style="display: none;">
        <input name="${compositeProperty}[${propertyIndex}].${property}.id" ng-model="item.${property}.id"/>
    </span>
</g:if>
<g:else>
    <g:select name="${compositeProperty}[${propertyIndex}].${property}.id" class="compositionField" optionKey="id" ng-model="item.${property}.id" from="${type.list()}" noSelection="['':'']" placeholder="${placeholder}"/>
</g:else>
