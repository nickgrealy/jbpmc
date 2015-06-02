package org.jbpmc.runtimeImplementations;

import org.jbpmc.runtime.ProcessInstance;
import org.jbpmc.runtime.Task;

import java.util.List;
import java.util.Map;

public class ProcessInstanceImpl extends HasMetaDataImpl implements ProcessInstance {

    String status;
    List<Task> tasks;
    Map<String, Object> data;

    public String getStatus() {
        return status;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ProcessInstanceImpl{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", tasks=" + tasks.size() +
                '}';
    }
}
