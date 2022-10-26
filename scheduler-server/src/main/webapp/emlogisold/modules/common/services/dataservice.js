

//=============================================================================
// Service in charge of handling communication with REST resources
//

(function () {
    console.log('in accountService.');
/*
    var svc = angular.copy(

            var dataService = function (config, customersService, customersBreezeService) {
        return (config.useBreeze) ? customersBreezeService : customersService;
    };
// _.extend(destination, *sources) 

*/

    angular.module('emlogis.commonservices')
    .factory('dataService', ['crudDataService', '$http', '$q', function(crudDataService, $http, $q) {

        console.log('creating dataService...');

        var factory = _.clone(crudDataService),     // clone dataService to extend it.
//            baseUrl = './rest/';
            baseUrl = '../emlogis/rest/';


        //====================================================================
        // public methods

        //--------------------------------------------------------------------
        // Account related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of roles associated to user or group account
        * Note: query parameters and paging parameters are present BUT NOT IMPLEMENTED TODAY 
        *
        * expected  queryParams format is { 
        *    filter: <filter expression>, 
        *    orderby: <attribute name>,
        *    orderdir: <'ASC' | 'DESC'>
        * }
        * paging is optional (leave it undefined or specify pageIndex < 0) to skip paging
        * if pecified, paging starts at 0
        * returns the object  {                
        *   data: <array of records>,
        *   total: <total nb of records for that resource> 
        * }
        */
        factory.getAccountRoles = function(resource, accountId, queryParams, pageIndex, pageSize) {

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var url = baseUrl + resource + '/' + accountId + '/roles';
            console.log('--> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
            );

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };

        /*
        * get the list of roles  NOT associated to an account

        */
        factory.getUnassociatedAccountRoles = function(resource, groupId, queryParams, pageIndex, pageSize) {

            return factory.getElements(resource+'/'+groupId+'/unassociatedroles', queryParams, pageIndex, pageSize);

        };

        /*
        * add a Role to an account
        *
        */
        factory.addRoleToAccount = function(resource, accountId, roleId) {

            var url = baseUrl + resource + '/' + accountId + '/ops/addrole?roleId=' + roleId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove a Role from an account
        *
        */
        factory.removeRoleFromAccount = function(resource, accountId, roleId) {

            var url = baseUrl + resource + '/' + accountId + '/ops/removerole?roleId=' + roleId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        //--------------------------------------------------------------------
        // GroupAccount related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of user accounts in a group
        */
        factory.getGroupMembers = function(groupId, queryParams, pageIndex, pageSize) {

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var url = baseUrl + 'groupaccounts/' + groupId + '/users';
            console.log('--> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
            );

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };

        /*
        * get the list of user accounts NOT belonging to a group
        */
        factory.getUnassociatedGroupMembers = function(groupId, queryParams, pageIndex, pageSize) {

            return factory.getElements('groupaccounts/'+groupId+'/unassociatedusers', queryParams, pageIndex, pageSize);
        };

        /*
        * add a userAccount to a group
        *
        */
        factory.addMemberToGroup = function(groupId, userAccountId) {

            var url = baseUrl + 'groupaccounts/' + groupId + '/ops/adduser?memberId=' + userAccountId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove a userAccount from a group
        *
        */
        factory.removeMemberFromGroup = function(groupId, userAccountId) {

            var url = baseUrl + 'groupaccounts/' + groupId + '/ops/removeuser?memberId=' + userAccountId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        //--------------------------------------------------------------------
        // UserAccount related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of Groups a user account belongs to
        */
        factory.getUserGroups = function(useraccountId, queryParams, pageIndex, pageSize) {

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var url = baseUrl + 'useraccounts/' + useraccountId + '/groups';
            console.log('--> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
            );

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };


        //--------------------------------------------------------------------
        // Role related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of permissions accounts in a Role
        */
        factory.getRolePermissions = function(roleId, queryParams, pageIndex, pageSize) {

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var url = baseUrl + 'roles/' + roleId + '/permissions';
            console.log('--> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
            );

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };

        /*
        * get the list of permissions  NOT belonging to a role
        */
        factory.getUnassociatedRolePermissions = function(roleId, queryParams, pageIndex, pageSize) {

            return factory.getElements('roles/' + roleId + '/unassociatedpermissions', queryParams, pageIndex, pageSize);
        };

        /*
        * add a Permission to a Role
        *
        */
        factory.addPermissionToRole = function(roleId, permissionId) {

            var url = baseUrl + 'roles/' + roleId + '/ops/addpermission?permissionId=' + permissionId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove a Permission from a Role
        *
        */
        factory.removePermissionFromRole = function(roleId, permissionId) {

            var url = baseUrl + 'roles/' + roleId + '/ops/removepermission?permissionId=' + permissionId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        //--------------------------------------------------------------------
        // Site related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of Teams associated to a Site
        */
        factory.getSiteTeams = function(siteId, queryParams, pageIndex, pageSize) {

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var url = baseUrl + 'sites/' + siteId + '/teams';
            console.log('--> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
            );

            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };

        /*
        * get the list of Teams  NOT belonging to a Site
        */
        // TODO remove this method below once teams are built as per 'specified' APIs
        factory.getUnassociatedSiteTeams = function(siteId, queryParams, pageIndex, pageSize) {

            // TODO replace this temporary impl that returns all teams
            // TEMPORARY IMPL
            return factory.getElements('teams', queryParams, pageIndex, pageSize);
        };



        /*
        * get the list of skills associated to a Site
        */
        factory.getSiteSkills = function(siteId, queryParams, pageIndex, pageSize) {

            return factory.getElements('sites/' + siteId + '/skills', queryParams, pageIndex, pageSize);
        };

        /*
        * get the list of skills  NOT belonging to a Site
        */
        factory.getUnassociatedSiteSkills = function(siteId, queryParams, pageIndex, pageSize) {

            return factory.getElements('sites/'+siteId+'/unassociatedskills', queryParams, pageIndex, pageSize);
        };

        /*
        * add a Skill to a Site
        *
        */
        factory.addSiteSkill = function(siteId, skillId) {

            var url = baseUrl + 'sites/' + siteId + '/ops/addskill?skillId=' + skillId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove a Skill from a Site
        *
        */
        factory.removeSkillFromSite = function(siteId, skillId) {

            var url = baseUrl + 'sites/' + siteId + '/ops/removeskill?skillId=' + skillId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
         * get list of Timezones
         *
         */
        factory.getTimeZones = function () {
            var url = baseUrl + 'sites/timezones';
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            })
        };

        //--------------------------------------------------------------------
        // Team related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of skills associated to a Team
        */
        factory.getTeamSkills = function(teamId, queryParams, pageIndex, pageSize) {

            return factory.getElements('teams/' + teamId + '/skills', queryParams, pageIndex, pageSize);
        };

        /*
        * get the list of skills  NOT belonging to a Team
        */
        factory.getUnassociatedTeamSkills = function(teamId, queryParams, pageIndex, pageSize) {

            return factory.getElements('teams/' + teamId + '/unassociatedskills', queryParams, pageIndex, pageSize);
        };

        /*
        * add a Skill to a Team
        *
        */
        factory.addTeamSkill = function(teamId, skillId) {

            var url = baseUrl + 'teams/' + teamId + '/ops/addskill?skillId=' + skillId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove a Skill from a Team
        *
        */
        factory.removeSkillFromTeam = function(teamId, skillId) {

            var url = baseUrl + 'teams/' + teamId + '/ops/removeskill?skillId=' + skillId;
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get the list of employees associated to a Team
        */
        factory.getTeamEmployees = function(teamId, queryParams, pageIndex, pageSize) {

            return factory.getElements('teams/' + teamId + '/employees', queryParams, pageIndex, pageSize);
        };

        /*
        * get the list of skills  NOT belonging to a Team
        */
        factory.getUnassociatedTeamEmployees = function(teamId, queryParams, pageIndex, pageSize) {

            return factory.getElements('teams/' + teamId + '/unassociatedemployees', queryParams, pageIndex, pageSize);
        };

        /*
        * add Employee to team
        *
        */
        factory.addEmployeeToTeam = function(teamId, employeeId, createDto) {

            return factory.addTeamToEmployee(employeeId, teamId, createDto);
        };

        /*
        * remove an Employee from a Team
        *
        */
        factory.removeEmployeeFromTeam = function(teamId, employeeId) {

            return factory.removeEmployeeFromTeam(employeeId, teamId);
        }; 

        /*
        * upadte a Team-Employee relationship
        *       
        * returns the updated object
        */
        factory.updateTeamEmployee = function (teamId, employeeId, updateDto) {

            return factory.updateEmployeeTeam(employeeId, teamId, updateDto);
        };

        /*
        * get the list of shiftStructures associated to a Team
        */
        factory.getShiftStructures = function(teamId, queryParams, pageIndex, pageSize) {

            return factory.getElements('teams/' + teamId + '/shiftstructures', queryParams, pageIndex, pageSize);
        };

        /*
        * create and add a ShiftStructure to a Team
        *
        */
        factory.createShiftStructure = function(teamId, createDto) {

            var url = baseUrl + 'teams/' + teamId + '/shiftstructures';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get a ShiftStructure 
        *
        */
        factory.getShiftStructure = function(teamId, shiftStructureId) {

            var url = baseUrl + 'teams/' + teamId + '/shiftstructures/' + shiftStructureId;
            console.log('--> getting( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * update a ShiftStructure 
        *
        */
        factory.updateShiftStructure = function(teamId, shiftStructureId, updateDto) {

            var url = baseUrl + 'teams/' + teamId + '/shiftstructures/' + shiftStructureId;
            console.log('--> updating( ' + url + ')');
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * delete and remove a ShiftStructure from a Team
        *
        */
        factory.deleteShiftStructure = function(teamId, shiftStructureId) {

            var url = baseUrl + 'teams/' + teamId + '/shiftstructures/' + shiftStructureId;
            console.log('--> posting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get the list of Schedule associated to a Team
        */
        factory.getTeamSchedules = function(teamId, queryParams, pageIndex, pageSize) {

            return factory.getElements('teams/' + teamId + '/schedules', queryParams, pageIndex, pageSize);
        };


        //--------------------------------------------------------------------
        // Shift ShiftStructure related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of Team associated to a ShiftStructure
        * Note: query parameters and paging parameters are present BUT NOT IMPLEMENTED TODAY 
        *
        */
        factory.getShiftStructureTeams = function(shiftStructureId, queryParams, pageIndex, pageSize) {

            return factory.getElements('shiftstructures/' + shiftStructureId + '/teams', queryParams, pageIndex, pageSize);
        };

        //--------------------------------------------------------------------
        // Shift Requirement related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of shiftReqs associated to a ShiftStructure
        */
        factory.getShiftReqs = function(shiftStructureId, queryParams, pageIndex, pageSize) {

            return factory.getElements('shiftstructures/' + shiftStructureId + '/shiftreqs', queryParams, pageIndex, pageSize);
        };

        /*
        * create and add a ShiftReq to a ShiftStructure
        *
        */
        factory.createShiftReq = function(shiftStructureId, createDto) {

            var url = baseUrl + 'shiftstructures/' + shiftStructureId + '/shiftreqs';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get a ShiftReq 
        *
        */
        factory.getShiftReq = function(shiftStructureId, shiftReqId) {

            var url = baseUrl + 'shiftstructures/' + shiftStructureId + '/shiftreqs/' + shiftReqId;
            console.log('--> loading( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * Update a ShiftReq 
        *
        */
        factory.updateShiftReq = function(shiftStructureId, shiftReqId, updateDto) {

            var url = baseUrl + 'shiftstructures/' + shiftStructureId + '/shiftreqs/' + shiftReqId;
            console.log('--> updating( ' + url + ')');
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * delete and remove a ShiftReq from a ShiftStructure
        *
        */
        factory.deleteShiftReq = function(shiftStructureId, shiftReqId) {

            var url = baseUrl + 'shiftstructures/' + shiftStructureId + '/shiftreqs/' + shiftReqId;
            console.log('--> deleting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        //--------------------------------------------------------------------
        // Employee related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of Teams  an Employee belongs to
        */
        factory.getEmployeeTeams = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/teams', queryParams, pageIndex, pageSize);
        };

        /*
        * get the list of Teams an Employee DOESN't belong to
        */
        factory.getUnassociatedEmployeeTeams = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/unassociatedteams', queryParams, pageIndex, pageSize);
        };

        /*
        * add Employee to team
        *
        */
        factory.addTeamToEmployee = function(employeeId, teamId, createDto) {

            if (createDto === undefined) {
                // assume we get a teamId only instead of a createDto
                // in that case, set  default values for dto attributes
                createDto = {
                    isFloating: false,
                    isHomeTeam: false,
                    isSchedulable: true,
                    teamId: teamId
                };
            }
            var url = baseUrl + 'employees/' + employeeId + '/teams/';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove an Employee from a Team
        *
        */
        factory.removeTeamFromEmployee = function(employeeId, teamId) {

            var url = baseUrl + 'employees/' + employeeId + '/teams/' + teamId;
            console.log('--> posting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        }; 

        /*
        * upadte a Team-Employee relationship
        *       
        * returns the updated object
        */
        factory.updateEmployeeTeam = function (employeeId, teamId, updateDto) {

            var url = baseUrl + resource + 'employees/' + employeeId + '/teams/' + teamId;
            //console.log('--> updating( ' + url + ' params=' + urlParams);
            console.log('--> updating( ' + url + ') with:' + updateDto);
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get the list of skills associated to an Employee

        */

        factory.getEmployeeSkills = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/skills',queryParams, pageIndex, pageSize);
        };

        
        /*
        * get the list of skills  NOT belonging to a Employee
        */
        factory.getUnassociatedEmployeeSkills = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/unassociatedskills', queryParams, pageIndex, pageSize);
        };

        /*
        * add a Skill to an Employee
        *
        */
        factory.addSkillToEmployee = function(employeeId, skillId, createDto) {

            if (createDto === undefined) {
                // assume we get a skillId only instead of a createDto
                // in that case, set  default values for dto attributes
                createDto = {
                    isPrimarySkill: false,
                    skillScore: 3,
                    skillId: skillId
                };
            }

            var url = baseUrl + 'employees/' + employeeId + '/skills/';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * remove a Skill from an Employee
        *
        */
        factory.removeSkillFromEmployee = function(employeeId, skillId) {

            var url = baseUrl + 'employees/' + employeeId + '/skills/' + skillId;
            console.log('--> posting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        }; 

        /*
        * upadte a Skill-Employee relationship
        *       
        * returns the updated object
        */
        factory.updateEmployeeSkill = function (employeeId, skillId, updateDto) {

            var url = baseUrl + 'employees/' + employeeId + '/skills/' + skillId;
            //console.log('--> updating( ' + url + ' params=' + urlParams);
            console.log('--> updating( ' + url + ') with:' + updateDto);
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        *  get Employee Account
        *       
        * returns the associated account object
        */
        factory.getEmployeeAccount = function (employeeId) {

            var url = baseUrl + 'employees/' + employeeId + '/useraccount';
            console.log('--> getting account( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        /*
        * get the list of Contracts associated to an Employee
        *
        */
        factory.getEmployeeContracts = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/contracts', queryParams, pageIndex, pageSize);
        };


        /*
        * get the list of Calendar Independent Availability records associated to an Employee
        */
        factory.getEmployeeCIAvailabilities = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/ciavailability', queryParams, pageIndex, pageSize);
        };

        /*
        * get a CIAvailability 
        *
        */
        factory.getEmployeeCIAvailability = function(employeeId, availabilityId) {

            var url = baseUrl + 'employees/' + employeeId + '/ciavailability/' + availabilityId;
            console.log('--> getting( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * create CIAvailability for an Employee
        *
        */
        factory.createEmployeeCIAvailability = function(employeeId, createDto) {

            var url = baseUrl + 'employees/' + employeeId + '/ciavailability';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * update a CIAvailability 
        *
        */
        factory.updateEmployeeCIAvailability = function(employeeId, availabilityId, updateDto) {

            var url = baseUrl + 'employees/' + employeeId + '/ciavailability/' + availabilityId;
            console.log('--> updating( ' + url + ')');
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * delete and remove a CIAvailability from an Employee
        *
        */
        factory.deleteEmployeeCIAvailability = function(employeeId, availabilityId) {

            var url = baseUrl + 'employees/' + employeeId + '/ciavailability/' + availabilityId;
            console.log('--> posting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get the list of Calendar Dependent Availability records associated to an Employee
        */
        factory.getEmployeeCDAvailabilities = function(employeeId, queryParams, pageIndex, pageSize) {

            return factory.getElements('employees/' + employeeId + '/cdavailability', queryParams, pageIndex, pageSize);
        };

        /*
        * get a CDAvailability 
        *
        */
        factory.getEmployeeCDAvailability = function(employeeId, availabilityId) {

            var url = baseUrl + 'employees/' + employeeId + '/cdavailability/' + availabilityId;
            console.log('--> getting( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * create CDAvailability for an Employee
        *
        */
        factory.createEmployeeCDAvailability = function(employeeId, createDto) {

            var url = baseUrl + 'employees/' + employeeId + '/cdavailability';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * update a CDAvailability 
        *
        */
        factory.updateEmployeeCDAvailability = function(employeeId, availabilityId, updateDto) {

            var url = baseUrl + 'employees/' + employeeId + '/cdavailability/' + availabilityId;
            console.log('--> updating( ' + url + ')');
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * delete and remove a CDAvailability from an Employee
        *
        */
        factory.deleteEmployeeCDAvailability = function(employeeId, availabilityId) {

            var url = baseUrl + 'employees/' + employeeId + '/cdavailability/' + availabilityId;
            console.log('--> posting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        //--------------------------------------------------------------------
        // Schedule related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of ShiftStructures associated to a Schedule
        * Note: query parameters and paging parameters are present BUT NOT IMPLEMENTED TODAY 
        *
        */
        factory.getScheduleShiftStructures = function(scheduleId, queryParams, pageIndex, pageSize) {

            return factory.getElements('schedules/' + scheduleId + '/shiftstructures', queryParams, pageIndex, pageSize);
        };

        /*
        * get the list of Team associated to a Schedule
        * Note: query parameters and paging parameters are present BUT NOT IMPLEMENTED TODAY 
        *
        */
        factory.getScheduleTeams = function(scheduleId, queryParams, pageIndex, pageSize) {

            return factory.getElements('schedules/' + scheduleId + '/teams', queryParams, pageIndex, pageSize);
        };

        /*
        * get the list of Shifts associated to a Schedule
        * Note: query parameters and paging parameters are present BUT NOT IMPLEMENTED TODAY 
        *
        */
        factory.getScheduleShifts = function(scheduleId, queryParams, pageIndex, pageSize) {

            return factory.getElements('schedules/' + scheduleId + '/shifts', queryParams, pageIndex, pageSize);
        };

        /**
         * get list of Shifts associated to a Schedule and specified by time period
         * filter - array of filters
         */
        factory.getScheduleShiftsByPeriod = function (scheduleId, startDate, endDate, filter) {

            var url = baseUrl + 'schedules/' + scheduleId + '/shifts/ops/get';
            filter = filter.join(';');
            return $http.get(url, {params: {startdate: startDate, enddate: endDate, filter: filter}})
                .then(function (response) {
                return factory.toResultSet(response);
            });
        }

        /*
        * Execute a Schedule        *
        */
        factory.executeSchedule = function(scheduleId, config) {

            var url = baseUrl + 'schedules/' + scheduleId + '/ops/execute';
            console.log('--> executing( ' + url + ')');
            return $http.post(url, config).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        factory.duplicateSchedule = function (scheduleId, dto) {

            var url = baseUrl + 'schedules/' + scheduleId + '/ops/duplicate';
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        }

        /*
        * promote a Schedule        
        */
        factory.promoteSchedule = function(scheduleId) {

            var url = baseUrl + 'schedules/' + scheduleId + '/ops/promote';
            console.log('--> executing( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * reset a Schedule state        
        */
        factory.resetScheduleState = function(scheduleId) {

            var url = baseUrl + 'schedules/' + scheduleId + '/ops/resetstate';
            console.log('--> executing( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * generate Schedule  Shifts
        */
        factory.generateShifts = function(scheduleId) {

            var url = baseUrl + 'schedules/' + scheduleId + '/ops/generateshifts';
            console.log('--> executing( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * Abort a Schedule        *
        */
        factory.abortSchedule = function(scheduleId) {

            var url = baseUrl + 'schedules/' + scheduleId + '/ops/abort';
            console.log('--> posting( ' + url + ')');
            return $http.post(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };


        /*
        * get the list of Changes associated to a Schedule
        * Note: queryParams has optional extra parameters specific to that API
        *
        */
        factory.getScheduleChanges = function(scheduleId, queryParams, pageIndex, pageSize) {

            return factory.getElements('schedules/' + scheduleId + '/changes', queryParams, pageIndex, pageSize);

            var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
            var specificParams = _.pick(queryParams, 'startdate', 'enddate', 'type', 'employees');
            _.defaults(params, specificParams);   
            var url = baseUrl + resource;
            console.log('dataService --> querying( ' + url + ' q=' + params.filter
                + " orderby: " + params.orderby + '(' + params.orderdir 
                + ') from: ' + params.offset + '[' + params.limit + ']'
                + ' between: ' + params.startdate + ' and ' + params.enddate 
                + ' for type: ' + params.type + ' and employees: ' + params.employees 
            );
            return $http.get(url, {params: params}).then(function (response) {
                return factory.toResultSet(response);
            });
        };

        /*
         * get the list of Eployees associated to a Schedule
         *
         */
        factory.getScheduleEmployees = function(scheduleId, queryParams, pageIndex, pageSize) {

            return factory.getElements('schedules/' + scheduleId + '/employees', queryParams, pageIndex, pageSize);
        };

        /*
         * get the list of ScheduleOptions
         *
         */
        factory.getScheduleOptions = function(scheduleId) {

            var url = baseUrl + 'schedules/' + scheduleId + '/options';
            console.log('--> getting( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
         * update ScheduleOptions
         *
         */
        factory.updateScheduleOptions = function(scheduleId, updateDto) {

            var url = baseUrl + 'schedules/' + scheduleId + '/options';
            console.log('--> updating( ' + url + ')');
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };
        //--------------------------------------------------------------------
        // Contract Line  related methods 
        //--------------------------------------------------------------------

        /*
        * get the list of contractLines associated to a Contract
        */
        factory.getContractLines = function(contractId, queryParams, pageIndex, pageSize) {

            return factory.getElements('contracts/' + contractId + '/contractlines', queryParams, pageIndex, pageSize);
        };

        /*
        * create and add a ContractLine to a Contract
        *
        */
        factory.createContractLine = function(contractId, createDto) {

            var url = baseUrl + 'contracts/' + contractId + '/contractlines';
            console.log('--> posting( ' + url + ')');
            var dto = factory.metamodel.toDto(createDto);
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * get a ContractLine 
        *
        */
        factory.getContractLine = function(contractId, contractLineId) {

            var url = baseUrl + 'contracts/' + contractId + '/contractlines/' + contractLineId;
            console.log('--> loading( ' + url + ')');
            return $http.get(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * Update a ContractLine 
        *
        */
        factory.updateContractLine = function(contractId, contractLineId, updateDto) {

            var url = baseUrl + 'contracts/' + contractId + '/contractlines/' + contractLineId;
            console.log('--> updating( ' + url + ')');
            var dto = factory.metamodel.toDto(updateDto);
            return $http.put(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
        * delete and remove a ContractLine from a Contract
        *
        */
        factory.deleteContractLine = function(contractId, contractLineId) {

            var url = baseUrl + 'contracts/' + contractId + '/contractlines/' + contractLineId;
            console.log('--> deleting( ' + url + ')');
            return $http.delete(url).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        //====================================================================
        // MetaModel configuration

        // defines for each class attributes that need a mapping between Dto and UI object. 
        var metamodel = factory.metamodel;
        metamodel.BaseEntity = {
            created: 'tdate',
            updated: 'tdate'               
        };

        metamodel.UserAccount = factory.metamodel.BaseEntity;
        metamodel.GroupAccount = metamodel.BaseEntity;
        metamodel.Role = metamodel.BaseEntity;
        metamodel.Permission = metamodel.BaseEntity;

        metamodel.Site = metamodel.BaseEntity;
        metamodel.Team = metamodel.BaseEntity;
        metamodel.Skill = metamodel.BaseEntity;
        metamodel.ShiftType = metamodel.BaseEntity;
        metamodel.AbsenceType = metamodel.BaseEntity;

        metamodel.Employee = _.defaults({}, 
            metamodel.BaseEntity,
            {startDate: 'tdate'}
        );

        metamodel.Schedule = _.defaults({}, 
            metamodel.BaseEntity,
            {
                startDate: 'tdate', 
                endDate: 'tdate',
                executionStartDate: 'tdate', 
                requestSentDate: 'tdate', 
                executionAckDate: 'tdate', 
                responseReceivedDate: 'tdate', 
                executionEndDate: 'tdate',
                completionReport: 'tjson'
            }
        );
        metamodel.ShiftStructure = _.defaults({}, 
            metamodel.BaseEntity,
            {startDate: 'tdate'}
        );
        metamodel.ShiftReq = _.defaults({}, 
            metamodel.BaseEntity,
            {startTime: 'ttime'}
        );

        metamodel.CIAvailabilityTimeFrame = _.defaults({}, 
            metamodel.BaseEntity,
            {
                startTime: 'ttime',
                startDate: 'tdate', 
                endDate: 'tdate' 
            }
        );

        metamodel.CDAvailabilityTimeFrame = _.defaults({}, 
            metamodel.BaseEntity,
            {
                startTime: 'ttime',
                startDate: 'tdate', 
            }
        );

        metamodel.IntMinMaxCL = _.defaults({}, 
            metamodel.BaseEntity,{
                fromDto: function(elt){
                    var descr = '';
                    if (elt.minimumEnabled) {
                        descr += "Min: " + elt.minimumValue;
                        if (elt.minimumWeight > 0) {
                            descr += '(w:' + elt.minimumWeight + ')';
                        }
                    }
                    descr += ' ';
                    if (elt.maximumEnabled) {
                        descr += "Max: " + elt.maximumValue;
                        if (elt.maximumWeight > 0) {
                            descr += '(w:' + elt.maximumWeight + ')';
                        }
                    }
                    return {description: descr};
                }
            }
        );

        metamodel.BooleanCL = _.defaults({}, 
            metamodel.BaseEntity,{
                fromDto: function(elt){
                    var descr = (elt.enabled ? 'On' : 'Off');
                    if (elt.weight > 0) {
                        descr += '(w: ' + elt.weight + ')';
                    }
                    return {description: descr};
                }
            }
        );


        //====================================================================
        // private methods


        // 
        console.log('dataService created.');
        return factory;

    }]);

}());

