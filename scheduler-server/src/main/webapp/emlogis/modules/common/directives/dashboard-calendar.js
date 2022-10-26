angular.module('emlogis.commonDirectives').directive('dashboardCalendar', ['$modal', '$timeout', '$filter', 'uiCalendarConfig', 'applicationContext',
  function ($modal, $timeout, $filter, uiCalendarConfig, applicationContext) {

    return {
      restrict: 'E',
      scope: {
        getAccountInfo: '&',
        getShifts: '&',
        managerView: '=',
        getEligibleEntities: '&',
        getOpenShifts: '&',
        submitSelectedEligibleEntities: '&',
        submitSelectedOpenShifts: '&',
        submitPtoRequest: '&'
      },
      controller: function($scope) {
        $scope.shiftsLoaded = false;
        $scope.shiftsSource = [];
        $scope.requestsInfo = [];
        $scope.accountInfo = {};
        $scope.selectedDate = null;
        $scope.datePickerOpened = false;
        $scope.syncCalendarToolTip = 'Synch your calendar with your email apps like iCalendar, Outlook, or Gmail.';

        $scope.openDatePicker = function($event) {
          $event.preventDefault();
          $event.stopPropagation();
          $scope.datePickerOpened = true;
        };

        $scope.datePickerOptions = {
          formatYear: 'yyyy',
          startingDay: 1
        };

        $scope.openSyncCalendarPopup = function() {
          $modal.open({
            templateUrl: 'syncCalendarPopupModal.html',
            controller: 'SyncCalendarPopupModalInstanceCtrl',
            windowClass: 'sync-calendar-modal',
            resolve: {
              calendarSyncUrl: function() {
                var fullUrl = 'webcal://';
                fullUrl += location.host + '/scheduler-server/emlogis' + $scope.accountInfo.calendarSyncUrl;

                return fullUrl;
              }
            }
          });
        };

        var populateShiftsSource = function(responseData) {
          var shift = {};
          var teams = $scope.accountInfo.teams;
          var className = '';
          var localTodayDate = moment.tz(new Date(), $scope.accountInfo.timezone);
          localTodayDate.hours(0);
          localTodayDate.minutes(0);
          localTodayDate.seconds(0);
          localTodayDate.milliseconds(0);
          var todayStartTime = localTodayDate.unix() * 1000;
          var todayEndTime = todayStartTime + 24 * 3600 * 1000;
          angular.forEach(responseData.shifts.result, function(row) {
            var typeStr = '';
            if (row[3] === true) {
              typeStr = 'extra';
            } else if (row[3] === false) {
              typeStr = 'regular';
            } else {
              typeStr = 'overtime';
            }
            if (row[1] < todayStartTime) {
              className = 'fc-past';
            } else if (row[1] > todayEndTime) {
              className = 'fc-future';
            } else {
              className = 'fc-today';
            }
            shift = {
              id: row[0],
              title: 'Shift',
              start: moment.tz(row[1], $scope.accountInfo.timezone),
              end: moment.tz(row[2], $scope.accountInfo.timezone),
              type: typeStr,
              skillAbbrev: row[4],
              skill: row[5],
              assignment: '',
              team: row[6],
              comment: row[7],
              className: className
            };
            var findTeam = _.find(teams, function(team) { return team.name === row[6]; });
            shift.isHomeTeam = findTeam.isHomeTeam;
            $scope.shiftsSource.push(shift);
          });
          angular.forEach(responseData.empUnavailabilities, function(empUnavailability) {
            shift = {
              title: 'PTO Vacation',
              start: moment.tz(empUnavailability.startDate, $scope.accountInfo.timezone),
              end: moment.tz(empUnavailability.endDate, $scope.accountInfo.timezone),
              className: 'pto-vacation-container',
              type: 'pto-vacation',
              status: empUnavailability.status || ""
            };
            $scope.shiftsSource.push(shift);
          });
          angular.forEach(responseData.orgHolidays, function(orgHoliday) {
            shift = {
              title: 'Company Holiday',
              start: moment.tz(orgHoliday.startDate, $scope.accountInfo.timezone),
              end: moment.tz(orgHoliday.endDate, $scope.accountInfo.timezone),
              className: 'company-holiday-container',
              type: 'company-holiday'
            };
            $scope.shiftsSource.push(shift);
          });
        };

        var populateRequestsInfo = function(responseData) {
          var requestInfo = {};

          angular.forEach(responseData.openShiftsByDays, function(shiftsNumber, shiftsDuration) {
            var startDateInMilliseconds = parseInt(shiftsDuration.substring(0, shiftsDuration.indexOf('-')));
            var endDateInMilliseconds = parseInt(shiftsDuration.substring(shiftsDuration.indexOf('-') + 1));
            requestInfo = {
              title: 'OS ' + shiftsNumber,
              date: moment.tz(startDateInMilliseconds, $scope.accountInfo.timezone).format('YYYY-MM-DD'),
              start: startDateInMilliseconds,
              end: endDateInMilliseconds,
              className: 'open-shifts-tick-container',
              type: 'open-shifts-tick',
              openShiftsNumber: shiftsNumber
            };
            $scope.requestsInfo.push(requestInfo);
          });

          angular.forEach(responseData.submittedWipSwapRequestInfos, function(info) {
            if (info.requestStatus !== 'PEER_PENDING' && info.requestStatus !== 'ADMIN_PENDING') {
              return;
            }
            var originatorShift = _.find(responseData.shifts.result, function(iterator) {
              return (iterator[0] === info.shiftId);
            });
            if (typeof originatorShift === 'undefined' || originatorShift === null) {
              console.log('------------Submitted shift is no longer available.-------------\n');
              console.log(info);
              return;
            }
            requestInfo = {
              type: 'submitted-wip-swap-request',
              title: (info.requestType === 'SHIFT_SWAP_REQUEST')? 'SWAP->': 'WIP->',
              subType: (info.requestType === 'SHIFT_SWAP_REQUEST')? 'submitted-swap-request': 'submitted-wip-request',
              date: moment.tz(originatorShift[1], $scope.accountInfo.timezone).format('YYYY-MM-DD'),
              originatorShiftId: originatorShift[0],
              className: 'submitted-wip-swap-info'
            };
            $scope.requestsInfo.push(requestInfo);
          });

          angular.forEach(responseData.peerSwapRequestInfos, function(info) {
            if (info.peerStatus !== 'PEER_PENDING' && info.peerStatus !== 'PEER_APPROVED') {
              return;
            }
            var peerShift = _.find(responseData.shifts.result, function(iterator) {
              return (iterator[0] === info.peerShiftId);
            });
            if (typeof peerShift === 'undefined' || peerShift === null) {
              console.log('------------Peer swap requested shift is no longer available.-------------\n');
              console.log(info);
              return;
            }
            requestInfo = {
              type: 'peer-swap-request',
              title: 'SWAP<-',
              date: moment.tz(peerShift[1], $scope.accountInfo.timezone).format('YYYY-MM-DD'),
              peerShiftId: peerShift[0],
              className: 'peer-swap-info'
            };
            $scope.requestsInfo.push(requestInfo);
          });

          angular.forEach(responseData.peerWipRequestInfos, function(info) {
            if (info.peerStatus !== 'PEER_PENDING' && info.peerStatus !== 'PEER_APPROVED') {
              return;
            }
            requestInfo = {
              type: 'peer-wip-request',
              title: 'WIP<-',
              date: moment.tz(info.submitterShiftStartDateTime, $scope.accountInfo.timezone).format('YYYY-MM-DD'),
              className: 'peer-wip-info'
            };
            $scope.requestsInfo.push(requestInfo);
          });

          angular.forEach(responseData.submittedTimeOffRequestInfos, function(info) {
            if (info.requestStatus !== 'PEER_PENDING' && info.requestStatus !== 'ADMIN_PENDING') {
              return;
            }
            requestInfo = {
              type: 'submitted-time-off-request',
              title: 'Time Off',
              date: moment.tz(info.requestDate, $scope.accountInfo.timezone).format('YYYY-MM-DD'),
              className: 'submitted-time-off-info'
            };
            $scope.requestsInfo.push(requestInfo);
          });

          angular.forEach(responseData.submittedOsRequestInfos, function(info) {
            if (info.requestStatus !== 'PEER_PENDING' && info.requestStatus !== 'ADMIN_PENDING') {
              return;
            }
            requestInfo = {
              type: 'submitted-os-request',
              title: 'OS',
              date: moment.tz(info.requestDate, $scope.accountInfo.timezone).format('YYYY-MM-DD'),
              className: 'submitted-os-info'
            };
            $scope.requestsInfo.push(requestInfo);
          });
        };

        var renderRequestsInfo = function() {

          // Prepare rendering data
          var requestsInfo = {};
          angular.forEach($scope.requestsInfo, function(requestInfo) {
            if (typeof requestsInfo[requestInfo.date] === 'undefined') {
              requestsInfo[requestInfo.date] = {};
            }
            if (requestInfo.type === 'open-shifts-tick') {
              requestsInfo[requestInfo.date][requestInfo.type] = {
                start: requestInfo.start,
                end: requestInfo.end,
                title: requestInfo.title
              };
            } else if (requestInfo.type === 'submitted-wip-swap-request') {
              if (typeof requestsInfo[requestInfo.date][requestInfo.subType] === 'undefined') {
                requestsInfo[requestInfo.date][requestInfo.subType] = [requestInfo.originatorShiftId];
              } else {
                if (requestsInfo[requestInfo.date][requestInfo.subType].indexOf(requestInfo.originatorShiftId) < 0) {
                  requestsInfo[requestInfo.date][requestInfo.subType].push(requestInfo.originatorShiftId);
                }
              }
            } else if (requestInfo.type === 'peer-swap-request') {
              if (typeof requestsInfo[requestInfo.date][requestInfo.type] === 'undefined') {
                requestsInfo[requestInfo.date][requestInfo.type] = [requestInfo.peerShiftId];
              } else {
                if (requestsInfo[requestInfo.date][requestInfo.type].indexOf(requestInfo.peerShiftId) < 0) {
                  requestsInfo[requestInfo.date][requestInfo.type].push(requestInfo.peerShiftId);
                }
              }
            } else {
              requestsInfo[requestInfo.date][requestInfo.type] = true;
            }
          });

          // Draw actual requests info
          angular.forEach(requestsInfo, function(dateRequestsInfo, date) {
            var dateHeaderElement = $('.fc-day-number[data-date="' + date + '"]');
            var dateText = moment.tz(date, $scope.accountInfo.timezone).format('D');
            var dateTableElement = dateHeaderElement.parent().parent().parent();
            var headerInnerHTML = '<div class="date-header-val">' + dateText + '</div><div class="date-header-requests-info text-center">';

            // Date header requests info rendering
            if (dateRequestsInfo['peer-wip-request']) {
              headerInnerHTML += '<span class="peer-wip-request request-icon-container"><img src="/scheduler-server/emlogis/img/peer-wip-request.svg" width="18"></span>';
            }
            if (dateRequestsInfo['submitted-os-request']) {
              headerInnerHTML += '<span class="submitted-os-request request-icon-container"><img src="/scheduler-server/emlogis/img/submitted-os-request.svg" width="18"></span>';
            }
            if (dateRequestsInfo['submitted-time-off-request']) {
              headerInnerHTML += '<span class="submitted-time-off-request request-icon-container"><img src="/scheduler-server/emlogis/img/submitted-time-off-request.svg" width="18"></span>';
            }
            headerInnerHTML += '</div><div class="date-header-os-tick text-right">';
            if (typeof dateRequestsInfo['open-shifts-tick'] !== 'undefined') {
              headerInnerHTML += '<span class="open-shifts-tick"><a id="os-link-' + date + '" href>' + dateRequestsInfo['open-shifts-tick']['title'] + '</a></span>';
            }
            headerInnerHTML += '</div><div class="clearfix"></div>';
            dateHeaderElement.html(headerInnerHTML);
            angular.element('#os-link-' + date).click(function(e) {
              e.preventDefault();
              angular.element(this).scope().openRequestOpenShiftsModal(dateRequestsInfo['open-shifts-tick']['start'], dateRequestsInfo['open-shifts-tick']['end']);
            });

            // Date body requests info rendering
            $timeout(function() {
              if (typeof dateRequestsInfo['submitted-swap-request'] !== 'undefined') {
                angular.forEach(dateRequestsInfo['submitted-swap-request'], function(originatorShiftId) {
                  var originatorShiftElement = dateTableElement.find('.' + originatorShiftId);
                  var originalShiftRequestInfoElement = originatorShiftElement.find('.fc-request-info');
                  originalShiftRequestInfoElement.append('<span class="submitted-swap-request request-icon-container"><img src="/scheduler-server/emlogis/img/submitted-swap-request.svg" width="18"></span>');
                });
              }
              if (typeof dateRequestsInfo['submitted-wip-request'] !== 'undefined') {
                angular.forEach(dateRequestsInfo['submitted-wip-request'], function(originatorShiftId) {
                  var originatorShiftElement = dateTableElement.find('.' + originatorShiftId);
                  var originalShiftRequestInfoElement = originatorShiftElement.find('.fc-request-info');
                  originalShiftRequestInfoElement.append('<span class="submitted-wip-request request-icon-container"><img src="/scheduler-server/emlogis/img/submitted-wip-request.svg" width="18"></span>');
                });
              }
              if (typeof dateRequestsInfo['peer-swap-request'] !== 'undefined') {
                angular.forEach(dateRequestsInfo['peer-swap-request'], function(peerShiftId) {
                  var peerShiftElement = dateTableElement.find('.' + peerShiftId);
                  var peerShiftRequestInfoElement = peerShiftElement.find('.fc-request-info');
                  peerShiftRequestInfoElement.append('<span class="peer-swap-request request-icon-container"><img src="/scheduler-server/emlogis/img/peer-swap-request.svg" width="18"></span>');
                });
              }
            }, 500);
          });
        };

        $scope.initializeShifts = function() {
          var dayInMilliseconds = 24 * 3600 * 1000;
          var nextMonthFirstDate = new Date($scope.selectedDate.getFullYear(), $scope.selectedDate.getMonth() + 1, 1);
          var selectedMonthLastDate = new Date(nextMonthFirstDate.getTime() - dayInMilliseconds);

          var selectedMonthFirstDateInArrFormat = [$scope.selectedDate.getFullYear(), $scope.selectedDate.getMonth(), 1];
          var localFirstDate = moment.tz(selectedMonthFirstDateInArrFormat, $scope.accountInfo.timezone);
          var selectedMonthLastDateInArrFormat = [selectedMonthLastDate.getFullYear(), selectedMonthLastDate.getMonth(), selectedMonthLastDate.getDate()];
          var localLastDate = moment.tz(selectedMonthLastDateInArrFormat, $scope.accountInfo.timezone);

          var startDate = new Date(localFirstDate).getTime();
          var endDate = new Date(localLastDate).getTime();

          $scope.shiftsSource = [];
          $scope.requestsInfo = [];
          $scope.getShifts({startDate: startDate, endDate: endDate}).then(function(response) {
            uiCalendarConfig.calendars.dashboardCalendar.fullCalendar('removeEvents');

            // Populate Shifts
            populateShiftsSource(response.data);

            // Populate Requests Info
            populateRequestsInfo(response.data);

            $scope.shiftsLoaded = true;
            angular.forEach($scope.shiftsSource, function(shift) {
              uiCalendarConfig.calendars.dashboardCalendar.fullCalendar('renderEvent', shift);
            });

            renderRequestsInfo();

            $timeout(function() {
              $scope.styleShifts();
            }, 0);
          }, function(err) {
            uiCalendarConfig.calendars.dashboardCalendar.fullCalendar('removeEvents');
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          }).finally(function() {
            uiCalendarConfig.calendars.dashboardCalendar.fullCalendar('unselect');
            $(window).resize();
          });
        };

        $scope.styleShifts = function() {
          var fillBackgroundOfElements = function(containerType) {
            var containerElements = angular.element('.' + containerType);
            angular.forEach(containerElements, function(container) {
              var classList = container.classList;
              var dateClass = _.find(classList, function(classItem) { return classItem.indexOf('shift-start-') > -1; });
              var shiftStartDate = dateClass.substring(12);
              var bgElement = container.parentElement.parentElement.parentElement.parentElement.parentElement.parentElement.children[0];
              var matchingBg = _.find(bgElement.children[0].children[0].children[0].children, function(child) {
                return child.getAttribute('data-date') === shiftStartDate;
              });
              if (typeof matchingBg !== 'undefined' && matchingBg !== null) {
                matchingBg.className = matchingBg.className + ' ' + containerType + '-bg';
              }
            });
          };
          fillBackgroundOfElements('company-holiday-container');
          fillBackgroundOfElements('pto-vacation-container');

          var positionCompanyHolidays = function() {
            var containerElements = angular.element('.company-holiday-container');
            angular.forEach(containerElements, function(container) {
              var tr = container.parentElement.parentElement;
              var trOffsetTop = tr.offsetTop;
              var cellHeight = angular.element(tr.offsetParent.parentElement.parentElement).height();
              var offsetTop = cellHeight - trOffsetTop - 39;
              container.style.top = offsetTop + 'px';
              container.style.left = '0';
            });
          };
          positionCompanyHolidays();
        };

        $scope.requestTimeOff = function() {
          var modalInstance = $modal.open({
            templateUrl: 'ptoRequestPopupModal.html',
            controller: 'PtoRequestPopupModalInstanceCtrl',
            windowClass: 'pto-request-modal',
            resolve: {
              siteId: function() {
                return $scope.accountInfo.siteId;
              },
              timezone: function() {
                return $scope.accountInfo.timezone;
              }
            }
          });

          modalInstance.result.then(function(modalResult) {
            var results = [];

            $scope.submitPtoRequest({requestData: modalResult.ptoRequest}).then(function(response) {
              angular.forEach(response.data.data.errors, function(error) {
                var identifier = error.identifier;
                var dateVal = moment.tz(parseInt(identifier.substr(5)), $scope.accountInfo.timezone).format('YYYY-MM-DD');
                var message = dateVal + ': ' + $filter('translate')("home.ERROR_OCCURRED");
                results.push({identifier: identifier, message: message});
              });

              angular.forEach(response.data.data.created, function(createdItem) {
                var identifier = createdItem.identifier;
                var dateVal = moment.tz(parseInt(identifier.substr(5)), $scope.accountInfo.timezone).format('YYYY-MM-DD');
                var message = dateVal + ': ' + createdItem.requestStatus;
                results.push({identifier: identifier, message: message});
              });

              hideFutureMonthWeeks();
              $scope.initializeShifts();
              $scope.applyTodayStyle('.fc-bg');
              $scope.applyTodayStyle('.fc-content-skeleton');
              $modal.open({
                templateUrl: 'actionResultPopupModal.html',
                controller: 'ActionResultPopupModalInstanceCtrl',
                windowClass: 'action-result-modal',
                resolve: {
                  results: function() {
                    return results;
                  }
                }
              });
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
            });
          });
        };

        $scope.openRequestOpenShiftsModal = function(start, end) {
          var tickInfo = {
            start: start,
            end: end
          };

          var modalInstance = $modal.open({
            templateUrl: 'openShiftsPopupModal.html',
            controller: 'OpenShiftsPopupModalInstanceCtrl',
            size: 'lg',
            windowClass: 'open-shifts-modal',
            resolve: {
              tickInfo: function() {
                return tickInfo;
              },
              getOpenShifts: function() {
                return $scope.getOpenShifts;
              },
              timezone: function() {
                return $scope.accountInfo.timezone;
              }
            }
          });
          modalInstance.result.then(function(modalResult) {
            var results = [];

            $scope.submitSelectedOpenShifts({tickInfo: tickInfo, selectedOpenShifts: modalResult.selectedOpenShifts}).then(function(response) {
              angular.forEach(response.data.data.errors, function(error) {
                var identifier = error.identifier;
                var openShiftId = identifier.substr(8);
                var errorOpenShift = _.find(modalResult.selectedOpenShifts, 'shiftId', openShiftId);
                var message = errorOpenShift.date + ' ' + errorOpenShift.shift + ' ' + errorOpenShift.team + ' ' + errorOpenShift.skill + ': ' + $filter('translate')("home.ERROR_OCCURRED");
                results.push({identifier: identifier, message: message});
              });

              angular.forEach(response.data.data.created, function(createdItem) {
                var identifier = createdItem.identifier;
                var openShiftId = identifier.substr(8);
                var openShiftItem = _.find(modalResult.selectedOpenShifts, 'shiftId', openShiftId);
                var message = openShiftItem.date + ' ' + openShiftItem.shift + ' ' + openShiftItem.team + ' ' + openShiftItem.skill + ': ' + createdItem.requestStatus;
                results.push({identifier: identifier, message: message});
              });

              hideFutureMonthWeeks();
              $scope.initializeShifts();
              $scope.applyTodayStyle('.fc-bg');
              $scope.applyTodayStyle('.fc-content-skeleton');
              $modal.open({
                templateUrl: 'actionResultPopupModal.html',
                controller: 'ActionResultPopupModalInstanceCtrl',
                windowClass: 'action-result-modal',
                resolve: {
                  results: function() {
                    return results;
                  }
                }
              });
            }, function(error) {
              applicationContext.setNotificationMsgWithValues(JSON.stringify(error.data), 'danger', true);
            });
          });
        };

        $scope.onShiftClick = function(shift, jsEvent, view) {
          if (shift.className.indexOf('fc-past') > -1 ||
            shift.type === 'pto-vacation' || shift.type === 'company-holiday') {
            return;
          }

          var modalInstance = $modal.open({
            templateUrl: 'manageShiftPopupModal.html',
            controller: 'ManageShiftPopupModalInstanceCtrl',
            size: 'lg',
            windowClass: 'manage-shift-modal',
            resolve: {
              originatorShift: function () {
                return shift;
              },
              getEligibleEntities: function() {
                return $scope.getEligibleEntities;
              },
              timezone: function() {
                return $scope.accountInfo.timezone;
              }
            }
          });
          modalInstance.result.then(function(modalResult) {
            $scope.submitSelectedEligibleEntities({tabType: modalResult.tabType, originatorShift: shift, selectedEligibleEntities: modalResult.selectedEligibleEntities}).then(function(response) {
              var tabTypeMessage = '';
              var message = '';
              if (modalResult.tabType === 'swap') {
                tabTypeMessage = $filter('translate')('home.SHIFT_SWAP_REQUEST');
              } else if (modalResult.tabType === 'wip') {
                tabTypeMessage = $filter('translate')('home.WORK_IN_PLACE_REQUEST');
              }
              if (response.data.data.errors.length > 0) {
                message = tabTypeMessage + ': ' + $filter('translate')("home.ERROR_OCCURRED");
                applicationContext.setNotificationMsgWithValues(message, 'danger', true);
              } else {
                message = tabTypeMessage + ': ' + response.data.data.created[0].requestStatus;
                applicationContext.setNotificationMsgWithValues(message, 'success', true);
              }

              hideFutureMonthWeeks();
              $scope.initializeShifts();
              $scope.applyTodayStyle('.fc-bg');
              $scope.applyTodayStyle('.fc-content-skeleton');
            }, function(error) {
              applicationContext.setNotificationMsgWithValues(JSON.stringify(error.data), 'danger', true);
            });
          });
        };

        $scope.eventRender = function(event, element, view) {
          var innerHTML = '';
          var simpleDateStr = event.start.format('YYYY-MM-DD');
          var shift = event;

          if (shift.type === 'company-holiday') {
            element.parent().css('position', 'relative');
            element.css('position', 'absolute');
            element.addClass('shift-start-' + simpleDateStr);
            innerHTML = '<div class="fc-content fc-content-type-' + shift.type + '"><div class="fc-title">' +
              shift.title + '</div></div>';
          } else if (shift.type === 'pto-vacation') {
            element.addClass('shift-start-' + simpleDateStr);
            innerHTML = '<div class="fc-content fc-content-type-' + shift.type + '"><div class="fc-title">' +
              shift.title + '</div><div class="fc-status">' + shift.status + '</div></div>';
          } else {
            var shiftTypeLabel;
            var startTime = moment.tz(shift.start, $scope.accountInfo.timezone);
            var endTime = moment.tz(shift.end, $scope.accountInfo.timezone);
            var startTimeLabel = '';
            var endTimeLabel = '';
            var hasComment = (typeof shift.comment !== 'undefined' && shift.comment !== null)? 'has-comment': '';

            if (shift.type === 'regular') {
              shiftTypeLabel = '';
            } else if (shift.type === 'extra') {
              shiftTypeLabel = 'Extra';
            } else if (shift.type === 'overtime') {
              shiftTypeLabel = 'OT';
            }

            if (startTime.minutes() > 0) {
              startTimeLabel = startTime.format('h:mma');
            } else {
              startTimeLabel = startTime.format('ha');
            }
            startTimeLabel = startTimeLabel.substr(0, startTimeLabel.length - 1);
            if (endTime.minutes() > 0) {
              endTimeLabel = endTime.format('h:mma');
            } else {
              endTimeLabel = endTime.format('ha');
            }
            endTimeLabel = endTimeLabel.substr(0, endTimeLabel.length - 1);

            var dateLabel = startTime.format('YYYY-MM-DD');
            innerHTML = '<div class="fc-content fc-content-type-' + shift.type + ' ' + dateLabel + ' ' + shift.id + ' ' + hasComment + '"><div class="fc-info-block fc-info-block-normal"><div class="fc-time-type-skill fc-info-row"><span class="fc-time">' + startTimeLabel + '-' + endTimeLabel +
              '</span><span class="fc-type"> ' + shiftTypeLabel + '</span><span class="fc-skill">' + shift.skillAbbrev + '</span></div><div class="fc-assignment fc-info-row">' + shift.assignment +
              '</div></div><div class="fc-info-block fc-info-block-highlight"><div class="fc-team fc-info-row"><i class="fa fa-home team-home-' + shift.isHomeTeam + '"></i> ' + shift.team + '</div></div><div class="fc-request-info text-center"></div></div>';
          }

          element.html(innerHTML);
          if (typeof shift.comment !== 'undefined' && shift.comment !== null) {
            element.children('.fc-content').tooltip({content: shift.comment, position: {my: "center bottom", at: "center top"}});
          }
        };

        $scope.applyTodayStyle = function(selector) {
          var localTodayDate = moment.tz(new Date(), $scope.accountInfo.timezone);
          var localTodayDateStr = localTodayDate.format('YYYY-MM-DD');
          var bgTaggedAsToday = angular.element(selector + ' .fc-today');
          if (bgTaggedAsToday.length > 0) {
            if (new Date(bgTaggedAsToday.attr('data-date')) < new Date(localTodayDateStr)) {
              bgTaggedAsToday.removeClass('fc-today');
              bgTaggedAsToday.removeClass('fc-state-highlight');
              bgTaggedAsToday.addClass('fc-past');
            } else if (new Date(bgTaggedAsToday.attr('data-date')) > new Date(localTodayDateStr)) {
              bgTaggedAsToday.removeClass('fc-today');
              bgTaggedAsToday.removeClass('fc-state-highlight');
              bgTaggedAsToday.addClass('fc-future');
            }
          }
          _.forEach(angular.element(selector), function(rowBg) {
            var todayBg = _.find(rowBg.children[0].children[0].children[0].children, function(dayBg) {
              return dayBg.getAttribute('data-date') === localTodayDateStr;
            });
            if (typeof todayBg !== 'undefined' && todayBg !== null) {
              todayBg.classList.remove('fc-past');
              todayBg.classList.remove('fc-future');
              todayBg.classList.add('fc-today');
              todayBg.classList.add('fc-state-highlight');
              if (selector === '.fc-bg') {
                todayBg.style['background-color'] = '#fff';
                todayBg.style['box-shadow'] = '0 0 40px #555';
              }
              return false;
            }
          });
        };

        $scope.calendarConfig = {
          calendar: {
            height: 1011,
            header:{
              left: 'title',
              center: '',
              right: ''
            },
            eventClick: $scope.managerView ? function() {} : $scope.onShiftClick,
            eventRender: $scope.eventRender
          }
        };

        var hideFutureMonthWeeks = function() {
          var weekRows = angular.element('.fc-widget-content .fc-week');
          angular.forEach(weekRows, function(weekRow) {
            var futureMonthElements = angular.element(weekRow).children('.fc-bg').find('.fc-other-month');
            if (futureMonthElements.length === 7) {
              angular.element(weekRow).css('display', 'none');
            }
          });
        };

        $scope.$watch('selectedDate', function() {
          if (typeof uiCalendarConfig.calendars.dashboardCalendar !== 'undefined' && uiCalendarConfig.calendars.dashboardCalendar !== null) {
            if (typeof $scope.selectedDate !== 'undefined' && $scope.selectedDate !== null) {
              var selectedDateInArrFormat = [$scope.selectedDate.getFullYear(), $scope.selectedDate.getMonth(), $scope.selectedDate.getDate()];
              var localDate = moment.tz(selectedDateInArrFormat, $scope.accountInfo.timezone);
              var currentCalendarDate = uiCalendarConfig.calendars.dashboardCalendar.fullCalendar('getDate');
              if (!$scope.shiftsLoaded || localDate.format('YYYY-MM') !== currentCalendarDate.format('YYYY-MM')) {
                $scope.shiftsLoaded = false;
                uiCalendarConfig.calendars.dashboardCalendar.fullCalendar('gotoDate', localDate);

                hideFutureMonthWeeks();
                $scope.initializeShifts();
                $scope.applyTodayStyle('.fc-bg');
                $scope.applyTodayStyle('.fc-content-skeleton');
              }
            }
          }
        });
      },
      link: function(scope, element, attrs) {
        scope.getAccountInfo().then(function(response) {
          scope.accountInfo = response;
          scope.calendarConfig.calendar.timezone = scope.accountInfo.timezone;
          var firstDayNumber = 0;
          switch (scope.accountInfo.siteFirstDayOfweek.toLowerCase()) {
            case 'sunday':
              firstDayNumber = 0;
              break;
            case 'monday':
              firstDayNumber = 1;
              break;
            case 'tuesday':
              firstDayNumber = 2;
              break;
            case 'wednesday':
              firstDayNumber = 3;
              break;
            case 'thursday':
              firstDayNumber = 4;
              break;
            case 'friday':
              firstDayNumber = 5;
              break;
            case 'saturday':
              firstDayNumber = 6;
              break;
            default:
              firstDayNumber = 0;
              break;
          }
          scope.calendarConfig.calendar.firstDay = firstDayNumber;

          $timeout(function() {
            angular.element('.fc-toolbar .fc-left').prepend('<button class="fc-prev-button fc-nav-button btn eml-btn-grey-lightest">' + '<' + '</button>');
            angular.element('.fc-toolbar .fc-left').append('<button class="fc-next-button fc-nav-button btn eml-btn-grey-lightest">' + '>' + '</button>');
            angular.element('.fc-prev-button').click(function() {
              var selectedYear = scope.selectedDate.getFullYear();
              var selectedMonth = scope.selectedDate.getMonth();
              scope.selectedDate = new Date(selectedYear, selectedMonth - 1, 1);
              scope.$apply();
            });
            angular.element('.fc-next-button').click(function() {
              var selectedYear = scope.selectedDate.getFullYear();
              var selectedMonth = scope.selectedDate.getMonth();
              scope.selectedDate = new Date(selectedYear, selectedMonth + 1, 1);
              scope.$apply();
            });
            var localTodayDate = moment.tz(new Date(), scope.accountInfo.timezone);
            scope.selectedDate = new Date(localTodayDate.year(), localTodayDate.month(), localTodayDate.date());
            scope.applyTodayStyle('.fc-bg');
            scope.applyTodayStyle('.fc-content-skeleton');
          }, 0);
        });
      },
      templateUrl: 'modules/common/partials/dashboard-calendar.html'
    };
  }
]);

angular.module('emlogis.commonDirectives').controller('ManageShiftPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', 'originatorShift', 'getEligibleEntities', 'timezone', 'applicationContext',
  function ($scope, $modalInstance, $timeout, originatorShift, getEligibleEntities, timezone, applicationContext) {

    var weekdays = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    $scope.originatorShift = originatorShift;
    $scope.originatorShiftDate = weekdays[parseInt(originatorShift.start.format('e'))] + ', ' +
      originatorShift.start.format('MM/DD/YYYY');
    var startTimeStr = originatorShift.start.format('h:mma');
    startTimeStr = startTimeStr.substr(0, startTimeStr.length - 1);
    var endTimeStr = originatorShift.end.format('h:mma');
    endTimeStr = endTimeStr.substr(0, endTimeStr.length - 1);
    $scope.originatorShiftStartEndTime = '(' + startTimeStr + ' - ' + endTimeStr + ')';
    $scope.originatorShiftDuration = ((originatorShift.end.diff(originatorShift.start))/(3600 * 1000)) + 'hrs';

    $scope.original = {
      swapEligibleShifts: [],
      swapEligibleShiftsCount: 0,
      wipEligibleEmployees: [],
      wipEligibleEmployeesCount: 0
    };

    $scope.filtered = {
      swapEligibleShifts : [],
      swapEligibleShiftsCount: 0,
      wipEligibleEmployees: [],
      wipEligibleEmployeesCount: 0
    };

    $scope.selected = {
      swapEligibleShifts: [],
      wipEligibleEmployees: []
    };

    $scope.loadingState = {
      swapEligibleShiftsLoaded: false,
      wipEligibleEmployeesLoaded: false
    };

    $scope.parseSwapEligibleShiftForGrid = function(eligibleEntity) {
      var entity = {
        id: eligibleEntity.shiftId,
        teamMemberId: eligibleEntity.employeeId,
        teamMember: eligibleEntity.employeeName,
        team: eligibleEntity.teamName,
        skill: eligibleEntity.skillName,
        date: moment.tz(eligibleEntity.startDateTime, timezone).format('MM/DD/YYYY'),
        shift: moment.tz(eligibleEntity.startDateTime, timezone).format('hh:mma') + ' - ' +
          moment.tz(eligibleEntity.endDateTime, timezone).format('hh:mma')
      };

      return entity;
    };

    $scope.parseWipEligibleEmployeeForGrid = function(eligibleEntity) {
      var entity = {
        id: eligibleEntity.employeeId,
        teamMember: eligibleEntity.employeeName
      };

      return entity;
    };

    $scope.initializeEligibleEntities = function(type, eligibleEntities) {
      if (type === 'swap') {
        if (eligibleEntities.length === 0) {
          $scope.filterConf.swap.dateFrom = null;
          $scope.filterConf.swap.dateTo = null;
        } else {
          var minDate = eligibleEntities[0].startDateTime;
          var maxDate = eligibleEntities[0].startDateTime;
          angular.forEach(eligibleEntities, function(swapEligibleShift) {
            if (minDate > swapEligibleShift.startDateTime) {
              minDate = swapEligibleShift.startDateTime;
              return;
            }
            if (maxDate < swapEligibleShift.startDateTime) {
              maxDate = swapEligibleShift.startDateTime;
            }
          });
          minDate = moment.tz(minDate, timezone);
          maxDate = moment.tz(maxDate, timezone);
          $scope.filterConf.swap.dateFrom = new Date(minDate.year(), minDate.month(), minDate.date());
          $scope.filterConf.swap.dateTo = new Date(maxDate.year(), maxDate.month(), maxDate.date());
        }

        $scope.original.swapEligibleShifts = eligibleEntities;
        $scope.original.swapEligibleShiftsCount = eligibleEntities.length;
        $scope.onSwapFilterChange();
      } else {
        $scope.original.wipEligibleEmployees = eligibleEntities;
        $scope.original.wipEligibleEmployeesCount = eligibleEntities.length;
        $scope.onWipFilterChange();
      }
    };

    $scope.tabs = [{
      selected: false,
      heading: 'home.SHIFT_SWAP_REQUEST',
      submitLabel: 'home.SEND_SWAP_REQUEST'
    }, {
      selected: false,
      heading: 'home.WORK_IN_PLACE_REQUEST',
      submitLabel: 'home.SEND_WIP_REQUEST'
    }];
    $scope.selectedTab = null;

    $scope.selectSubTab = function (tab) {
      angular.forEach($scope.tabs, function (item) {
        item.selected = false;
      });

      tab.selected = true;
      $scope.selectedTab = tab;
    };

    $scope.toggleRequestButton = function(requestType) {
      if (requestType === 'swap') {
        $scope.selectSubTab($scope.tabs[0]);
      } else if (requestType === 'wip') {
        $scope.selectSubTab($scope.tabs[1]);
      }
    };

    $scope.$watch('selectedTab', function() {
      if ($scope.selectedTab === $scope.tabs[0]) {
        if (!$scope.loadingState.swapEligibleShiftsLoaded) {
          getEligibleEntities({tabType: 'swap', shiftId: originatorShift.id}).then(function(response) {
            $scope.initializeEligibleEntities('swap', response.data.swappableShifts);
          }, function(err) {
            $scope.initializeEligibleEntities('swap', []);
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          }).finally(function() {
            $scope.loadingState.swapEligibleShiftsLoaded = true;
          });
        }
      } else if ($scope.selectedTab === $scope.tabs[1]) {
        if (!$scope.loadingState.wipEligibleEmployeesLoaded) {
          getEligibleEntities({tabType: 'wip', shiftId: originatorShift.id}).then(function(response) {
            $scope.initializeEligibleEntities('wip', response.data.eligibleTeammates);
          }, function(err) {
            $scope.initializeEligibleEntities('wip', []);
            applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
          }).finally(function() {
            $scope.loadingState.wipEligibleEmployeesLoaded = true;
          });
        }
      }
    });

    $scope.datePickerConf = {
      opened: {
        from: false,
        to: false
      },
      options: {
        formatYear: 'yyyy',
        startingDay: 1
      }
    };

    $scope.openDatePicker = function($event, selector) {
      $event.preventDefault();
      $event.stopPropagation();

      $scope.datePickerConf.opened[selector] = true;
    };

    var now = new Date();
    $scope.filterConf = {
      swap: {
        teamMemberName: '',
        dateFrom: null,
        dateTo: null,
        time: '0;1440',
        sliderOptions: {
          from: 0,
          to: 1440,
          step: 30,
          dimension: '',
          scale: ['0', '|', '|', '|', '|', '|', '3', '|', '|', '|', '|', '|', '6',
            '|', '|', '|', '|', '|', '9', '|', '|', '|', '|', '|', '12',
            '|', '|', '|', '|', '|', '15', '|', '|', '|', '|', '|', '18',
            '|', '|', '|', '|', '|', '21', '|', '|', '|', '|', '|', '24'],
          calculate: function(value) {
            var hours = Math.floor( value / 60 );
            var mins = ( value - hours*60 );
            return (hours < 10 ? "0"+hours : hours) + ":" + ( mins === 0 ? "00" : mins );
          },
          css: {
            pointer: { 'background-color': '#0e9ac9' },
            background: { 'background-color': '#899498' },
            range: { 'background-color': '#0e9ac9' }
          }
        }
      },
      wip: {
        teamMemberName: ''
      }
    };

    $scope.swapListGridOptions = {
      data: 'filtered.swapEligibleShifts',
      enableRowSelection: true,
      enableSelectAll: true,
      multiSelect: true,
      columnDefs: [
        {field: 'id', visible: false },
        {field: 'teamMemberId', visible: false },
        {field: 'teamMember'},
        {field: 'team'},
        {field: 'skill'},
        {field: 'date'},
        {field: 'shift'}
      ],
      onRegisterApi: function(gridApi) {
        gridApi.selection.on.rowSelectionChanged($scope, function(row) {
          if (row.isSelected) {
            $scope.selected.swapEligibleShifts.push(row.entity);
          } else {
            $scope.selected.swapEligibleShifts = _.filter($scope.selected.swapEligibleShifts, function(entity) {return entity.id !== row.entity.id;});
          }
        });

        gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
          angular.forEach(rows, function(row) {
            if (row.isSelected) {
              $scope.selected.swapEligibleShifts.push(row.entity);
            } else {
              $scope.selected.swapEligibleShifts = _.filter($scope.selected.swapEligibleShifts, function(entity) {return entity.id !== row.entity.id;});
            }
          });
        });
      }
    };

    $scope.wipListGridOptions = {
      data: 'filtered.wipEligibleEmployees',
      enableRowSelection: true,
      enableSelectAll: true,
      multiSelect: true,
      columnDefs: [
        {field: 'id', visible: false},
        {field: 'teamMember'}
      ],
      onRegisterApi: function(gridApi) {
        gridApi.selection.on.rowSelectionChanged($scope, function(row) {
          if (row.isSelected) {
            $scope.selected.wipEligibleEmployees.push(row.entity);
          } else {
            $scope.selected.wipEligibleEmployees = _.filter($scope.selected.wipEligibleEmployees, function(entity) {return entity.id !== row.entity.id;});
          }
        });

        gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
          angular.forEach(rows, function(row) {
            if (row.isSelected) {
              $scope.selected.wipEligibleEmployees.push(row.entity);
            } else {
              $scope.selected.wipEligibleEmployees = _.filter($scope.selected.wipEligibleEmployees, function(entity) {return entity.id !== row.entity.id;});
            }
          });
        });
      }
    };

    $scope.$watch('filterConf.swap.time', function() {
      $scope.onSwapFilterChange();
    });

    $scope.onSwapFilterChange = function() {
      $scope.filtered.swapEligibleShifts = [];
      angular.forEach($scope.original.swapEligibleShifts, function(shiftIterator) {
        var shiftStartDateTime = moment.tz(shiftIterator.startDateTime, timezone);
        var shiftStartDate = moment.tz([shiftStartDateTime.year(), shiftStartDateTime.month(), shiftStartDateTime.date()], timezone);
        var shiftStartTimeInMinutes = parseInt(shiftStartDateTime.format('H')) * 60 + parseInt(shiftStartDateTime.format('m'));

        var filterTimeFrom = parseInt($scope.filterConf.swap.time.substring(0, $scope.filterConf.swap.time.indexOf(';')));
        var filterTimeTo = parseInt($scope.filterConf.swap.time.substring($scope.filterConf.swap.time.indexOf(';') + 1));
        var dateFromInArrFormat = [$scope.filterConf.swap.dateFrom.getFullYear(), $scope.filterConf.swap.dateFrom.getMonth(),
          $scope.filterConf.swap.dateFrom.getDate()];
        var filterDateFrom = moment.tz(dateFromInArrFormat, timezone);
        var dateToInArrFormat = [$scope.filterConf.swap.dateTo.getFullYear(), $scope.filterConf.swap.dateTo.getMonth(),
          $scope.filterConf.swap.dateTo.getDate()];
        var filterDateTo = moment.tz(dateToInArrFormat, timezone);

        if (shiftStartDate.unix() >= filterDateFrom.unix() && shiftStartDate.unix() <= filterDateTo.unix()) {
          if (shiftStartTimeInMinutes >= filterTimeFrom && shiftStartTimeInMinutes <= filterTimeTo) {
            if ($scope.filterConf.swap.teamMemberName === '' ||
              shiftIterator.employeeName.indexOf($scope.filterConf.swap.teamMemberName) > -1) {
              var shift = $scope.parseSwapEligibleShiftForGrid(shiftIterator);
              $scope.filtered.swapEligibleShifts.push(shift);
            }
          }
        }
      });
      $scope.filtered.swapEligibleShiftsCount = $scope.filtered.swapEligibleShifts.length;
      $timeout(function () {
        angular.element('.swap-list-grid').resize();
      }, 0);
    };

    $scope.onWipFilterChange = function() {
      $scope.filtered.wipEligibleEmployees = [];
      angular.forEach($scope.original.wipEligibleEmployees, function(employeeIterator) {
        if ($scope.filterConf.wip.teamMemberName === '' ||
          employeeIterator.employeeName.indexOf($scope.filterConf.wip.teamMemberName) > -1) {
          var employee = $scope.parseWipEligibleEmployeeForGrid(employeeIterator);
          $scope.filtered.wipEligibleEmployees.push(employee);
        }
      });
      $scope.filtered.wipEligibleEmployeesCount = $scope.filtered.wipEligibleEmployees.length;
      $timeout(function() {
        angular.element('.wip-list-grid').resize();
      }, 0);
    };

    $scope.submitRequest = function() {
      if ($scope.selectedTab === $scope.tabs[0]) {
        $modalInstance.close({tabType: 'swap', selectedEligibleEntities: $scope.selected.swapEligibleShifts});
      } else {
        $modalInstance.close({tabType: 'wip', selectedEligibleEntities: $scope.selected.wipEligibleEmployees});
      }
    };

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };
  }]);

angular.module('emlogis.commonDirectives').controller('OpenShiftsPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$timeout', 'applicationContext', 'tickInfo', 'getOpenShifts', 'timezone',
  function ($scope, $modalInstance, $timeout, applicationContext, tickInfo, getOpenShifts, timezone) {

    $scope.original = {
      openShifts: []
    };

    $scope.selected = {
      openShifts: []
    };

    $scope.loadingState = {
      openShiftsLoaded: false
    };

    $scope.parseOpenShiftForGrid = function(openShift) {
      var entity = {
        id: openShift.id,
        shiftId: openShift.shiftId,
        start: openShift.startDateTime,
        end: openShift.endDateTime,
        date: moment.tz(openShift.startDateTime, timezone).format('MM/DD/YYYY'),
        team: openShift.teamName,
        skill: openShift.skillName,
        shift: moment.tz(openShift.startDateTime, timezone).format('hh:mma') + ' - ' + moment.tz(openShift.endDateTime, timezone).format('hh:mma'),
        terms: openShift.terms,
        deadline: moment.tz(openShift.deadline, timezone).format('MM/DD/YYYY'),
        comment: openShift.comments
      };

      return entity;
    };

    $scope.openShiftsGridOptions = {
      data: 'original.openShifts',
      enableRowSelection: true,
      enableSelectAll: true,
      multiSelect: true,
      columnDefs: [
        {field: 'id', visible: false},
        {field: 'shiftId', visible: false},
        {field: 'start', visible: false},
        {field: 'end', visible: false},
        {field: 'date'},
        {field: 'team'},
        {field: 'skill'},
        {field: 'shift'},
        {field: 'terms'},
        {field: 'deadline'},
        {field: 'comment'}
      ],
      onRegisterApi: function(gridApi) {
        gridApi.selection.on.rowSelectionChanged($scope, function(row) {
          if (row.isSelected) {
            $scope.selected.openShifts.push(row.entity);
          } else {
            $scope.selected.openShifts = _.filter($scope.selected.openShifts, function(entity) {return entity.id !== row.entity.id;});
          }
        });

        gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
          angular.forEach(rows, function(row) {
            if (row.isSelected) {
              $scope.selected.openShifts.push(row.entity);
            } else {
              $scope.selected.openShifts = _.filter($scope.selected.openShifts, function(entity) {return entity.id !== row.entity.id;});
            }
          });
        });
      }
    };

    getOpenShifts({startDate: tickInfo.start, endDate: tickInfo.end}).then(function(response) {
      angular.forEach(response.data, function(shiftIterator) {
        var entity = $scope.parseOpenShiftForGrid(shiftIterator);
        $scope.original.openShifts.push(entity);
      });
    }, function(err) {
      applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
    }).finally(function() {
      $scope.loadingState.openShiftsLoaded = true;
      $timeout(function () {
        $('.open-shifts-grid').resize();
      }, 0);
    });

    $scope.submitOpenShiftsRequest = function() {
      $modalInstance.close({selectedOpenShifts: $scope.selected.openShifts});
    };

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };
  }]);

angular.module('emlogis.commonDirectives').controller('PtoRequestPopupModalInstanceCtrl', ['$scope', '$modalInstance', '$translate', 'DashboardService', 'applicationContext', 'siteId', 'timezone',
  function ($scope, $modalInstance, $translate, DashboardService, applicationContext, siteId, timezone) {
    var nowInLocalTime = moment.tz(new Date(), timezone);
    nowInLocalTime.hours(0).minutes(0).seconds(0);
    var dayInMilliseconds = 24 * 3600000;
    var selYear = nowInLocalTime.year();
    var selMonth = nowInLocalTime.month();
    var selDate = nowInLocalTime.date();
    var consts = {
      invalidType: {
        nonDateValues: 0,
        startDateInPast: 1,
        endDateEarlierThanStartDate: 2
      }
    };

    $scope.dateInputsValidation = true;
    $scope.dateInputsInvalidMsg = '';
    $scope.modelList = {
      timeOffTypes: []
    };

    $scope.datePickerModel = {
      startDate: new Date(selYear, selMonth, selDate),
      endDate: new Date(new Date(selYear, selMonth, selDate).getTime() + dayInMilliseconds),
      startDateOpened: false,
      endDateOpened: false,
      datePickerOptions: {
        formatYear: 'yyyy',
        startingDay: 1
      },
      openDatePicker: function($event, selector) {
        $event.preventDefault();
        $event.stopPropagation();

        if (selector === 'startDate') {
          $scope.datePickerModel.startDateOpened = true;
        } else {
          $scope.datePickerModel.endDateOpened = true;
        }
      }
    };

    $scope.data = {
      timeOffType: null,
      timeOffStartDate: nowInLocalTime.unix() * 1000,
      lengthInDays: 2,
      note: ''
    };

    var onInvalidDateInputs = function(invalidType) {
      $scope.dateInputsValidation = false;
      $scope.data.timeOffStartDate = null;
      $scope.data.lengthInDays = 0;
      if (invalidType === consts.invalidType.nonDateValues) {
        $translate('home.invalid_dates_msg.NON_DATE_VALUES').then(function(translation) {
          $scope.dateInputsInvalidMsg = translation;
        });
      } else if (invalidType === consts.invalidType.startDateInPast) {
        $translate('home.invalid_dates_msg.START_DATE_IN_PAST').then(function(translation) {
          $scope.dateInputsInvalidMsg = translation;
        });
      } else if (invalidType === consts.invalidType.endDateEarlierThanStartDate) {
        $translate('home.invalid_dates_msg.END_DATE_EARLIER_THAN_START_DATE').then(function(translation) {
          $scope.dateInputsInvalidMsg = translation;
        });
      }
    };

    var onValidDateInputs = function() {
      var yearVal = $scope.datePickerModel.startDate.getFullYear();
      var monthVal = $scope.datePickerModel.startDate.getMonth();
      var dateVal = $scope.datePickerModel.startDate.getDate();
      var lengthInDays = ($scope.datePickerModel.endDate.getTime() - $scope.datePickerModel.startDate.getTime())/dayInMilliseconds + 1;
      $scope.dateInputsValidation = true;
      $scope.data.timeOffStartDate = moment.tz([yearVal, monthVal, dateVal], timezone).unix() * 1000;
      $scope.data.lengthInDays = lengthInDays;
    };

    var compareLocalTimeWithNow = function(localTime) {
      var localYear = localTime.getFullYear();
      var localMonth = localTime.getMonth();
      var localDate = localTime.getDate();
      if (moment.tz([localYear, localMonth, localDate], timezone).unix() < nowInLocalTime.unix()) {
        return false;
      }

      return true;
    };

    $scope.onSelectedDateChanged = function(selector) {
      if (typeof $scope.datePickerModel.startDate === 'undefined' || $scope.datePickerModel.startDate === null ||
        typeof $scope.datePickerModel.endDate === 'undefined' || $scope.datePickerModel.endDate === null) {
        onInvalidDateInputs(consts.invalidType.nonDateValues);
        return;
      }
      if (!compareLocalTimeWithNow($scope.datePickerModel.startDate)) {
        onInvalidDateInputs(consts.invalidType.startDateInPast);
        return;
      }
      if ($scope.datePickerModel.startDate > $scope.datePickerModel.endDate) {
        if (selector === 'start') {
          $scope.datePickerModel.endDate = $scope.datePickerModel.startDate;
        } else {
          $scope.datePickerModel.startDate = $scope.datePickerModel.endDate;
        }
      }
      onValidDateInputs();
    };

    $scope.submitPtoRequest = function() {
      $modalInstance.close({ptoRequest: $scope.data});
    };

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };

    DashboardService.getAbsenceTypes(siteId).then(function(response) {
      $scope.modelList.timeOffTypes = response.data.result;
    }, function(err) {
      applicationContext.setNotificationMsgWithValues(JSON.stringify(err.data), 'danger', true);
    });
  }]);

angular.module('emlogis.commonDirectives').controller('SyncCalendarPopupModalInstanceCtrl', ['$scope', '$modalInstance', 'calendarSyncUrl',
  function ($scope, $modalInstance, calendarSyncUrl) {

    $scope.calendarSyncUrl = calendarSyncUrl;

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };

  }]);

angular.module('emlogis.commonDirectives').controller('ActionResultPopupModalInstanceCtrl', ['$scope', '$modalInstance', 'results',
  function ($scope, $modalInstance, results) {

    $scope.results = results;

    $scope.close = function () {
      $modalInstance.dismiss('cancel');
    };

  }]);