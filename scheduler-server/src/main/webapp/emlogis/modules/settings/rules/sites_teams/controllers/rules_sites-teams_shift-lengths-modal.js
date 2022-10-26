(function () {
  "use strict";

  var rules = angular.module('emlogis.rules');

  rules.controller('RulesNewShiftLengthsModalCtrl',
    ['$scope', '$timeout', '$modalInstance', 'rulesShiftLTService', 'lengths', 'siteId',
      function ($scope, $timeout, $modalInstance, rulesShiftLTService, lengths, siteId) {

        //--------------------------------------------------------------------
        // Defaults for New Shift Lengths Modal
        //--------------------------------------------------------------------

        var i,
            lengthsToSelectInit = [],
            nsl = this;

        nsl.lengths = lengths;
        nsl.siteId = siteId;

        nsl.lengthsEndings = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];
        nsl.lengthEngingSelected = 0;
        nsl.lengthsToSelect = [];
        nsl.newLengthsToAdd = [];

        nsl.isEditing = false;



        nsl.createListOfLengthsToSelect = function(){
          lengthsToSelectInit = [];
          for (i = 0; i <= 24; i++) {
            var minutes = i * 60 + nsl.lengthEngingSelected;
            var lengthExists = checkIfLengthExists(minutes);

            var lengthObj = {
              lengthInMin: minutes,
              exists: lengthExists,
              disabled: lengthExists
            };
            lengthsToSelectInit.push(lengthObj);
          }
          nsl.lengthsToSelect = angular.copy(lengthsToSelectInit);
        };


        var checkIfLengthExists = function(minutes){
          var found = _.find(nsl.lengths, { 'lengthInMin': minutes});
          return found !== undefined;
        };


        nsl.createListOfLengthsToSelect();



        //--------------------------------------------------------------------
        // CRUD
        //--------------------------------------------------------------------


        nsl.updateEditing = function(length){
          nsl.isEditing = !angular.equals(lengthsToSelectInit, nsl.lengthsToSelect);
          nsl.newLengthsToAdd.push(length);
        };



        // Add New Shift

        nsl.addNewShiftLengths = function(){
          var uniqueList = _.uniq(nsl.newLengthsToAdd, function(length, key, a) {
            return length.lengthInMin;
          });

          var multiAdd = { shiftLengthDtos: [] };

          angular.forEach(uniqueList, function(lengthToAdd){
            var dto = {
              lengthInMin: lengthToAdd.lengthInMin,
              active: true
            };
            multiAdd.shiftLengthDtos.push(dto);
          });

          rulesShiftLTService.addNewShiftLengths(nsl.siteId, multiAdd).then( function(lengths){
            if (lengths) {
              nsl.lengths = lengths;
              nsl.closeModal();

            } else {
              var oldLength = rulesShiftLTService.getShiftsLengthsInit();
              nsl.lengths = oldLength;
              nsl.closeModal();
            }
          });

        };


        //--------------------------------------------------------------------
        // Modal methods
        //--------------------------------------------------------------------


        nsl.closeModal = function () {
          $modalInstance.close(nsl.lengths);
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