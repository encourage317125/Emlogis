(function () {
  "use strict";


  //
  // SVG filters
  // This element is placed right after opening <body> tag
  // to be applied to all SVG images across the app

  var svgFilters = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div style="width: 0;height: 0">' +
                  '<svg xmlns="http://www.w3.org/2000/svg" style="width: 0;height: 0">' +
                    '<filter id="dropshadow" height="130%">' +
                      '<feGaussianBlur in="SourceAlpha" stdDeviation="5"/>' +
                      '<feOffset dx="0" dy="5" result="offsetblur"/>' +
                      '<feComponentTransfer>' +
                        '<feFuncA type="linear" slope="0.5"/>' +
                      '</feComponentTransfer>' +
                      '<feMerge>' +
                        '<feMergeNode/>' +
                        '<feMergeNode in="SourceGraphic"/>' +
                      '</feMerge>' +
                    '</filter>' +
                  '</svg>' +
                '</div>'
    };
  };
  svgFilters.$inject = [];
  angular.module('emlogis').directive('svgFilters', svgFilters);



  //
  // Profile icon
  // for top menu section

  var iconProfile = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="8 0 130 140"> <!-- 0 0 141 128 -->' +
                    '<path class="eml-svg eml-icon-profile" d="M71 5C38.4 5 12 31.4 12 64s26.4 59 59 59 59-26.4 59-59S103.6 5 71 5zm41.6 93.5c-5.4-2.3-18.1-6.7-26-9-.7-.2-.8-.2-.8-3 0-2.3 1-4.6 1.9-6.6 1-2.1 2.2-5.7 2.6-9 1.2-1.4 2.8-4.1 3.9-9.3.9-4.6.5-6.3-.1-7.8-.1-.2-.1-.3-.2-.5-.2-1.1.1-6.7.9-11 .5-3-.1-9.3-4.2-14.6-2.6-3.3-7.6-7.4-16.6-8h-5c-8.9.6-13.9 4.6-16.5 7.9-4.1 5.2-4.8 11.6-4.2 14.6.8 4.3 1.1 9.9.9 11 0 .2-.1.3-.2.5-.6 1.6-1 3.2-.1 7.8 1 5.2 2.7 8 3.9 9.3.4 3.2 1.6 6.8 2.6 9 .7 1.6 1.1 3.7 1.1 6.7 0 2.8-.1 2.8-.7 3-8.1 2.4-21.1 7.1-26 9.2-8-9.4-12.8-21.6-12.8-34.8-.1-29.7 24.2-54 54-54s54.1 24.3 54.1 54.1c0 13.1-4.7 25.2-12.5 34.5z"/>' +
                  '</svg>' +
                '</div>'
    };
  };
  iconProfile.$inject = [];
  angular.module('emlogis').directive('iconProfile', iconProfile);



  //
  // Messages icon
  // for top menu section

  var iconMessages = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="3 -4 137 140">' +
                    '<path class="eml-svg eml-icon-messages" d="M116.9 5H25.1C14.6 5 6 13.4 6 23.9v52.8c0 10.4 8.6 18.9 19.1 18.9h42.1L97.8 122V95.6h19.1c10.6 0 19.1-8.4 19.1-18.9V23.9C136 13.4 127.4 5 116.9 5z"/>' +
                  '</svg>' +
                '</div>'
    };
  };
  iconMessages.$inject = [];
  angular.module('emlogis').directive('iconMessages', iconMessages);



  //
  // Setting icon
  // for breadcrumb section

  var iconSettings = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="7 0 127 128">' +
                    '<path class="eml-svg eml-icon-settings" d="M70.5 52.3c-6.2 0-11.2 5-11.2 11.2 0 6.2 5 11.2 11.2 11.2s11.2-5 11.2-11.2c0-6.2-5-11.2-11.2-11.2zm52.1 0H112c-1-3.7-2.4-7.1-4.3-10.3l7.5-7.5c2.9-2.9 2.9-7.6 0-10.5l-5.3-5.3c-2.9-2.9-7.6-2.9-10.5 0L92 26.3c-3.2-1.8-6.7-3.3-10.3-4.3V11.4c0-4.1-3.3-7.4-7.4-7.4h-7.4c-4.1 0-7.4 3.3-7.4 7.4V22c-3.6 1-7.1 2.4-10.3 4.3l-7.5-7.5c-2.9-2.9-7.6-2.9-10.5 0l-5.3 5.3c-2.9 2.9-2.9 7.6 0 10.5l7.5 7.5c-1.8 3.2-3.3 6.7-4.3 10.3H18.4c-4.1 0-7.4 3.3-7.4 7.4v7.4c0 4.1 3.3 7.4 7.4 7.4H29c1 3.7 2.4 7.1 4.3 10.3l-7.5 7.5c-2.9 2.9-2.9 7.6 0 10.5l5.3 5.3c2.9 2.9 7.6 2.9 10.5 0l7.5-7.5c3.2 1.8 6.7 3.3 10.3 4.3v10.5c0 4.1 3.3 7.4 7.4 7.4h7.4c4.1 0 7.4-3.3 7.4-7.4V105c3.6-1 7.1-2.4 10.3-4.3l7.5 7.5c2.9 2.9 7.6 2.9 10.5 0l5.3-5.3c2.9-2.9 2.9-7.6 0-10.5l-7.5-7.5c1.8-3.2 3.3-6.7 4.3-10.3h10.6c4.1 0 7.4-3.3 7.4-7.4v-7.4c0-4.1-3.3-7.5-7.4-7.5zM70.5 89.5c-14.4 0-26-11.7-26-26 0-14.4 11.7-26 26-26 14.4 0 26 11.7 26 26 0 14.4-11.6 26-26 26z"/>' +
                  '</svg>' +
                '</div>'
    };
  };
  iconSettings.$inject = [];
  angular.module('emlogis').directive('iconSettings', iconSettings);



  //
  // Help icon
  // for breadcrumb section

  var iconHelp = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="7 0 128 128">' +
                    '<path class="eml-svg eml-icon-help" fill-rule="evenodd" clip-rule="evenodd" d="M70.5 4.8c32.7 0 59.3 26.5 59.3 59.3s-26.5 59.3-59.3 59.3S11.2 96.8 11.2 64 37.8 4.8 70.5 4.8zm-7.2 82.4h11.9v11.1H63.3V87.2zm-4-32.2c0-8.1 3.4-13.3 10.8-13.3 3.1 0 8.7 2.7 8.7 9.9 0 6-3.5 8.1-7.2 11.4-4.7 4-6.9 7.7-6.9 18.1h9.6c0-8.5 3.3-11.2 7.8-15 3.8-3.4 7.8-6.3 7.8-14.8 0-11.5-8.5-18-19.4-18-13.2 0-21.3 8.6-21.3 21.7h10.1z"/>' +
                  '</svg>' +
                '</div>'
    };
  };
  iconHelp.$inject = [];
  angular.module('emlogis').directive('iconHelp', iconHelp);



  //
  // Mark as Read icon
  // for Notification's dropdown in top menu

  var markAsRead = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 141 128">' +
                    '<path class="eml-svg eml-icon-mark-read" d="M112.7 22.3C101.5 11.1 86.7 5 71 5c-15.8 0-30.6 6.1-41.7 17.3-23 23-23 60.4 0 83.5C40.4 116.9 55.2 123 71 123c15.8 0 30.6-6.1 41.7-17.3 23-23 23-60.4 0-83.4zm-3.4 80c-10.2 10.2-23.9 15.9-38.3 15.9-14.5 0-28.1-5.6-38.3-15.9-21.1-21.1-21.1-55.5 0-76.7C42.9 15.4 56.5 9.8 71 9.8c14.5 0 28.1 5.6 38.3 15.9 21.1 21.1 21.1 55.5 0 76.6zM95.8 46.4l-34.6 37-15.1-16.2c-.9-1-2.4-1-3.4-.1s-1 2.4-.1 3.4l16.9 18h.1v.1c.1.1.3.2.5.3.1.1.2.1.3.2.3.1.6.2.9.2.3 0 .6-.1.9-.2.1 0 .2-.1.3-.2.2-.1.3-.2.5-.3v-.1h.1l36.3-38.8c.9-1 .9-2.5-.1-3.4-1.1-.9-2.6-.9-3.5.1z"/> ' +
                  '</svg>' +
                '</div>'
    };
  };
  markAsRead.$inject = [];
  angular.module('emlogis').directive('markAsRead', markAsRead);



  //
  // Close icon
  // for popup windows and "delete" actions

  var iconClose = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 141 128">' +
                    '<path class="eml-svg eml-icon-close" d="M83.4 62.8L129.6 17 116.7 4.2 70.5 50.1 24.2 4 11.4 16.8l46.3 45.9-47.8 47.4 12.8 12.8 47.9-47.4 47.6 47.2 12.7-12.8"/>' +
                  '</svg>' +
                '</div>'
    };
  };
  iconClose.$inject = [];
  angular.module('emlogis').directive('iconClose', iconClose);



  //
  // Calendar icon
  // for inline editing in Employee profile module

  var iconCalendar = function () {
    return {
      restrict: 'A',
      replace: true,
      template: '<div>' +
                  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 141 128">' +
                    '<path class="eml-svg eml-icon-calendar" d="M95.9 18.8h-6.3v13.1h6.3V18.8zM83 96.8h6.5V58.1H83v6.5h-6.5v6.3H83v25.9zM50.6 19.1h-6.5v12.8h6.5V19.1zm51.8 6.5v12.9H83.1V25.6h-26v12.8H37.4V25.6H18v82.8c0 8.1 6.6 14.7 14.7 14.7h74.7c8.1 0 14.7-6.6 14.7-14.7V25.6h-19.7zm13.1 78.1c0 3.6-2.9 6.5-6.5 6.5H31c-3.6 0-6.5-2.9-6.5-6.5V45.1h91v58.6zm-65-6.5H57V58.1h-6.5v6.5H44v6.5h6.5v26.1z"/>' +
                  '</svg>' +
                '</div>'
    };
  };
  iconCalendar.$inject = [];
  angular.module('emlogis').directive('iconCalendar', iconCalendar);



}());