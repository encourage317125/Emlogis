angular.module('emlogis.monitoring').controller('MonitoringDatabasePerCustomerCtrl',
  ['$scope', '$q', 'MonitoringService', '$state', 'applicationContext',
    function ($scope, $q, MonitoringService, $state, applicationContext) {

      $scope.entityType = 'dbpercustomer';
      $scope.entityDetailsField = '';

      MonitoringEntityListCtlr($scope, $q, MonitoringService, $state, applicationContext);
      $scope.gridOptions.enableRowHeaderSelection = false;
      $scope.gridOptions.multiSelect = false;
      $scope.gridOptions.columnDefs = [
        {
          field: 'name',
          minWidth: 150
        },
        {
          field: 'dbInfo.Site',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SITES'
        },
        {
          field: 'dbInfo.Team',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.TEAMS'
        },
        {
          field: 'dbInfo.Employee',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.EMPLOYEES'
        },
        {
          field: 'dbInfo.UserAccount',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.ACCOUNTS'
        },
        {
          field: 'dbInfo.GroupAccount',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.GROUPS'
        },
        {
          field: 'dbInfo.Role',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.ROLES'
        },
        {
          field: 'dbInfo.ACE',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.ACE'
        },
        {
          field: 'dbInfo.Skill',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SKILLS'
        },
        {
          field: 'dbInfo.AvailabilityTimeFrame.total',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.AVAILABILITY_RECORDS'
        },
        {
          field: 'dbInfo.Contract.total',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.CONTRACTS'
        },
        {
          field: 'dbInfo.ContractLine.total',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.CONTRACT_LINES'
        },
        {
          field: 'dbInfo.ShiftLength',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SHIFT_LENGTHS'
        },
        {
          field: 'dbInfo.ShiftType',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SHIFT_TYPES'
        },
        {
          field: 'dbInfo.Schedule.total',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SCHEDULES'
        },
        {
          field: 'dbInfo.ScheduleChange.total',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SCHEDULE_CHANGES'
        },
        {
          field: 'dbInfo.ShiftPattern',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SHIFT_PATTERNS'
        },
        {
          field: 'dbInfo.ShiftReq',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SHIFT_REQS'
        },
        {
          field: 'dbInfo.Shift',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SHIFTS'
        },
        {
          field: 'dbInfo.PatternElt',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SHIFT_REQS_ELTS'
        },
        {
          field: 'dbInfo.PostedOpenShift.total',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.POSTED_OPEN_SHIFTS'
        },
        {
          field: 'dbInfo.SendNotification',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.SEND_NOTIFICATION'
        },
        {
          field: 'dbInfo.ReceiveNotification',
          headerCellTemplate: headerCellTemplate(),
          name: 'monitoring.RECEIVE_NOTIFICATION'
        }
      ];

      function headerCellTemplate() {
        return "<div ng-class=\"{ 'sortable': sortable }\">" +
          "<div class=\"ui-grid-vertical-bar\">&nbsp;</div>" +
          "<div class=\"ui-grid-cell-contents\" col-index=\"renderIndex\" title='{{col.name CUSTOM_FILTERS | translate}}' ><span translate>{{col.name CUSTOM_FILTERS }} </span>" +
          "<span ui-grid-visible=\"col.sort.direction\" class='ui-grid-icon-my' ng-class=\"{ 'ui-grid-icon-up-dir': col.sort.direction == asc, 'ui-grid-icon-down-dir': col.sort.direction == desc, 'ui-grid-icon-blank': !col.sort.direction }\">&nbsp;</span></span>" +
          "</div>" +
          "<div ng-if=\"filterable\" class=\"ui-grid-filter-container\" ng-repeat=\"colFilter in col.filters\">" +
          "<input type=\"text\" class=\"ui-grid-filter-input\" ng-model=\"colFilter.term\" ng-click=\"$event.stopPropagation()\" ng-attr-placeholder=\"{{colFilter.placeholder || ''}}\">" +
          "<div class=\"ui-grid-filter-button\" ng-click=\"colFilter.term = null\">" +
          "<i class=\"ui-grid-icon-cancel right\" ng-show=\"!!colFilter.term\">&nbsp;</i> <!-- use !! because angular interprets 'f' as false -->" +
          "</div>" +
          "</div>" +
          "</div>";
      }

    }
  ]);
