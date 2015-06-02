package org.jbpmc.runtimeImplementations;

import org.jbpmc.runtime.HasMetaData;

import java.util.Map;

public class HasMetaDataImpl implements HasMetaData {

    String id;
    String name;
    Map<String, Object> metaData;

    public HasMetaDataImpl() {
    }

    public HasMetaDataImpl(String id, String name, Map<String, Object> metaData) {
        this.id = id;
        this.name = name;
        this.metaData = metaData;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
