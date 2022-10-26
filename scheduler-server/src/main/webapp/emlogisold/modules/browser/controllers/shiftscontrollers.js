(function () {
    var module = angular.module('emlogis.browser');

    /**
     * Controller for Shifts grid
     */
    module.controller('ShiftsCtlr', ['$scope', '$stateParams', 'dataService', 'dialogs', '$rootScope',
        function($scope, $stateParams, dataService, dialogs, $rootScope) {

            console.log("inside ShiftsCtlr");

            var shiftsctrl = new ShiftsBaseCtlr();
            shiftsctrl.init.call(shiftsctrl, $scope, $stateParams, dataService, dialogs, $rootScope);

        }
    ]);

    /**
     * Directive for creating dhtmlx scheduler component
     */
    module.directive('dhxScheduler', function ($rootScope) {
        return {
            restrict: 'A',
            transclude: true,
            template: '<div class="dhx_cal_navline" ng-transclude></div>' +
                '<div class="dhx_cal_header"></div>' +
                '<div class="dhx_cal_data"></div>' +
                '<div ng-show="displayLoadingIndicator" class="loadingIndicator"></div>',

            link: function ($scope, $element) {
                //default state of the scheduler

                if (!$rootScope.scheduler)
                    $rootScope.scheduler = {};
                $rootScope.scheduler.mode = $rootScope.scheduler.mode || "timeline";
                var scheduleStartDate = angular.element('form[name="form1"]').scope().elt.startDate;
                $rootScope.scheduler.date = scheduleStartDate || $rootScope.scheduler.date;
                $rootScope.scheduler.firstLoad = true;


                //mode or date
                $scope.$watch(function () {
                    return $rootScope.scheduler.mode + $rootScope.scheduler.date.toString();
                }, function (nv, ov) {
                    var mode = scheduler.getState();
                    if (nv.date != mode.date || nv.mode != mode.mode)
                        scheduler.setCurrentView($rootScope.scheduler.date, $rootScope.scheduler.mode);
                }, true);


                scheduler.dhtmlXTooltip.config.className = 'dhtmlXTooltip tooltip';
                scheduler.dhtmlXTooltip.config.timeout_to_display = 50;
                scheduler.dhtmlXTooltip.config.delta_x = 15;
                scheduler.dhtmlXTooltip.config.delta_y = -20;

                var format=scheduler.date.date_to_str("%Y-%m-%d %H:%i");
                scheduler.templates.tooltip_text = function(start,end,event) {

                    var employee = event.employeeId ? event.text : "not assigned";
                    var template = "<b>Employee:</b> " + employee + "<br/>" +
                        "<b>Start date:</b> " + format(new Date(event.startDateTime)) +"<br/>" +
                        "<b>End date:</b> "+ format(new Date(event.endDateTime)) +"<br/>" +
                        "<b>Type:</b> " + event.shiftTypeName+"<br/>" +
                        "<b>Skill:</b> " + event.skillName+"<br/>" +
                        "<b>Team:</b> " + event.teamName
                    return template;
                };

                //styling for dhtmlx scheduler
                $element.addClass("dhx_cal_container");

                scheduler.createTimelineView({
                    section_autoheight: false,
                    name:"timeline",
                    x_unit:"minute",//measuring unit of the X-Axis.
                    x_date:"%H:%i", //date format of the X-Axis
                    x_step:60,      //X-Axis step in 'x_unit's
                    x_size:24,      //X-Axis length specified as the total number of 'x_step's
                    x_start:0,     //X-Axis offset in 'x_unit's
                    x_length:24,    //number of 'x_step's that will be scrolled at a time
                    y_unit:         //sections of the view (titles of Y-Axis)
                        scheduler.serverList("sections"),
                    y_property: "sectionId", //mapped data property
                    render:"bar"             //view mode
                });

                scheduler.locale.labels.timeline_tab = "Day";

                //Displaying shift text in week view
                scheduler.templates.event_text=function(start,end,event){
                    var notEndedText = "";
                    if(event.notEnded){
                        notEndedText = "-->..";
                    }
                    var continueText = "";
                    if(event.continue){
                        continueText = "..-->";
                    }
                    return "<span title='"+event.text+"'>"+event.text+"</span><br>" +
                        "<span>"+continueText+"</span><br>" +
                        "<span style='position: absolute; bottom: 10px;'>"+notEndedText+"</span>";
                }

                if(!scheduler._is_initialized()){
                    scheduler.attachEvent("onViewChange", function (nm, nd){
                        if ($rootScope.scheduler.firstLoad || nd.getTime() != $rootScope.scheduler.date.getTime() || nm != $rootScope.scheduler.mode){
                            $rootScope.getElements();
                            $rootScope.scheduler.firstLoad = false;
                        }
                        $rootScope.scheduler.mode = nm;
                        $rootScope.scheduler.date = nd;
                    });
                }
                //init scheduler
                scheduler.init($element[0], $rootScope.scheduler.mode, $rootScope.scheduler.date);

            }
        }
    });

}());

var ShiftsBaseCtlr = function() {   // defining base constructor
    console.log("ShiftsBaseCtlr base constructor");
    return this;
};


ShiftsBaseCtlr.prototype.init = function($scope, $stateParams, dataService, dialogs, $rootScope) {

    var entityId = $stateParams.id;

    $scope.colors = {
        openshift: '#FF5D5D',
        assignedshift: '#1796b0',
        splittedshift: '#037D94'
    }

    $scope.openShifts = false;
    $scope.openShiftsFilter = "assignmentType is null";

    $scope.currentFilters = [];
    $scope.mainFilters = [];

    $scope.filters = [];

    $scope.addFilter = function () {
        $scope.filters.push({type: $scope.groups[0], value: ''});
    }

    $scope.removeFilter = function (elt) {
        var index = $scope.filters.indexOf(elt);
        $scope.filters.splice(index, 1)
    }

    $scope.go = function () {
        $scope.mainFilters.length = 0;
        angular.forEach($scope.filters, function (filter) {
            $scope.mainFilters.push(filter.type+"Name"+"='"+filter.value+"'");
        })
        $scope.getElements();
    }
    $scope.addOpenShiftsFilter = function () {
        $scope.currentFilters.push($scope.openShiftsFilter);
    };

    $scope.removeOpenShiftsFilter = function () {
        var index = $scope.currentFilters.indexOf($scope.openShiftsFilter);
        $scope.currentFilters.splice(index, 1)
    };

    $scope.switchOpen = function () {
        $scope.openShifts = !$scope.openShifts;
        if(!$scope.openShifts){
            $scope.removeOpenShiftsFilter();
            $scope.selected = "";
        } else {
            $scope.addOpenShiftsFilter();
            $scope.selected = "btn-primary";
        }
        $scope.getElements();
    }

    $scope.events = [];

    $scope.groups = ['team','employee'];
    $scope.currentGroup = $scope.groups
        [0];
//            $rootScope.scheduler = { date: new Date() };

    $rootScope.getElements = function() {
        var startDate = scheduler.getState().min_date;
        var endDate = scheduler.getState().max_date;

        if(startDate && endDate){
            dataService.getScheduleShiftsByPeriod(entityId, startDate.getTime(), endDate.getTime(),
                $scope.currentFilters.concat($scope.mainFilters))
                .then(function (result) {
                    var data = result.data;

                    $scope.events = _.map(data, function (elt) {
                        var opened = false;
                        if(!elt.employeeId){
                            opened = true;
                        }
                        var event = elt;
                        event.text = !opened ? elt.employeeName : 'Open Shift';
                        event.start_date = new Date(elt.startDateTime);
                        event.end_date = new Date(elt.endDateTime);
                        event.color = !opened ? $scope.colors.assignedshift : $scope.colors.openshift;
                        event.sectionId = elt[$scope.currentGroup + 'Id'] || 'Open';
                        event.mainId = elt.id;
                        return event;
                    });

                    $scope.events = splitShifts($scope.events);

                    $scope.sections = _.uniq(_.map(data, function (elt) {
                        return {
                            key:elt[$scope.currentGroup + 'Id'] || 'Open',
                            label:elt[$scope.currentGroup + 'Name'] || 'Open'
                        };

                    }), function (item) {
                        return item.key;
                    });
//                            $scope.sections = _.filter($scope.sections, function(n){ return n.key != null });

                    scheduler.updateCollection("sections", $scope.sections);
                    scheduler.clearAll();
                    var shifts = angular.copy($scope.events);
                    //updating shifts
                    scheduler.parse(shifts, "json");

                    $scope.displayLoadingIndicator = false;

                }, function (error) {
                    $scope.displayLoadingIndicator = false;
                    dialogs.error("Error", error.data.message, 'lg');
                })
            $scope.displayLoadingIndicator = true;
        }

    }

    //Spliting shifts which takes multiply days
    function splitShifts(shifts) {
        for (var i = 0; i < shifts.length; i++ ){
            if(shifts[i].start_date.getDate() < shifts[i].end_date.getDate() ){
                shifts.splice(i+1, 0, angular.copy(shifts[i]));
                shifts[i].end_date.setDate(shifts[i].start_date.getDate()+1);
                shifts[i].end_date.setHours(0,0,-1);
                shifts[i+1].start_date.setDate(shifts[i+1].start_date.getDate()+1);
                shifts[i+1].start_date.setHours(0,0);

                shifts[i+1].id = shifts[i+1].id+'=='+i;

                shifts[i].notEnded = true;
                shifts[i+1].continue = true;

                shifts[i].color = $scope.colors.splittedshift;
                shifts[i+1].color = $scope.colors.splittedshift;
            }
        }
        return shifts;

    }

    return this;
};