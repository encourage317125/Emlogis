(function () {
  "use strict";

  var employees = angular.module('emlogis.employees');

  employees.controller('EmployeeDetailsProfileCtrl',
    ['$scope', '$state', '$filter', '$timeout', '$modal', 'applicationContext', 'authService', 'employeesDetailsService', 'appFunc', 'dataService',
    function ($scope, $state, $filter, $timeout, $modal, applicationContext, authService, employeesDetailsService, appFunc, dataService) {

      //--------------------------------------------------------------------
      // On Ctrl load
      //--------------------------------------------------------------------

      var profile = this;
      profile.employee = null;
      profile.allSiteTeams = [];

      profile.isEditing = false;
      profile.updatesCounter = 0;
      profile.isWageClosed = true;

      profile.genderOptions = ['0', '1', '2'];
      profile.employeeTypes = [ 'FullTime', 'PartTime' ];
      profile.activityTypes = [ 'Active', 'Inactive', 'Pooled' ];

      profile.picture = null;
      profile.pictureToLoad = null;
      profile.isPictureDisplayed = false;

      profile.hireDate = {
        isOpened: false,
        openDatepicker: function($event) {

          if(profile.hireDate.isOpened) {
            // Prevent datepicker from closing this calendar popup
            $event.preventDefault();
            $event.stopPropagation();

          } else {
            // Delay opening until next tick,
            // otherwise calendar popup will be immediately closed
            $timeout(function() {
              profile.hireDate.isOpened = true;
            });
          }
        }
      };
      profile.startDate = {
        isOpened: false,
        openDatepicker: function($event) {

          if(profile.startDate.isOpened) {
            // Prevent datepicker from closing this calendar popup
            $event.preventDefault();
            $event.stopPropagation();

          } else {
            // Delay opening until next tick,
            // otherwise calendar popup will be immediately closed
            $timeout(function() {
              profile.startDate.isOpened = true;
            });
          }
        }
      };
      profile.endDate = {
        isOpened: false,
        openDatepicker: function($event) {

          if(profile.endDate.isOpened) {
            // Prevent datepicker from closing this calendar popup
            $event.preventDefault();
            $event.stopPropagation();

          } else {
            // Delay opening until next tick,
            // otherwise calendar popup will be immediately closed
            $timeout(function() {
              profile.endDate.isOpened = true;
            });
          }
        }
      };



      employeesDetailsService.getEmployeeDetails($state.params.id).then(function(employee){
        updateProfileModel(employee);

        employeesDetailsService.getEmployeePicture(employee).then(function(picture){
          updateProfilePicture(picture);
        });
      });



      var updateProfileModel = function(employee){
        profile.employee = employee;
        //console.log('+++ profile.employee updated', profile.employee);
        //console.log('1 employeeObj.hireDate', moment(employee.hireDate).format() );
        //console.log('1 utc employeeObj.hireDate', moment.tz(employee.hireDate, 'UTC').format() );
      };

      var updateProfilePicture = function(picture){
        profile.picture = picture;
        profile.isPictureDisplayed = picture.size > 0;
      };


      //
      // check if user has permission to edit wages

      profile.hasWagePermission = function () {
        return authService.hasPermission('EmployeeWages_Mgmt');
      };

      profile.displayWage = function(){
        profile.isWageClosed = false;
      };



      //--------------------------------------------------------------------
      // CRUD
      //--------------------------------------------------------------------


      profile.deleteEmployee = function(){
        employeesDetailsService.deleteEmployee(profile.employee).
          then(function(employee){
            updateProfileModel(employee);
            $state.go("authenticated.employees.list");
          });
      };



      //
      // on Save button click in Profile section
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
            employeesDetailsService.updateEmployeeAccount(employeeForEdit, newLogin).then( function(data){
              profile.updatesCounter--;
            });
          }
        } else if ( employeeForEdit.userAccountDto ) {
          newLogin = { login: employeeForEdit.userAccountDto.login };
          profile.updatesCounter++;
          employeesDetailsService.updateEmployeeAccount(employeeForEdit, newLogin).then( function(data){
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
            employeesDetailsService.updateEmployeeHomeTeam(employeeForEdit, employeeForEdit.homeTeam.teamId, putDto)
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
            employeesDetailsService.addHomeTeamToEmployee(employeeForEdit, employeeForEdit.homeTeam.teamId, assosiateDto)
              .then(function(res){
                profile.updatesCounter--;
              })
            ;
          }
        }


        // 3. Check if details were changed

        var prepareEmployeeDetailsDto = function(employeeObj){
          //console.log('2 employeeObj.hireDate', employeeObj.hireDate);
          return {
            firstName:          employeeObj.firstName,
            middleName:         employeeObj.middleName,
            lastName:           employeeObj.lastName,
            employeeIdentifier: employeeObj.employeeIdentifier,
            professionalLabel:  employeeObj.professionalLabel,
            gender:             employeeObj.gender,
            homePhone:          employeeObj.homePhone || null,
            mobilePhone:        employeeObj.mobilePhone || null,
            homeEmail:          employeeObj.homeEmail || null,
            workEmail:          employeeObj.workEmail || null,
            hireDate:           employeeObj.hireDate,
            startDate:          employeeObj.startDate,
            endDate:            employeeObj.endDate,
            hourlyRate:         employeeObj.hourlyRate,
            employeeType:       employeeObj.employeeType,
            activityType:       employeeObj.activityType
          };
        };

        var oldEmployee = prepareEmployeeDetailsDto(profile.employee);
        var newEmployee = prepareEmployeeDetailsDto(employeeForEdit);

        if ( !angular.equals(oldEmployee, newEmployee) ) {
          profile.updatesCounter++;
          console.log('newEmployee', newEmployee);
          employeesDetailsService.updateEmployee(employeeForEdit, newEmployee).then(function(res){
            profile.updatesCounter--;
            console.log(res);
          });
        }

        // 4. Wait for all API calls to be resolved,
        // then update page options and get updated employee

        var removeThisWatcher = $scope.$watch("profile.updatesCounter", function(newVal, oldVal) {
          if (newVal === 0 && oldVal === 1) {
            employeesDetailsService.getEmployeeDetails(employeeForEdit.id).then(function(employee){
              applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
              updateProfileModel(employee);
              profile.isEditing = false;
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            });
            removeThisWatcher();                                           // remove this $watch
          }
        });
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

            employeesDetailsService.uploadEmployeePicture(profile.employee, img).then(function(picture){
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


      //
      // delete Employee picture

      profile.deletePicture = function(){
        var img = profile.picture;
        img.dto = { image: null };
        employeesDetailsService.uploadEmployeePicture(profile.employee, img).then(function(picture){
          updateProfilePicture(picture);
        });
      };


      //--------------------------------------------------------------------
      // Editing process
      //--------------------------------------------------------------------


      profile.startEditing = function () {
        employeesDetailsService.getSiteTeams().then( function(data) {
          profile.allSiteTeams = data;
        });
        profile.employeeForEdit = angular.copy(profile.employee);
        profile.isEditing = true;
      };


      profile.cancelEditing = function () {
        profile.isEditing = false;
      };


      profile.isModified = function() {
        return !angular.equals(profile.employeeForEdit, profile.employee);
      };



      //--------------------------------------------------------------------
      // Emergency modal
      //--------------------------------------------------------------------

      profile.openEmergencyContactModal = function (size) {

        var modalInstance = $modal.open({
          templateUrl:  'modules/employees/partials/employee-details/empl-details-emergency-modal.html',
          controller:   'EmployeesDetailsEmergencyModalCtrl as ec',
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
    }
  ]);

  employees.directive('fileModel', ['$parse', function ($parse) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var model = $parse(attrs.fileModel);
        var modelSetter = model.assign;

        element.bind('change', function(){
          scope.$apply(function(){
            modelSetter(scope, element[0].files[0]);
          });
        });
      }
    };
  }]);

})();