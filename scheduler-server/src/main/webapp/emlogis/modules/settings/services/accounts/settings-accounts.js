angular.module('emlogis.settings')
  .service('SettingsAccountsService', ['$http', '$rootScope', '$q', 'applicationContext', 'UtilsService',
    function ($http, $rootScope, $q, applicationContext, UtilsService) {
      var baseUrl = applicationContext.getBaseRestUrl();

      function sendRequest(urlPart, method, requestPayload) {
        var apiUrl = baseUrl + urlPart;
        var req = {
          method: method,
          url: apiUrl
        };
        if (method === 'POST' || method === 'PUT') {
          req.data = requestPayload;
        }

        return $http(req);
      }

      this.getEntityList = function (entityType, filterTxt, offset, limit, orderBy, orderDir) {
        var urlPart = '';
        var queryPart = [];

        if (entityType === $rootScope.consts.entityTypes.group) {
          urlPart = 'groupaccounts/ops/query';
        } else if (entityType === $rootScope.consts.entityTypes.user) {
          urlPart = 'useraccounts/ops/query';
        } else if (entityType === $rootScope.consts.entityTypes.role) {
          urlPart = 'roles/ops/query';
        } else {
          // do other operations
        }

        if (!UtilsService.checkEmpty(filterTxt)) {
          queryPart.push('search=' + filterTxt + '&searchfields=name');
        }
        if (!UtilsService.checkEmpty(offset)) {
          queryPart.push('offset=' + offset);
        }
        if (!UtilsService.checkEmpty(limit)) {
          queryPart.push('limit=' + limit);
        }
        if (!UtilsService.checkEmpty(orderBy)) {
          queryPart.push('orderby=' + orderBy);
        }
        if (!UtilsService.checkEmpty(orderDir)) {
          queryPart.push('orderdir=' + orderDir);
        }

        if (queryPart.length > 0) {
          urlPart += '?' + queryPart.join('&');
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.getEntityDetails = function (entityType, entityId) {
        var urlPart;
        if (entityType === $rootScope.consts.entityTypes.group) {
          urlPart = 'groupaccounts/' + entityId;
        } else if (entityType === $rootScope.consts.entityTypes.user) {
          urlPart = 'useraccounts/' + entityId;
        } else if (entityType === $rootScope.consts.entityTypes.role) {
          urlPart = 'roles/' + entityId;
        } else {
          // Other operations
        }


        return sendRequest(urlPart, 'GET', null);
      };

      this.getRelatedEntities = function (sourceEntityType, sourceEntityId, relatedEntityType, associated, filter, offset, limit, orderBy, orderDir) {
        var urlPart = '';
        var queryPart = [];
        var associatedString = '';
        if (!associated) {
          associatedString = 'unassociated';
        }

        if (sourceEntityType === $rootScope.consts.entityTypes.group) {
          if (relatedEntityType === $rootScope.consts.entityTypes.user) {
            urlPart = 'groupaccounts/' + sourceEntityId + '/' + associatedString + 'users';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.role) {
            urlPart = 'groupaccounts/' + sourceEntityId + '/' + associatedString + 'roles';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.accessControl) {
            urlPart = 'groupaccounts/' + sourceEntityId + '/aces/ops/getsitesteamsaces';
          }
          else {
            // do other operations
          }
        } else if (sourceEntityType === $rootScope.consts.entityTypes.user) {
          if (relatedEntityType === $rootScope.consts.entityTypes.group) {
            urlPart = 'useraccounts/' + sourceEntityId + '/' + associatedString + 'groups';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.role) {
            urlPart = 'useraccounts/' + sourceEntityId + '/' + associatedString + 'roles';
          } else {
            // do other operations
          }
        } else if (sourceEntityType === $rootScope.consts.entityTypes.role) {
          if (relatedEntityType === $rootScope.consts.entityTypes.accessControl) {
            urlPart = 'roles/' + sourceEntityId + '/aces/ops/getsitesteamsaces';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.permission) {
            urlPart = 'roles/' + sourceEntityId + '/' + associatedString + 'permissions';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.group) {
            urlPart = 'roles/' + sourceEntityId + '/' + associatedString + 'groupaccounts';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.user) {
            urlPart = 'roles/' + sourceEntityId + '/' + associatedString + 'useraccounts';
          } else {
            // do other operations
          }
        } else {
          // do Other operations
        }

        if (!UtilsService.checkEmpty(offset)) {
          queryPart.push('offset=' + offset);
        }
        if (!UtilsService.checkEmpty(limit)) {
          queryPart.push('limit=' + limit);
        }
        if (!UtilsService.checkEmpty(orderBy)) {
          queryPart.push('orderby=' + orderBy);
        }
        if (!UtilsService.checkEmpty(orderDir)) {
          queryPart.push('orderdir=' + orderDir);
        }
        if (!UtilsService.checkEmpty(filter)) {
          queryPart.push('filter=' + filter);
        }

        if (queryPart.length > 0) {
          urlPart += '?' + queryPart.join('&');
        }

        return sendRequest(urlPart, 'GET', null);
      };

      this.operateOnRelatedEntity = function (operation, sourceEntityType, sourceEntityId, relatedEntityType, payLoad) {
        var urlPart = null;
        var method = 'POST';

        if (sourceEntityType === $rootScope.consts.entityTypes.group) {
          if (relatedEntityType === $rootScope.consts.entityTypes.user) {
            urlPart = 'groupaccounts/' + sourceEntityId + '/ops/' + operation + 'users';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.role) {
            urlPart = 'groupaccounts/' + sourceEntityId + '/ops/' + operation + 'roles';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.accessControl) {
          urlPart = 'groupaccounts/' + sourceEntityId + '/aces/ops/setsitesteamsaces';
          } else {
            // do other operations
          }
        } else if (sourceEntityType === $rootScope.consts.entityTypes.user) {
          if (relatedEntityType === $rootScope.consts.entityTypes.group) {
            urlPart = 'useraccounts/' + sourceEntityId + '/ops/' + operation + 'groups';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.role) {
            urlPart = 'useraccounts/' + sourceEntityId + '/ops/' + operation + 'roles';
          } else {
            // do other operations
          }
        } else if (sourceEntityType === $rootScope.consts.entityTypes.role) {
          if (relatedEntityType === $rootScope.consts.entityTypes.group) {
            urlPart = 'roles/' + sourceEntityId + '/ops/' + operation + 'groups';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.user) {
            urlPart = 'roles/' + sourceEntityId + '/ops/' + operation + 'users';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.accessControl) {
            urlPart = 'roles/' + sourceEntityId + '/aces/ops/setsitesteamsaces';
          } else if (relatedEntityType === $rootScope.consts.entityTypes.permission) {
            urlPart = 'roles/' + sourceEntityId + '/ops/' + operation + 'permissions';
          } else {
            // do other operations
          }
        } else {
          // do other operations
        }

        return sendRequest(urlPart, method, payLoad);
      };

      this.createEntity = function (entityType, entityDetails) {
        var urlPart = null;

        if (entityType === $rootScope.consts.entityTypes.group) {
          urlPart = 'groupaccounts/';
        } else if (entityType === $rootScope.consts.entityTypes.user) {
          urlPart = 'useraccounts/';
        } else if (entityType === $rootScope.consts.entityTypes.role) {
          urlPart = 'roles/';
        } else {
          // do other operations
        }

        return sendRequest(urlPart, 'POST', entityDetails);
      };

      this.updateEntity = function (entityType, entityId, entityDetails) {
        var urlPart = null;

        if (entityType === $rootScope.consts.entityTypes.group) {
          urlPart = 'groupaccounts/' + entityId;
        } else if (entityType === $rootScope.consts.entityTypes.user) {
          urlPart = 'useraccounts/' + entityId;
        } else if (entityType === $rootScope.consts.entityTypes.role) {
          urlPart = 'roles/' + entityId;
        } else {
          // do other operations
        }

        return sendRequest(urlPart, 'PUT', entityDetails);
      };

      this.duplicateEntity = function (entityType, entityId, entityDetails) {
        var urlPart = null;

        if (entityType === $rootScope.consts.entityTypes.role) {
          urlPart = 'roles/' + entityId + '/ops/duplicate';
        } else {
          //do other operations
        }

        return sendRequest(urlPart, 'POST', entityDetails);
      };

      this.deleteEntity = function (entityType, entityId) {
        var urlPart = null;

        if (entityType === $rootScope.consts.entityTypes.group) {
          urlPart = 'groupaccounts/' + entityId;
        } else if (entityType === $rootScope.consts.entityTypes.user) {
          urlPart = 'useraccounts/' + entityId;
        } else if (entityType === $rootScope.consts.entityTypes.role) {
          urlPart = 'roles/' + entityId;
        } else {
          // do other operations
        }

        return sendRequest(urlPart, 'DELETE', null);
      };
    }
  ]);