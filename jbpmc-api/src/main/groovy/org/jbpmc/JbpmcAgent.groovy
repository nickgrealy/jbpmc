package org.jbpmc

/**
 * @since 1.0.1
 * @param < Configuration > implementation of the {@link org.jbpmc.core.BaseAgentConfiguration} class.
 */
abstract class JbpmcAgent<Configuration extends Map> implements BpmRuntimeAgent {

    Configuration configuration

    public JbpmcAgent(Map configuration){
        this.configuration = configuration
    }

}