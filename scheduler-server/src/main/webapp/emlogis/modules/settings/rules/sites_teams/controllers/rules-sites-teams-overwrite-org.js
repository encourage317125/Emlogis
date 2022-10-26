(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesOverwriteOrgCtrl', ['$scope', 'applicationContext', 'rulesAbsTypesService',
    function ($scope, applicationContext, rulesAbsTypesService) {


      //--------------------------------------------------------------------
      // On page load
      //--------------------------------------------------------------------

      var over = this;
      over.typesInit = [];
      over.types = [];
      over.site = {};


      $scope.$watch("selectedSite", function(newSite) {
        if (newSite) {
          over.site = newSite;
          //shift.getAbsenceTypes(newSite.id);
        }
      });


      //--------------------------------------------------------------------
      // CRUD
      //--------------------------------------------------------------------






      //--------------------------------------------------------------------
      // Other related methods
      //--------------------------------------------------------------------
      


    }]);
})();