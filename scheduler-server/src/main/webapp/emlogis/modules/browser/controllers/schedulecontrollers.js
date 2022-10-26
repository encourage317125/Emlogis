// TODO: allow the selection of more than one Schedule on creation


(function () {

    var module =  angular.module('emlogis.browser');

    /* 
     * Controller for a single Schedule entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per Schedule API definition
     */
    // Image Base folder path

    // Image Base folder path
    var imagePath = "modules/browser/assets/images/";
    
    module.controller(
        'ScheduleCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'sseService', 'appContext',
        'dataService', '$modal', 'dialogs', 'alertsManager',
        function($scope, $stateParams, $state, stateManager, $filter, authService, sseService, appContext,
                 dataService, $modal, dialogs, alertsManager) {

            console.log("ScheduleCtlr: " + getScopeHierachy($scope));

            $scope.maxComputationTimes = [
                {label:'Infinite',   id:0, val: -1},
                {label:'1  min',     id:1, val: 60},
                {label:'5  min',     id:2, val: 300},
                {label:'10 min',     id:3, val: 600},
                {label:'15 min',     id:4, val: 900},
                {label:'30 min',     id:5, val: 1800}
            ];

            $scope.maximumUnimprovedSecondsSpents = [
                {label:'Default (100)',   id:0, val: 0},
                {label:'10',     id:0, val: 10},
                {label:'20',     id:1, val: 20},
                {label:'50',     id:2, val: 50},
                {label:'100',    id:3, val: 100},
                {label:'200',    id:4, val: 200}
            ];


            $scope.scheduleTypes = [
                {label:'Pattern based',   id:0, val: 'ShiftPatternBased'},
                {label:'Shift Structure based',     id:1, val: 'ShiftStructureBased'}
            ];

            /*
            * override postGetElement() to compute durations 
            */
            $scope.postGetElement = function() {
                var elt = $scope.elt;
                if (elt) {
                    _.extend($scope, {
                        totalExeTime: duration(elt.executionStartDate, elt.executionEndDate),
                        requestBuildTime: duration(elt.executionStartDate, elt.requestSentDate),
                        requestAckTime: duration(elt.requestSentDate, elt.executionAckDate),
                        engineProcessingTime: duration(elt.executionAckDate, elt.responseReceivedDate),
                        responseProcessingTime: duration(elt.responseReceivedDate, elt.executionEndDate)
                    });

                    var maxComputationTime = elt.maxComputationTime;
                    var options = $scope.maxComputationTimes;
                    var i;
                    for (i=0 ; i < options.length; i++) {
                        if (options[i].val == maxComputationTime) {
                            $scope.maxComputationTime = options[i];
                            break;
                        }
                    }

                    var maximumUnimprovedSecondsSpent = elt.maximumUnimprovedSecondsSpent;
                    options = $scope.maximumUnimprovedSecondsSpents;
                    for (i=0 ; i < options.length; i++) {
                        if (options[i].val == maximumUnimprovedSecondsSpent) {
                            $scope.maximumUnimprovedSecondsSpent = options[i];
                            break;
                        }
                    }

                    var scheduleType = elt.scheduleType;
                    options = $scope.scheduleTypes;
                    for (i=0 ; i < options.length; i++) {
                        if (options[i].val == scheduleType) {
                            $scope.scheduleType = options[i];
                            break;
                        }
                    }
                }
            };

            EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService, dialogs, alertsManager);

            $scope.ctlr = 'ScheduleCtrl';
            // set some default values if create mode
            if (!$scope.elt || !$scope.elt.id) {
                $scope.maxComputationTime = $scope.maxComputationTimes[0];
                $scope.maximumUnimprovedSecondsSpent = $scope.maximumUnimprovedSecondsSpents[3];
                $scope.scheduleType = $scope.scheduleTypes[0];
                $scope.preservePreAssignedShifts = false;
                $scope.preservePostAssignedShifts = false;
           }


            // register an event consumer to diplay the progression of the schedule execution
            // ideally we should unregister this listener when leaving this page.
            // possibly need to register a stateLeve event to do that

            //"<SysNotifications><jcso><><Schedule><Progress><bf2f0a13-b1c1-4db6-bd39-2dd351e39dd6>"
            // Key format: <topic><tenantId><accountId><entityClass><eventType><entityId>

            sseService.registerConsumer({
                id: 'scheduleCtlrProgressHandler',
                selector: function (key) {
                    if ($scope.elt && $scope.elt.id) {
                        var keyselector = '<SysNotifications><.*><><Schedule><Progress><' + $scope.elt.id + '>';
                        var match = key.match(keyselector);
                        console.log( "Schedule Progress event: " + key + " with: " + keyselector + " -> " + match); 
                        return key.match(keyselector);
                    }
                    return false;                    // for now, subscribe to all events
                },
                callback: function (key, serverEvent) {
                    $scope.$apply(function () {     // use $scope.$apply to refresh the view
                        $scope.progress = serverEvent.progress;
                        $scope.hardScore = (serverEvent.hardScore == -999999 ? '' : serverEvent.hardScore);
                        $scope.mediumScore = (serverEvent.mediumScore == -999999 ? '' : serverEvent.mediumScore);
                        $scope.softScore = (serverEvent.softScore == -999999 ? '' : serverEvent.softScore);
                        $scope.progressInfo  = serverEvent.msg;
                    });
                },
                scope: $scope,
                params: []
            });
            sseService.registerConsumer({
                id: 'scheduleCtlrUpdateHandler',
                selector: function (key) {
                    if ($scope.elt && $scope.elt.id) {
                        var keyselector = '<ObjLifecycle><.*><><Schedule><Update><' + $scope.elt.id + '>';
                        var match = key.match(keyselector);
                        console.log( "Schedule Update event: " + key + " with: " + keyselector + " -> " + match); 
                        return key.match(keyselector);
                    }
                    return false;                    // for now, subscribe to all events
                },
                callback: function (key, serverEvent) {
                    $scope.$apply(function () {     // use $scope.$apply to refresh the view
                        var updatedElt = serverEvent;
                        // convert date attributes from longs to Js dates (done by dataservice layer normally)
                        var dateAttributes = ['created', 'updated', 'startDate', 'endDate',
                            'executionStartDate', 'requestSentDate', 'executionAckDate', 'responseReceivedDate', 'executionEndDate'
                        ];                      
                        for (var i=0 ; i < dateAttributes.length; i++) {
                            var dateAttr = dateAttributes[i];
                            if (updatedElt[dateAttr] > 0) {
                                updatedElt[dateAttr] = new Date(updatedElt[dateAttr]);
                            }
                            else {
                                updatedElt[dateAttr] =  '-';   
                            }
                        }
                        // then update current elt in scope
                        $scope.elt = updatedElt;
                    });
                },
                scope: $scope,
                params: []
            });


            // Override the method to build the create and update Dtos

            /*
            * getCreateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object create  
            */
            $scope.getCreateDto = function() {
                console.log('in ScheduleCtlr.getCreateDto()');
                // build the create Dto
                var elt = $scope.elt;

                // base create dto
                //var dto = _.pick( elt, 'startDate', 'teamIds');
                var startDate = Date.parse(elt.startDate); 
                var dto = {
                    clName: elt.clName,
                    startDate: startDate, 
                    scheduleLengthInDays: 7,
                    scheduleType: 'ShiftStructureBased',
                    teamIds:[elt.teamIds]
                };

                // add update dto 
                dto.updateDto =  _.pick(elt, 'name', 'description');
                dto.updateDto.status = 'Simulation';
                dto.updateDto.maxComputationTime = $scope.maxComputationTime.val; 
                dto.updateDto.maximumUnimprovedSecondsSpent = $scope.maximumUnimprovedSecondsSpent.val;
                dto.updateDto.scheduleType = $scope.scheduleType.val; 
                dto.updateDto.preservePreAssignedShifts = $scope.preservePreAssignedShifts;
                dto.updateDto.preservePostAssignedShifts = $scope.preservePostAssignedShifts;
                return dto;
            };


            /*
            * getUpdateDto() method to be overriden in some controllers that need
            * to process the data to be sent to the backend on object update  
            */
            $scope.getUpdateDto = function() {
                console.log('in ScheduleCtlr.getUpdateDto()');
                var elt = $scope.elt;
                var dto =  _.pick( elt, 'clName', 'name', 'description', 'ruleWeightMultipliers', 'preservePreAssignedShifts', 'preservePostAssignedShifts');
//                var startDate = Date.parse(elt.startDate);
//                dto.startDate = startDate;
                dto.maxComputationTime = $scope.maxComputationTime.val;
                dto.maximumUnimprovedSecondsSpent = $scope.maximumUnimprovedSecondsSpent.val;
                // remove scheduleType as no longer supported by schedule.update API
                delete dto.scheduleType;
                return dto;
            };

            $scope.vetoCreate = function() {
                if ($scope.elt.teamIds) {
                    // sideId is present, one can create the Schedule
                    return false;
                }
                // needs to select a Site

                $scope.opts = {
                    scope: $scope,  // use current scope for modal so as to get results more conveniently
                    backdrop: true,
                    backdropClick: true,
                    dialogFade: false,
                    keyboard: true,
                    templateUrl : 'modules/browser/partials/entity_selectentity.tpl.html',
                    controller : ModalInstanceCtrl,
                    size : 'lg',
                    resolve: {} // empty storage
                };

                var subctlrConfig = {
                    title: 'Please select a Team',
                    entity: $scope.entity,
                    relatedentity: 'team',
                    entityTypeLabel: '$scope.entityTypeLabel',
                    relatedentityTypeLabel: 'Team',
                    dataServiceResource: 'teams',
                    dataServiceGetEntitiesAPI: 'getElements',
                    columns:  [ // grid columns
                        {name: 'name', label: 'Name', sortable: true},
                        {name: 'description', label: 'Description', sortable: true},
                        {name: 'id', label: 'Id',sortable: true}
                    ],
                    relatedEntityIcon: imagePath + 'glyphicons/png/team.png'
                };

                $scope.opts.resolve.item = function() {
                    return angular.copy(subctlrConfig); // pass params to Dialog
                };

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(selectedEntity){
                    //on ok button press
                    $scope.elt.teamIds = selectedEntity.id;
                    // how to trigger saveOrCreate() on base editCtlr ?
                    console.log("Fire create...");
                    $scope.saveOrCreate();
                },function(){
                    //on cancel button press
                    $scope.elt.teamIds = false;  // clear selection on cancel
                    console.log("Modal Closed");
                });
                return true;
            };


            /*
            * Execute a Schedule 
            */
            $scope.execute = function() {
                console.log('in ScheduleCtlr.execute()');
                var elt = $scope.elt;
                $scope.progress = 0;
                $scope.hardScore = '';
                $scope.mediumScore = '';
                $scope.softScore = '';
                $scope.progressInfo = '';

                var dto = {
                    maxComputationTime: elt.maxComputationTime,
                    maximumUnimprovedSecondsSpent: elt.maximumUnimprovedSecondsSpent
                };
                dataService
                .executeSchedule(elt.id, dto)
                .then(function (elt) {
                    console.log('--> executed[' + elt.name + ': ' + elt.id + ']'); 
                    $scope.elt = elt;  // update view with updated schedule
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            };

            /*
            * Carry Schedule over to next period
            */
            $scope.carryOverNextOrPrev = function(next) {
                console.log('in ScheduleCtlr.carryOverNext()');
                var elt = $scope.elt;
                var newDate = elt.startDate.getTime();
                var delta = elt.scheduleLengthInDays * 24 * 3600 * 1000;
                if (next) {
                    newDate += delta;
                }
                else {
                    newDate -= delta;
                }
                console.log("Duplicating schedule for: " + new Date(newDate));
                dataService
                .duplicateSchedule(elt.id, {startDate: newDate, mode:0})
                .then(function (elt) {
                    $scope.elt = elt;   // updated view with 
                                        // TODO update Shifts tab
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            };

            /*
            * Promote Schedule 
            */
            $scope.promote = function() {
                console.log('in ScheduleCtlr.promote()');
                var elt = $scope.elt;

                dataService
                .promoteSchedule(elt.id)
                .then(function (elt) {
                    $scope.elt = elt;   // updated view with 
                                        // TODO update Shifts tab
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            };

            /*
            * Generate Shifts 
            */
            $scope.generateShifts = function() {
                console.log('in ScheduleCtlr.generateShifts()');
                var elt = $scope.elt;

                dataService
                .generateShifts(elt.id)
                .then(function (elt) {
                    $scope.elt = elt;   // updated view with 
                                        // TODO update Shifts tab
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            };

            /*
            * Reset Schedule State 
            */
            $scope.resetState = function() {
                console.log('in ScheduleCtlr.resetState()');
                var elt = $scope.elt;

                dataService
                .resetScheduleState(elt.id)
                .then(function (elt) {
                    $scope.elt = elt;   // updated view with 
                                        // TODO update Shifts tab
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            };

            /*
            * Refresh a Schedule 
            */
            $scope.refresh = function() {
                $scope.getElement();
                $state.go('authenticated.entity.relatedentity', {relatedentity: 'shift'},  {reload: true});
            }; 

            /*
            * Abort a Schedule 
            */
            $scope.abort = function() {
                console.log('in ScheduleCtlr.abort()');
                var elt = $scope.elt;

                dataService
                .abortSchedule(elt.id)
                .then(function (elt) {
                    console.log('--> abort sent[' + elt.name + ': ' + elt.id + ']'); 
                    $scope.elt = elt;  // update view with update schedule
                }, function (error) {
                    dialogs.error("Error", error.data.message, 'lg');
                });               
            };

            /*
             * Edit Constraints modal dialog
             */
            $scope.editConstraints = function () {
                initRules();

                $scope.opts = {
                    scope: $scope,  // use current scope for modal so as to get results more conveniently
                    backdrop: true,
                    backdropClick: true,
                    dialogFade: false,
                    keyboard: true,
                    templateUrl : 'modules/browser/partials/schedule_constraints.html',
                    controller : SimpleModalInstanceCtrl,
                    resolve: {
                        newSchedule: function () {
                            return $scope.rules;
                        }
                    }
                };

                var modalInstance = $modal.open($scope.opts);
                modalInstance.result.then(function(){
                    //on ok button press
                    $scope.elt.ruleWeightMultipliers = $scope.rules;

                },function(){
                    //on cancel button press
                    console.log("Modal Closed");
                });
                return true;
            };

            function initRules() {
                $scope.rules = angular.copy($scope.elt.ruleWeightMultipliers);
                $scope.rulesIsEnable = angular.copy($scope.elt.ruleWeightMultipliers);
                for(var i in $scope.rulesIsEnable){
                    if($scope.rulesIsEnable[i]>0){
                        $scope.rulesIsEnable[i] = true;
                    } else {
                        $scope.rulesIsEnable[i] = false;
                    }
                }
                $scope.rulesBackup = {};
            }

            $scope.disableEnableRule = function (rule) {
                if($scope.rulesIsEnable[rule]){
                    $scope.rules[rule] = $scope.rulesBackup[rule] || 1;
                } else {
                    $scope.rulesBackup[rule] = $scope.rules[rule];
                    $scope.rules[rule] = 0;
                }
            };

            $scope.resetAllRules = function () {
                initRules();
            };

            $scope.resetRule = function (rule) {
                $scope.rules[rule] = $scope.elt.ruleWeightMultipliers[rule];
                $scope.rulesIsEnable[rule] = $scope.elt.ruleWeightMultipliers[rule];
                if($scope.rulesIsEnable[rule]>0){
                    $scope.rulesIsEnable[rule] = true;
                } else {
                    $scope.rulesIsEnable[rule] = false;
                }
            };

            /*
             * Duplicate schedule modal dialog
             */
            $scope.duplicate = function () {

                var newDate = angular.copy($scope.elt.startDate);
                newDate.setDate($scope.elt.startDate.getDate() + $scope.elt.scheduleLengthInDays);
                $scope.newSchedule = {
                    startDate: newDate,
                    mode: $scope.assignModes[0]
                };
                $scope.opts = {
                    scope: $scope,  // use current scope for modal so as to get results more conveniently
                    backdrop: true,
                    backdropClick: true,
                    dialogFade: false,
                    keyboard: true,
                    templateUrl : 'modules/browser/partials/schedule_duplicate.html',
                    controller : SimpleModalInstanceCtrl,
                    resolve: {
                        newSchedule: function () {
                            return $scope.newSchedule;
                        }
                    }
                };

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(){
                    //on ok button press
                    var dto = $scope.newSchedule;
                    console.log("Duplicating schedule for: " + dto.startDate);
                    dto.mode = dto.mode.val;
                    dto.startDate.setHours(0,0,0);
                    dto.startDate = dto.startDate.getTime();
                    dataService.duplicateSchedule($stateParams.id, dto)
                        .then(function (data) {
                            $state.go('authenticated.entity', {entity: "schedule", id: data.id});
                        }, function (error) {
                            dialogs.error("Error", error.data.message, 'lg');
                        });
                },function(){
                    //on cancel button press
                    console.log("Modal Closed");
                });
                return true;
            };

            $scope.assignModes = [
                {label: 'NOASSIGNMENT', val: 'NOASSIGNMENT'},
                {label:'PREASSIGNMENT', val: 'PREASSIGNMENT'},
                {label:'ALLASSIGNMENT', val: 'ALLASSIGNMENT'}
            ];

            $scope.newSchedule = {};

            $scope.executeWithMaxDuration = function () {
                $scope.opts = {
                    scope: $scope,  // use current scope for modal so as to get results more conveniently
                    backdrop: true,
                    backdropClick: true,
                    dialogFade: false,
                    keyboard: true,
                    templateUrl : 'modules/browser/partials/schedule_execute.html',
                    controller : SimpleModalInstanceCtrl,
                    resolve: {
                        executeConfig: function () {
                            return $scope.executeConfig;
                        }
                    }
                };

                var modalInstance = $modal.open($scope.opts);

                modalInstance.result.then(function(){
                    //on ok button press
                    var config = $scope.executeConfig;
                    config.maxComputationTime = config.maxComputationTime.val;
                    dataService.executeSchedule($stateParams.id, config)
                        .then(function (elt) {
                            console.log('--> executed[' + elt.name + ': ' + elt.id + ']');
                            $scope.elt = elt;  // update view with updated schedule
                        }, function (error) {
                            dialogs.error("Error", error.data.message, 'lg');
                        });
                },function(){
                    //on cancel button press
                    console.log("Modal Closed");
                });
                return true;
            };

            $scope.executeConfig = {
                maxComputationTime: $scope.maxComputationTimes[0]
            };

    }]);

    var SimpleModalInstanceCtrl = function ($scope, $modalInstance, $modal) {
        $scope.ok = function () {
            $modalInstance.close();
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
            
}());



