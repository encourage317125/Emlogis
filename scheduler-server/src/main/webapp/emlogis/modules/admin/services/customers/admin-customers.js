angular.module('emlogis.admin')
  .service('AdminCustomersService', ['$http', 'applicationContext', 'UtilsService',
    function($http, applicationContext, UtilsService) {
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

      this.getCustomerList = function(filterTxt, offset, limit, orderBy, orderDir) {
        var urlPart = '';
        var queryPart = [];

        urlPart = 'serviceproviders/emlogisservice/orgs/ops/query';
        if (!UtilsService.checkEmpty(filterTxt)) {
          queryPart.push('search=' + filterTxt + '&searchfields=name');
        }
        if (!UtilsService.checkEmpty(offset)) {
          queryPart.push('offset=' + offset);
        }
        if (!UtilsService.checkEmpty(limit)) {
          queryPart.push('limit=' + limit);
        }
        if (!UtilsService.checkEmpty(orderBy)) {
          queryPart.push('orderby=' + orderBy);
        }
        if (!UtilsService.checkEmpty(orderDir)) {
          queryPart.push('orderdir=' + orderDir);
        }

        if (queryPart.length > 0) {
          urlPart += '?' + queryPart.join('&');
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.getCustomerDetails = function(tenantId) {
        var urlPart = 'serviceproviders/emlogisservice/orgs/' + tenantId;

        return sendRequest(urlPart, 'GET', null);
      };

      this.createCustomer = function(customerDetails) {
        var urlPart = 'serviceproviders/emlogisservice/orgs/';

        return sendRequest(urlPart, 'POST', customerDetails);
      };

      this.updateCustomer = function(tenantId, customerDetails) {
        var urlPart = 'serviceproviders/emlogisservice/orgs/' + tenantId;

        return sendRequest(urlPart, 'PUT', customerDetails);
      };

      this.deleteCustomer = function(tenantId) {
        var urlPart = 'serviceproviders/emlogisservice/orgs/' + tenantId;

        return sendRequest(urlPart, 'DELETE', null);
      };
    }
  ]);