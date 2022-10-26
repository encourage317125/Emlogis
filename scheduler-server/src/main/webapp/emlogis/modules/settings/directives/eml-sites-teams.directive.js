(function () {
  "use strict";

  //
  // This directive is build upon
  // Bootstrap component Label (.label)
  //
  // all Tags must have "id" and "name" properties
  // for the current implementation of this directive to work

  var emlSitesTeams = function ($filter) {

    return {
      restrict: 'EA',
      replace: true,
      scope: {
        acl: '=acl',// array of entities to display
        show: '&'   // Show the dialog box of site/teams
      },
      templateUrl: 'modules/settings/partials/include/eml-sites-teams.include.html',
      link: function(scope, element, attrs){
        // console.log('+++ Inside eml-objects directive...');                    // DEV mode

        //scope.allTags = scope.tagslist.concat(scope.addtagslist);          // Combine all Tags into one array

        var deleteDuplicatesFromArray = function(arr) {
          var cleaned = [];
          arr.forEach(function(itm) {
            var unique = true;
            cleaned.forEach(function(itm2) {
              if ( _.isEqual(itm, itm2) ) unique = false;
            });
            if (unique) cleaned.push(itm);
          });
          return cleaned;
        };

        //scope.allTags = deleteDuplicatesFromArray(scope.allTags);          // Delete duplicates from array of Tags
        //scope.allTags = $filter('orderBy')(scope.allTags, 'name');         // Rearrange the list ABC order by name prop

        scope.$watch('tagslist', function(newValue) {
          if ( newValue )
            // console.log("~~~ I see a data change in Tags component!");  // DEV mode
            // console.log(scope.addtagslist);                             // DEV mode

            scope.tagslist = $filter('orderBy')(scope.tagslist, 'name');   // Display Tags in ABC order by name prop

            //
            // Check if Tags to display array (tagslist) is the same
            // as array of all potential Tags to add (addtagslist)

            if ( angular.equals(scope.tagslist, scope.addtagslist) ) {     // If arrays are equal, meaning all Tags are displayed
              scope.noMoreTagsToAdd = true;                                // hide "plus" button

            } else {                                                       // If arrays differ,
              scope.dropDownList = [];                                     // create an empty array for Tags to be added, and

              //
              // Filter out already displayed Tags
              // from the array of all Tags that can potentially be added

              scope.dropDownList = scope.allTags.filter(function(newTag){
                return scope.tagslist.filter(function(displayedTag){
                    return displayedTag.id == newTag.id;
                  }).length === 0;
              });

              scope.noMoreTagsToAdd = scope.dropDownList < 1;              // Hide "plus" btn, if dropDown list is empty
            }
          }, true)
        ;
      }
    };
  };
  //
  //
  //tags.$inject = ['$filter'];
  angular.module('emlogis.settings').directive('emlSitesTeams', emlSitesTeams);

}());