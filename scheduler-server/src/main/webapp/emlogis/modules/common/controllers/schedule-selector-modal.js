angular.module('emlogis.commonControllers').controller('ScheduleSelectorModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout',
  'applicationContext', 'dataService', 'uiGridConstants','checkboxModel','customFilter',
  function ($scope, $modalInstance, $timeout, applicationContext, dataService,
            uiGridConstants,checkboxModel,customFilter) {

    $scope.original = {
      schedules: [],
      sites: [],
      teams: []
    };

    $scope.loadingState = {
      schedulesLoaded: false
    };

    if (checkboxModel) {
      $scope.checkboxModel = checkboxModel;
    }
    else {
      $scope.checkboxModel = {
        simulationGenerated: false,
        production: true,
        posted: true
      };
    }

    function scheduleComparator(firstSchedule, secondSchedule) {
      if (firstSchedule.start < secondSchedule.start) {
        return 1;
      } else if (firstSchedule.start > secondSchedule.start) {
        return -1;
      } else {
        return 0;
      }
    }

    $scope.datePickerModel = {
      scheduleDate: null,
      datePickerOpened: false,
      datePickerOptions: {
        formatYear: 'yyyy',
        startingDay: 1
      },
      openDatePicker: function($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.datePickerModel.datePickerOpened = true;
      }
    };

    $scope.parseFilteredScheduleDate = function() {
      if (typeof $scope.datePickerModel.scheduleDate === 'undefined' || $scope.datePickerModel.scheduleDate === null) {
        $scope.filter.scheduleDate = null;
        $scope.filterSchedules();
        return;
      }

      var yearVal = $scope.datePickerModel.scheduleDate.getFullYear();
      var monthVal = $scope.datePickerModel.scheduleDate.getMonth();
      var dateVal = $scope.datePickerModel.scheduleDate.getDate();
      var dateInArrFormat = [yearVal, monthVal, dateVal];
      $scope.filter.scheduleDate = moment.tz(dateInArrFormat, $scope.filter.site.siteTimeZone).unix() * 1000;
      $scope.filterSchedules();
    };

    $scope.filter = {
      scheduleName: '',
      site: null,
      team: null,
      scheduleDate: null
    };

    $scope.sortOptions = {
      sortBy: null,
      sortDir: null
    };

    $scope.filterSchedules = function() {
      $scope.schedulesGridOptions.paginationCurrentPage = 1;
      $scope.getSchedules();
    };

    $scope.onSelectedSiteChanged = function() {
      if ($scope.filter.site === null) {
        $scope.original.teams = [];
      } else {
        $scope.original.teams = $scope.filter.site.children;
      }
      $scope.filter.team = null;
      $scope.datePickerModel.scheduleDate = null;
      $scope.filter.scheduleDate = null;
      $scope.filterSchedules();
    };

    $scope.onSelectedTeamChanged = function() {
      $scope.datePickerModel.scheduleDate = null;
      $scope.filter.scheduleDate = null;
      $scope.filterSchedules();
    };

    $scope.parseSchedule = function(schedule) {
      var lengthInWeeks = moment.tz(schedule.endDate, schedule.site.siteTimeZone).week() - moment.tz(schedule.startDate, schedule.site.siteTimeZone).week() + 1;
      var teamsText = '';
      var teamsValues = '';

      if (schedule.teams.length === 0) {
        teamsText = '-';
      } else if (schedule.teams.length === 1) {
        teamsText = schedule.teams[0].teamName;
      } else {
        teamsText = schedule.teams.length + ' teams';
      }

      angular.forEach(schedule.teams, function(teamIterator, index) {
        if (index === schedule.teams.length -1 ) {
          teamsValues += teamIterator.teamName;
        } else {
          teamsValues += teamIterator.teamName + ',\r\n';
        }
      });


      var entity = {
        id: schedule.id,
        name: schedule.name,
        status: schedule.status,
        start: schedule.startDate,
        end: schedule.endDate,
        startDate: moment.tz(schedule.startDate, schedule.site.siteTimeZone).format('MMMM DD, YYYY'),
        endDate: moment.tz(schedule.endDate, schedule.site.siteTimeZone).format('MMMM DD, YYYY'),
        site: schedule.site.siteName,
        teamsValues: teamsValues,
        teams: teamsText,
        lengthInDays: schedule.lengthInDays,
        lengthInWeeks: lengthInWeeks,
        lengthInDaysString: schedule.lengthInDays + ' Days'
      };

      return entity;
    };

    $scope.selectSchedule = function(row) {
      $modalInstance.close(row.entity);
    };

    function rowTemplate() {
      return '<div ng-click="grid.appScope.selectSchedule(row)" ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>';
    }

    $scope.processOnSortChanged = function(grid, sortColumns) {
      if (sortColumns.length === 0) {
        $scope.sortOptions.sortBy = null;
        $scope.sortOptions.sortDir = null;
      } else {
        $scope.sortOptions.sortBy = sortColumns[0].field;
        if (sortColumns[0].sort.direction === uiGridConstants.ASC) {
          $scope.sortOptions.sortDir = 'ASC';
        } else if (sortColumns[0].sort.direction === uiGridConstants.DESC) {
          $scope.sortOptions.sortDir = 'DESC';
        }
      }
      $scope.getSchedules();
    };

    $scope.schedulesGridOptions = {
      data: 'original.schedules',
      useExternalSorting: true,
      paginationPageSize: 10,
      paginationCurrentPage: 1,
      rowTemplate: rowTemplate(),
      columnDefs: [
        { field: 'id', visible: false },
        { field: 'name' },
        { field: 'startDate' },
        { field: 'endDate' },
        { field: 'status' },
        { field: 'site', enableSorting: false },
        { field: 'teams',
          enableSorting: false,
          cellTooltip: function( row, col ) {
            return row.entity.teamsValues;
          }
        },
        { field: 'teamsValues', visible: false }
      ],
      onRegisterApi: function( gridApi ) {
        gridApi.core.on.sortChanged( $scope, $scope.processOnSortChanged );
      }
    };

    $scope.getSchedules = function() {
      $scope.loadingState.schedulesLoaded = false;
      $scope.original.schedules = [];
      var statusesVal = null;
      var scheduleDateVal = null;
      var sitesVal = null;
      var teamsVal = null;

      if ($scope.checkboxModel.simulationGenerated ||
        $scope.checkboxModel.production || $scope.checkboxModel.posted) {
        statusesVal = [];
        if ($scope.checkboxModel.simulationGenerated) {
          statusesVal.push(0);
        }
        if ($scope.checkboxModel.production) {
          statusesVal.push(1);
        }
        if ($scope.checkboxModel.posted) {
          statusesVal.push(2);
        }
      } else {
        $scope.schedulesGridOptions.totalItems = 0;
        $scope.loadingState.schedulesLoaded = true;
        $timeout(function () {
          $('.schedules-grid').resize();
        }, 0);
        return;
      }

      if ($scope.filter.site !== null) {
        sitesVal = [$scope.filter.site.id];
        scheduleDateVal = $scope.filter.scheduleDate;
        if ($scope.filter.team !== null) {
          teamsVal = [$scope.filter.team.id];
        }
      }

      var payLoad = {
        sites: sitesVal,
        teams: teamsVal,
        statuses: statusesVal,
        startDate: scheduleDateVal,
        search: $scope.filter.scheduleName,
        paging: {
          limit: $scope.schedulesGridOptions.paginationPageSize,
          offset: ($scope.schedulesGridOptions.paginationCurrentPage - 1) * $scope.schedulesGridOptions.paginationPageSize
        },
        ordering: {
          orderby: $scope.sortOptions.sortBy,
          orderdir: $scope.sortOptions.sortDir
        }
      };

      if ( customFilter) {
        payLoad[customFilter.key] = customFilter.value;
      }
      dataService.getSchedules(payLoad).then(function(response) {
        angular.forEach(response.data, function(scheduleIterator) {
          var entity = $scope.parseSchedule(scheduleIterator);
          $scope.original.schedules.push(entity);
        });
        $scope.original.schedules.sort(scheduleComparator);
        $scope.schedulesGridOptions.totalItems = response.total;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      }).finally(function() {
        $scope.loadingState.schedulesLoaded = true;
        $timeout(function () {
          $('.schedules-grid').resize();
        }, 0);
      });
    };

    $scope.navigateSchedulesPage = function() {
      $scope.getSchedules();
    };

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };

    function siteTeamComparator(firstEntity, secondEntity) {
      if (firstEntity.name < secondEntity.name) {
        return -1;
      } else if (firstEntity.name > secondEntity.name) {
        return 1;
      } else {
        return 0;
      }
    }

    var initializeSitesTeams = function() {
      dataService.getSitesTeamsTree({}).then(function(response) {
        $scope.original.sites = response.data;
        $scope.original.sites.sort(siteTeamComparator);
        angular.forEach($scope.original.sites, function(site) {
          site.children.sort(siteTeamComparator);
        });
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    initializeSitesTeams();
    $scope.getSchedules();
  }]);
