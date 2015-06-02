package org.jbpmc.ibm.core

import org.jbpmc.core.BaseAgentConfiguration

class IbmConfiguration extends BaseAgentConfiguration<IbmConfiguration> {

    String httpUrl, username, password
    int timeoutMillis = 60 * 1000, longTimeoutMillis = 2 * 60 * 1000
    boolean useCache = false

    IbmConfiguration(Map map = [:]) {
        super(map)
    }

    IbmConfiguration verify(){
        // todo Provide default implementation - read @Required annotations, or simply return false if any value is null?
        def required = ['httpUrl', 'username', 'password'].find { get(it) == null ? it : null }
        assert !required, "found null configuration value '$required'"
        this
    }

}
