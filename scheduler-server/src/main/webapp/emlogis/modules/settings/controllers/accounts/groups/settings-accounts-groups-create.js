angular.module('emlogis.settings').controller('SettingsAccountsGroupsCreateCtrl', ['$scope', '$q', '$timeout', '$state', '$modal', '$http', '$filter', 'applicationContext', 'crudDataService', 'uiGridConstants', 'SettingsAccountsService', 'UtilsService',
  function ($scope, $q, $timeout, $state, $modal, $http, $filter, applicationContext, crudDataService, uiGridConstants, SettingsAccountsService, UtilsService) {

    $scope.editMode = 'New';
    $scope.editLabel = 'app.CREATE';
    $scope.entityId = null;

    $scope.populateEntityDetails = function (entity) {
      var entityDetails = {};

      entityDetails.id = null;
      entityDetails.name = null;
      entityDetails.info = [
        [
          {
            label: 'app.NAME',
            key: 'name',
            placeHolder: 'app.NAME',
            value: null,
            class: 'col-sm-3',
            type: 'text',
            validation: 'required'
          },
          {
            label: 'settings.accounts.DESCRIPTION',
            key: 'description',
            placeHolder: 'settings.accounts.DESCRIPTION',
            value: null,
            class: 'col-sm-3',
            type: 'text',
            validation: 'none'
          }
        ]
      ];

      return entityDetails;
    };
  }
]);
