angular.module('emlogis.settings')
  .controller('SettingsAccountsGroupsGroupDetailsCtrl',
  [
    '$scope',
    '$state',
    '$stateParams',
    '$q',
    '$http',
    '$filter',
    '$modal',
    '$log',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'SettingsAccountsService',
    'UtilsService',
    'dataService',
    function ($scope, $state, $stateParams, $q, $http, $filter, $modal, $log, applicationContext, crudDataService,
              uiGridConstants, SettingsAccountsService, UtilsService, dataService) {


      // init function: settings/accounts/group-detail
      $scope.init = function() {
        $scope.entityId = $stateParams.entityId;
        $scope.getEntityDetails();
      };


      $scope.getEntityDetails = function () {
        SettingsAccountsService.getEntityDetails($scope.entityType, $scope.entityId).then(function (response) {
          $scope.entity = response.data;
          $scope.fillEntityDetails();
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        });
      };

      $scope.fillEntityDetails = function () {
        $scope.entityDetails = $scope.populateEntityDetails($scope.entity);
      };

      $scope.populateEntityDetails = function (entity) {
        var entityDetails = {};

        entityDetails.id = entity.id;
        entityDetails.name = entity.name;
        entityDetails.info = [
          [
            {
              label: 'app.NAME',
              key: 'name',
              placeHolder: 'app.NAME',
              value: entity.name,
              class: 'col-sm-4',
              type: 'text',
              validation: 'required'
            }
          ],
          [
            {
              label: 'settings.accounts.DESCRIPTION',
              key: 'description',
              placeHolder: 'settings.accounts.DESCRIPTION',
              value: entity.description,
              class: 'col-sm-8',
              type: 'text',
              validation: 'none'
            }

          ]
        ];

        entityDetails.users = {};
        entityDetails.roles = {};
        entityDetails.acl = {};

        // Get list of users & roles & acls
        $scope.loadRelatedEntities($scope.consts.entityTypes.user, entityDetails.users);
//        $scope.loadRelatedEntitiesWithFilter($scope.consts.entityTypes.role, entityDetails.roles, "primaryKey.id NOT Like 'acl-%'");
        $scope.loadRelatedEntitiesWithFilter($scope.consts.entityTypes.role, entityDetails.roles, "primaryKey.id+NOT+Like+%27acl-%25%27");
        $scope.loadRelatedEntities($scope.consts.entityTypes.accessControl, entityDetails.acl);

        entityDetails.canDuplicate = false;

        return entityDetails;
      };

      /**
       * Eml Object related functions
       */
      $scope.openRolesModal = function (group) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-groups-roles-modal.tmpl.html',
          controller: 'SettingsAccountsGroupsRolesModalCtrl as te',
          size: 'lg',
          resolve: {
            group: function () {
              return group;
            }
          }
        });

        modalInstance.result.then(function () {
//          $scope.loadRelatedEntities($scope.consts.entityTypes.role, $scope.entityDetails.roles);
          $scope.loadRelatedEntitiesWithFilter($scope.consts.entityTypes.role, $scope.entityDetails.roles, "primaryKey.id+NOT+Like+%27acl-%25%27");
        }, function () {
          $log.info('Modal dismissed at: ' + new Date());
        });
      };

      $scope.openUsersModal = function (group) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-groups-users-modal.tmpl.html',
          controller: 'SettingsAccountsGroupsUsersModalCtrl as te',
          size: 'lg',
          resolve: {
            group: function () {
              return group;
            }
          }
        });

        modalInstance.result.then(function () {
          $scope.loadRelatedEntities($scope.consts.entityTypes.user, $scope.entityDetails.users);

        }, function () {
          $log.info('Modal dismissed at: ' + new Date());
        });
      };

      $scope.openAclModal = function (group) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-groups-acl-modal.tmpl.html',
          controller: 'SettingsAccountsGroupsAclModalCtrl as te',
          size: 'lg',
          resolve: {
            group: function () {
              return group;
            },
            acl: function () {
              return angular.copy($scope.entityDetails.acl);
            }
          }
        });

        modalInstance.result.then(function (response) {
          $scope.loadRelatedEntities($scope.consts.entityTypes.accessControl, $scope.entityDetails.acl);
        }, function () {
          $log.info('Modal dismissed at: ' + new Date());
        });
      };

      /**
       * Tag Directive Related Func
       */

      $scope.removeMemberFromGroup = function(member) {

        return dataService.removeMemberFromGroup($scope.entity.id, member.id)
          .then(function(res){
            $scope.loadRelatedEntities($scope.consts.entityTypes.user, $scope.entityDetails.users);
          }, function(error) {
            if (error.message) {
              applicationContext.setNotificationMsgWithValues(error.message, '', true, '');
            }
            else {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            }
          });

      };

      $scope.removeRoleFromGroup = function(role) {

        return dataService.removeRoleFromGroup($scope.entity.id, role.id)
          .then(function(res){
            //$scope.loadRelatedEntities($scope.consts.entityTypes.role, $scope.entityDetails.roles);
            $scope.loadRelatedEntitiesWithFilter($scope.consts.entityTypes.role, $scope.entityDetails.roles, "primaryKey.id+NOT+Like+%27acl-%25%27");
          }, function(error) {
            if (error.message) {
              applicationContext.setNotificationMsgWithValues(error.message, '', true, '');
            }
            else {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            }
          });
      };


      /**
       * Load related entities
       */
      $scope.loadRelatedEntities = function(relatedEntityType, relatedEntities) {

        return $scope.loadRelatedEntitiesWithFilter(relatedEntityType, relatedEntities);
      };


      /**
       * Load related entities, passing a filter via query param
       */
      $scope.loadRelatedEntitiesWithFilter = function(relatedEntityType, relatedEntities, filter) {

        var deferred = $q.defer();

        SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, relatedEntityType, true, filter).then(function (response) {
          if (response.data) {
            angular.copy(response.data, relatedEntities);
            deferred.resolve(relatedEntities);
          } else {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
            deferred.reject('Error Occurred while trying to get ' + relatedEntityType + ' List');
          }
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          deferred.reject('Error Occurred while trying to get ' + relatedEntityType + ' List');
        });

        return deferred.promise;
      };


      /**
       * Update Entity: Group Details
       */
      $scope.updateEntityDetails = function () {

        $scope.submitClicked = true;

        var editPromise = null;
        $scope.entityEditDetails = {};


        if (!$scope.entityDetails.info[0][0].value) { //name
          applicationContext.setNotificationMsgWithValues('app.PLEASE_FILL_CORRECT_DATA_IN_FIELDS', 'danger', true);
          return;
        }

        $scope.entityEditDetails['name'] = $scope.entityDetails.info[0][0].value; //name
        $scope.entityEditDetails['description'] = $scope.entityDetails.info[1][0].value; //description


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
          $state.go($scope.entityDetailsStateName, {entityId: response.data.id});
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          $state.go($scope.entityListStateName);
        });
      };

      /**
       * Fired when user click user name
       */
      $scope.viewUser = function(object) {
        $state.go('authenticated.settings.accounts.users.userDetails', {entityId: object.id});
      };

      /**
       * Fired when user click Role Name
       */
      $scope.viewRole = function(object) {
        $state.go('authenticated.settings.accounts.roles.roleDetails', {entityId: object.id});
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

      //call initialization file
      $scope.init();
    }
]);
