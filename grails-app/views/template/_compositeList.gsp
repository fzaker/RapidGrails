%{--<g:render template="/template/compositeItem" model="${[instance: instance, i:0, name: compositeProperty, hidden: false]}" />--}%

<script type="text/javascript">
    var ${compositeProperty}ChildCount = ${instance?."${compositeProperty}"?.size()} + 0;

    function addTo${compositeProperty}(){
        var clone = $("#${compositeProperty}_clone").clone();
        var scriptsClone = $("#${compositeProperty}_clone").find('script').clone();
        clone.find('script').remove();
        clone.css('display', 'block');
        var h = clone.html();
        h = h.replace(/_clone/g, ${compositeProperty}ChildCount);
        clone.html(h);
        clone.attr('id', clone.attr('id').replace(/_clone/g, ${compositeProperty}ChildCount));
        $("#${compositeProperty}_childList").append($(clone));

        for (var i = 0; i < scriptsClone.length; i++) {
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.innerHTML = $(scriptsClone[i]).html().replace(/_clone/g, ${compositeProperty}ChildCount);
            $("#${compositeProperty}_childList").append(script);
        }

        ${compositeProperty}ChildCount++;
    }

    $('.del-${compositeProperty}').live('click', function() {
        var prnt = $(this).parents(".${compositeProperty}-div");
        var delInput = prnt.find("input[id$=deleted]");
        var newValue = prnt.find("input[id$=new]").attr('value');
        if(newValue == 'true'){
            prnt.remove();
        }else{
            delInput.attr('value','true');
            prnt.hide();
        }
    });

</script>
<div class="initialList">
    <div id="${compositeProperty}_childList" style="float:none;">
        <g:each var="child" in="${instance?."${compositeProperty}"}" status="i">
            <g:render template="/template/compositeItem" model="${[instance: instance, i:i, name: compositeProperty, hidden: false]}" />
        </g:each>
    </div>
    <div class="compositeForm">
        <img src="<g:resource dir="images" file="portfolio_add.png"/>" alt="" onclick="addTo${compositeProperty}();" style="cursor: pointer;"/>
    </div>
</div>

<g:render template="/template/compositeItem" model="${[instance: instance, i:'_clone', name: compositeProperty, hidden: true]}" />
<script type="text/javascript">
    $(document).ready(function() {
        var clone = $("#${compositeProperty}_clone");
        clone.appendTo($("form").has(clone).parent());
    })
</script>