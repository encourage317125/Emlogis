(function () {
  "use strict";

  var injectParams = ['$document', 'UtilsService'];

  var validateMessages = function () {

    return {
      restrict: 'AE',
      replace: true,
      scope: {
        messagesFor: '=',
        submitted: '=',
        patternRegExp: '@',
        patternErrorMessage: '@',
        watchValue: '@'
      },
      templateUrl: 'modules/common/partials/validate-messages.include.html',
      link: function(scope, element) {

        var watchValue = scope.watchValue ? scope.watchValue : 'messagesFor.$error';

        //
        // Add 'eml-form-to-validate'
        // for multiple forms on a page

        var ngForm = element.closest('ng-form');
        var ngFormAttr = element.closest('[ng-form]');

        if ( ngForm.length > 0 ) {
          ngForm.addClass('eml-form-to-validate');
        } else if ( ngFormAttr.length > 0 ) {
          ngFormAttr.addClass('eml-form-to-validate');
        } else {
          element.closest('form').addClass('eml-form-to-validate');
        }

        //
        // On submit
        // check every required field in a form whether it's valid or not.

        scope.$watch('submitted', function(submitted) {
          var hasPatternErrorButPatternIsValid = scope.messagesFor.$error.pattern &&
                                                 scope.watchValue &&
                                                 scope.patternRegExp &&
                                                 new RegExp(scope.patternRegExp).test(scope.$eval(watchValue));

          if (submitted && scope.messagesFor.$invalid && !hasPatternErrorButPatternIsValid) {

            // If this field is invalid,
            // add Bootstrap class 'has-error' to the 'form-group' it belongs to.
            // This will trigger CSS styling for notification icons and border color.

            element.parent('.form-group').addClass('has-error');

            // Show error message for only the first invalid field in a form
            // to avoid messy and frustrating user experience

            element.closest('.eml-form-to-validate').find('.validate-message').css( "opacity", "0" ).first().css( "opacity", "1" );
          }
        }, true);


        //
        // When watchValue for this field is being changed
        scope.$watch(watchValue, function(newValue) {
          setTimeout(function(){

            // Check the DOM
            // to show error message for only the first invalid field in a form
            // to avoid messy and frustrating user experience
            element.closest('.eml-form-to-validate').find('.validate-message').css( "opacity", "0" ).first().css( "opacity", "1" );

            // in case you have a pattern and you want to check it by different field then the $modelValue (like in case of Dates)
            // pattern should be checked manually
            if (scope.patternRegExp && scope.watchValue) {
              element.closest('.eml-form-to-validate').find("div[ng-message='pattern'] .validate-message").css( "opacity", "0" );
              element.parents('.form-group').removeClass('has-error');
              if (!new RegExp(scope.patternRegExp).test(newValue)) {
                element.closest('.form-group').find(".validate-message").css( "opacity", "1" );
                element.parents('.form-group').addClass('has-error');
              }
            }

            // And if $error object is empty,
            // meaning that there is no more errors for this field,
            // remove 'has-error' class from 'form-group'
            if (_.isEmpty(scope.messagesFor.$error)) {
              element.parents('.form-group').removeClass('has-error');
              element.closest('.form-group').find(".validate-message").css( "opacity", "0" );
            }
          }, 200);
        }, true);


      }
    };
  };


  validateMessages.$inject = injectParams;
  angular.module('emlogis.commonDirectives').directive('validateMessages', validateMessages);

}());