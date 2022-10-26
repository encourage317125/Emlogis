angular.module('emlogis.monitoring').controller('MonitoringSubsystemsEnginesCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext',
    function ($scope, $q, MonitoringService, $state, applicationContext) {

      $scope.entityType = 'engine';
      $scope.entityDetailsField = 'name';

      MonitoringEntityListCtlr($scope, $q, MonitoringService, $state, applicationContext);
      $scope.shutdownPromise = function () {
        var promises = [];
        angular.forEach($scope.selectedEntities, function (entity) {
          promises.push(MonitoringService.shutdownEngine(entity.name));
        });
        return $q.all(promises);
      };

      $scope.shutdown = function () {
        $scope.shutdownPromise()
          .then(function () {
            $scope.loadData();
          });
      };

      $scope.buttons = [
        {
          title: 'Shutdown',
          onClick: $scope.shutdown
        }
      ];
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