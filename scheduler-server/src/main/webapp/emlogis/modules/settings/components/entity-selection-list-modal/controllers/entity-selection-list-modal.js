angular.module('emlogis.settings').controller('entitySelectionListModalCtrl', ['$scope', '$q', '$modalInstance', 'SettingsAccountsService', 'originalEntityDetails', 'entityDetails', 'convertEntityToGridRow',
  function ($scope, $q, $modalInstance, SettingsAccountsService, originalEntityDetails, entityDetails, convertEntityToGridRow) {

    $scope.data = {
      entityList: [],
      selectedEntityIds: [],
      originalEntityDetails: originalEntityDetails,
      entityDetails: entityDetails,
      convertEntityToGridRow: function (entity) {
        return convertEntityToGridRow({message: entity});
      }
    };

    $scope.processSelectedRow = function (row) {
      if (row.isSelected) {
        $scope.data.selectedEntityIds.push(row.entity.id);
      } else {
        var idIndex = $scope.data.selectedEntityIds.indexOf(row.entity.id);
        $scope.data.selectedEntityIds.splice(idIndex, 1);
      }
    };

    $scope.ok = function () {
      $modalInstance.close($scope.data.selectedEntityIds);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };

    $scope.getEntityList = function () {
      var deferred = $q.defer();
      var offset = 0;
      var limit = 0;
      SettingsAccountsService.getRelatedEntities($scope.data.originalEntityDetails.entityType, $scope.data.originalEntityDetails.id, $scope.data.entityDetails.entityType, false, null, offset, limit).then(function (response) {
        if (response.data) {
          deferred.resolve({data: response.data.result});
        } else {
          deferred.reject('Error Occurred while trying to get Entity List');
        }
      }, function (err) {
        console.log(err);
        deferred.reject('Error Occurred while trying to get Entity List');
      });
      return deferred.promise;
    };

    $scope.getEntityListForPagination = function (entityListGridOptions, paginationOptions) {
      var deferred = $q.defer();
      var offset = (paginationOptions.pageNumber - 1) * paginationOptions.pageSize;
      var limit = paginationOptions.pageSize;
      SettingsAccountsService.getRelatedEntities($scope.data.originalEntityDetails.entityType, $scope.data.originalEntityDetails.id, $scope.data.entityDetails.entityType, false, null, offset, limit).then(function (response) {
        if (response.data) {
          entityListGridOptions.totalItems = response.data.total;
          deferred.resolve({data: response.data.result});
        } else {
          deferred.reject('Error Occurred while trying to get Entity List');
        }
      }, function (err) {
        console.log(err);
        deferred.reject('Error Occurred while trying to get Entity List');
      });
      return deferred.promise;
    };
  }
]);