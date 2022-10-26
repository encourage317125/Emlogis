(function() {
  "use strict";

  // Service
  // Create service function

  var rulesShiftLengthsTypesService = function($http, $q, $filter, applicationContext, dataService){

    var slt = this,
        sltShiftLengthsInit,
        sltShiftLengths,
        sltShiftTypesInit,
        sltShiftTypes,
        abcOrder = { orderby: 'name', orderdir: 'ASC' };


    //--------------------------------------------------------------------
    // Shift Lengths related methods
    //--------------------------------------------------------------------


    // GET all existing Shift Lengths for a Site

    slt.getShiftLengths = function(siteId){

      return dataService.getShiftLengths(siteId, abcOrder, 1, -1).then(function(res){
        console.log('getShiftLengths', res);
        sltShiftLengthsInit = res.data;

        angular.forEach(sltShiftLengthsInit, function(length){
          length.fullHours = Math.floor(length.lengthInMin / 60);
        });

        sltShiftLengths = angular.copy(sltShiftLengthsInit);
        return sltShiftLengths;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    //
    // provide an Initial SLT Lengths

    slt.getShiftsLengthsInit = function(){
      return sltShiftLengthsInit;
    };



    // UPDATE a Shift Length

    slt.updateShiftLengths = function(siteId, lengthsList){
      var deferred = $q.defer();

      angular.forEach(lengthsList, function(lengthToEdit, i){

        var dto = {
          name: lengthToEdit.name,
          description: lengthToEdit.description,
          lengthInMin: lengthToEdit.lengthInMin,
          active: lengthToEdit.active
        };

        return dataService.updateShiftLength(siteId, lengthToEdit.id, dto).then(function(res){
          applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);

          if ( i + 1 === lengthsList.length ) {
            deferred.resolve( slt.getShiftLengths(siteId) );
          }

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      });

      return deferred.promise;
    };



    // Multi add new Lengths

    slt.addNewShiftLengths = function(siteId, dto){
      return dataService.addNewShiftLengths(siteId, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);

        return slt.getShiftLengths(siteId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    //--------------------------------------------------------------------
    // Shift Types related methods
    //--------------------------------------------------------------------

    // GET Shift Types for a Shift Length

    slt.getShiftTypesForLength = function(siteId, shiftLengthId){

      return dataService.getShiftTypesForLength(siteId, shiftLengthId, { orderby: 'startTime', orderdir: 'ASC' }, 1, -1).then(function(res){
        console.log('getShiftTypesForLength', res);
        sltShiftTypesInit = res.data;

        angular.forEach(sltShiftTypesInit, function(type){
          type.isEditing = false;
          //type.isCreating = false;
        });

        sltShiftTypes = angular.copy(sltShiftTypesInit);
        return sltShiftTypes;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // Upd multi Types activation

    slt.updShiftTypesActivation = function(siteId, dto){
      return dataService.updShiftTypesActivation(siteId, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);

        return slt.getShiftTypesForLength(siteId, res.data[0].shiftLengthId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // save Shift Type

    slt.saveShiftType = function(siteId, type, dto) {
      return dataService.updateShiftType(siteId, type.id, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        return slt.getShiftTypesForLength(siteId, type.shiftLengthId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // add custom Type

    slt.addShiftType = function(siteId, dto) {
      return dataService.addShiftType(siteId, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        return slt.getShiftTypesForLength(siteId, dto.shiftLengthId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // delete Shift Type

    slt.deleteShiftType = function(siteId, type) {
      return dataService.deleteShiftType(siteId, type.id).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
        return slt.getShiftTypesForLength(siteId, type.shiftLengthId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // bulk add Shift Types

    slt.bulkAddNewShiftTypes = function(siteId, dto) {
      return dataService.bulkAddNewShiftTypes(siteId, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        return slt.getShiftTypesForLength(siteId, dto.shiftLengthId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

  };


  // Inject dependencies and
  // add service to the Rules module

  rulesShiftLengthsTypesService.$inject = ['$http', '$q', '$filter', 'applicationContext', 'dataService'];
  angular.module('emlogis.rules').service('rulesShiftLTService', rulesShiftLengthsTypesService);

})();