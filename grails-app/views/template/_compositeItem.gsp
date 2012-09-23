<div id="${name}${i}" class="${name}-div compositeForm"
     style="margin-bottom:5px;margin-left: 5px;<g:if test="${hidden}">display:none;</g:if>">

    <rg:compositeFormRow parent="${instance}" compositeProperty="${name}" index="${i}"/>
    <span class="del-${name}" style="clear: both;">
        <img src="${fam.icon(name: 'cross')}"
             style="vertical-align:middle;"/>
    </span>
</div>