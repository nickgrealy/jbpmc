package org.jbpmc.runtime;

import java.util.Map;

public interface HasMetaData {

    String getId();

    String getName();

    Map<String, Object> getMetaData();

}
