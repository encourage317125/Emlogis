package com.emlogis.rest.resources;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Main Application JAXRS class
 * 
 * assuming the artifact Id in pom.xml = scheduler-server, (<artifactId>scheduler-server</artifactId>)
 * wo further configuration than the default one, this application resources will be accessible at http://localhost:8080/scheduler-server/emlogis/rest/
 * 
 * Ex: 	http://localhost:8080/scheduler-server/emlogis/rest/hello/json for the test helloworld resource
 * 		http://localhost:8080/scheduler-server/emlogis/rest/sessions for the login logout resource
 * 
 * @author 
 *
 */
@ApplicationPath("/emlogis/rest")
public class EmLogisApplication extends Application {}

//public class EmLogisApplication  {}
