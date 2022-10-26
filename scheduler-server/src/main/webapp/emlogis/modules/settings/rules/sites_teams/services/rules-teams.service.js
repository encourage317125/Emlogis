(function() {
  "use strict";

  // Service
  // Create service function

  var rulesTeamsService = function($http, $q, applicationContext, crudDataService, dataService){

    var baseUrl = applicationContext.getBaseRestUrl(),
        factory = _.clone(crudDataService),
        abcOrder = {orderby:'name', orderdir:'ASC'};


    //--------------------------------------------------------------------
    // Teams related methods
    //--------------------------------------------------------------------


    // GET all Teams for a Site

    this.getSiteTeams = function(siteId){
      return dataService.getSiteTeams(siteId, abcOrder, 1, -1).then(function(response){
          //console.log('~~~ allTeams for current Site: ', res.data);
          return response;

        }, function (err) {
          applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
        })
      ;
    };

    // GET a Team details

    this.getTeamDetails = function(teamId) {
      return $http.get( baseUrl + 'teams/' + teamId )
        .then( function (response) {
          return response;
        });
    };


    // Update a Team details

    this.putTeamDetails = function(teamId, dto) {
      return factory.updateElement('teams', teamId, dto)
        .then( function (response) {
          return response;
        })
      ;
    };


    // Add a New Team

    this.addNewTeam = function(newTeamDto) {
      return factory.createElement('teams', newTeamDto)
        .then( function(res){
          return res;
        })
      ;
    };



    //--------------------------------------------------------------------
    // Team's Skills related methods
    //--------------------------------------------------------------------


    // GET Skills for a Team

    this.getTeamSkills = function(teamId) {
      return dataService.getTeamSkills(teamId, abcOrder, 1, -1).then( function (response) {
        //console.log('~~~ Loaded Skills for the Team...', response.data);
        return response.data;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    // GET Unassociated Skills for a Team

    this.getUnassociatedTeamSkills = function(teamId) {
      return dataService.getUnassociatedTeamSkills(teamId, abcOrder, 1, -1).then( function (response) {
        //console.log('~~~ Loaded Unassosiated Skills for the Team...', response.data);
        return response.data;

      }, function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    // Attach Skill to a Team

    this.addTeamSkill = function(newTeamId, skillId){
      return dataService.addTeamSkill(newTeamId, skillId)
        .then(function(res){
          return res;
        });
    };


    // Remove Skill from a Team

    this.removeSkillFromTeam = function(newTeamId, skillId){
      return dataService.removeSkillFromTeam(newTeamId, skillId)
        .then(function(res){
          return res;
        });
    };



    //--------------------------------------------------------------------
    // Team's Employees related methods
    //--------------------------------------------------------------------


    // Load Employees
    // that belong to this Team

    this.loadTeamEmployees = function(teamId, queryParams, pageNum, numOfRows){
      return dataService.getTeamEmployees(teamId, queryParams, pageNum, numOfRows).then( function(res){
        console.log('+++ Loaded Employees for the Team:', res);    //DEV mode
        return res;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



    // Removes association between a team and specified employees

    this.removeEmployeesTeamMembership = function(teamId, emplIdsList) {
      var url = baseUrl + 'teams/' + teamId + '/membership/ops/removeemployees';
      return $http.post(url, emplIdsList).then( function (res) {
        applicationContext.setNotificationMsgWithValues('Selected employees were successfully removed!', 'success', true);
        return res;

      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };


    // Adds association between a team and specified employees

    this.addEmployeesTeamMembership = function(teamId, emplIdsList) {
      return $http.post( baseUrl + 'teams/' + teamId + '/membership/ops/addemployees', emplIdsList ).then( function (res) {
        return res;
      });
    };


    // GET Emloyees NOT belonging to a Team

    this.getTeamUnassociatedEmployees = function(teamId, sortParams, pageNum, perPage){
      return dataService.getUnassociatedTeamEmployees(teamId, sortParams, pageNum, perPage)
        .then( function(res){
          return res;
        })
      ;
    };


    // GET Employee details || , queryParams, pageIndex, pageSize

    /*this.getEmployeeDetails = function(employeeId){
      return factory.getElement('employees', employeeId, {})
        .then(function(res){
          return res;
        });
    };*/

    this.getEmployeeDetails = function(employeeId){
      return $http.get(baseUrl + 'employees/' + employeeId + '/info')
        .then(function(res){
          return res;
        });
    };


    // Delete a Team

    this.deleteTeam = function(teamId) {
      return dataService.deleteTeam(teamId).then(function(res){
        applicationContext.setNotificationMsgWithValues('rules.site_teams.TEAM_DELETED', 'success', true);
        return res;

      }, function (err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    };



  };


  // Inject dependencies and
  // add service to the Rules module

  rulesTeamsService.$inject = ['$http', '$q', 'applicationContext', 'crudDataService', 'dataService'];
  angular.module('emlogis.rules').service('rulesTeamsService', rulesTeamsService);

})();