(function () {
  "use strict";

  //
  // This directive is build upon
  // Bootstrap component Well (.well)
  // and Bootstrap UI component Collapse (ui.bootstrap.collapse)
  
  var editWell = function () {

    return {
      restrict: 'EA',
      replace: true,
      scope: {
        isCollapsable: '=',
        isCollapsed: '=',
        name: '@'
      },
      transclude: true,
      templateUrl: 'modules/common/partials/edit-well.include.html',
      link: function(scope, element, attributes, controller, transclude){
        //console.log('+++ Inside Edit-Well directive...');

        //
        // Custom transclusion
        // for 2 transcluded  areas:
        // Well Body and addition to Well Header (optional)

        transclude(function(clone){
          angular.forEach(clone, function(cloneEl){
            if (cloneEl.nodeType === 1 && cloneEl.attributes['transclude-to']){

              var tId = cloneEl.attributes['transclude-to'].value;                // get desired target id
              var target = element.find('[transclude-id="' + tId + '"]');         // find target element with this id
              target.append(cloneEl);                                             // append element to target
            }
          });
        });
      }
    };
  };


  editWell.$inject = [];
  angular.module('emlogis.commonDirectives').directive('editWell', editWell);

}());
