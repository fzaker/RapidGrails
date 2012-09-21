(function($){
    $.fn.searchBox = function() {
        this.each(function() {
            $(this).val("salam");
            var searchResultDivId = "searchResult";
            var searchResult = "<div id=\"" + searchResultDivId + "\" style=\"width: 240px; display: none;\">"
            searchResult += "<table id=\"" + searchResultDivId + "_list\"></table><div id=\"" + searchResultDivId + "_pager\"></div>";
            searchResult += "</div>";
            $('body').append($(searchResult));

            $("#" + searchResultDivId + "_list").jqGrid({
                url:'server.php?q=2',
                datatype: "json",
                direction: "rtl",
                colNames:['Inv No','Date', 'Client', 'Amount','Tax','Total','Notes'],
                colModel:[
                        {name:'id',index:'id', width:55},
                        {name:'invdate',index:'invdate', width:90},
                        {name:'name',index:'name asc, invdate', width:100},
                        {name:'amount',index:'amount', width:80, align:"right"},
                        {name:'tax',index:'tax', width:80, align:"right"},
                        {name:'total',index:'total', width:80,align:"right"},
                        {name:'note',index:'note', width:150, sortable:false}
                ],
                rowNum:10,
                rowList:[10,20,30],
                pager: '#' + searchResultDivId + '_pager',
                sortname: 'id',
                viewrecords: true,
                sortorder: "desc"
            });
            jQuery("#" + searchResultDivId + "_list").jqGrid('navGrid','#' + searchResultDivId + '_pager', {edit:false,add:false,del:false,search:false});

            $(this).keypress(function(event){
                if ((event.keyCode == 13) || (event.keyCode == 40)) { // 13 = enter, 40 = cursor down
                    var pos = $(this).offset();
                    var height = $(this).outerHeight();
                    var disposition = $("#" + searchResultDivId).outerWidth() - $(this).outerWidth();
                    $("#" + searchResultDivId).css({
                        position: "absolute",
                        top: pos.top + height + 3 + "px",
                        left: pos.left - disposition + "px"
                    }).show();
                }
            });

            $(this).focusout(function(event, data){
//                var g = $("#" + searchResultDivId + ":focus");
                var g = $(":focus");
                if (g.length == 0)
                    $("#" + searchResultDivId).hide();
            });
        });
    };
})(jQuery);