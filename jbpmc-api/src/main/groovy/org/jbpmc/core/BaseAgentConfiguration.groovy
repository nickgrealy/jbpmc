package org.jbpmc.core

/**
 * <p>
 *     This class facilitates creating a configuration object. It intercepts accessor calls to the field variables, and
 *     delegates the calls to the underlying {@link HashMap} implementation.
 * </p>
 * <p>
 *     To use, extend this <i>BaseAgentConfiguration</i> class, and provide fields and a base constructor. e.g.
 *     <pre><code>class SampleConfiguration extends BaseAgentConfiguration {

    String httpUrl, username, password

    SampleConfiguration(Map map = [:]) {
        super(map)
    }

}</code></pre>
 * </p>
 * <p>
 *     You can then use the extended configuration class, like so:
 *     <pre><code>// Example 1
def conf = new SampleConfiguration()
conf.httpUrl = 'http://foobar1'

// Example 2
new SampleConfiguration(httpUrl: 'http://foobar2')

// Example 3
new SampleConfiguration([httpUrl: 'http://foobar3'])

// Example 4
def props = [httpUrl: 'http://foobar4'] as Properties
props as SampleConfiguration</code></pre>
 * </p>
 */
class BaseAgentConfiguration<Impl extends BaseAgentConfiguration> extends HashMap {

    BaseAgentConfiguration(Map map = [:]) {
        super()
        putAll(map)
    }

    void setProperty(String name, value){
        put(name, value)
    }

    def getProperty(String name){
        get(name)
    }

    /**
     * A blank noop method. Override to implement config verification.
     */
    Impl verify(){}
}
