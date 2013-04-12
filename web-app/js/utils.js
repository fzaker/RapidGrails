function capitaliseFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function setUrlParam(url, param_name, param_value) {
    var newAdditionalURL = "";
    var baseURL = url.substring(0,url.indexOf("?"));
    var aditionalURL = url.substring(url.indexOf("?")+1);
    var temp = "";
    if (aditionalURL) {
        var tempArray = aditionalURL.split("&");
        for (var i in tempArray) {
            if (tempArray[i].indexOf(param_name) == -1) {
                newAdditionalURL += temp + tempArray[i];
                temp = "&";
            }
        }
    }
    var add_param = temp + param_name + "=" + param_value;
    return baseURL + "?" + newAdditionalURL + add_param;
}
function removeNulls(obj){
    if(typeof(obj)=='object'){
        var res={};
        for(var key in obj){
            if(obj[key])
                res[key]=removeNulls(obj[key])
        }
        return res
    }
    return obj
}
function sendSaveRequest(formContainerId, gridItToReload, url, domainClass, params) {

    //load jquery.form.js from rapidgrails/web-app/js/

    var frm = jQuery("#" + formContainerId + ">form");
    if(frm.find('.ng-invalid').length>0){
        frm.find('.form-validation').show()
    }else{
        frm.ajaxSubmit({
            url: url,
            type:(params && params.method)?params.method:"get",
            data: {domainClass:domainClass},
            success: function(response) {
                if(typeof(response)=='object' && response.length>0){
                    var validation = frm.find('.form-validation')
                    validation.html('')
                    $(response).each(function(){
                        validation.append('<div>'+this+'</div>')
                    })
                    validation.show()
                }else{
                    jQuery("#" + capitaliseFirstLetter(gridItToReload) + "Grid").trigger('reloadGrid');
                    jQuery("#" + formContainerId).dialog("close")
                    if(params && params.saveCallback){
                        eval(params.saveCallback+"(response)");
                    }
                }
            }
        })
    }
}
