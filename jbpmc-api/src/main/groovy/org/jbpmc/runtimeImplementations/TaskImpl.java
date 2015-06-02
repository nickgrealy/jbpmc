package org.jbpmc.runtimeImplementations;

import org.jbpmc.runtime.Task;

import java.util.Map;

public class TaskImpl extends HasMetaDataImpl implements Task {

    String status;
    String assignedTo;
    Map<String, Object> data;

    public String getStatus() {
        return status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TaskImpl{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", assignedTo='" + assignedTo + '\'' +
                '}';
    }
}
