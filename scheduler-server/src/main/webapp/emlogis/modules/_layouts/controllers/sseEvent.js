var app = angular.module('emlogis');

// This Controller will generate sseevent

app.controller('SseEventCtrl', ['$scope', '$rootScope', 'sseService',
    function ($scope, $rootScope, sseService) {

      // TEMP SseEvent TODO: where are Site-level notification being created?
      $rootScope.sseEventVisible = true;
      $scope.closeSseEvent = function () {
        $rootScope.sseEventVisible = false;
      };

        // register a consumer for SSE that will display the event in header
        // (registration can happen safely even before getting events is
        // started.)
        $scope.eventCnt = 0;
        $scope.eventData = 'no event';
        sseService.registerConsumer({
            selector: function () {
                return true;					// for now, subscribe to all
                // events
            },
            callback: function (key, serverEvent) {
                $scope.$apply(function () {     // use $scope.$apply to refresh
                    // the view
                    /**
                     * Maximum 200 characters
                     */
                    $scope.eventData = JSON.stringify(serverEvent).substr(0,200);

                    $scope.eventCnt++;
                });
            },
            scope: $scope,
            params: []
        });


    }
]);

