var profileModule = angular.module('emlogis.profile');

profileModule.controller('ProfileDetailCtrl',
  [
    '$scope',
    '$state',
    '$q',
    '$sessionStorage',
    '$filter',
    '$modal',
    'applicationContext',
    'crudDataService',
    'uiGridConstants',
    'authService',
    'profileDetailService',
    'dataService',
    function($scope, $state, $q, $sessionStorage, $filter, $modal, applicationContext, crudDataService,uiGridConstants,
             authService,profileDetailService,dataService) {

      console.log('Profile Detail controller');

      $scope.tabs[0].active = true;

      var baseUrl = applicationContext.getBaseRestUrl();
      var factory = _.clone(crudDataService);
      var profile = this;

      profile.genderOptions = ['0', '1', '2'];
      profile.employeeTypes = [ 'FullTime', 'PartTime' ];
      profile.activityTypes = [ 'Active', 'Inactive', 'Pooled' ];

      /**
       * init function
       */
      $scope.init = function() {

        profileDetailService.getDetail()
          .then(function(employee){
            updateProfileModel(employee);

            profileDetailService.getProfilePicture(employee).then(function(picture){
              updateProfilePicture(picture);
          });
        });

        dataService.getEmployeeHoursAndOvertime(profile.currentAccountInfo.employeeId).
          then(function(res) {
            $scope.settings = convertTime(fromMinutesToHours, res.data);
            $scope.options.overtimeType = defineOvertimeType();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });

      };

      //--------------------------------------------------------------------
      // EMLOGIS panel
      //--------------------------------------------------------------------

      //
      // Update Approvals settings

      $scope.updateEditingApprovals = function(){
        profile.isEditingApprovals = !angular.equals(profile.employee.autoApprovalsSettingDto, employeeInit.autoApprovalsSettingDto);
      };

      $scope.$watch(function () {
        if ( profile.employee ) {
          return profileDetailService.getProfileTeam();
        }
      }, function (newTeam, oldTeam) {
        if ( !newTeam || oldTeam === undefined || angular.equals(newTeam, oldTeam) ) {
          return;
        }
        profileDetailService.getDetail().then(function(employee){
          updateProfileModel(employee);
        });
      });



//      $scope.$watch("getEmployeeDetails()", function(details) {
//        if (!details) return;
//        $scope.hasPrimarySkill = _.some(details.skillInfo, { 'isPrimarySkill': true });
//      });



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
          return $scope.options.overtimeType && !_.includes(["daily", "dailyAndWeekly", "dailyAndBiweekly"], $scope.options.overtimeType.id);
        },
        weekly: function() {
          return $scope.options.overtimeType && !_.includes(["weekly", "dailyAndWeekly"], $scope.options.overtimeType.id);
        },
        biweekly: function() {
          return $scope.options.overtimeType && !_.includes(["biweekly", "dailyAndBiweekly"], $scope.options.overtimeType.id);
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

      function generateArrayOfNumbers(min, max, noNA) {
        var arr = noNA ? [] : [$scope.translations.na];
        for (var i = min; i <= max; i++) {
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
          daysPerWeek: settingsObj.daysPerWeek,
          consecutiveDays:   settingsObj.consecutiveDays || $scope.translations.na,
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

      $scope.updateSettings = function() {
        var settings = convertTime(fromHoursToMinutes, $scope.settings);
        dataService.updateEmployeeHoursAndOvertime($scope.currentAccountInfo.employeeId, settings).
          then(function(res) {
            applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
            $scope.options.overtimeType = defineOvertimeType();
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
      };


      var updateProfileModel = function(employee){
        profile.employee = employee;
        //console.log('+++ profile.employee updated', profile.employee);
      };

      var updateProfilePicture = function(picture){
        profile.picture = picture;
        profile.isPictureDisplayed = picture.size > 0;
      };

      //
      // delete Employee picture

      profile.deletePicture = function(){
        var img = profile.picture;
        img.dto = { image: null };
        profileDetailService.uploadEmployeePicture(profile.employee, img).then(function(picture){
          updateProfilePicture(picture);
        });
      };


      //--------------------------------------------------------------------
      // Editing process
      //--------------------------------------------------------------------


      profile.startEditing = function () {
        profileDetailService.getSiteTeams().then( function(data) {
          profile.allSiteTeams = data;
        });
        profile.employeeForEdit = angular.copy(profile.employee);
        profile.isEditing = true;
      };

      // Profile Detail
      profile.cancelEditing = function () {
        profile.isEditing = false;
      };

      //--------------------------------------------------------------------
      // Emergency modal
      //--------------------------------------------------------------------

      profile.openEmergencyContactModal = function (size) {

        var modalInstance = $modal.open({
          templateUrl:  'modules/employees/partials/employee-details/empl-details-emergency-modal.html',
          controller:   'ProfileDetailEmergencyModalCtrl as ec',
          backdrop:     'static',
          size: size,
          resolve: {
            employee: function () {
              return profile.employee;
            }
          }
        });


        modalInstance.result.then(function (employee) {
          updateProfileModel(employee);
        }, function () {
          //console.log('Modal dismissed at: ' + new Date());
        });
      };


      /**
       * Variable initialization
       */


      profile.currentAccountInfo = JSON.parse($sessionStorage.info);
      profile.isEditingApprovals = false;

      /**
       * Settings: Hours
       */

      $scope.translations = {
        min:                    $filter('translate')("employees.tabs.hours.MIN"),
        max:                    $filter('translate')("employees.tabs.hours.MAX"),
        hoursPerDay:            $filter('translate')("employees.tabs.hours.HOURS_PER_DAY"),
        hoursPerWeek:           $filter('translate')("employees.tabs.hours.HOURS_PER_WEEK"),
        daysPerWeek:            $filter('translate')("employees.tabs.hours.DAYS_PER_WEEK"),
        consecutiveDays:        $filter('translate')("employees.tabs.hours.CONSECUTIVE_DAYS"),
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
        daysPerWeek:      generateArrayOfNumbers(1, 7, true),
        overtimeType:     null,
        overtimeTypes: [
          { id: "daily",            name: $filter('translate')("employees.tabs.hours.DAILY") },
          { id: "weekly",           name: $filter('translate')("employees.tabs.hours.WEEKLY") },
          { id: "biweekly",         name: $filter('translate')("employees.tabs.hours.BIWEEKLY") },
          { id: "dailyAndWeekly",   name: $filter('translate')("employees.tabs.hours.DAILY_AND_WEEKLY") },
          { id: "dailyAndBiweekly", name: $filter('translate')("employees.tabs.hours.DAILY_AND_BIWEEKLY") },
          { id: "na",               name: $scope.translations.na }
        ]
      };

      $scope.hasPrimarySkill = false;
      $scope.hoursEditDisabled = true;
//      $scope.getProfile = profileDetailService.getEmployeeInit;

      var ot = $scope.options.overtimeTypes;

      // on Save button click in User Profile section
      // update Employee Details, Employee Login and Employee HomeTeam

      profile.saveEmployeeProfile = function(employeeForEdit){
        profile.updatesCounter = 0;

        // 1. Check if login was changed

        var newLogin;
        if ( profile.employee.userAccountDto ) {
          var isLoginChanged = !angular.equals(profile.employee.userAccountDto.login, employeeForEdit.userAccountDto.login);
          if ( isLoginChanged ) {
            profile.updatesCounter++;
            newLogin = { login: employeeForEdit.userAccountDto.login };
            profileDetailService.updateEmployeeAccount(employeeForEdit, newLogin).then( function(data){
              profile.updatesCounter--;
            });
          }
        } else if ( employeeForEdit.userAccountDto ) {
          newLogin = { login: employeeForEdit.userAccountDto.login };
          profile.updatesCounter++;
          profileDetailService.updateEmployeeAccount(employeeForEdit, newLogin).then( function(data){
            profile.updatesCounter--;
          });
        }


        // 2. Check if home team was changed

        if ( !angular.equals(profile.employee.homeTeam.teamId, employeeForEdit.homeTeam.teamId) ) {
          profile.updatesCounter++;

          // check if new home team is already assigned to an employee
          var teamIsAssosiated = (_.find(employeeForEdit.teamInfo, { 'teamId': employeeForEdit.homeTeam.teamId })) ? true : false;
          if (teamIsAssosiated) {
            var putDto = { isHomeTeam: true };
            profileDetailService.updateEmployeeHomeTeam(employeeForEdit, employeeForEdit.homeTeam.teamId, putDto)
              .then(function(res){
                profile.updatesCounter--;
              })
            ;

          } else {
            var assosiateDto = {
              isHomeTeam: true,
              teamId: employeeForEdit.homeTeam.teamId,
              isSchedulable: true,
              isFloating: false
            };
            profileDetailService.addHomeTeamToEmployee(employeeForEdit, employeeForEdit.homeTeam.teamId, assosiateDto)
              .then(function(res){
                profile.updatesCounter--;
              })
            ;
          }
        }


        // 3. Check if details were changed

        var prepareEmployeeDetailsDto = function(employeeObj){
          return {
            firstName: employeeObj.firstName,
            middleName: employeeObj.middleName,
            lastName: employeeObj.lastName,
            employeeIdentifier: employeeObj.employeeIdentifier,
            professionalLabel: employeeObj.professionalLabel,
            gender: employeeObj.gender,
            homePhone: employeeObj.homePhone,
            mobilePhone: employeeObj.mobilePhone,
            homeEmail: employeeObj.homeEmail,
            workEmail: employeeObj.workEmail,
            hireDate: employeeObj.hireDate,
            startDate: employeeObj.startDate,
            endDate: employeeObj.endDate,
            hourlyRate: employeeObj.hourlyRate,
            employeeType: employeeObj.employeeType,
            activityType: employeeObj.activityType
          };
        };

        var oldEmployee = prepareEmployeeDetailsDto(profile.employee);
        var newEmployee = prepareEmployeeDetailsDto(employeeForEdit);

        if ( !angular.equals(oldEmployee, newEmployee) ) {
          profile.updatesCounter++;
          profileDetailService.updateEmployee(employeeForEdit, newEmployee).then(function(res){
            profile.updatesCounter--;
            //console.log(res);
          });
        }

        // 4. Wait for all API calls to be resolved,
        // then update page options and get updated employee

        //var removeThisWatcher = $scope.$watch("profile.updatesCounter", function(newVal, oldVal) {
        //  if (newVal === 0 && oldVal === 1) {
        //    profileDetailService.getEmployeeDetails(employeeForEdit.id).then(function(employee){
        //      applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        //      updateProfileModel(employee);
        //      profile.isEditing = false;
        //    }, function(err) {
        //      applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        //    });
        //    removeThisWatcher();                                           // remove this $watch
        //  }
        //});
      };

      //
      // Upload Employee picture

      profile.uploadPicture = function($event){
        var evt = $event,
          img = profile.pictureToLoad,
          reader = new FileReader();

        // If we use onloadend, we need to check the readyState.
        reader.onloadend = function(evt) {
          if (evt.target.readyState == FileReader.DONE) { // DONE == 2
            var imageBytes, commaIndex, image, dto;

            imageBytes = evt.target.result;
            commaIndex = imageBytes.indexOf(',') + 1;
            image = imageBytes.slice(commaIndex);
            img.dto = { image: image };

            profileDetailService.uploadEmployeePicture(profile.employee, img).then(function(picture){
              updateProfilePicture(picture);
            });
          }
        };
        reader.readAsDataURL(img);
      };

      //
      // when new picture is selected
      // upload Employee picture

      $scope.$watch('profile.pictureToLoad', function(newVal){
        if (newVal) {
          profile.uploadPicture();
        }
      });


    }
  ]
);