var home = angular.module('emlogis.reports');

home.controller('ReportsDetailsCtrl',
  [
    '$scope',
    'ReportsService',
    'ReportsiHubService',
    'applicationContext',
    'authService',
    '$stateParams',
    '$filter',
    '$q',
    '$sce',
    function ($scope,
              ReportsService,
              ReportsiHubService,
              applicationContext,
              authService,
              $stateParams,
              $filter,
              $q,
              $sce) {

      var reportName = "";
      var reportNamePrefix = "/Reports/";
      var visualId = null;
      var accountInfo = {};
      $scope.reportInfo = null;
      var param = null;

      $q.when(ReportsService.reportsConfigPromise && ReportsService.loadJsapiPromise).then(function () {
        $scope.reportInfo = ReportsService.getReportInfo($stateParams.groupId, $stateParams.id);
        initReport();
      });

      function initReport() {
        reportName = $stateParams.id + ".rptdesign";
        if (!accountInfo.tenantId) {
          ReportsService.getAccountInfo().then(function (res) {
            accountInfo = res.data;
          }, function (error) {
            applicationContext.setNotificationMsgWithValues(error.data.message, 'danger', true, error.statusText);
          });
        }
        if ($scope.reportInfo.ui === 0) {
          init();
        }
        if ($scope.reportInfo.ui === 1) {
          initWithBirt();
        }
        //if ($scope.reportInfo.ui === 2){
        //  initInFrame();
        //}

      }

      function init() {

        try {
          actuate.load("viewer");
          actuate.load("parameter");
          var reqOpts = new actuate.RequestOptions();
          reqOpts.setCustomParameters({
            "code": authService.getToken()
          });
          if (!actuate.isInitialized()) {
            actuate.initialize(
              ReportsiHubService.getIhubUrl(),
              reqOpts,
              null,
              null,
              getParamsInfo,
              function (err) {
                console.log(err);
              });
          } else {
            getParamsInfo();
          }
        } catch(error) {
          applicationContext.setNotificationMsgWithValues(error.message, 'danger', true, error.statusText);
        }

      }

      $scope.parameters = [];

      function removeExtention(filename) {
        return filename.replace(/\.[^/.]+$/, "");
      }

      function getParamsInfo() {
        $scope.parameterIsLoading = true;
        ReportsiHubService.items().then(function (res) {
          var items = res.data.ItemList.File;
          var report = _.find(items, function (item) {
            return removeExtention(item.Name) == removeExtention(reportName);
          });
          if (report) {
            visualId = report.Id;
            ReportsiHubService.parameters(visualId).then(function (res) {
              var params = res.data;
              renderParamsForUI(params.ParameterList.ParameterDefinition);
            }, function (error) {
              applicationContext.setNotificationMsgWithValues(error.data.error.message + ' - ' + error.data.error.description, 'danger', true);
              $scope.parameterIsLoading = false;
            });
          } else {
            applicationContext.setNotificationMsgWithValues('Report does not exist', 'danger', true);
            $scope.parameterIsLoading = false;
          }
        });
      }

      function renderParamsForUI(paramdefs) {

        $scope.parameters = paramdefs.filter(function (el) {
          return el.Name !== "restApiUrl";
        });

        angular.forEach($scope.parameters, function (val, key) {
          if (val.DataType == 'DateOnly') {
            val._type = 'Date';
          }
          if ((val.DataType == 'String' || val.DataType == 'Time') && !val.IsAdHoc) {
            val._type = 'Select';
          }
          if (val.DataType == 'String' && val.IsAdHoc) {
            val._type = 'MultiSelect';
          }
          if (val.DataType == 'Boolean') {
            val._type = 'Boolean';
          }
        });

        angular.forEach($scope.parameters, function (val, key) {

          if (val._type == 'Date') {
            val.value = new Date();
            val.dateOpened = false;
          }
          if (val.SelectNameValueList === undefined ||
            (val.SelectNameValueList !== undefined && val.SelectNameValueList.NameValuePair === undefined)) {
            val.SelectNameValueList = {NameValuePair: []};
          }

          var value = null;
          if (val.Name === 'Security') {
            value = accountInfo.tenantId + "," + accountInfo.accountId;
          }

          if (val._type === 'Date') {
            value = $filter('date')(val.value, "MM/dd/yyyy");
          }
          if (val._type === 'Boolean') {
            value = (val.DefaultValue.toLowerCase() === 'true');
          }
          val._Value = value;
        });

        $scope.fetchNext(0);

      }

      function getAllParamValues() {
        var paramValues = [];
        angular.forEach($scope.parameters, function (val, key) {
          if (val._Value !== null && val.Name !== 'ShowSkill' && val.Name !== 'ShowTeamName' ) {
            paramValues.push({
              Name: val.Name,
              Value: val._Value
            });
          }
        });
        return paramValues;
      }

      function getCurentParamValues(index) {
        if ($scope.parameters.length > index) {
          var paramValues = [];
          for (var i = 0; i < index; i++) {
            var item = $scope.parameters[i];
            if (item._Value !== null && item.Group && item.Group === $scope.parameters[index].Group) {
              var tmp= item._Value;
              //todo
              if(item.Name === 'siteInfo' || item.Name === 'SelectedSiteInfo'){
                tmp = {};
              }
              paramValues.push({
                Name: item.Name,
                Value: tmp
              });
            }
          }
          return paramValues;
        }
      }

      //on change event
      $scope.fetchNextFromUI = function (index) {
        var param = $scope.parameters[index];

        changeValue(param);
        if (angular.isDefined(param.Group)){
          $scope.fetchNext(index);
        }
      };

      function changeValue(param) {
        if (param._type == 'Date') {
          param._Value = $filter('date')(param.value, "MM/dd/yyyy");
        }
        if (param._type == 'Select' && param.Name !== 'Security') {
          param._Value = param.value.Value;
        }
        if (param._type == 'MultiSelect') {
          var values = "";
          angular.forEach(param.SelectNameValueList.NameValuePair, function (val1, key1) {
            if (val1.ticked) {
              if (values !== "") {
                values += ',';
              }
              values += '\'' + val1.Value + '\'';
            }
          });
          param._Value = values;
        }
      }
      //index - index of param in $scope.parameters
      $scope.fetchNext = function (index) {

        $scope.parameterIsLoading = true;

        var param = $scope.parameters[index];

        changeValue(param);

        var next = index + 1;
        var paramNext;
        do {
          paramNext = $scope.parameters[next];
          next++;
        } while (paramNext && paramNext.IsHidden);
        next--;

        if (paramNext) {
          if (paramNext.IsDynamicSelectionList || paramNext._Value === null) {
            ReportsiHubService.picklist(visualId, paramNext.Name, paramNext.Group, getCurentParamValues(next))
              .then(function (res) {
                paramNext.SelectNameValueList = res.data.ParameterPickList || {};

                if (paramNext.SelectNameValueList.NameValuePair === undefined) {
                  paramNext.SelectNameValueList.NameValuePair = [];
                }
                if (paramNext._type == 'Select') {
                  if (!paramNext.DefaultValueIsNull){
                    paramNext.value = _.find(paramNext.SelectNameValueList.NameValuePair, function (item) {
                      return item.Value == paramNext.DefaultValue;
                    });
                  } else {
                    paramNext.value = paramNext.SelectNameValueList.NameValuePair[0];
                  }
                }
                if (paramNext._type == 'MultiSelect') {
                  paramNext.selectedValues = [];

                  angular.forEach(paramNext.SelectNameValueList.NameValuePair, function (val, key) {
                    val.ticked = true;
                  });
                }
                if (paramNext.SelectNameValueList.NameValuePair.length > 0) {
                  $scope.fetchNext(next);
                } else {
                  $scope.clearNext(next);
                }
              }, function (error) {
                applicationContext.setNotificationMsgWithValues(error.data.error.message + ' - ' +
                error.data.error.description, 'danger', true);
              });
          } else {
            //todo change  paramNext._type === 'Date'
            if (paramNext.SelectNameValueList.NameValuePair.length > 0 || paramNext._type === 'Date') {
              $scope.fetchNext(next);
            } else {
              $scope.clearNext(next);
            }
          }
        } else {
          $scope.parameterIsLoading = false;
        }

      };

      $scope.clearNext = function (index) {
        var param = $scope.parameters[index];
        if (param.IsDynamicSelectionList) {
          param.SelectNameValueList.NameValuePair = [];
          param._Value = null;
        }

        if ($scope.parameters[index + 1]) {
          $scope.clearNext(index + 1);
        } else {
          $scope.parameterIsLoading = false;
        }
      };

      $scope.submitted = false;

      $scope.runReport = function ($event, valid) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.submitted = true;

        if (valid) {
          $scope.parameterIsLoading = true;
          ReportsiHubService.execute(visualId, getAllParamValues()).then(function (res) {
            $scope.parameterIsLoading = false;
            console.log(res.data);
            var viewer = new actuate.Viewer("viewer");
            //viewer.setReportName(reportName);
            //viewer.setParameterValues({"ParameterValue": paramValues});
            viewer.setReportDocument("/$$$Transient/" + res.data.ObjectId +"."+res.data.OutputFileType, res.data.connectionHandle);
            viewer.setSize(window.innerWidth - 45 , 900);
            viewer.submit();
            $('#actuateViewerContainerviewer').bind('DOMMouseScroll', function (e) {
              e.stopPropagation();
            });
            $('#actuateViewerContainerviewer').bind('mousewheel', function (e) {
              e.stopPropagation();
            });
          }, function (error) {
            $scope.parameterIsLoading = false;
            applicationContext.setNotificationMsgWithValues(error.data.error.message + ' - ' +
              error.data.error.description, 'danger', true);
          });
        }

      };

      // open calendar control
      $scope.openCalendar = function ($event, sObj, propName) {
        $event.preventDefault();
        $event.stopPropagation();
        sObj[propName] = true;
      };


      //====================== display parameters UI from BIRT

      function initWithBirt() {
        actuate.load("viewer");
        actuate.load("dialog");
        actuate.load("parameter");
        var reqOpts = new actuate.RequestOptions();
        reqOpts.setCustomParameters({
          'code': authService.getToken()
        });
        if (!actuate.isInitialized()) {
          actuate.initialize(
            ReportsiHubService.getIhubUrl(),
            reqOpts,
            null,
            null,
            displayParams);
        } else {
          displayParams();
        }
      }

      function displayParams() {
        //$scope.loading = 'visible-opt';
        //document.getElementById("progress_animation_null").style.display = 'block';
        document.getElementById("run").style.display = 'none';
        param = new actuate.Parameter("parameters");
        param.setReportName(reportNamePrefix + reportName);
        param.submit(
          function () {
            document.getElementById("progress_animation_null").style.display = 'block';
            param.downloadParameters(function (paramdefs) {
              for (var i = 0; i < paramdefs.length; i++) {
                if (paramdefs[i].getName() == 'Security') {
                  paramdefs[i].setDefaultValue(accountInfo.tenantId + "," + accountInfo.accountId);
                }
                if (paramdefs[i].getName() == 'siteId' || paramdefs[i].getName() == 'teamId') {
                  paramdefs[i].setSelectNameValueList([]);
                }
              }
              updateParams(paramdefs);
            });
          }, function (error) {
            console.log(error);
          }
        );
      }

      function updateParams(paramdefs) {
        param.renderContent(paramdefs, function () {
          //document.getElementsByClassName("param-opt").style.visibility = 'visible';
          //$scope.visible = 'visible-opt';
          document.getElementById("progress_animation_null").style.display = 'none';
          //document.getElementById("parameters-container").style.display = 'block';
          document.getElementById("run").style.display = 'inline-block';
        });
      }

      $scope.processParameters = function () {
        param.downloadParameterValues(runReportWithBirt);
      };

      function runReportWithBirt(paramvalues) {
        var viewer = new actuate.Viewer("viewer");
        viewer.setReportName(reportNamePrefix + reportName);
        viewer.setParameterValues(paramvalues);
        viewer.setSize(window.innerWidth - 45, window.innerHeight - 200);
        viewer.submit();
      }


    }
  ]);
