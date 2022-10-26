angular.module('emlogis.commonservices')
  .service('UtilsService', ['$http', 'applicationContext',
    function($http, applicationContext) {
      var baseUrl = applicationContext.getBaseRestUrl();

      function sendRequest(urlPart, method, requestPayload) {
        var apiUrl = baseUrl + urlPart;
        var req = {
          method: method,
          url: apiUrl
        };
        if (method === 'POST' || method === 'PUT') {
          req.data = requestPayload;
        }

        return $http(req);
      }

      this.getAvailableTimeZones = function() {
        var urlPart = 'sites/timezones';

        return sendRequest(urlPart, 'GET', null);
      };

      this.checkEmpty = function(variable) {
        if (typeof variable === 'undefined' || variable === null || variable === '') {
          return true;
        } else {
          return false;
        }
      };
      
      this.isObjEmpty = function(obj) {
        // null and undefined are "empty"
        if (obj === null) return true;

        // Assume if it has a length property with a non-zero value
        // that that property is correct.
        if (obj.length > 0)    return false;
        if (obj.length === 0)  return true;

        // Otherwise, does it have any properties of its own?
        // Note that this doesn't handle
        // toString and valueOf enumeration bugs in IE < 9
        for (var key in obj) {
          if (hasOwnProperty.call(obj, key)) return false;
        }
        return true;
      };
    }
  ]);