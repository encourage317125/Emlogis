angular.module('emlogis.reports')
  .service('ReportsiHubService', ['$http', 'authService',
    'applicationContext',
    function ($http, authService, applicationContext) {

      var ihubUrl = "";

      this.setIhubUrl = function (host, port) {
        ihubUrl = "http://" + host + ":" + port + "/iportal";
      };

      this.getIhubUrl = function () {
        return ihubUrl;
      };

      var defaultFolderId;

      var baseUrl = applicationContext.getBaseRestUrl();

      function sendiHubRequest(urlPart, method, requestPayload) {

        var apiUrl = baseUrl + "ihub/" + method.toLowerCase();
        var req = {
          method: method,
          url: apiUrl,
          params: {
            authid: authService.getihubAuthId(),
            urlpart: urlPart
          }
        };
        if (method === 'GET') {
          req.params.params = requestPayload;
        } else {
          req.data = requestPayload;
        }
        return $http(req);
      }

      this.getAuthId = function () {
        return authService.getihubAuthId();
      };

      this.getFolderId = function () {
        sendiHubRequest('folders/', 'GET').then(function (res) {
          if (angular.isDefined(res.data.ItemList)) {
            var list = res.data.ItemList;
            if (angular.isDefined(list.File)) {
              angular.forEach(list.File, function (item) {
                if (item.Name === 'Reports' && item.FileType === 'Directory') {
                  defaultFolderId = item.Id;
                }
              });
            }
          }

        });
      };


      this.items = function () {
        return sendiHubRequest('folders/' + defaultFolderId + '/items', 'GET');
      };

      this.parameters = function (visualId) {
        return sendiHubRequest('visuals/' + visualId + '/parameters', 'GET');
      };

      this.picklist = function (visualId, paramName, cascadingGroupName, precedingParamValues) {
        var url = 'visuals/' + visualId + '/parameters/picklist';
        var payload = {
          paramName: paramName
        };
        if (cascadingGroupName) {
          payload.cascadingGroupName = cascadingGroupName;
        }
        if (precedingParamValues) {
          payload.precedingParamValues = JSON.stringify({ParameterValue: precedingParamValues});
        }
        return sendiHubRequest(url, 'GET', payload);
      };

      this.execute = function (visualId, paramValues) {
        return sendiHubRequest('visuals/' + visualId + '/execute', 'POST',
          $.param({
            paramValues: JSON.stringify({"ParameterValue": paramValues})
          }));
      };

    }
  ]);
