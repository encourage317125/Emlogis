(function () {
    var module = angular.module('emlogis.browser');

    /**
     * Controller for schedule options
     */
    module.controller('ScheduleReportCtrl', ['$scope', '$stateParams', 'dataService', 'dialogs', 'alertsManager', '$rootScope',
        function ($scope, $stateParams, dataService, dialogs, alertsManager, $rootScope) {

            console.log("inside ScheduleReportCtrl");

            $scope.elt = angular.element('form[name="form1"]').scope().elt;

            dataService.getScheduleReport($stateParams.id)
                .then(function (responce) {
                    $scope.report = responce;
                    $scope.report.completionReport = angular.fromJson(responce.completionReport);
                });


        }
    ]);


}());

