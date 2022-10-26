(function() {
  "use strict";

  // Service
  // Create service function

  var employeesDetailsService = function($filter, $cacheFactory, $q, dialogs, applicationContext, appFunc, dataService){

    var eds = this,
        employee,
        employeeInit,
        employeeSiteTimeZone,
        employeeCache = $cacheFactory('employeeCache'),
        pictureCache = $cacheFactory('pictureCache'),
        allOrgSkillsCache = $cacheFactory('allOrgSkillsCache'),
        allSiteTeamsCache = $cacheFactory('allSiteTeamsCache'),
        sitesTeamsTreeCache = $cacheFactory('sitesTeamsTreeCache'),
        abcOrder = { orderby: 'name', orderdir: 'ASC' };


    //--------------------------------------------------------------------
    // Cached Employee Details
    //--------------------------------------------------------------------


    //
    // get Employee details for managers view

    eds.getEmployeeDetails = function(employeeId){
      return dataService.getEmployeeDetails(employeeId, { cache: employeeCache })
        .then(function(res){
          employeeInit = res.data;
          employeeInit.cacheId = res.config.url;
          console.log('got hireDate', employeeInit.hireDate);
          console.log('got hireDate UTC ms', moment.tz(employeeInit.hireDate, 'UTC').valueOf());

          employeeInit.primarySkill = findPrimarySkill(employeeInit.skillInfo);
          employeeInit.homeTeam = findHomeTeam(employeeInit.teamInfo);

          employeeInit.skillInfo = $filter('orderBy')(employeeInit.skillInfo, 'name');
          employeeInit.teamInfo = $filter('orderBy')(employeeInit.teamInfo, 'name');

          employeeSiteTimeZone = employeeInit.siteInfo.timeZone;

          // Convert dates to Site time zone
          employeeInit.hireDate = appFunc.convertToBrowserTimezone(employeeInit.hireDate, employeeSiteTimeZone);
          employeeInit.startDate = appFunc.convertToBrowserTimezone(employeeInit.startDate, employeeSiteTimeZone);
          employeeInit.endDate = appFunc.convertToBrowserTimezone(employeeInit.endDate, employeeSiteTimeZone);

          employee = angular.copy(employeeInit);
          return employee;
        })
      ;
    };


    //
    // provide Initial Employee obj

    eds.getEmployeeInit = function(){
      return employeeInit;
    };


    //
    // provide Employee HomeTeam

    eds.getEmployeeTeam = function(){
      return employee.homeTeam;
    };


    //
    // provide Employee status

    eds.getEmployeeActivityType = function(){
      return employee.activityType;
    };


    //
    // clean cache that can be modified in other modules

    eds.cleanExternalCache = function(){
      allOrgSkillsCache.removeAll();
      allSiteTeamsCache.removeAll();
    };



    //--------------------------------------------------------------------
    // Employee Profile section
    //--------------------------------------------------------------------

    //
    // update an Employee

    eds.updateEmployee = function(employee, dto){
      // prepare dates for back-end format
      dto.hireDate = prepareDate(dto.hireDate);
      dto.startDate = prepareDate(dto.startDate);
      dto.endDate = prepareDate(dto.endDate);

      return dataService.updateEmployee(employee.id, dto).then(function(res){
        employeeCache.remove(employee.cacheId);                               // clear cache for this employee
        return eds.getEmployeeDetails(employee.id);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    //
    // delete an Employee

    eds.deleteEmployee = function(employee){
      var deferred = $q.defer();

      // Confirm deletion
      var question = $filter('translate')("rules.site_teams.DELETE_TEAM") + employee.firstName + ' ' + employee.lastName + '?';
      var dlg = dialogs.confirm('app.PLEASE_CONFIRM', question);

      dlg.result.then(function () {
        return dataService.deleteEmployee(employee.id).then(function(){
          applicationContext.setNotificationMsgWithValues('app.DELETED_SUCCESSFULLY', 'success', true);
          employeeCache.remove(employee.cacheId);                               // clear cache for this employee
          eds.getEmployeeDetails(employee.id).then(function(res){
            deferred.resolve(res);
          });

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        });
      });

      return deferred.promise;
    };



    // --------- Employee Account ---------


    //
    // get account info for an Employee
    // TEMP TODO is it needed?

    eds.getEmployeeAccount = function(employeeId){
      return dataService.getEmployeeAccount(employeeId).then(function(res){
        //console.log('+++ getEmployeeAccount', res);
      });
    };


    //
    // update Employee's account

    eds.updateEmployeeAccount = function(employee, dto){
      //console.log('+++ login to upd', dto);
      return dataService.updateEmployeeAccount(employee.id, dto).then(function(res){
        employeeCache.remove(employee.cacheId);                               // clear cache for this employee

        //eds.getEmployeeAccount(employee.id); // TODO: returns 200 but login is not being updated

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // --------- Employee picture ---------


    //
    // get Employee picture

    eds.getEmployeePicture = function(employee){ // TODO: add caching for Employee picture
      var userId = employee.userAccountDto.id;
      return dataService.getUserPicture(userId).then(function(res){
        var imageData = res.data;
        if ( res.data.size > 0 ) {
          imageData.pictureCacheId = res.config.url;
          imageData.image = 'data:image/JPEG;base64,' + imageData.image;
        }
        return imageData;
      });
    };


    //
    // upload new Employee image

    eds.uploadEmployeePicture = function(employee, img){
      var userId = employee.userAccountDto.id;
      return dataService.uploadUserPicture(userId, img.dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        //console.log('+++ uploadEmployeePicture', res);
        //pictureCache.remove(img.pictureCacheId); TODO add when picture is caching
        return eds.getEmployeePicture(employee).then(function(picture){
          return picture;
        });
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // --------- Employee Home Team ---------

    //
    // change Home Team for an Employee,
    // if this Teams is already associated to Employee

    eds.updateEmployeeHomeTeam = function(employee, teamId, dto){
      return dataService.updateEmployeeHomeTeam(employee.id, teamId, dto).then(function(res){
        if (employee.cacheId) employeeCache.remove(employee.cacheId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    //
    // change Home Team for an Employee,
    // if this Teams is not currently associated to Employee

    eds.addHomeTeamToEmployee = function(employee, teamId, dto){
      return dataService.addTeamToEmployee(employee.id, teamId, dto).then(function(res){
        employeeCache.remove(employee.cacheId);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    //--------------------------------------------------------------------
    // Employee Sidebar section
    //--------------------------------------------------------------------

    // --------- User --------

    //
    // Request a Password reset for a user

    eds.requestUserPasswordReset = function() {
      var deferred = $q.defer();

      // Confirm deletion
      var question = $filter('translate')("employees.RESET_PASSWORD") + employee.firstName + ' ' + employee.lastName + '?';
      var dlg = dialogs.confirm('app.PLEASE_CONFIRM', question);

      dlg.result.then(function () {
        return dataService.requestUserPasswordReset(employee.userAccountDto.id).then( function(res){
          var notification = $filter('translate')('employees.RESET_REQUESTED') + res.data.emailAddress;
          applicationContext.setNotificationMsgWithValues(notification, 'success', true);
          deferred.resolve(res);

        }, function(err) {
          applicationContext.setNotificationMsgWithValues(err.data.info, 'danger', true);
        });
      });

      return deferred.promise;
    };

    //
    // Toggle notifications for a user

    eds.toggleUserNotifications = function(enable) {
      var question = (enable ? $filter('translate')("employees.ENABLE_NOTIFICATIONS_QUESTION") :
                               $filter('translate')("employees.DISABLE_NOTIFICATIONS_QUESTION")) +
                               employee.firstName + ' ' + employee.lastName + '?',
          dlg = dialogs.confirm('app.PLEASE_CONFIRM', question);

      dlg.result.then(
        // if 'Yes' pressed
        function () {
          return dataService.toggleUserNotifications(employee.userAccountDto.id, enable).then(function(res) {
            applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          }, function(err) {
            applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
          });
        // if 'No' pressed
        }, function() {
          employee.notificationSettings.isNotificationEnabled = !employee.notificationSettings.isNotificationEnabled;
        });
    };




    // --------- Autoapprovals panel ---------

    //
    // update Autoapprovals settings for  an Employee

    eds.updateEmployeeAutoapprovals = function(employee, dto){
      return dataService.updateEmployeeAutoapprovals(employee.id, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        employeeCache.remove(employee.cacheId);                               // clear cache for this employee
        return eds.getEmployeeDetails(employee.id);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // --------- PTO panel ---------

    //
    // get all PTOs for an Employee

    eds.getEmployeePTO = function(employee){
      return dataService.getEmployeeCDAvailabilities(employee.id, {filter: 'isPTO=1', orderby:'startDateTime', orderdir:'DESC'}, 1, -1)
        .then(function(res){
          angular.forEach(res.data, function(pto){
            pto.startDateTime = appFunc.convertToBrowserTimezone(pto.startDateTime, employeeSiteTimeZone);
          });
          //console.log('getEmployeeCDAvailabilities', res);
          return res.data;
        })
      ;
    };


    // --------- Skills panel ---------

    //
    // get all Skills for current Org

    eds.getSkills = function(){
      return dataService.getSkills(abcOrder, 1, -1, allOrgSkillsCache).then(function(res){
        //console.log('+++ getSkills', res);
        return res.data;
      });
    };


    //
    // update multiple skills for an Employee

    eds.updateEmployeeSkills = function(employee, dto){
      return dataService.updateEmployeeSkills(employee.id, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        employeeCache.remove(employee.cacheId);                               // clear cache for this employee
        return eds.getEmployeeDetails(employee.id);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // --------- Teams panel ---------

    //
    // get all Teams for a Site Employee belongs to TODO: check caching and dataService conflict

    eds.getSiteTeams = function(){
      return dataService.getSiteTeams(employee.siteInfo.siteId, abcOrder, 1, -1, allSiteTeamsCache).then(function(res){
        //console.log('getSiteTeams', res);
        return res.data;
      });
    };


    //
    // update Employee teams

    eds.updateEmployeeTeams = function(employee, dto){
      return dataService.updateEmployeeTeams(employee.id, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        employeeCache.remove(employee.cacheId);                               // clear cache for this employee
        return eds.getEmployeeDetails(employee.id);

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    //--------------------------------------------------------------------
    // Employee creation
    //--------------------------------------------------------------------


    //
    // create an Employee

    eds.createEmployee = function(dto){
      return dataService.createEmployee(dto).then(function(res){
        return res;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    //
    // get SitesTeamsTree

    eds.getSitesTeamsTree = function() {
      return dataService.getSitesTeamsTree().then(function(res) {
        return res.data;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    //--------------------------------------------------------------------
    // Help methods
    //--------------------------------------------------------------------


    var findPrimarySkill = function(allEmployeeSkills){
      var primarySkill = null;
      angular.forEach(allEmployeeSkills, function(skill) {
        if (skill.isPrimarySkill){
          primarySkill = angular.copy(skill);
        }
      });
      return primarySkill;
    };


    var findHomeTeam = function(allEmployeeTeams){
      var homeTeam = null;
      angular.forEach(allEmployeeTeams, function(team) {
        if (team.isHomeTeam) {
          homeTeam = angular.copy(team);
        }
      });
      return homeTeam;
    };


    var prepareDate = function(dateFromModel){
      if (dateFromModel) {
        var newDate = moment(dateFromModel).format('YYYY-MM-DD');

        /*var newDateTZ = moment.tz(newDate, employeeSiteTimeZone).format();
        var newDateUTC = moment(newDateTZ).tz('UTC').format();
        console.log('newDate', newDate);
        console.log('newDateTZ', newDateTZ);
        console.log('newDateUTC', newDateUTC);
        console.log('newDateTZ v', moment(newDateTZ).valueOf());
        console.log('newDateUTC v', moment(newDateUTC).valueOf());*/

        return moment.tz(newDate, employeeSiteTimeZone).valueOf();
      }
    };

  };


  // Inject dependencies and
  // add service to the Rules module

  employeesDetailsService.$inject = ['$filter', '$cacheFactory', '$q', 'dialogs', 'applicationContext', 'appFunc', 'dataService'];
  angular.module('emlogis.employees').service('employeesDetailsService', employeesDetailsService);

})();