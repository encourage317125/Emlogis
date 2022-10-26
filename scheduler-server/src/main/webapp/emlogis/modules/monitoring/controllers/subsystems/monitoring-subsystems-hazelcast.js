angular.module('emlogis.monitoring').controller('MonitoringSubsystemsHazelcastCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext', '$filter',
    function ($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter) {

      $scope.entityType = 'hazelcast';
      MonitoringEntityDetailsCtlr($scope, $q, MonitoringService, $state, $stateParams, applicationContext, $filter);

      $scope.names = [
        {
          globalAssignmentReqQueueSize: 'monitoring.hazelcast.GLOBAL_ASSIGNMENT_REQ_QUEUE_SIZE',
          globalQualificationReqQueueSize: 'monitoring.hazelcast.GLOBAL_QUALIFICATION_REQ_QUEUE_SIZE',
          globalResponseQueueSize: 'monitoring.hazelcast.GLOBAL_RESPONSE_QUEUE_SIZE'
        }, {
          requestDataMapSize: 'monitoring.hazelcast.REQUEST_DATAMAP_SIZE',
          responseDataMapSize: 'monitoring.hazelcast.RESPONSE_DATAMAP_SIZE',
          qualificationTrackerMapSize: 'monitoring.hazelcast.QUALIFICATION_TRACKER_MAP_SIZE'
        }, {
          abortMapSize: 'monitoring.hazelcast.ABORT_MAP_SIZE',
          shutdownMapSize: 'monitoring.hazelcast.SHUTDOWN_MAP_SIZE'
        }, {
          appServers: 'monitoring.hazelcast.APPSERVERS',
          engines: 'monitoring.hazelcast.ENGINES'
        }
      ];

    }
  ]);