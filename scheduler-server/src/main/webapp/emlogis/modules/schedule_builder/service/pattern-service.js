(function () {

  //console.log('In Shift Pattern Graph Service.');

  var defaultMaxDemandCount = 5;

  angular.module('emlogis.schedule_builder')

  .service('patternGraph', function () {

    var graph = {
    };

    // const variable
    var MIN_PER_HOUR = 60;
    var MIN_PER_SCALE = 30;



    return {
      initializeGridTimes: function() {

        // Define Shift Graph Time Scale
        graph.gridTimes = [
          {
            label: "12A",
            colspan: 2
          },
          {
            label: "1A",
            colspan: 2
          },
          {
            label: "2A",
            colspan: 2
          },
          {
            label: "3A",
            colspan: 2
          },
          {
            label: "4A",
            colspan: 2
          },
          {
            label: "5A",
            colspan: 2
          },
          {
            label: "6A",
            colspan: 2
          },
          {
            label: "7A",
            colspan: 2
          },
          {
            label: "8A",
            colspan: 2
          },
          {
            label: "9A",
            colspan: 2
          },
          {
            label: "10A",
            colspan: 2
          },
          {
            label: "11A",
            colspan: 2
          },
          {
            label: "12P",
            colspan: 2
          },
          {
            label: "1P",
            colspan: 2
          },
          {
            label: "2P",
            colspan: 2
          },
          {
            label: "3P",
            colspan: 2
          },
          {
            label: "4P",
            colspan: 2
          },
          {
            label: "5P",
            colspan: 2
          },
          {
            label: "6P",
            colspan: 2
          },
          {
            label: "7P",
            colspan: 2
          },
          {
            label: "8P",
            colspan: 2
          },
          {
            label: "9P",
            colspan: 2
          },
          {
            label: "10P",
            colspan: 2
          },
          {
            label: "11P",
            colspan: 2
          },
          {
            label: "12A",
            colspan: 2
          },
          {
            label: "1A",
            colspan: 2
          },
          {
            label: "2A",
            colspan: 2
          },
          {
            label: "3A",
            colspan: 2
          },
          {
            label: "4A",
            colspan: 2
          },
          {
            label: "5A",
            colspan: 2
          },
          {
            label: "6A",
            colspan: 2
          },
          {
            label: "7A",
            colspan: 2
          },
          {
            label: "8A",
            colspan: 2
          },
          {
            label: "9A",
            colspan: 2
          },
          {
            label: "10A",
            colspan: 2
          },
          {
            label: "11A",
            colspan: 2
          },
          {
            label: "12P",
            colspan: 2
          },
          {
            label: "1P",
            colspan: 2
          },
          {
            label: "2P",
            colspan: 2
          },
          {
            label: "3P",
            colspan: 2
          },
          {
            label: "4P",
            colspan: 2
          },
          {
            label: "5P",
            colspan: 2
          },
          {
            label: "6P",
            colspan: 2
          },
          {
            label: "7P",
            colspan: 2
          },
          {
            label: "8P",
            colspan: 2
          },
          {
            label: "9P",
            colspan: 2
          },
          {
            label: "10P",
            colspan: 2
          },
          {
            label: "11P",
            colspan: 2
          }
        ];

      },
      // Initialize Required HeaderItems
      initializeRequiredHeaderItems: function() {
        graph.requiredHeaderItems = [];
      },
      initializeExcessHeaderItems: function() {
        graph.excessHeaderItems = [];
      },
      //Empty Grid Items, Header Items with all 0
      emptyGridTimes: function() {

        angular.forEach(graph.gridTimes, function(gridTime, key) {

          /*
           Required Header Items
           */
          graph.requiredHeaderItems.push(
            {
              label: gridTime.label + "1", //separate half an hour, mostly it will not used
              value: 0
            }
          );

          graph.requiredHeaderItems.push(
            {
              label: gridTime.label + "2", //separate half an hour, mostly it will not used
              value: 0
            }
          );

          /*
           Excess Header Items
           */
          graph.excessHeaderItems.push(
            {
              label: gridTime.label + "1", //separate half an hour, mostly it will not used
              value: 0
            }
          );

          graph.excessHeaderItems.push(
            {
              label: gridTime.label + "2", //separate half an hour, mostly it will not used
              value: 0
            }
          );
        });

      },

      /**
       * Build the Bar-Style Graph of Shifts : Manual & Demand
       * @param currentShiftPattern
       */
      buildGraph: function(currentShiftPattern){

        /**
         * Empty requiredHeaderItems and excessheaderitems
         */
        angular.forEach(graph.requiredHeaderItems, function(item) {
          item.value = 0;
        });

        angular.forEach(graph.excessHeaderItems, function(item) {
          item.value = 0;
        });

        var idx = 0;
        graph.requiredItems = {'totalRow': 0}; //initialize totalRow of requiredItems
        graph.excessItems = {'totalRow': 0}; //initialize totalRow of excessItems

        // Calculate Items

        graph.renderedRequiredShifts = [];
        graph.renderedExcessShifts = [];

        var _this = this;

        angular.forEach(currentShiftPattern.shifts, function(shift, key) {

          /**
           * Same Shift is pointing at same shift type
           * Assign each shift with copy of shift
           */

          if (!shift || shift.hasOwnProperty('id') === false) {
            //alert('Please select all of your shift types'); // Localization should be applied
            return;
          }

          var requiredShift = angular.copy(shift);
          var excessShift = angular.copy(shift);

          // add required,excess count
          requiredShift.required = currentShiftPattern.hours[idx].required;
          requiredShift.excess = currentShiftPattern.hours[idx].excess;
          excessShift.required = currentShiftPattern.hours[idx].required;
          excessShift.excess = currentShiftPattern.hours[idx].excess;

          requiredShift.x = requiredShift.start * 2; // get cell index, x
          excessShift.x = excessShift.start * 2; // get cell index, x

          /*
           Save Index to Shift it will be used in shift modal update
           */
          requiredShift.index = idx;
          excessShift.index = idx;

          var requiredNum = parseInt(currentShiftPattern.hours[idx].required) || 0;
          var excessNum = parseInt(currentShiftPattern.hours[idx].excess) || 0;

          for (var i = 0; i < shift.shiftLengthLength / MIN_PER_HOUR * 2; i++) {
            graph.requiredHeaderItems[requiredShift.x + i].value += requiredNum;
            graph.excessHeaderItems[excessShift.x + i].value += excessNum;
          }

          //calculate y for required

          var isShiftAssigned = false;
          if (requiredShift.required > 0 ) {

            for(i = 0; i < graph.requiredItems.totalRow; i++) { //i : row
              if (_this.checkShiftOverlap(graph.renderedRequiredShifts, requiredShift, i) === false ){
                requiredShift.y = i;

                graph.renderedRequiredShifts.push(requiredShift);
                isShiftAssigned = true;
                break;
              }
            }

            if (isShiftAssigned === false){

              /**
               * All rows are full, increase row and assign
               */
              requiredShift.y = graph.requiredItems.totalRow;
              graph.renderedRequiredShifts.push(requiredShift);
              graph.requiredItems.totalRow++;
            }
          }

          // calculate for excess
          isShiftAssigned = false;
          if (excessShift.excess > 0 ) {

            for(i = 0; i < graph.excessItems.totalRow; i++) { //i : row
              if (_this.checkShiftOverlap(graph.renderedExcessShifts, excessShift, i) === false ){
                excessShift.y = i;

                graph.renderedExcessShifts.push(excessShift);
                isShiftAssigned = true;
                break;
              }
            }

            if (isShiftAssigned === false){

              /**
               * All rows are full, increase row and assign
               */
              excessShift.y = graph.excessItems.totalRow;
              graph.renderedExcessShifts.push(excessShift);
              graph.excessItems.totalRow++;
            }
          }

          idx++;
        });

        var row = [];
        var renderedShift = null;
        var j;

        // Build Required Items Cell
        graph.requiredItems.data = [];
        for (i = 0; i < graph.requiredItems.totalRow; i++) {
          /**
           *  Iterate all columns
           */
          row = [];

          for (j = 0; j < 48 / MIN_PER_SCALE * MIN_PER_HOUR; j++) {

            // Check whether it is occupied with any shift or not
            renderedShift = _this.getOverlapShift(graph.renderedRequiredShifts,j,i);
            if ( renderedShift === null) {
              row.push({'shiftLengthLength': MIN_PER_SCALE, 'x': j }); //30 mins cell
            }
            else {
              renderedShift.class = "selected";
              row.push(renderedShift);
              j += renderedShift.shiftLengthLength / MIN_PER_HOUR * 2 -1; // increased column index
            }
          }
          graph.requiredItems.data.push(row);

        }

        // Build Excess Items Cell
        graph.excessItems.data = [];
        for (i = 0; i < graph.excessItems.totalRow; i++) {
          /**
           *  Iterate all columns
           */
          row = [];

          for (j = 0; j < 48 / MIN_PER_SCALE * MIN_PER_HOUR; j++){

            // Check whether it is occupied with any shift or not
            renderedShift = _this.getOverlapShift(graph.renderedExcessShifts, j,i);
            if ( renderedShift === null) {
              row.push({"shiftLengthLength": MIN_PER_SCALE, 'x': j}); //30 mins cell
            }
            else {
              renderedShift.class = "selected";
              row.push(renderedShift);
              j += renderedShift.shiftLengthLength / MIN_PER_HOUR * 2 -1; // increased column index
            }
          }
          graph.excessItems.data.push(row);

        }

        graph.optimumCols = 24;
        var ele = null;
        /**
         * Calculate optimumCols
         */
        for (var i in graph.renderedRequiredShifts) {
          ele = graph.renderedRequiredShifts[i];
          if (ele.x + ele.shiftLengthLength / MIN_PER_HOUR * 2 > graph.optimumCols * 2) {
            graph.optimumCols += 12;
          }
        }

        for (i in graph.renderedRequiredShifts) {
          ele = graph.renderedRequiredShifts[i];
          if (ele.x + ele.shiftLengthLength / MIN_PER_HOUR * 2 > graph.optimumCols * 2) {
            graph.optimumCols += 12;
          }
        }
      },
      /**
       *
       * @param x
       * @param y
       */
      getOverlapShift: function(shiftCollection, x, y) {

        // Use native javascript for - loop because it contains break & continue
        for (var i=0; i < shiftCollection.length; i++) {
          var ele = shiftCollection[i];

          if (ele.y != y ){
            continue;
          }
          // check the overlap between rendered shift and new shift
          if  ((ele.x <= x) && (x < ele.x + ele.shiftLengthLength / MIN_PER_HOUR * 2)){
            return ele;
          }

        }

        return null;

      },
      /**
       * Check Overlap for shift
       * @param shiftCollection
       * @param shift
       * @param y
       */
      checkShiftOverlap: function(shiftCollection, shift, y) {

        // Use native javascript for - loop because it contains break & continue
        for (var i=0; i < shiftCollection.length; i++) {
          var ele = shiftCollection[i];

          if (ele.y != y ){
            continue;
          }
          // check the overlap between rendered shift and new shift
          /**
           * Shift.shiftLengthLength are 1 hour so should multiply by 2
           */
          if  ((ele.x <= shift.x) && (shift.x < ele.x + ele.shiftLengthLength / MIN_PER_HOUR * 2)){
            return true;
          }

          if  ((ele.x < shift.x + shift.shiftLengthLength / MIN_PER_HOUR *2 ) && (shift.x + shift.shiftLengthLength / MIN_PER_HOUR * 2 < ele.x + ele.shiftLengthLength / MIN_PER_HOUR * 2)){
            return true;
          }

          /**
           * check vice versa
           */
          if  ((shift.x <= ele.x) && (ele.x < shift.x + shift.shiftLengthLength / MIN_PER_HOUR * 2)){
            return true;
          }

          if  ((shift.x < ele.x + ele.shiftLengthLength / MIN_PER_HOUR *2 ) && (ele.x + ele.shiftLengthLength / MIN_PER_HOUR * 2 < shift.x + shift.shiftLengthLength / MIN_PER_HOUR * 2)){
            return true;

          }
        }

        return false;

      },
      getGraph: function() {
        return graph;
      },
      setGraph: function(value) {
        graph = value;
      }
    };
  })


  .service('patternList', ['$timeout', 'applicationContext', 'crudDataService','patternGraph',
    function ($timeout, applicationContext,crudDataService,patternGraph) {


      var factory = _.clone(crudDataService);
      var patternCalendar = {
        originShiftPattern: {},
        currentShiftPattern: {},
        demandShiftLengths: null
      };
      var graph = patternGraph.getGraph();

      var defaultPatternName = 'New Shift';
      var _this = this;

      return {

        /**
         * Init shift pattern day collection
         * if specificDateOnly is true, only clear last patternDay
         */

        initShiftPatternDayCollection: function(firstDayIntOfWeek) {

          var daysOfWeek =[
            {
              "day": "SUNDAY",
              "display": "calendar.SUNDAY",
              "patterns": []
            },
            {
              "day": "MONDAY",
              "display": "calendar.MONDAY",
              "patterns": []
            },
            {
              "day": "TUESDAY",
              "display": "calendar.TUESDAY",
              "patterns": []
            },
            {
              "day": "WEDNESDAY",
              "display": "calendar.WEDNESDAY",
              "patterns": []
            },
            {
              "day": "THURSDAY",
              "display": "calendar.THURSDAY",
              "patterns": []
            },
            {
              "day": "FRIDAY",
              "display": "calendar.FRIDAY",
              "patterns": []
            },
            {
              "day": "SATURDAY",
              "display": "calendar.SATURDAY",
              "patterns": []
            }
          ];

          /**
           * Empty the patterns only, keep the shiftpatternday object as a reference
           */

          if ((patternCalendar.shiftPatternDayCollection || null) === null) {
            patternCalendar.shiftPatternDayCollection = [];
          }
          else {
            patternCalendar.shiftPatternDayCollection.length = 0;
          }
          for (var i = 0; i < 7; i++) {
            patternCalendar.shiftPatternDayCollection.push(daysOfWeek[(i + firstDayIntOfWeek) % 7]);
          }

          //else {
          //  angular.forEach(patternCalendar.shiftPatternDayCollection, function(shiftPatternDay,key) {
          //    shiftPatternDay.patterns = [];
          //  });
          //}


          patternCalendar.maxDayPatternCount = 0;

        },
        /**
         *
         * @param column: Monday-Sunday (for General), date (for Specific), existing shiftPatternDay object (for existing)
         * @param dayType
         * @returns {*}
         */
        getShiftPatternDay: function(column, dayType, firstDayIntOfWeek) {
          /**
           * in case shift pattern is not assigned it will be assigned to Monday
           */
          var i = null;
          if (dayType === GENERAL ) {
            for (i in patternCalendar.shiftPatternDayCollection) {
              if (patternCalendar.shiftPatternDayCollection[i].day == column){
                return patternCalendar.shiftPatternDayCollection[i];
              }
            }
          }
          else if (dayType === SPECIFIC) {

            /**
             * day contains specific date now
             */
            var dayOfWeek = (new Date(column)).getDay();
            return patternCalendar.shiftPatternDayCollection[(dayOfWeek - firstDayIntOfWeek + 7) % 7 ];
          }
          else {
            /**
             * day represents previous existing shiftpatternday
             */
            for (i in patternCalendar.shiftPatternDayCollection) {
              if (patternCalendar.shiftPatternDayCollection[i].day == column) {
                return patternCalendar.shiftPatternDayCollection[i];
              }
            }
          }


          return null;
        },

        /**
         * This is internal function after user pass validation for array
         * @param shiftPatternDay
         * @param newPattern
         */
        processAddShiftPatternForDay: function(shiftPatternDay, newPattern) {

          /**
           * Insert the pattern into ShiftPatternDay in a sorted manner
           */

          for (var i=0; i<shiftPatternDay.patterns.length; i++) {
            if (shiftPatternDay.patterns[i].name > newPattern.name) {
              break;
            }
          }

          shiftPatternDay.patterns.splice(i,0,newPattern);

          this.updateMaxDayPatternCount();
        },

        // Update Max Pattern Count per Day
        updateMaxDayPatternCount: function() {

          patternCalendar.maxDayPatternCount = 0;

          angular.forEach(patternCalendar.shiftPatternDayCollection, function(shiftPatternDay, key) {

            if (shiftPatternDay.patterns.length > patternCalendar.maxDayPatternCount){
              patternCalendar.maxDayPatternCount = shiftPatternDay.patterns.length;
            }

          });

        },
        /**
         * get ShiftType based on id from shiftType collection
         * @param shiftTypeIdprocessAddShiftPatternForDay
         * @returns {shiftType}
         */
        getShiftType: function(shiftTypeId) {

          var shiftTypes = patternCalendar.option.shiftTypes;
          for (var i in shiftTypes) {
            var ele = shiftTypes[i];
            if (ele.id === shiftTypeId) {
              return ele;
            }
          }

          return null;

        },
        getShiftLength: function(shiftLengthId) {

          var shiftLengths = patternCalendar.option.manualShiftLengths;
          for (var i in shiftLengths) {
            var ele = shiftLengths[i];
            if (ele.id === shiftLengthId) {
              return ele;
            }
          }

          return null;

        },

        /**
        * Listens for changing current pattern
        */
        selectShiftPattern: function(pattern) {

          if (pattern.shiftPatternDayOfWeek) {
            this.processSetCurrentShiftPattern(pattern, this.getShiftPatternDay(pattern.shiftPatternDayOfWeek));
          }
          else {

            this.processSetCurrentShiftPattern(pattern,patternCalendar.currentShiftPatternDay);
          }

        },

        // Listens for shift pattern add
        addShiftPattern: function(pattern) {

          this.processAddShiftPatternForDay(this.getShiftPatternDay(pattern.shiftPatternDayOfWeek), pattern);

        },


        /**
         * getShiftPattern Obj from loaded shift patterns;
         * mostly shiftPattern object references are refreshed after reloading
         * @param shiftPatternDay
         * @param id
         */
        getShiftPattern: function(shiftPatternDay,id) {
          for (var i in shiftPatternDay.patterns) {
            var ele = shiftPatternDay.patterns[i];
            if (ele.id === id ) {
              return ele;
            }
          }
          return null;
        },

        /**
         * getShiftPattern Obj from loaded shift patternCollection;
         * mostly shiftPattern object references are refreshed after reloading
         * @param shiftPatternDay
         * @param id
         */
        getShiftPatternById: function(id) {
          for (var i in patternCalendar.shiftPatternDayCollection) {
            var shiftPatternDay = patternCalendar.shiftPatternDayCollection[i];

            for (var j in shiftPatternDay.patterns) {
              var ele = shiftPatternDay.patterns[j];
              if (ele.id === id ) {
                return ele;
              }
            }

          }
          return null;
        },

        /**
         * Set Current Shift Pattern, Loads shifts as well
         * Sometimes it is necessary to watch currentShiftPattern variable
         * @param pattern
         * @param shiftPatternDay
        */
        processSetCurrentShiftPattern: function(pattern, shiftPatternDay) {

          patternCalendar.currentShiftPattern.selected = false; // make current one not-selected
          pattern.selected = true;
          /**
           * Keep original one
           */
          patternCalendar.currentShiftPatternDay = shiftPatternDay;
          patternCalendar.currentShiftPattern = pattern;
          var originPattern = angular.copy(pattern, patternCalendar.originShiftPattern);


          if (pattern.loaded !== true) {
            /**
             * if it has id, load shift types for shift pattern
             */
            var _this = this;

  //          patternCalendar.currentShiftPattern = pattern;
  //          angular.copy(pattern, patternCalendar.originShiftPattern);

            factory.getElement("shiftpatterns" , pattern.id)
              .then(function(result){

                pattern.loaded = true;
                pattern.shifts = [];
                pattern.shiftIds = [];
                pattern.hours = [];
                pattern.shiftLengths = [];
                pattern.shiftReqDtos = result.shiftReqDtos;

                /**
                 * Max Rows in schedule demand creation, the dots graph
                 */
                //patternCalendar.currentShiftPattern.maxDemandCount = defaultMaxDemandCount;
                //patternCalendar.option.demandCount = patternCalendar.currentShiftPattern.maxDemandCount; //default demand Count
                //_this.clearDemands(patternCalendar.currentShiftPattern, patternCalendar.currentShiftPattern.maxDemandCount);
                /**
                 * This is used for manual shift pattern
                 */
                angular.forEach(result.shiftReqDtos, function(shiftReqDto, key) {
                  /*
                   Set shift type id
                   */
                  //var shift = getShiftType(shiftReqDto.shiftTypeId);
                  pattern.shifts.push(angular.copy(_this.getShiftType(shiftReqDto.shiftTypeId)));
                  pattern.shiftIds.push(shiftReqDto.shiftTypeId);
                  pattern.hours.push({'required': shiftReqDto.employeeCount, 'excess': shiftReqDto.excessCount});
                  pattern.shiftLengths.push(_this.getShiftLength(shiftReqDto.shiftLengthId));

                });

                angular.copy(pattern.shifts, originPattern.shifts);
                angular.copy(pattern.shiftIds, originPattern.shiftIds);
                angular.copy(pattern.hours, originPattern.hours);
                angular.copy(pattern.shiftLengths, originPattern.shiftLengths);

                /**
                 * below is used for demand based pattern
                 */

                pattern.shiftLengthList = result.shiftLengthList;
                pattern.shiftDemandDtos = result.shiftDemandDtos;
                pattern.shiftDemands = null;

                pattern.type = result.type;

                /**
                 * Update Option Values
                 */
                patternCalendar.option.patternType = pattern.shiftPatternType;
                patternCalendar.option.cdDate = pattern.shiftPatternCdDate;
                if (pattern.shiftPatternCdDate === null) {
                  patternCalendar.option.dayType = 'GENERAL';
                }
                else {
                  patternCalendar.option.dayType = 'SPECIFIC';
                }

                if (patternCalendar.option.patternType === 'Demand') {
                  if (pattern.shifts.length > 0) {
                    patternCalendar.option.demandPatternGenerated = true; //graph is already generated
                  }

                }
                _this.generateShiftPattern(pattern);
                //                    angular.copy(pattern.shiftLengthList, $scope.originShiftPattern.shiftLengthList);
                //                      angular.forEach(result.shiftDemandDtos, function(demandDto, key) {
                //                        /*
                //                         Set shift type id
                //                         */
                //                        //var shift = getShiftType(shiftReqDto.shiftTypeId);
                //                        pattern.shifts.push(getShiftType(shiftReqDto.shiftTypeId));
                //                        pattern.hours.push({'required': shiftReqDto.employeeCount, 'excess': shiftReqDto.excessCount});
                //
                //                        angular.copy(pattern.shifts, $scope.originShiftPattern.shifts);
                //                        angular.copy(pattern.hours, $scope.originShiftPattern.hours);
                //
                //                      });
  //              patternCalendar.currentShiftPattern = pattern;
  //              angular.copy(pattern, patternCalendar.originShiftPattern);

  //              var shiftPatternDay = null;
  //              if (pattern.shiftPatternCdDate !==null) {
  //                shiftPatternDay = _this.getShiftPatternDay(pattern.shiftPatternCdDate, SPECIFIC);
  //              }
  //              else {
  //                shiftPatternDay = _this.getShiftPatternDay(pattern.shiftPatternDayOfWeek, GENERAL);
  //              }
  //
  //              patternCalendar.currentShiftPatternDay = shiftPatternDay;

              }, function (error) {
                console.log(error);
                applicationContext.setNotificationMsgWithValues(error.data.message, '', true, error.statusText);
              });
          }
          else {

            /**
             * Update Option Values
             */

            patternCalendar.option.patternType = pattern.shiftPatternType;
            patternCalendar.option.cdDate = pattern.shiftPatternCdDate;
            if (pattern.shiftPatternCdDate === null) {
              patternCalendar.option.dayType = 'GENERAL';
            }
            else {
              patternCalendar.option.dayType = 'SPECIFIC';
            }

            this.generateShiftPattern(pattern);
          }

        },

        /**
         * Update the shift pattern grid according to the condition
         */
        processAssignShiftPatterns: function(args) {

          var site = args.site;
          var team = args.team;
          var skill = args.skill;
          var shiftPatterns = [];

          if ((skill || null) !==null) {
            shiftPatterns = skill.generalPatterns.concat(skill.specificPatterns);
          }

          /**
           * Update Main Screen Specific Date
           */
          var cleanOnly = args.cleanOnly;

          /**
           * It will be used selected shift pattern after assign
           */
          var firstShirtPattern = null;

          /**
           * if CurrentPattern is contained in this list, we can select it otherwise select first element
           */
          var isCurrentPatternContained = false;
          var currentShiftPatternId = patternCalendar.currentShiftPattern.id;
          var currentShiftPattern = null;
          var shiftPatternDay = null;

          // Init Shift PatternDay Collection
          if (site === null ) {
            this.initShiftPatternDayCollection(0);
          }
          else {
            this.initShiftPatternDayCollection(site.firstDayIntOfWeek);
          }


          if (cleanOnly !== true ) {
            for (var i in shiftPatterns) {
              var ele = shiftPatterns[i];

  //            ele.shifts = [];
  //            ele.hours = [];
  //            ele.loaded = false;

              /* Conditions to Check */
              if ((site.id != ele.siteId) || (team.id != ele.teamId) || (skill.id != ele.skillId)) {
                continue;
              }

              if (currentShiftPatternId === ele.id ) {
                currentShiftPattern = ele;
                currentShiftPattern.shifts = ele.shifts;
                currentShiftPattern.shiftIds = ele.shiftIds;
                currentShiftPattern.hours = ele.hours;
                isCurrentPatternContained = true;
              }

              if (firstShirtPattern === null ) {
                firstShirtPattern = ele;
              }

              /*
               Check site, team and skill of pattern to match with dropdown list settings
               It will also assign the current shift pattern
               */
              shiftPatternDay = null;

              /**
               * It will only load into grid if pattern belongs to general day
               * Sometimes, for some reason shiftPatternDayOfWeek contains null value
               */


              if (ele.shiftPatternCdDate !==null) {
                shiftPatternDay = this.getShiftPatternDay(ele.shiftPatternCdDate, SPECIFIC, site.firstDayIntOfWeek);
              }
              else {
                shiftPatternDay = this.getShiftPatternDay(ele.shiftPatternDayOfWeek, GENERAL);
              }

              if (shiftPatternDay !== null) {
                this.processAddShiftPatternForDay(shiftPatternDay, ele);
              }

            }

            if (isCurrentPatternContained === true) {

              if (currentShiftPattern.shiftPatternCdDate !==null) {
                shiftPatternDay = this.getShiftPatternDay(currentShiftPattern.shiftPatternCdDate, SPECIFIC);
              }
              else {
                shiftPatternDay = this.getShiftPatternDay(currentShiftPattern.shiftPatternDayOfWeek, GENERAL);
              }

              this.processSetCurrentShiftPattern(currentShiftPattern,shiftPatternDay);
            }
            else if (firstShirtPattern !==null ) {

              if (firstShirtPattern.shiftPatternCdDate !==null) {
                shiftPatternDay = this.getShiftPatternDay(firstShirtPattern.shiftPatternCdDate, SPECIFIC);
              }
              else {
                shiftPatternDay = this.getShiftPatternDay(firstShirtPattern.shiftPatternDayOfWeek, GENERAL);
              }

              this.processSetCurrentShiftPattern(firstShirtPattern,shiftPatternDay);
            }
          }

          // Update Max Day Pattern
          this.updateMaxDayPatternCount();

          // Set first shift pattern as selected
          if (firstShirtPattern === null) {
            this.assignEmptyShiftPattern();
          }

        },

        getPatternCalendar: function() {
          return patternCalendar;
        },
        setPatternCalendar: function(value) {
          patternCalendar = value;
        },
        setDefaultOptions: function() {
          patternCalendar.option.patternType = 'Set';
          patternCalendar.option.dayType = 'GENERAL';
          patternCalendar.option.cdDate = null;
          patternCalendar.option.shiftPatternType = 'Set';
          patternCalendar.option.shiftPatternCdDate = null;
        },
        updateDemandShiftLengths: function (pattern, mouse) {

          if (mouse === true && patternCalendar.option.editing !== true) {
            patternCalendar.option.editing = true;
            pattern.shiftReqDtos = [];
          }

          /**
           * Clear the allowed property for demandHeaderItems
           */
          //angular.forEach($scope.demandHeaderItems, function(demandHeaderItem, key) {
          //  demandHeaderItem.allowed = false;
          //});

          //angular.forEach($scope.patternCalendar.demandShiftLengths, function(demandShiftLength, key){
          //
          //  if (demandShiftLength.checked) {
          //
          //    var shiftType = getShiftTypeFromShiftLength(demandShiftLength.id);
          //
          //    if (shiftType !== null) {
          //      for (var j=0; j< shiftType.shiftLengthLength / demandCellMin; j++ ) {
          //        $scope.demandHeaderItems[j+shiftType.start * MIN_PER_HOUR / demandCellMin ].allowed = true;
          //      }
          //    }
          //  }
          //
          //});

        },

        //clear dots area
        clearDemands: function(pattern, count) {

          pattern.demands = [];
          this.clearDemandHeaderItems(pattern);

          this.updateDemandShiftLengths(pattern); //update demand dto selection

          for (var i = 1; i <= count; i++) {
            var rowData = {
              row: i,
              data: []
            };

            pattern.demands.push(rowData);

            for (var j = 0; j < pattern.demandHeaderItems.length; j++) {
              rowData.data.push(0); //0: nothing, 1: it has demand, 2: unexpected extra
            }
          }
        },

        /**
         * Clear DemandHeaderItems: Dots Area
         */
        clearDemandHeaderItems: function(pattern) {
          pattern.demandHeaderItems = [];

          //Initialize Header Items with all 0
          angular.forEach(graph.gridTimes, function(gridTime, key) {

            /*
             Required Header Items
             */
            pattern.demandHeaderItems.push(
              {
                label: gridTime.label + "1", //separate half an hour, mostly it will not used
                value: '',
                allowed: false
              }
            );

            pattern.demandHeaderItems.push(
              {
                label: gridTime.label + "2", //separate half an hour, mostly it will not used
                value: '',
                allowed: false
              }
            );

          });
        },
        
        //Generate Shift Graph including circle demands and bar graphs
        generateShiftPattern: function() {
          if (patternCalendar.option.patternType === 'Demand') { //if only demand pattern

            if (!patternCalendar.currentShiftPattern.shiftDemandDtos) {
              // The data is not loaded completely
              return;
            }

            var ele = null;
            var j = 0;


            /**
             * Update the Demand Pattern ShiftLengths
             */
            for (var i=0; i < patternCalendar.demandShiftLengths.length; i++) {
              ele = patternCalendar.demandShiftLengths[i];
              ele.checked = false;
              if (patternCalendar.currentShiftPattern.shiftLengthList && patternCalendar.currentShiftPattern.shiftLengthList.indexOf(ele.id) > -1) {
                ele.checked = true;
              }
            }

            /**
             * Update MaxDemandCount
             */
            patternCalendar.currentShiftPattern.maxDemandCount = 0;


            for (i=0; i<patternCalendar.currentShiftPattern.shiftDemandDtos.length; i++) {
              ele = patternCalendar.currentShiftPattern.shiftDemandDtos[i];
              if (patternCalendar.currentShiftPattern.maxDemandCount < ele.employeeCount) {
                patternCalendar.currentShiftPattern.maxDemandCount = ele.employeeCount;
              }
            }

            if (patternCalendar.currentShiftPattern.maxDemandCount < defaultMaxDemandCount) {
              patternCalendar.currentShiftPattern.maxDemandCount = defaultMaxDemandCount;
            }

            this.clearDemands(patternCalendar.currentShiftPattern, patternCalendar.currentShiftPattern.maxDemandCount);
            patternCalendar.option.demandCount = patternCalendar.currentShiftPattern.maxDemandCount;

            /**
             * Apply shiftDemandDtos
             */
            for (i=0; i<patternCalendar.currentShiftPattern.shiftDemandDtos.length; i++) {

              ele = patternCalendar.currentShiftPattern.shiftDemandDtos[i];
              var index = ele.startTime / ele.lengthInMin / 60 / 1000;

              patternCalendar.currentShiftPattern.demandHeaderItems[index].value = ele.employeeCount;

              for (j =0; j < patternCalendar.currentShiftPattern.maxDemandCount; j++) {

                if (j< patternCalendar.currentShiftPattern.maxDemandCount - parseInt(ele.employeeCount)) {
                  patternCalendar.currentShiftPattern.demands[j].data[index] = 0; // empty
                }
                else {
                  patternCalendar.currentShiftPattern.demands[j].data[index] = 1; //show circle
                }

              }

            }

            if (patternCalendar.option.demandPatternGenerated === true) {

              $timeout(function(){
                patternGraph.buildGraph(patternCalendar.currentShiftPattern);

                /**
                 * compare requiredHeaderItems:ShiftReqDtos and demandHeaderItems:DemandHeaderItems
                 * and show dots in demands with yellow color
                 */

                for (i=0; i< graph.requiredHeaderItems.length; i++) {
                  var required = graph.requiredHeaderItems[i];
                  var demand = patternCalendar.currentShiftPattern.demandHeaderItems[i];
                  var diff = 0;

                  if (required.value > (demand.value || 0)) {
                    diff = required.value - (demand.value || 0);
                    /**
                     * Show yellow dots for the difference
                     */
                    var end = patternCalendar.currentShiftPattern.maxDemandCount - (demand.value || 0) ;
                    for (j =end -diff ; j < end; j++) {
                      patternCalendar.currentShiftPattern.demands[j].data[i] = 2; // empty
                    }

                  }
                }

              });
            }



          }
          else { //manual pattern

            $timeout(function(){
              patternGraph.buildGraph(patternCalendar.currentShiftPattern);
            });
          }  
        },

        /**
         * Demand Shift lengths : Check
         */
        checkAllDemandShiftLengths: function() {
          _.each(patternCalendar.demandShiftLengths, function(ele) {
            ele.checked = true;
          });
        },

        /**
         * Demand Shift lengths : Uncheck
         */
        uncheckAllDemandShiftLengths: function() {
          _.each(patternCalendar.demandShiftLengths, function(ele) {
            ele.checked = false;
          });
        },

        /**
         * Setup default demands with demandcount : circleArea
         */
        initializeDefaultDemands: function(pattern) {
          pattern.maxDemandCount = defaultMaxDemandCount;
          patternCalendar.option.demandCount = pattern.maxDemandCount; //default demand Count
          this.clearDemands(pattern, pattern.maxDemandCount);
        },

        /**
         * create and select empty shift pattern
         */
        assignEmptyShiftPattern: function() {
          var emptyPattern = {loaded: true, name: ''};
          patternCalendar.option.shiftPatternType = 'Set';
          patternCalendar.option.shiftPatternCdDate = null;
          var emptyPatternDay = {};


          // To be used for shift patterns generation
          emptyPattern.shifts = []; // Shift value
          emptyPattern.shiftIds = [];
          emptyPattern.hours = []; // Contains required and excess hours
          emptyPattern.shiftLengths = [];

          this.processSetCurrentShiftPattern(emptyPattern, emptyPatternDay);
          this.setDefaultOptions();
        }
      };
    }
  ]);


}());
