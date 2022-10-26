angular.module('emlogis.settings').controller('SettingsAccountsRolesListCtrl',
  [
    '$scope',
    '$state',
    '$http',
    '$filter',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'SettingsAccountsService',
    'UtilsService',
    function ($scope, $state, $http, $filter, applicationContext, crudDataService, uiGridConstants,
              SettingsAccountsService, UtilsService) {

      $scope.entityDetails = {
        entityType: $scope.consts.entityTypes.role,
        entityColumnDefs: [
          { field: 'id', visible: false },
          { field: 'name', width: '12%' },
          { field: 'description', width: '15%' },
          { field: 'groups', width: '15%' },
          { field: 'userAccounts', enableSorting: false },
          { field: 'created', width: '13%', visible: false },
          { field: 'updated', width: '13%', visible: false },
          { field: 'ownedBy', width: '12%', visible: false }
        ],
        needPagination: true,
        entityDetailsStateName: 'authenticated.settings.accounts.roles.roleDetails'
      };

      $scope.convertEntityToGridRow = function (role) {
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
      };
    }
  ]
);
