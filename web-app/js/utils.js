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

function sendSaveRequest(formContainerId, gridItToReload, url, domainClass, params) {

    //load jquery.form.js from rapidgrails/web-app/js/

    var frm = jQuery("#" + formContainerId + ">form");
    frm.ajaxSubmit({
        url: url,
        type:(params && params.method)?params.method:"get",
        data: {domainClass:domainClass},
        success: function(response) {
            jQuery("#" + capitaliseFirstLetter(gridItToReload) + "Grid").trigger('reloadGrid');
            jQuery("#" + formContainerId).dialog("close")
            if(params && params.saveCallback){
                eval(params.saveCallback+"(response)");
            }
        }
    })
}
