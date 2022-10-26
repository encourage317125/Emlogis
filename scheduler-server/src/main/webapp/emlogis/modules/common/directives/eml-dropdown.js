(function () {
  "use strict";

  var emlDropdown = function ($timeout) {

    return {
      restrict: 'E',
      replace: true,
      scope: {
        model: '=',
        options: '=',
        property: '@',
        leftText: '=',
        topText: '=',
        leftTextWidth: '@',
        disabled: '=',
        keepTopTextHeight: '@',
        onSelect: "&"
      },
      templateUrl: 'modules/common/partials/eml-dropdown.html',
      link: function(scope) {

        var DEFAULT_LEFT_TEXT_WIDTH = 50,
          DEFAULT_TOP_TEXT_MARGIN_LEFT = 9,
          DEFAULT_TOP_TEXT_HEIGHT = 18,

          leftTextWidth     = scope.leftTextWidth || (scope.leftText ? DEFAULT_LEFT_TEXT_WIDTH : 0),
          topTextMarginLeft = leftTextWidth || DEFAULT_TOP_TEXT_MARGIN_LEFT,
          topTextHeight     = (scope.keepTopTextHeight || scope.topText) ? DEFAULT_TOP_TEXT_HEIGHT : 0;

        scope.leftTextBlockWidth = {width: leftTextWidth + 'px'};
        scope.topTextMargin = {marginLeft: topTextMarginLeft + 'px', height: topTextHeight};

        scope.selectOption = function(opt) {
          scope.model = opt;
          if (_.isFunction(scope.onSelect)) {
            $timeout(function() {
              scope.onSelect();
            });
          }
        };

        scope.getDisplayValue = function (model) {
          if (!model) return '...';
          return (scope.property && model[scope.property]) ? model[scope.property] : model;
        };
      }
    };
  };

  emlDropdown.$inject = ['$timeout'];
  angular.module('emlogis.commonDirectives').directive('emlDropdown', emlDropdown);

}());
