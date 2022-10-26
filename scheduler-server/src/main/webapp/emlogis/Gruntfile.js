module.exports = function (grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    watch: {
      sass: {
        files: ['modules/**/assets/stylesheets/*.scss', 'public/stylesheets/application.scss'],
        tasks: ['sass:dist', 'autoprefixer', 'exec:pushToServer']
        //tasks: ['sass:dist']
      },
      concatApplication: {
        files: ['angular-init.js', 'modules/**/*.js', 'modules/**/**/*.js'],
        tasks: ['jshint', 'concat:application', 'exec:pushToServer']
        //tasks: ['jshint', 'concat:application']
      },
      concatUtil: {
        files: ['util/*.js', 'util/**/*.js'],
        tasks: ['jshint', 'concat:util', 'exec:pushToServer']
        //tasks: ['jshint', 'concat:util']
      },
      css: {
        files: ['libs/**/*.css', 'libs/**/**/*.css'],
        tasks: ['concat:css', 'exec:pushToServer']
        //tasks: ['concat:css']
      },
      html: {
        files: ['modules/**/*.html', 'modules/**/*.html'],
        tasks: ['exec:pushToServer']
        //tasks: ['concat:css']
      }
    },
    sass: {                              // Task
      dist: {                            // Target
        options: {                       // Target options
          style: 'expanded',
          precision: 8
        },
        files: {                         // Dictionary of files
          'public/stylesheets/application.css': 'public/stylesheets/application.scss'       // 'destination': 'source'
        }
      }
    },
    // Javascript files concatenation
    concat: {
      options: {
        separator: grunt.util.linefeed + ';' + grunt.util.linefeed
      },
      // include all 3rd party library files
      libs: {
        src: [
          'libs/jquery/1.11.1/jquery.js',
          'libs/jquery-ui/1.11.2/jquery-ui.min.js',
          'node_modules/angular/angular.js',
          'node_modules/angular-sanitize/angular-sanitize.js',
          'node_modules/angular-animate/angular-animate.js',
          'node_modules/angular-messages/angular-messages.js',
          'libs/angular-ui-router/0.2.11/angular-ui-router.js',
          'libs/angular-ui-bootstrap/0.13.0/ui-bootstrap-tpls.js',
          'libs/bootstrap-switch/js/bootstrap-switch.min.js',
          'libs/angular-ui-ng-grid/3.0.0/js/ui-grid-unstable.js',
          'libs/angular-ui-calendar/moment.min.js',
          'libs/angular-ui-calendar/moment-timezone-with-data.min.js',
          'libs/angular-ui-calendar/fullcalendar.js',
          'libs/angular-ui-calendar/gcal.js',
          'libs/angular-ui-calendar/calendar.js',
          'libs/angular-ui-sortable/0.13.3/sortable.js',
          'libs/angular-google-maps/angular-google-maps.min.js',
          'libs/angular-slider/ng-slider.min.js',
          'libs/angular-storage/ngStorage.js',
          'libs/isteven-multi-select/4.0.0/isteven-multi-select.js',
          'libs/angular-breadcrumb/0.2.3/angular-breadcrumb.js',
          'libs/angular-translate/2.4.2/angular-translate.js',
          'libs/angular-translate/2.4.2/angular-translate-loader-static-files.js',
          'libs/angular-dialog-service/dialogs.min.js',
          'libs/angular-bootstrap-switch/angular-bootstrap-switch.min.js',
          'libs/lodash/lodash.min.js',
          'libs/underscore-string/2.3.0/underscore.string.js',
          'libs/underscore-date/0.0.2/underscore.date.js',
          'libs/bootstrap-datetime-picker/bootstrap-datetimepicker.min.js',
          'libs/scheduler/4.1.0/dhtmlxscheduler.js',
          'libs/scheduler/4.1.0/ext/dhtmlxscheduler_timeline.js',
          'libs/scheduler/4.1.0/ext/dhtmlxscheduler_tooltip.js',
          'libs/angular-load/angular-load.min.js',
          'libs/cryptojs/sha256.js'
        ],
        dest: 'public/js/library.js'
      },
      util: {
        src: [
          'util/*.js',
          'util/**/*.js'
        ],
        dest: 'public/js/util.js'
      },

      //include angular-init and all moduels .js files
      application: {
        src: ['angular-init.js', 'modules/**/*.js'],
        dest: 'public/js/application.js'
      },
      css: {
        options: {
          separator: grunt.util.linefeed + grunt.util.linefeed
        },
        src: [
          'libs/jquery-ui/1.11.2/jquery-ui.min.css',
          'libs/font-awesome/4.2.0/css/font-awesome.css',
          'libs/angular-ui-ng-grid/3.0.0/css/ui-grid-unstable.css',
          'libs/isteven-multi-select/4.0.0/isteven-multi-select.css',
          'libs/angular-dialog-service/dialogs.css',
          'libs/angular-ui-calendar/fullcalendar.css',
          'libs/angular-slider/css/ng-slider.min.css',
          'libs/scheduler/4.1.0/dhtmlxscheduler.css',
          'libs/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css',
          'libs/bootstrap-datetime-picker/bootstrap-datetimepicker.min.css'
        ],
        dest: 'public/stylesheets/library.css'
      }
    },
    uglify: {
      all: {
        files: [{
          expand: true,
          cwd: 'public/js',
          src: '**/*.js',
          dest: 'public/js'
        }]
      }
    },
    jshint: {
      options: {
        laxbreak: true,
        sub: true
      },
      all: ['modules/**/*.js', 'util/**/*.js']
    },
    exec: {
      pushToServer: {
        command: '~/egs_sync.sh'
      }
    },
    autoprefixer: {
      options: {
        browsers: ['last 10 versions', 'ie 9', '> 1%']
      },
      main: {
        src: 'public/stylesheets/application.css',
        dest: 'public/stylesheets/application.css'
      }
    }
  });

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-sass');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-exec');
  grunt.loadNpmTasks('grunt-autoprefixer');


  // Default task(s).
  grunt.registerTask('default', ['sass', 'autoprefixer', 'concat', 'jshint', 'exec', 'watch']);
  grunt.registerTask('dev', ['sass', 'autoprefixer', 'concat', 'jshint', 'watch']);
  //grunt.registerTask('prod', ['sass', 'concat', 'jshint', 'uglify:all']);
  grunt.registerTask('prod', ['sass', 'autoprefixer', 'concat', 'jshint']);


};