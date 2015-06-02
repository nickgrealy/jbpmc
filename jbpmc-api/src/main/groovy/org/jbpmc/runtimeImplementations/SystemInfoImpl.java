package org.jbpmc.runtimeImplementations;

import org.jbpmc.runtime.SystemInfo;

public class SystemInfoImpl extends HasMetaDataImpl implements SystemInfo {

    String applicationVersion;

    @Override
    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
}
