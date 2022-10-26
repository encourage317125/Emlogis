//=============================================================================
// Service in charge of handling communication with REST resources
//

(function () {
  //console.log('in accountService.');
  /*
   var svc = angular.copy(

   var dataService = function (config, customersService, customersBreezeService) {
   return (config.useBreeze) ? customersBreezeService : customersService;
   };
   // _.extend(destination, *sources)

   */

  angular.module('emlogis.commonservices')
    .factory('dataService', ['crudDataService', '$http', '$q',
      function(crudDataService, $http, $q) {

        //console.log('creating dataService...');

        var factory = _.clone(crudDataService),     // clone dataService to extend it.
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
          //console.log('--> querying( ' + url + ' q=' + params.filter
          //    + " orderby: " + params.orderby + '(' + params.orderdir
          //    + ') from: ' + params.offset + '[' + params.limit + ']'
          //);

          return $http.get(url, {params: params}).then(function (response) {
            return factory.toResultSet(response);
          });
        };

        /*
         * get the list of roles  NOT associated to an account

         */
        factory.getUnassociatedGroupAccountRoles = function(resource, groupId, queryParams, pageIndex, pageSize) {

          return factory.getElements(resource+'/'+groupId+'/unassociatedroles', queryParams, pageIndex, pageSize);

        };

        /*
         * add a Role to an account
         *
         */
        factory.addRoleToAccount = function(resource, accountId, roleId) {

          var url = baseUrl + resource + '/' + accountId + '/ops/addrole';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, roleId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * remove a Role from an account
         *
         */
        factory.removeRoleFromAccount = function(resource, accountId, roleId) {

          var url = baseUrl + resource + '/' + accountId + '/ops/removerole';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, roleId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * get the list of groups NOT associated to an account
         */
        factory.getUnassociatedAccountGroups = function(accountId, queryParams, pageIndex, pageSize) {

          var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
          return $http.get(baseUrl + "useraccounts/" + accountId + '/unassociatedgroups', {params: params});
        };

        /*
         * get the list of roles NOT associated to an account
         */
        factory.getUnassociatedAccountRoles = function(accountId) {
          return $http.get(baseUrl + "useraccounts/" + accountId + '/unassociatedroles');
        };

        /*
         * create userAccount
         */
        factory.createUserAccount = function(dto) {
          return $http.post(baseUrl + "useraccounts", dto);
        };

        /*
         * update userAccount
         */
        factory.updateUserAccount = function(accountId, dto) {
          return $http.put(baseUrl + "useraccounts/" + accountId, dto);
        };

        /*
         * delete userAccount
         */
        factory.deleteUserAccount = function(accountid) {
          return $http.delete(baseUrl + "useraccounts/" + accountid);
        };

        /*
         * add group to userAccount
         */
        factory.addGroupsToUserAccount = function(accountId, dto) {
          return $http.post(baseUrl + "useraccounts/" + accountId + "/ops/addgroups", dto);
        };

        /*
         * remove group from userAccount
         */
        factory.removeGroupsFromUserAccount = function(accountId, dto) {
          return $http.post(baseUrl + "useraccounts/" + accountId + "/ops/removegroups", dto);
        };


        /*
         * get the list of groups NOT associated to an account
         */
        factory.getUnassociatedRolesGroups = function(resource, roleId, queryParams, pageIndex, pageSize) {

          return factory.getElements(resource+'/'+roleId+'/unassociatedgroupaccounts', queryParams, pageIndex, pageSize);

        };

        factory.getUnassociatedRolesUsers = function(resource, roleId, queryParams, pageIndex, pageSize) {
          return factory.getElements(resource+'/'+roleId+'/unassociateduseraccounts', queryParams, pageIndex, pageSize);
        };

        factory.getUnassociatedRolesPermissions = function(resource, roleId, queryParams, pageIndex, pageSize) {
          return factory.getElements(resource+'/'+roleId+'/unassociatedpermissions', queryParams, pageIndex, pageSize);
        };

        /*
         * add a userAccounts to a group
         *
         */
        factory.addRolesToGroup = function(groupId, dto) {
          return $http.post(baseUrl + "groupaccounts/" + groupId + "/ops/addroles", dto);
        };

        /*
         * add role to userAccount
         */
        factory.addRolesToUserAccount = function(accountId, dto) {
          return $http.post(baseUrl + "useraccounts/" + accountId + "/ops/addroles", dto);
        };

        /*
         * remove role from userAccount
         */
        factory.removeRolesFromUserAccount = function(accountId, dto) {
          return $http.post(baseUrl + "useraccounts/" + accountId + "/ops/removeroles", dto);
        };

        /*
         * delete Role
         */
        factory.deleteRole = function(roleId) {
          return $http.delete(baseUrl + "roles/" + roleId);
        };

        /*
         * request password reset for user
         */
        factory.requestPasswordReset = function(dto) {
          return $http.post(baseUrl + "useraccounts/ops/resetpassword", dto);
        };

        /*
         * request password for User
         */
        factory.requestUserPasswordReset = function(userId) {
          return $http.post(baseUrl + "useraccounts/" + userId + "/ops/resetpassword");
        };

        /*
         * Toggle notifications for User
         */
        factory.toggleUserNotifications = function(userId, enable) {
          return $http.post(baseUrl + "useraccounts/" + userId + "/ops/enablenotification", enable);
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
          //console.log('--> querying( ' + url + ' q=' + params.filter
          //    + " orderby: " + params.orderby + '(' + params.orderdir
          //    + ') from: ' + params.offset + '[' + params.limit + ']'
          //);

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

          var url = baseUrl + 'groupaccounts/' + groupId + '/ops/adduser';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, userAccountId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * add a userAccounts to a group
         *
         */
        factory.addMembersToGroup = function(groupId, dto) {

          var url = baseUrl + 'groupaccounts/' + groupId + '/ops/addusers';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * remove a userAccount from a group
         *
         */
        factory.removeMemberFromGroup = function(groupId, userAccountId) {

          var url = baseUrl + 'groupaccounts/' + groupId + '/ops/removeuser';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, userAccountId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * remove a Role from an group
         *
         */
        factory.removeRoleFromGroup = function(groupId, roleId) {

          var url = baseUrl + 'groupaccounts/' + groupId + '/ops/removerole';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, roleId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };


        //--------------------------------------------------------------------
        // UserAccount related methods
        //--------------------------------------------------------------------

        /*
         * get UserAccountViewDto with user account entity attributes, list of groups and roles
         */
        factory.getUserAccountView = function(userAccountId) {
          return $http.get(baseUrl + "useraccounts/" + userAccountId + "/userview");
        };

        /*
         * get the list of Groups a user account belongs to
         */
        factory.getUserGroups = function(useraccountId, queryParams, pageIndex, pageSize) {

          var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
          var url = baseUrl + 'useraccounts/' + useraccountId + '/groups';
          //console.log('--> querying( ' + url + ' q=' + params.filter
          //    + " orderby: " + params.orderby + '(' + params.orderdir
          //    + ') from: ' + params.offset + '[' + params.limit + ']'
          //);

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
          //console.log('--> querying( ' + url + ' q=' + params.filter
          //    + " orderby: " + params.orderby + '(' + params.orderdir
          //    + ') from: ' + params.offset + '[' + params.limit + ']'
          //);

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
         */
        factory.addPermissionToRole = function(roleId, permissionId) {

          var url = baseUrl + 'roles/' + roleId + '/ops/addpermission';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, permissionId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * remove a Permission from a Role
         *
         */
        factory.removePermissionFromRole = function(roleId, permissionId) {

          var url = baseUrl + 'roles/' + roleId + '/ops/removepermission';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, permissionId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * remove groups from role
         */
        factory.removeGroupsFromRole = function(roleId, dto) {
          return $http.post(baseUrl + "roles/" + roleId + "/ops/removegroups", dto);
        };

        /*
         * remove users from role
         */
        factory.removeUsersFromRole = function(roleId, dto) {
          return $http.post(baseUrl + "roles/" + roleId + "/ops/removeusers", dto);
        };

        /*
         * add groups to role
         */
        factory.addGroupsToRole = function(roleId, dto) {
          return $http.post(baseUrl + "roles/" + roleId + "/ops/addgroups", dto);
        };

        /*
         * add a userAccounts to a role
         */
        factory.addUsersToRole = function(roleId, dto) {

          var url = baseUrl + 'roles/' + roleId + '/ops/addusers';

          return $http.post(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * add a Permissions to a Role
         */
        factory.addPermissionsToRole = function(roleId, dto) {

          var url = baseUrl + 'roles/' + roleId + '/ops/addpermissions';

          return $http.post(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };




        //--------------------------------------------------------------------
        // Site related methods
        //--------------------------------------------------------------------

        /*
         * Get Sites
         */

        factory.getSites = function() {
          var url = baseUrl + 'sites';

          return $http.get(url).then(function(response) {
            return factory.toResultSet(response);
          });
        };

        /*
         * delete Site
         */

        factory.deleteSite = function(siteId) {

          var url = baseUrl + 'sites/' + siteId + '/ops/softdelete/';
          return $http.post(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * get the list of Teams associated to a Site
         */
        factory.getSiteTeams = function(siteId, queryParams, pageIndex, pageSize, cache) {

          var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
          var url = baseUrl + 'sites/' + siteId + '/teams';
          //console.log('--> querying( ' + url + ' q=' + params.filter
          //    + " orderby: " + params.orderby + '(' + params.orderdir
          //    + ') from: ' + params.offset + '[' + params.limit + ']'
          //);
          var cacheVal = cache && cache !== undefined ? cache : false;

          return $http.get(url, {params: params, cache: cacheVal}).then(function (response) {
            return factory.toResultSet(response);
          });
        };

        /*
         *  GET Sites+Teams tree
         */

        factory.getSitesTeamsTree = function(queryParams){
          return $http.get(baseUrl + 'sites/siteteamskills', queryParams).then(function(res){
            return factory.toResultSet(res);
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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> posting( ' + url + ')');
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
          });
        };

        //--------------------------------------------------------------------
        // Absence Types methods
        //--------------------------------------------------------------------

        // GET Absence Types

        factory.getAbsenceTypes = function (siteId, queryParams, pageIndex, pageSize) {

          return factory.getElements('sites/'+siteId+'/absencetypes', queryParams, pageIndex, pageSize);
        };

        // DELETE Absence Type

        factory.deleteAbsenceType = function (siteId, typeId) {
          return $http.delete(baseUrl + 'sites/' + siteId + '/absencetypes/' + typeId);
        };

        // UPDATE Absence Type

        factory.updateAbsenceType = function (siteId, typeId, dto) {
          return $http.put(baseUrl + 'sites/' + siteId + '/absencetypes/' + typeId, dto);
        };

        // ADD Absence Type

        factory.addAbsenceType = function (siteId, dto) {
          return $http.post(baseUrl + 'sites/' + siteId + '/absencetypes', dto);
        };

        //--------------------------------------------------------------------
        // Shift Lengths & Types methods
        //--------------------------------------------------------------------

        // GET Shift Lengths

        factory.getShiftLengths = function (siteId, queryParams, pageIndex, pageSize) {

          return factory.getElements('sites/'+siteId+'/shiftlengths', queryParams, pageIndex, pageSize);
        };

        // Update

        factory.updateShiftLength = function (siteId, lengthId, dto) {
          return $http.put(baseUrl + 'sites/' + siteId + '/shiftlengths/' + lengthId, dto);
        };

        // Multi Add New Shift Lengths

        factory.addNewShiftLengths = function (siteId, dto) {
          return $http.post(baseUrl + 'sites/' + siteId + '/shiftlengths/ops/createmultiple', dto);
        };

        /* Shift Types */

        // GET Shift Types belonging to a Shift Length

        factory.getShiftTypesForLength = function(siteId, shiftLengthId, queryParams, pageIndex, pageSize) {
          return factory.getElements('sites/' + siteId + '/shiftlengths/' + shiftLengthId + '/shifttypes', queryParams, pageIndex, pageSize);
        };

        // Upd multi Shift Types activation

        factory.updShiftTypesActivation = function(siteId, dto) {
          return $http.post(baseUrl + 'sites/' + siteId + '/shifttypes/ops/updateactivation', dto);
        };

        // Update a Type

        factory.updateShiftType = function(siteId, typeId, dto) {
          return $http.put(baseUrl + 'sites/' + siteId + '/shifttypes/' + typeId, dto);
        };

        // Delete a Type

        factory.deleteShiftType = function(siteId, typeId) {
          return $http.delete(baseUrl + 'sites/' + siteId + '/shifttypes/' + typeId);
        };

        // Add a Type

        factory.addShiftType = function(siteId, dto) {
          return $http.post(baseUrl + 'sites/' + siteId + '/shifttypes', dto);
        };

        // Bulk Add Shift Types

        factory.bulkAddNewShiftTypes = function(siteId, dto) {
          return $http.post(baseUrl + 'sites/' + siteId + '/shifttypes/ops/createmultiple', dto);
        };


        //--------------------------------------------------------------------
        // Team related methods
        //--------------------------------------------------------------------

        /*
         * Get Teams
         */

        factory.getTeams = function() {
          var url = baseUrl + 'teams';

          return $http.get(url).then(function(response) {
            return factory.toResultSet(response);
          });
        };

        /*
         * delete Team
         *
         */
        factory.deleteTeam = function(teamId) {

          var url = baseUrl + 'teams/' + teamId + '/ops/softdelete/';
          return $http.post(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };


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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> getting( ' + url + ')');
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
          //console.log('--> updating( ' + url + ')');
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
          //console.log('--> posting( ' + url + ')');
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

        /*
         * Get Schedules
         */
        factory.getSchedules = function(payLoad) {
          return factory.getElementsByPost('schedules/ops/querynew', payLoad);
        };

        //--------------------------------------------------------------------
        // Team-Employee membership related methods
        //--------------------------------------------------------------------

        /*
         Get the list of team membership objects
         */

        factory.getTeamMembership = function(teamId, queryParams, pageIndex, pageSize) {
          return factory.getElements('teams/' + teamId + '/membership', queryParams, pageIndex, pageSize);
        };


        /*
         Removes association between a team and specified employees
         */

        factory.removeEmployeesTeamMembership = function(teamId, emplIdsList) {
          //return factory.createElement('teams/' + teamId + '/membership/ops/removeemployees', emplIdsList);

          return factory.createElement('teams/' + teamId + '/membership/ops/removeemployees', emplIdsList);
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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> loading( ' + url + ')');
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
          //console.log('--> updating( ' + url + ')');
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
          //console.log('--> deleting( ' + url + ')');
          return $http.delete(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        //--------------------------------------------------------------------
        // Employee related methods
        //--------------------------------------------------------------------

        /*
         * get list of Employees
         */
        factory.getEmployees = function(queryParams, pageIndex, pageSize) {
          return factory.getElements('employees/ops/query/', queryParams, pageIndex, pageSize);
        };

        /*
         * search for Employee
         */
        factory.searchEmployees = function(queryParams) {
          return factory.getElements('employees/ops/quicksearch', queryParams);
        };

        /*
         * Get Employee details
         */
        factory.getEmployeeDetails = function(employeeId, queryParams) {
          return $http.get(baseUrl + 'employees/' + employeeId + '/managerdetailsview', queryParams).then(function(res){
            return res;
          });
        };

        /*
         * update Employee
         */
        factory.updateEmployee = function(employeeId, dto) {

          return factory.updateElement('employees', employeeId, dto);
        };

        /*
         * delete Employee
         */
        factory.deleteEmployee = function(employeeId) {

          var url = baseUrl + 'employees/' + employeeId + '/ops/softdelete/';
          return $http.post(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * create Employee
         */
        factory.createEmployee = function(dto) {

            var url = baseUrl + 'employees/';
            return $http.post(url, dto).then(function (response) {
                return factory.toObjectResult(response);
            });
        };

        /*
         * get Employee picture
         */
        factory.getEmployeePicture = function(employeeId, queryParams) {
          return $http.get(baseUrl + 'employees/' + employeeId + '/picture', queryParams).then(function(res){
            return res;
          });
        };

        /*
         * NEW get Employee picture via useraccount
         */
        factory.getUserPicture = function(userId) {
          return $http.get(baseUrl + 'useraccounts/' + userId + '/picture').then(function(res){
            return res;
          });
        };

        /*
         * update Employee picture
         */
        factory.uploadUserPicture = function(userId, fd) {
            return $http.put(baseUrl + 'useraccounts/' + userId + '/picture', fd).then(function(res){
                return res;
            });
        };

        /*
         * update Employee picture
         */
        factory.uploadEmployeePicture = function(employeeId, fd) {
            return $http.put(baseUrl + 'employees/' + employeeId + '/picture', fd).then(function(res){
                return res;
            });
        };

        /*
         * Get Employee autoapprovals
         */
        factory.getEmployeeAutoapprovals = function(employeeId, queryParams) {
          return $http.get(baseUrl + 'employees/' + employeeId + '/autoapprovals', queryParams).then(function(res){
            return res;
          })
            ;
        };

        /*
         * update Employee autoapprovals settings
         */
        factory.updateEmployeeAutoapprovals = function(employeeId, dto) {

          //return factory.updateElement('employees', employeeId, '/autoapprovals', dto);

          var url = baseUrl + 'employees/' + employeeId + '/autoapprovals';
          return $http.put(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * update Employee's Skills
         */
        factory.updateEmployeeSkills = function(employeeId, dto) {
          var url = baseUrl + 'employees/' + employeeId + '/ops/updateemployeeskills';
          return $http.post(url, dto).then(function(res){
            return factory.toObjectResult(res);
          });
        };

        /*
         * update Employee's Teams
         */
        factory.updateEmployeeTeams = function(employeeId, dto) {
          var url = baseUrl + 'employees/' + employeeId + '/ops/updateemployeeteams';
          return $http.post(url, dto).then(function(res){
            return factory.toObjectResult(res);
          });
        };

        /*
         * get Employee preferences
         */
        factory.getEmployeePreferences = function(userId) {
          return $http.get(baseUrl + 'useraccounts/' + userId + '/notificationsettings');
        };

        /*
         * update Employee preferences
         */
        factory.updateEmployeePreferences = function(userId, dto) {
          return $http.put(baseUrl + 'useraccounts/' + userId + '/notificationsettings', dto);
        };

        /*
         * get Employee hours and overtime
         */
        factory.getEmployeeHoursAndOvertime = function(employeeId) {
          return $http.get(baseUrl + 'employees/' + employeeId + '/hoursandovertime');
        };

        /*
         * update Employee hours and overtime
         */
        factory.updateEmployeeHoursAndOvertime = function(employeeId, dto) {
          return $http.put(baseUrl + 'employees/' + employeeId + '/hoursandovertime', dto);
        };

        /*
         * get Employee calendar view
         */
        factory.getEmployeeCalendarView = function(employeeId, queryParams) {
          return $http.get(baseUrl + 'employees/' + employeeId + '/calendarview', queryParams);
        };

        /*
         * Get Employee Calendar and Availability View
         */
        factory.getEmployeeCalendarAndAvailabilityView = function(scheduleId, employeeId, queryParams) {
          return $http.get(baseUrl + 'schedules/' + scheduleId + '/employees/' + employeeId + '/calendarandavailabilityview', queryParams);
        };

        /*
         * get Employee availability/preferences view
         */
        factory.getEmployeeAvailabilityView = function(employeeId, startDate, endDate) {
          var employeeIdPart = employeeId ? ("/" + employeeId) : "";
          return $http.post(baseUrl + 'employees' + employeeIdPart + '/ops/availcal/view?daterangestart=' + startDate + '&daterangeend=' + endDate);
        };

        /*
         * get Employee availability preview
         */
        factory.getEmployeeAvailabilityPreview = function(employeeId, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId + '/ops/availcal/workflowrequestpreview', dto);
        };

        /*
         * update Employee cd availability
         */
        factory.updateEmployeeCDAvailability = function(employeeId, dateRangeStart, dateRangeEnd, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId +
            '/ops/availcal/cdavailupdate?daterangestart=' + dateRangeStart + '&daterangeend=' + dateRangeEnd, dto);
        };

        /*
         * update Employee ci availability
         */
        factory.updateEmployeeCIAvailability = function(employeeId, dateRangeStart, dateRangeEnd, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId +
            '/ops/availcal/ciavailupdate?daterangestart=' + dateRangeStart + '&daterangeend=' + dateRangeEnd, dto);
        };

        /*
         * update Employee cd preference
         */
        factory.updateEmployeeCDPreference = function(employeeId, dateRangeStart, dateRangeEnd, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId +
            '/ops/availcal/cdprefupdate?daterangestart=' + dateRangeStart + '&daterangeend=' + dateRangeEnd, dto);
        };

        /*
         * update Employee ci preference
         */
        factory.updateEmployeeCIPreference = function(employeeId, dateRangeStart, dateRangeEnd, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId +
            '/ops/availcal/ciprefupdate?daterangestart=' + dateRangeStart + '&daterangeend=' + dateRangeEnd, dto);
        };

        /*
         * Update a Weekday Rotation
         */
        factory.updateWeekdayRotation = function(employeeId, dateRangeStart, dateRangeEnd, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId +
            '/ops/availcal/rotationupdate?daterangestart=' + dateRangeStart + '&daterangeend=' + dateRangeEnd, dto);
        };

        /*
         * Copy employee availability
         */
        factory.copyEmployeeAvailability = function(employeeId, dateRangeStart, dateRangeEnd, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId +
            '/ops/availcal/cdcopy?daterangestart=' + dateRangeStart + '&daterangeend=' + dateRangeEnd, dto);
        };

        /*
         * Update a specific week's Max Days Per Week
         */
        factory.updateEmployeeMaxDaysPerWeek = function(employeeId, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId + '/ops/availcal/maxdaysupdate', dto);
        };

        /*
         * Update employee availability couple weekends option
         */
        factory.updateEmployeeCoupleWeekends = function(employeeId, dto) {
          return $http.post(baseUrl + 'employees/' + employeeId + '/ops/availcal/coupleweekendsupdate', dto);
        };

        /*
         * Request availability change (Employee view)
         */
        factory.requestAvailabilityChange = function(dto) {
          return $http.post(baseUrl + 'requests/submitter/', dto);
        };

        /*
         * get the list of Calendar Dependent Availability records associated to an Employee
         */
        factory.getEmployeeCDAvailabilities = function(employeeId, queryParams, pageIndex, pageSize) {

          return factory.getElements('employees/' + employeeId + '/cdavailability', queryParams, pageIndex, pageSize);
        };

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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> posting( ' + url + ')');
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
          ////console.log('--> updating( ' + url + ' params=' + urlParams);
          //console.log('--> updating( ' + url + ') with:' + updateDto);
          var dto = factory.metamodel.toDto(updateDto);
          return $http.put(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.updateEmployeeHomeTeam = function (employeeId, teamId, updateDto) {

          var url = baseUrl + 'employees/' + employeeId + '/teams/' + teamId;
          return $http.put(url, updateDto).then(function (response) {
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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> posting( ' + url + ')');
          return $http.delete(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * upadate a Skill-Employee relationship
         *
         * returns the updated object
         */
        factory.updateEmployeeSkill = function (employeeId, skillId, updateDto) {

          var url = baseUrl + 'employees/' + employeeId + '/skills/' + skillId;
          ////console.log('--> updating( ' + url + ' params=' + urlParams);
          //console.log('--> updating( ' + url + ') with:' + updateDto);
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
          //console.log('--> getting account( ' + url + ')');
          return $http.get(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.updateEmployeeAccount = function (employeeId, dto) {

          var url = baseUrl + 'employees/' + employeeId + '/useraccount';
          //console.log('--> getting account( ' + url + ')');
          return $http.put(url, dto).then(function (response) {
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
         * Get eligible shifts for selected shift
         */
        factory.getSwapEligibleShiftsForShift = function(employeeId, shiftId) {
          var payLoad = {
            shiftId: shiftId
          };

          return $http.post(baseUrl + 'employees/' + employeeId + '/ops/getswapeligibleshifts', payLoad);
        };

        /*
         * Get eligible employees for proposed open shift
         */
        factory.getWipEligibleEmployeesForProposedOpenShift = function(scheduleId, shiftInfo, overrideOptions) {
          var payLoad = {
            teamId: shiftInfo.teamId,
            skillId: shiftInfo.skillId,
            startDateTime: shiftInfo.start,
            endDateTime: shiftInfo.end
          };
          if (overrideOptions !== null) {
            payLoad.overrideOptions = overrideOptions;
          }

          return $http.post(baseUrl + 'schedules/' + scheduleId + '/ops/getproposedopeneshifteligibleemployees', payLoad);
        };

        /*
         * Get drop shift reasons for site
         */
        factory.getDropShiftReasons = function(siteId) {
          return $http.get(baseUrl + "sites/" + siteId + "/dropshiftreasonsandabsencetypes");
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

        /*
         * get the list of Shifts associated to a Schedule using additional filters
         * Note: query parameters and paging parameters are present BUT NOT IMPLEMENTED TODAY
         *
         */
        factory.getScheduleShiftsOps = function(scheduleId, queryParams) {

          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/ops';
          return $http.get(url, {params: queryParams})
            .then(function (response) {
              return response;
            });
        };

        /**
         * get list of Shifts associated to a Schedule and specified by time period
         * filter - array of filters
         */
        factory.getScheduleShiftsByPeriod = function (scheduleId, startDate, endDate, filter, returnedFields) {

          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/ops/get';
          filter = filter.join(';');
          return $http.get(url, {params: {startdate: startDate, enddate: endDate, filter: filter, returnedfields: returnedFields}})
            .then(function (response) {
              return factory.toResultSet(response);
            });
        };

        /*
         * Execute a Schedule        *
         */
        factory.executeSchedule = function(scheduleId, dto) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/execute';
          //console.log('--> executing( ' + url + ')');
          return $http.post(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };


        factory.duplicateSchedule = function (scheduleId, dto) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/duplicate';
          return $http.post(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * promote a Schedule
         */
        factory.promoteSchedule = function(scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/promote';
          //console.log('--> executing( ' + url + ')');
          return $http.post(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * delete a schedule
         */
        factory.deleteSchedule = function(scheduleId) {
          var url = baseUrl + 'schedules/' + scheduleId;

          return $http.delete(url).then(function(response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * reset a Schedule state
         */
        factory.resetScheduleState = function(scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/resetstate';
          //console.log('--> executing( ' + url + ')');
          return $http.post(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * generate Schedule  Shifts
         */
        factory.generateShifts = function(scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/generateshifts';
          //console.log('--> executing( ' + url + ')');
          return $http.post(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * Abort a Schedule        *
         */
        factory.abortSchedule = function(scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/abort';
          //console.log('--> posting( ' + url + ')');
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
          /*
           var params = factory.prepareQueryParams(queryParams, pageIndex, pageSize);
           var specificParams = _.pick(queryParams, 'startdate', 'enddate', 'type', 'employees');
           _.defaults(params, specificParams);
           var url = baseUrl + resource;
           //console.log('dataService --> querying( ' + url + ' q=' + params.filter
           + " orderby: " + params.orderby + '(' + params.orderdir
           + ') from: ' + params.offset + '[' + params.limit + ']'
           + ' between: ' + params.startdate + ' and ' + params.enddate
           + ' for type: ' + params.type + ' and employees: ' + params.employees
           );
           return $http.get(url, {params: params}).then(function (response) {
           return factory.toResultSet(response);
           });
           */
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
          //console.log('--> getting( ' + url + ')');
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
          //console.log('--> updating( ' + url + ')');
          var dto = factory.metamodel.toDto(updateDto);
          return $http.put(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * get Execution report of Schedule
         */
        factory.getScheduleReport = function (scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/executionreport';
          //console.log('--> getting( ' + url + ')');
          return $http.get(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.getScheduleReportExt = function (scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/executionreportext';
          //console.log('--> getting( ' + url + ')');
          return $http.get(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * get Schedule Settings for settings dialog in schedule builder
         */
        factory.getScheduleSettings = function (scheduleId) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/getsettings';
          //console.log('--> getting( ' + url + ')');
          return $http.get(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        /*
         * update Schedule Settings for settings dialog in schedule builder
         */
        factory.updateScheduleSettings = function (scheduleId, updateDto) {

          var url = baseUrl + 'schedules/' + scheduleId + '/ops/setsettings';
          //console.log('--> updating( ' + url + ')');
          var dto = factory.metamodel.toDto(updateDto);
          return $http.put(url, dto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };
        //--------------------------------------------------------------------
        // Shifts related methods
        //--------------------------------------------------------------------

        factory.deleteShift = function (scheduleId, shiftId) {
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/' + shiftId;
          //console.log('--> deleting( ' + url + ')');
          return $http.delete(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.createShift = function (scheduleId, shiftDto) {
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, shiftDto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.updateShift = function (scheduleId, shiftId, updateDto) {
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/' + shiftId;
          //console.log('--> updating( ' + url + ')');
          return $http.put(url, updateDto).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.dropShift = function (scheduleId, shiftId, reasonId) {
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/' + shiftId + '/ops/drop';
          //console.log('--> posting( ' + url + ')');

          console.log(scheduleId, shiftId, reasonId);

          return $http.post(url, reasonId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.assignShift = function (scheduleId, shiftId, employeeId) {
          //TODO: This still uses the deprecated API. It should be changed to use the new API...
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/' + shiftId + '/ops/deprecatedassign';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, employeeId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        // Work in place
        factory.wipShift = function (scheduleId, shiftId, wipEmployeeId) {
          //TODO: This still uses the deprecated API. It should be changed to use the new API...
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/' + shiftId + '/ops/deprecatedwip';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, wipEmployeeId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.swapShift = function (scheduleId, shiftAId, shiftBId) {
          //TODO: This still uses the deprecated API. It should be changed to use the new API...
          var url = baseUrl + 'schedules/' + scheduleId + '/shifts/' + shiftAId + '/ops/deprecatedswap';
          //console.log('--> posting( ' + url + ')');
          return $http.post(url, shiftBId).then(function (response) {
            return factory.toObjectResult(response);
          });
        };

        factory.submitSelectedEligibleEntities = function(eligibleEntityType, scheduleId, originatorShift, selectedEligibleEntity, comment) {
          var urlPart = null;
          var payLoad = null;

          if (eligibleEntityType === 'swap') {
            urlPart = 'schedules/' + scheduleId + '/shifts/' + originatorShift.id + '/ops/swap';
            payLoad = {
              shiftBId: selectedEligibleEntity.shiftId
            };
          } else if (eligibleEntityType === 'wip') {
            urlPart = 'schedules/' + scheduleId + '/shifts/' + originatorShift.id + '/ops/wip';
            payLoad = {
              wipEmployeeId: selectedEligibleEntity.employeeId
            };
          }

          return $http.post(baseUrl + urlPart, payLoad).then(function(response) {
            return factory.toObjectResult(response);
          });
        };

        factory.manageShift = function(scheduleId, shiftId, comment, shiftInfo, osShiftInfo) {
          var urlPart = 'schedules/' + scheduleId + '/shifts/' + shiftId + '/ops/manage';
          var payLoad = {
            comment: comment,
            force: false,
            overrideOptions: null,
            shiftInfo: shiftInfo,
            osShiftInfo: osShiftInfo
          };

          return $http.post(baseUrl + urlPart, payLoad).then(function(response) {
            return factory.toObjectResult(response);
          });
        };

        factory.createShiftByAction = function(scheduleId, shiftInfo, action, employeeId) {
          var urlPart = 'schedules/' + scheduleId + '/ops/createshift';
          var payLoad = {
            shiftInfo: shiftInfo,
            action: action,
            employeeId: employeeId
          };

          return $http.post(baseUrl + urlPart, payLoad).then(function(response) {
            return factory.toObjectResult(response);
          });
        };

        factory.assignShiftToEmployee = function(scheduleId, shiftId, employeeId) {
          var urlPart = 'schedules/' + scheduleId + '/shifts/' + shiftId + '/ops/assign';
          var payLoad = {
            employeeId: employeeId
          };

          return $http.post(baseUrl + urlPart, payLoad).then(function(response) {
            return factory.toObjectResult(response);
          });
        };

        // Delete Open Shift

        factory.deleteOS = function(scheduleId, shiftId) {
          var urlPart = 'schedules/' + scheduleId + '/shifts/' + shiftId;

          return $http.delete(baseUrl + urlPart).then(function(response) {
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
          //console.log('--> posting( ' + url + ')');
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
          //console.log('--> loading( ' + url + ')');
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
          //console.log('--> updating( ' + url + ')');
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
          //console.log('--> deleting( ' + url + ')');
          return $http.delete(url).then(function (response) {
            return factory.toObjectResult(response);
          });
        };



        //--------------------------------------------------------------------
        // Organization level methods
        //--------------------------------------------------------------------


        /* ----------------------      ORGANIZATION settings   -------------------------------*/

        //
        // Get Org details

        factory.getOrgDetails = function(){
          return factory.getElement('org', '');
        };


        //
        // Update Org details
        factory.updOrgDetails = function(dto){
          return factory.updateElement('org', '', dto);
        };


        //
        // Update /org/schedulesettings

        factory.updScheduleSettings = function(dto){
          return factory.updateElement('org', 'schedulingsettings', dto);
        };


        //
        // Get Org summary counters

        factory.getOrgCounters = function(){
          return factory.getElement('org/summary', '');
        };


        //
        // Get service provider summary counters

        factory.getServiceProviderCounters = function(){
          return factory.getElement('serviceproviders/summary', '');
        };


        /* ----------------------      HOLIDAYS   -------------------------------*/


        /*
         * Get the list of Holiday definitions associated to the 'current' organization.
         *
         */
        factory.getHolidays = function() {
          return $http.get(baseUrl + 'org/holiday/');
        };

        /*
         * Updates a Holiday element
         *
         */
        factory.updateHoliday = function(id, dto) {
          return $http.put(baseUrl + 'org/holiday/' + id, dto);
        };

        /*
         * Created a new Holiday element and adds it to the list of Holiday elements associated to the Organization
         *
         */
        factory.createHoliday = function(dto) {
          return $http.post(baseUrl + 'org/holiday/', dto);
        };

        /*
         * Removes a Holiday element specified by its id
         *
         */
        factory.deleteHoliday = function(id) {
          return $http.delete(baseUrl + 'org/holiday/' + id);
        };

        /*
         * Duplicate holidays from one year to another
         *
         */
        factory.duplicateYear = function(from, to) {
          return $http.post(baseUrl + "org/holidays/ops/duplicateyear", {
            yearFrom : from,
            yearTo : to
          });
        };


        /* ----------------------      SKILLS   -------------------------------*/


        /*
         * Get list of all skills
         *
         */
        factory.getSkills = function(queryParams, pageIndex, pageSize, cache) {
          return factory.getElements('skills', queryParams, pageIndex, pageSize, cache);
        };

        /*
         * Gets list of sites with the skill specified by skillId
         *
         */
        factory.getSitesBySkillId = function(skillId) {
          return $http.get(baseUrl + 'skills/' + skillId + "/sites");
        };

        /*
         * Get list of teams with the skill specified by skillId
         *
         */
        factory.getTeamsBySkillId = function(skillId) {
          return $http.get(baseUrl + 'skills/' + skillId + "/teams");
        };

        /*
         * Get list of team associations for skill
         *
         */
        factory.getTeamAssociations = function(skillId) {
          return $http.get(baseUrl + 'skills/' + skillId + "/teamassociations");
        };

        /*
         * Update team associations
         *
         */
        factory.updateTeamAssociations = function(skillId, dto) {
          return $http.post(baseUrl + 'skills/' + skillId + "/teamassociations/ops/update", dto);
        };

        /*
         * Get list of site/team associations for skill
         *
         */
        factory.getSiteTeamAssociations = function(skillId) {
          return $http.get(baseUrl + 'skills/' + skillId + "/siteteamassociations");
        };

        /*
         * Update the Skill identified by skillId
         *
         */
        factory.updateSkill = function(id, dto) {
          return $http.put(baseUrl + 'skills/' + id, dto);
        };

        /*
         * Delete a Skill specified by its id
         *
         */
        factory.deleteSkill = function(id) {
          return $http.delete(baseUrl + 'skills/' + id);
        };

        /*
         * Create a new Skill
         *
         */
        factory.createSkill = function(dto) {
          return $http.post(baseUrl + 'skills', dto);
        };


        //
        // Change Password

        factory.changePassword = function(currentPassword, newPassword) {
          return $http.post('../emlogis/rest/useraccounts/ops/chgpassword', {
            currentPassword: currentPassword,
            newPassword: newPassword
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
            startDate: 'tdate'
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
        //console.log('dataService created.');
        return factory;

      }]);

}());


