angular.module('emlogis.monitoring').controller('MonitoringSubsystemsAppServersCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext',
    function ($scope, $q, MonitoringService, $state, applicationContext) {

      $scope.entityType = 'appserver';
      $scope.entityDetailsField = 'name';

      MonitoringEntityListCtlr($scope, $q, MonitoringService, $state, applicationContext);

      $scope.gridOptions.columnDefs = [
        {
          field: 'ip',
          name: 'monitoring.sessions.IP',
          headerCellTemplate: $scope.headerCellTemplate()
        }, {
          field: 'name',
          name: 'app.NAME',
          headerCellTemplate: $scope.headerCellTemplate()
        }, {
          field: 'updated',
          name: 'monitoring.UPDATED',
          cellTemplate: $scope.dateCellTemplate(),
          headerCellTemplate: $scope.headerCellTemplate()
        }
      ];

    }
  ]);