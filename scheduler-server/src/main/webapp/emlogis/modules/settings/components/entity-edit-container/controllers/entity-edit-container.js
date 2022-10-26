angular.module('emlogis.settings').controller('EntityEditContainerCtrl', ['$scope', '$q', '$state', '$modal', '$timeout', 'SettingsAccountsService', 'applicationContext',
  function ($scope, $q, $state, $modal, $timeout, SettingsAccountsService, applicationContext) {

    $scope.entity = null;
    $scope.entityDetails = {};
    $scope.submitClicked = false;

    $scope.fillEntityDetails = function () {
      $scope.entityDetails = $scope.populateEntityDetails($scope.entity);
      $scope.originalEntityDetails = {
        id: $scope.entityDetails.id,
        entityType: $scope.entityType
      };
    };

    $scope.selectSubTabed = function (relatedEntity) {
      angular.forEach($scope.entityDetails.relatedEntities, function (item) {
        item.selected = false;
      });

      relatedEntity.selected = true;
    };

    $scope.addRelatedEntities = function (relatedEntityDetails, selectedEntityIds) {
      var deferred = $q.defer();
      SettingsAccountsService.operateOnRelatedEntity('add', $scope.entityType, $scope.entityId, relatedEntityDetails.entityType, selectedEntityIds).then(function (response) {
        deferred.resolve('Added Successfully!!!');
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        deferred.reject('Error occurred while trying to add related entities');
      });
      return deferred.promise;
    };

    $scope.removeRelatedEntities = function (relatedEntityDetails, selectedEntities) {
      var deferred = $q.defer();
      var selectedEntityIds = [];

      angular.forEach(selectedEntities, function(entity) {
        if (relatedEntityDetails.entityType === $scope.consts.entityTypes.role &&
          entity.id === 'adminrole' && $scope.entityType === $scope.consts.entityTypes.user &&
          $scope.entityDetails.id === 'admin') {
          applicationContext.setNotificationMsgWithValues('settings.accounts.ASSOCIATED_ADMIN_ROLE_CANT_BE_REMOVED', 'success', true);
        } else {
          selectedEntityIds.push(entity.id);
        }
      });

      SettingsAccountsService.operateOnRelatedEntity('remove', $scope.entityType, $scope.entityId, relatedEntityDetails.entityType, selectedEntityIds).then(function (response) {
        deferred.resolve('Removed Successfully!!!');
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        deferred.reject('Error occurred while trying to remove related entities');
      });
      return deferred.promise;
    };

    $scope.applyEdit = function (isValid) {

      $scope.submitClicked = true;
      if (!isValid) {
        applicationContext.setNotificationMsgWithValues('app.PLEASE_FILL_CORRECT_DATA_IN_FIELDS', 'danger', true);
        return;
      }

      var editPromise = null;
      $scope.entityEditDetails = {};
      angular.forEach($scope.entityDetails.info, function (rowData) {
        angular.forEach(rowData, function (infoField) {
          $scope.entityEditDetails[infoField.key] = infoField.value;
        });
      });

      if ($scope.editMode === 'New') {
        editPromise = SettingsAccountsService.createEntity($scope.entityType, $scope.entityEditDetails);
      } else {
        editPromise = SettingsAccountsService.updateEntity($scope.entityType, $scope.entityDetails.id, $scope.entityEditDetails);
      }
      editPromise.then(function (response) {
        if ($scope.editMode === 'New') {
          applicationContext.setNotificationMsgWithValues('app.CREATED_SUCCESSFULLY', 'success', true);
        } else {
          applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
        }
        $state.go($scope.entityEditStateName, {entityId: response.data.id});
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        $state.go($scope.entityListStateName);
      });
    };

    $scope.closeEdit = function () {
      if ($scope.editMode === 'New') {
        $state.go($scope.entityListStateName);
      } else {
        $state.go($scope.entityDetailsStateName, {entityId: $scope.entityDetails.id});
      }
    };

    $scope.initEditDetails = function () {
      if ($scope.editMode === 'New') {
        $scope.entity = {};
        $scope.fillEntityDetails();
      } else {
        SettingsAccountsService.getEntityDetails($scope.entityType, $scope.entityId).then(function (response) {
          $scope.entity = response.data;
          $scope.fillEntityDetails();
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        });
      }
    };

    $scope.initEditDetails();
  }
]);
