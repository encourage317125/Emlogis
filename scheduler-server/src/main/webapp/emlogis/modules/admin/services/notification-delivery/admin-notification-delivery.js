angular.module('emlogis.admin')
  .service('AdminNotificationDeliveryService', ['$http', 'applicationContext',
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

      this.getNotificationDeliveryProviders = function() {
        var urlPart = 'msgdeliveryproviders';

        return sendRequest(urlPart, 'GET', null);
      };

      this.getEmailProviders = function() {
        var urlPart = 'msgdeliveryproviders?filter=deliveryType=1';

        return sendRequest(urlPart, 'GET', null);
      };

      this.getSmsProviders = function() {
        var urlPart = 'msgdeliveryproviders?filter=deliveryType=0';

        return sendRequest(urlPart, 'GET', null);
      };

      this.getProviderDetails = function(providerId) {
        var urlPart = 'msgdeliveryproviders/' + providerId;

        return sendRequest(urlPart, 'GET', null);
      };

      this.createProvider = function(providerDetails) {
        var urlPart = 'msgdeliveryproviders';

        return sendRequest(urlPart, 'POST', providerDetails);
      };

      this.updateProvider = function(providerId, providerDetails) {
        var urlPart = 'msgdeliveryproviders/' + providerId;

        return sendRequest(urlPart, 'PUT', providerDetails);
      };

      this.deleteProvider = function(providerId) {
        var urlPart = 'msgdeliveryproviders/' + providerId;

        return sendRequest(urlPart, 'DELETE', null);
      };

      this.testProvider = function(providerId) {
        var urlPart = 'msgdeliveryproviders/' + providerId + '/ops/check';

        return sendRequest(urlPart, 'POST', {});
      };
    }
  ]);