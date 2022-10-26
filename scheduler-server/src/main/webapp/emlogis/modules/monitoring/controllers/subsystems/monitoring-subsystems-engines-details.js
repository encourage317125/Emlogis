angular.module('emlogis.monitoring').controller('MonitoringSubsystemsEnginesDetailsCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', '$stateParams', 'applicationContext', '$filter',
    function ($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter) {

      $scope.entityType = 'engine';
      MonitoringEntityDetailsCtlr($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter);

      $scope.shutdown = function () {
        MonitoringService.shutdownEngine($stateParams.id)
          .then(function (response) {
            MonitoringService.stopRefresh();
            $scope.buttons[1].disabled = true;
            applicationContext.setNotificationMsgWithValues('Engine has been shut down', 'success', true);
          }, function (error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, 'danger', true);
          });
      };

      $scope.buttons = [
        {
          title: '<span class="glyphicon glyphicon-refresh" />',
          onClick: $scope.loadData
        },
        {
          title: 'Shutdown Engine (N/A)',
          onClick: $scope.shutdown,
          disabled: false
        }
      ];

      $q.when($scope.loadPromise).then(function (res) {
        $scope.title = $scope.entity.name;
      });

      $scope.names = [
        {
          name: 'app.NAME'
        }, {
          updated: 'monitoring.UPDATED'
        }, {
          ip: 'monitoring.sessions.IP'
        }
      ];

      $scope.options = {
        updated: {
          filter: 'date',
          params: 'yyyy/MM/dd HH:mm:ss'
        }
      };

      $scope.parentState = 'authenticated.monitoring.subsystems.engines';


    }
  ]);