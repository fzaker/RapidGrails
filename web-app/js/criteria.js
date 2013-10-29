//(function($){
//    $.fn.criteria = function() {
//        this.each(function() {
//
//        });
//    };
//})(jQuery);

var loadGrid = function (group, gridId) {
    var eleman = $("#" + group);
    var criteria = getCriteria(eleman);
    var grid = $("#" + gridId);
    var url = grid.getGridParam('url');
    var newUrl = setUrlParam(url, "filter", $.toJSON(criteria));
    grid.setGridParam({url: newUrl, page: 1});
    grid.trigger("reloadGrid");
}
var exportGrid = function (group, gridId) {
    var eleman = $("#" + group);
    var criteria = getCriteria(eleman);
    var grid = $("#" + gridId);
    var url = grid.getGridParam('url');
    var newUrl = setUrlParam(url, "filter", $.toJSON(criteria));
    var newUrl = setUrlParam(url, "export", 'true');
    window.open(newUrl)
}

var loadGridWithCriteria = function (gridId, criteria) {
    var grid = $("#" + gridId);
    var url = grid.getGridParam('url');
    var newUrl = setUrlParam(url, "filter", criteria);
    grid.setGridParam({url: newUrl, page: 1});
    grid.trigger("reloadGrid");
}

var getCriteria = function (eleman) {
    var result = getCriteriaRecursive(eleman);
    if (!result)
        result = [];
    else if (!result.length) {
        var t = [];
        t.push(result);
        result = t;
    }
    return result;
}

var getCriteriaRecursive = function (eleman) {
    var criteria;

    var childCriteriaList = [];
    eleman.children().each(function () {
        var childCriteria = getCriteriaRecursive($(this));
        if (childCriteria)
            childCriteriaList.push(childCriteria);
    });

    var op = $(eleman).attr("op");
    var thirdparam = $(eleman).attr("thirdparam");
    var unary = eval($(eleman).attr("unary"));
    if (op) {
        criteria = {};
        criteria.op = op;
        if (thirdparam)
            criteria.thirdParam = parseInt(thirdparam)
        var v = $(eleman).val();

        if (v == 'date.struct') {
            var name = $(eleman).attr("id")
            var year = $("#" + name + "_year").val()
            var month = $("#" + name + "_month").val()
            var day = $("#" + name + "_day").val()
            if (year && month && day)
                v = year + "/" + month + "/" + day
            else
                v = ''
        }
        if ((op == 'like' || op == 'ilike') && v)
            v = '%25' + v + '%25';
        if (op == 'in')
            v = eval(v)
        var f = $(eleman).attr("name");

        if (v && f) {
            criteria.val = v;
            criteria.field = f;
        }
        else if(f && unary) {
            criteria.field = f;
        }
        else if (!f && (childCriteriaList.length > 0))
            criteria.data = childCriteriaList;
        else if (!f && (childCriteriaList.length == 0))
            criteria = null;
        else if (f && (!v))
            criteria = null;
    } else if (childCriteriaList.length > 0) {
        criteria = childCriteriaList;
        if (criteria.length == 1)
            criteria = criteria[0];
    }
    return criteria;
}
