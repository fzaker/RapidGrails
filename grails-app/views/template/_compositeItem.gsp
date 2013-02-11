<div id="${name}${i}" class="${name}-div compositeForm"
    <g:if test="${ngrepeat}">ng-repeat="item in ${className}Instance.variationValues"</g:if>
     style="margin-bottom:5px;margin-left: 5px;<g:if test="${hidden}">display:none;</g:if>">
    <rg:compositeRow parent="${instance}" compositeProperty="${name}" index="${i}"/>
    <span class="del-${name}" style="clear: both;">
        <img src="${fam.icon(name: 'cross')}"
             style="vertical-align:middle;"/>
    </span>
</div>