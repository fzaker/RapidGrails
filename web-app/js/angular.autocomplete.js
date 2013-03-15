//var directives = angular.module('ng');
//
//directives.directive('autocomplete', ['$http', function($http) {
//    return function (scope, element, attrs) {
//        element.autocomplete({
//            minLength:2,
//            source:"/EShop/rapidGrails/search?domainClass="+element.attr('domainclass')+"&like=true",
//            focus:function (event, ui) {
//                element.val(ui.item.label);
//                return true;
//            },
//            select:function (event, ui) {
//                scope.$eval(element.attr('property')+".id="+ ui.item.id);
//                scope.$apply;
//                return true;
//            },
//            change:function (event, ui) {
//                if (ui.item === null) {
//                    scope.$eval(element.attr('property')+'=null') ;
//                }
//            }
//        }).data("autocomplete")._renderItem = function (ul, item) {
//            return $("<li></li>")
//                .data("item.autocomplete", item)
//                .append("<a>" + item.label + "</a>")
//                .appendTo(ul);
//        };
//    }
//}]);
