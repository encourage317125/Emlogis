angular.module('emlogis.settings').controller('SettingsAccountsUsersUserDetailsCtrl',
  ['$scope', '$state', '$stateParams', '$q', '$translate', 'dataService', 'applicationContext',
  function ($scope, $state, $stateParams, $q, $translate, dataService, applicationContext) {

    $scope.userAccountId = $stateParams.entityId;
    $scope.userDetails = null;
    $scope.unassosiatedGroups = null;
    $scope.unassosiatedRoles = null;
    $scope.initialGroups = [];
    $scope.initialRoles = [];
    $scope.createdOn = null;
    $scope.updatedOn = null;
    $scope.lastLoggedIn = "-";
    $scope.enableUserStatusEditing = false;

    $translate('settings.accounts.ARE_YOU_SURE_DELETE_USER?').
      then(function (translation) {
        $scope.confirmationToDeleteUser = translation;
      });

    dataService.getUserAccountView($scope.userAccountId).
      then(function(res) {
        $scope.userDetails = res.data;

        $scope.userName = $scope.userDetails.firstName + " " + $scope.userDetails.lastName;
        $scope.initialGroups = _.clone($scope.userDetails.groups);
        $scope.initialRoles = _.clone($scope.userDetails.roles);
        $scope.userDetails.gender = parseInt($scope.userDetails.gender);
        $scope.createdOn = new Date($scope.userDetails.created).toDateString();
        $scope.updatedOn = new Date($scope.userDetails.updated).toDateString();

        if ($scope.userDetails.lastLogged) {
          $scope.lastLoggedIn = new Date($scope.userDetails.lastLogged).toDateString();
        }
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });

    dataService.getUnassociatedAccountGroups($scope.userAccountId, {filter: "primaryKey.id NOT Like 'employeegroup%'"}).
      then(function(res) {
        $scope.unassosiatedGroups = res.data.result;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });

    dataService.getUnassociatedAccountRoles($scope.userAccountId).
      then(function(res) {
        $scope.unassosiatedRoles = res.data.result;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });

    $scope.inactivityPeriodOptions = [
      {value: 0, label: "-"}
    ];

    for (var i=5; i<35; i=i+5) {
      $scope.inactivityPeriodOptions.push({value: i, label: i +" min"});
    }

    $scope.genderOptions = [
      {value: 0, label: "N/A"},
      {value: 1, label: "Male"},
      {value: 2, label: "Female"}
    ];

    $scope.userStatusOptions = [
      {value: "Active", label: "Active"},
      {value: "Suspended", label: "Suspended"},
      {value: "Locked", label: "Locked"},
      {value: "Revoked", label: "Revoked"}
    ];

    $scope.addGroupToUser = function(group) {
      $scope.userDetails.groups.push(group);
    };

    $scope.removeGroupFromUser = function(group) {
      $scope.userDetails.groups = _.reject($scope.userDetails.groups, function(g) {
        return g.name === group.name;
      });
    };

    $scope.addRoleToUser = function(role) {
      $scope.userDetails.roles.push(role);
    };

    $scope.removeRoleFromUser = function(role) {
      $scope.userDetails.roles = _.reject($scope.userDetails.roles, function(r) {
        return r.name === role.name;
      });
    };

    $scope.editUserStatus = function(edit) {
      $scope.enableUserStatusEditing = edit;
    };

    $scope.requestPasswordReset = function() {
      dataService.requestPasswordReset({
        tenantId: $scope.userDetails.tenantId,
        login: $scope.userDetails.login
      }).
        then(function(res) {
          applicationContext.setNotificationMsgWithValues('settings.accounts.PASSWORD_RESETED', 'success', true);
        }, function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
    };

    $scope.updateUserAccount = function() {
      var dto = _.clone($scope.userDetails),
        groupsToAdd = _.reject(dto.groups, function(g) {
          return _.some($scope.initialGroups, 'name', g.name);
        }),
        groupsToRemove = _.filter($scope.initialGroups, function(g) {
          return !_.some(dto.groups, 'name', g.name);
        }),
        rolesToAdd = _.reject(dto.roles, function(r) {
          return _.some($scope.initialRoles, 'name', r.name);
        }),
        rolesToRemove = _.filter($scope.initialRoles, function(r) {
          return !_.some(dto.roles, 'name', r.name);
        });

      delete dto.groups;
      delete dto.roles;
      delete dto.inheritedRoles;
      delete dto.timeZone;
      delete dto.tenantId;

      $q.all([
        dataService.updateUserAccount($scope.userAccountId, dto),
        dataService.addGroupsToUserAccount($scope.userAccountId, _.pluck(groupsToAdd, "id")),
        dataService.removeGroupsFromUserAccount($scope.userAccountId, _.pluck(groupsToRemove, "groupId")),
        dataService.addRolesToUserAccount($scope.userAccountId, _.pluck(rolesToAdd, "id")),
        dataService.removeRolesFromUserAccount($scope.userAccountId, _.pluck(rolesToRemove, "roleId"))
      ])
      .then(function(res) {
        applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
        $scope.userName = $scope.userDetails.firstName + " " + $scope.userDetails.lastName;
      }, function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };

    $scope.deleteUserAccount = function() {
      if (confirm($scope.confirmationToDeleteUser)) {
        dataService.deleteUserAccount($scope.userAccountId).
          then(function(res) {
            applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
            $state.go('authenticated.settings.accounts.users.list');
          }, function (err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
      }
    };

  }
]);
