(function () {
  "use strict";

  //
  // This directive is build upon
  // Bootstrap component Panel (.panel)
  // and Bootstrap UI component Collapse (ui.bootstrap.collapse)

  var collapsablePanel = function () {

    return {
      restrict: 'EA',
      replace: true,
      scope: {
        isCollapsable: '=',
        isCollapsed: '=',
        name: '='
      },
      transclude: true,
      templateUrl: 'modules/common/partials/collapsable-panel.tmpl.html',
      link: function(scope, element, attrs, controller, transclude){
        //console.log('+++ Inside Collapsable Panel directive...');

        //
        // Watch for
        // Panel changes its collapsing state
        // to change the collapsing icon

        scope.$watch('isCollapsed', function(newValue) {
          if ( newValue ) {                                                       // If Panel becomes collapsed
            element.find('.collapse-icon-minus').css('opacity', '0')              // hide "minus" icon
                   .parent()
                   .find('.collapse-icon-plus').css('opacity','1');               // and display "plus" icon
            element.find('.panel-heading').css('background-color', '#fff');       // make panel heading white
          } else {                                                                // If Panel becomes open
            element.find('.collapse-icon-plus').css('opacity', '0')               // hide "plus" icon
                   .parent()
                   .find('.collapse-icon-minus').css('opacity','1');              // and display "minus" icon
            element.find('.panel-heading').css('background-color', '#f5f5f5');    // make panel heading grey
          }
        }, true);


        //
        // Custom transclusion
        // for 2 transcluded  areas:
        // Panel Body and addition to Panel Header (optional)

        transclude(function(clone){
          angular.forEach(clone, function(cloneEl){
            if (cloneEl.nodeType === 1 && cloneEl.attributes['transclude-to']){

              var tId = cloneEl.attributes['transclude-to'].value;                // get desired target id
              var target = element.find('[transclude-id="' + tId + '"]');         // find target element with this id
              target.append(cloneEl);                                             // append element to target
            }
          });
        });


        //
        // Avoid collapsing a Panel
        // on a .checkbox click in its header

        $('.eml-panel-header-right').find('.fa, .checkbox, .checkbox-inline').on('click', function(e){
          e.stopPropagation();
        });
      }
    };
  };


  collapsablePanel.$inject = [];
  angular.module('emlogis.commonDirectives').directive('collapsablePanel', collapsablePanel);

}());