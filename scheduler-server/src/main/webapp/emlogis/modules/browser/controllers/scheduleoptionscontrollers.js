(function () {
    var module = angular.module('emlogis.browser');

    /**
     * Controller for schedule options
     */
    module.controller('ScheduleOptionsCtrl', ['$scope', '$stateParams', 'dataService', 'dialogs', 'alertsManager', '$rootScope',
        function ($scope, $stateParams, dataService, dialogs, alertsManager, $rootScope) {

            console.log("inside ScheduleOptionsCtrl");

            $scope.options = [

//                {name: 'Override MAX CONSECUTIVE DAYS', value:'MAX_CONSECUTIVE_DAYS_OVERRIDE'},
                {name: 'Override PTO', value: 'PTO_OVERRIDE'},
//                {name: 'Override ALL DAY UNAVAILABLE', value:'ALL_DAY_UNAVAILABLE_OVERRIDE'},
//                {name: 'Override TIME WINDOW UNAVAILABLE', value:'TIME_WINDOW_UNAVAILABLE_OVERRIDE'},
                {name: 'Override WEEKDAY ROTATION', value: 'WEEKDAY_ROTATION_OVERRIDE'},
                {name: 'Override COUPLED WEEKEND', value: 'COUPLED_WEEKEND_OVERRIDE'},
                {name: 'Override DAYS OFF AFTER', value: 'DAYS_OFF_AFTER_OVERRIDE'},
                {name: 'Override DAYS OFF BEFORE', value: 'DAYS_OFF_BEFORE_OVERRIDE'},
                {name: 'Override MIN HOURS BETWEEN DAYS', value: 'MIN_HOURS_BETWEEN_DAYS_OVERRIDE'},
                {name: 'Override MIN HOURS DAY', value: 'MIN_HOURS_DAY_OVERRIDE'},
                {name: 'Override MAX HOURS DAY', value: 'MAX_HOURS_DAY_OVERRIDE'},
                {name: 'Override MAX HOURS WEEK', value: 'MAX_HOURS_WEEK_OVERRIDE'},
                {name: 'Override MAX DAYS WEEK', value: 'MAX_DAYS_WEEK_OVERRIDE'},
                {name: 'Override MIN HOURS WEEK', value: 'MIN_HOURS_WEEK_OVERRIDE'},
//                {name: 'EMPLOYEE UNAVAILABLE', value:'EMPLOYEE_UNAVAILABLE'},
//                {name: 'Override AVOID', value:'AVOID_OVERTIME'},
//                {name: 'TEAM FLOAT ON', value:'TEAM_FLOAT_ON'}
            ];

            $scope.employees = [];
            $scope.scheduleoptions = {};

            dataService.getScheduleEmployees($stateParams.id, {}, 1, -1)
                .then(function (employees) {
                    createEmptyModel(employees);
                    dataService.getScheduleOptions($stateParams.id)
                        .then(function (options) {
                            setModel(options);
                        });
                });

            function createEmptyModel(employees) {
                $scope.employees = employees.data;

                angular.forEach($scope.employees, function (emp) {
                    emp.options = {};
                    angular.forEach($scope.options, function (opt) {
                        emp.options[opt.value] = false;
                    });
                });
            }

            function setModel(options) {
                $scope.scheduleoptions = options;
                var opts = options.overrideOptions;
                for (var i in opts) {
                    var j;
                    switch (opts[i].scope) {
                        case "None":
                            break;
                        case "All":
                            for (j = 0; j < $scope.employees.length; j++) {
                                $scope.employees[j].options[i] = true;
                            }
                            break;
                        case "Select":
                            for (j = 0; j < $scope.employees.length; j++) {
                                if (_.contains(opts[i].employeeIds, $scope.employees[j].id)) {
                                    $scope.employees[j].options[i] = true;
                                }

                            }
                            break;
                    }
                }
            }

            $scope.change = function (emp, opt) {

                var option = $scope.scheduleoptions.overrideOptions[opt.value];
                var ids = option.employeeIds;
                if (emp.options[opt.value]) {
                    ids.push(
                        emp.id
                    );
                } else {
                    if (option.scope == "All") {
                        for (var j = 0; j < $scope.employees.length; j++) {
                            ids.push(
                                $scope.employees[j].id
                            );
                        }
                    }
                    var index = ids.indexOf(emp.id);
                    ids.splice(index, 1);

                }

                switch (ids.length) {
                    case 0:
                        option.scope = "None";
                        break;
                    case $scope.employees.length:
                        option.scope = "All";
                        ids.length = 0;
                        break;
                    default :
                        option.scope = "Select";
                        break;
                }

            };

            $scope.clear = function (opt) {
                var option = $scope.scheduleoptions.overrideOptions[opt.value];
                var ids = option.employeeIds;
                option.scope = "None";
                ids.length = 0;
                for (var j = 0; j < $scope.employees.length; j++) {
                    $scope.employees[j].options[opt.value] = false;
                }


            };

            $scope.all = function (opt) {
                var option = $scope.scheduleoptions.overrideOptions[opt.value];
                var ids = option.employeeIds;
                option.scope = "All";
                ids.length = 0;
                for (var j = 0; j < $scope.employees.length; j++) {
                    $scope.employees[j].options[opt.value] = true;
                }
            };

            $scope.saveOptions = function () {
                dataService.updateScheduleOptions($stateParams.id, $scope.scheduleoptions)
                    .then(function (result) {
                        alertsManager.addAlert("'" + $stateParams.id + "' successfully updated.", 'success');
                    }, function (error) {
                        dialogs.error("Error", error.data.message, 'lg');
                    });
            };

        }
    ]);


}());

