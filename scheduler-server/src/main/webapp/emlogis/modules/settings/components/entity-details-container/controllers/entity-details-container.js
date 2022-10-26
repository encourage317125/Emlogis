angular.module('emlogis.settings').controller('EntityDetailsContainerCtrl', ['$scope', '$state', '$modal', '$timeout', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $modal, $timeout, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {

    $scope.entity = null;
    $scope.entityDetails = {};

    $scope.fillEntityDetails = function () {
      $scope.entityDetails = $scope.populateEntityDetails($scope.entity);
    };

    $scope.selectSubTab = function (relatedEntity) {
      angular.forEach($scope.entityDetails.relatedEntities, function (item) {
        item.selected = false;
      });

      relatedEntity.selected = true;
    };

    $scope.openDuplicateEntityModal = function () {
      var modalInstance = $modal.open({
        templateUrl: 'duplicateEntity.html',
        controller: 'DuplicateEntityModalInstanceCtrl',
        backdrop: false,
        windowClass: 'duplicate-entity-modal',
        resolve: {
          entityName: function () {
            return $scope.entityName;
          },
          fieldsToRedefineInDuplication: function () {
            return $scope.entityDetails.fieldsToRedefineInDuplication;
          }
        }
      });

      /*$timeout(function() {
       var duplicateEntityButtonPosition = $('.btn-duplicate-entity').offset();
       $('.duplicate-entity-modal').css('position', 'absolute');
       $('.duplicate-entity-modal').css('top', duplicateEntityButtonPosition.top + 30);
       $('.duplicate-entity-modal').css('left', duplicateEntityButtonPosition.left - 400);
       }, 0);*/

      modalInstance.result.then(function (fieldsToRedefineInDuplication) {
        var inputJson = {};
        angular.forEach(fieldsToRedefineInDuplication, function (field) {
          inputJson[field.key] = field.value;
        });

        SettingsAccountsService.duplicateEntity($scope.entityType, $scope.entityId, inputJson).then(function (response) {
          applicationContext.setNotificationMsgWithValues('app.DUPLICATED_SUCCESSFULLY', 'success', true);
          var newEntityId = response.data.id;
          $state.go($scope.entityDetailsStateName, {entityId: newEntityId});
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        });
      });
    };

    $scope.goToEntityEditState = function () {
      if ($scope.entityType === $scope.consts.entityTypes.role && $scope.entityDetails.id === 'adminrole') {
        applicationContext.setNotificationMsgWithValues('settings.accounts.DEFAULT_ADMIN_ROLE_CANT_BE_EDITED', 'success', true);
        return;
      }
      $state.go($scope.entityEditStateName, {entityId: $scope.entityId});
    };

    $scope.confirmDeleteEntity = function () {
      if ($scope.entityType === $scope.consts.entityTypes.user && $scope.entityDetails.id === 'admin') {
        applicationContext.setNotificationMsgWithValues('settings.accounts.DEFAULT_ADMIN_USER_ACCOUNT_CANT_BE_DELETED', 'success', true);
        return;
      }

      if ($scope.entityType === $scope.consts.entityTypes.role && $scope.entityDetails.id === 'adminrole') {
        applicationContext.setNotificationMsgWithValues('settings.accounts.DEFAULT_ADMIN_ROLE_CANT_BE_DELETED', 'success', true);
        return;
      }

      SettingsAccountsService.deleteEntity($scope.entityType, $scope.entityId).then(function (response) {
        applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
        $state.go($scope.entityListStateName);
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    $scope.getEntityDetails = function () {
      SettingsAccountsService.getEntityDetails($scope.entityType, $scope.entityId).then(function (response) {
        $scope.entity = response.data;
        $scope.fillEntityDetails();
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    $scope.getEntityDetails();
  }
]);

angular.module('emlogis.settings').controller('DuplicateEntityModalInstanceCtrl', ['$scope', '$modalInstance', 'entityName', 'fieldsToRedefineInDuplication',
  function ($scope, $modalInstance, entityName, fieldsToRedefineInDuplication) {
    $scope.entityDetails = {
      entityName: entityName
    };
    $scope.fieldsToRedefineInDuplication = fieldsToRedefineInDuplication;

    $scope.confirmDuplicateEntity = function () {
      $modalInstance.close($scope.fieldsToRedefineInDuplication);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }
]);