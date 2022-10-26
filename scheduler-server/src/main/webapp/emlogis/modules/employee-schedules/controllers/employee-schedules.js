angular.module('emlogis.employeeSchedules')
  .controller('EmployeeSchedulesCtrl',
  ['$scope', '$state', '$stateParams', '$modal', '$sessionStorage', '$timeout', 'applicationContext', 'authService',
    'appFunc', 'dataService', 'EmployeeSchedulesService',
  function($scope, $state, $stateParams, $modal, $sessionStorage, $timeout, applicationContext, authService,
           appFunc, dataService, EmployeeSchedulesService) {

    $scope.consts = {
      daysOfWeek: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
      calendarTimeLabels: [
        {label: '12A', value: '00'},  {label: '1A', value: '01'},   {label: '2A', value: '02'},
        {label: '3A', value: '03'},   {label: '4A', value: '04'},   {label: '5A', value: '05'},
        {label: '6A', value: '06'},   {label: '7A', value: '07'},   {label: '8A', value: '08'},
        {label: '9A', value: '09'},   {label: '10A', value: '10'},  {label: '11A', value: '11'},
        {label: '12P', value: '12'},  {label: '1P', value: '13'},   {label: '2P', value: '14'},
        {label: '3P', value: '15'},   {label: '4P', value: '16'},   {label: '5P', value: '17'},
        {label: '6P', value: '18'},   {label: '7P', value: '19'},   {label: '8P', value: '20'},
        {label: '9P', value: '21'},   {label: '10P', value: '22'},  {label: '11P', value: '23'}
      ],
        scheduleStatus: {
            POSTED: "Posted",
            SIMULATION: "Simulation",
            PRODUCTION: "Production"
        }
    };

    $scope.sort = {
      field: 'employeeName',
      order: 'asc'
    };
    $scope.filter = {
      homeTeams: [],
      assignedTeams: [],
      primarySkills: [],
      assignedSkills: [],
      selectedHomeTeams: [],
      selectedAssignedTeams: [],
      selectedPrimarySkills: [],
      selectedAssignedSkills: [],
      startTime: '0;1440',
      employeeName: '',
      sliderOptions: {
        from: 0,
        to: 1440,
        step: 30,
        dimension: '',
        scale: ['0', '|', '|', '|', '|', '|', '3', '|', '|', '|', '|', '|', '6',
          '|', '|', '|', '|', '|', '9', '|', '|', '|', '|', '|', '12',
          '|', '|', '|', '|', '|', '15', '|', '|', '|', '|', '|', '18',
          '|', '|', '|', '|', '|', '21', '|', '|', '|', '|', '|', '24'],
        calculate: function(value) {
          var hours = Math.floor( value / 60 );
          var mins = ( value - hours*60 );
          return (hours < 10 ? "0"+hours : hours) + ":" + ( mins === 0 ? "00" : mins );
        },
        css: {
          pointer: { 'background-color': '#0e9ac9' },
          background: { 'background-color': '#899498' },
          range: { 'background-color': '#0e9ac9' }
        }
      },
      displayScheduledOnly: false
    };
    $scope.checkboxModel = {
      showHourlyRate: false
    };

    $scope.viewMode = {
      mode: null
    };

    $scope.selectedSchedule = null;
    $scope.scheduleInfoLoaded = null;
    $scope.summaryCollapsed = false;
    $scope.timeIntervalInMinutes = [{value: 0, width: '25%'}, {value: 15, width: '25%'}, {value: 30, width: '25%'}, {value: 45, width: '25%'}];
    $scope.headCountsInDayView = {};
    $scope.rowHeightsOfEmployeesInWeekView = {};
    $scope.openShiftsRowHeightInWeekView = null;

    $scope.onFilterChanged = function(needShiftsFilter) {
      $timeout(function() {
        angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employee) {
          var employeeName = employee[1] + ' ' + employee[2];
          if (employeeName.toLowerCase().indexOf($scope.filter.employeeName.toLowerCase()) < 0) {
            employee[9] = false;
            return;
          }
          if (_.findIndex($scope.filter.homeTeams, 'id', employee[5]) > -1 &&
            _.findIndex($scope.filter.selectedHomeTeams, 'id', employee[5]) === -1) {
            employee[9] = false;
            return;
          }
          if (_.findIndex($scope.filter.primarySkills, 'id', employee[6]) > -1 &&
            _.findIndex($scope.filter.selectedPrimarySkills, 'id', employee[6]) === -1) {
            employee[9] = false;
            return;
          }
          employee[9] = true;
        });

        if (needShiftsFilter) {
          $scope.applyFilterToShifts();
        }
        $scope.calculateSummaryInfoForSelectedWeekOrDay();
      }, 0);
    };

    $scope.onFilterHomeTeamsChanged = function() {
      $scope.onFilterChanged(false);
    };

    $scope.onFilterAssignedTeamsChanged = function() {
      $scope.onFilterChanged(true);
    };

    $scope.onFilterPrimarySkillsChanged = function() {
      $scope.onFilterChanged(false);
    };

    $scope.onFilterAssignedSkillsChanged = function() {
      $scope.onFilterChanged(true);
    };

    $scope.onEmployeeNameChanged = function() {
      $scope.onFilterChanged(false);
    };

    $scope.$watch('filter.startTime', function() {
      if (typeof $scope.selectedSchedule !== 'undefined' && $scope.selectedSchedule !== null) {
        $scope.onFilterChanged(true);
      }
    });

    $scope.setViewMode = function(viewMode) {
      $scope.viewMode.mode = viewMode;
    };

    var initializeDayViewInfo = function(dateTimeStamp) {
      dateTimeStamp = parseInt(dateTimeStamp);
      var weekIndex = -1;
      var dayLabelKey = null;
      var dateVal = moment.tz(dateTimeStamp, $scope.selectedSchedule.siteInfo[2]).format('YYYY-MM-DD');

      weekIndex = _.findIndex($scope.selectedSchedule.weeks, function(weekIterator) {
        var tempKey = _.findKey(weekIterator.dayLabels, function(dayLabelIterator) {
          return (dayLabelIterator.date === dateVal);
        });
        if (typeof tempKey !== 'undefined') {
          dayLabelKey = tempKey;
        }
        return (typeof tempKey !== 'undefined');
      });

      if (weekIndex > -1) {
        $scope.selectWeek($scope.selectedSchedule.weeks[weekIndex]);
        $scope.selectDay($scope.selectedSchedule.weeks[weekIndex].dayLabels[dayLabelKey]);

        // initialize head counts
        var getAppropriateShiftsCountOfEmployee = function(employee, comparedDateTimeStamp) {
          if (typeof $scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employee[0]] === 'undefined') {
            return 0;
          }

          var foundIndex = _.findIndex($scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employee[0]]['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date], function(shiftIterator) {
            return (shiftIterator.start <= comparedDateTimeStamp && shiftIterator.end >= comparedDateTimeStamp);
          });
          if (foundIndex > -1) {
            return 1;
          }
          return 0;
        };

        var getAppropriateShiftsCountForMoment = function(dateTimeStamp) {
          var count = 0;
          angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employee) {
            var countRes = getAppropriateShiftsCountOfEmployee(employee, dateTimeStamp);
            count += countRes;
          });

          return count;
        };

        angular.forEach($scope.consts.calendarTimeLabels, function(timeLabel) {
          $scope.headCountsInDayView[timeLabel.label] = [];
          for (var i=0; i<4; i++) {
            var minutes;
            if (i === 0) {
              minutes = '00';
            } else {
              minutes = (15 * i).toString();
            }
            var momentTimeStr = timeLabel.value + ':' + minutes;
            var fullMomentStr = $scope.selectedSchedule.selectedWeek.selectedDay.date + ' ' + momentTimeStr;
            var dateTimeStamp = moment.tz(fullMomentStr, $scope.selectedSchedule.siteInfo[2]).unix() * 1000;
            var count = getAppropriateShiftsCountForMoment(dateTimeStamp);
            $scope.headCountsInDayView[timeLabel.label].push(count);
          }
        });
      }
    };

    $scope.getSchedule = function(scheduleId, dateTimeStamp, reloadFlg) {
      if ((typeof reloadFlg === 'undefined' || !reloadFlg) &&
        $scope.selectedSchedule !== null && $scope.selectedSchedule.id === scheduleId) {
        if ($scope.viewMode.mode === 'day') {
          initializeDayViewInfo(dateTimeStamp);
        }
        $scope.calculateSummaryInfoForSelectedWeekOrDay();
      } else {
        $scope.scheduleInfoLoaded = false;
        $scope.selectedSchedule = null;
        EmployeeSchedulesService.getSchedule(scheduleId).then(function(response) {
          $scope.initializeSchedule(response.data);
          if ($scope.viewMode.mode === 'day') {
            initializeDayViewInfo(dateTimeStamp);
          }
          $scope.calculateSummaryInfoForSelectedWeekOrDay();
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        }).finally(function() {
          $scope.scheduleInfoLoaded = true;
        });
      }
    };

    var reLoadSchedule = function() {
      if ($scope.viewMode.mode === 'week') {
        $scope.getSchedule($scope.selectedSchedule.id, null, true);
      } else if ($scope.viewMode.mode === 'day') {
        var dateTimeStamp = moment.tz($scope.selectedSchedule.selectedWeek.selectedDay.date, $scope.selectedSchedule.siteInfo[2]).unix() * 1000;
        $scope.getSchedule($scope.selectedSchedule.id, dateTimeStamp, true);
      }
    };

    $scope.goToWeekViewFromSelectedDay = function() {
      $state.go('authenticated.employeeSchedules.weekView.schedule', {scheduleId: $scope.selectedSchedule.id});
    };

    $scope.goToDayViewFromSelectedWeek = function(dayLabel) {
      var dateTimeStamp = moment.tz(dayLabel.date, $scope.selectedSchedule.siteInfo[2]).unix() * 1000;
      $state.go('authenticated.employeeSchedules.dayView.schedule', {scheduleId: $scope.selectedSchedule.id, dateTimeStamp: dateTimeStamp});
    };

    $scope.goToPreviousDay = function() {
      var currentDateTimeStamp = moment.tz($scope.selectedSchedule.selectedWeek.selectedDay.date, $scope.selectedSchedule.siteInfo[2]).unix() * 1000;
      var dayInMilliseconds = 24 * 3600000;
      var dateTimeStamp = currentDateTimeStamp - dayInMilliseconds;
      $state.go('authenticated.employeeSchedules.dayView.schedule', {scheduleId: $scope.selectedSchedule.id, dateTimeStamp: dateTimeStamp});
    };

    $scope.goToNextDay = function() {
      var currentDateTimeStamp = moment.tz($scope.selectedSchedule.selectedWeek.selectedDay.date, $scope.selectedSchedule.siteInfo[2]).unix() * 1000;
      var dayInMilliseconds = 24 * 3600000;
      var dateTimeStamp = currentDateTimeStamp + dayInMilliseconds;
      $state.go('authenticated.employeeSchedules.dayView.schedule', {scheduleId: $scope.selectedSchedule.id, dateTimeStamp: dateTimeStamp});
    };

    $scope.selectWeek = function(week) {
      angular.forEach($scope.selectedSchedule.weeks, function(weekIterator) {
        weekIterator.selected = false;
      });

      week.selected = true;
      $scope.selectedSchedule.selectedWeek = week;
    };

    $scope.selectDay = function(dayLabel) {
      angular.forEach($scope.selectedSchedule.selectedWeek.dayLabels, function(dayIterator) {
        dayIterator.selected = false;
      });

      dayLabel.selected = true;
      $scope.selectedSchedule.selectedWeek.selectedDay = dayLabel;
    };

    function comparator(firstElement, secondElement) {
      var firstValue = null;
      var secondValue = null;

      if ($scope.sort.field === 'employeeName') {
        firstValue = firstElement[1];
        secondValue = secondElement[1];
      } else if ($scope.sort.field === 'serviceYears') {
        firstValue = firstElement[8];
        secondValue = secondElement[8];
      } else if ($scope.sort.field === 'rate') {
        firstValue = firstElement[3];
        secondValue = secondElement[3];
      } else if ($scope.sort.field === 'homeTeam') {
        firstValue = $scope.getHomeTeamName(firstElement[5]);
        if (typeof firstValue === 'undefined') {
          firstValue = firstElement[5];
        }
        secondValue = $scope.getHomeTeamName(secondElement[5]);
        if (typeof secondValue === 'undefined') {
          secondValue = secondElement[5];
        }
      } else if ($scope.sort.field === 'primarySkill') {
        firstValue = $scope.getPrimarySkillName(firstElement[6]);
        if (typeof firstValue === 'undefined') {
          firstValue = firstElement[6];
        }
        secondValue = $scope.getPrimarySkillName(secondElement[6]);
        if (typeof secondValue === 'undefined') {
          secondValue = secondElement[6];
        }
      }

      if (firstValue < secondValue) {
        if ($scope.sort.order === 'asc') {
          return -1;
        } else {
          return 1;
        }
      } else if (firstValue > secondValue) {
        if ($scope.sort.order === 'asc') {
          return 1;
        } else {
          return -1;
        }
      } else {
        return 0;
      }
    }

    $scope.toggleSort = function(field, order) {
      if (typeof order === 'undefined' || order === null) {
        if ($scope.sort.field === field) {
          if ($scope.sort.order === 'asc') {
            $scope.sort.order = 'dec';
          } else if ($scope.sort.order === 'dec') {
            $scope.sort.order = 'asc';
          } else {
            $scope.sort.order = 'asc';
          }
        } else {
          $scope.sort.field = field;
          $scope.sort.order = 'asc';
        }
      } else {
        $scope.sort.field = field;
        $scope.sort.order = order;
      }

      $scope.selectedSchedule.employeesInfo.result.sort(comparator);
    };

    $scope.getHomeTeamName = function(teamId) {
      return _.result(_.find($scope.filter.homeTeams, 'id', teamId), 'name');
    };

    $scope.getAssignedTeamName = function(teamId) {
      return _.result(_.find($scope.filter.assignedTeams, 'id', teamId), 'name');
    };

    $scope.getPrimarySkillName = function(skillId) {
      return _.result(_.find($scope.filter.primarySkills, 'id', skillId), 'name');
    };

    $scope.getAssignedSkillName = function(skillId) {
      return _.result(_.find($scope.filter.assignedSkills, 'id', skillId), 'name');
    };

    $scope.getEmployeeTeamNamesList = function(employee) {
      var listStr = '';
      angular.forEach(employee[4].split(','), function(teamId) {
        if (teamId === employee[5]) {
          listStr += $scope.getHomeTeamName(teamId) + ', ';
        } else {
          listStr += $scope.getAssignedTeamName(teamId) + ', ';
        }
      });
      listStr = listStr.substr(0, listStr.length - 2);

      return listStr;
    };

    $scope.getEmployeeSkillNamesList = function(employee) {
      var listStr = '';
      angular.forEach(employee[7].split(','), function(skillId) {
        if (skillId === employee[6]) {
          listStr += $scope.getPrimarySkillName(skillId) + ', ';
        } else {
          listStr += $scope.getAssignedSkillName(skillId) + ', ';
        }
      });
      listStr = listStr.substr(0, listStr.length - 2);

      return listStr;
    };

    $scope.applyFilterToShift = function(shift) {
      var filterTimeFrom = parseInt($scope.filter.startTime.substring(0, $scope.filter.startTime.indexOf(';')));
      var filterTimeTo = parseInt($scope.filter.startTime.substring($scope.filter.startTime.indexOf(';') + 1));
      var shiftStartDateTime = moment.tz(shift.start, $scope.selectedSchedule.siteInfo[2]);
      var shiftStartTimeInMinutes = parseInt(shiftStartDateTime.format('H')) * 60 + parseInt(shiftStartDateTime.format('m'));

      if (_.findIndex($scope.filter.assignedTeams, 'id', shift.teamId) > -1 &&
        _.findIndex($scope.filter.selectedAssignedTeams, 'id', shift.teamId) === -1) {
        shift.filterPassed = false;
        return;
      }
      if (_.findIndex($scope.filter.assignedSkills, 'id', shift.skillId) > -1 &&
        _.findIndex($scope.filter.selectedAssignedSkills, 'id', shift.skillId) === -1) {
        shift.filterPassed = false;
        return;
      }

      if (shiftStartTimeInMinutes < filterTimeFrom || shiftStartTimeInMinutes > filterTimeTo) {
        shift.filterPassed = false;
        return;
      }
      shift.filterPassed = true;
    };

    $scope.applyFilterToShifts = function() {
      var maxShiftsCountPerDay = 0;
      var heightVal = 0;

      angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employee) {
        maxShiftsCountPerDay = 0;
        angular.forEach($scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employee[0]], function(dateShifts, calendarDate) {
          angular.forEach(dateShifts, function(shift) {
            $scope.applyFilterToShift(shift);
          });
          if (maxShiftsCountPerDay < dateShifts.length) {
            maxShiftsCountPerDay = dateShifts.length;
          }
        });
        heightVal = 54 * Math.max(maxShiftsCountPerDay, 1);
        $scope.rowHeightsOfEmployeesInWeekView[employee[0]] = heightVal + 'px';
      });

      maxShiftsCountPerDay = 0;
      angular.forEach($scope.selectedSchedule.selectedWeek.shifts.openShifts, function(dateShifts, calendarDate) {
        angular.forEach(dateShifts, function(shift) {
          $scope.applyFilterToShift(shift);
        });
        if (maxShiftsCountPerDay < dateShifts.length) {
          maxShiftsCountPerDay = dateShifts.length;
        }
      });
      heightVal = 54 * Math.max(maxShiftsCountPerDay, 1);
      $scope.openShiftsRowHeightInWeekView = heightVal + 'px';
    };

    function numberStringWithCommas(x) {
      return x.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    $scope.calculateSummaryInfoForSelectedWeekOrDay = function() {
      var totalHours = 0;
      var totalCost = 0;
      var totalUnfilled = 0;
      var oneHourInMilliseconds = 3600000;

      if ($scope.selectedSchedule === null) {
        return;
      }

      angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employee) {
          /* FIXME parse employee array to object and use named object fields, e.g. 'employeeId' instead of "employee[0]"
           * Issue is global and related not only to current method but for the whole UI.
           * It is not possible to understand what information is behind the 'employee[9]' without backend.
           */
        if (employee[9]) {
          var hoursOfEmployee = 0;
          if ($scope.viewMode.mode === 'week') {
            angular.forEach($scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employee[0]], function(dateShifts, calendarDate) {
              angular.forEach(dateShifts, function(shift) {
                if (shift.filterPassed) {
                  var shiftDurationInHours = (shift.end - shift.start)/oneHourInMilliseconds;
                  totalHours += shiftDurationInHours;
                  hoursOfEmployee += shiftDurationInHours;
                }
              });
            });
          } else if ($scope.viewMode.mode === 'day') {
            if (typeof $scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employee[0]] !== 'undefined') {
              angular.forEach($scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employee[0]]['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date], function(shift) {
                if (shift.filterPassed) {
                  var shiftDurationInHours = (shift.end - shift.start)/oneHourInMilliseconds;
                  totalHours += shiftDurationInHours;
                  hoursOfEmployee += shiftDurationInHours;
                }
              });
            }
          }
          totalCost += employee[3] * hoursOfEmployee;
        }
      });
      if ($scope.viewMode.mode === 'week') {
        angular.forEach($scope.selectedSchedule.selectedWeek.shifts.openShifts, function(dateShifts) {
          angular.forEach(dateShifts, function(shift) {
            if (shift.filterPassed) {
              var shiftDurationInHours = (shift.end - shift.start)/oneHourInMilliseconds;
              totalHours += shiftDurationInHours;
              totalUnfilled++;
            }
          });
        });
        $scope.selectedSchedule.selectedWeek.hours = numberStringWithCommas(totalHours.toFixed(2));
        $scope.selectedSchedule.selectedWeek.cost = '$' + numberStringWithCommas(totalCost.toFixed(2));
        $scope.selectedSchedule.selectedWeek.unfilled = numberStringWithCommas(totalUnfilled.toString());
      } else if ($scope.viewMode.mode === 'day') {
        if (typeof $scope.selectedSchedule.selectedWeek.shifts.openShifts !== 'undefined' && $scope.selectedSchedule.selectedWeek.shifts.openShifts !== null) {
          angular.forEach($scope.selectedSchedule.selectedWeek.shifts.openShifts['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date], function(shift) {
            if (shift.filterPassed) {
              var shiftDurationInHours = (shift.end - shift.start)/oneHourInMilliseconds;
              totalHours += shiftDurationInHours;
              totalUnfilled++;
            }
          });
        }
        $scope.selectedSchedule.selectedWeek.selectedDay.hours = numberStringWithCommas(totalHours.toFixed(2));
        $scope.selectedSchedule.selectedWeek.selectedDay.cost = '$' + numberStringWithCommas(totalCost.toFixed(2));
        $scope.selectedSchedule.selectedWeek.selectedDay.unfilled = numberStringWithCommas(totalUnfilled.toString());
      }
    };

    $scope.employeeHasShifts = function(employeeId) {
      var result = false;
      if ($scope.viewMode.mode === 'week') {
        var foundObject = _.find($scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employeeId], function(dateShifts, calendarDate) {
          return (dateShifts.length > 0);
        });
        result = (typeof foundObject !== 'undefined');
      } else {
        if (typeof $scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employeeId] !== 'undefined' &&
          typeof $scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employeeId]['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date] !== 'undefined') {
          result = ($scope.selectedSchedule.selectedWeek.shifts.employeeShifts[employeeId]['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date].length > 0);
        }
      }

      return result;
    };

    $scope.openShiftsExisting = function() {
      var result = false;
      if ($scope.viewMode.mode === 'week') {
        var foundObject = _.find($scope.selectedSchedule.selectedWeek.shifts.openShifts, function(dateShifts, calendarDate) {
          return (dateShifts.length > 0);
        });
        result = (typeof foundObject !== 'undefined');
      } else {
        if (typeof $scope.selectedSchedule.selectedWeek.selectedDay !== 'undefined' && $scope.selectedSchedule.selectedWeek.selectedDay !== null) {
          if (typeof $scope.selectedSchedule.selectedWeek.shifts.openShifts['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date] !== 'undefined') {
            result = ($scope.selectedSchedule.selectedWeek.shifts.openShifts['calendar-day-' + $scope.selectedSchedule.selectedWeek.selectedDay.date].length > 0);
          }
        }
      }

      return result;
    };

    $scope.checkIfPastSchedule = function() {
      var currentTime = new Date().getTime();
      var scheduleEndTime = $scope.selectedSchedule.endDate;
      if (scheduleEndTime < currentTime) {
        return true;
      }
      return false;
    };

    $scope.$watch('selectedSchedule.selectedWeek', function() {
      if ($scope.selectedSchedule === null) {
        return;
      }

      $timeout(function() {
        $scope.applyFilterToShifts();
        $scope.calculateSummaryInfoForSelectedWeekOrDay();
      }, 0);
    });

    $scope.$watch('selectedSchedule.selectedWeek.selectedDay', function() {
      if ($scope.selectedSchedule === null) {
        return;
      }

      $timeout(function() {
        $scope.calculateSummaryInfoForSelectedWeekOrDay();
      }, 0);
    });

    $scope.toggleSummary = function() {
      $scope.summaryCollapsed = !$scope.summaryCollapsed;
      if ($scope.summaryCollapsed) {
        angular.element('.schedule-calendar .employee-info.schedule-calendar-cell').css('width', '440px');
      } else {
        angular.element('.schedule-calendar .employee-info.schedule-calendar-cell').css('width', '210px');
      }
    };

    $scope.initializeWeekLabels = function() {
      angular.forEach($scope.selectedSchedule.weeks, function(week) {
        var dayInMilliseconds = 24 * 3600000;
        week.selected = false;
        week.label = moment.tz(week.start, $scope.selectedSchedule.siteInfo[2]).format('MMMM DD') +
          ' to ' + moment.tz(week.end, $scope.selectedSchedule.siteInfo[2]).format('MMMM DD');
        week.dayLabels = {};

        for (var i=0; i<7; i++) {
          var momentObj = moment.tz(week.start + i * dayInMilliseconds, $scope.selectedSchedule.siteInfo[2]);
          var titleText = momentObj.format('MMM DD');
          var fullStrText = momentObj.format('MMMM DD, YYYY');
          var labelText = $scope.consts.daysOfWeek[momentObj.day()] + ' ' + momentObj.format('MM/DD');
          var dateText = momentObj.format('YYYY-MM-DD');
          week.dayLabels['calendar-day-' + dateText] = {title: titleText, fullStr: fullStrText, label: labelText, date: dateText, selected: false};
        }
      });
    };

    $scope.arrangeShifts = function() {
      var dayInMilliseconds = 24 * 3600000;
      angular.forEach($scope.selectedSchedule.weeks, function(week) {
        var arrangedShifts = {
          employeeShifts: {},
          openShifts: {}
        };
        angular.forEach(week.shifts, function(shift) {
          var dateStr = moment.tz(shift[6], $scope.selectedSchedule.siteInfo[2]).format('YYYY-MM-DD');
          var startTimeStr = '';
          var endTimeStr = '';
          var tempStr = '';
          var startTimeMoment = moment.tz(shift[6], $scope.selectedSchedule.siteInfo[2]);
          var endTimeMoment = moment.tz(shift[7], $scope.selectedSchedule.siteInfo[2]);

          if (startTimeMoment.minutes() > 0) {
            tempStr = startTimeMoment.format('h:ma');
          } else {
            tempStr = startTimeMoment.format('ha');
          }
          startTimeStr = tempStr.substr(0, tempStr.length - 1);
          if (endTimeMoment.minutes() > 0) {
            tempStr = endTimeMoment.format('h:ma');
          } else {
            tempStr = endTimeMoment.format('ha');
          }
          endTimeStr = tempStr.substr(0, tempStr.length - 1);
          var timeStr = startTimeStr + '-' + endTimeStr;
          var startTimeOffSetPercentInDay = (((startTimeMoment.hours() * 3600 + startTimeMoment.minutes() * 60 + startTimeMoment.seconds()) * 1000 + startTimeMoment.milliseconds())/dayInMilliseconds) * 100;
          startTimeOffSetPercentInDay += '%';
          var lengthPercentInDay = ((shift[7] - shift[6])/dayInMilliseconds) * 100;
          lengthPercentInDay += '%';

          var shiftObj = {
            id: shift[0],
            type: 'normal',
            excessType: (shift[1])? 'extra': 'regular',
            excessTypeStr: (shift[1])? 'Extra': '',
            start: shift[6],
            end: shift[7],
            timeStr: timeStr,
            startTimeOffSetPercentInDay: startTimeOffSetPercentInDay,
            lengthPercentInDay: lengthPercentInDay,
            skillId: shift[4],
            skillName: $scope.getAssignedSkillName(shift[4]),
            skillAbbrev: shift[5],
            assignment: 'Assignment',
            teamId: shift[2],
            teamName: $scope.getAssignedTeamName(shift[2]),
            posted: '',
            requested: '',
            comment: shift[9]
          };
          if (shiftObj.comment === null) {
            shiftObj.commentClass = '';
          } else {
            shiftObj.commentClass = 'has-comment';
          }

          if (typeof shift[3] !== 'undefined' && shift[3] !== null) {
            if (typeof arrangedShifts.employeeShifts[shift[3]] === 'undefined') {
              arrangedShifts.employeeShifts[shift[3]] = {};
            }
            if (typeof arrangedShifts.employeeShifts[shift[3]]['calendar-day-' + dateStr] === 'undefined') {
              arrangedShifts.employeeShifts[shift[3]]['calendar-day-' + dateStr] = [];
            }
            arrangedShifts.employeeShifts[shift[3]]['calendar-day-' + dateStr].push(shiftObj);
          } else {
            if (typeof arrangedShifts.openShifts['calendar-day-' + dateStr] === 'undefined') {
              arrangedShifts.openShifts['calendar-day-' + dateStr] = [];
            }
            shiftObj.type = 'open';
            var foundShift = _.find(week.postedOpenShifts, function(postedIterator) {
              return (postedIterator[0] === shiftObj.id);
            });
            if (typeof foundShift !== 'undefined') {
              shiftObj.posted = 'Posted ' + moment.tz(foundShift[1], $scope.selectedSchedule.siteInfo[2]).format('YYYY-MM-DD');
              shiftObj.requested = (foundShift[2])? '(R)': '';
            }
            arrangedShifts.openShifts['calendar-day-' + dateStr].push(shiftObj);
          }
        });
        week.shifts = arrangedShifts;
        angular.forEach(week.dayLabels, function(dateLabel, calendarDate) {
          if (typeof week.shifts.openShifts[calendarDate] !== 'undefined' &&
            week.shifts.openShifts[calendarDate] !==null ) {
            dateLabel.openShiftsCount = week.shifts.openShifts[calendarDate].length;
          } else {
            dateLabel.openShiftsCount = 0;
          }
        });
      });
    };

    $scope.arrangeEmployeeHours = function() {
      angular.forEach($scope.selectedSchedule.weeks, function(week) {
        var arrangedEmployeeHours = {};
        angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employee) {
          var foundInfo = _.find(week.employees, 'employeeId', employee[0]);
          if (typeof foundInfo !== 'undefined') {
            var minimumHours = (foundInfo.minimumMinutes/60).toFixed(1);
            var scheduledHours = (foundInfo.scheduledMinutes/60).toFixed(1);
            var alarmed = (minimumHours > scheduledHours);
            arrangedEmployeeHours[employee[0]] = {
              minimumHours: minimumHours,
              scheduledHours: scheduledHours,
              hoursAlarmed: alarmed
            };
          } else {
            arrangedEmployeeHours[employee[0]] = {
              minimumHours: '-',
              scheduledHours: '-',
              hoursAlarmed: false
            };
          }
        });
        week.employees = arrangedEmployeeHours;
      });
    };

    $scope.getDisplayedEmployeesCount = function() {
      var number = 0;
      angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employee) {
        if (employee[9] && (!$scope.filter.displayScheduledOnly || $scope.employeeHasShifts(employee[0]))) {
          number++;
        }
      });
      return number;
    };

    $scope.initializeSchedule = function(responseData) {
      $scope.selectedSchedule = responseData;
      var siteTimeZone = $scope.selectedSchedule.siteInfo[2];
      $scope.selectedSchedule.startDateStr = moment.tz($scope.selectedSchedule.startDate, siteTimeZone).format('MMMM DD, YYYY');
      $scope.selectedSchedule.lengthInWeeks = $scope.selectedSchedule.scheduleLengthInDays/7;
      $scope.selectedSchedule.isDeletable = function () {
        //Show "Delete" button if schedule is not actual yet.
        if ($scope.selectedSchedule.status == $scope.consts.scheduleStatus.POSTED) {
          var scheduleStartDateTime = appFunc.convertToBrowserTimezone(
              $scope.selectedSchedule.startDate, siteTimeZone);
          return scheduleStartDateTime > new Date();
        }
        return true;
      }();

      var selectedScheduleTotalHours = $scope.selectedSchedule.totalMinutes/60;
      $scope.selectedSchedule.totalHours = numberStringWithCommas(selectedScheduleTotalHours.toFixed(2));
      $scope.selectedSchedule.overtimeHours = numberStringWithCommas($scope.selectedSchedule.overtimeHours.toFixed(2));
      $scope.selectedSchedule.totalCost = '$' + numberStringWithCommas($scope.selectedSchedule.totalCost.toFixed(2));
      $scope.selectedSchedule.unfilledShiftCount = numberStringWithCommas($scope.selectedSchedule.unfilledShiftCount.toString());
      $scope.filter.homeTeams = [];
      $scope.filter.assignedTeams = [];
      $scope.filter.primarySkills = [];
      $scope.filter.assignedSkills = [];
      $scope.filter.selectedHomeTeams = [];
      $scope.filter.selectedAssignedTeams = [];
      $scope.filter.selectedPrimarySkills = [];
      $scope.filter.selectedAssignedSkills = [];

      angular.forEach($scope.selectedSchedule.teamsInfo.result, function(teamInfo) {
        var info = {
          id: teamInfo[0],
          name: teamInfo[1],
          ticked: true
        };
        $scope.filter.homeTeams.push(info);
      });
      $scope.filter.homeTeams.sort(function(firstTeam, secondTeam) {
        if (firstTeam.name < secondTeam.name) {
          return -1;
        } else if (firstTeam.name > secondTeam.name) {
          return 1;
        } else {
          return 0;
        }
      });
      $scope.filter.assignedTeams = angular.copy($scope.filter.homeTeams);

      angular.forEach($scope.selectedSchedule.skillsInfo.result, function(skillInfo) {
        var info = {
          id: skillInfo[0],
          name: skillInfo[1],
          ticked: true
        };
        $scope.filter.primarySkills.push(info);
      });
      $scope.filter.primarySkills.sort(function(firstSkill, secondSkill) {
        if (firstSkill.name < secondSkill.name) {
          return -1;
        } else if (firstSkill.name > secondSkill.name) {
          return 1;
        } else {
          return 0;
        }
      });
      $scope.filter.assignedSkills = angular.copy($scope.filter.primarySkills);

      angular.forEach($scope.selectedSchedule.employeesInfo.result, function(employeeInfo) {
        employeeInfo[3] = employeeInfo[3].toFixed(2); // hourly rate
        employeeInfo.push(true); // Add visible property to employee

        if (typeof employeeInfo[8] !== 'number') {
          employeeInfo[8] = -1;
          employeeInfo[10] = '-';
        } else {
          var abstractEndDate = moment.tz(employeeInfo[8], $scope.selectedSchedule.siteInfo[2]);
          var abstractStartDate = moment.tz(0, $scope.selectedSchedule.siteInfo[2]);
          var monthsDiff = abstractEndDate.diff(abstractStartDate, 'months');
          var yearsDiff = Math.floor(monthsDiff/12);
          monthsDiff = monthsDiff - 12 * yearsDiff;

          var yearsDiffStr = '', monthsDiffStr = '';
          if (yearsDiff !== 0) {
            yearsDiffStr = yearsDiff + 'y ';
          }
          if (monthsDiff !== 0) {
            monthsDiffStr = monthsDiff + 'm';
          }
          if (yearsDiff === 0 && monthsDiff === 0) {
            employeeInfo[10] = '0';
          } else {
            employeeInfo[10] = yearsDiffStr + monthsDiffStr;
          }
        }
      });

      $scope.toggleSort('employeeName', 'asc');
      $scope.initializeWeekLabels();
      $scope.arrangeShifts();
      $scope.arrangeEmployeeHours();
      $scope.selectedSchedule.weeks[0].selected = true;
      $scope.selectedSchedule.selectedWeek = $scope.selectedSchedule.weeks[0];
    };

    $scope.promoteSchedule = function() {
      var modalInstance = $modal.open({
        templateUrl: 'promoteScheduleConfirmationModal.html',
        controller: 'PromoteScheduleConfirmationModalInstanceCtrl',
        size: 'sm',
        windowClass: 'promote-schedule-modal'
      });
      modalInstance.result.then(function(result) {
        if (typeof result !== 'undefined' && result !== null && result.answer) {
          dataService.promoteSchedule($scope.selectedSchedule.id).then(function(response) {
            applicationContext.setNotificationMsgWithValues('employee_schedules.SCHEDULE_PROMOTED_SUCCESSFULLY', 'success', true);
            reLoadSchedule();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
        }
      });
    };

    $scope.deleteSchedule = function() {
      dataService.deleteSchedule($scope.selectedSchedule.id).then(function(response) {
        applicationContext.setNotificationMsgWithValues('employee_schedules.SCHEDULE_DELETED_SUCCESSFULLY', 'success', true);
        $scope.selectedSchedule = null;
        $scope.scheduleInfoLoaded = null;
        $scope.headCountsInDayView = {};
        $scope.rowHeightsOfEmployeesInWeekView = {};
        $scope.openShiftsRowHeightInWeekView = null;
        $state.go('authenticated.employeeSchedules.weekView', null, {location: true});
      }, function(err) {
        var errorMessage = err.data ? err.data.message : "Schedule delete failed";
        applicationContext.setNotificationMsgWithValues(JSON.stringify(errorMessage), 'danger', true);
      });
    };

    $scope.openSiteSchedulesModal = function() {
      var modalInstance = $modal.open({
        templateUrl: 'scheduleSelectorModal.html',
        controller: 'ScheduleSelectorModalInstanceCtrl',
        size: 'lg',
        windowClass: 'schedules-modal',
        resolve: {
          checkboxModel: function() {
            return {
              simulationGenerated: false,
              production: true,
              posted: true
            };
          },
          customFilter: function() {
            return null;
          }
        }
      });
      modalInstance.result.then(function(selectedSchedule) {
        if ($scope.viewMode.mode === 'week') {
          $state.go('authenticated.employeeSchedules.weekView.schedule', {scheduleId: selectedSchedule.id});
        } else if ($scope.viewMode.mode === 'day') {
          $state.go('authenticated.employeeSchedules.dayView.schedule', {scheduleId: selectedSchedule.id, dateTimeStamp: selectedSchedule.start});
        }
      });
    };

    $scope.openManageShiftModal = function(shift, employee) {
      var modalInstance = $modal.open({
        templateUrl: 'manageScheduleShiftPopupModal.html',
        controller: 'ManageScheduleShiftPopupModalInstanceCtrl',
        windowClass: 'manage-shift-modal',
        resolve: {
          selectedSchedule: function() {
            return $scope.selectedSchedule;
          },
          selectedShift: function() {
            return shift;
          },
          selectedEmployee: function() {
            return employee;
          }
        }
      });
      modalInstance.result.then(function(modalResult) {
        if (modalResult.operation === 'drop') {
          dataService.dropShift($scope.selectedSchedule.id, shift.id, modalResult.dropShiftReasonId).then(function(response) {
            applicationContext.setNotificationMsgWithValues('employee_schedules.SHIFT_DROPPED_SUCCESSFULLY', 'success', true);
            reLoadSchedule();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
        } else if (modalResult.operation === 'swap') {
          dataService.submitSelectedEligibleEntities('swap', $scope.selectedSchedule.id, shift, modalResult.newSelectedShift, modalResult.comment).then(function(response) {
            applicationContext.setNotificationMsgWithValues('employee_schedules.SWAP_REQUEST_SENT_SUCCESSFULLY', 'success', true);
            reLoadSchedule();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
        } else if (modalResult.operation === 'update') {
          var shiftInfo = {
            newStartDateTime: modalResult.startTime,
            newEndDateTime: modalResult.endTime
          };
          var osShiftInfo = null;

          if (typeof modalResult.spaceOperation !== 'undefined') {
            osShiftInfo = {
              action: modalResult.spaceOperation,
              startDateTime: modalResult.newShiftStartTime,
              endDateTime: modalResult.newShiftEndTime
            };

            if (modalResult.spaceOperation === 'CreateAndAssign') {
              osShiftInfo.employeeId = modalResult.newFillEmployee;
            }

            if (typeof modalResult.ptoType !== 'undefined') {
              osShiftInfo.unavailabilityInfo = {
                absenceTypeId: modalResult.ptoType
              };
            }
          }

          dataService.manageShift($scope.selectedSchedule.id, shift.id, modalResult.comment, shiftInfo, osShiftInfo).then(function(response) {
            applicationContext.setNotificationMsgWithValues('employee_schedules.SHIFT_UPDATED_SUCCESSFULLY', 'success', true);
            reLoadSchedule();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
        }
      });
    };

    $scope.openManageOpenShiftsModal = function(date) {
      var modalInstance = $modal.open({
        templateUrl: 'manageOpenShiftsPopupModal.html',
        controller: 'ManageOpenShiftsPopupModalInstanceCtrl',
        size: 'lg',
        backdrop: false,
        windowClass: 'manage-open-shifts-modal',
        resolve: {
          siteId: function() {
            return $scope.selectedSchedule.siteInfo[0];
          },
          scheduleId: function() {
            return $scope.selectedSchedule.id;
          },
          dateRangeStart: function() {
            if (typeof date === 'undefined' || date === null) {
              return $scope.selectedSchedule.selectedWeek.start;
            } else {
              return moment.tz(date + ' 00:00', $scope.selectedSchedule.siteInfo[2]).unix() * 1000;
            }
          },
          dateRangeEnd: function() {
            if (typeof date === 'undefined' || date === null) {
              return $scope.selectedSchedule.selectedWeek.end - 1;
            } else {
              var dayInMilliseconds = 24 * 3600000;
              return moment.tz(date + ' 00:00', $scope.selectedSchedule.siteInfo[2]).unix() * 1000 + dayInMilliseconds - 1;
            }
          },
          timezone: function() {
            return $scope.selectedSchedule.siteInfo[2];
          }
        }
      });
      modalInstance.result.then(function() {
        //reLoadSchedule();
      });
    };

    $scope.openAddNewShiftModal = function() {
      var modalInstance = $modal.open({
        templateUrl: 'addNewShiftPopupModal.html',
        controller: 'AddNewShiftPopupModalInstanceCtrl',
        size: 'lg',
        windowClass: 'add-new-shift-modal',
        resolve: {
          viewMode: function() {
            return $scope.viewMode.mode;
          },
          selectedSchedule: function() {
            return $scope.selectedSchedule;
          }
        }
      });
      modalInstance.result.then(function(modalResult) {
        dataService.createShiftByAction($scope.selectedSchedule.id, modalResult.shiftInfo, modalResult.action, modalResult.employeeId).then(function(response) {
          applicationContext.setNotificationMsgWithValues('employee_schedules.SHIFT_CREATED_SUCCESSFULLY', 'success', true);
          reLoadSchedule();
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
        });
      });
    };

    $scope.openFillShiftModal = function(shift) {
      var modalInstance = $modal.open({
        templateUrl: 'fillShiftPopupModal.html',
        controller: 'FillShiftPopupModalInstanceCtrl',
        windowClass: 'fill-shift-modal',
        resolve: {
          selectedSchedule: function() {
            return $scope.selectedSchedule;
          },
          selectedOpenShift: function() {
            return shift;
          }
        }
      });
      modalInstance.result.then(function(modalResult) {
        if (modalResult.action === 'PostShift') {
          var openShifts = {};
          openShifts[modalResult.shiftId] = [modalResult.employeeId];

          var payLoad = {
            comments: '',
            terms: null,
            deadline: null,
            overrideOptions: null,
            openShifts: openShifts
          };

          EmployeeSchedulesService.postOpenShifts($scope.selectedSchedule.id, payLoad).then(function(response) {
            applicationContext.setNotificationMsgWithValues('employee_schedules.SHIFT_POSTED_SUCCESSFULLY', 'success', true);
            reLoadSchedule();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
        } else if (modalResult.action === 'FillShift') {
          if (typeof modalResult.employeeId !== 'undefined' && modalResult.employeeId !== null) {
            dataService.assignShiftToEmployee($scope.selectedSchedule.id, modalResult.shiftId, modalResult.employeeId).then(function(response) {
              applicationContext.setNotificationMsgWithValues('employee_schedules.SHIFT_FILLED_SUCCESSFULLY', 'success', true);
              reLoadSchedule();
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
            });
          }
        } else if (modalResult.action === 'deleteShift') {
          reLoadSchedule();
        }
      });
    };

    $scope.openAssignShiftsModal = function(selectedEmployee) {
      var modalInstance = $modal.open({
        templateUrl: 'assignShiftsPopupModal.html',
        controller: 'AssignShiftsPopupModalInstanceCtrl',
        size: 'lg',
        windowClass: 'assign-shifts-modal',
        resolve: {
          selectedSchedule: function() {
            return $scope.selectedSchedule;
          },
          selectedEmployee: function() {
            return selectedEmployee;
          }
        }
      });
      modalInstance.result.then(function(modalResult) {
        if (modalResult.selectedOpenShiftId !== null) {
          dataService.assignShiftToEmployee($scope.selectedSchedule.id, modalResult.selectedOpenShiftId, selectedEmployee[0]).then(function(response) {
            applicationContext.setNotificationMsgWithValues('employee_schedules.SHIFT_ASSIGNED_SUCCESSFULLY', 'success', true);
            reLoadSchedule();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          });
        }
      });
    };

    $scope.openOverrides = function () {
      $modal.open({
        templateUrl: 'modules/common/partials/schedule-settings-overrides.html',
        controller: 'ScheduleBuilderCreateSettingsCtrl',
        windowClass: 'overrides-modal',
        resolve: {
          selectedSchedule: function() {
            return $scope.selectedSchedule;
          }
        }
      });
    };

    $scope.hasMgmtPermission = function() {
      return authService.hasPermission('Tenant_Mgmt');
    };

    $scope.hasViewPermission = function () {
      return authService.hasPermission('Tenant_View');
    };
  }
]);

angular.module('emlogis.employeeSchedules').controller('ManageOpenShiftsPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', '$filter', 'applicationContext', 'EmployeeSchedulesService', 'siteId', 'scheduleId', 'dateRangeStart', 'dateRangeEnd', 'timezone',
  function ($scope, $modalInstance, $timeout, $filter, applicationContext, EmployeeSchedulesService, siteId, scheduleId, dateRangeStart, dateRangeEnd, timezone) {

    $scope.data = {
      originalOpenShifts: [],
      openShifts: [],
      selectedOpenShifts: [],
      employees: [],
      selectedEmployees: [],
      comments: '',
      deadline: moment.tz([new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 7], timezone).unix() * 1000,
      terms: 'AutoApprove',
      postingRulesShown: false,
      messages: []
    };

    $scope.loadingState = {
      populatingOverrideOptions: true,
      populatingOpenShifts: true,
      loadingEligibilityData: false,
      postingOpenShifts: false
    };

    $scope.options = {
      firstRequestOnly: true,
      overrideOptions: {},
      allOverrideOptions: false
    };

    $scope.checkboxModel = {
      notPostedOnly: false,
      requested: false
    };

    $scope.datePickerModel = {
      postUntil: new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate() + 7),
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

    $scope.toggleAllOverrideOptions = function() {
      if ($scope.options.allOverrideOptions) {
        $scope.options.overrideOptions.TEAM_FLOAT_ON = true;
        $scope.options.overrideOptions.AVOID_OVERTIME = true;
        $scope.options.overrideOptions.ALL_DAY_UNAVAILABLE_OVERRIDE = true;
        $scope.options.overrideOptions.TIME_WINDOW_UNAVAILABLE_OVERRIDE = true;
        $scope.options.overrideOptions.MAX_DAYS_WEEK_OVERRIDE = true;
        $scope.options.overrideOptions.MAX_CONSECUTIVE_DAYS_OVERRIDE = true;
        $scope.options.overrideOptions.MAX_HOURS_WEEK_OVERRIDE = true;
        $scope.options.overrideOptions.MAX_HOURS_DAY_OVERRIDE = true;
        $scope.options.overrideOptions.MIN_HOURS_DAY_OVERRIDE = true;
      } else {
        $scope.options.overrideOptions.TEAM_FLOAT_ON = false;
        $scope.options.overrideOptions.AVOID_OVERTIME = false;
        $scope.options.overrideOptions.ALL_DAY_UNAVAILABLE_OVERRIDE = false;
        $scope.options.overrideOptions.TIME_WINDOW_UNAVAILABLE_OVERRIDE = false;
        $scope.options.overrideOptions.MAX_DAYS_WEEK_OVERRIDE = false;
        $scope.options.overrideOptions.MAX_CONSECUTIVE_DAYS_OVERRIDE = false;
        $scope.options.overrideOptions.MAX_HOURS_WEEK_OVERRIDE = false;
        $scope.options.overrideOptions.MAX_HOURS_DAY_OVERRIDE = false;
        $scope.options.overrideOptions.MIN_HOURS_DAY_OVERRIDE = false;
      }
    };

    $scope.parseDeadline = function() {
      var yearVal = $scope.datePickerModel.postUntil.getFullYear();
      var monthVal = $scope.datePickerModel.postUntil.getMonth();
      var dateVal = $scope.datePickerModel.postUntil.getDate();
      var dateInArrFormat = [yearVal, monthVal, dateVal];
      $scope.data.deadline = moment.tz(dateInArrFormat, timezone).unix() * 1000;
    };

    $scope.parseTerms = function() {
      if ($scope.options.firstRequestOnly) {
        $scope.data.terms = 'AutoApprove';
      } else {
        $scope.data.terms = null;
      }
    };

    $scope.togglePostingRulesPopup = function(employeeListUpdateNeeded) {
      if (employeeListUpdateNeeded) {
        $scope.getEligibilityDataFromSelectedEntities();
      }
      $scope.data.postingRulesShown = !$scope.data.postingRulesShown;
    };

    $scope.getOverrideOptions = function() {
      $scope.loadingState.populatingOverrideOptions = true;
      EmployeeSchedulesService.getOverrideOptions(siteId).then(function(response) {
        $scope.options.overrideOptions = response.data.overrideOptions;
      }, function(err) {
        $scope.data.messages.push({type: 'danger', msg: $filter('translate')('employee_schedules.OVERRIDE_OPTIONS_GET_ERROR')});
      }).finally(function() {
        $scope.loadingState.populatingOverrideOptions = false;
      });
    };

    $scope.updateOverrideOptions = function() {
      EmployeeSchedulesService.updateOverrideOptions(siteId, $scope.options.overrideOptions).then(function(overrideOptionsResponse) {
        $scope.data.messages.push({type: 'success', msg: $filter('translate')('employee_schedules.OVERRIDE_OPTIONS_UPDATED_SUCCESSFULLY')});
      }, function(overrideOptionsErr) {
        $scope.data.messages.push({type: 'danger', msg: $filter('translate')('employee_schedules.OVERRIDE_OPTIONS_UPDATED_ERROR')});
      });
    };

    $scope.openShiftsGridOptions = {
      data: 'data.openShifts',
      enableRowSelection: true,
      enableSelectAll: true,
      multiSelect: true,
      columnDefs: [
        { field: 'shiftId', visible: false },
        { field: 'shift', width: '200', enableSorting: false },
        { field: 'teamName', width: '100' },
        { field: 'skillName', width: '100' },
        { field: 'posted', width: '90',
          cellClass: function(grid, row, col, rowRenderIndex, colRenderIndex) {
            if (grid.getCellValue(row,col) !== '-') {
              return 'cell-bold';
            }
          }
        },
        { field: 'req', width: '70',
          cellClass: function(grid, row, col, rowRenderIndex, colRenderIndex) {
            if (grid.getCellValue(row,col).charAt(0) !== '-') {
              return 'cell-req-bold cell-bold';
            }
          }
        }
      ]
    };

    $scope.processSelectedRow = function(gridName, row) {
      if (row.isSelected) {
        if (gridName === 'openShifts') {
          $scope.data.selectedOpenShifts.push(row.entity);
          $scope.data.employees = [];
          $scope.data.selectedEmployees = [];
        } else {
          $scope.data.selectedEmployees.push(row.entity);
        }
      } else {
        if (gridName === 'openShifts') {
          $scope.data.selectedOpenShifts = _.filter($scope.data.selectedOpenShifts, function(entity){ return entity.shiftId !== row.entity.shiftId; });
          $scope.data.employees = [];
          $scope.data.selectedEmployees = [];
        } else {
          $scope.data.selectedEmployees = _.filter($scope.data.selectedEmployees, function(entity){ return entity.id !== row.entity.id; });
        }
      }
    };

    $scope.openShiftsGridOptions.onRegisterApi = function(gridApi) {
      gridApi.selection.on.rowSelectionChanged($scope, function (row) {
        $scope.processSelectedRow('openShifts', row);
      });

      gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows) {
        angular.forEach(rows, function (row) {
          $scope.processSelectedRow('openShifts', row);
        });
      });
    };

    function comparator(firstElement, secondElement) {
      var firstValue = firstElement.startDateTime;
      var secondValue = secondElement.startDateTime;

      if (firstValue < secondValue) {
        return -1;
      } else if (firstValue > secondValue) {
        return 1;
      } else {
        return 0;
      }
    }

    $scope.populateOpenShifts = function() {
      $scope.data.openShifts = [];
      $scope.data.selectedOpenShifts = [];
      $scope.data.employees = [];
      $scope.data.selectedEmployees = [];
      EmployeeSchedulesService.getOpenShifts(scheduleId, dateRangeStart, dateRangeEnd).then(function(response) {
        $scope.data.openShifts = response.data.result;
        angular.forEach($scope.data.openShifts, function(openShift) {
          var startMomentObj = moment.tz(openShift.startDateTime, timezone);
          var endMomentObj = moment.tz(openShift.endDateTime, timezone);
          var startTimeStr = '';
          var endTimeStr = '';
          if (startMomentObj.minutes() > 0) {
            startTimeStr = startMomentObj.format('hh:mma');
          } else {
            startTimeStr = startMomentObj.format('hha');
          }
          if (endMomentObj.minutes() > 0) {
            endTimeStr = endMomentObj.format('hh:mma');
          } else {
            endTimeStr = endMomentObj.format('hha');
          }
          var shiftStr = startMomentObj.format('MMMM DD, YYYY, ') + startTimeStr + ' - ' + endTimeStr;

          openShift.shift = shiftStr;
          openShift.posted = (typeof openShift.postId === 'undefined' || openShift.postId === null || openShift.postId === 0)? '-': moment.tz(openShift.postId, timezone).format('MM/DD/YYYY');
          var reqCountStr = (openShift.reqCount === 0)? '-': openShift.reqCount;
          var empCountStr = (openShift.empCount === 0)? '-': openShift.empCount;
          openShift.req = reqCountStr + '/' + empCountStr;
        });
        $scope.loadingState.populatingOpenShifts = false;
        $scope.data.openShifts.sort(comparator);
        $scope.data.originalOpenShifts = angular.copy($scope.data.openShifts);
        $timeout(function () {
          $('.open-shifts-grid').resize();

          // Make font bold for entire row which has req > 0
          angular.forEach($('.cell-req-bold'), function(reqCell) {
            $(reqCell).parent().css('font-weight', 900);
          });
        }, 0);
      }, function(err) {
        $scope.data.messages.push({type: 'danger', msg: $filter('translate')('employee_schedules.POPULATING_OPEN_SHIFTS_ERROR')});
      });
    };

    $scope.filterDisplayedShifts = function() {
      $scope.data.openShifts = [];
      $scope.data.selectedOpenShifts = [];
      $scope.data.employees = [];
      $scope.data.selectedEmployees = [];

      angular.forEach($scope.data.originalOpenShifts, function(openShift) {
        if ($scope.checkboxModel.notPostedOnly) {
          if (openShift.postId > 0) {
            return;
          }
        }
        if ($scope.checkboxModel.requested) {
          if (openShift.reqCount <=0) {
            return;
          }
        }
        $scope.data.openShifts.push(openShift);
      });
    };

    $scope.getEligibilityDataFromSelectedEntities = function() {
      $scope.data.employees = [];
      $scope.data.selectedEmployees = [];
      $scope.loadingState.loadingEligibilityData = true;
      EmployeeSchedulesService.getEligibilityDataFromSelectedEntities(scheduleId, null, _.map($scope.data.selectedOpenShifts, 'shiftId'), $scope.options.overrideOptions).then(function(response) {
        angular.forEach(response.data.openShifts, function(openShift) {
          var foundIndex = _.findIndex($scope.data.openShifts, 'shiftId', openShift.id);
          $scope.data.openShifts[foundIndex].eligibleEmployees = openShift.employees;

          angular.forEach(openShift.employees, function(employee) {
            if (_.findIndex($scope.data.employees, 'id', employee.id) < 0) {
              $scope.data.employees.push(employee);
            }
          });
        });
        $timeout(function () {
          $('.employees-grid').resize();
        }, 0);
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      }).finally(function() {
        $scope.loadingState.loadingEligibilityData = false;
      });
    };

    $scope.employeesGridOptions = {
      data: 'data.employees',
      enableRowSelection: true,
      enableSelectAll: true,
      multiSelect: true,
      columnDefs: [
        { field: 'id', visible: false},
        { field: 'name'},
        { field: 'homeTeamName'}
      ]
    };

    $scope.employeesGridOptions.onRegisterApi = function(gridApi) {
      gridApi.selection.on.rowSelectionChanged($scope, function (row) {
        $scope.processSelectedRow('employees', row);
      });

      gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows) {
        angular.forEach(rows, function (row) {
          $scope.processSelectedRow('employees', row);
        });
      });
    };

    $scope.postOpenShifts = function() {
      $scope.loadingState.postingOpenShifts = true;
      var openShifts = {};
      angular.forEach($scope.data.selectedOpenShifts, function(selectedOpenShift) {
        openShifts[selectedOpenShift.shiftId] = [];
        angular.forEach($scope.data.selectedEmployees, function(selectedEmployee) {
          if (_.findIndex(selectedOpenShift.eligibleEmployees, 'id', selectedEmployee.id) > -1) {
            openShifts[selectedOpenShift.shiftId].push(selectedEmployee.id);
          }
        });
        if (openShifts[selectedOpenShift.shiftId].length === 0) {
          delete openShifts[selectedOpenShift.shiftId];
        }
      });

      var payLoad = {
        comments: $scope.data.comments,
        terms: $scope.data.terms,
        deadline: $scope.data.deadline,
        overrideOptions: $scope.options.overrideOptions,
        openShifts: openShifts
      };

      EmployeeSchedulesService.postOpenShifts(scheduleId, payLoad).then(function(openShiftsResponse) {
        $scope.data.messages.push({type: 'success', msg: openShiftsResponse.data.result.length + ' ' + $filter('translate')('employee_schedules.OPEN_SHIFTS_POSTED_SUCCESSFULLY')});
      }, function(openShiftsErr) {
        $scope.data.messages.push({type: 'danger', msg: $filter('translate')('employee_schedules.OPEN_SHIFTS_POSTED_ERROR')});
      }).finally(function() {
        $scope.loadingState.postingOpenShifts = false;
        $scope.populateOpenShifts();
      });
    };

    $scope.closeMessage = function(index) {
      $scope.data.messages.splice(index, 1);
    };

    $scope.close = function () {
      $modalInstance.close();
    };

    $scope.getOverrideOptions();
    $scope.populateOpenShifts();
  }]);

angular.module('emlogis.employeeSchedules').controller('PromoteScheduleConfirmationModalInstanceCtrl', ['$scope', '$modalInstance',
  function ($scope, $modalInstance) {
    $scope.confirmToPromote = function() {
      $modalInstance.close({answer: true});
    };

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };
  }]);

angular.module('emlogis.employeeSchedules').directive('shiftTimeLine',
  function() {
    return {
      restrict: 'A',
      link: function (scope, elem, attrs) {
        scope.timeLineWidth = attrs.width;
        scope.maxHours = 48;

        scope.$on('event:updateShift', function (event, args) {
          init();
        });

        function init() {
          elem.resizable({handles: 'w,e', containment: 'parent', grid: [scope.data.step, 1], minWidth : 1});
        }

        elem.on('resizestop', function (evt, ui) {
          scope.$apply(function() {
            scope.$eval(attrs.firstpos + '=' + ui.position.left);
            scope.$eval(attrs.endpos + '=' + (ui.size.width + 2));
            scope.data.swapShift = false;
            scope.getWipEligibleEmployees();
          });
        });

        scope.initShiftEditForm();
        init();
      }
    };
  });

angular.module('emlogis.employeeSchedules').controller('ManageScheduleShiftPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', 'dataService', 'applicationContext', 'selectedSchedule', 'selectedShift', 'selectedEmployee',
  function ($scope, $modalInstance, $timeout, dataService, applicationContext, selectedSchedule, selectedShift, selectedEmployee) {
    var siteId = selectedSchedule.siteInfo[0];
    var timezone = selectedSchedule.siteInfo[2];

    $scope.watchers = {};

    $scope.removeWatchers = function() {
      for (var i in $scope.watchers) {
        $scope.watchers[i]();
      }
    };

    $scope.data = {
      selectedEmployee: selectedEmployee,
      teamName: selectedShift.teamName,
      skillName: selectedShift.skillName,
      swapShift: false,
      eligibleEmployeesLoaded: true,
      eligibleEmployees: [],
      eligibleShiftsLoaded: false,
      eligibleShifts: [],
      eligibleShiftsGridOptions: {
        data: 'data.eligibleShifts',
        enableRowSelection: true,
        enableSelectAll: false,
        multiSelect: false,
        columnDefs: [
          { name: 'shiftId', visible: false },
          { name: 'employeeId', visible: false },
          { name: 'employeeName', width: 140 },
          { name: 'teamName', width: 100 },
          { name: 'skillName', width: 120 },
          { name: 'date', width: 80 },
          { name: 'shift', width: 150, sortable: false }
        ],
        onRegisterApi: function(gridApi) {
          $scope.gridApi = gridApi;
          gridApi.selection.on.rowSelectionChanged($scope, function(row) {
            if (row.isSelected) {
              $scope.data.newSelectedShift = row.entity;
            } else {
              $scope.data.newSelectedShift = null;
            }
          });
        }
      },
      absenceTypes: [],
      comment: ''
    };
    $scope.data.formattedTime = moment.tz(selectedShift.start, timezone).format('dddd, MM/DD/YYYY (h:mma') + ' - ' +
      moment.tz(selectedShift.end, timezone).format('h:mma)');

    $scope.hasNewShift = function() {
      if (typeof $scope.data.endPtoDate === 'undefined' || $scope.data.endPtoDate === null ||
        typeof $scope.data.startPtoDate === 'undefined' || $scope.data.startPtoDate === null) {
        return false;
      }

      if ($scope.data.endPtoDate.unix() > $scope.data.startPtoDate.unix()) {
        return true;
      }

      return false;
    };

    $scope.getAbsenceTypes = function() {
      dataService.getAbsenceTypes(siteId).then(function(result) {
        $scope.data.absenceTypes = result.data;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    $scope.getWipEligibleEmployees = function() {
      if ($scope.data.action !== 'CreateAndAssign') {
        return;
      }

      $scope.data.eligibleEmployeesLoaded = false;
      $scope.data.eligibleEmployees = [];
      var shiftInfo = {
        teamId: selectedShift.teamId,
        skillId: selectedShift.skillId,
        start: $scope.data.startPtoDate.unix() * 1000,
        end: $scope.data.endPtoDate.unix() * 1000
      };

      dataService.getWipEligibleEmployeesForProposedOpenShift(selectedSchedule.id, shiftInfo).then(function(result) {
        $scope.data.eligibleEmployees = result.data.eligibleEmployees;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      }).finally(function() {
        $scope.data.eligibleEmployeesLoaded = true;
      });
    };

    $scope.getSwapEligibleShifts = function() {
      $scope.data.eligibleShiftsLoaded = false;
      $scope.data.eligibleShifts = [];
      dataService.getSwapEligibleShiftsForShift(selectedEmployee[0], selectedShift.id).then(function(result) {
        angular.forEach(result.data.swappableShifts, function(shift) {
          shift.date = moment.tz(shift.startDateTime, timezone).format('MM/DD/YYYY');
          shift.shift = moment.tz(shift.startDateTime, timezone).format('hh:mma') + ' - ' +
            moment.tz(shift.endDateTime, timezone).format('hh:mma');
          $scope.data.eligibleShifts.push(shift);
        });
        $timeout(function() {
          $('.eligible-shifts-grid').resize();
        }, 0);
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      }).finally(function() {
        $scope.data.eligibleShiftsLoaded = true;
      });
    };

    $scope.getAbsenceTypes();

    $scope.initTimeLine = function() {
      var timeLineWidth = $scope.timeLineWidth;
      var startDateLine = moment.tz($scope.data.selectedShift.start, timezone);
      startDateLine.hours(startDateLine.hours() - $scope.hoursBeforeAfter).minutes(0).seconds(0).milliseconds(0);
      startDateLine.hours(startDateLine.hours() + startDateLine.hours()%2).minutes(0).seconds(0).milliseconds(0);
      var endDateLine = moment.tz($scope.data.selectedShift.end, timezone);
      endDateLine.hours(endDateLine.hours() + $scope.hoursBeforeAfter).minutes(0).seconds(0).milliseconds(0);
      var hoursN = (endDateLine.unix() - startDateLine.unix()) / 3600;

      var koef = 1;
      if (hoursN > 12) {
        koef = Math.ceil(hoursN / 12);
        switch (koef) {
          case 5:
            koef = 6;
            break;
          case 7:
            koef = 8;
            break;
          default:
            if (koef > 8 && koef <= 12) {
              koef = 12;
            } else if (koef > 12) {
              koef = 24;
            }
        }
        //koef = Math.pow(2, Math.floor(hoursN / 12));
      }
      $scope.hours = [];
      for (var i = 0; i < hoursN / koef; i++) {
        var temp = moment.tz(startDateLine, timezone);
        temp.hours(temp.hours() + i * koef);
        $scope.hours[i] = temp;
      }

      var minStep = 15;
      //var minStep = 15 * Math.pow(2, Math.floor(Math.sqrt(koef1)) - 1);
      //if (minStep > 60) {
      //    minStep = 60;
      //}
      $scope.data.step = timeLineWidth / (hoursN * (60 / minStep));

      var controlDate = moment.tz($scope.hours[$scope.hours.length - 1], timezone);
      controlDate.hours(controlDate.hours() + koef);
      if (controlDate.unix() > endDateLine.unix()) {
        endDateLine = controlDate;
      }

      var lineDiff = (endDateLine.unix() - startDateLine.unix()) * 1000;
      var startPoint = ($scope.data.startDate.unix() - startDateLine.unix()) * 1000;
      var duration = ($scope.data.endDate.unix() - $scope.data.startDate.unix()) * 1000;

      $scope.data.shiftModel = {
        start: startPoint * timeLineWidth / lineDiff,
        width: duration * timeLineWidth / lineDiff
      };

      $scope.data.newShiftModel.start = ($scope.data.startPtoDate.unix() - startDateLine.unix()) * 1000 * timeLineWidth / lineDiff;
      $scope.data.newShiftModel.width = ($scope.data.endPtoDate.unix() - $scope.data.startPtoDate.unix()) * 1000 * timeLineWidth / lineDiff;

      $scope.data.limits = {
        start: ($scope.data.selectedShift.start - startDateLine.unix() * 1000) * timeLineWidth / lineDiff,
        end: ($scope.data.selectedShift.end - startDateLine.unix() * 1000) * timeLineWidth / lineDiff
      };

      $scope.watchers.shiftModel = $scope.$watch('[data.shiftModel.start, data.shiftModel.width]', function (newValue, oldValue) {
        $scope.data.startDate = moment.tz(roundTime((newValue[0] * lineDiff) / timeLineWidth + startDateLine.unix() * 1000, minStep), timezone);
        $scope.data.endDate = moment.tz(roundTime((newValue[1] * lineDiff) / timeLineWidth + $scope.data.startDate.unix() * 1000, minStep), timezone);
        $scope.data.dropShitAttempt = false;

        var newVal = {
          start: newValue[0],
          width: newValue[1]
        };

        var oldVal = {
          start: oldValue[0],
          width: oldValue[1]
        };

        var shiftModel = {
          start: newVal.start,
          width: newVal.width
        };
        var newShiftModel = angular.copy($scope.data.newShiftModel);

        angular.forEach(newValue, function(value, key) {
          if (newValue[key] < 0) {
            newValue[key] = 0;
          }
        });

        if (!$scope.hasNewShift()) {
          if (newVal.start > oldVal.start) {
            newShiftModel.start = $scope.data.limits.start;
            if ((newVal.start - $scope.data.limits.start) > 0) {
              newShiftModel.width = newVal.start - $scope.data.limits.start;
            } else {
              newShiftModel.width = 0;
            }
            newShiftModel.position = 'before';
          } else if (newVal.width < oldVal.width) {
            newShiftModel.start = newVal.start + newVal.width;
            if (($scope.data.limits.end - newVal.width) > 0) {
              newShiftModel.width = $scope.data.limits.end - (newVal.width + newVal.start);
            } else {
              newShiftModel.width = 0;
            }
            newShiftModel.position = 'after';
          }
        } else if (newShiftModel.position == 'before') {
          if ((newVal.start > oldVal.start)) {
            newShiftModel.width += (newVal.start - oldVal.start);
          } else if (newVal.start >= newShiftModel.start) {
            newShiftModel.width = newShiftModel.width - (oldVal.start - newVal.start);
          } else {
            newShiftModel.start = newVal.start;
            newShiftModel.width = 0;
          }
          //} else if (newShiftModel.position == 'after' && $scope.backUpEndDate.getTime() != $scope.endDate.getTime()) {
        } else if (newShiftModel.position == 'after' && (
          ( Math.abs((newVal.start + newVal.width) - (oldVal.start + oldVal.width)) > 3 ))) {
          var widthDiff = newVal.width - oldVal.width;
          if ((newVal.width < oldVal.width)) {
            newShiftModel.start += widthDiff;
            newShiftModel.width -= widthDiff;

          } else if ((newVal.start + newVal.width) < (newShiftModel.start + newShiftModel.width) &&
            (newVal.start + newVal.width) > newShiftModel.start) {
            newShiftModel.start += widthDiff;
            newShiftModel.width -= widthDiff;
          } else {
            newShiftModel.start = newVal.start;
            newShiftModel.width = 0;
          }
        }

        if ($scope.hasNewShift()) {
          if (newShiftModel.position == 'before' && (newVal.width < oldVal.width) &&
            ((newVal.width + newVal.start) < $scope.data.limits.end)) {
            shiftModel.width = Math.abs($scope.data.limits.end - newVal.start);
          }

          if (newShiftModel.position == 'after' && (newVal.width < oldVal.width) &&
            (shiftModel.start > $scope.data.limits.start)) {
            shiftModel.start = $scope.data.limits.start;
            shiftModel.width = Math.abs(newShiftModel.start - $scope.data.limits.start);
          }

          if (shiftModel.start + shiftModel.width > newShiftModel.start  && newShiftModel.position == 'after'){
            var diff = shiftModel.start + shiftModel.width - newShiftModel.start;
            newShiftModel.width -= diff;
            newShiftModel.start += diff;
          }
        }

        $scope.data.shiftModel = angular.copy(shiftModel);
        $scope.data.newShiftModel = angular.copy(newShiftModel);
        $scope.data.startPtoDate = moment.tz(roundTime(($scope.data.newShiftModel.start * lineDiff) / timeLineWidth + startDateLine.unix() * 1000, minStep), timezone);
        $scope.data.endPtoDate = moment.tz(roundTime(($scope.data.newShiftModel.width * lineDiff) / timeLineWidth + $scope.data.startPtoDate.unix() * 1000, minStep), timezone);
      }, true);

      function roundTime(time, min) {
        return (Math.round(time / 1000 / 60 / min)) * min * 60 * 1000;
      }
    };

    $scope.initShiftEditForm = function() {
      $scope.hoursBeforeAfter = 4;
      if (typeof $scope.gridApi !== 'undefined' && $scope.gridApi !== null) {
        $scope.gridApi.selection.clearSelectedRows();
      }

      $scope.data = _.assign($scope.data, {
        selectedShift: angular.copy(selectedShift),
        newFillEmployee: null,
        newSelectedShift: null,
        startDate: moment.tz(selectedShift.start, timezone),
        endDate: moment.tz(selectedShift.end, timezone),
        startPtoDate: moment.tz(selectedShift.start, timezone),
        endPtoDate: moment.tz(selectedShift.start, timezone),
        newShiftModel: {
          start: 0,
          width: 0,
          position: ''
        },
        action: 'CreateAndDrop',
        changeToPto: false,
        selectedAbsenceType: null
      });

      $scope.initTimeLine();
    };

    $scope.onSwapShiftChanged = function() {
      console.log("onSwapShiftChanged");

      $scope.data.dropShitAttempt = false;
      $scope.initShiftEditForm();
      if ($scope.data.swapShift) {
        $scope.getSwapEligibleShifts();
      }
    };

    $scope.onDropShiftAttempt = function() {
      var siteId = selectedSchedule.siteInfo[0];
      $scope.data.dropShitAttempt = true;

      dataService.getDropShiftReasons(siteId).then(function(res) {
        console.log("getDropShiftReasons", res);
        $scope.dropShiftReasons = res.data.result;
        $scope.dropShiftReasonId = $scope.dropShiftReasons[0].id;
      }, function(err) {
        applicationContext.setNotificationMsgWithValues("Failed to load drop shit reasons", 'danger', true);
      });

    };

    $scope.zoomIn = function() {
      if ($scope.hoursBeforeAfter > 1) {
        $scope.hoursBeforeAfter /= 2;

        var tempStartDateLine = moment.tz($scope.data.selectedShift.start, timezone);
        tempStartDateLine.hours(tempStartDateLine.hours() - $scope.hoursBeforeAfter).minutes(0).seconds(0).milliseconds(0);
        var tempEndDateLine = moment.tz($scope.data.selectedShift.end, timezone);
        tempEndDateLine.hours(tempEndDateLine.hours() + $scope.hoursBeforeAfter).minutes(0).seconds(0).milliseconds(0);
        if (tempStartDateLine.unix() <= $scope.data.startDate.unix() &&
          tempEndDateLine.unix() >= $scope.data.endDate.unix()) {
          $scope.updateTimeLine();
          return true;
        } else {
          $scope.hoursBeforeAfter *= 2;
          return false;
        }
      }
    };

    $scope.zoomOut = function() {
      $scope.hoursBeforeAfter *= 2;

      var tempStartDateLine = moment.tz($scope.data.selectedShift.start, timezone);
      tempStartDateLine.hours(tempStartDateLine.hours() - $scope.hoursBeforeAfter).minutes(0).seconds(0).milliseconds(0);
      var tempEndDateLine = moment.tz($scope.data.selectedShift.end, timezone);
      tempEndDateLine.hours(tempEndDateLine.hours() + $scope.hoursBeforeAfter).minutes(0).seconds(0).milliseconds(0);
      if ((tempEndDateLine.unix() - tempStartDateLine.unix()) < ($scope.maxHours * 3600)) {
        $scope.updateTimeLine();
        return true;
      } else {
        $scope.hoursBeforeAfter /= 2;
        return false;
      }
    };

    $scope.zoomBest = function() {
      var i = true;
      while(i) {
        i = $scope.zoomIn();
      }
    };

    $scope.updateTimeLine = function() {
      $scope.removeWatchers();
      $scope.$broadcast('event:updateShift');

      $scope.initTimeLine();
    };

    $scope.dropShift = function() {
      $modalInstance.close({operation: 'drop', dropShiftReasonId: $scope.dropShiftReasonId});
    };

    $scope.submit = function() {
      var result = null;
      var startTime = null;
      var endTime = null;
      var newShiftStartTime = null;
      var newShiftEndTime = null;

      if ($scope.data.swapShift) {
        result = {
          operation: 'swap',
          newSelectedShift: $scope.data.newSelectedShift
        };
      } else {
        startTime = $scope.data.startDate.unix() * 1000;
        endTime = $scope.data.endDate.unix() * 1000;
        result = {
          operation: 'update',
          startTime: startTime,
          endTime: endTime
        };
        if ($scope.hasNewShift()) {
          newShiftStartTime = $scope.data.startPtoDate.unix() * 1000;
          newShiftEndTime = $scope.data.endPtoDate.unix() * 1000;
          result.newShiftStartTime = newShiftStartTime;
          result.newShiftEndTime = newShiftEndTime;
          if ($scope.data.changeToPto) {
            result.ptoType = $scope.data.selectedAbsenceType;
          }
          result.spaceOperation = $scope.data.action;
          if ($scope.data.action === 'CreateAndAssign') {
            result.newFillEmployee = $scope.data.newFillEmployee;
          }
        }
      }
      result.comment = $scope.data.comment;
      $modalInstance.close(result);
    };

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };
  }]);

angular.module('emlogis.employeeSchedules').controller('AddNewShiftPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', '$filter', 'applicationContext', 'dataService', 'viewMode', 'selectedSchedule',
  function ($scope, $modalInstance, $timeout, $filter, applicationContext, dataService, viewMode, selectedSchedule) {
    var siteId = selectedSchedule.siteInfo[0];
    var scheduleId = selectedSchedule.id;
    var timezone = selectedSchedule.siteInfo[2];
    var teams = [];

    $scope.data = {
      selectedScheduleStatus: selectedSchedule.status,
      teams: _.filter(selectedSchedule.teamsInfo.result, function(teamInfo) {
        return (teamInfo[2] === true);
      }),
      skills: [],
      selectedTeam: null,
      selectedSkill: null,
      shiftTime: {
        start: null,
        end: null
      },
      selectedOperationType: 'addAsOpenShift',
      selectedEligibleEmployee: null,
      eligibleEmployees: [],
      eligibleEmployeesLoaded: true,
      showAvailableOnly: true,
      employeeCalendarHeaderCells: [],
      employeeCalendarShifts: {},
      employeeCalendarShiftsRowHeight: '51px',
      employeeCalendarAvailItems: {},
      employeeCalendarAvailItemsRowHeight: '26px',
      employeeCalendarPrefItems: {},
      employeeCalendarPrefItemsRowHeight: '26px',
      sortOption: 'sortByName'
    };

    function nameComparator(firstEntity, secondEntity) {
      if (firstEntity[1] < secondEntity[1]) {
        return -1;
      } else if (firstEntity[1] > secondEntity[1]) {
        return 1;
      } else {
        return 0;
      }
    }

    $scope.data.teams.sort(nameComparator);

    var initializeTeamsSkills = function() {
      dataService.getSitesTeamsTree({}).then(function(response) {
        teams = _.result(_.find(response.data, function(site) {return site.id === siteId;}), 'children');
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    $scope.onSelectedTeamChanged = function() {
      if ($scope.data.selectedTeam === null) {
        $scope.data.skills = [];
      } else {
        var selectedTeamId = $scope.data.selectedTeam[0];
        var selectedTeam = _.find(teams, function(team) {return team.id === selectedTeamId;});
        $scope.data.skills = _.filter(selectedSchedule.skillsInfo.result, function(skillInfo) {
          return (skillInfo[3] && _.findIndex(selectedTeam.children, function(skill) {
            return (skill.id === skillInfo[0]);
          }) > -1);
        });
        $scope.data.skills.sort(nameComparator);
      }
      $scope.data.selectedSkill = null;
      $scope.data.eligibleEmployees = [];
    };

    $scope.onSelectedSkillChanged = function() {
      if ($scope.data.selectedSkill === null) {
        $scope.data.eligibleEmployees = [];
        return;
      }
      $scope.getWipEligibleEmployees();
      initializeCalendarCells();
    };

    $scope.onFillShiftSelected = function() {
      $scope.getWipEligibleEmployees();
      initializeCalendarCells();
    };

    $scope.canShowFooterContent = function() {
      return ($scope.data.selectedTeam !== null && $scope.data.selectedSkill !== null &&
        $scope.data.shiftTime.start !== null && $scope.data.selectedOperationType === 'fillShift' &&
        $scope.data.selectedEligibleEmployee !== null);
    };

    var calculateEmployeesInfo = function() {
      angular.forEach($scope.data.eligibleEmployees, function(employee) {
        var foundEmployee = _.find(selectedSchedule.employeesInfo.result, function(iteratee) {
          return (iteratee[0] === employee.employeeId);
        });
        var hoursOfEmployee = 0;
        angular.forEach(selectedSchedule.selectedWeek.shifts.employeeShifts[employee.employeeId], function(dateShifts, calendarDate) {
          angular.forEach(dateShifts, function(shift) {
            var shiftDurationInHours = (shift.end - shift.start)/3600000;
            hoursOfEmployee += shiftDurationInHours;
          });
        });
        var costOfEmployee = foundEmployee[3] * hoursOfEmployee;
        employee.hours = hoursOfEmployee;
        employee.cost = costOfEmployee;
      });
    };

    var comparator = function(firstElement, secondElement) {
      var firstValue = null;
      var secondValue = null;

      if ($scope.data.sortOption === 'sortByName') {
        firstValue = firstElement.employeeName;
        secondValue = secondElement.employeeName;
      } else if ($scope.data.sortOption === 'leastHoursFirst') {
        firstValue = firstElement.hours;
        secondValue = secondElement.hours;
      } else if ($scope.data.sortOption === 'leastCostFirst') {
        firstValue = firstElement.cost;
        secondValue = secondElement.cost;
      }

      if (firstValue < secondValue) {
        return -1;
      } else if (firstValue > secondValue) {
        return 1;
      } else {
        return 0;
      }
    };

    $scope.setSortOption = function(option) {
      $scope.data.sortOption = option;
      $scope.data.eligibleEmployees.sort(comparator);
    };

    $scope.getWipEligibleEmployees = function() {
      $scope.data.eligibleEmployees = [];
      $scope.data.selectedEligibleEmployee = null;

      if ($scope.data.selectedOperationType !== 'fillShift') {
        return;
      }
      if ($scope.data.selectedTeam === null || $scope.data.selectedSkill === null) {
        return;
      }
      if ($scope.data.shiftTime.start === null || $scope.data.shiftTime.end === null) {
        return;
      }

      $scope.data.eligibleEmployeesLoaded = false;
      var overrideOptions = null;

      if (!$scope.data.showAvailableOnly) {
        overrideOptions = {
          ALL_DAY_UNAVAILABLE_OVERRIDE: true,
          TIME_WINDOW_UNAVAILABLE_OVERRIDE: true
        };
      }

      var shiftInfo = {
        teamId: $scope.data.selectedTeam[0],
        skillId: $scope.data.selectedSkill[0],
        start: $scope.data.shiftTime.start,
        end: $scope.data.shiftTime.end
      };

      dataService.getWipEligibleEmployeesForProposedOpenShift(scheduleId, shiftInfo, overrideOptions).then(function(result) {
        $scope.data.eligibleEmployees = result.data.eligibleEmployees;
        calculateEmployeesInfo();
        $scope.setSortOption('sortByName');
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      }).finally(function() {
        $scope.data.eligibleEmployeesLoaded = true;
      });
    };

    var composeAvailabilityItem = function(availType, timeFrame) {
      var type = 'avail-item';
      var title = '';
      var className = '';

      if (availType === 'AVAIL') {
        var startDateTime = moment.tz(timeFrame.startDateTime, timezone);
        var endDateTime = moment.tz(timeFrame.endDateTime, timezone);

        title = startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
        className = 'partially-available';
      } else if (availType === 'DAY_OFF') {
        if (timeFrame.pto) {
          title = $filter('translate')('availability.PTO_VACATION');
          className = 'holiday-vacation';
        } else {
          title = $filter('translate')('availability.NOT_AVAILABLE');
          className = 'not-available';
        }
      } else {
        title = $filter('translate')('availability.UNKNOWN_AVAILABILITY_TYPE') + ' ' + availType;
      }

      var result = {
        type: type,
        title: title,
        className: className
      };

      return result;
    };

    var composePreferenceItem = function(prefType, timeFrame) {
      var type = 'pref-item';
      var title = '';
      var className = '';
      var startDateTime = null;
      var endDateTime = null;

      if (timeFrame.startDateTime && timeFrame.endDateTime) {
        startDateTime = moment.tz(timeFrame.startDateTime, timezone);
        endDateTime = moment.tz(timeFrame.endDateTime, timezone);
      }

      switch (prefType) {
        case 'AVOID_TIMEFRAME':
          title = $filter('translate')('availability.AVOID') + ' ' +
            startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
          className = 'avoid-time-frame';
          break;
        case 'PREFER_TIMEFRAME':
          title = $filter('translate')('availability.PREFER') + ' ' +
            startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
          className = 'prefer-time-frame';
          break;
        case 'AVOID_DAY':
          title = $filter('translate')('availability.AVOID_DAY');
          className = 'avoid-day';
          break;
        case 'PREFER_DAY':
          title = $filter('translate')('availability.PREFER_DAY');
          className = 'prefer-day';
          break;
        default:
          title = $filter('translate')('availability.UNKNOWN_PREFERENCE_TYPE') + ' ' + prefType;
      }

      var result = {
        type: type,
        title: title,
        className: className
      };

      return result;
    };

    $scope.onSelectedEligibleEmployeeChanged = function() {
      initializeCalendarCells();
      if ($scope.data.selectedEligibleEmployee === null) {
        return;
      }

      var rowCount = 0;
      var startTime = selectedSchedule.selectedWeek.start;
      var endTime = selectedSchedule.selectedWeek.end;

      var queryParams = {
        params: {
          startdate: startTime,
          enddate: endTime,
          returnedfields: 'id,startDateTime,endDateTime,excess,skillAbbrev,skillName,teamName'
        }
      };
      dataService.getEmployeeCalendarAndAvailabilityView(scheduleId, $scope.data.selectedEligibleEmployee.employeeId, queryParams).then(function(response) {
        // Draw Shifts
        angular.forEach(response.data.shifts.result, function(shift) {
          var shiftStartTimeDayOfWeek = moment.tz(shift[1], timezone).format('ddd');
          var startTimeStr = moment.tz(shift[1], timezone).format('h:mma');
          startTimeStr = startTimeStr.substr(0, startTimeStr.length - 1);
          var endTimeStr = moment.tz(shift[2], timezone).format('h:mma');
          endTimeStr = endTimeStr.substr(0, endTimeStr.length - 1);

          var shiftObj = {
            type: 'normal-shift',
            start: shift[1],
            end: shift[2],
            timeStr: startTimeStr + '-' + endTimeStr,
            team: shift[6],
            skill: shift[5],
            skillAbbrev: shift[4],
            className: ''
          };
          $scope.data.employeeCalendarShifts[shiftStartTimeDayOfWeek].push(shiftObj);
        });
        rowCount = _.max($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          return dayShifts.length;
        }).length;
        angular.forEach($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          if (dayShifts.length === rowCount) {
            dayShifts[dayShifts.length - 1].className = 'no-border';
          }
        });
        $scope.data.employeeCalendarShiftsRowHeight = Math.max(51, (51 * rowCount)) + 'px';

        // Draw Availability
        angular.forEach(response.data.availcalViewDto.availCDTimeFrames, function(availCDTimeFrame) {
          var availStartTimeDayOfWeek = moment.tz(availCDTimeFrame.startDateTime, timezone).format('ddd');
          var availItemObj = composeAvailabilityItem(availCDTimeFrame.availType, availCDTimeFrame);
          $scope.data.employeeCalendarAvailItems[availStartTimeDayOfWeek].push(availItemObj);
        });
        angular.forEach(response.data.availcalViewDto.availCITimeFrames, function(availCITimeFrame) {
          var availStartTimeDayOfWeek = availCITimeFrame.dayOfTheWeek.substr(0, 3);
          availStartTimeDayOfWeek = availStartTimeDayOfWeek.charAt(0).toUpperCase() + availStartTimeDayOfWeek.slice(1).toLowerCase();
          var availItemObj = composeAvailabilityItem(availCITimeFrame.availType, availCITimeFrame.timeFrameInstances[0]);
          $scope.data.employeeCalendarAvailItems[availStartTimeDayOfWeek].push(availItemObj);
        });
        rowCount = _.max($scope.data.employeeCalendarAvailItems, function(dayItems, day) {
          return dayItems.length;
        }).length;
        $scope.data.employeeCalendarAvailItemsRowHeight = Math.max(26, (26 * rowCount)) + 'px';
        $scope.data.employeeCalendarAvailPrefItemsRowHeight = Math.max(26, (26 * rowCount));

        // Draw Preference
        angular.forEach(response.data.availcalViewDto.prefCDTimeFrames, function(prefCDTimeFrame) {
          var prefStartTimeDayOfWeek = moment.tz(prefCDTimeFrame.startDateTime, timezone).format('ddd');
          var prefItemObj = composePreferenceItem(prefCDTimeFrame.prefType, prefCDTimeFrame);
          $scope.data.employeeCalendarPrefItems[prefStartTimeDayOfWeek].push(prefItemObj);
        });
        angular.forEach(response.data.availcalViewDto.prefCITimeFrames, function(prefCITimeFrame) {
          var prefStartTimeDayOfWeek = prefCITimeFrame.dayOfTheWeek.substr(0, 3);
          prefStartTimeDayOfWeek = prefStartTimeDayOfWeek.charAt(0).toUpperCase() + prefStartTimeDayOfWeek.slice(1).toLowerCase();
          var prefItemObj = composePreferenceItem(prefCITimeFrame.prefType, prefCITimeFrame.timeFrameInstances[0]);
          $scope.data.employeeCalendarPrefItems[prefStartTimeDayOfWeek].push(prefItemObj);
        });
        rowCount = _.max($scope.data.employeeCalendarPrefItems, function(dayItems, day) {
          return dayItems.length;
        }).length;
        $scope.data.employeeCalendarPrefItemsRowHeight = Math.max(26, (26 * rowCount)) + 'px';
        $scope.data.employeeCalendarAvailPrefItemsRowHeight += Math.max(26, (26 * rowCount));
        $scope.data.employeeCalendarAvailPrefItemsRowHeight += 'px';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });

      dataService.getEmployeeDetails($scope.data.selectedEligibleEmployee.employeeId).then(function(response) {
        $scope.data.selectedEligibleEmployee.name = response.data.firstName + response.data.lastName;
        $scope.data.selectedEligibleEmployee.homePhone = (response.data.homePhone)? response.data.homePhone: 'N/A';
        $scope.data.selectedEligibleEmployee.cellPhone = (response.data.mobilePhone)? response.data.mobilePhone: 'N/A';
        $scope.data.selectedEligibleEmployee.email = (response.data.workEmail)? response.data.workEmail: 'N/A';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    var initializeCalendarHeaderCells = function() {
      var startDayOfWeek = moment.tz(selectedSchedule.selectedWeek.start, timezone).hours(0).minutes(0).seconds(0).milliseconds(0);
      for (var i=0; i<7; i++) {
        var day = startDayOfWeek.clone().add(i, 'days');
        $scope.data.employeeCalendarHeaderCells.push({dayOfWeek: day.format('ddd'), date: day.format('M/D'), timestamp: day.unix() * 1000});
      }
    };

    var initializeCalendarCells = function() {
      if ($scope.data.selectedOperationType !== 'fillShift') {
        return;
      }
      if ($scope.data.selectedTeam === null || $scope.data.selectedSkill === null) {
        return;
      }
      if ($scope.data.shiftTime.start === null || $scope.data.shiftTime.end === null) {
        return;
      }

      $scope.data.employeeCalendarShifts = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
      $scope.data.employeeCalendarAvailItems = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
      $scope.data.employeeCalendarPrefItems = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };

      var shiftStartTimeDayOfWeek = moment.tz($scope.data.shiftTime.start, timezone).format('ddd');
      var startTimeStr = moment.tz($scope.data.shiftTime.start, timezone).format('h:mma');
      startTimeStr = startTimeStr.substr(0, startTimeStr.length - 1);
      var endTimeStr = moment.tz($scope.data.shiftTime.end, timezone).format('h:mma');
      endTimeStr = endTimeStr.substr(0, endTimeStr.length - 1);

      var shiftObj = {
        type: 'new-shift',
        start: $scope.data.shiftTime.start,
        end: $scope.data.shiftTime.end,
        timeStr: startTimeStr + '-' + endTimeStr,
        team: $scope.data.selectedTeam[1],
        skill: $scope.data.selectedSkill[1],
        skillAbbrev: $scope.data.selectedSkill[2],
        className: ''
      };
      $scope.data.employeeCalendarShifts[shiftStartTimeDayOfWeek] = [shiftObj];
    };

    $scope.submit = function() {
      var actionStr = '';
      var employeeId = null;
      var shiftInfo = {
        teamId: $scope.data.selectedTeam[0],
        skillId: $scope.data.selectedSkill[0],
        startDateTime: $scope.data.shiftTime.start,
        endDateTime: $scope.data.shiftTime.end
      };

      if ($scope.data.selectedOperationType === 'addAsOpenShift') {
        actionStr = 'CreateAsOpenShift';
      } else if ($scope.data.selectedOperationType === 'postShift') {
        actionStr = 'CreateAndPost';
      } else if ($scope.data.selectedOperationType === 'fillShift') {
        actionStr = 'CreateAndAssign';
        employeeId = $scope.data.selectedEligibleEmployee.employeeId;
      }

      var result = {
        shiftInfo: shiftInfo,
        action: actionStr,
        employeeId: employeeId
      };
      $modalInstance.close(result);
    };

    $scope.close = function() {
      $modalInstance.dismiss('cancel');
    };

    $timeout(function() {
      var startMoment = null;
      var endMoment = null;
      if (viewMode === 'week') {
        startMoment = moment.tz(selectedSchedule.selectedWeek.start, timezone);
        endMoment = moment.tz(selectedSchedule.selectedWeek.end, timezone);
      } else {
        startMoment = moment.tz(selectedSchedule.selectedWeek.selectedDay.date, timezone);
        endMoment = moment.tz(selectedSchedule.selectedWeek.selectedDay.date, timezone).hours(23).minutes(59).seconds(59);
      }
      $('#start-datetime-picker').datetimepicker();
      $('#end-datetime-picker').datetimepicker();
      $('#start-datetime-picker').data('DateTimePicker').minDate(moment(startMoment.format('YYYY-MM-DD')));
      $('#start-datetime-picker').data('DateTimePicker').maxDate(moment(endMoment.format('YYYY-MM-DD HH:mm:ss')));
      $('#end-datetime-picker').data('DateTimePicker').minDate(moment(startMoment.format('YYYY-MM-DD')));
      $('#end-datetime-picker').data('DateTimePicker').maxDate(moment(endMoment.clone().add(1, 'days').hours(23).minutes(59).seconds(59).format('YYYY-MM-DD HH:mm:ss')));
      $('#start-datetime-picker').data('DateTimePicker').date(moment(startMoment.format('YYYY-MM-DD 08:00:00')));
      $('#end-datetime-picker').data('DateTimePicker').date(moment(startMoment.format('YYYY-MM-DD 16:00:00')));
      var startDateTimeStr = $('#start-datetime-picker').data('DateTimePicker').date().format('YYYY-MM-DD HH:mm:ss');
      var endDateTimeStr = $('#end-datetime-picker').data('DateTimePicker').date().format('YYYY-MM-DD HH:mm:ss');
      $scope.data.shiftTime.start = moment.tz(startDateTimeStr, timezone).unix() * 1000;
      $scope.data.shiftTime.end = moment.tz(endDateTimeStr, timezone).unix() * 1000;

      $('#start-datetime-picker').on('dp.change', function (e) {
        $('#end-datetime-picker').data('DateTimePicker').maxDate(false);
        $('#end-datetime-picker').data('DateTimePicker').minDate(e.date);
        $('#end-datetime-picker').data('DateTimePicker').maxDate(e.date.clone().hours(23).minutes(59).seconds(59).add(1, 'days'));
        $('#end-datetime-picker').data('DateTimePicker').date(e.date.clone().add(8, 'hours'));
        var startDateTimeStr = e.date.format('YYYY-MM-DD HH:mm:ss');
        var endDateTimeStr = $('#end-datetime-picker').data('DateTimePicker').date().format('YYYY-MM-DD HH:mm:ss');
        $scope.data.shiftTime.start = moment.tz(startDateTimeStr, timezone).unix() * 1000;
        $scope.data.shiftTime.end = moment.tz(endDateTimeStr, timezone).unix() * 1000;
        $scope.getWipEligibleEmployees();
        initializeCalendarCells();
        $scope.$apply();
      });
      $('#end-datetime-picker').on('dp.change', function (e) {
        if ($('#start-datetime-picker').data('DateTimePicker').date() === null) {
          $('#start-datetime-picker').data('DateTimePicker').date(e.date.clone());
        }
        var startDateTimeStr = $('#start-datetime-picker').data('DateTimePicker').date().format('YYYY-MM-DD HH:mm:ss');
        var endDateTimeStr = e.date.format('YYYY-MM-DD HH:mm:ss');
        $scope.data.shiftTime.start = moment.tz(startDateTimeStr, timezone).unix() * 1000;
        $scope.data.shiftTime.end = moment.tz(endDateTimeStr, timezone).unix() * 1000;
        $scope.getWipEligibleEmployees();
        initializeCalendarCells();
        $scope.$apply();
      });
    }, 0);
    initializeTeamsSkills();
    initializeCalendarHeaderCells();
  }]);

angular.module('emlogis.employeeSchedules').controller('AssignShiftsPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', '$filter', 'applicationContext', 'dataService', 'EmployeeSchedulesService', 'selectedSchedule', 'selectedEmployee',
  function ($scope, $modalInstance, $timeout, $filter, applicationContext, dataService, EmployeeSchedulesService, selectedSchedule, selectedEmployee) {
    var siteId = selectedSchedule.siteInfo[0];
    var scheduleId = selectedSchedule.id;
    var timezone = selectedSchedule.siteInfo[2];

    $scope.data = {
      parsedEmployeeInfo: {
        id: selectedEmployee[0],
        name: selectedEmployee[1] + ' ' + selectedEmployee[2]
      },
      employeeCalendarHeaderCells: [],
      employeeCalendarShifts: {},
      employeeCalendarShiftsRowHeight: '51px',
      employeeCalendarAvailItems: {},
      employeeCalendarAvailItemsRowHeight: '26px',
      employeeCalendarPrefItems: {},
      employeeCalendarPrefItemsRowHeight: '26px',
      employeeCalendarAvailPrefItemsRowHeight: '52px',
      employeeCalendarOpenShifts: {},
      employeeCalendarOpenShiftsRowHeight: '51px',
      selectedOpenShiftId: null,
      openShiftsLoaded: false
    };

    var initializeEmployeeDetails = function() {
      dataService.getEmployeeDetails(selectedEmployee[0]).then(function(response) {
        $scope.data.parsedEmployeeInfo.homePhone = (response.data.homePhone)? response.data.homePhone: 'N/A';
        $scope.data.parsedEmployeeInfo.cellPhone = (response.data.mobilePhone)? response.data.mobilePhone: 'N/A';
        $scope.data.parsedEmployeeInfo.email = (response.data.workEmail)? response.data.workEmail: 'N/A';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    var initializeCalendarCells = function() {
      var startDayOfWeek = moment.tz(selectedSchedule.selectedWeek.start, timezone).hours(0).minutes(0).seconds(0).milliseconds(0);
      for (var i=0; i<7; i++) {
        var day = startDayOfWeek.clone().add(i, 'days');
        $scope.data.employeeCalendarHeaderCells.push({dayOfWeek: day.format('ddd'), date: day.format('M/D'), timestamp: day.unix() * 1000});
      }
      $scope.data.employeeCalendarShifts = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
      $scope.data.employeeCalendarAvailItems = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
      $scope.data.employeeCalendarPrefItems = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
    };

    var composeAvailabilityItem = function(availType, timeFrame) {
      var type = 'avail-item';
      var title = '';
      var className = '';

      if (availType === 'AVAIL') {
        var startDateTime = moment.tz(timeFrame.startDateTime, timezone);
        var endDateTime = moment.tz(timeFrame.endDateTime, timezone);

        title = startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
        className = 'partially-available';
      } else if (availType === 'DAY_OFF') {
        if (timeFrame.pto) {
          title = $filter('translate')('availability.PTO_VACATION');
          className = 'holiday-vacation';
        } else {
          title = $filter('translate')('availability.NOT_AVAILABLE');
          className = 'not-available';
        }
      } else {
        title = $filter('translate')('availability.UNKNOWN_AVAILABILITY_TYPE') + ' ' + availType;
      }

      var result = {
        type: type,
        title: title,
        className: className,
        availType: availType,
        start: (availType === 'AVAIL')? timeFrame.startDateTime: null,
        end: (availType === 'AVAIL')? timeFrame.endDateTime: null
      };

      return result;
    };

    var composePreferenceItem = function(prefType, timeFrame) {
      var type = 'pref-item';
      var title = '';
      var className = '';
      var startDateTime = null;
      var endDateTime = null;

      if (timeFrame.startDateTime && timeFrame.endDateTime) {
        startDateTime = moment.tz(timeFrame.startDateTime, timezone);
        endDateTime = moment.tz(timeFrame.endDateTime, timezone);
      }

      switch (prefType) {
        case 'AVOID_TIMEFRAME':
          title = $filter('translate')('availability.AVOID') + ' ' +
            startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
          className = 'avoid-time-frame';
          break;
        case 'PREFER_TIMEFRAME':
          title = $filter('translate')('availability.PREFER') + ' ' +
            startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
          className = 'prefer-time-frame';
          break;
        case 'AVOID_DAY':
          title = $filter('translate')('availability.AVOID_DAY');
          className = 'avoid-day';
          break;
        case 'PREFER_DAY':
          title = $filter('translate')('availability.PREFER_DAY');
          className = 'prefer-day';
          break;
        default:
          title = $filter('translate')('availability.UNKNOWN_PREFERENCE_TYPE') + ' ' + prefType;
      }

      var result = {
        type: type,
        title: title,
        className: className
      };

      return result;
    };

    var drawCalendarContent = function() {
      var rowCount = 0;
      var startTime = selectedSchedule.selectedWeek.start;
      var endTime = selectedSchedule.selectedWeek.end;

      var queryParams = {
        params: {
          startdate: startTime,
          enddate: endTime,
          returnedfields: 'id,startDateTime,endDateTime,excess,skillAbbrev,skillName,teamName'
        }
      };
      dataService.getEmployeeCalendarAndAvailabilityView(scheduleId, $scope.data.parsedEmployeeInfo.id, queryParams).then(function(response) {
        // Draw Shifts
        angular.forEach(response.data.shifts.result, function(shift) {
          var shiftStartTimeDayOfWeek = moment.tz(shift[1], timezone).format('ddd');

          var startTimeStr;
          var startTimeMoment = moment.tz(shift[1], timezone);
          if (startTimeMoment.minutes() > 0) {
            startTimeStr = startTimeMoment.format('h:mma');
          } else {
            startTimeStr = startTimeMoment.format('ha');
          }
          startTimeStr = startTimeStr.substr(0, startTimeStr.length - 1);

          var endTimeStr;
          var endTimeMoment = moment.tz(shift[2], timezone);
          if (endTimeMoment.minutes() > 0) {
            endTimeStr = endTimeMoment.format('h:mma');
          } else {
            endTimeStr = endTimeMoment.format('ha');
          }
          endTimeStr = endTimeStr.substr(0, endTimeStr.length - 1);

          var shiftObj = {
            type: 'normal-shift',
            start: shift[1],
            end: shift[2],
            timeStr: startTimeStr + '-' + endTimeStr,
            team: shift[6],
            skill: shift[5],
            skillAbbrev: shift[4],
            className: ''
          };
          $scope.data.employeeCalendarShifts[shiftStartTimeDayOfWeek].push(shiftObj);
        });
        rowCount = _.max($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          return dayShifts.length;
        }).length;
        if (rowCount > 0) {
          angular.forEach($scope.data.employeeCalendarShifts, function(dayShifts, day) {
            if (dayShifts.length === rowCount) {
              dayShifts[dayShifts.length - 1].className = 'no-border';
            }
          });
        }
        $scope.data.employeeCalendarShiftsRowHeight = Math.max(51, (51 * rowCount)) + 'px';

        // Draw Availability
        angular.forEach(response.data.availcalViewDto.availCDTimeFrames, function(availCDTimeFrame) {
          var availStartTimeDayOfWeek = moment.tz(availCDTimeFrame.startDateTime, timezone).format('ddd');
          var availItemObj = composeAvailabilityItem(availCDTimeFrame.availType, availCDTimeFrame);
          $scope.data.employeeCalendarAvailItems[availStartTimeDayOfWeek].push(availItemObj);
        });
        angular.forEach(response.data.availcalViewDto.availCITimeFrames, function(availCITimeFrame) {
          var availStartTimeDayOfWeek = availCITimeFrame.dayOfTheWeek.substr(0, 3);
          availStartTimeDayOfWeek = availStartTimeDayOfWeek.charAt(0).toUpperCase() + availStartTimeDayOfWeek.slice(1).toLowerCase();
          var availItemObj = composeAvailabilityItem(availCITimeFrame.availType, availCITimeFrame.timeFrameInstances[0]);
          $scope.data.employeeCalendarAvailItems[availStartTimeDayOfWeek].push(availItemObj);
        });
        rowCount = _.max($scope.data.employeeCalendarAvailItems, function(dayItems, day) {
          return dayItems.length;
        }).length;
        $scope.data.employeeCalendarAvailItemsRowHeight = Math.max(26, (26 * rowCount)) + 'px';
        $scope.data.employeeCalendarAvailPrefItemsRowHeight = Math.max(26, (26 * rowCount));

        // Draw Preference
        angular.forEach(response.data.availcalViewDto.prefCDTimeFrames, function(prefCDTimeFrame) {
          var prefStartTimeDayOfWeek = moment.tz(prefCDTimeFrame.startDateTime, timezone).format('ddd');
          var prefItemObj = composePreferenceItem(prefCDTimeFrame.prefType, prefCDTimeFrame);
          $scope.data.employeeCalendarPrefItems[prefStartTimeDayOfWeek].push(prefItemObj);
        });
        angular.forEach(response.data.availcalViewDto.prefCITimeFrames, function(prefCITimeFrame) {
          var prefStartTimeDayOfWeek = prefCITimeFrame.dayOfTheWeek.substr(0, 3);
          prefStartTimeDayOfWeek = prefStartTimeDayOfWeek.charAt(0).toUpperCase() + prefStartTimeDayOfWeek.slice(1).toLowerCase();
          var prefItemObj = composePreferenceItem(prefCITimeFrame.prefType, prefCITimeFrame.timeFrameInstances[0]);
          $scope.data.employeeCalendarPrefItems[prefStartTimeDayOfWeek].push(prefItemObj);
        });
        rowCount = _.max($scope.data.employeeCalendarPrefItems, function(dayItems, day) {
          return dayItems.length;
        }).length;
        $scope.data.employeeCalendarPrefItemsRowHeight = Math.max(26, (26 * rowCount)) + 'px';
        $scope.data.employeeCalendarAvailPrefItemsRowHeight += Math.max(26, (26 * rowCount));
        $scope.data.employeeCalendarAvailPrefItemsRowHeight += 'px';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });

      // Draw Open Shifts
      EmployeeSchedulesService.getEligibilityDataFromSelectedEntities(scheduleId, [selectedEmployee[0]], null, null).then(function(result) {
        angular.forEach(result.data.openShifts, function(openShift) {
          var startTimeDayOfWeek = moment.tz(openShift.startDateTime, timezone).format('ddd');
          if (typeof $scope.data.employeeCalendarOpenShifts[startTimeDayOfWeek] === 'undefined') {
            $scope.data.employeeCalendarOpenShifts[startTimeDayOfWeek] = [];
          }

          var foundEligibleOpenShift = null;
          var foundDayShifts = _.find(selectedSchedule.selectedWeek.shifts.openShifts, function(dayShifts) {
            var foundShift = _.find(dayShifts, function(dayShift) {
              return (dayShift.id === openShift.id);
            });

            if (typeof foundShift !== 'undefined') {
              foundEligibleOpenShift = foundShift;
              return true;
            }
            return false;
          });

          if (typeof foundDayShifts !== 'undefined') {
            $scope.data.employeeCalendarOpenShifts[startTimeDayOfWeek].push(foundEligibleOpenShift);
          }
        });

        $scope.data.openShiftsLoaded = true;
        rowCount = _.max($scope.data.employeeCalendarOpenShifts, function(dayShifts, day) {
          return dayShifts.length;
        }).length;
        $scope.data.employeeCalendarOpenShiftsRowHeight = Math.max(51, (51 * rowCount)) + 'px';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    var validateOpenShiftTick = function(openShift, dayOfWeek) {
      var foundIndex = _.findIndex($scope.data.employeeCalendarShifts[dayOfWeek], function(shiftIterator) {
        return (shiftIterator.type !== 'new-shift' && (shiftIterator.end > openShift.start && shiftIterator.start < openShift.end));
      });

      if (foundIndex < 0) {
        foundIndex = _.findIndex($scope.data.employeeCalendarAvailItems[dayOfWeek], function(itemIterator) {
          return (itemIterator.availType === 'DAY_OFF' || (itemIterator.end > openShift.start && itemIterator.start < openShift.end));
        });
      }

      return (foundIndex < 0);
    };

    var updateEmployeeCalendarShifts = function(openShift, dayOfWeek) {
      if (openShift.ticked) {
        angular.forEach($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          _.remove(dayShifts, function(shiftIterator) {
            return (shiftIterator.type === 'new-shift');
          });
        });
        var addedOpenShift = angular.copy(openShift);
        addedOpenShift.type = 'new-shift';
        $scope.data.employeeCalendarShifts[dayOfWeek].push(addedOpenShift);
      } else {
        _.remove($scope.data.employeeCalendarShifts[dayOfWeek], function(shiftIterator) {
          return (shiftIterator.id === openShift.id);
        });
      }
      var rowCount = _.max($scope.data.employeeCalendarShifts, function(dayShifts, day) {
        return dayShifts.length;
      }).length;
      if (rowCount > 0) {
        angular.forEach($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          angular.forEach(dayShifts, function(dayShift) {
            dayShift.className = '';
          });
          if (dayShifts.length === rowCount) {
            dayShifts[dayShifts.length - 1].className = 'no-border';
          }
        });
      }
      $scope.data.employeeCalendarShiftsRowHeight = Math.max(51, (51 * rowCount)) + 'px';
    };

    $scope.tickOpenShift = function(openShift, dayOfWeek) {
      if ((!openShift.ticked /*&& validateOpenShiftTick(openShift, dayOfWeek)*/) || openShift.ticked) {
        angular.forEach($scope.data.employeeCalendarOpenShifts, function(dayOpenShifts, day) {
          angular.forEach(dayOpenShifts, function(dayOpenShift) {
            if (dayOpenShift.id !== openShift.id) {
              dayOpenShift.ticked = false;
            }
          });
        });
        openShift.ticked = !openShift.ticked;
        updateEmployeeCalendarShifts(openShift, dayOfWeek);
        $scope.data.selectedOpenShiftId = (openShift.ticked)? openShift.id: null;
      }
    };

    $scope.submit = function() {
      var result = {
        selectedOpenShiftId: $scope.data.selectedOpenShiftId
      };
      $modalInstance.close(result);
    };

    $scope.close = function() {
      $modalInstance.dismiss('cancel');
    };

    initializeEmployeeDetails();
    initializeCalendarCells();
    drawCalendarContent();
  }]);

angular.module('emlogis.employeeSchedules').controller('FillShiftPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', '$filter', 'applicationContext', 'dataService', 'EmployeeSchedulesService', 'selectedSchedule', 'selectedOpenShift',
  function ($scope, $modalInstance, $timeout, $filter, applicationContext, dataService, EmployeeSchedulesService, selectedSchedule, selectedOpenShift) {
    var siteId = selectedSchedule.siteInfo[0];
    var scheduleId = selectedSchedule.id;
    var timezone = selectedSchedule.siteInfo[2];

    $scope.data = {
      selectedScheduleStatus: selectedSchedule.status,
      selectedOpenShift: selectedOpenShift,
      selectedOperationType: 'postShift',
      selectedEligibleEmployee: null,
      eligibleEmployees: [],
      eligibleEmployeesLoaded: true,
      showAvailableOnly: true,
      employeeCalendarHeaderCells: [],
      employeeCalendarShifts: {},
      employeeCalendarShiftsRowHeight: '51px',
      employeeCalendarAvailItems: {},
      employeeCalendarAvailItemsRowHeight: '26px',
      employeeCalendarPrefItems: {},
      employeeCalendarPrefItemsRowHeight: '26px',
      sortOption: 'sortByName'
    };

    $scope.onFillShiftSelected = function() {
      $scope.getWipEligibleEmployees();
      initializeCalendarCells();
    };

    $scope.canShowFooterContent = function() {
      return ($scope.data.selectedOperationType === 'fillShift' && $scope.data.selectedEligibleEmployee !== null);
    };

    var calculateEmployeesInfo = function() {
      angular.forEach($scope.data.eligibleEmployees, function(employee) {
        var foundEmployee = _.find(selectedSchedule.employeesInfo.result, function(iteratee) {
          return (iteratee[0] === employee.id);
        });
        var hoursOfEmployee = 0;
        angular.forEach(selectedSchedule.selectedWeek.shifts.employeeShifts[employee.id], function(dateShifts, calendarDate) {
          angular.forEach(dateShifts, function(shift) {
            var shiftDurationInHours = (shift.end - shift.start)/3600000;
            hoursOfEmployee += shiftDurationInHours;
          });
        });
        var costOfEmployee = foundEmployee[3] * hoursOfEmployee;
        employee.hours = hoursOfEmployee;
        employee.cost = costOfEmployee;
      });
    };

    var comparator = function(firstElement, secondElement) {
      var firstValue = null;
      var secondValue = null;

      if ($scope.data.sortOption === 'sortByName') {
        firstValue = firstElement.name;
        secondValue = secondElement.name;
      } else if ($scope.data.sortOption === 'leastHoursFirst') {
        firstValue = firstElement.hours;
        secondValue = secondElement.hours;
      } else if ($scope.data.sortOption === 'leastCostFirst') {
        firstValue = firstElement.cost;
        secondValue = secondElement.cost;
      }

      if (firstValue < secondValue) {
        return -1;
      } else if (firstValue > secondValue) {
        return 1;
      } else {
        return 0;
      }
    };

    $scope.setSortOption = function(option) {
      $scope.data.sortOption = option;
      $scope.data.eligibleEmployees.sort(comparator);
    };

    $scope.getWipEligibleEmployees = function() {
      $scope.data.eligibleEmployees = [];
      $scope.data.selectedEligibleEmployee = null;

      if ($scope.data.selectedOperationType !== 'fillShift') {
        return;
      }

      $scope.data.eligibleEmployeesLoaded = false;
      var overrideOptions = null;

      if (!$scope.data.showAvailableOnly) {
        overrideOptions = {
          ALL_DAY_UNAVAILABLE_OVERRIDE: true,
          TIME_WINDOW_UNAVAILABLE_OVERRIDE: true
        };
      }

      EmployeeSchedulesService.getEligibilityDataFromSelectedEntities(scheduleId, null, [selectedOpenShift.id], overrideOptions).then(function(result) {
        if (result.data.openShifts.length > 0) {
          $scope.data.eligibleEmployees = result.data.openShifts[0].employees;
        }
        calculateEmployeesInfo();
        $scope.setSortOption('sortByName');
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      }).finally(function() {
        $scope.data.eligibleEmployeesLoaded = true;
      });
    };

    var composeAvailabilityItem = function(availType, timeFrame) {
      var type = 'avail-item';
      var title = '';
      var className = '';

      if (availType === 'AVAIL') {
        var startDateTime = moment.tz(timeFrame.startDateTime, timezone);
        var endDateTime = moment.tz(timeFrame.endDateTime, timezone);

        title = startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
        className = 'partially-available';
      } else if (availType === 'DAY_OFF') {
        if (timeFrame.pto) {
          title = $filter('translate')('availability.PTO_VACATION');
          className = 'holiday-vacation';
        } else {
          title = $filter('translate')('availability.NOT_AVAILABLE');
          className = 'not-available';
        }
      } else {
        title = $filter('translate')('availability.UNKNOWN_AVAILABILITY_TYPE') + ' ' + availType;
      }

      var result = {
        type: type,
        title: title,
        className: className
      };

      return result;
    };

    var composePreferenceItem = function(prefType, timeFrame) {
      var type = 'pref-item';
      var title = '';
      var className = '';
      var startDateTime = null;
      var endDateTime = null;

      if (timeFrame.startDateTime && timeFrame.endDateTime) {
        startDateTime = moment.tz(timeFrame.startDateTime, timezone);
        endDateTime = moment.tz(timeFrame.endDateTime, timezone);
      }

      switch (prefType) {
        case 'AVOID_TIMEFRAME':
          title = $filter('translate')('availability.AVOID') + ' ' +
            startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
          className = 'avoid-time-frame';
          break;
        case 'PREFER_TIMEFRAME':
          title = $filter('translate')('availability.PREFER') + ' ' +
            startDateTime.format('h:mma') + '-' + endDateTime.format('h:mma');
          className = 'prefer-time-frame';
          break;
        case 'AVOID_DAY':
          title = $filter('translate')('availability.AVOID_DAY');
          className = 'avoid-day';
          break;
        case 'PREFER_DAY':
          title = $filter('translate')('availability.PREFER_DAY');
          className = 'prefer-day';
          break;
        default:
          title = $filter('translate')('availability.UNKNOWN_PREFERENCE_TYPE') + ' ' + prefType;
      }

      var result = {
        type: type,
        title: title,
        className: className
      };

      return result;
    };

    $scope.onSelectedEligibleEmployeeChanged = function() {
      initializeCalendarCells();
      if ($scope.data.selectedEligibleEmployee === null) {
        return;
      }

      var rowCount = 0;
      var startTime = selectedSchedule.selectedWeek.start;
      var endTime = selectedSchedule.selectedWeek.end;

      var queryParams = {
        params: {
          startdate: startTime,
          enddate: endTime,
          returnedfields: 'id,startDateTime,endDateTime,excess,skillAbbrev,skillName,teamName'
        }
      };
      dataService.getEmployeeCalendarAndAvailabilityView(scheduleId, $scope.data.selectedEligibleEmployee.id, queryParams).then(function(response) {
        // Draw Shifts
        angular.forEach(response.data.shifts.result, function(shift) {
          var shiftStartTimeDayOfWeek = moment.tz(shift[1], timezone).format('ddd');
          var startTimeStr = moment.tz(shift[1], timezone).format('h:mma');
          startTimeStr = startTimeStr.substr(0, startTimeStr.length - 1);
          var endTimeStr = moment.tz(shift[2], timezone).format('h:mma');
          endTimeStr = endTimeStr.substr(0, endTimeStr.length - 1);

          var shiftObj = {
            type: 'normal-shift',
            start: shift[1],
            end: shift[2],
            timeStr: startTimeStr + '-' + endTimeStr,
            team: shift[6],
            skill: shift[5],
            skillAbbrev: shift[4],
            className: ''
          };
          $scope.data.employeeCalendarShifts[shiftStartTimeDayOfWeek].push(shiftObj);
        });
        rowCount = _.max($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          return dayShifts.length;
        }).length;
        angular.forEach($scope.data.employeeCalendarShifts, function(dayShifts, day) {
          if (dayShifts.length === rowCount) {
            dayShifts[dayShifts.length - 1].className = 'no-border';
          }
        });
        $scope.data.employeeCalendarShiftsRowHeight = Math.max(51, (51 * rowCount)) + 'px';

        // Draw Availability
        angular.forEach(response.data.availcalViewDto.availCDTimeFrames, function(availCDTimeFrame) {
          var availStartTimeDayOfWeek = moment.tz(availCDTimeFrame.startDateTime, timezone).format('ddd');
          var availItemObj = composeAvailabilityItem(availCDTimeFrame.availType, availCDTimeFrame);
          $scope.data.employeeCalendarAvailItems[availStartTimeDayOfWeek].push(availItemObj);
        });
        angular.forEach(response.data.availcalViewDto.availCITimeFrames, function(availCITimeFrame) {
          var availStartTimeDayOfWeek = availCITimeFrame.dayOfTheWeek.substr(0, 3);
          availStartTimeDayOfWeek = availStartTimeDayOfWeek.charAt(0).toUpperCase() + availStartTimeDayOfWeek.slice(1).toLowerCase();
          var availItemObj = composeAvailabilityItem(availCITimeFrame.availType, availCITimeFrame.timeFrameInstances[0]);
          $scope.data.employeeCalendarAvailItems[availStartTimeDayOfWeek].push(availItemObj);
        });
        rowCount = _.max($scope.data.employeeCalendarAvailItems, function(dayItems, day) {
          return dayItems.length;
        }).length;
        $scope.data.employeeCalendarAvailItemsRowHeight = Math.max(26, (26 * rowCount)) + 'px';
        $scope.data.employeeCalendarAvailPrefItemsRowHeight = Math.max(26, (26 * rowCount));

        // Draw Preference
        angular.forEach(response.data.availcalViewDto.prefCDTimeFrames, function(prefCDTimeFrame) {
          var prefStartTimeDayOfWeek = moment.tz(prefCDTimeFrame.startDateTime, timezone).format('ddd');
          var prefItemObj = composePreferenceItem(prefCDTimeFrame.prefType, prefCDTimeFrame);
          $scope.data.employeeCalendarPrefItems[prefStartTimeDayOfWeek].push(prefItemObj);
        });
        angular.forEach(response.data.availcalViewDto.prefCITimeFrames, function(prefCITimeFrame) {
          var prefStartTimeDayOfWeek = prefCITimeFrame.dayOfTheWeek.substr(0, 3);
          prefStartTimeDayOfWeek = prefStartTimeDayOfWeek.charAt(0).toUpperCase() + prefStartTimeDayOfWeek.slice(1).toLowerCase();
          var prefItemObj = composePreferenceItem(prefCITimeFrame.prefType, prefCITimeFrame.timeFrameInstances[0]);
          $scope.data.employeeCalendarPrefItems[prefStartTimeDayOfWeek].push(prefItemObj);
        });
        rowCount = _.max($scope.data.employeeCalendarPrefItems, function(dayItems, day) {
          return dayItems.length;
        }).length;
        $scope.data.employeeCalendarPrefItemsRowHeight = Math.max(26, (26 * rowCount)) + 'px';
        $scope.data.employeeCalendarAvailPrefItemsRowHeight += Math.max(26, (26 * rowCount));
        $scope.data.employeeCalendarAvailPrefItemsRowHeight += 'px';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });

      dataService.getEmployeeDetails($scope.data.selectedEligibleEmployee.id).then(function(response) {
        $scope.data.selectedEligibleEmployee.name = response.data.firstName + response.data.lastName;
        $scope.data.selectedEligibleEmployee.homePhone = (response.data.homePhone)? response.data.homePhone: 'N/A';
        $scope.data.selectedEligibleEmployee.cellPhone = (response.data.mobilePhone)? response.data.mobilePhone: 'N/A';
        $scope.data.selectedEligibleEmployee.email = (response.data.workEmail)? response.data.workEmail: 'N/A';
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };

    var initializeCalendarHeaderCells = function() {
      var startDayOfWeek = moment.tz(selectedSchedule.selectedWeek.start, timezone).hours(0).minutes(0).seconds(0).milliseconds(0);
      for (var i=0; i<7; i++) {
        var day = startDayOfWeek.clone().add(i, 'days');
        $scope.data.employeeCalendarHeaderCells.push({dayOfWeek: day.format('ddd'), date: day.format('M/D'), timestamp: day.unix() * 1000});
      }
    };

    var initializeCalendarCells = function() {
      if ($scope.data.selectedOperationType !== 'fillShift') {
        return;
      }

      $scope.data.employeeCalendarShifts = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
      $scope.data.employeeCalendarAvailItems = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };
      $scope.data.employeeCalendarPrefItems = { Sun: [], Mon: [], Tue: [], Wed: [], Thu: [], Fri: [], Sat: [] };

      var shiftStartTimeDayOfWeek = moment.tz(selectedOpenShift.start, timezone).format('ddd');
      var startTimeStr = moment.tz(selectedOpenShift.start, timezone).format('h:mma');
      startTimeStr = startTimeStr.substr(0, startTimeStr.length - 1);
      var endTimeStr = moment.tz(selectedOpenShift.end, timezone).format('h:mma');
      endTimeStr = endTimeStr.substr(0, endTimeStr.length - 1);

      var shiftObj = {
        type: 'new-shift',
        start: selectedOpenShift.start,
        end: selectedOpenShift.end,
        timeStr: startTimeStr + '-' + endTimeStr,
        team: selectedOpenShift.teamName,
        skill: selectedOpenShift.skillName,
        skillAbbrev: selectedOpenShift.skillAbbrev,
        className: ''
      };
      $scope.data.employeeCalendarShifts[shiftStartTimeDayOfWeek] = [shiftObj];
    };

    $scope.submit = function() {
      var actionStr = '';
      var employeeId = null;

      if ($scope.data.selectedOperationType === 'postShift') {
        actionStr = 'PostShift';
      } else if ($scope.data.selectedOperationType === 'fillShift') {
        actionStr = 'FillShift';
        employeeId = $scope.data.selectedEligibleEmployee.id;
      }

      var result = {
        shiftId: selectedOpenShift.id,
        action: actionStr,
        employeeId: employeeId
      };
      $modalInstance.close(result);
    };

    $scope.close = function() {
      $modalInstance.dismiss('cancel');
    };

    initializeCalendarHeaderCells();
    $scope.data.selectedOpenShift.dateStr = moment.tz(selectedOpenShift.start, timezone).format('MMMM DD');
    $scope.data.selectedOpenShift.startTimeStr = moment.tz(selectedOpenShift.start, timezone).format('h:mma');
    $scope.data.selectedOpenShift.endTimeStr = moment.tz(selectedOpenShift.end, timezone).format('h:mma');


    // Delete Open Shift

    $scope.deleteOS = function() {
      dataService.deleteOS(scheduleId, selectedOpenShift.id).then( function(res) {
        applicationContext.setNotificationMsgWithValues('{{ "app.DELETED_SUCCESSFULLY" | translate }}', 'success', true);
        var result = {
          shiftId: selectedOpenShift.id,
          action: 'deleteShift'
        };
        $modalInstance.close(result);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
      });
    };
  }]);
