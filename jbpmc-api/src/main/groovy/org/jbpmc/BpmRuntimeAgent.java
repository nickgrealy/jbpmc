package org.jbpmc;

import org.jbpmc.runtime.Process;
import org.jbpmc.runtime.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *     This is a generic interface, to provide a level of abstraction over the different BPM implementations.
 * </p>
 * <p>
 *     JBPMC implementations should implement this class. Features that aren't implemented, should throw
 *     {@link org.jbpmc.exception.OperationNotSupportedException}.
 * </p>
 */
public interface BpmRuntimeAgent {

    /**
     * Provides a list of Processes that can be run.
     */
    List<Process> getRunnableProcesses();

    /**
     * Run the given process, passing in any parameters.
     */
    ProcessInstance runProcess(Process process, Map<String, Object> parameters);

    /**
     * Provides a list of Services that can be run.
     */
    List<Service> getRunnableServices();

    /**
     * Run the given Service, passing in any parameters.
     */
    Map<String, Object> runService(Service service, Map<String, Object> parameters);

    /**
     * Retrieves data for the requested ProcessInstance.
     */
    ProcessInstance getProcessInstance(String processInstanceId);

    /**
     * Retrieves data for the reqeuested Task.
     */
    Task getTask(String taskId);

    /**
     * Reassigns a task to the supplied assignee.
     */
    void reassignTask(String taskId, String assignee);

    /**
     * Sets data on the Task object.
     */
    void setTaskData(String taskId, Map<String, Object> data);

    /**
     * Provides a list of all ProcessInstances.
     */
    List<ProcessInstance> getProcessInstances();

    /**
     * Deletes a ProcessInstance.
     */
    void deleteProcessInstance(String processInstanceId);

    /**
     * Retrievse information relating to the system.
     */
    SystemInfo getSystemInfo();

    // configuration?? (users / groups / epvs)

}