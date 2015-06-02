package org.jbpmc.ibm.core

class WsadminClient {

    public static final String KNOWN_ERROR_CODES_REGEX = /WASX7023E|WASX7318E|CWPKI0022E/
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
        validateProperties(aggrConfig, ['wsadmin_exec', 'ibmjdk_home'])
        assert new File(aggrConfig.wsadmin_exec).exists(), "Wsadmin executable '$aggrConfig.wsadmin_exec' does not exist!"
        aggrConfig
    }

    static def validateProperties(def config, def requiredList){
        requiredList.each {
            assert config[it], "Required property '$it' has not been set"
        }
    }

    String runJaclCommand(node, command){
        def cmd = buildCommand(node, command, false)
        runCommand(cmd)
    }

    String runJythonCommand(node, command){
        def cmd = buildCommand(node, command, true)
        runCommand(cmd)
    }

    String runScript(node, File script){
        def cmd = buildScript(node, script)
        runCommand(cmd)
    }

    String runClasspathScript(node, String classpathScript, List params = []){
        def script = copyClasspathFileToSystem(classpathScript)
        def cmd = buildScript(node, script) + params
        runCommand(cmd)
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

    private def runCommand(List cmd, File workingDir = new File(properties.wsadmin_exec).parentFile, long timeoutMillis = 1000*60*60) {
        println "Executing command: $workingDir> '" + cmd.join(' ') + "'"
        long start = System.currentTimeSeconds()
        def bout = new ByteArrayOutputStream(), berr = new ByteArrayOutputStream()
        def envVars = System.getenv().collectEntries { k,v -> [k, v]}
        envVars.JAVA_HOME = properties.ibmjdk_home
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
        println "Execution completed in $total seconds: exitVal='$exitVal'"
        if (proc.exitValue() != 0) {
            throw new RuntimeException("Process exited with non zero value '${exitVal}'. serr='${serr}'")
        }
        if (sout =~ KNOWN_ERROR_CODES_REGEX){
            throw new RuntimeException(sout)
        }
        sout
    }

}