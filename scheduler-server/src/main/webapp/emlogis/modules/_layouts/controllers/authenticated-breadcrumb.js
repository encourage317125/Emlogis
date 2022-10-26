var app = angular.module('emlogis');

// This Controller is authenticated global breadcrumb

app.controller('AuthenticatedBreadcrumbCtrl', ['$scope', '$rootScope',
    '$http', 'crudDataService', 'applicationContext',
    function ($scope, $rootScope, $http,
                 crudDataService, applicationContext) {

        //console.log("Authenticated Breadcrumb Controller");

        /**
         * Specify moduleName from applicationContext
         * And applicationContext will be updated later in individual model
         */
       //Specify moduleName from applicationContext

        $scope.module = applicationContext.getModule();

        $scope.hasNavBreadcrumb = function(){
          // Checking module.href for `home` avoids breadcrumbs duplication on Dashboard TODO review Dashboard breadcrumbs
          return !(($scope.module.name == 'Browser') || ($scope.module.href.indexOf('home') > 0));
        };


      // If Site-level Msg is visible
      $rootScope.$watch('sseEventVisible', function(newVal){
        $scope.siteMsgIsVisible = newVal;
      },true);

    }
]);

