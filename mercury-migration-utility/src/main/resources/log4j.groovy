

/**
 * Created by rjackson on 2/2/2015.
 */

// log4j.groovy
log4j {
    rootLogger="INFO, stdout,FILE"
    appender.stdout = "org.apache.log4j.ConsoleAppender"
    appender.'stdout.Target'="System.out"

    appender.'stdout.layout'="org.apache.log4j.PatternLayout"
    appender.'stdout.layout.ConversionPattern'="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"

    // Define the File appender
    appender.'FILE'="org.apache.log4j.FileAppender"

    // Set the name of the file for the client
    def logFile = System.properties['logFile']

    def logDirectory ="C:\\dev\\migration\\logs\\"

    if(logFile ){
        appender.'FILE.File'="${logDirectory}${logFile}.log"
    } else {
        appender.'FILE.File'="${logDirectory}migration_runtime.log"
    }


    // Set the immediate flush to true (default)
    appender.'FILE.ImmediateFlush'="true"

    // Set the threshold to debug mode
    appender.'FILE.Threshold'="debug"

    // Set the append to false, overwrite
    appender.'FILE.Append'="false"

    // Define the layout for file appender
    appender.'FILE.layout'="org.apache.log4j.PatternLayout"
    appender.'FILE.layout.conversionPattern'="%m%n"


}