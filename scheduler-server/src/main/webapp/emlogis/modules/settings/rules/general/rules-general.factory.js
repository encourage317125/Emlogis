(function () {
  "use strict";
  //console.log('+++ fire Rules factory');

  var rulesGeneralFactory = function ($http, $q, applicationContext) {
    //console.log('creating rulesGeneralFactory...');

    var baseUrl = applicationContext.getBaseRestUrl();

    return {
      getSchedulingSettings: function () {
        return $http.get(baseUrl + 'org/schedulingsettings')
          .then(function (response) {
            return response;
          });
      },

      putSchedulingSettings: function (obj) {
        return $http.put(baseUrl + 'org/schedulingsettings')
          .then(function (response) {
            return response;
          })
        ;
      }
    };
  };

  rulesGeneralFactory.$inject = ['$http', '$q', 'applicationContext'];
  angular.module('emlogis.rules').factory('rulesGeneralFactory', rulesGeneralFactory);


})();