package org.jbpmc.runtime;

import java.util.Map;

public interface Task extends HasMetaData {

    String getStatus();

    String getAssignedTo();

    Map<String, Object> getData();

}
