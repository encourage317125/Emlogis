angular.module('emlogis.settings').controller('RolesPermissionsCtrl', ['$scope', '$state', '$stateParams', '$q', '$http', '$filter', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService', 'applicationContext',
  function ($scope, $state, $stateParams, $q, $http, $filter, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService, applicationContext) {

    $scope.isInEdit = ($scope.editMode === 'Edit');
    $scope.permissions = [];
    $scope.originalAssociatedPermissions = [];

    $scope.updatePermissions = function () {
      var addedIds = [], removedIds = [];
      var promiseItem = null;
      var promises = [];
      angular.forEach($scope.permissions, function (permission) {
        var found = _.find($scope.originalAssociatedPermissions, function (originalPermission) {
          return originalPermission.id === permission.id;
        });
        if (typeof found !== 'undefined') {
          if (!permission.associated) {
            removedIds.push(permission.id);
          }
        } else {
          if (permission.associated) {
            addedIds.push(permission.id);
          }
        }
      });
      if (addedIds.length > 0) {
        promiseItem = SettingsAccountsService.operateOnRelatedEntity('add', $scope.entityType, $scope.entityId, $scope.consts.entityTypes.permission, addedIds);
        promises.push(promiseItem);
      }
      if (removedIds.length > 0) {
        promiseItem = SettingsAccountsService.operateOnRelatedEntity('remove', $scope.entityType, $scope.entityId, $scope.consts.entityTypes.permission, removedIds);
        promises.push(promiseItem);
      }

      $q.all(promises).then(function (responses) {
        applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
        $scope.populateAllPermissions();
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        $scope.populateAllPermissions();
      });
    };

    function comparator(firstPermission, secondPermission) {
      if (firstPermission.name.toLowerCase() < secondPermission.name.toLowerCase())
        return -1;
      if (firstPermission.name.toLowerCase() > secondPermission.name.toLowerCase())
        return 1;
      return 0;
    }

    $scope.populateAllPermissions = function () {
      var promises = [];

      promises.push($scope.relatedEntity.getEntityList());
      promises.push($scope.relatedEntity.getUnassociatedEntityList());
      $q.all(promises).then(function (responses) {
        angular.forEach(responses, function (response, responseIndex) {
          angular.forEach(response.data, function (permission) {
            if (responseIndex === 0) {
              permission.associated = true;
              $scope.originalAssociatedPermissions.push(permission);
            } else {
              permission.associated = false;
            }
            $scope.permissions.push(permission);
          });
        });
        $scope.permissions.sort(comparator);
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    $scope.populateAllPermissions();
  }
]);
