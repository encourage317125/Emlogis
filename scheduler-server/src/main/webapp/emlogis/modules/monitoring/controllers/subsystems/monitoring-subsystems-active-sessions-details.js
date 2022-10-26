angular.module('emlogis.monitoring').controller('MonitoringSubsystemsActiveSessionsDetailsCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', '$stateParams', 'applicationContext', '$filter',
    function ($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter) {

      $scope.vetoLoad = true;

      MonitoringEntityDetailsCtlr($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter);

      $scope.terminate = function () {
        console.log("terminate");
      };

      $scope.buttons = [
        {
          title: 'Terminate Session (N/A)',
          onClick: $scope.terminate
        }
      ];

      $scope.entity = MonitoringService.getSessionByToken($stateParams.id);
      $scope.title = $scope.entity.token + ' / ' + $scope.entity.tenantId + ' - ' + $scope.entity.userId;
      $scope.names = [
        {
          tenantId: 'monitoring.sessions.TENANT'
        }, {
          userName: 'monitoring.sessions.USER',
          impersonatingUserName: 'monitoring.sessions.IMPERSONATING'
        }, {
          clientIPAddress: 'monitoring.sessions.IP',
          clientHostname: 'monitoring.sessions.HOSTNAME'
        }, {
          started: 'monitoring.sessions.SESSIONS_STARTED'
        }, {
          clientUserAgent: 'monitoring.sessions.USER_AGENT'
        }, {
          token: 'monitoring.sessions.TOKEN'
        }
      ];

      $scope.options = {
        started: {
          filter: 'date',
          params: 'yyyy/MM/dd HH:mm:ss'
        }
      };
      $scope.parentState = 'authenticated.monitoring.subsystems.activesessions';


    }
  ]);