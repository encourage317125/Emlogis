(function () {
  "use strict";


  //
  // Page-level notification
  // This directive controls the DOM behavior
  // for notifications displayed below Top Menu

  var notification = function ($rootScope, $window, applicationContext) {
    return {
      restrict: 'AE',
      replace: true,
      templateUrl: 'modules/_layouts/partials/notification.tpl.html',
      controller: 'NotificationCtrl',
      link: function(scope, element, attrs, controller, transclude){

        //
        // Watch for
        // a new Page-level Notification
        // to be posted to applicationContext

        scope.$watch('msg', function(newValue, oldValue) {
          if (newValue.visible && newValue.type !== oldValue.type || newValue.content !== oldValue.content){
            var newMsg = newValue;

            if (newMsg.type === 'save') {
              showSaveBtn(newMsg);

              angular.element($window).bind("scroll", function() {
                startScrollTimer(newMsg);
              });
            }

          // If msg was hidden and it's not Save,
          // unbind scroll listener

          } else if (!newValue.visible && newValue.type !== 'save') {
            angular.element($window).unbind();
          }
        }, true);


        //
        // On leaving /general and /site_teams,
        // unbind scroll listener

        $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams){
          if (fromState.url.indexOf('general') > -1 || fromState.url.indexOf('site_teams') > -1) {
            angular.element($window).unbind();
          }
        });

        //
        // Show Save Notification
        // based on the visibility of Save btn in Action Bar

        var showSaveBtn = function(msg){
          msg.visible = !isSaveBtnVisible();
          applicationContext.setNotificationMsg(msg);
        };



        //
        // Start listening to
        // $window position on scroll

        var startScrollTimer = function(msg){
          //console.log('scrolling...');
          if (scrollTimer) {
            clearTimeout(scrollTimer);                  // clear any previous pending timer
          }

          var scrollTimer = setTimeout(function() {     // set new timer
            if (msg.type === 'save') {
              showSaveBtn(msg);
            }
          }, 10);
        };



        //
        // Calculate
        // if Save button in Action Bar
        // is visible to user

        var isSaveBtnVisible = function(){
          var siteLevelMsgHeight = $('#siteLevelMsg').outerHeight(),
              breadcrumbsHeight = $('.breadcrumb-section').outerHeight(),
              tabsHeight = $('.eml-top-tabs').outerHeight(),
              actionBarHeight = $('.eml-action-bar').outerHeight() * 0.7;

          var saveBtnHeight = breadcrumbsHeight + tabsHeight + actionBarHeight;
          var windowTopHeight = $(window).scrollTop() + siteLevelMsgHeight;

          return windowTopHeight < saveBtnHeight;
        };
      }
    };
  };

  notification.$inject = ['$rootScope', '$window', 'applicationContext'];
  angular.module('emlogis').directive('notification', notification);


}());