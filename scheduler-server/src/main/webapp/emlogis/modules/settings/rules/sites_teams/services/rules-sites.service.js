(function() {
  "use strict";

  // Service
  // Create service function

  var rulesSitesService = function($http, $q, applicationContext, dataService){

    var siteForm,
        baseUrl = applicationContext.getBaseRestUrl(),
        factory = _.clone(dataService);


    // Set Site Form

    this.setSiteForm = function(form){
      siteForm = form;
    };

    // Get Site Form

    this.getSiteForm = function(){
      return siteForm;
    };


    // GET all Sites associated to an Org

    this.getAllSites = function() {
      return factory.getElements('sites', { orderby:'name', orderdir:'ASC', offset: 1, limit: -1});
    };


    
    // GET Sites+Teams tree
    
    this.getSitesTeamsTree = function(){
      return dataService.getSitesTeamsTree({});
    };



    // GET a full Site DTO

    this.getSiteDetails = function(siteId) {
      return $http.get( baseUrl + 'sites/' + siteId )
        .then( function (response) {
          return response;
        });
    };



    // Update a Site

    this.updateSite = function(siteId, siteObj) {
      return factory.updateElement('sites', siteId, siteObj);
    };



    // Create a New Site

    this.saveNewSite = function(newSiteObj) {
      return factory.createElement('sites', newSiteObj);
    };

  };


  // Inject dependencies and
  // add service to the Rules module

  rulesSitesService.$inject = ['$http', '$q', 'applicationContext', 'dataService'];
  angular.module('emlogis.rules').service('rulesSitesService', rulesSitesService);

})();