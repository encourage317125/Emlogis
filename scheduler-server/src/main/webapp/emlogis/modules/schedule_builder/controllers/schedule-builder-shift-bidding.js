var scheduleBuilder = angular.module('emlogis.schedule_builder');

scheduleBuilder.controller('ScheduleBuilderShiftBiddingCtrl',
    [
        '$scope',
        '$state',
        'applicationContext',
        'crudDataService',
        'uiGridConstants',
        function($scope, $state, applicationContext, crudDataService,
                 uiGridConstants  ) {

            console.log('Schedule Builder Shift Bidding controller');

            $scope.tabs[2].active = true;

        }
    ]
);
