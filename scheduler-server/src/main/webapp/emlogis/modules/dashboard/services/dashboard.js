angular.module('emlogis.dashboard')
  .service('DashboardService', ['$http', '$q', '$interval', 'applicationContext',
    'UtilsService', 'authService',
    function ($http, $q, $interval, applicationContext, UtilsService, authService) {
      var baseUrl = applicationContext.getBaseRestUrl();
      var statusValuesOfTeamMemberRequests = {
        PENDING: ['PEER_PENDING'],
        ACCEPTED: ['PEER_APPROVED'],
        APPROVED: ['APPROVED'],
        OTHERS: ['PEER_DECLINED', 'DECLINED', 'WITDRAWN', 'DELETED', 'EXPIRED']
      };
      var statusLabelsOfTeamMemberRequests = {
        PEER_PENDING: 'PENDING',
        PEER_APPROVED: 'ACCEPTED',
        PEER_DECLINED: 'DENIED',
        APPROVED: 'APPROVED',
        DECLINED: 'DECLINED',
        WITHDRAWN: 'WITHDRAWN',
        DELETED: 'DELETED',
        EXPIRED: 'EXPIRED'
      };

      function sendRequest(urlPart, method, requestPayload) {
        var apiUrl = baseUrl + urlPart;
        var req = {
          method: method,
          url: apiUrl
        };
        if (method === 'POST' || method === 'PUT') {
          req.data = requestPayload;
        }

        return $http(req);
      }

      this.getCurrentAccountInfo = function(accountType) {
        var urlPart = '';
        if (accountType === 'Employee') {
          urlPart = 'employees/info';
        }
        else {
          urlPart = 'useraccounts/info';
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.getCurrentAccountAvailability = function(accountType, options) {
        var urlPart = '';
        if (accountType === 'Employee') {
          urlPart = 'useraccounts/employeeavailability?schedulestatus=' +
            options.scheduleStatus + '&startdate=' + options.startDate +
            '&enddate=' + options.endDate + '&returnedfields=' + options.returnedFields;
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.getAccountShifts = function(accountType, accountId, options) {
        var urlPart = '';
        if (accountType === 'Employee') {
          urlPart = 'employees/' + accountId + '/shifts?schedulestatus=' +
            options.scheduleStatus + '&startdate=' + options.startDate +
            '&enddate=' + options.endDate + '&returnedfields=' + options.returnedFields;
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.getAccountAvailabilityAndShifts = function(accountType, accountId, options) {
        var urlPart = '';
        if (accountType === 'Employee') {
          urlPart = 'employees/' + accountId + '/calendarview?schedulestatus=' +
            options.scheduleStatus + '&startdate=' + options.startDate +
            '&enddate=' + options.endDate + '&timezone=' + options.timezone + '&returnedfields=' + options.returnedFields +
            '&requestinfo=true';
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.getEligibleEntities = function(tabType, accountType, accountId, shiftId) {
        var urlPart = '';
        if (accountType === 'Employee') {
          if (tabType === 'swap') {
            urlPart = 'employees/' + accountId + '/ops/getswapeligibleshifts';
          } else if (tabType === 'wip') {
            urlPart = 'employees/' + accountId + '/ops/getwipeligibleemployees';
          }
        }

        var payLoad = {
          shiftId: shiftId
        };

        return sendRequest(urlPart, 'POST', payLoad);
      };

      this.submitSelectedEligibleEntities = function(tabType, accountType, accountId, originatorShift, selectedEligibleEntities) {
        var type = (tabType === 'swap')? 'SHIFT_SWAP_REQUEST': 'WIP_REQUEST';
        var dayInMilliseconds = 24 * 3600000;
        var recipients = [];
        var payLoad = {
          type: type,
          submitterId: accountId,
          expiration: originatorShift.start.unix() * 1000 - dayInMilliseconds,
          submitterShiftId: originatorShift.id,
          comment: ''
        };

        if (accountType === 'Employee') {
          if (tabType === 'swap') {
            angular.forEach(selectedEligibleEntities, function(entityIterator) {
              recipients.push(
                {
                  shiftId: entityIterator.id,
                  employeeId: entityIterator.teamMemberId
                });
            });
            payLoad.assignments = recipients;
          } else {
            angular.forEach(selectedEligibleEntities, function(entityIterator) {
              recipients.push(entityIterator.id);
            });
            payLoad.recipientIds = recipients;
          }
        }

        var urlPart = 'requests/submitter';

        return sendRequest(urlPart, 'POST', payLoad);
      };

      this.getCurrentAccountOpenShifts = function(accountType, options) {
        var urlPart = '';
        if (accountType === 'Employee') {
          urlPart = 'useraccounts/employeepostedopenshifts?startdate=' + options.startDate +
            '&enddate=' + options.endDate;
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.submitSelectedOpenShifts = function(accountType, accountId, tickInfo, selectedOpenShifts) {
        var type = 'OPEN_SHIFT_REQUEST';
        var dayInMilliseconds = 24 * 3600000;
        var urlPart = 'requests/submitter';
        var payLoad = {
          type: type,
          submitterId: accountId,
          expiration: tickInfo.end + 7 * dayInMilliseconds,
          comment: '',
          openShifts: []
        };

        angular.forEach(selectedOpenShifts, function(entityIterator) {
          var identifier = 'shiftId-' + entityIterator.shiftId;
          payLoad.openShifts.push({shiftId: entityIterator.shiftId, identifier: identifier});
        });

        return sendRequest(urlPart, 'POST', payLoad);
      };

      this.getAbsenceTypes = function(siteId) {
        var urlPart = 'sites/' + siteId + '/absencetypes?filter=isActive=1';

        return sendRequest(urlPart, 'GET', null);
      };

      this.submitPtoRequest = function(accountType, accountId, requestData) {
        var urlPart = 'requests/submitter';
        var dayInMilliseconds = 24 * 3600000;
        var payLoad = {
          type: 'TIME_OFF_REQUEST',
          submitterId: accountId,
          expiration: requestData.timeOffStartDate,
          comment: requestData.note,
          requests: []
        };

        for (var i=0; i<requestData.lengthInDays; i++) {
          var identifier = 'date-' + (requestData.timeOffStartDate + dayInMilliseconds * i);
          payLoad.requests.push({
            identifier: identifier,
            date: requestData.timeOffStartDate + dayInMilliseconds * i,
            absenceTypeId: requestData.timeOffType.id,
            reason: ''
          });
        }

        return sendRequest(urlPart, 'POST', payLoad);
      };


      this.getAccountTeamMemberRequests = function(payLoad) {
        var urlPart = 'requests/peer/ops/queryold';
        return sendRequest(urlPart, 'POST', payLoad);
      };

      this.respondToAccountTeamMemberRequest = function(teamMemberRequest, acceptance) {
        var urlPart = 'requests/peer/' + teamMemberRequest.requestId + '/ops/';

        if (acceptance) {
          urlPart += 'approve';
        } else {
          urlPart += 'decline';
        }

        var payLoad = {
          comment: (acceptance)? "Approved": "Declined"
        };

        if (teamMemberRequest.type === 'SHIFT_SWAP_REQUEST' && acceptance) {
          payLoad.shiftIdList = [];
//          payLoad.shiftId = teamMemberRequest.recipientShift.id;
          payLoad.shiftIdList.push(teamMemberRequest.recipientShift.id);
        }

        return sendRequest(urlPart, 'POST', payLoad);
      };

      this.getStatusValuesOfTeamMemberRequests = function(statusLabel) {
        return statusValuesOfTeamMemberRequests[statusLabel];
      };

      this.getStatusLabelOfTeamMemberRequest = function(status) {
        return statusLabelsOfTeamMemberRequests[status];
      };

      this.getRequestStatuses = function() {

//        PEER_PENDING, ADMIN_PENDING, APPROVED,DECLINED, WITHDRAWN,EXPIRED,DELETED

        return [
          {id:'ADMIN_PENDING',name:'Admin Pending',ticked:true},
          {id:'APPROVED',name:'Approved',ticked:true},
          {id:'DECLINED',name:'Declined',ticked:true},
          {id:'DELETED',name:'Deleted',ticked:true},
          {id:'EXPIRED',name:'Expired',ticked:true},
          {id:'PEER_PENDING',name:'Peer Pending',ticked:true},
          {id:'WITHDRAWN',name:'Withdrawn',ticked:true}
        ];

      };

      this.getRequestTypes = function() {

        return [
          {id:'AVAILABILITY_REQUEST',name:'Availability',ticked: false},
          {id:'OPEN_SHIFT_REQUEST',name:'Open Shift',ticked: false},
          {id:'SHIFT_SWAP_REQUEST',name:'Shift Swap',ticked: true},
          {id:'TIME_OFF_REQUEST', name:'Time Off',ticked: true},
          {id:'WIP_REQUEST',name:'Work in Place',ticked: true}
        ];
      };

      /**
       * Only for team member requests with label change of peer_pending -> pendng
       *
       */
      this.getTeamRequestStatuses = function() {

//        PEER_PENDING, ADMIN_PENDING, APPROVED,DECLINED, WITHDRAWN,EXPIRED,DELETED

        return [
          {id:'PEER_APPROVED',name:'Admin Pending',ticked:true},
          {id:'APPROVED',name:'Approved',ticked:true},
          {id:'DECLINED',name:'Declined',ticked:true},
          {id:'DELETED',name:'Deleted',ticked:true},
          {id:'EXPIRED',name:'Expired',ticked:true},
          {id:'PEER_PENDING',name:'Pending',ticked:true},
          {id:'WITHDRAWN',name:'Withdrawn',ticked:true}
        ];

      };

    }
  ]);