/**
 * Created by emlogis on 10/15/14.
 */
(function () {
    var module =  angular.module('emlogis.browser');
    /*
     * Controller for a single Site entity
     * Extends / Overrides the Generic 'EntityCtlr' to buid the create and update Dtos as per Team API definition
     */
    module.controller(
        'SiteCtlr',
        ['$scope', '$stateParams', '$state', 'stateManager', '$filter', 'authService', 'appContext', 'dataService',
            'dialogs', 'alertsManager',
            function($scope, $stateParams, $state, stateManager, $filter, authService, appContext, dataService,
                     dialogs, alertsManager) {

                $scope.weekendDefinitions = [
                    {label:'SATURDAY_SUNDAY', val: 'SATURDAY_SUNDAY'},
                    {label:'FRIDAY_SATURDAY_SUNDAY', val: 'FRIDAY_SATURDAY_SUNDAY'},
                    {label:'FRIDAY_SATURDAY_SUNDAY_MONDAY', val: 'FRIDAY_SATURDAY_SUNDAY_MONDAY'},
                    {label:'SATURDAY_SUNDAY_MONDAY', val: 'SATURDAY_SUNDAY_MONDAY'}
                ];

                $scope.firstDayOfWeeks = [
                    {label:'SUNDAY', val: 'SUNDAY'},
                    {label:'MONDAY', val: 'MONDAY'},
                    {label:'TUESDAY', val: 'TUESDAY'},
                    {label:'WEDNESDAY', val: 'WEDNESDAY'},
                    {label:'THURSDAY', val: 'THURSDAY'},
                    {label:'FRIDAY', val: 'FRIDAY'},
                    {label:'SATURDAY', val: 'SATURDAY'}
                ];

                $scope.postGetElement = function() {
                    var elt = $scope.elt;
                    if (elt) {

                        var firstDayOfWeek = elt.firstDayOfWeek;
                        var firstDayOfWeeks = $scope.firstDayOfWeeks;
                        var i;
                        for (i=0 ; i < firstDayOfWeeks.length; i++) {
                            if (firstDayOfWeeks[i].val == firstDayOfWeek) {
                                $scope.firstDayOfWeek = firstDayOfWeeks[i];
                                break;
                            }
                        }
                        var weekendDefinition = elt.weekendDefinition;
                        var weekendDefinitions = $scope.weekendDefinitions;
                        for (i=0 ; i < weekendDefinitions.length; i++) {
                            if (weekendDefinitions[i].val == weekendDefinition) {
                                $scope.weekendDefinition = weekendDefinitions[i];
                                break;
                            }
                        }
                        var timeZone = elt.timeZone;
                        var timeZones = $scope.timeZones;
                        for (i=0 ; i < timeZones.length; i++) {
                            if (timeZones[i] == timeZone) {
                                $scope.timeZone = timeZones[i];
                                break;
                            }
                        }
                    }
                };

                EditOrCreateEntityCtlr($scope, $stateParams, $state, stateManager, $filter, authService, appContext,
                    dataService, dialogs, alertsManager);

                $scope.ctlr = 'SiteCtlr';
                // set some default values if create mode
                if (!$scope.elt || !$scope.elt.id) {
                    $scope.firstDayOfWeek = $scope.firstDayOfWeeks[0];
                    $scope.weekendDefinition = $scope.weekendDefinitions[0];
                }

                dataService.getTimeZones()
                    .then(function (responce) {
                        $scope.timeZones = responce;
                    });

                // Override the method to build the create and update Dtos

                /*
                 * getCreateDto() method to be overriden in some controllers that need
                 * to process the data to be sent to the backend on object create
                 */
                $scope.getCreateDto = function() {

                    // build the create Dto
                    var elt = $scope.elt;
                    var dto = _.pick( elt,  'clName', 'name', 'description', 'timeZone', 'weekendDefinition', 'firstDayOfWeek');
                    dto.weekendDefinition = $scope.weekendDefinition.val;
                    dto.firstDayOfWeek = $scope.firstDayOfWeek.val;
                    dto.timeZone = $scope.timeZone;
                    return dto;
                };


                /*
                 * getUpdateDto() method to be overriden in some controllers that need
                 * to process the data to be sent to the backend on object update
                 */
                $scope.getUpdateDto = function() {

                    var elt = $scope.elt;
                    var dto = _.pick( elt, 'clName', 'name', 'description', 'timeZone', 'weekendDefinition', 'firstDayOfWeek');
                    dto.weekendDefinition = $scope.weekendDefinition.val;
                    dto.firstDayOfWeek = $scope.firstDayOfWeek.val;
                    dto.timeZone = $scope.timeZone;
                    return dto;
                };

            }
        ]);
}());