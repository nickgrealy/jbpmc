package org.jbpmc.runtime;

public interface ProcessInstance extends HasMetaData {

    String getId();

    String getName();

    String getStatus();

    Iterable<Task> getTasks();
}
