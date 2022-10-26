Configure security component in ihub_custom_security_config.properties file:
- auth_validation_url - path to emlogis REST API (change http host and port if needed)
- logging_file_path - path to folder where log file will be saved
- logging_file_name - log file name

To build for testing:
- Add libs from /lib folder to module's dependencies
- build with dependencies

To build for IHub:
- build EmlogisIhubSecurityAdapter.jar without dependencies
- put jar to "{ihub's installation directory}/modules/BIRTiHub/iHub/web/iportal/WEB-INF/lib" catalogue
- change following lines in "{ihub's installation directory}/modules/BIRTiHub/iHub/web/iportal/WEB-INF/web.xml"
 (add param-value for SECURITY_ADAPTER_CLASS):
        <param-name>SECURITY_ADAPTER_CLASS</param-name>
        <param-value>EmlogisIhubSecurityAdapter</param-value>

IMPORTANT!!!
Make sure that IHub instance contains
- "httpclient-{version}.jar"
- "httpcore-{version}.jar"
with {version} >= 4.4.1
Jar files can be found in "{ihub's installation directory}/modules/BIRTiHub/iHub/web/iportal/WEB-INF/lib"