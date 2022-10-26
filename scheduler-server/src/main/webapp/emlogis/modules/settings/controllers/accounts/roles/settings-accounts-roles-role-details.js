angular.module('emlogis.settings')
  .controller('SettingsAccountsRolesRoleDetailsCtrl',
  [
    '$scope',
    '$state',
    '$stateParams',
    '$q',
    '$translate',
    '$http',
    '$modal',
    '$log',
    '$filter',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'SettingsAccountsService',
    'UtilsService',
    'dataService',
    function ($scope, $state, $stateParams, $q, $translate, $http, $modal, $log, $filter, applicationContext, crudDataService,
              uiGridConstants, SettingsAccountsService, UtilsService, dataService) {

      // init function: settings/accounts/roles
      $scope.init = function() {

        $translate('settings.accounts.ARE_YOU_SURE_DELETE_ROLE?').
          then(function (translation) {
            $scope.confirmationToDeleteRole = translation;
          });

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
              validation: 'required',
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

        entityDetails.groups = {};
        entityDetails.users = {};
        entityDetails.permissions = {};
        entityDetails.acl = {};

        // Get list of users & roles & acls
        $scope.loadRelatedEntities($scope.consts.entityTypes.group, entityDetails.groups);
        $scope.loadRelatedEntities($scope.consts.entityTypes.user, entityDetails.users);
        $scope.loadRelatedEntities($scope.consts.entityTypes.permission, entityDetails.permissions);
        $scope.loadRelatedEntities($scope.consts.entityTypes.accessControl, entityDetails.acl);

        entityDetails.canDuplicate = true;
        entityDetails.fieldsToRedefineInDuplication = [
          {
            label: 'app.NAME',
            key: 'name',
            value: ''
          },
          {
            label: 'settings.accounts.LABEL',
            key: 'label',
            value: ''
          },
          {
            label: 'settings.accounts.DESCRIPTION',
            key: 'description',
            value: ''
          }
        ];
        entityDetails.relatedEntities = [
          {
            entityDetails: {
              entityType: $scope.consts.entityTypes.accessControl,
              tabHeading: 'settings.accounts.ACCESS_CONTROL'
            },
            isSelectionListDetails: false,
            selected: true,
            getEntityList: function () {
              var deferred = $q.defer();
              var self = this;
              SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, self.entityDetails.entityType, true).then(function (response) {
                if (response.data) {
                  deferred.resolve({data: response.data});
                } else {
                  deferred.reject('Error Occurred while trying to get Access Control List');
                }
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
                deferred.reject('Error Occurred while trying to get Access Control List');
              });
              return deferred.promise;
            },
            templateUrl: 'modules/settings/partials/accounts/roles/include/roles-access-control-list.html'
          },
          {
            entityDetails: {
              entityType: $scope.consts.entityTypes.permission,
              tabHeading: 'settings.accounts.PERMISSIONS'
            },
            isSelectionListDetails: false,
            selected: false,
            getEntityList: function () {
              var deferred = $q.defer();
              var self = this;
              SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, self.entityDetails.entityType, true).then(function (response) {
                if (response.data) {
                  deferred.resolve({data: response.data.result});
                } else {
                  deferred.reject('Error Occurred while trying to get Permissions List');
                }
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
                deferred.reject('Error Occurred while trying to get Permissions List');
              });
              return deferred.promise;
            },
            getUnassociatedEntityList: function () {
              var deferred = $q.defer();
              var self = this;
              SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, self.entityDetails.entityType, false).then(function (response) {
                if (response.data) {
                  deferred.resolve({data: response.data.result});
                } else {
                  deferred.reject('Error Occurred while trying to get Unassociated Permissions List');
                }
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
                deferred.reject('Error Occurred while trying to get Unassociated Permissions List');
              });
              return deferred.promise;
            },
            templateUrl: 'modules/settings/partials/accounts/roles/include/roles-permissions.html'
          },
          {
            entityDetails: {
              entityType: $scope.consts.entityTypes.group,
              tabHeading: 'settings.accounts.GROUPS',
              entityColumnDefs: [
                { field: 'id', visible: false },
                { field: 'name', width: '14%' },
                { field: 'description', width: '20%' },
                { field: 'members', width: '13%' },
                { field: 'roles', width: '13%' },
                { field: 'created', width: '15%' },
                { field: 'updated', width: '15%' },
                { field: 'ownedBy', width: '10%' }
              ],
              needPagination: false,
              entityDetailsStateName: 'authenticated.settings.accounts.groups.groupDetails'
            },
            isSelectionListDetails: true,
            getEntityList: function () {
              var deferred = $q.defer();
              var self = this;
              var offset = 0;
              var limit = 0;
              SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, self.entityDetails.entityType, true, null, offset, limit).then(function (response) {
                if (response.data) {
                  deferred.resolve({data: response.data.result});
                } else {
                  deferred.reject('Error Occurred while trying to get Groups List');
                }
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
                deferred.reject('Error Occurred while trying to get Groups List');
              });
              return deferred.promise;
            },
            selected: false,
            convertEntityToGridRow: function (group) {
              var gridRow = {};

              gridRow.id = group.id;
              gridRow.name = group.name;
              gridRow.description = group.description;
              gridRow.members = group.nbOfMembers;
              gridRow.roles = group.roles;
              gridRow.created = new Date(group.created).toString();
              gridRow.updated = new Date(group.updated).toString();
              gridRow.ownedBy = group.ownedBy;

              return gridRow;
            },
            templateUrl: 'modules/settings/partials/include/entity-list-wrapper.html'
          },
          {
            entityDetails: {
              entityType: $scope.consts.entityTypes.user,
              tabHeading: 'settings.accounts.USERS',
              entityColumnDefs: [
                { field: 'id', visible: false },
                { field: 'name', width: '12%' },
                { field: 'login', width: '12%' },
                { field: 'status', width: '8%' },
                { field: 'employeeAccount', width: '10%' },
                { field: 'groups', width: '13%' },
                { field: 'roles', width: '13%' },
                { field: 'created', width: '12%' },
                { field: 'updated', width: '12%' },
                { field: 'ownedBy', width: '8%' }
              ],
              needPagination: true,
              entityDetailsStateName: 'authenticated.settings.accounts.users.userDetails'
            },
            isSelectionListDetails: true,
            getEntityList: function (entityListGridOptions, paginationOptions) {
              var deferred = $q.defer();
              var self = this;
              var offset = (paginationOptions.pageNumber - 1) * paginationOptions.pageSize;
              var limit = paginationOptions.pageSize;
              var orderBy = paginationOptions.orderBy;
              var orderDir = paginationOptions.orderDir;
              SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, self.entityDetails.entityType, true, null, offset, limit, orderBy, orderDir).then(function (response) {
                if (response.data) {
                  entityListGridOptions.totalItems = response.data.total;
                  deferred.resolve({data: response.data.result});
                } else {
                  deferred.reject('Error Occurred while trying to get Users List');
                }
              }, function (err) {
                applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
                deferred.reject('Error Occurred while trying to get Users List');
              });
              return deferred.promise;
            },
            selected: false,
            convertEntityToGridRow: function (user) {
              var gridRow = {};

              gridRow.id = user.id;
              gridRow.name = user.name;
              gridRow.login = user.login;
              gridRow.status = user.status;

              if (typeof user.employeeId !== 'undefined' && user.employeeId !== null) {
                gridRow.employeeAccount = user.employeeFirstName + ' ' + user.employeeLastName;
              } else {
                gridRow.employeeAccount = '?';
              }

              gridRow.groups = user.groups;
              gridRow.roles = user.roles;
              gridRow.created = new Date(user.created).toString();
              gridRow.updated = new Date(user.updated).toString();
              gridRow.ownedBy = user.ownedBy;

              return gridRow;
            },
            templateUrl: 'modules/settings/partials/include/entity-list-wrapper.html'
          }
        ];

        return entityDetails;
      };

      /**
       * Load related entities
       */
      $scope.loadRelatedEntities = function(relatedEntityType, relatedEntities) {

        var deferred = $q.defer();

        SettingsAccountsService.getRelatedEntities($scope.entityType, $scope.entityId, relatedEntityType, true, null, 0,0, 'name', 'asc').then(function (response) {
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
       * Tag Directive Related Func
       */

      $scope.removeGroupFromRole = function(group) {

        return dataService.removeGroupsFromRole($scope.entity.id, [group.id])
          .then(function(res){
            $scope.loadRelatedEntities($scope.consts.entityTypes.group, $scope.entityDetails.groups);
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

      $scope.removeUserFromRole = function(user) {

        return dataService.removeUsersFromRole($scope.entity.id, [user.id])
          .then(function(res){
            $scope.loadRelatedEntities($scope.consts.entityTypes.user, $scope.entityDetails.users);
            $scope.loadRelatedEntities($scope.consts.entityTypes.group, $scope.entityDetails.groups);
          }, function(error) {
            if (error.message) {
              applicationContext.setNotificationMsgWithValues(error.message, '', true, '');
            }
            else {
              applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
            }
          });
      };

      $scope.removePermissionFromRole = function(permission) {

        return dataService.removePermissionFromRole($scope.entity.id, permission.id)
          .then(function(res){
            $scope.loadRelatedEntities($scope.consts.entityTypes.permission, $scope.entityDetails.permissions);
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
       * Eml Object related functions
       */
      $scope.openGroupsModal = function (role) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-roles-groups-modal.tmpl.html',
          controller: 'SettingsAccountsRolesGroupsModalCtrl as te',
          size: 'lg',
          resolve: {
            role: function () {
              return role;
            }
          }
        });

        modalInstance.result.then(function () {
          $scope.loadRelatedEntities($scope.consts.entityTypes.group, $scope.entityDetails.groups);
          $scope.loadRelatedEntities($scope.consts.entityTypes.user, $scope.entityDetails.users);

        }, function () {
          $log.info('Modal dismissed at: ' + new Date());
        });
      };

      $scope.openUsersModal = function (role) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-roles-users-modal.tmpl.html',
          controller: 'SettingsAccountsRolesUsersModalCtrl as te',
          size: 'lg',
          resolve: {
            role: function () {
              return role;
            }
          }
        });

        modalInstance.result.then(function () {
          $scope.loadRelatedEntities($scope.consts.entityTypes.user, $scope.entityDetails.users);
          $scope.loadRelatedEntities($scope.consts.entityTypes.group, $scope.entityDetails.groups);

        }, function () {
          $log.info('Modal dismissed at: ' + new Date());
        });
      };

      $scope.openPermissionsModal = function (role) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-roles-permissions-modal.tmpl.html',
          controller: 'SettingsAccountsRolesPermissionsModalCtrl as te',
          size: 'lg',
          resolve: {
            role: function () {
              return role;
            }
          }
        });

        modalInstance.result.then(function () {
          $scope.loadRelatedEntities($scope.consts.entityTypes.permission, $scope.entityDetails.permissions);

        }, function () {
          $log.info('Modal dismissed at: ' + new Date());
        });
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

      $scope.openAclModal = function (role) {

        var modalInstance = $modal.open({
          templateUrl: 'modules/settings/partials/include/settings-accounts-roles-acl-modal.tmpl.html',
          controller: 'SettingsAccountsRolesAclModalCtrl as te',
          size: 'lg',
          resolve: {
            role: function () {
              return role;
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
       * Fired when user click group name
       */
      $scope.viewGroup = function(object) {
        $state.go('authenticated.settings.accounts.groups.groupDetails', {entityId: object.id});
      };

      /**
       * Fired when user click user name
       */
      $scope.viewUser = function(object) {
        $state.go('authenticated.settings.accounts.users.userDetails', {entityId: object.id});
      };

      /**
       * Update Entity: Role Details
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
