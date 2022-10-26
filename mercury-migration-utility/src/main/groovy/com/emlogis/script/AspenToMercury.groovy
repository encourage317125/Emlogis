package com.emlogis.script

import com.emlogis.script.migration.data.ImportMercuryData
import com.emlogis.script.migration.data.ExportAspenData
import com.emlogis.script.migration.data.ImportMercuryData
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import org.apache.log4j.Level
import org.apache.log4j.PropertyConfigurator

@Log4j
/**
 * Created with IntelliJ IDEA.
 * User: rjackson
 * Date: 9/17/14
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */


class AspenToMercuryScript {

    AspenToMercuryScript(args) {
        this.args = args
    }

    static  {
        URL url = this.getClass().getResource("/log4j.groovy")
        def config = new ConfigSlurper().parse(url)
        PropertyConfigurator.configure(config.toProperties())
    }

    def args

    def entityList = ['shiftLengths', 'shiftTypes', 'skills', 'sites', 'absenceTypes', 'employees',
            'contractLines', 'employeeSkills','CDAvailabilty', 'CIAvailabilty', 'teams','teamSkills',
            'shiftstructures','schedules','shiftReqs', 'shifts', 'sitesContractLines', 'notfications']

    def listEntities() {
        println  entityList
    }


    def run() {

        def cli = new CliBuilder(usage: 'migrate_Aspen_to_Mercury [options]' )
        cli.with {
            e(longOpt: 'export', 'Export file/directory', type: String, args:1, required: false)
            i(longOpt: 'import', 'Import file/directory', type: String, args:1, required: false)
            c(longOpt: 'configFile', 'Configuration file', type: String, args:1, required: false)
            v(longOpt: 'verbose', 'Set logging to verbose')
            l(longOpt: 'list-entities', 'List of Entities')
            t(longOpt: 'tenandId', 'Tenant Id', type: String, args:1, required: false)
            x(longOpt: 'exclude', 'entities to exclude lisetd at the end of the command', required: false)
            h(longOpt: 'help', 'Help - Usage Information')
            m(longOpt: 'limit-employees', 'Limit employees')
            s(longOpt: 'schedule-limit', 'earliest schedule start date', type: String, args:1, required: false)
            d(longOpt: 'nugrate-schedule-req-only', 'migrate schedule requirements only')
            k(longOpt: 'hickory', 'Run for Hickory')
        }

        def exportFilePath
        def importFilePath
        File configFile
        def configFilePath
        def tenantId
        def entities = []
        def limitEmployees = false
        def limitSchedule  = false
        def migrateScheduleReqOnly = false
        def hickory = false
        def scheduleLimitDate

        def opt=cli.parse(args)

        if(!opt || opt.h) {
            cli.usage()
            return
        }

        if(!opt || opt.l) {
            listEntities()
            return
        }

        if(!opt.c) {
            println "You must set a configuration File"
            cli.usage()
            return
        }

        if(!opt.e && !opt.i) {
            println "You must choose export and/or import "
            cli.usage()
            return
        }

        if(opt.v) {
            log.setLevel(Level.INFO)
            log.info("Verbose messages requested...")
        }

        if(opt.e){
           exportFilePath =  opt.e
           log.info("The export file is: ${opt.e}")
        }

        if(opt.i){
            importFilePath =  opt.i
            log.info("The import file is: ${opt.i}")
        }

        if(opt.c){
            configFilePath =  opt.c
            log.info("The configuration file is: ${opt.c}")
        }

        if(opt.t){
            tenantId =  opt.t
            log.info("The tenantId is: ${opt.t}")
        }

        if(opt.m) {
            limitEmployees = true
        }

        if(opt.s) {
            limitSchedule = true
            scheduleLimitDate = opt.s
        }

        if(opt.x)  {
          entities = opt.arguments()
        }

        if(opt.d) {
            migrateScheduleReqOnly = true
        }

        if(opt.k) {
            hickory = true
            log.info("Hickory Migration")
        }

        // Read configuration file
        configFile = new File(configFilePath);

        JsonSlurper jsonSlurper = new JsonSlurper()

        def jsonResult = jsonSlurper.parseText(configFile.text)

        def aspenConfig = jsonResult.aspenConfig
        def mercuryConfig = jsonResult.mercuryConfig
        def clientTimeZoneId = mercuryConfig.clientTimeZoneId

        if(exportFilePath) {
            // Export Data from Aspen
            ExportAspenData aspenExport = new ExportAspenData(exportFilePath, aspenConfig, limitEmployees, limitSchedule,
                    scheduleLimitDate, hickory)
            aspenExport.getAspenData()
            aspenExport.saveAspenData()
        }

        if(importFilePath) {
            // Import Aspen Data into Mercury

            // Check tenantId
            if(!tenantId) {
                tenantId = aspenConfig.mercuryConfig.tenantId

                if(!tenantId) {
                    log.error("A tenant Id must be defined as an option orin the configuration file.")
                    cli.usage()
                    return
                }
            }

            ImportMercuryData mercuryImport = new ImportMercuryData(importFilePath, tenantId, mercuryConfig, entities,
                migrateScheduleReqOnly, clientTimeZoneId)

            mercuryImport.getData()
            mercuryImport.importData()
        }
    }
}

def mercuryMigrate = new AspenToMercuryScript(args)

mercuryMigrate.run()

