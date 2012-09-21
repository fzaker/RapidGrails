function capitaliseFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

function setUrlParam(url, param_name, param_value) {
    var newAdditionalURL = "";
    var tempArray = url.split("?");
    var baseURL = tempArray[0];
    var aditionalURL = tempArray[1];
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
