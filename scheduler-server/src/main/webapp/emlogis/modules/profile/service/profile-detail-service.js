(function() {
  "use strict";

  // Service
  // Create service function

  var profileDetailService = function($http, $filter, $q, dialogs, applicationContext, appFunc, dataService){

    var eds = this,
      profile,
      profileInit,
      profileSiteTimeZone,
      abcOrder = { orderby: 'name', orderdir: 'ASC' };

    var baseUrl = applicationContext.getBaseRestUrl();

    //--------------------------------------------------------------------
    // Cached Employee Details
    //--------------------------------------------------------------------


    //
    // get User Profile Detail for current logged in user

    eds.getDetail = function(){

      return $http.get(baseUrl + 'employees/profileview', {})
        .then(function(res){
          profile = res.data;

          profile.primarySkill = findPrimarySkill(profile.skillInfo);
          profile.homeTeam = findHomeTeam(profile.teamInfo);

          profile.skillInfo = $filter('orderBy')(profile.skillInfo, 'name');
          profile.teamInfo = $filter('orderBy')(profile.teamInfo, 'name');

          profileSiteTimeZone = profile.siteInfo.timeZone;
          profile.hireDate = appFunc.convertToBrowserTimezone(profile.hireDate, profileSiteTimeZone);
          profile.startDate = appFunc.convertToBrowserTimezone(profile.startDate, profileSiteTimeZone);
          profile.endDate = appFunc.convertToBrowserTimezone(profile.endDate, profileSiteTimeZone);


          return profile;
        })
      ;
    };

    //
    // provide Employee HomeTeam

    eds.getProfileTeam = function(){
      return profile.homeTeam;
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
        applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', '', true);
        return eds.getDetail();

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
          eds.getDetail().then(function(res){
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



    // --------- Profile Management: Employee picture ---------


    //
    // get Profile picture

    eds.getProfilePicture = function(employee){
      return dataService.getEmployeePicture(employee.id).then(function(res){ //, { cache: pictureCache } TODO: add caching for Employee picture
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
      return dataService.uploadEmployeePicture(employee.id, img.dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        //console.log('+++ uploadEmployeePicture', res);
        //pictureCache.remove(img.pictureCacheId); TODO add when picture is caching
        return eds.getProfilePicture(employee).then(function(picture){
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


    // --------- Autoapprovals panel ---------

    //
    // update Autoapprovals settings for  an Employee

    eds.updateEmployeeAutoapprovals = function(employee, dto){
      return dataService.updateEmployeeAutoapprovals(employee.id, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);

        return eds.getDetail();

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
            pto.startDateTime = appFunc.convertToBrowserTimezone(pto.startDateTime, profileSiteTimeZone);
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

        return eds.getDetail();

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // --------- Teams panel ---------

    //
    // get all Teams for a Site Employee belongs to TODO: check caching and dataService conflict

    eds.getSiteTeams = function(){
      return dataService.getSiteTeams(profile.siteInfo.siteId, abcOrder, 1, -1).then(function(res){
        //console.log('getSiteTeams', res);
        return res.data;
      });
    };


    //
    // update Employee teams

    eds.updateEmployeeTeams = function(employee, dto){
      return dataService.updateEmployeeTeams(employee.id, dto).then(function(res){
        applicationContext.setNotificationMsgWithValues('app.SAVED_SUCCESSFULLY', 'success', true);
        return eds.getDetail();

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

    eds.getSitesTeamsTree = function(){
      return dataService.getSitesTeamsTree({ cache: sitesTeamsTreeCache }).then(function(res) {
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
        /* TODO check if timezone convertion is needed, if on data load it's being converted
        var newDate = new Date(dateFromModel);
        var date = appFunc.getDateWithTimezone(
          newDate.getFullYear(),
          newDate.getMonth(),
          newDate.getDate(),
          employeeSiteTimeZone
        ).getTime();*/
        var date = dateFromModel.getTime();
        return date;
      }
    };

  };


  // Inject dependencies and
  // add service to the Profile module

  profileDetailService.$inject = ['$http', '$filter', '$q', 'dialogs', 'applicationContext', 'appFunc', 'dataService'];
  angular.module('emlogis.profile').service('profileDetailService', profileDetailService);

})();