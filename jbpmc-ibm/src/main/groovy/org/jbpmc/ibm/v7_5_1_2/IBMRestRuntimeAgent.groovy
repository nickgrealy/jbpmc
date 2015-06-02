package org.jbpmc.ibm.v7_5_1_2

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.Method
import org.jbpmc.JbpmcAgent
import org.jbpmc.exception.OperationNotSupportedException
import org.jbpmc.ibm.core.CookieRetainingHttpBuilder
import org.jbpmc.ibm.core.HttpException
import org.jbpmc.ibm.core.IbmConfiguration
import org.jbpmc.runtimeImplementations.*
import org.jbpmc.runtime.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * <p>
 *     This class provides an IBM BPM implementation of the {@link org.jbpmc.BpmRuntimeAgent} class.
 * </p>
 * <p>
 *     <h3>Example usage</h3>
 *     <pre><code>def agent = new IBMRestRuntimeAgent(new IbmConfiguration([
    httpUrl : 'http://server:port',
    username: 'username',
    password: 'password'
]))
agent.getProcessInstance(12345).tasks.findAll { it.status == 'ACTIVE' }.each {
    agent.reassignTask(it, 'tw_admin')
}</code></pre>
 * </p>
 * <p>
 *     <b>Reference:</b> <a href="http://www-01.ibm.com/support/knowledgecenter/SSFPJS_7.5.0/com.ibm.wbpm.bpc.doc/topics/cdev_restapis.html?lang=en">ibm.com</a>
 * </p>
 */
class IBMRestRuntimeAgent extends JbpmcAgent<IbmConfiguration> {

    static final Logger logger = LoggerFactory.getLogger(IBMRestRuntimeAgent)

    static def baseApiUrl = '/rest/bpm/wle/v1'
    static def systemDetailsApiUrl = "$baseApiUrl/systems"
    static def processAppsApiUrl = "$baseApiUrl/processApps"
    static def processApiUrl = "$baseApiUrl/process"
    static def serviceApiUrl = "$baseApiUrl/service"
    static def exposedApiUrl = "$baseApiUrl/exposed"
    static def taskApiUrl = "$baseApiUrl/task"
    static def authUrl = "/bpmrest-ui"

    final CookieRetainingHttpBuilder httpClient
    boolean authenticated = false

    /**
     * Connects to an IBM BPM '/bpmrest-ui' service.
     *
     * @param url
     * @param username
     * @param password
     */
    IBMRestRuntimeAgent(Map props) {
        super(props)
        logger.info "RestUtils connecting to $configuration.httpUrl"
        httpClient = new CookieRetainingHttpBuilder(new URL(configuration.httpUrl))
        httpClient.setTimeoutMillis(configuration.timeoutMillis) // 1 minute
    }

    /* interface implementations */

    @Override
    List<Process> getRunnableProcesses() {
        def jsonResponse = get "$exposedApiUrl/process"
        jsonResponse.data.exposedItemsList.collect { wrapProcess(it) }
    }

    @Override
    ProcessInstance runProcess(Process process, Map<String, Object> parameters) {
        throw new OperationNotSupportedException() // todo implement
    }

    @Override
    List<Service> getRunnableServices() {
        def jsonResponse = get "$exposedApiUrl/service"
        jsonResponse.data.exposedItemsList.collect { wrapService(it) }
    }

    @Override
    Map<String, Object> runService(Service service, Map<String, Object> parameters) {
        throw new OperationNotSupportedException() // todo implement
    }

    @Override
    List<ProcessInstance> getProcessInstances() {
        throw new OperationNotSupportedException() // todo implement
    }

    @Override
    void deleteProcessInstance(String processInstanceId) {
        def processInstance = getProcessInstance(processInstanceId)
        if (processInstance.status != 'Terminated'){
            logger.info "Terminating process instance '$processInstanceId'"
            post("$processApiUrl/$processInstanceId", '', [action:'terminate', parts: 'all'])
        }
        logger.info "Deleting process instance '$processInstanceId'"
        post("$processApiUrl/$processInstanceId", '', [action:'delete', parts: 'all'])
    }

    @Override
    SystemInfo getSystemInfo() {
        Map map = get(systemDetailsApiUrl).data.systems.first()
        new SystemInfoImpl(id: map.systemID, name: map.hostname, applicationVersion: map.version, metaData: map)
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        wrapProcessInstance(get("$processApiUrl/$processInstanceId").data) // todo lazy load tasks?
    }

    @Override
    public Task getTask(String taskId) {
        wrapTask(get("$taskApiUrl/$taskId", [parts: 'all']).data)
    }

    @Override
    void reassignTask(String taskId, String assignee) {
        post "$taskApiUrl/$taskId".toString(), "action=assign&toUser=$assignee&parts=all"
    }

    @Override
    void setTaskData(String taskId, Map<String, Object> data) {
        post "$serviceApiUrl/$taskId",
                'action=setData&params=' + URLEncoder.encode(new JsonBuilder(data).toString(), 'UTF-8')
    }

    /* utility methods */

    /**
     * Authenticate with the server, and populate the auth Cookies.
     * @param username
     * @param password
     * @return
     */
    private void authenticate(String username, String password) {
        // todo handle session timeout?
        if (!authenticated) {
            def authHeader = [Authorization: "Basic ${"$username:$password".bytes.encodeBase64().toString()}"]
            logger.info "GET $authUrl"
            def result = httpClient.request(
                    Method.GET,
                    authUrl,
                    authHeader,
                    [:]
            )
            handleResult result
            authenticated = true
            // todo verify system version using "system details"?
        }
    }

    Map get(httpPath, Map httpParams = [:], int timeoutMillis = 60000) {
        authenticate(configuration.username, configuration.password)
        logger.info "GET $httpPath"
        httpClient.setTimeoutMillis(timeoutMillis)
        def result = httpClient.request(
                Method.GET,
                httpPath.toString(),
                [Accept: 'application/json'],
                httpParams)
        def json = handleResult result
        new JsonSlurper().parseText(json)
    }

    Map post(httpPath, body = '', Map httpParams = [:]) {
        authenticate(configuration.username, configuration.password)
        logger.info "POST $httpPath ? $body"
        def result = httpClient.request(
                Method.POST,
                httpPath.toString(),
                [
                        Accept        : 'application/json',
                        'Content-Type': 'application/x-www-form-urlencoded'
                ],
                httpParams,
                body.toString()
        )
        def json = handleResult(result)
        new JsonSlurper().parseText(json)
    }

    Map delete(httpPath, Map httpParams = [:]) {
        authenticate(configuration.username, configuration.password)
        logger.info "DELETE $httpPath"
        def result = httpClient.request(
                Method.DELETE,
                httpPath.toString(),
                [
                        Accept        : 'application/json',
                ],
                httpParams
        )
        def json = handleResult(result)
        new JsonSlurper().parseText(json)
    }

    Process wrapProcess(Map map) {
        // type, itemID, itemReference, processAppID, snapshotID, display, tip, ID
        addSnapshotMetaData(map)
        new ProcessImpl(id: map.ID, name: map.display, metaData: map)
    }

    Service wrapService(Map map) {
        // type, itemID, itemReference, processAppID, snapshotID, display, tip, ID
        addSnapshotMetaData(map)
        new ServiceImpl(id: map.ID, name: map.display, metaData: map)
    }

    ProcessInstance wrapProcessInstance(map) {
        def tasks = map.tasks.collect { wrapTask(it) }
        new ProcessInstanceImpl(id: map.piid, name: map.name,
                status: map.executionState, metaData: map, tasks: tasks)
    }

    Task wrapTask(map) {
        new TaskImpl(id: map.tkiid, name: map.name, status: map.status,
                assignedTo: map.assignedTo, metaData: map, data: map.data.variables)
    }

    Map<String, Map<String, Object>> cachedSnapshotData = [:]
    Map<String, Map<String, Object>> cachedProcessAppData = [:]

    private static File getCacheFile() {
        def today = new SimpleDateFormat('yyyyMMdd').format(new Date())
        new File(System.properties.'java.io.tmpdir'.toString(), "IBMRestRuntimeAgentCache_${today}.json")
    }

    private static void writeToCache(def key, Map object) {
        def file = getCacheFile()
        logger.info "Writing data to cache file '$file'..."
        Map dataMap = readFromCache()
        dataMap[key.toString()] = object
        file.exists() ? file.delete() : null
        file << new JsonBuilder(dataMap).toString()
    }

    private static Map readFromCache(String key = null) {
        def file = getCacheFile()
        logger.info "Reading data from cache file '$file'..."
        def dataMap = file.exists() ? new JsonSlurper().parse(file) : [:]
        (Map) (key == null ? dataMap : dataMap[key])
    }

    void addSnapshotMetaData(Map metaData) {
        if (cachedProcessAppData.isEmpty()) {
            Map jsonResponse
            if (configuration.useCache) {
                jsonResponse = readFromCache(processAppsApiUrl)
            }
            if (jsonResponse == null) {
                jsonResponse = get processAppsApiUrl, [:], configuration.longTimeoutMillis
                if (configuration.useCache) {
                    writeToCache(processAppsApiUrl, jsonResponse)
                }
            }
            jsonResponse.data.processAppsList.each { app ->
                cachedProcessAppData.put(app.ID.toString(), [
                        processApp_id            : app.ID,
                        processApp_name          : app.name,
                        processApp_shortName     : app.shortName,
                        processApp_description   : app.description,
                        processApp_defaultVersion: app.defaultVersion,
                        processApp_lastModifiedBy: app.lastModifiedBy,
                        processApp_lastModifiedOn: app.lastModified_on,
                ])
                app.installedSnapshots.each { snap ->
                    cachedSnapshotData.put(snap.ID.toString(), [
                            snapshot_id        : snap.ID,
                            snapshot_name      : snap.name,
                            snapshot_acronym   : snap.acronym,
                            snapshot_active    : snap.active,
                            snapshot_branchID  : snap.branchID,
                            snapshot_branchName: snap.branchName,
                            snapshot_tip       : snap.snapshotTip,
                    ])
                }
            }
        }
        metaData << (cachedSnapshotData.get(metaData.snapshotID.toString()) ?: [:])
        metaData << (cachedProcessAppData.get(metaData.processAppID.toString()) ?: [:])
    }

    static def handleResult(result) {
        if (result instanceof HttpException) {
            throw result
        } else {
            return result
        }
    }
}
