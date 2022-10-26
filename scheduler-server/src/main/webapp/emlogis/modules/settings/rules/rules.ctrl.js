(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesCtrl', ['$scope', 'applicationContext', 'timeZones', 'authService', 'countriesList',
    function ($scope, applicationContext, timeZones, authService, countriesList) {

      // Countries list
      $scope.countriesList = countriesList;

      //--------------------------------------------------------------------
      // Defaults for Rules module
      //--------------------------------------------------------------------

      $scope.tabs = [
        {heading: "rules.GENERAL", route: 'authenticated.rules.general'},
        {heading: "rules.SITE_TEAMS", route: 'authenticated.rules.site_teams'},
        {heading: "rules.SKILLS", route: 'authenticated.rules.skills'},
        //{heading: "rules.LICENSES_CERTIFICATIONS", route: 'authenticated.rules.licenses'},
        {heading: "rules.HOLIDAYS", route: 'authenticated.rules.holidays'}
      ];

      $scope.timeZones = timeZones;
      $scope.username = applicationContext.getUsername();
      $scope.msg = applicationContext.getNotificationMsg();

      $scope.page = {
        editing: false,
        submitted: false
      };


      //--------------------------------------------------------------------
      // Update editing status
      //--------------------------------------------------------------------

      $scope.updateEditing = function () {
        console.log('updateEditing triggered, editing is ', $scope.page.editing);
        if ($scope.page.editing !== true) {
          $scope.page.editing = true;
          applicationContext.setNotificationMsgWithValues('Don\'t forget to save your changes', 'save', true, $scope.username);
        }
      };

      //check next permissions for save,update operations
      $scope.hasMgmtPermission = function () {
        return authService.hasPermission(['SystemConfiguration_Mgmt','Demand_Mgmt']);
      };
    }]);


  //--------------------------------------------------------------------
  // Custom filters for Rules module
  //--------------------------------------------------------------------

  rules.filter('addDaysToTranslate', function () {
    return function (number) {
      return number + "_DAYS";
    };
  });

  rules.filter('addMinutesToTranslate', function () {
    return function (number) {
      return number + "_MINUTES";
    };
  });

  rules.filter('toTranslate', function () {
    return function (val, transaltePath) {
      return transaltePath + val;
    };
  });

  rules.filter('hideMinus', function () {
    return function (number) {
      return number > -1 ? number : '';
    };
  });

  // Shift Lengths

  rules.filter('displayAsHoursMinutes', function () {
    return function (minutes) {
      var hours = Math.floor(minutes / 60);
      var mins = minutes % 60;
      return hours + 'h ' + (mins === 0 ? '' : mins + 'm');
    };
  });

  rules.filter('displayAsHMM', function () {
    return function (minutes) {
      var hours = Math.floor(minutes / 60);
      var mins = minutes % 60 + '';
      return hours + ':' + (mins.length < 2 ? '0' + mins : mins);
    };
  });

  rules.filter('filterOutHour', function () {
    return function (input, hour) {
      var newList = [];
      angular.forEach(input, function (length) {
        if (length.fullHours === hour) {
          newList.push(length);
        }
      });
      return newList;
    };
  });

  rules.filter('minsToHours', function () {
    return function (minutes) {
      return minutes > 0 ? Math.floor(minutes / 60) : minutes;
    };
  });

  rules.filter('minsToHoursFloat', function () {
    return function (minutes) {
      return minutes > 0 ? minutes / 60 : minutes;
    };
  });

  rules.filter('hoursToMins', function () {
    return function (hours) {
      return hours > 0 ? hours * 60 : hours;
    };
  });

  // Shift Types

  rules.filter('milisecondsToTime', function () {
    return function (milliseconds) {
      return moment().startOf('day').add(milliseconds, 'milliseconds').format('h:mm a');
    };
  });

  rules.filter('startTimeToEndTime', function (milisecondsToTimeFilter) {
    return function (startTimeMs, shiftLengthLengthInMin) {
      // Convert and calculate End Time in ms
      var endTimeMs = shiftLengthLengthInMin * 60 * 1000 + startTimeMs;

      // If End Time overlaps midnight
      var midnightMs = 24 * 60 * 60 * 1000;
      var endTimeMsCalc = endTimeMs >= midnightMs ? endTimeMs - midnightMs : endTimeMs;

      // Format it as 'h:mm a'
      var formatted = milisecondsToTimeFilter(endTimeMsCalc);
      var arr = formatted.split(' ');

      // If End Time overlaps midnight - add '+'
      return endTimeMs >= midnightMs ? arr.join('+ ') : arr.join(' ');
    };
  });

  // for adding a new custom Type using momentJS

  rules.filter('startTimeToEndTimeMoment', function () {
    return function (startDate, shiftLengthLength) {
      return moment(startDate).add(shiftLengthLength, 'm').format('HH:mm');
    };
  });

  rules.filter('startTimeMomentFormat', function () {
    return function (startDate) {
      return moment(startDate).format('HH:mm');
    };
  });

})();