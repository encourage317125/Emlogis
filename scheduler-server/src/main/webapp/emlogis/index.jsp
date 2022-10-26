<!DOCTYPE html>
<html ng-app="emlogis" class="secured-application waiting-for-angular">
	<head>
	    <meta charset="utf-8"/>
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <title>EmLogis Suite</title>
	    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">

		<!-- Load Montserrat web font - Temp placement TODO to be replaced by local fonts in fonts.scss -->
        <link href='https://fonts.googleapis.com/css?family=Montserrat' rel='stylesheet' type='text/css'>

	    <script>document.write('<base href="' + document.location + '" />');</script>

		<!--
	    	lib.js file contains the all the js files under libs folder
	     -->
	    <script charset="utf-8" src="public/js/library.js"></script>

	    <!--
	    	lib.js file contains the all the js files under libs folder
	     -->
	    <script charset="utf-8" src="public/js/util.js"></script>

	    <!--
	    	Application.js file contains the all the js files under modules folder
	     -->
	    <script charset="utf-8" src="public/js/application.js"></script>

        <!--
	    	Library.css file contains all the scss files under moduels folder
	     -->
        <link rel="stylesheet" href="public/stylesheets/library.css"/>

	    <!--
	    	Application.css file contains all css files which are compiled from scss files under moduels folder
	     -->
	    <link rel="stylesheet" href="public/stylesheets/application.css"/>

	</head>

	<body class="emlogis" ng-controller="AppCtrl">

		<!-- header section -->
		<div ng-include="'modules/_layouts/partials/header.tpl.html'"></div>

        <!-- SseEvent & Heartbit-->
        <%--<div ng-include="'modules/_layouts/partials/sse_event.tpl.html'"></div>--%>

        <!-- Module Section : Individual module will be included here-->
		<div ui-view></div>

        <!-- Alert Ctrl : This section will show alert message-->
        <div ng-controller="AlertsCtrl">
            <div id="alert">
                <alert class="alert-animation" ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">
                    {{alert.msg}}
                </alert>
            </div>
        </div>

	</body>
</html>
