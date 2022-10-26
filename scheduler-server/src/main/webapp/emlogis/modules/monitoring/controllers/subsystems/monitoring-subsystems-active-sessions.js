angular.module('emlogis.monitoring').controller('MonitoringSubsystemsActiveSessionsCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext',
    function ($scope, $q, MonitoringService, $state, applicationContext) {

      $scope.entityType = 'session';
      $scope.entityDetailsField = 'token';
      $scope.saveResult = true;

      MonitoringEntityListCtlr($scope, $q, MonitoringService, $state, applicationContext);

      $scope.terminate = function () {
        console.log("terminate");
      };

      $scope.buttons = [
        {
          title: 'Terminate Sessions (N/A)',
          onClick: $scope.terminate
        }
      ];

      $scope.gridOptions.columnDefs = [
        {
          field: 'tenantId',
          name: 'monitoring.sessions.TENANT',
          headerCellTemplate: $scope.headerCellTemplate(),
          minWidth: 150
        },
        {
          field: 'userName',
          headerCellTemplate: $scope.headerCellTemplate(),
          name: 'monitoring.sessions.USER'
        },
        {
          field: 'impersonatingUserName',
          headerCellTemplate: $scope.headerCellTemplate(),
          name: 'monitoring.sessions.IMPERSONATING'
        },
        {
          field: 'clientIPAddress',
          headerCellTemplate: $scope.headerCellTemplate(),
          name: 'monitoring.sessions.IP'
        },
        {
          field: 'clientHostname',
          headerCellTemplate: $scope.headerCellTemplate(),
          name: 'monitoring.sessions.HOSTNAME'
        },
        {
          field: 'started',
          headerCellTemplate: $scope.headerCellTemplate(),
          cellTemplate: $scope.dateCellTemplate(),
          name: 'monitoring.sessions.STARTED'
        },
        {
          field: 'clientUserAgent',
          headerCellTemplate: $scope.headerCellTemplate(),
          name: 'monitoring.sessions.USER_AGENT'
        }

      ];

      $q.when($scope.loadPromise).then(function (res) {
        MonitoringService.setSessions($scope.entityList);
      });

    }
  ]);