/**
 * @author Andrii Mozharovskyi
 */
/*
 You are able to use any variable or function from this file in any event handler or JavaScript expression inside report.
 It is like global variables in BIRT terms.
 */

/*Global object to store Emlogis helpers*/
var emlogis = {};

//GLOBAL Default values
emlogis.def = (function(){
    return {
        reportName: "Set current report name to 'emlogisReportName' global variable in report initialize script",
        utcOffset: 0, //TODO: move to "time"
        time: {
            minutesInDay: 1440,
            invalidTimeString: "Invalid Time"
        },
        params: {
            schedule : {
                name: "Schedule"
            },
            site: {
                name: "Site"
            },
            teams: {
                name: "Teams"
            }
        },
        employment: {
            fullTime: {
                name: "Full Time",
                alias: "FullTime",
                shortName: "FT"
            },
            partTime: {
                name: "Part Time",
                alias: "PartTime",
                shortName: "PT"
            }
        },
        schedule: {
            status: {
                posted: 2
            }
        },
        shift: {
            changeType: {
                NOCHANGE: 0,
                WIP: 1,
                SWAP: 2,
                DROP: 3,
                ASSIGN: 4,
                EDIT: 5, 	// = update/modification
                CREATE: 6
            }
        },
        avail: {
            type: {
                AVAILABLE: {
                    key: "AVAIL",
                    text: "Available"
                },
                DAY_OFF: {
                    key: "DAY_OFF",
                    text: "Not Available"
                },
                UNKNOWN: {
                    key: "UNKNOWN",
                    text: "Unknown avail type"
                }
            }
        },
        pref: {
            type: {
                AVOID_DAY: {
                    key: "AVOID_DAY",
                    text: "Avoid Day"
                },
                PREFER_DAY: {
                    key: "PREFER_DAY",
                    text: "Prefer Day"
                },
                AVOID_TIMEFRAME: {
                    key: "AVOID_TIMEFRAME",
                    text: "Avoid"
                },
                PREFER_TIMEFRAME: {
                    key: "PREFER_TIMEFRAME",
                    text: "Prefer"
                },
                UNKNOWN: {
                    key: "UNKNOWN",
                    text: "Unknown pref type"
                }
            }
        }
    }
}());

/**
 * Use this module to work with current report specific data.
 */
emlogis.current = (function() {

    var selectedSite = (function(){
        var object = null;

        var parseObject = function() {
            object = emlogis.helper.dataStringAsObject(params[emlogis.def.params.site.name].value);
        };

        var getObject = function() {
            if(!object) {
                parseObject();
            }
            return object;
        };

        return {
            getObject: function() {
                return getObject();
            }
        }
    }());

    var selectedTeams = (function(){
        var ids = null,
            names = null,
            objects = null;

        var getDataStrings = function() {
            return params[emlogis.def.params.teams.name].value;
        };

        var parseObjects = function() {
            var teams = getDataStrings();
            var objects = [];
            for (var i = 0; i < teams.length; i++) {
                objects.push(emlogis.helper.dataStringAsObject(teams[i]+""));
            }
            return objects;
        };

        var getObjects = function() {
            if(objects) { return objects; }
            objects = parseObjects(); //save to reuse
            return objects;
        };

        return {
            getObjects: function() {
                return getObjects();
            },
            getObjById: function(id) {
                var objects = getObjects();
                for (var i = 0; i < objects.length; i++) {
                    var obj = objects[i];
                    if(obj.id === id) {
                        return obj;
                    }
                }
                return null;
            },
            /**
             * @see description for selectedSite.getValueFromData()
             */
            getValueFromData: function(key) {
                var teams = getDataStrings();
                var teamValuesByKey = [];
                for (var i = 0; i < teams.length; i++) {
                    teamValuesByKey.push(emlogis.helper.getFromDataString(teams[i]+"", key));
                }
                return teamValuesByKey;
            }
        }
    }());

    var selectedSchedule = (function() {
        var object = null;

        var parseObject = function() {
            object = emlogis.helper.dataStringAsObject(params[emlogis.def.params.schedule.name].value);
        };

        var getObject = function() {
            if(!object) {
                parseObject();
            }
            return object;
        };

        return {
            getObject: function() {
                return getObject();
            }
        }
    }());

    return {
        /* 
         * You should change value of this variable (report name) before report creation phase.
         * For example report "initialize" script.
         * It will be displayed in report header.
         */
        reportName: "Default report name (set it in report 'initialize' script)",
        site: {
            get: function(fieldName) {
                return selectedSite.getObject()[fieldName];
            },
            getObject: function() {
                return selectedSite.getObject();
            }
        },
        teams: {
            get: function(id) {
                return selectedTeams.getObjById(id);
            },
            getObjects: function() {
                return selectedTeams.getObjects();
            },
            getIds: function() {
                if(!selectedTeams.ids) {
                    selectedTeams.ids = selectedTeams.getValueFromData("id");
                }
                return selectedTeams.ids;
            },
            getNames: function() {
                if(!selectedTeams.names) {
                    selectedTeams.names = selectedTeams.getValueFromData("name");
                }
                return selectedTeams.names;
            }
        },
        schedule: {
            get: function(fieldName) {
                return selectedSchedule.getObject()[fieldName];
            },
            getObject: function() {
                return selectedSchedule.getObject();
            }
        },
        daysInterval: []	//For reports where data displayed for days/weeks/... (example: 4 weeks availability summary)
    }
}());

/* 
 * Use this function to convert date or long value to Site's time zone.
 */
function toSiteTime(date){
    return emlogis.date.toUTCTime(date, emlogis.current.site.get("timeZoneOffset"));
}

/* 
 * Use this function to convert date or long value before sending to server
 */
function toServerTime(date){
    var offset = new Date().getTimezoneOffset() * 60 * 1000 * -1 * 2;
    return emlogis.date.toUTCTime(date, offset);
}

/*Use this module's methods to work with JavaScript dates */
emlogis.date = (function () {

    //Local constants
    var C = {
        dayNames: {
            0: "Sunday",
            1: "Monday",
            2: "Tuesday",
            3: "Wednesday",
            4: "Thursday",
            5: "Friday",
            6: "Saturday"
        },
        weekStartDay: {
            "SUNDAY": 0,
            "MONDAY": 1,
            "TUESDAY": 2,
            "WEDNESDAY": 3,
            "THURSDAY": 4,
            "FRIDAY": 5,
            "SATURDAY": 6
        },
        invalidDateMessage: "Invalid Date",
        defaultDateSeparator: "/",
        defaultTimeSeparator: ":",
        defaultWeekStartDay: "SUNDAY",
        daysOfWeekNumber: 7
    };

    /**
     * Returns an array where each element represents day number in C.dayNames
     * or an empty array if weekStartDay is invalid (not in C.weekStartDay)
     * For example if weekStartDay == "MONDAY" function returns [1, 2, 3, 4, 5, 6, 0]
     */
    var getDaysOfWeekNumbers = function (weekStartDay) {
        var weekStartDayNumber = C.weekStartDay[weekStartDay];
        if(typeof weekStartDayNumber === "undefined") {
            return [];
        }
        var week = [];

        var counter = 0;
        var i = weekStartDayNumber;
        while (counter < C.daysOfWeekNumber) {
            if (i >= C.daysOfWeekNumber) {
                i = 0;
            }
            week.push(i);
            counter++;
            i++;
        }

        return week;
    };

    /**
     * Returns an object where each element represents day name in C.dayNames
     * or an empty object if weekStartDay is invalid (not in C.weekStartDay)
     * For example if weekStartDay == "MONDAY" function returns
     * Object {0: "Monday", 1: "Tuesday", 2: "Wednesday", 3: "Thursday", 4: "Friday", 5: "Saturday", 6: "Sunday"}
     */
    var getDaysOfWeek = function (weekStartDay) {
        var daysOfWeekNumbers = getDaysOfWeekNumbers(weekStartDay);
        var week = {};
        for (var i = 0; i < daysOfWeekNumbers.length; i++) {
            week[i] = C.dayNames[daysOfWeekNumbers[i]];
        }
        return week;
    };

    //Public interface
    return {
        /**
         * @param date - Date object to process
         * @param weekStartDay - Optional! possible values "MONDAY", ..., "SUNDAY", default @see C.defaultWeekStartDay
         * @returns {Array} - array of 7 dayOfWeek objects, depends on "weekStartDay".
         */
        getWeek: function (date, weekStartDay) {
            if (!date || !(date instanceof Date)) {
                return C.invalidDateMessage;
            }
            weekStartDay = weekStartDay ? weekStartDay : C.defaultWeekStartDay;

            var week = [];
            var weekStartDate = emlogis.date.getFirstDayOfWeek(new Date(date), weekStartDay);
            for (var i = 0; i < 7; i++) {
                var currentDateTime = new Date(weekStartDate);
                currentDateTime.setDate(currentDateTime.getDate() + i);

                var currentDayOfWeekNumber = emlogis.date.getDay(currentDateTime, weekStartDay);
                var dayName = emlogis.date.getDayOfWeekName(currentDayOfWeekNumber + 1, weekStartDay);
                var dayOfWeek = {
                    name: dayName,
                    number: currentDayOfWeekNumber,
                    id: dayName.toUpperCase(),
                    dayOfMonth: currentDateTime.getDate(),
                    date: currentDateTime,
                    dayStartDateTime: emlogis.date.startOfDay(currentDateTime),
                    dayEndDateTime: emlogis.date.endOfDay(currentDateTime)
                };
                week.push(dayOfWeek);
            }
            return week;
        },
        getReportIntervalDays: function(startDate, weeksAmount, weekStartDay) {
            if(weeksAmount <= 0) {
                return "Invalid weeks amount"
            }
            var days = [];
            for(var i = 0; i < weeksAmount; i++) {
                var week = emlogis.date.getWeek(startDate, emlogis.current.site.get("firstDayOfWeek"));
                if(week === C.invalidDateMessage) {
                    return week;
                }
                days = emlogis.helper.pushArray(days, week);
                startDate = new Date(week[6].date.getTime());
                startDate.setDate(startDate.getDate() + 1);
            }
            return days;
        },
        /**
         * @param date - Date object to process
         * @param weekStartDay - Optional! possible values "MONDAY", ..., "SUNDAY", default @see C.defaultWeekStartDay
         * @returns {number} - number of day of week depending on weekStartDay.
         */
        getDay: function (date, weekStartDay) {
            if (!date || !(date instanceof Date)) {
                return -1;
            }

            weekStartDay = weekStartDay ? weekStartDay : C.defaultWeekStartDay;

            return getDaysOfWeekNumbers(weekStartDay).indexOf(date.getDay());
        },
        /**
         * @param date - Date object to process
         * @param weekStartDay - Optional! possible values "MONDAY", ..., "SUNDAY", default @see C.defaultWeekStartDay
         * @returns {Date} - copy of "date" parameter with date reset as the first day of the week, depends on "weekType".
         */
        getFirstDayOfWeek: function (date, weekStartDay) {
            if (!date || !(date instanceof Date)) {
                return C.invalidDateMessage;
            }

            weekStartDay = weekStartDay ? weekStartDay : C.defaultWeekStartDay;

            var firstDayOfWeekDate = date.getDate() - emlogis.date.getDay(date, weekStartDay);
            return new Date(new Date(date).setDate(firstDayOfWeekDate));
        },
        /**
         * @param dayOfWeekNumber - number from 1 to 7
         * @param weekStartDay - Optional! possible values "MONDAY", ..., "SUNDAY", default @see C.defaultWeekStartDay
         * @returns {String, undefined}
         */
        getDayOfWeekName: function (dayOfWeekNumber, weekStartDay) {
            if (!dayOfWeekNumber && dayOfWeekNumber !== 0) {
                return undefined;
            }

            var week = weekStartDay ? getDaysOfWeek(weekStartDay) : getDaysOfWeek(C.defaultWeekStartDay);

            return week[dayOfWeekNumber - 1];
        },
        /**
         * @param date - Date object to process
         * @returns {Date} - copy of "date" parameter with time reset as start of the day.
         */
        startOfDay: function (date) {
            if (!date || !(date instanceof Date)) {
                return date;
            }
            var startOfDay = new Date(date);
            startOfDay.setHours(0);
            startOfDay.setMinutes(0);
            startOfDay.setSeconds(0);
            return startOfDay;
        },
        /**
         * @param date - Date object to process
         * @returns {Date} - copy of "date" parameter with time reset as the end of the day.
         */
        endOfDay: function (date) {
            if (!date || !(date instanceof Date)) {
                return date;
            }
            var endOfDay = new Date(date);
            endOfDay.setHours(23);
            endOfDay.setMinutes(59);
            endOfDay.setSeconds(59);
            return endOfDay;
        },
        /**
         * @param date - possible representation of Date object in JavaScript (Date, long, String)
         * @returns {Date, String} - Date - if param "date" is valid Date representation, else - String.
         */
        validate: function (_date) {
            var date = null;
            if (!_date) {
                return C.invalidDateMessage;
            } else if (!(_date instanceof Date)) {
                date = new Date(_date);
                if (date == C.invalidDateMessage) {
                    date = Date.parse(_date);
                    if (isNaN(date)) {
                        return C.invalidDateMessage;
                    }
                }
            } else {
                date = new Date(_date);
            }
            return date;
        },
        /**
         * Calculate time with given UTC offset
         *
         * @param _date - Date object (or long value) to process
         * @param offsetMillis - Optional! default 0! needed offset in milliseconds
         * @returns {Date} - copy of "date" parameter with local time.
         */
        toUTCTime: function(_date, offsetMillis) {
            if (!(_date instanceof Date)) {
                _date = emlogis.helper.toNumber(_date);
            }
            var date = new Date(_date);
            if(date === C.invalidDateMessage){
                return date;
            }
            offsetMillis = emlogis.helper.toNumber(offsetMillis);
            if(isNaN(offsetMillis)) {
                throw new Error("emlogis.date.toUTCTime(): Invalid time zone offset value: " + offsetMillis);
            }
            // get UTC time in msec
            var utc = date.getTime() + (date.getTimezoneOffset() * 60000);
            // create new Date object using supplied offset
            return new Date(utc + offsetMillis);
        },
        /**
         * Format Date as date String using pattern "mm{separator}dd{separator}yyyy"
         *
         * @param date
         * @param separator Optional!
         * @returns {string}
         */
        toDateString: function (date, separator) {
            if (!date || !(date instanceof Date)) {
                return C.invalidDateMessage;
            }
            separator = separator ? separator : C.defaultDateSeparator;

            return emlogis.date.toString( date, "mm" + separator + "dd" + separator + "yyyy");
        },
        /**
         * Format Date as date String using pattern "mm{dateSeparator}dd{dateSeparator}yyyy hh{timeSeparator}MM{timeSeparator}ss"
         *
         * @param date
         * @param dateSeparator Optional! Nullable!
         * @param timeSeparator Optional!
         * @returns {string}
         */
        toDateTimeString: function (date, dateSeparator, timeSeparator) {
            if (!date || !(date instanceof Date)) {
                return C.invalidDateMessage;
            }
            dateSeparator = dateSeparator ? dateSeparator : C.defaultDateSeparator;
            timeSeparator = timeSeparator ? timeSeparator : C.defaultTimeSeparator;

            return emlogis.date.toString(
                date,
                "mm" + dateSeparator + "dd" + dateSeparator + "yyyy"
                + " " + "HH" + timeSeparator + "MM" + timeSeparator + "ss"
            );
        },
        /**
         * Format Date as time String using pattern "h[:mm]a/p"
         *
         * @param date
         * @returns {string}
         */
        toShort12TimeString: function (date) {
            if (!date || !(date instanceof Date)) {
                return C.invalidDateMessage;
            }
            return emlogis.date.timeObjectToShort12TimeString({hours: date.getHours(), minutes: date.getMinutes()});
        },
        timeObjectToShort12TimeString: function(timeObject) {
            if(!timeObject) {
                return null;
            }
            var hours = timeObject.hours;
            var minutes = timeObject.minutes;
            var ampm = hours >= 12 ? 'p' : 'a';
            hours = hours % 12;
            hours = hours ? hours : 12; // the hour '0' should be '12'
            minutes = minutes ? ":" + emlogis.helper.fillZeroesToSize(minutes) : "";
            return hours + minutes + ampm;
        },
        /**
         * @see description in "emlogis.dateFormat" module
         * @returns {string}
         */
        toString: function (date, mask) {
            return emlogis.dateFormat.format(date, mask);
        },
        /**
         * Purpose: if we have long value in string type (like: "1439565073236")
         * it cannot be converted to date within "new Date()",
         * so using this method we can be a bit safer
         *
         * @param value - any type of data
         * @returns {Date} - if value cannot be parsed returns "Invalid Date"
         */
        parse: function(value) {
            var date = new Date(value);
            if(date == C.invalidDateMessage) {
                var longVal = emlogis.helper.toNumber(value);
                date = new Date(longVal);
            }
            return date;
        },
        /**
         * Converts milliseconds to time object
         *
         * @param timeInMillis - milliseconds as String or Number
         * @returns {Object}
         */
        millisToTime: function(timeInMillis) {
            timeInMillis = emlogis.helper.toNumber(timeInMillis);
            if(isNaN(timeInMillis)) {
                return null;
            }
            var hrs = Math.floor(timeInMillis/3600000);
            var restTime = timeInMillis % 3600000;
            var mins = Math.floor(restTime/60000);
            restTime = restTime % 60000;
            var secs = Math.floor(restTime/1000);
            return {
                millis: timeInMillis,
                hours: hrs,
                minutes: mins,
                seconds: secs
            }
        },
        /**
         * Compares dates by Year, Month, and day of Month
         *
         * @param d1
         * @param d2
         * @returns {boolean}
         */
        isEqualDates: function(d1, d2) {
            if(d1 === d2
                || d1 && d2
                && d1.getFullYear() === d2.getFullYear()
                && d1.getMonth() === d2.getMonth()
                && d1.getDate() === d2.getDate()) {
                return true;
            }
            return false;
        }
    }
}());

/*Use this module's methods to get access to common helpers functions*/
emlogis.helper = (function () {

    //Local constants
    var C = {
        defaultDataStringDelimiter: ";",
        defaultDataStringKeyValueAccordanceSymbol: "@"
    };

    //Public interface
    return {
        /**
         * Builds a string of tokens like "key=value" separated by delimiters
         *
         * @param object - JavaScript native object
         * @param delimiter - Optional! string which will separate tokens
         * @returns {String}
         */
        buildDataString: function(object, delimiter) {
            if (!object || typeof object !== "object") return "";

            var _delimiter = delimiter ? delimiter : C.defaultDataStringDelimiter;
            var string = "";

            for(key in object) {
                string += key + C.defaultDataStringKeyValueAccordanceSymbol + object[key];
                string += _delimiter;
            }

            //Remove last delimiter, if any key/value pair
            if(string) {
                string = string.slice(0, string.lastIndexOf(_delimiter));
            }

            return string;
        },
        /**
         * Parses data string to find the key parameter
         * and returns corresponding value
         * or null if the key is not found
         *
         * @param dataString
         * @param key
         * @param delimiter - Optional! @see buildDataString function description
         * @returns {String, null}
         */
        getFromDataString: function(dataString, key, delimiter) {
            if(!dataString || typeof dataString !== "string") {
                throw "emlogis.helper.getFromDataString(): Invalid argument dataString: " + dataString;
            };

            var _delimiter = delimiter ? delimiter : C.defaultDataStringDelimiter;

            var tokens = dataString.split(_delimiter);

            for (var i = 0; i < tokens.length; i++) {
                var token = tokens[i];
                var tokenKey = "";
                var tokenValue = "";
                if(token) {
                    var tokenKeyValue = token.split(C.defaultDataStringKeyValueAccordanceSymbol);
                    tokenKey = tokenKeyValue[0];
                    tokenValue = tokenKeyValue[1];
                }

                if(tokenKey == key) {
                    return tokenValue;
                }
            }

            return null;
        },
        /**
         * Parses data string and returns it as object
         *
         * @param dataString
         * @param delimiter - Optional! @see buildDataString function description
         * @returns {Object, null}
         */
        dataStringAsObject: function(dataString, delimiter) {
            if(!dataString || typeof dataString !== "string") {
                throw "emlogis.helper.dataStringAsObject(): Invalid argument dataString: " + dataString;
            };

            var _delimiter = delimiter ? delimiter : C.defaultDataStringDelimiter;

            var tokens = dataString.split(_delimiter);
            var object = {};
            for (var i = 0; i < tokens.length; i++) {
                var token = tokens[i];
                var tokenKey = "";
                var tokenValue = "";
                if(token) {
                    var tokenKeyValue = token.split(C.defaultDataStringKeyValueAccordanceSymbol);
                    tokenKey = tokenKeyValue[0];
                    tokenValue = tokenKeyValue[1];
                }
                object[tokenKey] = tokenValue;
            }

            return object;
        },
        /**
         * @param string - string to append "0"
         * @param size - Optional! Default 2! size of expected result string,
         * @returns {String}
         */
        fillZeroesToSize: function (string, size) {
            if (string == null || typeof string === "undefined") {
                return string;
            }
            size = size ? size : 2;
            string = string + "";

            for (var i = string.length; i < size; i++) {
                string = "0" + string;
            }
            return string;
        },
        /**
         * Returns same as standard Object.keySet(object) (Except key "null"). It is not possible to use Object within BIRT JavaScript.
         *
         * @param object
         * @returns {Array}
         */
        keySet: function (object) {
            if (!object) return [];

            var keys = [];
            for (var id in object) {
                if (id != "null") keys.push(id);
            }
            return keys;
        },
        /**
         * @param array
         * @param value
         * @param equalType - Optional! Comparison type (true - '===', false/empty - '==')
         * @returns {Array}
         */
        contains: function(array, value, equalType){
            if(!array){
                return false;
            }
            equalType = equalType ? equalType : false;
            if(equalType) {
                for(var i = array.length;i--;){
                    if(array[i] === value){ return true; }
                }
            } else {
                for(var i = array.length;i--;){
                    if(array[i] == value){ return true; }
                }
            }
            return false;
        },
        /**
         * Removes anything that isn't a digit, decimal point, or minus sign (-) from the string
         * and tries to cast it to number
         *
         * @param string
         * @returns {number, NaN} - NaN if the string cannot be parsed or if it is not a string
         */
        toNumber: function(string) {
            if(typeof string === "number") {
                return string;
            }
            if(typeof string !== "string") {
                return NaN;
            }
            string = string.replace(/[^\d\.\-eE+]/g, "");
            return string.length == 0 ? NaN : string * 1;
        },
        /**
         * Adds all elements from "arrayToPush" to "targetArray"
         *
         * @param targetArray
         * @param arrayToPush
         * @returns {Array, string} - string if exception or wrong data received
         */
        pushArray: function(targetArray, arrayToPush) {
            if(!(targetArray instanceof Array) || !(arrayToPush instanceof Array)) {
                return "Invalid arguments received";
            }

            for(var i = 0; i < arrayToPush.length; i++){
                targetArray.push(arrayToPush[i]);
            }

            return targetArray;
        },
        /**
         * Converts "employmentType" to predefined in "emlogis.def.employment"
         *
         * @param employmentType
         * @returns {string} - string if exception or wrong data received
         */
        convertEmploymentType: function(employmentType){
            switch(employmentType) {
                case emlogis.def.employment.fullTime.alias:
                    return emlogis.def.employment.fullTime.shortName;
                case emlogis.def.employment.partTime.alias:
                    return emlogis.def.employment.partTime.shortName;
                default:
                    return employmentType;
            }
        }
    }
}());

emlogis.dateFormat = (function () {
    /*
     * Date Format 1.2.3
     * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
     * MIT license
     *
     * Includes enhancements by Scott Trenda <scott.trenda.net>
     * and Kris Kowal <cixar.com/~kris.kowal/>
     *
     * Accepts a date, a mask, or a date and a mask.
     * Returns a formatted version of the given date.
     * The date defaults to the current date/time.
     * The mask defaults to dateFormat.masks.default.
     */

    var dateFormat = function () {
        var token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
            timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
            timezoneClip = /[^-+\dA-Z]/g,
            pad = function (val, len) {
                val = String(val);
                len = len || 2;
                while (val.length < len) val = "0" + val;
                return val;
            };

        // Regexes and supporting functions are cached through closure
        return function (date, mask, utc) {
            var dF = dateFormat;

            // Passing date through Date applies Date.parse, if necessary
            date = date ? new Date(date) : new Date;
            if (isNaN(date)) throw SyntaxError("Invalid date");

            mask = String(dF.masks[mask] || mask || dF.masks["default"]);

            // Allow setting the utc argument via the mask
            if (mask.slice(0, 4) == "UTC:") {
                mask = mask.slice(4);
                utc = true;
            }

            var _ = utc ? "getUTC" : "get",
                d = date[_ + "Date"](),
                D = date[_ + "Day"](),
                m = date[_ + "Month"](),
                y = date[_ + "FullYear"](),
                H = date[_ + "Hours"](),
                M = date[_ + "Minutes"](),
                s = date[_ + "Seconds"](),
                L = date[_ + "Milliseconds"](),
                o = utc ? 0 : date.getTimezoneOffset(),
                flags = {
                    d: d,
                    dd: pad(d),
                    ddd: dF.i18n.dayNames[D],
                    dddd: dF.i18n.dayNames[D + 7],
                    m: m + 1,
                    mm: pad(m + 1),
                    mmm: dF.i18n.monthNames[m],
                    mmmm: dF.i18n.monthNames[m + 12],
                    yy: String(y).slice(2),
                    yyyy: y,
                    h: H % 12 || 12,
                    hh: pad(H % 12 || 12),
                    H: H,
                    HH: pad(H),
                    M: M,
                    MM: pad(M),
                    s: s,
                    ss: pad(s),
                    l: pad(L, 3),
                    L: pad(L > 99 ? Math.round(L / 10) : L),
                    t: H < 12 ? "a" : "p",
                    tt: H < 12 ? "am" : "pm",
                    T: H < 12 ? "A" : "P",
                    TT: H < 12 ? "AM" : "PM",
                    Z: utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
                    o: (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
                    S: ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
                };

            return mask.replace(token, function ($0) {
                return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
            });
        };
    }();

    // Some common format strings
    dateFormat.masks = {
        "default": "ddd mmm dd yyyy HH:MM:ss",
        shortDate: "m/d/yy",
        mediumDate: "mmm d, yyyy",
        longDate: "mmmm d, yyyy",
        fullDate: "dddd, mmmm d, yyyy",
        shortTime: "h:MM TT",
        mediumTime: "h:MM:ss TT",
        longTime: "h:MM:ss TT Z",
        isoDate: "yyyy-mm-dd",
        isoTime: "HH:MM:ss",
        isoDateTime: "yyyy-mm-dd'T'HH:MM:ss",
        isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
    };

    // Internationalization strings
    dateFormat.i18n = {
        dayNames: [
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        ],
        monthNames: [
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
            "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
        ]
    };

    return {
        format: function (date, mask) {
            return dateFormat(date, mask);
        }
    }
}());