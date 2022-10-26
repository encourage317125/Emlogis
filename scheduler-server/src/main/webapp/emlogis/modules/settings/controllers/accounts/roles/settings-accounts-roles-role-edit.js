angular.module('emlogis.settings').controller('SettingsAccountsRolesRoleEditCtrl', ['$scope', '$state', '$stateParams', '$q', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $stateParams, $q, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {

    $scope.editMode = 'Edit';
    $scope.editLabel = 'app.UPDATE';
    $scope.entityId = $stateParams.entityId;

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
            class: 'col-sm-3',
            type: 'text',
            validation: 'required'
          },
          {
            label: 'settings.accounts.DESCRIPTION',
            key: 'description',
            placeHolder: 'settings.accounts.DESCRIPTION',
            value: entity.description,
            class: 'col-sm-4',
            type: 'text',
            validation: 'none'
          },
          {
            label: 'settings.accounts.LABEL',
            key: 'label',
            placeHolder: 'settings.accounts.LABEL',
            value: entity.label,
            class: 'col-sm-3',
            type: 'text',
            validation: 'none'
          }
        ]
      ];

      entityDetails.relatedEntities = [
        {
          entityDetails: {
            entityType: $scope.consts.entityTypes.accessControl,
            tabHeading: 'settings.accounts.ACCESS_CONTROL'
          },
          isSelectionList: false,
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
          isSelectionList: false,
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
            needPagination: false
          },
          isSelectionList: true,
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
          templateUrl: 'modules/settings/partials/include/entity-selection-list-wrapper.html'
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
            needPagination: true
          },
          isSelectionList: true,
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
          templateUrl: 'modules/settings/partials/include/entity-selection-list-wrapper.html'
        }
      ];

      return entityDetails;
    };
  }
]);
