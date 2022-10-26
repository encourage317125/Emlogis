angular.module('emlogis.monitoring').controller('MonitoringSubsystemsNotificationsCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext', '$filter',
    function ($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter) {

      $scope.entityType = 'notifications';
      MonitoringEntityDetailsCtlr($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter);

      $scope.names = [
        {
          send: 'monitoring.notifications.SEND'
        },{
          receive: 'monitoring.notifications.RECEIVE'
        },{
          archivedReceiveQueue: 'monitoring.notifications.ARCHIVE_SEND'
        },{
          archivedSendQueue: 'monitoring.notifications.ARCHIVE_RECEIVED'
        }
      ];
    }
  ]);