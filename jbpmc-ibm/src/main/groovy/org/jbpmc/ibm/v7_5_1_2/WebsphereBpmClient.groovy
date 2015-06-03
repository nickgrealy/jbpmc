package org.jbpmc.ibm.v7_5_1_2

class WebsphereBpmClient extends WebsphereClient {

    WebsphereBpmClient(String environment = System.getProperty('env', 'dev'), Map environmentConfig = [:]) {
        super(environment, environmentConfig)
    }

    /**
     * Packages an IBM BPM application.
     *
     * @param node the configuration node, to run the command against (e.g. <code>client.properties.node1</code>)
     * @param applicationName the <i>containerAcronym</i> (e.g. <code>BIZAPP</code>)
     * @param applicationVersion the <i>containerSnapshotAcronym</i> (e.g. <code>1.2.3</code>)
     * @return
     */
    String packageApplication(def node, String applicationName, String applicationVersion){
        def outputFile = "/tmp/${applicationName}-${applicationVersion}-${new Date().format("yyyyMMddHHmmss")}.zip"
        println "Creating offline package name='$applicationName' version='$applicationVersion' outputFile='$outputFile' ..."
        runClasspathScript(node, "/WebsphereBpmClient/packageApplication.jython", [applicationName, applicationVersion, outputFile])
        outputFile
    }

    /**
     * Deploys the packaged IBM BPM application.
     *
     * @param packagedApplication the artifact created by running {@link #packageApplication(java.lang.Object, java.lang.String, java.lang.String)}.
     * @return
     */
    def deployApplication(File packagedApplication){
        runJythonCommand(properties.dmgr, "AdminTask.BPMInstallOfflinePackage('[-inputFile \"$packagedApplication.absolutePath\"]')")
    }
//    def undeployApplication(){}

}
