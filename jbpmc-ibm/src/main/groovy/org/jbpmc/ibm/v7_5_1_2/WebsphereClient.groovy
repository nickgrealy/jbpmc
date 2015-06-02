package org.jbpmc.ibm.v7_5_1_2

import org.jbpmc.ibm.core.WsadminClient

/**
 * <p>
 *     Provides methods to facilitate interacting with a Websphere Application server.
 * </p>
 * <p>
 *     <h3>Example usage</h3>
 *     <pre><code>
wsadmin_exec = '/somepath/wsadmin.bat'
ibmjdk_home = '/somepath/ibm_sdk70'

environments {
    dev {
        dmgr {
            username = 'wasadmin'
            password = 'password'
            hostname = 'server'
            conntype = 'SOAP'
            port = '8880'
        }
    }
}</code></pre>
 * </p>
 */
class WebsphereClient extends WsadminClient {

    WebsphereClient(String environment = System.getProperty('env', 'dev'), Map environmentConfig = [:]) {
        super(loadProperties(environment, environmentConfig))
    }

    List listApplications() {
        println "Listing applications ..."
        runJaclCommand(properties.dmgr, '$AdminApp list').readLines()[2..-1]
    }

    List listCells() {
        println "Listing cells ..."
        runJaclCommand(properties.dmgr, '$AdminConfig list Cell').readLines()[2..-1]
    }

    List listNodes() {
        println "Listing nodes ..."
        runJaclCommand(properties.dmgr, '$AdminConfig list Node').readLines()[2..-1]
    }

    List listApplicationManagers() {
        println "Listing application managers ..."
        runJaclCommand(properties.dmgr, '$AdminControl queryNames type=ApplicationManager,*').readLines()[2..-1]
    }

    def saveConfiguration() {
        println "Saving configuration ..."
        runJaclCommand(properties.dmgr, '$AdminConfig save')
    }

    def startApplication(applicationName, applicationManagerQuery) {
        println "Starting application: '$applicationName' ..."
        assert applicationName, "You must provide an applicationName"
        assert applicationManagerQuery, "You must provide an applicationManagerQuery"
        runJythonCommand(properties.dmgr, "AdminControl.invoke(AdminControl.queryNames('$applicationManagerQuery')," +
                "'startApplication','$applicationName')")
    }

    def stopApplication(applicationName, applicationManagerQuery) {
        println "Stopping application: '$applicationName' ..."
        assert applicationName, "You must provide an applicationName"
        assert applicationManagerQuery, "You must provide an applicationManagerQuery"
        runJythonCommand(properties.dmgr, "AdminControl.invoke(AdminControl.queryNames('$applicationManagerQuery')," +
                "'stopApplication','$applicationName')")
    }

    def uninstallApplication(applicationName) {
        println "Uninstalling application: '$applicationName' ..."
        assert applicationName, "You must provide an applicationName"
        runClasspathScript(properties.dmgr, '/websphere/uninstallApplication.jython', [applicationName])
    }

}
