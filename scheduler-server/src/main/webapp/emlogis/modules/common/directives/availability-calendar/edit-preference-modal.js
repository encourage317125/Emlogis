angular.module('emlogis.commonDirectives').controller('EditPreferenceModal',
  ['$scope', '$modalInstance', 'dataService', 'applicationContext', 'employeeId', 'availabilityType',
    'datesSelected', 'dateRangeStart', 'dateRangeEnd', 'timeZone', 'appFunc',
    function ($scope, $modalInstance, dataService, applicationContext, employeeId, availabilityType,
              datesSelected, dateRangeStart, dateRangeEnd, timeZone, appFunc) {

      $scope.availabilityType = availabilityType;
      $scope.datesSelected = datesSelected;
      $scope.datesToDisplay = "";

      if (availabilityType === "ci") {
        $scope.datesToDisplay = _.map(datesSelected, function(d) {
          return _.capitalize(d) + "s";
        });
      } else {
        $scope.datesToDisplay = _.map(datesSelected, function(d) {
          return moment(d).format("M/D");
        });
        datesSelected = _.map(datesSelected, function(d) {
          var date = moment(d);
          return appFunc.getDateWithTimezone(
            date.year(),
            date.month(),
            date.date(),
            timeZone
          ).getTime();
        });
      }

      $scope.datesToDisplay = $scope.datesToDisplay.join(", ");

      $scope.startDate = {
        model: new Date(),
        opened: false,
        open: function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.startDate.opened = true;
        }
      };
      
      $scope.endDate = {
        model: null,
        opened: false,
        open: function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.endDate.opened = true;
        }
      };

      $scope.action = "PREFER_DAY";

      $scope.preferTimeframes = [{
        startTime: moment().hour(8).minute(0).toDate(),
        endTime:  moment().hour(16).minute(0).toDate()
      }];

      $scope.avoidTimeframes = [{
        startTime: moment().hour(8).minute(0).toDate(),
        endTime:  moment().hour(16).minute(0).toDate()
      }];

      registerTimeFrameCorrector("preferTimeframes", 0);
      registerTimeFrameCorrector("avoidTimeframes", 0);

      function registerTimeFrameCorrector(timeFrameName, index) {
        $scope.$watch(timeFrameName + "[" + index + "]" + ".startTime", function(startTime) {
          if (!startTime) return;
          if ($scope[timeFrameName][index].endTime < startTime) {
            $scope[timeFrameName][index].endTime = startTime;
          }
        });

        $scope.$watch(timeFrameName + "[" + index + "]" + ".endTime", function(endTime) {
          if (!endTime) return;

          if (endTime.getHours() === 0 && endTime.getMinutes() === 0) return;

          if ($scope[timeFrameName][index].startTime > endTime) {
            $scope[timeFrameName][index].startTime = endTime;
          }
        });
      }

      $scope.addTimeframe = function(type) {
        var timeframes, timeFrameName;

        if (type === "prefer") {
          timeframes = $scope.preferTimeframes;
          timeFrameName = "preferTimeframes";
        } else {
          timeframes = $scope.avoidTimeframes;
          timeFrameName = "avoidTimeframes";
        }

        if (timeframes.length < 3) {
          timeframes.push({
            startTime: moment().hour(8).minute(0).toDate(),
            endTime:  moment().hour(16).minute(0).toDate()
          });
          registerTimeFrameCorrector(timeFrameName, timeframes.length - 1);
        }
      };

      $scope.removeTimeframe = function(type, index) {
        var timeframes = type === "prefer" ? $scope.preferTimeframes : $scope.avoidTimeframes;
        timeframes.splice(index, 1);
      };

      $scope.updatePreference = function() {

        var avoidTimeFrames, preferTimeFrames;

        if ($scope.action === "preferTimeframe") {
          preferTimeFrames = _.map($scope.preferTimeframes, function(t) {
            return {
              startTime: moment.duration({ minutes: t.startTime.getMinutes(), hours: t.startTime.getHours()}).asMilliseconds(),
              endTime: moment.duration({ minutes: t.endTime.getMinutes(), hours: t.endTime.getHours()}).asMilliseconds()
            };
          });
        }

        if ($scope.action === "avoidTimeframe") {
          avoidTimeFrames = _.map($scope.avoidTimeframes, function(t) {
            return {
              startTime: moment.duration({ minutes: t.startTime.getMinutes(), hours: t.startTime.getHours()}).asMilliseconds(),
              endTime: moment.duration({ minutes: t.endTime.getMinutes(), hours: t.endTime.getHours()}).asMilliseconds()
            };
          });
        }

        if ($scope.availabilityType === "cd") {
          dataService.updateEmployeeCDPreference(employeeId, dateRangeStart, dateRangeEnd, {
            action: ($scope.action === "preferTimeframe" || $scope.action === "avoidTimeframe") ? "TIMEFRAMES" : $scope.action,
            preferTimeFrames: preferTimeFrames,
            avoidTimeFrames: avoidTimeFrames,
            selectedDates: datesSelected
          }).then(function(res) {
            applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
            $modalInstance.close(res.data);
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            $modalInstance.dismiss('cancel');
          });
        }
        else {
          var startDate = appFunc.getDateWithTimezone(
              $scope.startDate.model.getFullYear(),
              $scope.startDate.model.getMonth(),
              $scope.startDate.model.getDate(),
              timeZone
            ).valueOf(),
            endDate = $scope.endDate.model && appFunc.getDateWithTimezone(
                $scope.endDate.model.getFullYear(),
                $scope.endDate.model.getMonth(),
                $scope.endDate.model.getDate(),
                timeZone
              ).valueOf();
          dataService.updateEmployeeCIPreference(employeeId, dateRangeStart, dateRangeEnd, {
            action: ($scope.action === "preferTimeframe" || $scope.action === "avoidTimeframe") ? "TIMEFRAMES" : $scope.action,
            preferTimeFrames: preferTimeFrames,
            avoidTimeFrames: avoidTimeFrames,
            effectiveStartDate: startDate,
            effectiveEndDate: endDate,
            "selectedDays": {
              "sunday":     _.contains(datesSelected, "sunday"),
              "monday":     _.contains(datesSelected, "monday"),
              "tuesday":    _.contains(datesSelected, "tuesday"),
              "wednesday":  _.contains(datesSelected, "wednesday"),
              "thursday":   _.contains(datesSelected, "thursday"),
              "friday":     _.contains(datesSelected, "friday"),
              "saturday":   _.contains(datesSelected, "saturday")
            }
          }).then(function(res) {
            applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
            $modalInstance.close(res.data);
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            $modalInstance.dismiss('cancel');
          });
        }
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };

    }]);