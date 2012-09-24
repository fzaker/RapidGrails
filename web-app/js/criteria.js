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
    grid.setGridParam({url:newUrl});
    grid.trigger("reloadGrid");
}

var loadGridWithCriteria = function (gridId, criteria) {
    var grid = $("#" + gridId);
    var url = grid.getGridParam('url');
    var newUrl = setUrlParam(url, "filter", criteria);
    grid.setGridParam({url:newUrl});
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
    if (op) {
        criteria = {};
        criteria.op = op;
        var v = $(eleman).val();
        if (op == 'like')
            v = '%25'+v+'%25';
        var f = $(eleman).attr("name");

        if (v && f) {
            criteria.val = v;
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
