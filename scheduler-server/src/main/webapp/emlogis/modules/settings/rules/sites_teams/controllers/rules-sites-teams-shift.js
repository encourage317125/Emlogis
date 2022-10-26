(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesShiftsCtrl', ['$scope', '$filter', '$modal', 'applicationContext', 'rulesShiftLTService',
    function ($scope, $filter, $modal, applicationContext, rulesShiftLTService) {


      //--------------------------------------------------------------------
      // On page load
      //--------------------------------------------------------------------

      var shiftLengthsInit,
          shiftTypesInit,
          selectedTypeInit,
          shift = this;

      shift.site = {};

      // Shift Lengths
      shift.lengths = [];
      shift.fullHoursList = [];
      shift.isLengthsEditing = false;
      shift.selectedLength = null;

      // Shift Types
      shift.types = [];
      shift.wereTypesEdited = false;
      shift.customShiftType = null;

      // Selected Type to Edit
      shift.selectedType = null;
      shift.isTypeEdited = false;


      // watch for Site being changed
      $scope.$watch("selectedSite", function(newSite) {
        if (newSite) {
          shift.site = newSite;
          shift.getShiftLengths(newSite.id);
        }
      });



      //--------------------------------------------------------------------
      // Shift Lengths
      //--------------------------------------------------------------------

      shift.getShiftLengths = function(siteId){
        return rulesShiftLTService.getShiftLengths(siteId).then(function(lengths){
          updateShiftLengthsModel(lengths);
        });
      };



      //
      // Update Lengths model

      var updateShiftLengthsModel = function(lengths){
        shift.lengths = lengths;
        console.log('shift.lengths', shift.lengths);
        shiftLengthsInit = rulesShiftLTService.getShiftsLengthsInit();
        prepareFullHoursList(shift.lengths);
        shift.isLengthsEditing = false;
      };


      //
      // Prepare Full Hours list

      var prepareFullHoursList = function(allLengths){
        shift.fullHoursList = [];

        angular.forEach(allLengths, function(length){
          var i = _.indexOf(shift.fullHoursList, length.fullHours);
          if (i < 0) {
            shift.fullHoursList.push(length.fullHours);
          }
        });
      };


      //
      // Check if Shift Lengths were changed

      shift.updLengthsEditing = function(lengthChanged) {
        shift.isLengthsEditing = !angular.equals(shift.lengths, shiftLengthsInit);

        // If any Length is unchecked and any Length is selected
        if (shift.isLengthsEditing && shift.types.length > 0) {
          // If unchecked Length is selected, empty Shift Types
          if ( !lengthChanged.active && lengthChanged.id === shift.types[0].shiftLengthId ) {
            shift.types = [];
          }
        }

        // If Length was checked back
        if (lengthChanged.active) {
          shift.selectLength(lengthChanged);
        }
      };



      //
      // Save Shift Lengths changes

      shift.saveShiftLengths = function(){
        var changedLength = [];

        angular.forEach(shift.lengths, function(length){
          var initLength = _.find(shiftLengthsInit, {id: length.id});
          if ( !angular.equals(length, initLength) ) {
            changedLength.push(length);
          }
        });

        return rulesShiftLTService.updateShiftLengths(shift.site.id, changedLength).then(function(lengths){
          if (lengths) {
            updateShiftLengthsModel(lengths);

          // If error occurs on Save, and there is no lengths returned
          // display old Lengths to avoid breaking the page (UAT-125)

          } else {
            var oldLength = rulesShiftLTService.getShiftsLengthsInit();
            updateShiftLengthsModel(oldLength);
          }
        });
      };



      //
      // Add new Shift Lengths

      shift.addNewShiftLengths = function () {
        var existingLengths = shift.lengths;
        var siteId = shift.site.id;

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/rules/sites_teams/partials/include/rules_sites-teams_shift-lengths-modal.tmpl.html',
          controller: 'RulesNewShiftLengthsModalCtrl as nsl',
          size: 'sm',
          resolve: {
            lengths: function () {
              return existingLengths;
            },
            siteId: function () {
              return siteId;
            }
          }
        });

        modalInstance.result.then(function (lengths) {
          updateShiftLengthsModel(lengths);

        }, function () {
          //$log.info('Modal dismissed at: ' + new Date());
        });
      };


      shift.stopPropagation = function($event) {
        $event.stopPropagation();
      };


      //
      // Select this Length

      shift.selectLength = function(length) {
        shift.selectedLength = length.active ? length : null;
      };



      //--------------------------------------------------------------------
      // Shift Types
      //--------------------------------------------------------------------


      // Watch for selected Length to be changed

      $scope.$watch("shift.selectedLength", function(newLength) {
        if (newLength) {
          shift.getShiftTypesForLength(newLength.id);
          //shift.wereTypesEdited = false;
          shift.selectedType = null;
          shift.isTypeEdited = false;
        }
      });


      // GET Shift Types for a selected Length

      shift.getShiftTypesForLength = function(shiftLengthId) {
        rulesShiftLTService.getShiftTypesForLength(shift.site.id, shiftLengthId).then( function(types){
          updateTypesModel(types);
        });
      };

      var updateTypesModel = function(types) {
        _.forEach(types, function(type){
          type.paidTimeInHours = type.paidTimeInMin === 0 ? type.paidTimeInMin : type.paidTimeInMin / 60;
          type.isSubmitted = false;
        });
        //console.log('types', types);
        shiftTypesInit = types;
        shift.selectedType = null;
        shift.customShiftType = null;
        refreshTypesModel();
      };



      // Refresh Types modal
      function refreshTypesModel() {
        shift.types = angular.copy(shiftTypesInit);
        shift.wereTypesEdited = false;
        $scope.selectedTypeForm.$setUntouched();
      }



      // Check if Types were changed

      shift.updTypesEditing = function() {
        shift.wereTypesEdited = !angular.equals(shift.types, shiftTypesInit);
      };



      //
      // Select this Type

      shift.selectType = function(typeToEdit) {
        selectedTypeInit = angular.copy(typeToEdit);
        shift.selectedType = typeToEdit;
        shift.updSelectedTypeEditing();

        angular.forEach(shift.types, function(type){
          type.isEditing = type.id === typeToEdit.id;
        });

        // Remove newly created Type, since it wasn't saved
        shift.customShiftType = typeToEdit.id ? null : shift.customShiftType;

        // Refresh Types to initial state to remove unsaved data
        refreshTypesModel();
      };



      //
      // Update Editing state of Selected Type

      shift.updSelectedTypeEditing = function() {
        shift.isTypeEdited = !angular.equals(shift.selectedType, selectedTypeInit);
      };



      //
      // Update multi Types active states

      shift.updShiftTypesActivation = function(){

        var dto = {};
        angular.forEach(shift.types, function(type){
          var typeInit = _.find(shiftTypesInit, {id: type.id});
          if ( !angular.equals(type, typeInit) ) {
            var id = type.id;
            dto[id] = type.active;
          }
        });

        return rulesShiftLTService.updShiftTypesActivation(shift.site.id, dto).then( function(types){
          updateTypesModel(types);
        });
      };



      //
      // Add a custom Type

      shift.addCustomShiftTypeRow = function() {
        var defaultTime = moment().hour(0).minute(0).second(0).milliseconds(0);

        shift.customShiftType = {
          name:               '',
          description:        '',
          startTime:          defaultTime,
          active:             true,
          shiftLengthLength:  shift.selectedLength.lengthInMin,
          shiftLengthId:      shift.selectedLength.id,
          isEditing:          true,
          paidTimeInHours:    shift.selectedLength.lengthInMin / 60
        };

        shift.selectType(shift.customShiftType);
      };



      //
      // Update a Type

      shift.saveShiftType = function() {

        if ( shift.selectedType.id ) {  // if Type exists

          var updDto = {
            name: shift.selectedType.name,
            description: shift.selectedType.description,
            startTime: shift.selectedType.startTime,
            active: shift.selectedType.active,
            paidTimeInMin: $filter('hoursToMins')(shift.selectedType.paidTimeInHours)
          };
          console.log('updDto', updDto);

          return rulesShiftLTService.saveShiftType(shift.site.id, shift.selectedType, updDto).then( function(types){
            updateTypesModel(types);
          });

        } else {   // if Type is newly created

          var startTime = moment(shift.selectedType.startTime);
          var newDto = {
            shiftLengthId: shift.selectedType.shiftLengthId,
            updateDto: {
              name: shift.selectedType.name,
              description: shift.selectedType.description,
              startTime: startTime.diff(moment(startTime).startOf('day')),
              active: shift.selectedType.active,
              paidTimeInMin: shift.selectedType.paidTimeInHours * 60
            }
          };
          console.log('newDto', newDto);

          return rulesShiftLTService.addShiftType(shift.site.id, newDto).then( function(types){
            updateTypesModel(types);
          });
        }
      };



      //
      // Delete a Type

      shift.deleteShiftType = function(type, $event) {
        $event.stopPropagation($event);
        return rulesShiftLTService.deleteShiftType(shift.site.id, type).then( function(types){
          updateTypesModel(types);
        });
      };



      //
      // Bulk add pre-defined Types

      shift.addBulkNewShiftTypes = function () {
        var existingTypes = shift.types;
        var site = shift.site;
        var selectedLength = shift.selectedLength;

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/rules/sites_teams/partials/include/rules_sites-teams_shift-bulk-types-modal.tmpl.html',
          controller: 'RulesNewShiftBulkTypesModalCtrl as bulk',
          size: 'sm',
          resolve: {
            types: function () {
              return existingTypes;
            },
            selectedLength: function () {
              return selectedLength;
            },
            site: function () {
              return site;
            }
          }
        });

        modalInstance.result.then(function (types) {
          updateTypesModel(types);

        }, function () {
          //$log.info('Modal dismissed at: ' + new Date());
        });
      };


    }]);
})();