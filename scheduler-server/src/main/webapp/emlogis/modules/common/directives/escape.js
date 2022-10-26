angular.module('emlogis.commonDirectives').directive('ngEscape', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 27) {
                scope.$apply(function (){
                    scope.$eval(attrs.ngEscape);
                });
                event.preventDefault();
            }
        });
    };
});
