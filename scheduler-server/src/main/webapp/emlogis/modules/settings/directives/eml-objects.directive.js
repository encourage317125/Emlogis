(function () {
  "use strict";

  //
  // This directive is build upon
  // Bootstrap component Label (.label)
  //
  // all Tags must have "id" and "name" properties
  // for the current implementation of this directive to work

  var emlObjects = function ($filter) {

    return {
      restrict: 'EA',
      replace: true,
      scope: {
        readonly: '=',
        list: '=list',              // array of entities to display
        view: '&',              // method to show detail of entity from displayed list
        delete: '&',                     // method to remove entity from displayed list
        show: '&'     // method to trigger when Entity is being added to displayed list
      },
      templateUrl: 'modules/settings/partials/include/eml-objects.include.html',
      link: function(scope, element, attrs){
        // console.log('+++ Inside eml-objects directive...');                    // DEV mode

      }
    };
  };
  //
  //
  //tags.$inject = ['$filter'];
  angular.module('emlogis.settings').directive('emlObjects', emlObjects);

}());