angular.module('emlogis.commonDirectives').directive('focusMe', ['$timeout', '$parse',
  function($timeout, $parse) {
    return {
      link: function(scope, element, attrs) {
        var model = $parse(attrs.focusMe),
            resetOnBlur = attrs.resetOnBlur;
        scope.$watch(model, function(value) {
          if(value === true) {
            $timeout(function() {
              element[0].focus();
            });
          }
        });

        if (resetOnBlur) {
          element.bind('blur', function() {
            console.log("resetOnBlur catched");
            scope.$apply(model.assign(scope, false));
          });
        }
      }
    };
}]);