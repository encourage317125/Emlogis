angular.module('emlogis.monitoring').controller('MonitoringSubsystemsAppServersDetailsCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', '$stateParams', 'applicationContext', '$filter',
    function ($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter) {

      $scope.entityType = 'appserver';
      MonitoringEntityDetailsCtlr($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter);

      $scope.terminate = function () {
        console.log("terminate");
      };
      $scope.buttons = [
        {
          title: '<span class="glyphicon glyphicon-refresh" />',
          onClick: $scope.loadData
        },
        {
          title: 'Terminate (N/A)',
          onClick: $scope.terminate
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

      $scope.parentState = 'authenticated.monitoring.subsystems.appservers';


    }
  ]);