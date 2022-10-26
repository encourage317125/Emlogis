angular.module('emlogis.admin').controller('CustomersMapViewCtrl', ['$scope', 'applicationContext', 'uiGmapGoogleMapApi',
  function($scope, applicationContext, uiGmapGoogleMapApi) {
    uiGmapGoogleMapApi.then(function(maps) {
      $scope.consts.addresses = [
        {
          address: 'New York City',
          country: 'United States'
        }, {
          address: 'Mexico City',
          country: 'Mexico'
        }, {
          address: 'Beijing',
          country: 'China'
        }, {
          address: 'Tokyo',
          country: 'Japan'
        }, {
          address: 'Sofia',
          country: 'Bulgaria'
        }, {
          address: 'Toronto',
          country: 'Canada'
        }, {
          address: 'New Delhi',
          country: 'India'
        }, {
          address: 'Los Angeles',
          country: 'United States'
        }
      ];
      $scope.map = { center: { latitude: 45, longitude: -73 }, zoom: 8 };
      $scope.customerList = [];
      $scope.getCustomerList('').then(function(response) {
        $scope.customerList = response.data;
        angular.forEach($scope.customerList, function(customer) {

        });
      }, function(err) {
        applicationContext.setNotificationMsgWithValues(err.data.message, 'danger', true);
      });
    });
  }
]);