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
String.prototype.hashCode = function () {
    var hash = 0, i, char;
    if (this.length == 0) return hash;
    for (i = 0; i < this.length; i++) {
        char = this.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
};

var loadOverlay = function (remoteAddress, saveAddress, saveCallback, loadCallback, params) {
    $.ajaxSettings.traditional = true;
    $.ajax({
        type:"GET",
        url:remoteAddress
    }).done(function (response) {
            var r = $("#ajax-form" + remoteAddress.hashCode());
            if (!r.length)
                r = $("<form id='ajax-form" + remoteAddress.hashCode() + "' enctype='multipart/form-data' action='" + saveAddress + "'></form>")
            r.html("")

            r.dialog({
                modal:true,
                resizable:false,
                buttons:{
                    'ذخیره':function () {
                        if(params && params.beforeSubmit)
                            params.beforeSubmit();
                        r.ajaxSubmit({
                            url:saveAddress,
                            type:"post",
                            success:function (resp) {
                                if(params && params.afterSave)
                                    params.afterSave(resp)
                                if (resp == 0 || typeof resp == 'object') {
                                    if (saveCallback)
                                        saveCallback(resp)
                                    var r = $("#ajax-form" + remoteAddress.hashCode());
                                    r.dialog("destroy");
                                    r.remove()
                                } else {
                                    var r = $("#ajax-form" + remoteAddress.hashCode());
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
            if (params && params.width) {
                r.dialog("option", "width", params.width)
                r.dialog("option", "position", "top")
            }

            r.append(response);
            if (loadCallback)
                loadCallback(response);
        });
}