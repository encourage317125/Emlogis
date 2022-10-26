(function() {

    // browser configuration: prior to adding a new entity to the browser,
    // make sure you configure
    // #1 the appContext.entity2resource object (.run() method at the very bottom of the script)
    // #2 the ctlrConfig (controller config)

    // Controllers config data.
    // data which is passed to controllers to configure them
    // organized per 'types' of controllers at first level, then structured as needed by specifi type of controller
    // and the  add a top level in map to have config by controllertype (list/ entitiy/ related entity ...)


    angular
        .module('emlogis.browser', ['ui.bootstrap', 'ui.router', 'http-auth-interceptor', 'emlogis.commonservices', 'emlogis.accountmgmt', 'dialogs.main', 'ngAnimate'])
        .config(

            function($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, dialogsProvider) {

                $stateProvider
                // generic state for top level entity lists (all groups, all users, all roles)
                // should likely be defined at App level as applying to any entity
                    .state('browser', {
                    url: '/browser/{entity}',
                    templateUrl: function($stateParams) {
                        var entity = $stateParams.entity;
                        var config = ctlrConfig.entityListCtlr[entity];
                        if (config && config.partial) {
                            return 'modules/browser/partials/' + config.partial;
                        } else {
                            return 'modules/browser/partials/' + $stateParams.entity + 'list.html';
                        }
                    },
                    data: {
                        ncyBreadcrumbLabel: '{{entityTypeLabel}}s'
                    },
                    // returning a controller name is mandatory for breadcrumb label to use scope.
                    controllerProvider: function($stateParams) {

                        var entity = $stateParams.entity;
                        var config = ctlrConfig.entityListCtlr[entity];
                        var ctlr;
                        if (config) {
                            if (config.genericCtlr) {
                                // this entity is configured for using the generic ctlr
                                ctlr = 'EntityListCtlr';
                            } else {
                                // specific controler, thus by convention, ctlr name should be: entity with first letter in capital + MgmtCtlr
                                // ex for groupaccounts: 'GroupaccountListCtlr'
                                ctlr = entity.substring(0, 1).toUpperCase() + entity.substring(1, entity.length) + 'ListCtlr';
                            }
                            // copy config into $stateParams to pass them to ctlr
                            $stateParams.ctlrConfig = config;
                        } else {
                            // assume we use the specific controler, see naming convention above
                            ctlr = entity.substring(0, 1).toUpperCase() + entity.substring(1, entity.length) + 'ListCtlr';
                        }
                        return ctlr;
                    }
                })

                // generic state for top entity details (specific group, user, role)
                // should actually be defined at App level as applying to any entity
                .state('entity', {
                    url: '/entity/{entity}/{id}', //?parentEntityId={parentEntityId}
                    templateUrl: function($stateParams) {
                        // by convention, tpl name should be: <entity>.html
                        var url = 'modules/browser/partials/' + $stateParams.entity + '.html';
                        return url;
                    },
                    data: {
                        ncyBreadcrumbLabel: '{{entityTypeLabel}}: {{elt.name}}'
                    },
                    controllerProvider: function($stateParams) {
                        console.log("entity.getCtlr(): " + _.values($stateParams));
                        var entity = $stateParams.entity;
                        var config = ctlrConfig.entityCtlr[entity];
                        $stateParams.ctlrConfig = config;
                        // check if we use the generic entity controller
                        if (config.genericCtlr) {
                            // yes, this entity is configured for using the generic ctlr
                            return 'EntityCtlr';
                        }
                        // by convention, ctlr name should be: entity with first letter in capital + Ctlr
                        // ex for groupaccounts: 'GroupaccountCtlr'
                        var entity = $stateParams.entity;
                        var ctrl = entity.substring(0, 1).toUpperCase() + entity.substring(1, entity.length) + 'Ctlr';
                        return ctrl;
                    }
                })

                // top level state for creating an entity associated to a parent one
                // controllers are  same as for top level entities 
                .state('createchildentity', {
                        url: '/parententity/{parentEntity}/{parentEntityId}/createchild/{entity}/{id}/',
                        templateUrl: function($stateParams) {

                            // by convention, tpl name should be: <parententity>_create<entity>.html
                            // but can be overriden by config.secundaryEntityCreatePartial
                            var url = 'modules/browser/partials/',
                                config = ctlrConfig.relatedEntityCtlr[$stateParams.parentEntity][$stateParams.entity];
                            if (config && config.secundaryEntityCreatePartial) {
                                url = config.secundaryEntityCreatePartial;
                            } else {
                                url += ($stateParams.parentEntity + '_create' + $stateParams.entity + '.html');
                            }
                            return url;
                        },
                        data: {
                            ncyBreadcrumbLabel: 'Create new {{entityTypeLabel}}'
                        },
                        controllerProvider: function($stateParams) {
                            var entity = $stateParams.entity;

                            // get the navigation configuration 
                            // and copy config into $stateParams to pass it to ctlr
                            var config = ctlrConfig.relatedEntityCtlr[$stateParams.parentEntity][$stateParams.entity];
                            $stateParams.ctlrConfig = config;

                            // get the entity configuration
                            var config = ctlrConfig.entityCtlr[entity];
                            // check if we use the generic entity controller
                            if (config.genericCtlr) {
                                // yes, this entity is configured for using the generic ctlr
                                $stateParams.ctlrConfig = config;
                                return 'EntityCtlr';
                            }
                            // Otherwise, use the cistom ctlr which by convention, has a name that must be: entity with first letter in capital + Ctlr
                            // ex for groupaccounts: 'GroupaccountCtlr'
                            var entity = $stateParams.entity;
                            var ctrl = entity.substring(0, 1).toUpperCase() + entity.substring(1, entity.length) + 'Ctlr';
                            return ctrl;
                        }
                    })
                    /*
                                  // by convention, ctlr name should be: entity with first letter in capital + Ctlr
                                            // ex for groupaccounts: 'GroupaccountCtlr'
                                            var entity = $stateParams.entity;
                                            var ctrl = entity.substring(0, 1).toUpperCase() + entity.substring(1, entity.length) + 'Ctlr';
                                            return ctrl;
                                        }
                                    })
                         */

                // sub states for related entities
                .state('entity.relatedentity', {
                    url: '/relatedentity/{relatedentity}',
                    templateUrl: function($stateParams) {
                        // by convention, tpl name should be: <entity>_<relatedentity> + list.html
                        // ex for useraccounts of groupaccounts: 'entity_relatedentitylist.tpl.html'
                        var url,
                            entity = $stateParams.entity,
                            relatedentity = $stateParams.relatedentity;
                        var config = ctlrConfig.relatedEntityCtlr[entity][relatedentity];
                        if (config && config.partial) {
                            return 'modules/browser/partials/' + config.partial;
                        } else {
                            url = 'modules/browser/partials/' + $stateParams.entity + '_' + $stateParams.relatedentity + 'list.html';
                        }
                        return url;
                    },
                    data: {
                        ncyBreadcrumbLabel: '{{relatedentityTypeLabel}}s'
                    },
                    controllerProvider: function($stateParams) {
                        // by convention, ctlr name should be: entity with first letter in capital + ListCtlr
                        // ex for useraccounst of groupaccounts: 'Groupaccount_useraccountListCtlr'
                        var ctlr,
                            entity = $stateParams.entity,
                            relatedentity = $stateParams.relatedentity;

                        ctlr = 'SecundaryEntityListCtlr'; // force
                        var config = ctlrConfig.relatedEntityCtlr[entity];
                        if (config) {
                            config = config[relatedentity];
                            if (config) {
                                config.entity = entity;
                                config.relatedentity = relatedentity;
                                $stateParams.ctlrConfig = config;
                                if (config.controller) {
                                    ctlr = config.controller;
                                }
                            }
                        }
                        return ctlr;
                    }
                });
                dialogsProvider.useBackdrop('static');
                dialogsProvider.useEscClose(false);
                dialogsProvider.useCopy(false);
                dialogsProvider.setSize('sm');
            }
        )
        .run(function(appContext) {

            var entity2resource = appContext.get('entity2resource', {});
            _.defaults(entity2resource, {
                useraccount: {
                    restResource: "useraccounts",
                    label: "User Account"
                },
                groupaccount: {
                    restResource: "groupaccounts",
                    label: "User Group"
                },
                role: {
                    restResource: "roles",
                    label: "Role"
                },
                permission: {
                    restResource: "permissions",
                    label: "Permission"
                },
                ace: {
                    restResource: "aces",
                    label: "Access Control Element"
                },

                site: {
                    restResource: "sites",
                    label: "Site"
                },
                team: {
                    restResource: "teams",
                    label: "Team"
                },
                skill: {
                    restResource: "skills",
                    label: "Skill"
                },
                employee: {
                    restResource: "employees",
                    label: "Employee"
                },
                absencetype: {
                    restResource: "absencetypes",
                    label: "Absence Type"
                },
                ciavailability: {
                    restResource: "ciavailability",
                    label: "CI Availability"
                },
                cdavailability: {
                    restResource: "cdavailability",
                    label: "CD Availability"
                },
                schedule: {
                    restResource: "schedules",
                    label: "Schedule"
                },
                shiftstructure: {
                    restResource: "shiftstructures",
                    label: "Shift Structure"
                },
                shiftreq: {
                    restResource: "shiftreqs",
                    label: "Shift Requirement"
                },
                shift: {
                    restResource: "shifts",
                    label: "Shift"
                },
                shifttype: {
                    restResource: "shifttypes",
                    label: "Shift Type"
                },
                change: {
                    restResource: "changes",
                    label: "Change"
                },
                contract: {
                    restResource: "contracts",
                    label: "Contract"
                },
                sitecontract: {
                    restResource: "contracts",
                    label: "Site Contract"
                },
                teamcontract: {
                    restResource: "contracts",
                    label: "Team Contract"
                },
                employeecontract: {
                    restResource: "contracts",
                    label: "Employee Contract"
                },
                contractline: {
                    restResource: "contractlines",
                    label: "Contract Line"
                },
                appserver: {
                    restResource: "appservers",
                    label: "App Server"
                },
                engine: {
                    restResource: "engines",
                    label: "Engine"
                },
                session: {
                    restResource: "sessions",
                    label: "Session"
                }
            });
        });


    var ctlrConfig = {
        entityListCtlr: { // config for Management type controllers, which display a list of toplevel homogeneous  entities
            // an entry  with genericCtlr = true indicates the Generic 'EntityListCtlr' controller will be used for that listing instances of that entity
            groupaccount: {
                genericCtlr: true,
                viewPermissions: ['Account.View'],
                createPermissions: ['Account.Mgmt'],
                editPermissions: ['Account.Mgmt'],
                deletePermissions: ['Account.Mgmt'],
                partial: 'entitylist.tpl.html',
                // TODO: add support for renderers on columns basd on type
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'description'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/usergroup.png'
            },
            useraccount: {
                genericCtlr: true,
                viewPermissions: ['Account.View'],
                createPermissions: ['Account.Mgmt'],
                editPermissions: ['Account.Mgmt'],
                deletePermissions: ['Account.Mgmt'],
                partial: 'useraccountlist.html',
                // TODO: add support for renderers on columns basd on type
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'login',
                        label: 'Login',
                        sortable: true
                    }, {
                        name: 'email',
                        label: 'Email',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'description', 'login', 'email'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/user.png'
            },
            role: {
                genericCtlr: true,
                viewPermissions: ['Account.View', 'Role.View'],
                createPermissions: ['Role.Mgmt'],
                editPermissions: ['Role.Mgmt'],
                deletePermissions: ['Role.Mgmt'],
                partial: 'entitylist.tpl.html',
                // TODO: add support for renderers on columns basd on type
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'description'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/role.png'
            },
            permission: {
                genericCtlr: true,
                viewPermissions: ['Account.View', 'Role.View'],
                createPermissions: [],
                editPermissions: [],
                deletePermissions: [],
                partial: 'entitylist.tpl.html',
                // TODO: add support for renderers on columns basd on type
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'type',
                        label: 'Type',
                        sortable: true
                    }, {
                        name: 'scope',
                        label: 'Scope',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'type', 'scope'], // card attributes
                entityDrillDown: false, // enable link to show entity details
                entityDelete: false, // enable element delete btn
                entityAdd: false, // enable element add btn
                entityIcon: './img/glyphicons/png/permission.png'
            },
            skill: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View'],
                createPermissions: ['OrganizationProfile.Mgmt'],
                editPermissions: ['OrganizationProfile.Mgmt'],
                deletePermissions: ['OrganizationProfile.Mgmt'],
                partial: 'entitylist.tpl.html',
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'abbreviation',
                        label: 'Abbreviation',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'abbreviation', 'description'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn          
                entityIcon: './img/glyphicons/png/skill.png'
            },
            site: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View'],
                createPermissions: ['OrganizationProfile.Mgmt'],
                editPermissions: ['OrganizationProfile.Mgmt'],
                deletePermissions: ['OrganizationProfile.Mgmt'],
                partial: 'entitylist.tpl.html',
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'firstDayOfWeek',
                        label: 'First Day Of Week',
                        sortable: true
                    }, {
                        name: 'timeZone',
                        label: 'Time Zone',
                        sortable: true
                    }, {
                        name: 'weekendDefinition',
                        label: 'Weekend Definition',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'description'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn        
                entityIcon: './img/glyphicons/png/site.png'
            },
            team: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View'],
                createPermissions: ['OrganizationProfile.Mgmt'],
                editPermissions: ['OrganizationProfile.Mgmt'],
                deletePermissions: ['OrganizationProfile.Mgmt'],
                partial: 'entitylist.tpl.html',
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'abbreviation',
                        label: 'Abbreviation',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'description'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn 
                entityIcon: './img/glyphicons/png/team.png'
            },
            employee: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View', 'Employee.View'],
                createPermissions: ['OrganizationProfile.Mgmt', 'Employee.Mgmt'],
                editPermissions: ['OrganizationProfile.Mgmt', 'Employee.Mgmt'],
                deletePermissions: ['OrganizationProfile.Mgmt', 'Employee.Mgmt'],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'firstName',
                        label: 'First Name',
                        sortable: true
                    }, {
                        name: 'lastName',
                        label: 'Last Name',
                        sortable: true
                    }, {
                        name: 'notificationEmail',
                        label: 'Notification Email',
                        sortable: true
                    }, {
                        name: 'notificationSmsNumber',
                        label: 'Notification SmsNumber',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['firstName', 'lastName', 'employeeIdentifier'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/user.png'
            },
            schedule: {
                genericCtlr: true,
                viewPermissions: ['Demand.View'],
                createPermissions: ['Demand.Mgmt'],
                editPermissions: ['Demand.Mgmt'],
                deletePermissions: ['Demand.Mgmt'],
                partial: 'entitylist.tpl.html',
                // TODO: add support for renderers on columns basd on type
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'startDate',
                        label: 'Start Date',
                        dateFilter: 'yyyy-MM-dd',
                        sortable: true
                    }, {
                        name: 'endDate',
                        label: 'End Date',
                        dateFilter: 'yyyy-MM-dd',
                        sortable: true
                    }, {
                        name: 'status',
                        label: 'Status',
                        sortable: true
                    }, {
                        name: 'state',
                        label: 'Exe State',
                        sortable: true
                    }, {
                        name: 'executionStartDate',
                        label: 'Last Run',
                        dateFilter: 'yyyy-MM-dd HH:mm',
                        sortable: true
                    }, {
                        name: 'completion',
                        label: 'Completion',
                        sortable: true
                    }, {
                        name: 'engineLabel',
                        label: 'Engine',
                        sortable: true
                    }, {
                        name: 'scheduleGroupId',
                        label: 'GroupId',
                        sortable: false
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: false
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'type', 'startDate'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/schedule.png'
            },
            shiftstructure: {
                genericCtlr: true,
                viewPermissions: ['Demand.View'],
                createPermissions: ['Demand.Mgmt'],
                editPermissions: ['Demand.Mgmt'],
                deletePermissions: ['Demand.Mgmt'],
                partial: 'entitylist.tpl.html',
                // TODO: add support for renderers on columns basd on type
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'startDate',
                        dateFilter: 'yyyy-MM-dd',
                        label: 'Start Date',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: false
                },
                //card:['startDate'],       // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/shiftstructure.png'
            },
            shiftreq: {
                genericCtlr: true,
                viewPermissions: ['Demand.View'],
                createPermissions: ['Demand.Mgmt'],
                editPermissions: ['Demand.Mgmt'],
                deletePermissions: ['Demand.Mgmt'],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'startTime',
                        label: 'Start Time',
                        sortable: true
                    }, {
                        name: 'endTime',
                        label: 'End Time',
                        sortable: true
                    }, {
                        name: 'dayIndex',
                        label: 'Day Index',
                        sortable: true
                    }, {
                        name: 'night',
                        label: 'is Night',
                        sortable: true
                    }, {
                        name: 'excess',
                        label: 'is Excess',
                        sortable: true
                    }, {
                        name: 'shiftTypeId',
                        label: 'ShiftType Id',
                        sortable: true
                    }, {
                        name: 'employeeCount',
                        label: 'Employees',
                        sortable: true
                    }, {
                        name: 'skillId',
                        label: 'Skill Id',
                        sortable: true
                    }, {
                        name: 'skillProficiencyLevel',
                        label: 'Skill level',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: false
                },
                //card:['startDate'],       // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/shiftreq.png'
            },
            shifttype: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View', 'Employee.View'],
                createPermissions: ['OrganizationProfile.Mgmt'],
                editPermissions: ['OrganizationProfile.Mgmt'],
                deletePermissions: ['OrganizationProfile.Mgmt'],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'shiftLength',
                        label: 'Length',
                        sortable: true
                    }, {
                        name: 'paidHours',
                        label: 'Paid Hours',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'shiftLength', 'paidHours'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn
                entityIcon: './img/glyphicons/png/unknown.png'
            },
            absencetype: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View', 'Employee.View'],
                createPermissions: ['OrganizationProfile.Mgmt'],
                editPermissions: ['OrganizationProfile.Mgmt'],
                deletePermissions: ['OrganizationProfile.Mgmt'],
                partial: 'entitylist.tpl.html',
                // by default type is assumed to be string and value rendered as is
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'hoursToDeduct',
                        label: 'Hours  to deduct',
                        sortable: true
                    }, {
                        name: 'description',
                        label: 'Description',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: true
                },
                card: ['name', 'hoursToDeduct', 'description'], // card attributes
                entityDrillDown: true, // enable link to show entity details
                entityDelete: true, // enable element delete btn
                entityAdd: true, // enable element add btn 
                entityIcon: './img/glyphicons/png/unknown.png'
            },
            contract: {
                genericCtlr: true,
                viewPermissions: ['OrganizationProfile.View', 'Employee.View'],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'clName',
                        label: 'Type',
                        sortable: false
                    }, {
                        name: 'defaultContract',
                        label: 'Default Contract',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: false
                },
                entityDrillDown: true,   // enable link to show entity details
                entityDelete: false,     // enable element delete btn
                entityAdd: false,        // enable element add btn
                entityIcon: './img/glyphicons/png/contract.png'
            },
            appserver: {
                genericCtlr: true,
                viewPermissions: [],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'ip',
                        label: 'IP@',
                        sortable: false
                    }, {
                        name: 'updated',
                        label: 'Last heartbit',
                        dateFilter: 'yyyy-MM-dd HH:mm',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: false
                },
                entityDrillDown: false,   // enable link to show entity details
                entityDelete: false,     // enable element delete btn
                entityAdd: false,        // enable element add btn
                entityIcon: './img/glyphicons/png/appserver.png'
            },
            engine: {
                genericCtlr: true,
                viewPermissions: [],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'name',
                        label: 'Name',
                        sortable: true
                    }, {
                        name: 'ip',
                        label: 'IP@',
                        sortable: false
                    }, {
                        name: 'updated',
                        label: 'Last heartbit',
                        dateFilter: 'yyyy-MM-dd HH:mm',
                        sortable: true
                    }, {
                        name: 'id',
                        label: 'Id',
                        sortable: true
                    }
                ],
                views: {
                    list: true,
                    card: false
                },
                entityDrillDown: false,   // enable link to show entity details
                entityDelete: false,     // enable element delete btn
                entityAdd: false,        // enable element add btn
                entityIcon: './img/glyphicons/png/engine.png'
            },

            session: {
                genericCtlr: true,
                viewPermissions: [],
                partial: 'entitylist.tpl.html',
                columns: [ // grid columns
                    {
                        name: 'userName',
                        label: 'User',
                        sortable: false
                    }, {
                        name: 'impersonatingUserName',
                        label: 'Impersonating User',
                        sortable: false
                    }, {
                        name: 'ip',
                        label: 'IP@ (NA)',
                        sortable: false
                    }, {
                        name: 'started',
                        label: 'Started (NA)',
                        dateFilter: 'yyyy-MM-dd HH:mm',
                        sortable: false
                    }, {
                        name: 'token',
                        label: 'Token',
                        sortable: false
                    }, {
                        name: 'lang',
                        label: 'Language',
                        sortable: false
                    },
                ],
                views: {
                    list: true,
                    card: false
                },
                entityDrillDown: false,     // enable link to show entity details
                entityDelete: false,        // enable element delete btn
                entityAdd: false,           // enable element add btn
                entityIcon: './img/glyphicons/png/unknown.png'
            }
        },

        entityCtlr: { // config for Entity type controllers, which allow to Create and/or Edit an entity
            groupaccount: {
                genericCtlr: true,
                tabs: [{
                    name: 'useraccount',
                    label: 'Users',
                    icon: './img/glyphicons/png/user.png'
                }, {
                    name: 'role',
                    label: 'Roles',
                    icon: './img/glyphicons/png/role.png'
                }, {
                    name: 'acl',
                    label: 'Access Control Lists (NA)',
                    icon: './img/glyphicons/png/unknown.png'
                }],
                defaultRelatedEntity: 'useraccount'
            },
            useraccount: {
                genericCtlr: true,
                tabs: [{
                    name: 'groupaccount',
                    label: 'Groups',
                    icon: './img/glyphicons/png/usergroup.png'
                }, {
                    name: 'role',
                    label: 'Roles',
                    icon: './img/glyphicons/png/role.png'
                }, {
                    name: 'acl',
                    label: 'Access Control Lists (NA)',
                    icon: './img/glyphicons/png/unknown.png'
                }],
                defaultRelatedEntity: 'groupaccount'
            },
            role: {
                genericCtlr: true,
                tabs: [{
                    name: 'permission',
                    label: 'Permissions',
                    icon: './img/glyphicons/png/permission.png'
                }, {
                    name: 'acl',
                    label: 'Access Control Lists (NA)',
                    icon: './img/glyphicons/png/unknown.png'
                }],
                defaultRelatedEntity: 'permission'
            },
            permission: {
                genericCtlr: false, // false as the default ctlr allows actions (such as delete entities) which are not permitted for permissions
                defaultRelatedEntity: ''
            },
            skill: {
                genericCtlr: true,
                tabs: [{
                    name: 'site',
                    label: 'Sites (NA)',
                    icon: './img/glyphicons/png/site.png'
                }, {
                    name: 'team',
                    label: 'Teams (NA)',
                    icon: './img/glyphicons/png/team.png'
                }, {
                    name: 'employee',
                    label: 'Employees (NA)',
                    icon: './img/glyphicons/png/user.png'
                }],
                defaultRelatedEntity: 'site'
            },
            site: {
                genericCtlr: false,
                tabs: [{
                    name: 'team',
                    label: 'Teams',
                    icon: './img/glyphicons/png/user.png'
                }, {
                    name: 'skill',
                    label: 'Skills',
                    icon: './img/glyphicons/png/skill.png'
                }, {
                    name: 'employee',
                    label: 'Employees (NA)',
                    icon: './img/glyphicons/png/user.png'
                }],
                defaultRelatedEntity: 'team'
            },
            team: {
                genericCtlr: false,
                tabs: [{
                    name: 'employee',
                    label: 'Employees',
                    icon: './img/glyphicons/png/user.png'
                }, {
                    name: 'skill',
                    label: 'Skills',
                    icon: './img/glyphicons/png/skill.png'
                }, {
                    name: 'schedule',
                    label: '^ Schedules (NA)',
                    icon: './img/glyphicons/png/schedule.png'
                }, {
                    name: 'shiftstructure',
                    label: 'Shift Structures',
                    icon: './img/glyphicons/png/shiftstructure.png'
                }],
                defaultRelatedEntity: 'employee'
            },
            employee: {
                genericCtlr: false,
                tabs: [{
                    name: 'team',
                    label: 'Teams',
                    icon: './img/glyphicons/png/team.png'
                }, {
                    name: 'skill',
                    label: 'Skills',
                    icon: './img/glyphicons/png/skill.png'
                }, {
                    name: 'ciavailability',
                    label: 'CI Availability (NA)',
                    icon: './img/glyphicons/png/unknown.png'
                }, {
                    name: 'cdavailability',
                    label: 'CD Availability (NA)',
                    icon: './img/glyphicons/png/unknown.png'
                }],
                defaultRelatedEntity: 'team'
            },
            schedule: {
                genericCtlr: false,
                tabs: [{
                    name: 'team',
                    label: 'Teams',
                    icon: './img/glyphicons/png/team.png'
                }, {
                    name: 'shiftstructure',
                    label: 'Shift Structures',
                    icon: './img/glyphicons/png/shiftstructure.png'
                }, {
                    name: 'shift',
                    label: 'Shifts & Assignments',
                    icon: './img/glyphicons/png/shift.png'
                }, {
                    name: 'shiftsgrid',
                    label: 'Shifts',
                    icon: './img/glyphicons/png/shift.png'
                    
                }, {
                    name: 'change',
                    label: 'Changes',
                    icon: './img/glyphicons/png/change.png'
                    
                }, {
                    name: 'scheduleoptions',
                    label: 'Schedule options',
                    icon: './img/glyphicons/png/unknown.png'

                }],
                defaultRelatedEntity: 'shift'
            },
            shiftstructure: {
                genericCtlr: false,
                tabs: [{
                    name: 'schedules',
                    label: '^ Schedules (NA)',
                    icon: './img/glyphicons/png/schedule.png'
                }, {
                    name: 'shiftreq',
                    label: 'Shift Requirements',
                    icon: './img/glyphicons/png/shiftreq.png'
                }],
                defaultRelatedEntity: 'schedules'
            },
            shiftreq: {
                genericCtlr: false,
                defaultRelatedEntity: ''
            },
            shifttype: {
                genericCtlr: true,
                defaultRelatedEntity: ''
            },
            absencetype: {
                genericCtlr: true,
                defaultRelatedEntity: ''
            },
            ciavailability: {
                genericCtlr: false,
                defaultRelatedEntity: ''
            },
            cdavailability: {
                genericCtlr: false,
                defaultRelatedEntity: ''
            },
            // see after this variable declaration for the extension to employee, team, site, contract
            contract: {
                genericCtlr: true,                
                tabs: [{
                    name: 'contractlines',
                    label: 'Contract Lines',
                    icon: './img/glyphicons/png/contract.png'
                }],
                defaultRelatedEntity: 'contractline'
            },
            contractline: {
                genericCtlr: false,                
                defaultRelatedEntity: ''
            }
        },

        relatedEntityCtlr: { // config for RelatedEntity type controllers (invoked from EntityCtlr s)

            groupaccount: { // groupaccount navigation
                useraccount: {
                    viewPermissions: ['Account.View'],
                    addPermissions: ['Account.Mgmt'],
                    removePermissions: ['Account.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'login',
                            label: 'Login',
                            sortable: true
                        }, {
                            name: 'email',
                            label: 'Email',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/user.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getGroupMembers',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedGroupMembers',
                    dataServiceAssociateEntityAPI: 'addMemberToGroup',
                    dataServiceUnassociateEntityAPI: 'removeMemberFromGroup',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                },
                role: {
                    viewPermissions: ['Account.View', 'Role.View'],
                    addPermissions: ['Account.Mgmt'],
                    removePermissions: ['Account.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/role.png',
                    dataServiceResource: 'groupaccounts',
                    dataServiceGetAssociatedEntitiesAPI: 'getAccountRoles',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedAccountRoles',
                    dataServiceAssociateEntityAPI: 'addRoleToAccount',
                    dataServiceUnassociateEntityAPI: 'removeRoleFromAccount',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                }
            },

            useraccount: { // useraccount navigation
                groupaccount: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    viewPermissions: ['Account.View'],
                    addPermissions: [],
                    removePermissions: [],
                    type: 'addremove',
                    dataServiceGetAssociatedEntitiesAPI: 'getUserGroups',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/usergroup.png'
                },
                role: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    viewPermissions: ['Account.View', 'Role.View'],
                    addPermissions: ['Account.Mgmt'],
                    removePermissions: ['Account.Mgmt'],
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/role.png',
                    dataServiceResource: 'useraccounts',
                    dataServiceGetAssociatedEntitiesAPI: 'getAccountRoles',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedAccountRoles',
                    dataServiceAssociateEntityAPI: 'addRoleToAccount',
                    dataServiceUnassociateEntityAPI: 'removeRoleFromAccount',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                }
            },

            role: { // role navigation
                permission: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    //          viewPermissions: ['Role.View'],
                    addPermissions: ['Role.Mgmt'],
                    removePermissions: ['Role.Mgmt'],
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'type',
                            label: 'Type',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'scope',
                            label: 'Scope',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/permission.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getRolePermissions',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedRolePermissions',
                    dataServiceAssociateEntityAPI: 'addPermissionToRole',
                    dataServiceUnassociateEntityAPI: 'removePermissionFromRole',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                }
            },

            site: { // site navigation
                team: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/team.png',
                    viewPermissions: ['Demand.View'],
                    createPermissions: ['Demand.Mgmt'],
                    deletePermissions: ['Demand.Mgmt'],
                    dataServiceGetAssociatedEntitiesAPI: 'getSiteTeams',
                    dataServiceCreateAssociatedEntityAPI: 'createSiteTeam',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedSiteTeams',
                    dataServiceDeleteAssociatedEntityAPI: 'deleteSiteTeam',
                    secundaryEntityCreatePartial: 'modules/browser/partials/site_createteam.html'
                },
                skill: {
                    viewPermissions: ['OrganizationProfile.View'],
                    addPermissions: ['OrganizationProfile.Mgmt'],
                    removePermissions: ['OrganizationProfile.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'abbreviation',
                            label: 'Abbreviation',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/skill.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getSiteSkills',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedSiteSkills',
                    dataServiceAssociateEntityAPI: 'addSiteSkill',
                    dataServiceUnassociateEntityAPI: 'removeSkillFromSite',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                }
            },

            team: { // team navigation
                employee: {
                    viewPermissions: ['OrganizationProfile.View', 'Employee.View'],
                    addPermissions: ['OrganizationProfile.Mgmt'],
                    removePermissions: ['OrganizationProfile.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            dto: 'employeeSummaryDto',
                            name: 'firstName',
                            label: 'First Name',
                            sortable: true
                        }, {
                            dto: 'employeeSummaryDto',
                            name: 'lastName',
                            label: 'Last Name',
                            sortable: true
                        }, {
                            name: 'isFloating',
                            label: 'Floating',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'isHomeTeam',
                            label: 'Home Team',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'isSchedulable',
                            label: 'Schedulable',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            //                    dto: 'employeeNamesDto',
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        },
                    ],
                    relatedEntityIcon: './img/glyphicons/png/user.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getTeamEmployees',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedTeamEmployees',
                    dataServiceAssociateEntityAPI: 'addEmployeeToTeam',
                    dataServiceUnassociateEntityAPI: 'removeEmployeeFromTeam',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                },
                skill: {
                    viewPermissions: ['OrganizationProfile.View'],
                    addPermissions: ['OrganizationProfile.Mgmt'],
                    removePermissions: ['OrganizationProfile.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'abbreviation',
                            label: 'Abbreviation',
                            sortable: true
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        },
                    ],
                    relatedEntityIcon: './img/glyphicons/png/team.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getTeamSkills',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedTeamSkills',
                    dataServiceAssociateEntityAPI: 'addTeamSkill',
                    dataServiceUnassociateEntityAPI: 'removeSkillFromTeam',
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                },
                schedule: {
                    viewPermissions: ['Demand.View'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'startDate',
                            label: 'Start Date',
                            dateFilter: 'yyyy-MM-dd HH:mm',
                            sortable: true
                        }, {
                            name: 'type',
                            label: 'Type',
                            sortable: true
                        }, {
                            name: 'state',
                            label: 'State',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/schedule.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getTeamSchedules'
                },
                shiftstructure: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'startDate',
                            label: 'Start date',
                            dateFilter: 'yyyy-MM-dd HH:mm',
                            sortable: false
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: false
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/shiftstructure.png',
                    viewPermissions: ['Demand.View'],
                    createPermissions: ['Demand.Mgmt'],
                    deletePermissions: ['Demand.Mgmt'],
                    secundaryEntity: true,
                    dataServiceGetAssociatedEntitiesAPI: 'getShiftStructures',
                    dataServiceGetAssociatedEntityAPI: 'getShiftStructure',
                    dataServiceCreateAssociatedEntityAPI: 'createShiftStructure',
                    dataServiceDeleteAssociatedEntityAPI: 'deleteShiftStructure',
                    secundaryEntityCreatePartial: 'modules/browser/partials/team_createshiftstructure.html'
                }
            },

            employee: { // employee navigation
                team: {
                    //          viewPermissions: ['OrganizationProfile.View'],
                    addPermissions: ['OrganizationProfile.Mgmt'],
                    removePermissions: ['OrganizationProfile.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            dto: 'teamDto',
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'isHomeTeam',
                            label: 'Home Team',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'isFloating',
                            label: 'Floating',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'isSchedulable',
                            label: 'Schedulable',
                            sortable: true,
                            showInAddModal: false
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/user.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getEmployeeTeams',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedEmployeeTeams',
                    dataServiceAssociateEntityAPI: 'addTeamToEmployee',
                    dataServiceUnassociateEntityAPI: 'removeTeamFromEmployee',
                    unassociateIdAttribute: {
                        o: 'teamDto',
                        id: 'id'
                    },
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html'
                },
                skill: {
                    //          viewPermissions: ['OrganizationProfile.View'],
                    addPermissions: ['OrganizationProfile.Mgmt'],
                    removePermissions: ['OrganizationProfile.Mgmt'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            dto: 'skillDto',
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            dto: 'skillDto',
                            name: 'abbreviation',
                            label: 'Abbreviation',
                            sortable: true
                        }, {
                            name: 'isPrimarySkill',
                            label: 'Primary',
                            sortable: true
                        }, {
                            name: 'skillScore',
                            label: 'Level',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/skill.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getEmployeeSkills',
                    dataServiceGetUnassociatedEntitiesAPI: 'getUnassociatedEmployeeSkills',
                    dataServiceAssociateEntityAPI: 'addSkillToEmployee',
                    dataServiceUnassociateEntityAPI: 'removeSkillFromEmployee',
                    unassociateIdAttribute: {
                        o: 'skillDto',
                        id: 'id'
                    },
                    secundaryEntityAddPartial: 'modules/browser/partials/entity_addrelatedentity.tpl.html' /* a bit ugly but same template as for team */
                },
                ciavailability: {
                    //          viewPermissions: ['OrganizationProfile.View'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        
                        {
                            name: 'availabilityType',
                            label: 'Type',
                            sortable: true
                        },
                        {
                            name: 'dayOfTheWeek',
                            label: 'Day of week',
                            sortable: true
                        }, 
                        {
                            name: 'startTime',
                            label: 'At',
                            dateFilter: 'HH:mm',
                            sortable: true
                        },{
                            name: 'durationInMinutes',
                            label: 'For (mins)',
                            sortable: true
                        }, {
                            name: 'absenceTypeId',
                            label: 'Absence Type',
                            sortable: true
                        }, {
                            name: 'reason',
                            label: 'Absence reason',
                            sortable: true
                        }, {
                            name: 'startDate',
                            label: 'From (date)',
                            dateFilter: 'yyyy-MM-dd',
                            sortable: true
                        }, {
                            name: 'endDate',
                            label: 'To (date)',
                            dateFilter: 'yyyy-MM-dd',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/unknown.png',
                    viewPermissions: ['Demand.View'],
                    createPermissions: ['Demand.Mgmt'],
                    deletePermissions: ['Demand.Mgmt'],
                    secundaryEntity: true,
                    dataServiceGetAssociatedEntitiesAPI: 'getEmployeeCIAvailabilities',
                    dataServiceGetAssociatedEntityAPI: 'getEmployeeCIAvailability',
                    dataServiceCreateAssociatedEntityAPI: 'createEmployeeCIAvailability',
                    dataServiceUpdateAssociatedEntityAPI: 'updateEmployeeCIAvailability',
                    dataServiceDeleteAssociatedEntityAPI: 'deleteEmployeeCIAvailability',
                    secundaryEntityCreatePartial: 'modules/browser/partials/employee_createciavailability.html'
                },
                cdavailability: {
                    //          viewPermissions: ['OrganizationProfile.View'],
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        
                        {
                            name: 'availabilityType',
                            label: 'Type',
                            sortable: true
                        },{
                            name: 'startDate',
                            label: 'On (date)',
                            dateFilter: 'yyyy-MM-dd',
                            sortable: true
                        },{
                            name: 'startTime',
                            label: 'At',
                            dateFilter: 'HH:mm',
                            sortable: true
                        },{
                            name: 'durationInMinutes',
                            label: 'For (mins)',
                            sortable: true
                        }, {
                            name: 'absenceTypeId',
                            label: 'Absence Type',
                            sortable: true
                        }, {
                            name: 'reason',
                            label: 'Absence reason',
                            sortable: true
                        },  {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/unknown.png',
                    viewPermissions: ['Demand.View'],
                    createPermissions: ['Demand.Mgmt'],
                    deletePermissions: ['Demand.Mgmt'],
                    secundaryEntity: true,
                    dataServiceGetAssociatedEntitiesAPI: 'getEmployeeCDAvailabilities',
                    dataServiceGetAssociatedEntityAPI: 'getEmployeeCDAvailability',
                    dataServiceCreateAssociatedEntityAPI: 'createEmployeeCDAvailability',
                    dataServiceUpdateAssociatedEntityAPI: 'updateEmployeeCDAvailability',
                    dataServiceDeleteAssociatedEntityAPI: 'deleteEmployeeCDAvailability',
                    secundaryEntityCreatePartial: 'modules/browser/partials/employee_createcdavailability.html'
                }
            },

            shiftstructure: {              
                team: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: false
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: false
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/team.png',
                    viewPermissions: ['Demand.View'],
                    secundaryEntity: true,
                    dataServiceGetAssociatedEntitiesAPI: 'getShiftStructureTeams'
                },
                shiftreq: {
//                    partial: 'entity_relatedentitylist.tpl.html',
//                    columns: [ // grid columns
//                        {
//                            name: 'dayIndex',
//                            label: 'Day Index',
//                            sortable: true
//                        }, {
//                            name: 'startTime',
//                            label: 'Start Time',
//                            dateFilter: 'HH:mm',
//                            sortable: true
//                        }, {
//                            name: 'durationInMins',
//                            label: 'Duration (mins)',
//                            sortable: true
//                        }, {
//                            name: 'employeeCount',
//                            label: 'Employees',
//                            sortable: true
//                        }, {
//                            name: 'id',
//                            label: 'Id',
//                            sortable: true
//                        }
//                    ],
//                    relatedEntityIcon: './img/glyphicons/png/shiftreq.png',
                    viewPermissions: ['Demand.View'],
                    createPermissions: ['Demand.Mgmt'],
                    deletePermissions: ['Demand.Mgmt'],
                    secundaryEntity: true,
                    dataServiceGetAssociatedEntityAPI: 'getShiftReq',
                    dataServiceGetAssociatedEntitiesAPI: 'getShiftReqs',
                    dataServiceCreateAssociatedEntityAPI: 'createShiftReq',
                    dataServiceUpdateAssociatedEntityAPI: 'updateShiftReq',
                    dataServiceDeleteAssociatedEntityAPI: 'deleteShiftReq',
                    secundaryEntityCreatePartial: 'modules/browser/partials/shiftreq.html'
                }
            },

            schedule: { // schedule navigation
                team: {
                    viewPermissions: ['Demand.View'],
                    dataServiceGetAssociatedEntitiesAPI: 'getScheduleTeams',
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'name',
                            label: 'Name',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/team.png',
                },
                shiftstructure: {
                    viewPermissions: ['Demand.View'],
                    dataServiceGetAssociatedEntitiesAPI: 'getScheduleShiftStructures',
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'startDate',
                            label: 'Start Date',
                            dateFilter: 'yyyy-MM-dd HH:mm',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/shiftstructure.png',
                },
                shift: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    viewPermissions: ['Demand.View'],
                    columns: [ // grid columns
                        {
                            name: 'startDateTime',
                            label: 'Start DateTime',
                            dateFilter: 'yyyy-MM-dd HH:mm',
                            sortable: true
                        }, {
                            name: 'endDateTime',
                            label: 'End DateTime',
                            dateFilter: 'HH:mm',
                            sortable: true
                        }, {
                            name: 'employeeName',
                            label: 'Employee',
                            sortable: true
                        }, {
                            name: 'skillName',
                            label: 'Skill',
                            sortable: true
                        }, {
                            name: 'shiftTypeName',
                            label: 'Shift Type',
                            sortable: true
                        }, {
                            name: 'teamName',
                            label: 'Team',
                            sortable: true
                        }, {
                            name: 'employeeId',
                            label: 'Employee Id',
                            sortable: true
                        }, {
                            name: 'scheduleStatus',
                            label: 'Schedule Status',
                            sortable: true
                        }, {
                            name: 'assignmentType',
                            label: 'AssignmentType',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/unknown.png',
                    dataServiceGetAssociatedEntitiesAPI: 'getScheduleShifts'
                },
                shiftsgrid:{
                    partial: 'schedule_shiftsgrid.html',
                    dataServiceGetAssociatedEntitiesAPI: 'getScheduleShifts',
                    controller: 'ShiftsCtlr'
                },
                change: {
                    viewPermissions: ['Demand.View'],
                    dataServiceGetAssociatedEntitiesAPI: 'getScheduleChanges',
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'changeDate',
                            label: 'Change Date',
                            dateFilter: 'yyyy-MM-dd HH:mm',
                            sortable: true
                        }, {
                            name: 'reason',
                            label: 'Reason',
                            sortable: true
                        }, {
                            name: 'category',
                            label: 'Category',
                            sortable: true
                        }, {
                            name: 'type',
                            label: 'Type',
                            sortable: true
                        }, {
                            name: 'changeEmployeeName',
                            label: 'Changed By',
                            sortable: true
                        }, {
                            name: 'employeeAName',
                            label: 'Employee A',
                            sortable: true
                        }, {
                            name: 'employeeBName',
                            label: 'Employee B',
                            sortable: true
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: true
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/unknown.png'
                },
                scheduleoptions:{
                    partial: 'schedule_options.html',
                    controller: 'ScheduleOptionsCtrl'
                }
            },

            employeecontract: {
                contractline: {
                    partial: 'entity_relatedentitylist.tpl.html',
                    columns: [ // grid columns
                        {
                            name: 'contractLineType',
                            label: 'Type',
                            sortable: false
                        }, {
                            name: 'description',
                            label: 'Description',
                            sortable: false
                        }, {
                            name: 'id',
                            label: 'Id',
                            sortable: false
                        }
                    ],
                    relatedEntityIcon: './img/glyphicons/png/contractline.png',
                    viewPermissions: ['OrganizationProfile.View', 'Employee.View'],
                    createPermissions: ['OrganizationProfile.Mgmt', 'Employee.Mgmt'],
                    deletePermissions: ['OrganizationProfile.Mgmt', 'Employee.Mgmt'],
                    secundaryEntity: true,
                    dataServiceGetAssociatedEntitiesAPI: 'getContractLines',
                    dataServiceGetAssociatedEntityAPI: 'getContractLine',
                    dataServiceCreateAssociatedEntityAPI: 'createContractLine',
                    dataServiceDeleteAssociatedEntityAPI: 'deleteContractLine',
                    secundaryEntityCreatePartial: 'modules/browser/partials/contract_createcontractline.html'
                }
            }
        }
    };

    // duplicate the contract config to site, team, employee contracts
    var entityCtlr = ctlrConfig.entityCtlr;
    entityCtlr.sitecontract = entityCtlr.employeecontract = entityCtlr.employeecontract = entityCtlr.contract;


}());
