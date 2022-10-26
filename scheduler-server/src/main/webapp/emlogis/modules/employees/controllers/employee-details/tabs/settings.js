(function () {
  "use strict";

  angular.module('emlogis.employees').controller('EmployeeDetailsSettingsCtrl',
    ['$scope', '$state', '$filter', '$timeout', 'dataService', 'applicationContext', 'employeesDetailsService',
      function ($scope, $state, $filter, $timeout, dataService, applicationContext, employeesDetailsService) {

        $scope.employeeId = $state.params.id;

        $scope.translations = {
          min:                    $filter('translate')("employees.tabs.hours.MIN"),
          max:                    $filter('translate')("employees.tabs.hours.MAX"),
          hoursPerDay:            $filter('translate')("employees.tabs.hours.HOURS_PER_DAY"),
          hoursPerWeek:           $filter('translate')("employees.tabs.hours.HOURS_PER_WEEK"),
          maxDaysPerWeek:         $filter('translate')("employees.tabs.hours.MAX_DAYS_PER_WEEK"),
          maxConsecutiveDays:     $filter('translate')("employees.tabs.hours.MAX_CONSECUTIVE_DAYS"),
          primarySkillHours:      $filter('translate')("employees.tabs.hours.PRIMARY_SKILL_HOURS"),
          overtime:               $filter('translate')("employees.tabs.hours.OVERTIME"),
          dailyOvertimeStarts:    $filter('translate')("employees.tabs.hours.DAILY_OVERTIME_STARTS"),
          weeklyOvertimeStarts:   $filter('translate')("employees.tabs.hours.WEEKLY_OVERTIME_STARTS"),
          beweeklyOvertimeStarts: $filter('translate')("employees.tabs.hours.BIWEEKLY_OVERTIME_STARTS"),
          beweeklyMinHours:       $filter('translate')("employees.tabs.hours.BIWEEKLY_MIN_HOURS"),
          na:                     $filter('translate')("employees.tabs.hours.NA")
        };

        $scope.settings = {
          daysPerWeek: 0,
          consecutiveDays: 0,
          minHoursPerDay: 0,
          maxHoursPerDay: 0,
          minHoursPerWeek: 0,
          maxHoursPerWeek: 0,
          primarySkillHours: 0,
          overtimeDto: {
            dailyOvertimeMins: 0,
            weeklyOvertimeMins: 0,
            biweeklyOvertimeMins: 0
          }
        };

        $scope.options = {
          minHoursPerDay:   generateArrayOfNumbers(1, 12),
          maxHoursPerDay:   generateArrayOfNumbers(1, 24),
          minHoursPerWeek:  generateArrayOfNumbers(1, 168), // 7 days in hours
          maxHoursPerWeek:  generateArrayOfNumbers(1, 168),
          hoursPer2Weeks:   generateArrayOfNumbers(1, 336), // 14 days in hours
          daysPerWeek:      generateArrayOfNumbers(1, 7),
          overtimeType:     null,
          overtimeTypes: [
            { id: "daily",            name: $filter('translate')("employees.tabs.hours.DAILY") },
            { id: "weekly",           name: $filter('translate')("employees.tabs.hours.WEEKLY") },
            { id: "biweekly",         name: $filter('translate')("employees.tabs.hours.BIWEEKLY") },
            { id: "dailyAndWeekly",   name: $filter('translate')("employees.tabs.hours.DAILY_AND_WEEKLY") },
            { id: "dailyAndBiweekly", name: $filter('translate')("employees.tabs.hours.DAILY_AND_BIWEEKLY") },
            { id: "na",               name: $scope.translations.na }
          ],
          dailyOvertime:   generateArrayOfNumbers(1, 24, 0.5),
          weeklyOvertime:  generateArrayOfNumbers(1, 168, 0.5),
          biweeklyOvertime:  generateArrayOfNumbers(1, 336, 0.5)
        };

        $scope.hasPrimarySkill = false;
        $scope.getEmployeeDetails = employeesDetailsService.getEmployeeInit;

        $scope.$watch("getEmployeeDetails()", function(details) {
          if (!details) return;
          $scope.hasPrimarySkill = _.some(details.skillInfo, { 'isPrimarySkill': true });
        });

        var ot = $scope.options.overtimeTypes;

        function defineOvertimeType() {
          var sets = $scope.settings.overtimeDto,
              na = $scope.translations.na;
          if (sets.dailyOvertimeMins != na && sets.weeklyOvertimeMins != na)   return _.find(ot, 'id', "dailyAndWeekly");
          if (sets.dailyOvertimeMins != na && sets.biweeklyOvertimeMins != na) return _.find(ot, 'id', "dailyAndBiweekly");
          if (sets.dailyOvertimeMins != na)    return _.find(ot, 'id', "daily");
          if (sets.weeklyOvertimeMins != na)   return _.find(ot, 'id', "weekly");
          if (sets.biweeklyOvertimeMins != na) return _.find(ot, 'id', "biweekly");
          return _.find(ot, 'id', "na");
        }

        $scope.disableOvertimeInput = {
          daily: function() {
            var disabled = $scope.options.overtimeType && !_.includes(["daily", "dailyAndWeekly", "dailyAndBiweekly"], $scope.options.overtimeType.id);
            if (disabled) {
              $scope.settings.overtimeDto.dailyOvertimeMins = $scope.translations.na;
            }
            return disabled;
          },
          weekly: function() {
            var disabled = $scope.options.overtimeType && !_.includes(["weekly", "dailyAndWeekly"], $scope.options.overtimeType.id);
            if (disabled) {
              $scope.settings.overtimeDto.weeklyOvertimeMins = $scope.translations.na;
            }
            return disabled;
          },
          biweekly: function() {
            var disabled = $scope.options.overtimeType && !_.includes(["biweekly", "dailyAndBiweekly"], $scope.options.overtimeType.id);
            if (disabled) {
              $scope.settings.overtimeDto.biweeklyOvertimeMins = $scope.translations.na;
            }
            return disabled;
          }
        };

        $scope.onMaxHoursChange = function(minHoursProp, maxHoursProp) {
          $timeout(function() {
            if ($scope.settings[maxHoursProp] === $scope.translations.na) return;
            if ($scope.settings[minHoursProp] > $scope.settings[maxHoursProp]) {
              $scope.settings[minHoursProp] = $scope.settings[maxHoursProp];
            }
          });
        };

        $scope.onMinHoursChange = function(minHoursProp, maxHoursProp) {
          $timeout(function() {
            if ($scope.settings[minHoursProp] === $scope.translations.na) return;
            if ($scope.settings[minHoursProp] > $scope.settings[maxHoursProp]) {
              $scope.settings[maxHoursProp] = $scope.settings[minHoursProp];
            }
          });
        };

        function generateArrayOfNumbers(min, max, step, noNA) {
          var arr = noNA ? [] : [$scope.translations.na];
          step = step || 1;
          for (var i = min; i <= max; i=i+step) {
            arr.push(i);
          }
          return arr;
        }

        function fromMinutesToHours(minutes) {
          return minutes <= 0 ? $scope.translations.na : (minutes%60 === 0 ? minutes/60 : (minutes/60).toFixed(1));
        }

        function fromHoursToMinutes(hours) {
          return hours == $scope.translations.na ? -1 : hours*60;
        }

        function convertTime(convertFn, settingsObj) {
          return {
            daysPerWeek:       settingsObj.daysPerWeek,
            consecutiveDays:   settingsObj.consecutiveDays,
            minHoursPerDay:    convertFn(settingsObj.minHoursPerDay),
            maxHoursPerDay:    convertFn(settingsObj.maxHoursPerDay),
            minHoursPerWeek:   convertFn(settingsObj.minHoursPerWeek),
            maxHoursPerWeek:   convertFn(settingsObj.maxHoursPerWeek),
            primarySkillHours: convertFn(settingsObj.primarySkillHours),
            overtimeDto: {
              dailyOvertimeMins:    convertFn(settingsObj.overtimeDto.dailyOvertimeMins),
              weeklyOvertimeMins:   convertFn(settingsObj.overtimeDto.weeklyOvertimeMins),
              biweeklyOvertimeMins: convertFn(settingsObj.overtimeDto.biweeklyOvertimeMins)
            }
          };
        }

        function validOvertime(overtimeTypeId, overtimeDto) {
          return overtimeTypeId === "daily" && overtimeDto.dailyOvertimeMins > 0 ||
                 overtimeTypeId === "weekly" && overtimeDto.weeklyOvertimeMins > 0 ||
                 overtimeTypeId === "biweekly" && overtimeDto.biweeklyOvertimeMins > 0 ||
                 overtimeTypeId === "dailyAndWeekly" && overtimeDto.dailyOvertimeMins > 0 && overtimeDto.weeklyOvertimeMins > 0 ||
                 overtimeTypeId === "dailyAndBiweekly" && overtimeDto.dailyOvertimeMins > 0 && overtimeDto.biweeklyOvertimeMins > 0 ||
                 overtimeTypeId === "na";
        }

        $scope.updateSettings = function() {
          var settings = convertTime(fromHoursToMinutes, $scope.settings);

          if (!validOvertime($scope.options.overtimeType.id, settings.overtimeDto)) {
            applicationContext.setNotificationMsgWithValues("Please specify overtime values for selected overtime type", 'danger', true);
            return;
          }

          settings.daysPerWeek = $scope.settings.daysPerWeek === $scope.translations.na ? -1 : $scope.settings.daysPerWeek;
          settings.consecutiveDays = $scope.settings.consecutiveDays === $scope.translations.na ? -1 : $scope.settings.consecutiveDays;

          dataService.updateEmployeeHoursAndOvertime($scope.employeeId, settings).
            then(function(res) {
              applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
              $scope.options.overtimeType = defineOvertimeType();
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            });
        };

        dataService.getEmployeeHoursAndOvertime($scope.employeeId).
            then(function(res) {
              $scope.settings = convertTime(fromMinutesToHours, res.data);

              $scope.settings.daysPerWeek = $scope.settings.daysPerWeek > 0 ? $scope.settings.daysPerWeek : $scope.translations.na;
              $scope.settings.consecutiveDays = $scope.settings.consecutiveDays > 0 ? $scope.settings.consecutiveDays : $scope.translations.na;

              $scope.options.overtimeType = defineOvertimeType();
            }, function(err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });

    }]);
})();