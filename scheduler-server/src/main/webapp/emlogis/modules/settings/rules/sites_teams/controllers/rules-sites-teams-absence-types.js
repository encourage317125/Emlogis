(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesAbsenceTypesCtrl', ['$scope', '$filter', 'applicationContext', 'rulesAbsTypesService',
    function ($scope, $filter, applicationContext, rulesAbsTypesService) {


      //--------------------------------------------------------------------
      // On page load
      //--------------------------------------------------------------------

      var abs = this;
      abs.typesInit = [];
      abs.types = [];

      abs.site = {};
      abs.totalTypes = 0;
      abs.numOfRows = 20;
      abs.paginationCurrentPage = 1;


      $scope.$watch("selectedSite", function(newSite) {
        if (newSite) {
          abs.site = newSite;
          abs.getAbsenceTypes(newSite.id);
        }
      });


      abs.getAbsenceTypes = function(siteId){
        return rulesAbsTypesService.getAbsenceTypes(siteId, abs.paginationCurrentPage, abs.numOfRows).then(function(res){
          updateTypesModel(res);
        });
      };


      var updateTypesModel = function(res){
        abs.types = res.data;
        _.forEach(abs.types, function(type){
          type.isSubmitted = false;
        });
        abs.totalTypes = res.total;
        abs.paginationCurrentPage = res.pageNum;
        console.log('updateTypesModel', abs.types);
      };


      //--------------------------------------------------------------------
      // CRUD
      //--------------------------------------------------------------------


      //
      // Switch to/from Type editing

      abs.editType = function(type) {
        if (!type.isEditing) type.isEditing = true;
      };



      //
      // Add New Type to a table

      abs.addNewType = function() {
        var newType = {
          siteId: abs.site.id,
          name: '',
          description: '',
          timeToDeductInMin: $filter('minsToHours')(480),
          active: true,
          isEditing: true,
          isCreating: true
        };
        abs.types.unshift(newType);
      };



      //
      // Check if New Type is being already created

      abs.newTypeIsCreating = function() {
        return _.find(abs.types, {isCreating: true});
      };



      //
      // Save clicked

      abs.save = function(type) {
        return type.isCreating ? abs.saveNewType(type) : abs.saveType(type);
      };



      //
      // Create a New Type

      abs.saveNewType = function(type) {
        var dto = {
          name: type.name,
          description: type.description,
          timeToDeductInMin: $filter('hoursToMins')(type.timeToDeductInMin),
          active: type.active
        };

        return rulesAbsTypesService.addAbsenceType(type.siteId, dto).then(function(res){
          updateTypesModel(res);
        });
      };



      //
      // Delete a Type TODO: if isDeletable prop added

      abs.deleteType = function(type){
        return rulesAbsTypesService.deleteAbsenceType(type.siteId, type.id).then(function(res){
          updateTypesModel(res);
        });
      };



      //
      // Update a Type

      abs.saveType = function(type){
        var dto = {
          name: type.name,
          description: type.description,
          timeToDeductInMin: $filter('hoursToMins')(type.timeToDeductInMin),
          active: type.active
        };

        return rulesAbsTypesService.updateAbsenceType(type.siteId, type.id, dto).then(function(res){
          updateTypesModel(res);
        });
      };


    }]);
})();