angular.module('emlogis.commonDirectives').controller('CopyAvailabilityModal',
  ['$scope', '$modalInstance', '$filter', '$timeout', 'dataService', 'applicationContext',
    'employeeId', 'datesSelected', 'dateRangeStart', 'dateRangeEnd', 'timeZone', 'appFunc',
    function ($scope, $modalInstance, $filter, $timeout, dataService, applicationContext,
              employeeId, datesSelected, dateRangeStart, dateRangeEnd, timeZone, appFunc) {

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

      $scope.minDate = new Date(datesSelected[0]);
      $scope.maxDate = moment($scope.minDate).add(1, 'y').toDate();
      $scope.datesToDisplay = $scope.datesToDisplay.join(", ");

      $scope.startDate = {
        model: $scope.minDate,
        opened: false,
        open: function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.startDate.opened = true;
        }
      };
      
      $scope.endDate = {
        model: $scope.maxDate,
        opened: false,
        open: function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.endDate.opened = true;
        }
      };

      $scope.copyAvailability = true;
      $scope.copyPreference = true;
      $scope.repeatOptions = [{id: "EVERY_WEEK", label: $filter('translate')("availability.EVERY_WEEK")},
                              {id: "EVERY_OTHER_WEEK", label: $filter('translate')("availability.EVERY_OTHER_WEEK")},
                              {id: "EVERY_THIRD_WEEK", label: $filter('translate')("availability.EVERY_THIRD_WEEK")}];
      $scope.repeat = $scope.repeatOptions[0];
      $scope.repeatText = "REPEAT";

      $scope.copyEmployeeAvailability = function() {

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

        dataService.copyEmployeeAvailability(employeeId, dateRangeStart, dateRangeEnd, {
          selectedDates: datesSelected,
          effectiveStartDate: startDate,
          effectiveEndDate: endDate,
          availability: $scope.copyAvailability,
          preference: $scope.copyPreference,
          repeat: $scope.repeat.id
        }).then(function(res) {
          applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          $modalInstance.close(res.data);
        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };

    }]);