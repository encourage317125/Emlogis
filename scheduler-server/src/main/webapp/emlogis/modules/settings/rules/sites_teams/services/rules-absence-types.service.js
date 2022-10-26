(function() {
  "use strict";

  // Service
  // Create service function

  var rulesAbsenceTypesService = function($http, $q, $filter, applicationContext, crudDataService, dataService, dialogs){

    var ats = this,
        absTypesInit,
        totalTypes,
        absTypes,
        atsNumOfRows,
        atsPageNum,
        abcOrder = { orderby: 'name', orderdir: 'ASC' };


    //--------------------------------------------------------------------
    // Teams related methods
    //--------------------------------------------------------------------


    // GET all existing Absence Types

    ats.getAbsenceTypes = function(siteId, pageNum, numOfRows){
      atsNumOfRows = numOfRows;
      atsPageNum = pageNum;

      return dataService.getAbsenceTypes(siteId, abcOrder, pageNum, numOfRows).then(function(res){
        //console.log('getAbsenceTypes', res);

        absTypesInit = res.data;
        totalTypes = res.total;

        angular.forEach(absTypesInit, function(type){
          type.isEditing = false;
          type.isCreating = false;
          type.timeToDeductInMin = $filter('minsToHours')(type.timeToDeductInMin);
        });

        absTypes = angular.copy(absTypesInit);
        var returnDto = {
          data: absTypes,
          total: totalTypes,
          pageNum: atsPageNum
        };
        return returnDto;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    //
    // provide an Initial Abs Types list

    ats.getAbsTypesInit = function(){
      return absTypesInit;
    };


    //
    // provide current total number of Abs Types

    ats.getAbsTypesTotal = function(){
      return totalTypes;
    };


    // DELETE a Type

    ats.deleteAbsenceType = function(siteId, typeId){

      var deferred = $q.defer();
      var dlg = dialogs.confirm('app.PLEASE_CONFIRM', 'rules.DELETE');                      // Show modal window
      dlg.result
        .then(function (btn) {                                                              // If user confirms, proceed

          return dataService.deleteAbsenceType(siteId, typeId).then(function(res){
            applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
            deferred.resolve(ats.getAbsenceTypes(siteId, atsPageNum, atsNumOfRows));

          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });

        }, function (btn) {                                                                 // If user doesn't confirm
          // rollback
        }
      );
      return deferred.promise;
    };


    // UPDATE a Type

    ats.updateAbsenceType = function(siteId, typeId, dto){
      return dataService.updateAbsenceType(siteId, typeId, dto).then(function(res){

        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        return ats.getAbsenceTypes(siteId, atsPageNum, atsNumOfRows);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    // CREATE a Type

    ats.addAbsenceType = function(siteId, dto){
      return dataService.addAbsenceType(siteId, dto).then(function(res){

        applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
        var pageNum = findPageNumForNewTypeName(dto);
        return ats.getAbsenceTypes(siteId, pageNum, atsNumOfRows);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    var findPageNumForNewTypeName = function(newType){
      var allTypes = angular.copy(absTypesInit);
      allTypes.push(newType);
      allTypes = $filter('orderBy')(allTypes, 'name');

      var newTypeIndex = _.findIndex(allTypes, { name: newType.name });
      var pageNum = Math.floor(newTypeIndex / atsNumOfRows) + 1;
      return pageNum;
    };

  };


  // Inject dependencies and
  // add service to the Rules module

  rulesAbsenceTypesService.$inject = ['$http', '$q', '$filter', 'applicationContext', 'crudDataService', 'dataService', 'dialogs'];
  angular.module('emlogis.rules').service('rulesAbsTypesService', rulesAbsenceTypesService);

})();