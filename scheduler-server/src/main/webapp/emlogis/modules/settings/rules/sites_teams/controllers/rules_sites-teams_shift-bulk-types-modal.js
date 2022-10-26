(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesNewShiftBulkTypesModalCtrl',
    ['$scope', '$timeout', '$modalInstance', 'rulesShiftLTService', 'types', 'selectedLength', 'site',
      function ($scope, $timeout, $modalInstance, rulesShiftLTService, types, selectedLength, site) {

        //--------------------------------------------------------------------
        // Defaults for Bulk Shift Types Modal
        //--------------------------------------------------------------------

        var defaultTime = moment().hour(0).minute(0).second(0).milliseconds(0),
            bulk = this;

        bulk.types = types;
        bulk.site = site;
        bulk.selectedLength = selectedLength;

        bulk.minutesSteps = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];
        bulk.minutesStepSelected = 0;

        bulk.startTime = defaultTime;
        bulk.endTime = defaultTime;



        bulk.add = function() {
          var startTime = moment(bulk.startTime);
          var endTime = moment(bulk.endTime);

          var dto = {
            "isActive":       true,
            "shiftLengthId":  bulk.selectedLength.id,
            "interval":       bulk.minutesStepSelected,
            "startTime":      startTime.diff(moment(startTime).startOf('day')),
            "endTime":        endTime.diff(moment(endTime).startOf('day')),
            "baseName":       ''
          };
          console.log('bulkDto', dto);

          rulesShiftLTService.bulkAddNewShiftTypes(bulk.site.id, dto).then(function(types){
            bulk.types = types;
            bulk.closeModal();
          });
        };



        bulk.isValid = function() {
          var startTime = moment(bulk.startTime);
          var endTime = moment(bulk.endTime);
          return startTime.isBefore(endTime) && bulk.minutesStepSelected !== 0;
        };



        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        bulk.closeModal = function () {
          $modalInstance.close(bulk.types);
        };


        //
        // If user navigates away from the page,
        // dismiss the modal

        $scope.$on('$stateChangeStart', function(){
            $modalInstance.dismiss('cancel');
          }
        );

      }
    ]);
})();