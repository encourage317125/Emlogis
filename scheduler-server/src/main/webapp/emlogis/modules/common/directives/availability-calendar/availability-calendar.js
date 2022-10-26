angular.module('emlogis.commonDirectives').directive('availabilityCalendar',
  ['$modal', '$timeout', '$state', '$filter', 'dataService', 'uiCalendarConfig', 'applicationContext', 'appFunc',
  function ($modal, $timeout, $state, $filter, dataService, uiCalendarConfig, applicationContext, appFunc) {

    return {
      restrict: 'E',
      scope: {
        calendarMode: '@',
        employeeId: '=',
        siteTimeZone: '=',
        firstDayOfWeek: '=',
        absenceTypes: '=',
        previewParams: '='
      },
      controller: function($scope) {
        $scope.fakeModel = [[]];

        $scope.calendarModes = {
          MANAGER: "manager",
          EMPLOYEE: "employee",
          PREVIEW: "preview"
        };
      },
      templateUrl: 'modules/common/partials/availability-calendar/availability-calendar.html',
      link: function (scope) {

        if (_.values(scope.calendarModes).indexOf(scope.calendarMode) === -1) {
          throw "Unknown availability calendar mode: " + scope.calendarMode + ".";
        }

        scope.config = {
          calendar: {
            editable: false,
            columnFormat: "dddd",
            header:{
              left: '',
              center: '',
              right: ''
            }
          }
        };

        scope.datepicker = {
          model: null,
          opened: false,
          open: function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            scope.datepicker.opened = true;
          }
        };

        scope.monthYearPicker = {
          year: new Date().getFullYear(),
          month: new Date().getMonth()
        };

        scope.dateRangeStart = 0;
        scope.dateRangeEnd = 0;
        //scope.maxDaysPerWeek = [];
        scope.coupleWeekends = {};
        scope.showAvailability = true;
        scope.showPreference = true;

        scope.$watch('showAvailability', function(show) {
          if (show) {
            $(".availability").show();
          } else {
            $(".availability").hide();
          }
        });

        scope.$watch('showPreference', function(show) {
          if (show) {
            $(".preference").show();
          } else {
            $(".preference").hide();
          }
        });

        scope.openEditAvailabilityModal = function() {
          var modalInstance = $modal.open({
            templateUrl: 'modules/common/partials/availability-calendar/edit-availability-modal.html',
            controller: 'EditAvailabilityModal',
            resolve: {
              managerView: function() {
                return scope.calendarMode === scope.calendarModes.MANAGER;
              },
              employeeId: function() {
                return scope.employeeId;
              },
              availabilityType: function() {
                return scope.availabilityType;
              },
              datesSelected: function() {
                return datesSelected();
              },
              timeZone: function() {
                return scope.siteTimeZone;
              },
              absenceTypes: function() {
                return scope.absenceTypes;
              },
              dateRangeStart: function() {
                return scope.dateRangeStart;
              },
              dateRangeEnd: function() {
                return scope.dateRangeEnd;
              }
            }
          });

          modalInstance.result.then(function (availability) {
            if (scope.calendarMode === scope.calendarModes.MANAGER) {
              scope.availability = availability;
              refreshCalendar(true);
            }
          });
        };

        scope.openEditPreferenceModal = function() {
          var modalInstance = $modal.open({
            templateUrl: 'modules/common/partials/availability-calendar/edit-preference-modal.html',
            controller: 'EditPreferenceModal',
            resolve: {
              employeeId: function() {
                return scope.employeeId;
              },
              availabilityType: function() {
                return scope.availabilityType;
              },
              datesSelected: function() {
                return datesSelected();
              },
              timeZone: function() {
                return scope.siteTimeZone;
              },
              dateRangeStart: function() {
                return scope.dateRangeStart;
              },
              dateRangeEnd: function() {
                return scope.dateRangeEnd;
              }
            }
          });

          modalInstance.result.then(function (availability) {
            scope.availability = availability;
            refreshCalendar(true);
          });
        };

        scope.openCopyAvailabilityModal = function() {
          var modalInstance = $modal.open({
            templateUrl: 'modules/common/partials/availability-calendar/copy-availability-modal.html',
            controller: 'CopyAvailabilityModal',
            resolve: {
              employeeId: function() {
                return scope.employeeId;
              },
              datesSelected: function() {
                return datesSelected();
              },
              timeZone: function() {
                return scope.siteTimeZone;
              },
              dateRangeStart: function() {
                return scope.dateRangeStart;
              },
              dateRangeEnd: function() {
                return scope.dateRangeEnd;
              }
            }
          });

          modalInstance.result.then(function (availability) {
            scope.availability = availability;
            refreshCalendar(true);
          });
        };

        function openEditRotationModal() {
          var modalInstance = $modal.open({
            templateUrl: 'modules/common/partials/availability-calendar/edit-rotation-modal.html',
            controller: 'EditRotationModal',
            resolve: {
              employeeId: function() {
                return scope.employeeId;
              },
              selectedDay: function() {
                return datesSelected()[0];
              },
              dateRangeStart: function() {
                return scope.dateRangeStart;
              },
              dateRangeEnd: function() {
                return scope.dateRangeEnd;
              }
            }
          });

          modalInstance.result.then(function () {
            refreshCalendar();
          });
        }

        scope.availabilityType = undefined;

        function updateAvailabilityType() {
          var selectedDays = $('.day-selected'),
              type;

          if (selectedDays.hasClass("ci-day")) type = "ci";
          if (selectedDays.hasClass("cd-day")) type = "cd";

          scope.$apply(function () {
            scope.availabilityType = type;
          });
        }

        function datesSelected() {
          var selectedDays = $('.day-selected'),
              dates = [],
              dateAttr = scope.availabilityType === "cd" ? "data-date" : "data-day";

          selectedDays.each(function() {
            var date = $(this).parent().attr(dateAttr);
            if (date) {
              dates.push(date);
            }
          });

          return dates;
        }

        function deselectDays(className) {
          if (!className) {
            $('.availability-container').removeClass('day-selected');
          } else {
            $('.availability-container.' + className).removeClass(className + ' day-selected');
          }
        }

        function initCalendar() {
          var daysInCalendar = $('.fc-day-grid .fc-bg td');

          function timeFormat(time) {
            return moment(time).minutes() === 0 ? "h a" : "h:mm a";
          }

          function titleAndClass4AvailabiliyTimeFrameContainer(timeFrame, availabilityType) {
            var title = "",
                className = "",
                timeFrameClass = "";

            switch (availabilityType) {
              case "AVAIL":
                title = moment(timeFrame.startDateTime).format(timeFormat(timeFrame.startDateTime)) + " - " +
                        moment(timeFrame.endDateTime).format(timeFormat(timeFrame.endDateTime));
                className = "partially-available";
                break;
              case "DAY_OFF":
                if (timeFrame.pto) {
                  title = timeFrame.absenceTypeName;
                  className = "holiday-vacation";
                } else {
                  title = $filter('translate')("availability.NOT_AVAILABLE");
                  className = "not-available";
                }
                break;
              default:
                title = $filter('translate')("availability.UNKNOWN_AVAILABILITY_TYPE") + " " + availabilityType;
            }

            if (timeFrame.preview === "REMOVED") {
              timeFrameClass = "remove-time-frame";
            } else if (timeFrame.preview === "ADDED") {
              timeFrameClass = "add-time-frame";
            }

            return {
              title: title,
              className: className,
              timeFrameClass: timeFrameClass
            };
          }

          function title4PreferenceTimeFrame(timeFrame, preferenceType) {
            var title = "";

            switch (preferenceType) {
              case "AVOID_TIMEFRAME":
                title = $filter('translate')("availability.AVOID") + " " +
                        moment(timeFrame.startDateTime).format(timeFormat(timeFrame.startDateTime)) + " - " +
                        moment(timeFrame.endDateTime).format(timeFormat(timeFrame.endDateTime));
                break;
              case "PREFER_TIMEFRAME":
                title = $filter('translate')("availability.PREFER") + " " +
                        moment(timeFrame.startDateTime).format(timeFormat(timeFrame.startDateTime)) + " - " +
                        moment(timeFrame.endDateTime).format(timeFormat(timeFrame.endDateTime));
                break;
              case "AVOID_DAY":
                title = $filter('translate')("availability.AVOID_DAY");
                break;
              case "PREFER_DAY":
                title = $filter('translate')("availability.PREFER_DAY");
                break;
              default:
                title = $filter('translate')("availability.UNKNOWN_PREFERENCE_TYPE") + " " + preferenceType;
            }

            return title;
          }

          function addAvailabilityContainers() {
            _.each(daysInCalendar, function(d) {
              $(d).append("<div class='availability-container'>" +
                            "<div class='availability'></div>" +
                            "<div class='preference'></div>" +
                          "</div>");

              if (scope.calendarMode !== scope.calendarModes.PREVIEW) {
                $(d).unbind();
                $(d).click(function () {
                  var availabilityContainer = $(this).find(".availability-container");
                  if (availabilityContainer.hasClass("day-selected")) {
                    if (availabilityContainer.hasClass("ci-day")) {
                      deselectDays('ci-day');
                      availabilityContainer.addClass("day-selected cd-day");
                    } else {
                      availabilityContainer.removeClass("day-selected cd-day");
                    }
                  } else {
                    deselectDays('ci-day');
                    availabilityContainer.addClass("day-selected cd-day");
                  }
                  updateAvailabilityType();
                });
              }

            });
          }

          function convertTimeFramesToBrowserTimeZone(timeFrames, type) {
            var convertedTimeFrames = _.clone(timeFrames);

            if (type === 'ci') {
              _.each(convertedTimeFrames, function(instance) {
                _.each(instance.timeFrameInstances, function (timeFrame) {
                  if (timeFrame.startDateTime) {
                    timeFrame.startDateTime = appFunc.convertToBrowserTimezone(timeFrame.startDateTime, scope.siteTimeZone);
                  }
                  if (timeFrame.endDateTime) {
                    timeFrame.endDateTime = appFunc.convertToBrowserTimezone(timeFrame.endDateTime, scope.siteTimeZone);
                  }
                });
              });
            } else {
              _.each(convertedTimeFrames, function(timeFrame) {
                  if (timeFrame.startDateTime) {
                    timeFrame.startDateTime = appFunc.convertToBrowserTimezone(timeFrame.startDateTime, scope.siteTimeZone);
                  }
                  if (timeFrame.endDateTime) {
                    timeFrame.endDateTime = appFunc.convertToBrowserTimezone(timeFrame.endDateTime, scope.siteTimeZone);
                  }
              });
            }

            return convertedTimeFrames;
          }

          function displayCITimeFrames() {
            var ciAvailTimeFrames = convertTimeFramesToBrowserTimeZone(scope.availability.availCITimeFrames, "ci"),
                ciPrefTimeFrames  = convertTimeFramesToBrowserTimeZone(scope.availability.prefCITimeFrames, "ci"),
                ciHeaderCell = null;

            _.each(daysInCalendar, function(d) {

              _.each(ciAvailTimeFrames, function (t) {
                var availTimeFrames = _.filter(t.timeFrameInstances, function(f) {
                  return moment(f.startDateTime).format("YYYY-MM-DD") === d.dataset.date;
                });

                _.each(availTimeFrames, function(timeFrame) {
                  var tc = titleAndClass4AvailabiliyTimeFrameContainer(timeFrame, t.availType);

                  $(d).find(".availability-container").
                    addClass(tc.className).
                    find(".availability").
                    append("<div class='time-frame " + tc.timeFrameClass + "'>" + tc.title + "</div>");

                  ciHeaderCell = $(".ci-header th[data-day='" + t.dayOfTheWeek.toLowerCase() + "'] .availability-container");

                  if (!ciHeaderCell.hasClass(tc.title.replace(/\s+/g, ''))) {
                    ciHeaderCell.
                    addClass(tc.className + " " + tc.title.replace(/\s+/g, '')).
                    find(".availability").
                    append("<div class='time-frame " + tc.timeFrameClass + "'>" + tc.title + "</div>");
                  }
                });
              });

              _.each(ciPrefTimeFrames, function (t) {
                var prefTimeFrames = _.filter(t.timeFrameInstances, function(f) {
                  return moment(f.startDateTime).format("YYYY-MM-DD") === d.dataset.date;
                });

                _.each(prefTimeFrames, function(p) {
                  var title = title4PreferenceTimeFrame(p, t.prefType);

                  $(d).find(".preference").
                    append("<div class='pref-time-frame'>" + title + "</div>");

                  ciHeaderCell = $(".ci-header th[data-day='" + t.dayOfTheWeek.toLowerCase() + "'] .availability-container");

                  if (!ciHeaderCell.hasClass(title.replace(/\s+/g, ''))) {
                    ciHeaderCell.
                      addClass(title.replace(/\s+/g, '')).
                      find(".preference").
                      append("<div class='pref-time-frame'>" + title + "</div>");
                  }
                });
              });
            });
          }

          function displayCDTimeFrames() {
            var cdAvailTimeFrames = convertTimeFramesToBrowserTimeZone(scope.availability.availCDTimeFrames, "cd"),
                cdPrefTimeFrames  = convertTimeFramesToBrowserTimeZone(scope.availability.prefCDTimeFrames, "cd");

            _.each(daysInCalendar, function(d) {
              var availTimeFrames = _.filter(cdAvailTimeFrames, function(t) {
                return moment(t.startDateTime).format("YYYY-MM-DD") === d.dataset.date;
              }),
                prefTimeFrames = _.filter(cdPrefTimeFrames, function(t) {
                  return moment(t.startDateTime).format("YYYY-MM-DD") === d.dataset.date;
                });

              _.each(availTimeFrames, function(a) {
                var tc = titleAndClass4AvailabiliyTimeFrameContainer(a, a.availType);

                $(d).find(".availability-container").
                  addClass(tc.className).
                  find(".availability").
                  append("<div class='time-frame " + tc.timeFrameClass + "'>" + tc.title + "</div>");
              });

              _.each(prefTimeFrames, function(p) {
                var title = title4PreferenceTimeFrame(p, p.prefType);

                $(d).find(".preference").
                  append("<div class='pref-time-frame'>" + title + "</div>");
              });
            });
          }

          function displayRotation() {
            $(".fc-row.fc-widget-header tr:first th").each(function() {
              var dayInRotationObj, rotation;
              switch ($(this)[0].classList[2]) {
                case "fc-sun":
                  dayInRotationObj = "sunday";
                  break;
                case "fc-mon":
                  dayInRotationObj = "monday";
                  break;
                case "fc-tue":
                  dayInRotationObj = "tuesday";
                  break;
                case "fc-wed":
                  dayInRotationObj = "wednesday";
                  break;
                case "fc-thu":
                  dayInRotationObj = "thursday";
                  break;
                case "fc-fri":
                  dayInRotationObj = "friday";
                  break;
                case "fc-sat":
                  dayInRotationObj = "saturday";
                  break;
              }

              switch (scope.availability.weekdayRotations[dayInRotationObj]) {
                case "NONE":
                  rotation = "1:1";
                  break;
                case "EVERY_OTHER":
                  rotation = "1:2";
                  break;
                case "EVERY_THIRD":
                  rotation = "1:3";
                  break;
                case "TWO_OF_EVERY_FOUR":
                  rotation = "2:4";
                  break;
              }

              $(this).empty();
              $(this).append("<span class='rotation-day-of-week'>" + dayInRotationObj + "</span>");
              if (scope.calendarMode !== scope.calendarModes.PREVIEW) {
                $(this).append("<span class='rotation'>" + rotation + "</span>");
              }
              $(this).wrapInner("<div class='availability-container'></div>");
            });
          }

          function addCIHeader() {
            function toggleDayOfWeekColumn(fcDay, select) {
              var dayOfWeekColumn = $(".fc-day." + fcDay + " .availability-container"),
                  dayHeader = $(".fc-day-header." + fcDay + " .availability-container");

              deselectDays('cd-day');
              select = typeof select === "boolean" ? select : !dayOfWeekColumn.hasClass("day-selected ci-day");

              if (select) {
                dayOfWeekColumn.addClass("day-selected ci-day");
                dayHeader.addClass("day-selected ci-day");
              } else {
                dayOfWeekColumn.removeClass("day-selected ci-day");
                dayHeader.removeClass("day-selected ci-day");
              }
              updateAvailabilityType();
            }

            if (scope.calendarMode !== scope.calendarModes.PREVIEW) {
              $(".fc-row.fc-widget-header thead th").click(function (e) {
                if (e.target.className === "rotation" && scope.calendarMode === scope.calendarModes.MANAGER) {
                  deselectDays();
                  toggleDayOfWeekColumn($(this)[0].classList[2], true);
                  openEditRotationModal();
                } else {
                  toggleDayOfWeekColumn($(this)[0].classList[2]);
                }
              });
            }

            var headerCells = [
              '<th class="fc-day fc-mon" data-day="monday"></th>',
              '<th class="fc-day fc-tue" data-day="tuesday"></th>',
              '<th class="fc-day fc-wed" data-day="wednesday"></th>',
              '<th class="fc-day fc-thu" data-day="thursday"></th>',
              '<th class="fc-day fc-fri" data-day="friday"></th>',
              '<th class="fc-day fc-sat" data-day="saturday"></th>'
            ];

            if (scope.firstDayOfWeek === 'MONDAY') {
              headerCells.push('<th class="fc-day fc-sun" data-day="sunday"></th>');
            } else {
              headerCells.unshift('<th class="fc-day fc-sun" data-day="sunday"></th>');
            }

            var ciHeader = '<tr class="ci-header">' + headerCells.toString() + '</tr>';
            $(".fc-row.fc-widget-header thead").append(ciHeader);

            $(".ci-header").children()
              .append("<div class='availability-container'>" +
                        "<div class='availability'></div>" +
                        "<div class='preference'></div>" +
                      "</div>");

            if (scope.calendarMode !== scope.calendarModes.PREVIEW) {
              $(".ci-header").children().click(function() {
                toggleDayOfWeekColumn($(this)[0].classList[1]);
              });
            }

            // hide last calendar row with days from the next month
            $(".fc-day-grid .fc-row.fc-week").last().css("display", "none");
          }

          function adjustCIHeaderHeight() {
            var headerHeightDelta = 28,
                thHeight = $(".ci-header th").height() - headerHeightDelta;
            $(".ci-header .availability-container").height(thHeight);
            $(".max-days-header").height(thHeight);
            $(".ci-header .availability-container .preference").css({"position": "absolute", "bottom": "5px"});

            //if (scope.calendarMode === scope.calendarModes.PREVIEW) {
            //  $(".fc-scroller").css("overflow-y", "hidden");
            //  $(".fc-row.fc-widget-header").css({
            //    "margin-right": "0",
            //    "border-right-width": "0"
            //  });
            //  $(window).resize();
            //}
          }

          function toggleView() {
            if (!scope.showAvailability) {
              $(".availability").hide();
            }
            if (!scope.showPreference) {
              $(".preference").hide();
            }
          }

          addAvailabilityContainers();
          addCIHeader();
          displayCITimeFrames();
          displayCDTimeFrames();
          displayRotation();
          adjustCIHeaderHeight();
          toggleView();
        }

        function displayAvailability() {
          if (!scope.availability) return;

          //var firstDayOfWeekInCalendar = moment([scope.monthYearPicker.year, scope.monthYearPicker.month]).startOf('week'),
          //    weekCounter = 0;

          //scope.maxDaysPerWeek = [];
          //
          //_.forIn(scope.availability.maxDaysPerWeek, function(value) {
          //  scope.maxDaysPerWeek.push({
          //    days: value,
          //    initialDays: value,
          //    startDateOfWeek: moment(firstDayOfWeekInCalendar).add(weekCounter, 'weeks').valueOf(),
          //    showActionButtons: false,
          //    update: function() {
          //      var _this = this;
          //      dataService.updateEmployeeMaxDaysPerWeek(scope.employeeId, {weekStartDate: this.startDateOfWeek, maxDays: this.days}).
          //        then(function() {
          //          applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
          //          _this.initialDays = _this.days;
          //          _this.showActionButtons = false;
          //        }, function(err) {
          //          applicationContext.setNotificationMsgWithValues(err.data, 'danger', true);
          //        });
          //    },
          //    cancel: function() {
          //      this.days = this.initialDays;
          //      this.showActionButtons = false;
          //    }
          //  });
          //  weekCounter++;
          //});

          scope.coupleWeekends = {
            initialValue: scope.availability.coupleWeekends,
            value: scope.availability.coupleWeekends,
            update: function() {
              var _this = this;
              dataService.updateEmployeeCoupleWeekends(scope.employeeId, {coupleWeekends: this.value.toString()}).
                then(function() {
                  applicationContext.setNotificationMsgWithValues('app.UPDATED_SUCCESSFULLY', 'success', true);
                  _this.initialValue = _this.value;
                }, function(err) {
                  applicationContext.setNotificationMsgWithValues(err.data, 'danger', true);
                });
            },
            cancel: function() {
              this.value = this.initialValue;
            }
          };

          initCalendar();
        }

        function getAvailability(startDate, endDate) {

          if ((scope.calendarMode === scope.calendarModes.MANAGER || scope.calendarMode === scope.calendarModes.PREVIEW) &&
              !scope.employeeId) {
            throw "Availability calendar directive missing employeeId!";
          }

          if (scope.calendarMode === scope.calendarModes.PREVIEW) {
            dataService.getEmployeeAvailabilityPreview(scope.employeeId, {
              workflowRequestId: scope.previewParams.requestId,
              dateRangeStart: startDate,
              dateRangeEnd: endDate
            }).then(function(res) {
              console.log("response", res);
              scope.availability = res.data;
              displayAvailability();
            }, function(err) {
              applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
            });
          } else {
            dataService.getEmployeeAvailabilityView(scope.employeeId, startDate, endDate).
              then(function(res) {
                scope.availability = res.data;
                displayAvailability();
              }, function(err) {
                applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
              });
          }
        }

        scope.$watch('datepicker.model', function(date) {
          if (date && uiCalendarConfig.calendars.availabilityCalendar) {
            var selectedDateInArrFormat = [date.getFullYear(), date.getMonth(), date.getDate()],
              localDate = moment.tz(selectedDateInArrFormat, scope.siteTimeZone),
              currentCalendarDate = uiCalendarConfig.calendars.availabilityCalendar.fullCalendar('getDate');

            if (localDate.format('YYYY-MM') !== currentCalendarDate.format('YYYY-MM')) {
              uiCalendarConfig.calendars.availabilityCalendar.fullCalendar('gotoDate', localDate);

              scope.monthYearPicker = {
                year: date.getFullYear(),
                month: date.getMonth()
              };
            }
          }
        });

        function refreshCalendar(dontQueryAvailability) {
          var firstDayOfMonth, lastDayOfMonth, firstDayInCalendar, lastDayInCalendar;

          if (scope.calendarMode === scope.calendarModes.PREVIEW) {
            firstDayOfMonth = moment([scope.previewParams.year, scope.previewParams.month]);
            lastDayOfMonth = moment(firstDayOfMonth).endOf('month');
            firstDayInCalendar = getFirstDayInCalendar();
            lastDayInCalendar = getLastDayInCalendar();
          } else {
            firstDayOfMonth = moment([scope.monthYearPicker.year, scope.monthYearPicker.month]);
            lastDayOfMonth = moment(firstDayOfMonth).endOf('month');
            firstDayInCalendar = getFirstDayInCalendar();
            lastDayInCalendar = getLastDayInCalendar();
          }

          scope.dateRangeStart = appFunc.getDateWithTimezone(
            firstDayInCalendar.getFullYear(),
            firstDayInCalendar.getMonth(),
            firstDayInCalendar.getDate(),
            scope.siteTimeZone
          ).valueOf();

          scope.dateRangeEnd = appFunc.getDateWithTimezone(
            lastDayInCalendar.getFullYear(),
            lastDayInCalendar.getMonth(),
            lastDayInCalendar.getDate(),
            scope.siteTimeZone
          ).valueOf();

          function getFirstDayInCalendar() {
            return scope.firstDayOfWeek === "SUNDAY" ? moment(firstDayOfMonth).startOf('week').toDate() : moment(firstDayOfMonth).startOf('isoweek').toDate();
          }

          function getLastDayInCalendar() {
            return scope.firstDayOfWeek === "SUNDAY" ? moment(lastDayOfMonth).endOf('week').toDate() : moment(lastDayOfMonth).endOf('isoweek').toDate();
          }

          function getFirstDayOfWeekNumber() {
            return scope.firstDayOfWeek === "SUNDAY" ? 0 : 1;
          }

          scope.config.calendar.firstDay = getFirstDayOfWeekNumber();

          $('.availability-container').not(".rotation-container").remove();
          $('.ci-header').remove();
          $(".fc-row.fc-widget-header thead th").unbind();
          scope.availabilityType = undefined;

          scope.datepicker.model = firstDayOfMonth.toDate();

          if (dontQueryAvailability) {
            displayAvailability();
          } else {
            getAvailability(scope.dateRangeStart, scope.dateRangeEnd);
          }
        }

        var removeTimeZoneListener = scope.$watch("siteTimeZone", function(t) {

          if (t) {
            if (scope.calendarMode === scope.calendarModes.PREVIEW) {
              refreshCalendar();
            } else {
              removeTimeZoneListener();
              scope.$watch("monthYearPicker.month", function() {
                refreshCalendar();
              });
            }
          }
        });

      }
    };
  }
]);

