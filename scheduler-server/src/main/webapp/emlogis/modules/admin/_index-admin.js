angular.module('emlogis.admin', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor', 'ui.grid', 'ui.grid.moveColumns', 'ui.grid.pagination', 'ui.grid.selection', 'ui.grid.resizeColumns', 'emlogis.commonservices', 'emlogis.commonDirectives', 'frapontillo.bootstrap-switch', 'uiGmapgoogle-maps'])
  .config(['$stateProvider', '$urlRouterProvider',
    function ( $stateProvider, $urlRouterProvider) {

      $urlRouterProvider.when('/admin', '/admin/customers');
      $urlRouterProvider.when('/admin/', '/admin/customers');
      $urlRouterProvider.when('/admin/customers', '/admin/customers/list');
      $urlRouterProvider.when('/admin/customers/', '/admin/customers/list');
      $urlRouterProvider.when('/admin/notification-delivery', '/admin/notification-delivery/email');
      $urlRouterProvider.when('/admin/notification-delivery/', '/admin/notification-delivery/email');
      $urlRouterProvider.when('/admin/notification-delivery/email', '/admin/notification-delivery/email/edit');
      $urlRouterProvider.when('/admin/notification-delivery/email/', '/admin/notification-delivery/email/edit');
      $urlRouterProvider.when('/admin/notification-delivery/sms', '/admin/notification-delivery/sms/edit');
      $urlRouterProvider.when('/admin/notification-delivery/sms/', '/admin/notification-delivery/sms/edit');

      $stateProvider.state('authenticated.admin', {
        url: '/admin',
        abstract: true,
        views: {
          'content@authenticated': {
            templateUrl: 'modules/admin/partials/admin.html',
            controller: 'AdminCtrl'
          },
          'breadcrumb@authenticated': {
            templateUrl: 'modules/admin/partials/admin-breadcrumb.html',
            controller: 'AdminBreadcrumbCtrl'
          }
        },
        data: {
          ncyBreadcrumbLabel: '{{"nav.ADMIN" | translate}}',
          permissions: function (authService) {
            return authService.hasPermissionIn(['Tenant_Mgmt', 'Tenant_View']);
          }
        }
      })
        .state('authenticated.admin.customers', {
          url: '/customers',
          views: {
            'adminContent@authenticated.admin': {
              templateUrl: 'modules/admin/partials/customers/admin-customers.html',
              controller: 'AdminCustomersCtrl'
            },
            'adminBreadcrumb@authenticated.admin': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-breadcrumb.html',
              controller: 'AdminCustomersBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"admin.CUSTOMERS" | translate}}'
          }
        })
        .state('authenticated.admin.customers.list', {
          url: '/list',
          views: {
            'adminCustomersContent@authenticated.admin.customers': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-list.html',
              controller: 'AdminCustomersListCtrl'
            },
            'adminCustomersBreadcrumb@authenticated.admin.customers': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-list-breadcrumb.html',
              controller: 'AdminCustomersListBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.LIST" | translate}}'
          }
        })
        .state('authenticated.admin.customers.customerEdit', {
          url: '/:tenantId/edit',
          views: {
            'adminCustomersContent@authenticated.admin.customers': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-customer-edit.html',
              controller: 'AdminCustomersCustomerEditCtrl'
            },
            'adminCustomersBreadcrumb@authenticated.admin.customers': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-customer-edit-breadcrumb.html',
              controller: 'AdminCustomersCustomerEditBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.EDIT" | translate}}'
          }
        })
        .state('authenticated.admin.customers.create', {
          url: '/create',
          views: {
            'adminCustomersContent@authenticated.admin.customers': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-create.html',
              controller: 'AdminCustomersCreateCtrl'
            },
            'adminCustomersBreadcrumb@authenticated.admin.customers': {
              templateUrl: 'modules/admin/partials/customers/admin-customers-create-breadcrumb.html',
              controller: 'AdminCustomersCreateBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.CREATE" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery', {
          url: '/notification-delivery',
          views: {
            'adminContent@authenticated.admin': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery.html',
              controller: 'AdminNotificationDeliveryCtrl'
            },
            'adminBreadcrumb@authenticated.admin': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-breadcrumb.html',
              controller: 'AdminNotificationDeliveryBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"admin.NOTIFICATION_DELIVERY" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery.email', {
          url: '/email',
          views: {
            'adminNotificationDeliveryContent@authenticated.admin.notificationDelivery': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-email.html',
              controller: 'AdminNotificationDeliveryEmailCtrl'
            },
            'adminNotificationDeliveryBreadcrumb@authenticated.admin.notificationDelivery': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-email-breadcrumb.html',
              controller: 'AdminNotificationDeliveryEmailBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"admin.notification_delivery.EMAIL" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery.email.edit', {
          url: '/edit',
          views: {
            'adminNotificationDeliveryEmailContent@authenticated.admin.notificationDelivery.email': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-email-edit.html',
              controller: 'AdminNotificationDeliveryEmailEditCtrl'
            },
            'adminNotificationDeliveryEmailBreadcrumb@authenticated.admin.notificationDelivery.email': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-email-edit-breadcrumb.html',
              controller: 'AdminNotificationDeliveryEmailEditBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.EDIT" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery.email.createProvider', {
          url: '/create-provider/:providerType',
          views: {
            'adminNotificationDeliveryEmailContent@authenticated.admin.notificationDelivery.email': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-email-create-provider.html',
              controller: 'AdminNotificationDeliveryEmailCreateProviderCtrl'
            },
            'adminNotificationDeliveryEmailBreadcrumb@authenticated.admin.notificationDelivery.email': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-email-create-provider-breadcrumb.html',
              controller: 'AdminNotificationDeliveryEmailCreateProviderBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"admin.notification_delivery.CREATE_PROVIDER" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery.sms', {
          url: '/sms',
          views: {
            'adminNotificationDeliveryContent@authenticated.admin.notificationDelivery': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-sms.html',
              controller: 'AdminNotificationDeliverySmsCtrl'
            },
            'adminNotificationDeliveryBreadcrumb@authenticated.admin.notificationDelivery': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-sms-breadcrumb.html',
              controller: 'AdminNotificationDeliverySmsBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"admin.notification_delivery.SMS" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery.sms.edit', {
          url: '/edit',
          views: {
            'adminNotificationDeliverySmsContent@authenticated.admin.notificationDelivery.sms': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-sms-edit.html',
              controller: 'AdminNotificationDeliverySmsEditCtrl'
            },
            'adminNotificationDeliverySmsBreadcrumb@authenticated.admin.notificationDelivery.sms': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-sms-edit-breadcrumb.html',
              controller: 'AdminNotificationDeliverySmsEditBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"app.EDIT" | translate}}'
          }
        })
        .state('authenticated.admin.notificationDelivery.sms.createProvider', {
          url: '/create-provider/:providerType',
          views: {
            'adminNotificationDeliverySmsContent@authenticated.admin.notificationDelivery.sms': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-sms-create-provider.html',
              controller: 'AdminNotificationDeliverySmsCreateProviderCtrl'
            },
            'adminNotificationDeliverySmsBreadcrumb@authenticated.admin.notificationDelivery.sms': {
              templateUrl: 'modules/admin/partials/notification-delivery/admin-notification-delivery-sms-create-provider-breadcrumb.html',
              controller: 'AdminNotificationDeliverySmsCreateProviderBreadcrumbCtrl'
            }
          },
          data: {
            ncyBreadcrumbLabel: '{{"admin.notification_delivery.CREATE_PROVIDER" | translate}}'
          }
        });
    }
  ])
  .config(['uiGmapGoogleMapApiProvider', function(uiGmapGoogleMapApiProvider) {
    uiGmapGoogleMapApiProvider.configure({
      //    key: 'your api key',
      v: '3.17',
      libraries: 'weather,geometry,visualization'
    });
  }])
  .run(['$rootScope', '$http', function($rootScope, $http) {

  }]);
