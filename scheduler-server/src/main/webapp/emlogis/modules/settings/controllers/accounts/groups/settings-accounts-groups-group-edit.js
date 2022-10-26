angular.module('emlogis.settings').controller('SettingsAccountsGroupsGroupEditCtrl', ['$scope', '$state', '$q', '$stateParams', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $q, $stateParams, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {

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
            class: 'col-sm-3',
            type: 'text',
            validation: 'none'
          }
        ]
      ];

      entityDetails.relatedEntities = [
        {
          entityDetails: {
            entityType: $scope.consts.entityTypes.user,
            tabHeading: 'settings.accounts.USERS',
            entityColumnDefs: [
              { field: 'id', visible: false },
              { field: 'name', width: '12%' },
              { field: 'login', width: '12%' },
              { field: 'status', width: '8%' },
              { field: 'employeeAccount', width: '10%', enableSorting: false },
              { field: 'groups', width: '13%', enableSorting: false },
              { field: 'roles', width: '13%', enableSorting: false },
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
          selected: true,
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
        },
        {
          entityDetails: {
            entityType: $scope.consts.entityTypes.role,
            tabHeading: 'settings.accounts.ROLES',
            entityColumnDefs: [
              { field: 'id', visible: false },
              { field: 'name', width: '12%' },
              { field: 'description', width: '15%' },
              { field: 'groups', width: '15%' },
              { field: 'userAccounts', width: '20%' },
              { field: 'created', width: '13%' },
              { field: 'updated', width: '13%' },
              { field: 'ownedBy', width: '12%' }
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
                deferred.reject('Error Occurred while trying to get Roles List');
              }
            }, function (err) {
              applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
              deferred.reject('Error Occurred while trying to get Roles List');
            });
            return deferred.promise;
          },
          selected: false,
          convertEntityToGridRow: function (role) {
            var gridRow = {};

            gridRow.id = role.id;
            gridRow.name = role.name;
            gridRow.description = role.description;
            gridRow.groups = role.groups;
            gridRow.userAccounts = role.userAccounts;
            gridRow.created = new Date(role.created).toString();
            gridRow.updated = new Date(role.updated).toString();
            gridRow.ownedBy = role.ownedBy;

            return gridRow;
          },
          templateUrl: 'modules/settings/partials/include/entity-selection-list-wrapper.html'
        }
      ];

      return entityDetails;
    };
  }
]);
