var app = angular.module('emlogis.directives');

var GENERAL = 'GENERAL';
var SPECIFIC = 'SPECIFIC';
var EXISTING = 'EXISTING';

var hourDifference = (new Date().getTimezoneOffset()) / 60;
var minDifference = (Math.abs(new Date().getTimezoneOffset())) % 60;

var timeDifference = null;

if (hourDifference > 0) {
    timeDifference = '-';
}
else {
    timeDifference = '+';
}

if (hourDifference.toString().length < 2 )
    hourDifference = '0' + hourDifference.toString();

if (minDifference.toString().length < 2 )
    minDifference = '0' + minDifference.toString();

timeDifference += hourDifference + minDifference;

app.directive('shiftPatternList',[ '$rootScope', '$modal', 'crudDataService',
  'applicationContext',
  'patternList',
  'appFunc',
  function($rootScope, $modal, crudDataService, applicationContext, patternList, appFunc) {
  return {
    restrict: 'AEC',
    scope: {
      site: '=',
      team: '=',
      skill: '=',
      save: '&'
    },
    templateUrl: 'util/directive/templates/shift-pattern-list.tpl.html',
    link:function ( $scope, $element, $attrs, $controller ){

      var factory = _.clone(crudDataService);

      console.log('Shift Pattern List Directive');

      $scope.patternCalendar = patternList.getPatternCalendar();

      /**
       * Link variables with $scope variable
       */

      var defaultPatternName = 'New Shift';

      $scope.patternCalendar.maxDayPatternCount = 0;

      $scope.getBlankPatterns = function(patterns){
        return new Array($scope.patternCalendar.maxDayPatternCount - patterns.length);
      };

      /**
       * add one Shift Pattern for Day
       * @param shiftPatternDay
       * @param newPattern : null (on the fly), object: from backend
       */
      $scope.addShiftPatternForDay = function(shiftPatternDay, newPattern) {


        if (!newPattern) {

          newPattern = {
            'name' : defaultPatternName,
            'shifts': [],
            'hours': [],
            'shiftLengths': [],
            'shiftIds': [],
            'dayType': 'GENERAL',
            'cdDate': null,
            'shiftPatternCdDate': null,
            'shiftPatternType': 'Set'
          };
//
//
//          patternCalendar.option.patternType = pattern.shiftPatternType;
//          patternCalendar.option.cdDate = pattern.shiftPatternCdDate;
//          if (pattern.shiftPatternCdDate === null) {
//            patternCalendar.option.dayType = 'GENERAL';
//          }
//          else {
//            patternCalendar.option.dayType = 'SPECIFIC';
//          }

          /**
           * set it true so that it will not loaded from backend when use selects on the graph
           * @type {boolean}
           */
          newPattern.loaded = true;
          patternList.initializeDefaultDemands(newPattern);


            /**
             * if current shift pattern is editing
             */
          if ($scope.patternCalendar.option.editing) {

            appFunc.getSaveWorkDlg().then(function(reason){

              var working = applicationContext.getWorking();

              if (reason === DISCARD || reason === SKIP) {

                working.restoreFunc();

                /**
                 * Clear Selected pattern to false
                 */
                clearPatternsSelection($scope.skill.generalPatterns);
                clearPatternsSelection($scope.skill.specificPatterns);

                /**
                 * It is adding new shift pattern so editing should be true
                 */
                if (working.option !==null)
                  working.option.editing = true;
                patternList.processAddShiftPatternForDay(shiftPatternDay,newPattern);

                /**
                 * Set new pattern as selected
                 */

                patternList.processSetCurrentShiftPattern(newPattern, shiftPatternDay);

                //uncheck demandshiftlength
                patternList.uncheckAllDemandShiftLengths();

              }
              else if (reason === SAVE) {

//                if (($scope.patternCalendar.option.dayType === SPECIFIC) && (!$scope.patternCalendar.option.cdDate)) {
//                  applicationContext.setNotificationMsgWithValues('schedule_builder.PLEASE_SPECIFY_SPECIFIC_DATE', '', true, '');
//                  return;
//                }
//
                return working.saveFunc()
                  .then( function(result) {
                    patternList.processAddShiftPatternForDay(patternList.getShiftPatternDay(shiftPatternDay.day, EXISTING),newPattern);
                    patternList.processSetCurrentShiftPattern(newPattern, shiftPatternDay);
                    $scope.patternCalendar.option.editing = true;

                    patternList.uncheckAllDemandShiftLengths();

                    console.log('Add new shift after update/save');

                  },function (reason){
                    /**
                     * We have error and should roll back
                     */
                    working.restoreFunc();

                    /**
                     * Clear Selected pattern to false
                     */
                    clearPatternsSelection($scope.skill.generalPatterns);
                    clearPatternsSelection($scope.skill.specificPatterns);

                    patternList.processAddShiftPatternForDay(patternList.getShiftPatternDay(shiftPatternDay.day, EXISTING),newPattern);
                    patternList.processSetCurrentShiftPattern(newPattern, shiftPatternDay);
                    $scope.patternCalendar.option.editing = true;
                  }

                );
              }

            }, function(reason) {
                console.log('dismissed');
            });
          }
          else {
            patternList.uncheckAllDemandShiftLengths();
            patternList.processAddShiftPatternForDay(shiftPatternDay,newPattern);
            /**
             * Set new pattern as selected
             */

            patternList.processSetCurrentShiftPattern(newPattern, shiftPatternDay);

          }

          /**
           * If it is add new pattern on the web, mark editing as true
           */
          $scope.patternCalendar.option.editing = true;
        }
        else {
          $scope.uncheckAllDemandShiftLengths();
          patternList.processAddShiftPatternForDay(shiftPatternDay, newPattern);
          /**
           * Set new pattern as selected
           */

          patternList.processSetCurrentShiftPattern(newPattern, shiftPatternDay);



        }

      };

      /**
       * Set Current Shift Pattern, Loads shifts as well
       * Sometimes it is necessary to watch currentShiftPattern variable
       * @param pattern
       * @param shiftPatternDay
       */
      $scope.setCurrentShiftPattern = function (pattern, shiftPatternDay) {

        if ($scope.patternCalendar.currentShiftPattern) {

          /**
           * if same shift pattern clicked, return
           */
          if ($scope.patternCalendar.currentShiftPattern.id === pattern.id) {
            return;
          }

          /**
           * Previous demand ShiftLength is not to switch the current pattern without save/update
           */
          if ($scope.patternCalendar.option.editing ===true) {

            appFunc.getSaveWorkDlg().then(function(reason) {

              var working = applicationContext.getWorking();

              if (reason === DISCARD || reason === SKIP) {

                if (working.option !==null)
                  working.option.editing = false;

                working.restoreFunc();
                $scope.patternCalendar.currentShiftPattern.selected = false;
                patternList.processSetCurrentShiftPattern(pattern,shiftPatternDay);

              }
              else if (reason === SAVE) {
                $scope.patternCalendar.option.editing = false;
                return working.saveFunc()
                  .then( function(result) {

                    var tmpShiftPatternDay = patternList.getShiftPatternDay(shiftPatternDay.day, EXISTING);
                    var tmpShiftPattern = patternList.getShiftPattern(tmpShiftPatternDay,pattern.id);
                    patternList.processSetCurrentShiftPattern(tmpShiftPattern,tmpShiftPatternDay);

                  },function (reason){

                    $scope.patternCalendar.option.editing = true;
                    // do not switch
                  }

                );

              }

            }, function(reason) {
              console.log('dismissed');
            });

            return;
          }

          else {
            $scope.patternCalendar.currentShiftPattern.selected = false;

            if ($scope.patternCalendar.currentShiftPattern.id !== pattern.id) {
              if ($scope.patternCalendar.option.editing !== false) {
                $scope.patternCalendar.option.editing = false;
              }
            }
          }


          //Set Current Shift Pattern, PatternDay

        }

        patternList.processSetCurrentShiftPattern(pattern,shiftPatternDay);


      };


      /**
       * This function returns based on the weekday and day time on the page
       * @param shiftPatternDay
       * return true : can select current shift pattern or add
       */
      $scope.canClickLink = function(shiftPatternDay) {
        if (($scope.patternCalendar.option.dayType === 'GENERAL') && (shiftPatternDay.day !== 'SPECIFIC_DATE')) {
          return true;
        }
        else if (($scope.patternCalendar.option.dayType === 'SPECIFIC') && (shiftPatternDay.day === 'SPECIFIC_DATE')) {
          return true;
        }
        return false;
      };

      $scope.uncheckAllDemandShiftLengths = function() {
        
        
      };

      /**
       * Clear Selected Variable in the Shift patterns
       */
      function clearPatternsSelection(patterns) {
        _.each(patterns, function(pattern) {
          if (pattern.selected === true) {
            pattern.selected = false;
          }
        });
      }


      // Init ShiftPatterDay Collection: Shift Pattern List Directive with Starting from Sunday
      patternList.initShiftPatternDayCollection(0);

    }
  };
}]);

/**
 * This is the date pattern issue when we use angular 1.3
 */

app.directive('datepickerPopup', function (){
  return {
    restrict: 'EAC',
    require: 'ngModel',
    link: function(scope, element, attr, controller) {
      //remove the default formatter from the input directive to prevent conflict
      controller.$formatters.shift();
    }
  };
});