(function () {
    var module = angular.module('emlogis.browser');

    module.directive('shiftTimeline', function () {
        return {
            restrict: 'A',
            link: function postLink(scope, elem, attrs) {

                scope.timelineWidth = attrs.width;
                scope.maxHours = 48;

                scope.$on('event:showLightbox', function (event, args) {

                    scope.initShiftEditForm();
                    init();
                });
                scope.$on('event:updateShift', function (event, args) {
                    init();
                });

                function init() {
                    elem.resizable({handles: 'w,e', containment: "parent", grid: [scope.step, 1], minWidth : 1});
                }

                elem.on('resizestop', function (evt, ui) {
                    scope.$apply(function () {
                        scope.$eval(attrs.firstpos + '=' + ui.position.left);
                        scope.$eval(attrs.endpos + '=' + (ui.size.width + 2));
                    });
                });
            }
        };
    });

    /**
     * Controller for Shift edit
     */
    module.controller('ShiftEditCtlr', ['$scope', '$stateParams', 'dataService', 'dialogs', '$rootScope', 'alertsManager',
        function ($scope, $stateParams, dataService, dialogs, $rootScope, alertsManager) {

            console.log("inside ShiftEditCtlr");

            $scope.close = function () {
                $rootScope.currentShift = $scope.currentShift;
                $scope.close_form();
            };

            $scope.initShiftEditForm = function () {

                $scope.hoursBeforeAfter = 4;
                $scope.currentShift = $rootScope.currentShift;

                $scope.openShift = '';
                if ($scope.currentShift.employeeId === null) {
                    $scope.openShift = 'openShift';
                }

                $scope.startDate = new Date($scope.currentShift.startDateTime);
                $scope.endDate = new Date($scope.currentShift.endDateTime);

                $scope.startPtoDate = new Date($scope.currentShift.startDateTime);
                $scope.endPtoDate = new Date($scope.currentShift.startDateTime);

                $scope.newShiftModel = {
                    start: 0,
                    width: 0,
                    position: ''
                };

                $scope.initTimeline();

                $scope.dropfill = 'drop';
                $scope.changeToPto = false;

                $scope.hasNewShift = function () {
                    if ($scope.endPtoDate.getTime() > $scope.startPtoDate.getTime()) {
                        return true;
                    } else
                        return false;
                };

                $scope.getAbsenceTypes = function () {
                    dataService.getElement('teams', $scope.currentShift.teamId + '/site', {}) //getting site by teamId
                        .then(function (site) {
                            getAbsenceTypes(site);
                        }, function (error) {
                            console.log("Error while getting site for '" + $scope.currentShift.employeeName + "': " + error.data.message);
                        });
                };

                function getAbsenceTypes(site) {
                    dataService.getAbsenceTypes(site.id)
                        .then(function (result) {
                            var absenceTypes = [];
                            var ticked = true;
                            for (var i in result.data) {
                                if (i > 0) {
                                    ticked = false;
                                }
                                absenceTypes.push(
                                    {id: result.data[i].id, name: result.data[i].name, ticked: ticked}
                                );
                            }
                            $scope.absenceTypes = absenceTypes;
                        });
                }

                $scope.getAbsenceTypes();
                $scope.selectAbsenceTypes = [];
            };

            $scope.initTimeline = function () {

                var timelineWidth = $scope.timelineWidth;

                var startDateLine = new Date($scope.currentShift.startDateTime);
                startDateLine.setHours(startDateLine.getHours() - $scope.hoursBeforeAfter, 0, 0, 0);
                startDateLine.setHours(startDateLine.getHours() + startDateLine.getHours()%2, 0, 0, 0);
                var endDateLine = new Date($scope.currentShift.endDateTime);
                endDateLine.setHours(endDateLine.getHours() + $scope.hoursBeforeAfter, 0, 0, 0);

                $scope.hoursN = (endDateLine.getTime() - startDateLine.getTime()) / 1000 / 60 / 60;

                var koef1 = 1;
                if ($scope.hoursN > 12) {
                    koef1 = Math.ceil($scope.hoursN / 12);
                    switch (koef1){
                        case 5:
                            koef1 = 6;
                            break;
                        case 7:
                            koef1 = 8;
                            break;
                        default :
                            if(koef1 > 8 && koef1 <= 12){
                                koef1 = 12;
                            } if(koef1 > 12){
                            koef1 = 24;
                        }
                    }
                    //koef1 = Math.pow(2, Math.floor($scope.hoursN / 12));
                }
                $scope.hours = [];
                for (var i = 0; i < $scope.hoursN / koef1; i++) {
                    var temp = new Date(startDateLine);
                    temp.setHours(temp.getHours() + i * koef1);
                    $scope.hours[i] = temp;
                }

                var minStep = 15;
                //var minStep = 15 * Math.pow(2, Math.floor(Math.sqrt(koef1)) - 1);
                //if (minStep > 60) {
                //    minStep = 60;
                //}
                $scope.step = timelineWidth / ($scope.hoursN * (60 / minStep));

                var controldate = new Date($scope.hours[$scope.hours.length - 1]);
                controldate.setHours(controldate.getHours() + koef1);
                if (controldate.getTime() > endDateLine.getTime()) {
                    endDateLine = controldate;
                }

                var lineDiff = endDateLine.getTime() - startDateLine.getTime();
                var startPoint = $scope.startDate.getTime() - startDateLine.getTime();
                var duration = $scope.endDate.getTime() - $scope.startDate.getTime();

                $scope.shiftModel = {
                    start: startPoint * timelineWidth / lineDiff,
                    width: duration * timelineWidth / lineDiff
                };

                $scope.newShiftModel.start = ($scope.startPtoDate.getTime() - startDateLine.getTime()) * timelineWidth / lineDiff;
                $scope.newShiftModel.width = ($scope.endPtoDate.getTime() - $scope.startPtoDate.getTime()) * timelineWidth / lineDiff;


                $scope.limits = {
                    start: ($scope.currentShift.startDateTime - startDateLine.getTime()) * timelineWidth / lineDiff,
                    end: ($scope.currentShift.endDateTime - startDateLine.getTime()) * timelineWidth / lineDiff
                };

                $scope.watchers.shiftModel = $scope.$watch('[shiftModel.start, shiftModel.width]', function (newValue, oldValue) {

                    $scope.startDate = new Date(roundTime((newValue[0] * lineDiff) / timelineWidth + startDateLine.getTime(), minStep));
                    $scope.endDate = new Date(roundTime((newValue[1] * lineDiff) / timelineWidth + $scope.startDate.getTime(), minStep));

                    var newVal = {
                        start: newValue[0],
                        width: newValue[1]
                    };

                    var oldVal = {
                        start: oldValue[0],
                        width: oldValue[1]
                    };

                    var shiftModel = {
                        start: newVal.start,
                        width: newVal.width
                    };
                    var newShiftModel = angular.copy($scope.newShiftModel);

                    angular.forEach(newValue, function(value, key) {
                        if (newValue[key] < 0){
                            newValue[key] = 0;
                        }
                    });

                    if (!$scope.hasNewShift()) {
                        if ((newVal.start > oldVal.start)) {
                            newShiftModel.start = $scope.limits.start;
                            if ((newVal.start - $scope.limits.start) > 0) {
                                newShiftModel.width = newVal.start - $scope.limits.start;
                            } else {
                                newShiftModel.width = 0;
                            }
                            newShiftModel.position = 'before';

                        } else if ((newVal.width < oldVal.width)) {
                            newShiftModel.start = newVal.start + newVal.width;
                            if (($scope.limits.end - newVal.width) > 0) {
                                newShiftModel.width = $scope.limits.end - (newVal.width + newVal.start);
                            } else {
                                newShiftModel.width = 0;
                            }
                            newShiftModel.position = 'after';
                        }

                    } else if (newShiftModel.position == 'before') {
                        if ((newVal.start > oldVal.start)) {
                            newShiftModel.width += (newVal.start - oldVal.start);

                        } else if (newVal.start >= newShiftModel.start) {
                            newShiftModel.width = newShiftModel.width - (oldVal.start - newVal.start);
                        } else {
                            newShiftModel.start = newVal.start;
                            newShiftModel.width = 0;
                        }

                        //} else if (newShiftModel.position == 'after' && $scope.backUpEndDate.getTime() != $scope.endDate.getTime()) {
                    } else if (newShiftModel.position == 'after' && (
                            ( Math.abs((newVal.start + newVal.width) - (oldVal.start + oldVal.width)) > 3 ))) {
                        var widthDiff = newVal.width - oldVal.width;
                        if ((newVal.width < oldVal.width)) {
                            newShiftModel.start += widthDiff;
                            newShiftModel.width -= widthDiff;

                        } else if ((newVal.start + newVal.width) < (newShiftModel.start + newShiftModel.width) &&
                            (newVal.start + newVal.width) > newShiftModel.start) {
                            newShiftModel.start += widthDiff;
                            newShiftModel.width -= widthDiff;
                        } else {
                            newShiftModel.start = newVal.start;
                            newShiftModel.width = 0;
                        }
                    }

                    if($scope.hasNewShift()){
                        if (newShiftModel.position == 'before' && (newVal.width < oldVal.width) &&
                            ((newVal.width + newVal.start) < $scope.limits.end)) {
                            shiftModel.width = Math.abs($scope.limits.end - newVal.start);
                        }

                        if (newShiftModel.position == 'after' && (newVal.width < oldVal.width) &&
                            (shiftModel.start > $scope.limits.start)) {
                            shiftModel.start = $scope.limits.start;
                            shiftModel.width = Math.abs(newShiftModel.start - $scope.limits.start);
                        }

                        if (shiftModel.start + shiftModel.width > newShiftModel.start  && newShiftModel.position == 'after'){
                            var diff = shiftModel.start + shiftModel.width - newShiftModel.start;
                            newShiftModel.width -= diff;
                            newShiftModel.start += diff;
                        }
                    }


                    $scope.shiftModel = angular.copy(shiftModel);
                    $scope.newShiftModel = angular.copy(newShiftModel);

                    $scope.startPtoDate = new Date(roundTime(($scope.newShiftModel.start * lineDiff) / timelineWidth + startDateLine.getTime(), minStep));
                    $scope.endPtoDate = new Date(roundTime(($scope.newShiftModel.width * lineDiff) / timelineWidth + $scope.startPtoDate.getTime(), minStep));

                }, true);

                function roundTime(time, min) {
                    return (Math.round(time / 1000 / 60 / min)) * min * 60 * 1000;
                }
            };

            $scope.zoomIn = function () {
                if ($scope.hoursBeforeAfter > 1) {
                    $scope.hoursBeforeAfter /= 2;

                    var tempStartDateLine = new Date($scope.currentShift.startDateTime);
                    tempStartDateLine.setHours(tempStartDateLine.getHours() - $scope.hoursBeforeAfter, 0, 0, 0);
                    var tempEndDateLine = new Date($scope.currentShift.endDateTime);
                    tempEndDateLine.setHours(tempEndDateLine.getHours() + $scope.hoursBeforeAfter, 0, 0, 0);
                    if (tempStartDateLine.getTime() <= $scope.startDate.getTime() &&
                        tempEndDateLine.getTime() >= $scope.endDate.getTime()) {
                        $scope.updateTimeLine();
                        return true;
                    } else {
                        $scope.hoursBeforeAfter *= 2;
                        return false;
                    }
                }
            };

            $scope.zoomOut = function () {
                $scope.hoursBeforeAfter *= 2;

                var tempStartDateLine = new Date($scope.currentShift.startDateTime);
                tempStartDateLine.setHours(tempStartDateLine.getHours() - $scope.hoursBeforeAfter, 0, 0, 0);
                var tempEndDateLine = new Date($scope.currentShift.endDateTime);
                tempEndDateLine.setHours(tempEndDateLine.getHours() + $scope.hoursBeforeAfter, 0, 0, 0);

                if ( (tempEndDateLine.getTime() - tempStartDateLine.getTime()) < ($scope.maxHours * 60 * 60 * 1000)) {
                    $scope.updateTimeLine();
                    return true;
                } else {
                    $scope.hoursBeforeAfter /= 2;
                    return false;
                }
            };

            $scope.zoomBest = function () {
                var i = true;
                while(i){
                    i = $scope.zoomIn();
                }
            };

            $scope.loadEmployees = function () {
                dataService.getScheduleEmployees($stateParams.id)
                    .then(function (result) {
                        var filteredEmployees = _.uniq(result.data, function (item) {
                            return item.id;
                        });
                        var employees = [];
                        // Filter only name attributes
                        for (var i in filteredEmployees) {
                            employees.push(
                                {
                                    id: filteredEmployees[i].id,
                                    name: filteredEmployees[i].firstName + ' '
                                    + filteredEmployees[i].lastName,
                                    ticked: false
                                }
                            );
                        }
                        // Save the filtered properties into scope variable
                        $scope.employees = employees;
                    });
            };

            $scope.selectEmployee = [];
            $scope.selectEmployeeToAssign = [];
            $scope.loadEmployees();

            $scope.updateCurrentShift = function (newShift) {
                if (newShift) {
                    $scope.currentShift = _.extend($scope.currentShift, newShift);
                }
                $scope.removeWatchers();
                $scope.initShiftEditForm();
                $scope.$broadcast('event:updateShift');
            };

            $scope.updateTimeLine = function () {
                $scope.removeWatchers();
                $scope.$broadcast('event:updateShift');

                $scope.initTimeline();
            };

            $scope.assignShift = function () {
                if ($scope.selectEmployeeToAssign.length > 0) {
                    dataService.assignShift($stateParams.id, $scope.currentShift.mainId, $scope.selectEmployeeToAssign[0].id)
                        .then(function (res) {
                            $scope.updateCurrentShift(res);
                            alertsManager.addAlert('Shift successfully assigned to ' + $scope.selectEmployee[0].name, 'success');
                        }, function (err) {
                            dialogs.error('Error', 'Error while assigning shift: ' + err.data.message, 'lg');
                        });
                }
            };

            $scope.dropShift = function () {
                var dlg = dialogs.confirm('Please Confirm', 'Do you realy want to drop shift from ' + $scope.currentShift.employeeName + '?');
                dlg.result.then(function (btn) {
                    dataService.dropShift($stateParams.id, $scope.currentShift.mainId)
                        .then(function (result) {
                            alertsManager.addAlert('Shift from ' + $scope.currentShift.employeeName + ' successfully dropped.', 'success');
                            $scope.updateCurrentShift(result);
                        }, function (error) {
                            dialogs.error("Error", "Error while dropping shift from '" + $scope.currentShift.employeeName + "': " + error.data.message, 'lg');
                        });
                }, function (btn) {
                    console.log("shift not dropped");
                });
            };

            $scope.deleteShift = function () {
                var dlg = dialogs.confirm('Please Confirm', 'Do you realy want to delete shift from ' + $scope.currentShift.employeeName + '?');
                dlg.result.then(function (btn) {
                    dataService.deleteShift($stateParams.id, $scope.currentShift.mainId)
                        .then(function (result) {
                            $scope.close_form();
                            alertsManager.addAlert('Shift from ' + $scope.currentShift.employeeName + ' successfully deleted.', 'success');
                        }, function (error) {
                            dialogs.error("Error", "Error while deleted shift from '" + $scope.currentShift.employeeName + "': " + error.data.message, 'lg');
                        });
                }, function (btn) {
                    console.log("shift not deleted");
                });
            };

            $scope.saveAndApply = function () {
                console.log($scope.currentShift);

                var dto = _.pick($scope.currentShift, 'endDateTime', 'startDateTime');
                dto.startDateTime = $scope.startDate.getTime();
                dto.endDateTime = $scope.endDate.getTime();

                dataService.updateShift($stateParams.id, $scope.currentShift.mainId, dto)
                    .then(function (result) {

                        alertsManager.addAlert('Shift from ' + $scope.currentShift.employeeName + ' successfully updated.', 'success');

                        if ($scope.dropfill === 'fill' && $scope.hasNewShift()) {
                            createNewShift();
                        }
                        if ($scope.changeToPto && $scope.currentShift.employeeId !== null && $scope.hasNewShift()) {
                            createAvailability();
                        }
                        $scope.updateCurrentShift(result);
                    }, function (error) {
                        dialogs.error("Error", "Error while updating shift from '" + $scope.currentShift.employeeName + "': " + error.data.message, 'lg');
                    });
            };

            function createNewShift() {
                var newDto = _.pick($scope.currentShift, 'teamId', 'skillId', 'skillProficiencyLevel', 'endDateTime', 'startDateTime');
                newDto.startDateTime = $scope.startPtoDate.getTime();
                newDto.endDateTime = $scope.endPtoDate.getTime();
                // create open shift
                dataService.createShift($stateParams.id, newDto)
                    .then(function (result) {
                        if ($scope.selectEmployee.length > 0) {
                            //assign to selected employee
                            dataService.assignShift($stateParams.id, result.id, $scope.selectEmployee[0].id)
                                .then(function (res) {
                                    alertsManager.addAlert('Shift successfully created and assigned to ' + $scope.selectEmployee[0].id, 'success');
                                }, function (err) {
                                    dialogs.error('Error', 'Error while assigning shift: ' + err.data.message, 'lg');
                                });
                        } else { //
                            alertsManager.addAlert('Shift successfully created.', 'success');
                            console.log(result);
                        }

                    }, function (error) {
                        dialogs.error('Error', 'Error while creating shift: ' + error.data.message, 'lg');
                    });
            }

            function createAvailability() {
                var availabilityDto = {};
                availabilityDto.startDate = $scope.startPtoDate.getTime();
                availabilityDto.startTime = (($scope.startPtoDate.getHours() * 60) + $scope.startPtoDate.getMinutes()) * 60 * 1000;
                availabilityDto.durationInMinutes = ($scope.endPtoDate.getTime() - $scope.startPtoDate.getTime()) / 1000 / 60;
                availabilityDto.absenceTypeId = $scope.selectAbsenceTypes[0].id;
                availabilityDto.availabilityType = 'UnAvail';

                dataService.createEmployeeCDAvailability($scope.currentShift.employeeId, availabilityDto)
                    .then(function (result) {
                        alertsManager.addAlert('AvailabilityTimeFrame for ' + $scope.currentShift.employeeName + ' successfully created.', 'success');
                    }, function (error) {
                        dialogs.error("Error", "Error while creating AvailabilityTimeFrame for '" + $scope.currentShift.employeeName + "': " + error.data.message, 'lg');
                    });
            }


        }
    ]);
}());
