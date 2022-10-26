angular.module('emlogis.settings').controller('EntityListContainerCtrl', ['$scope', '$state', '$q', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $q, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {
    $scope.filter = {
      filterTxt: '',
      confirmedFilterTxt: ''
    };

    $scope.filterList = function () {
      $scope.filter.confirmedFilterTxt = $scope.filter.filterTxt;
    };

    $scope.getEntityList = function () {
      var deferred = $q.defer();
      var offset = 0;
      var limit = 0;
      SettingsAccountsService.getEntityList($scope.entityDetails.entityType, $scope.filter.filterTxt+ "&filter=primaryKey.id!='employeegroup'", offset, limit).then(function (response) {
        if (response.data) {
          deferred.resolve({data: response.data.result});
        } else {
          deferred.reject('Error Occurred while trying to get Entity List');
        }
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        deferred.reject('Error Occurred while trying to get Entity List');
      });
      return deferred.promise;
    };

    //list for pagnination
    $scope.getEntityListForPagination = function (entityListGridOptions, paginationOptions) {
      var deferred = $q.defer();
      var offset = (paginationOptions.pageNumber - 1) * paginationOptions.pageSize;
      var limit = paginationOptions.pageSize;
      var orderBy = paginationOptions.orderBy;
      var orderDir = paginationOptions.orderDir;
      SettingsAccountsService.getEntityList($scope.entityDetails.entityType, $scope.filter.filterTxt + "&filter=primaryKey.id!='employeegroup'", offset, limit, orderBy, orderDir).then(function (response) {
        if (response.data) {
          entityListGridOptions.totalItems = response.data.total;
          deferred.resolve({data: response.data.result});
        } else {
          deferred.reject('Error Occurred while trying to get Entity List');
        }
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        deferred.reject('Error Occurred while trying to get Entity List');
      });
      return deferred.promise;
    };

    //refresh the page.
    $scope.refresh = function() {
      $scope.getEntityListForPagination();
    };
  }
]);

