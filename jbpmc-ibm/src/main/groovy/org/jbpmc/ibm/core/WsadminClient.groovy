package org.jbpmc.ibm.core

class WsadminClient {

    public static final String KNOWN_ERROR_CODES_REGEX = /WASX\d{4}E|CWPKI\d{4}E/
    def properties

    WsadminClient(Map properties){
        this.properties = properties
    }

    static Map loadProperties(String environment, Map environmentConfig = [:], String configFilename = 'jbpmc-ibm.properties') {
        // todo test config over-loading (from diff sources)
        def aggrConfig = [:]
        List<URL> locations = [
                new File(System.properties.'user.home'.toString(), configFilename).toURI().toURL(),
                new File(configFilename).toURI().toURL(),
                this.class.getResource("/$configFilename"),
        ]
        locations.each {
            try {
                aggrConfig << new ConfigSlurper(environment).parse((URL)it)
                println "Read configuration from '$it'"
            } catch (Throwable t){
                System.err.println t.message
            }
        }
        aggrConfig << environmentConfig
        validateProperties(aggrConfig, ['wsadmin_exec', 'IBMJDK_HOME'])
        assert new File(aggrConfig.wsadmin_exec).exists(), "Wsadmin executable '$aggrConfig.wsadmin_exec' does not exist!"
        aggrConfig
    }

    static def validateProperties(def config, def requiredList){
        requiredList.each {
            assert config[it], "Required property '$it' has not been set"
        }
    }

    String runJaclCommand(node, command){
        List cmd = buildCommand(node, command, false)
        runCommand(cmd, node)
    }

    String runJythonCommand(node, command){
        List cmd = buildCommand(node, command, true)
        runCommand(cmd, node)
    }

    String runScript(node, File script){
        List cmd = buildScript(node, script)
        runCommand(cmd, node)
    }

    String runClasspathScript(node, String classpathScript, List params = []){
        def script = copyClasspathFileToSystem(classpathScript)
        List cmd = buildScript(node, script) + params
        runCommand(cmd, node)
    }

    private File copyClasspathFileToSystem(String source, File outputDir = new File(System.properties.'java.io.tmpdir')){
        def dest = new File(outputDir, source)
        if (dest.exists()){
            dest.delete()
        } else {
            dest.parentFile.mkdirs()
        }
        println "Copying file to '$dest' ..."
        dest.withDataOutputStream { os ->
            this.class.getResource(source).withInputStream { is ->
                os << is
            }
        }
        dest
    }

    private def buildCommand(def node, def command, boolean langJython = true){
        buildBaseCommand(node, langJython) + ['-c', command]
    }

    private def buildScript(def node, File script){
        assert script.exists(), "Script '$script' doesn't exist"
        buildBaseCommand(node, script.absolutePath.endsWith('.jython')) + ['-f', script.absolutePath]
    }

    private def buildBaseCommand(def node, boolean langJython = true){
        validateProperties(node, ['username', 'password', 'hostname', 'conntype', 'port'])
        [properties.wsadmin_exec, '-conntype', node.conntype, '-host', node.hostname, '-port', node.port,
         '-user', node.username, '-password', node.password, '-lang', langJython ? 'jython' : 'jacl']
    }

    private def runCommand(List cmd, def node = properties.dmgr, File workingDir = new File(properties.wsadmin_exec).parentFile, long timeoutMillis = 1000*60*60) {
        println "Executing command: $workingDir> '" + cmd.join(' ') + "'"
        long start = System.currentTimeSeconds()
        def bout = new ByteArrayOutputStream(), berr = new ByteArrayOutputStream()
        def envVars = System.getenv().collectEntries { k,v -> [k, v]}
        envVars.IBMJDK_HOME = properties.IBMJDK_HOME.replaceAll("\\\\", "/")
        envVars.WAS_LIBS = properties.WAS_LIBS.replaceAll("\\\\", "/")
        envVars.WAS_CONF = node.WAS_CONF.replaceAll("\\\\", "/")
        def sslClientProps = new File(envVars.WAS_CONF.toString(), 'ssl.client.props')
        if (sslClientProps.exists()){
            println "Updating 'user.root' in file: $sslClientProps.absolutePath"
            sslClientProps.text = sslClientProps.text.replaceAll(/user\.root=.*/, "user.root=${envVars.WAS_CONF}")
        }
        println "Using envVars: $envVars"
        def proc = cmd.execute(envVars.collect { k,v -> "$k=$v" }, workingDir)
        proc.waitForProcessOutput(
                new TeeOutputStream(System.out, bout),
                new TeeOutputStream(System.err, berr)
        )
        proc.waitForOrKill(timeoutMillis)
        def exitVal = proc.exitValue()
        long total = System.currentTimeSeconds() - start
        String sout = new String(bout.toByteArray()), serr = new String(berr.toByteArray())
        println "Execution completed in $total seconds."
        if (proc.exitValue() != 0) {
            throw new RuntimeException("Process exited with non zero value '${exitVal}'. sout='${sout}' serr='${serr}'")
        }
        if (sout =~ KNOWN_ERROR_CODES_REGEX){
            throw new RuntimeException(sout)
        }
        sout
    }

}