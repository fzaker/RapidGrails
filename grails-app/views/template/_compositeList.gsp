
<script type="text/javascript">

    $('.del-${compositeProperty}').live('click', function () {
        var prnt = $(this).parents(".${compositeProperty}-div");
        var delInput = prnt.find("input[id$=deleted]");
        var newValue = prnt.find("input[id$=new]").attr('value');
        if (newValue == 'true') {
            prnt.remove();
        } else {
            delInput.attr('value', 'true');
            prnt.hide();
        }
    });

</script>

<div class="initialList">
    <div id="${compositeProperty}_childList" style="float:none;">

        <g:render template="/template/compositeItem"
                  model="${[instance: instance, i: '{{$index}}',className:className, name: compositeProperty,ngrepeat:true]}"/>
    </div>

    <div class="compositeForm">
        <img src="${fam.icon(name: 'add')}" alt="" ng-click="addComposite${compositeProperty}()" style="cursor: pointer;"/>
    </div>
</div>
