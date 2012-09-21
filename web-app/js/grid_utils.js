$.fn.serializeObject = function () {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function () {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};
String.prototype.hashCode = function(){
    var hash = 0, i, char;
    if (this.length == 0) return hash;
    for (i = 0; i < this.length; i++) {
        char = this.charCodeAt(i);
        hash = ((hash<<5)-hash)+char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
};

var loadOverlay = function (remoteAddress, saveAddress, saveCallback,loadCallback) {
    $.ajaxSettings.traditional=true;
    $.ajax({
        type:"GET",
        url:remoteAddress
    }).done(function (response) {
            var r = $("#ajax-form"+remoteAddress.hashCode());
            if (!r.length)
                r = $("<form id='ajax-form"+remoteAddress.hashCode()+"' enctype='multipart/form-data'></form>")
            r.html("")

            r.dialog({
                modal:true,
                resizable:false,
                buttons:{
                    'ذخیره':function () {
                        r.ajaxSubmit({
                            url:saveAddress,
                            type:"post",
                            success:function(resp){
                                if(resp==0 || typeof resp == 'object'){
                                    if (saveCallback)
                                        saveCallback(resp)
                                }else{
                                    var r= $("#ajax-form"+remoteAddress.hashCode());
                                    r.html(resp);
                                    r.dialog("open");
                                }
                            }
                        })
                        $(this).dialog("close");
                    },
                    "انصراف":function () {
                        $(this).dialog("close");
                    }
                },
                close:function () {
                    r.html("")
                }
            })
            r.append(response);
            if(loadCallback)
                loadCallback(response);
        });
}