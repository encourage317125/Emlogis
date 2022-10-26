angular.module('emlogis.settings').controller('SettingsAccountsUsersListCtrl', ['$scope', '$state', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $state, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {

    $scope.entityDetails = {
      entityType: $scope.consts.entityTypes.user,
      entityColumnDefs: [
        { field: 'id', visible: false },
        { field: 'name'},
        { field: 'login'},
        { field: 'status'},
        { field: 'employeeAccount', enableSorting: false },
        { field: 'groups'},
        { field: 'roles', visible: false},
        { field: 'created', enableSorting: false, visible: false},
        { field: 'updated', enableSorting: false, visible: false},
        { field: 'ownedBy', enableSorting: false, visible: false}
      ],
      needPagination: true,
      entityDetailsStateName: 'authenticated.settings.accounts.users.userDetails'
    };

    $scope.convertEntityToGridRow = function (user) {
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
    };
  }
]);
