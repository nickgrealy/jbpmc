package org.jbpmc.ibm.core

import org.jbpmc.exception.OperationFailedException
import org.jbpmc.runtimeImplementations.HasMetaDataImpl
import org.jbpmc.runtime.HasMetaData

/**
 * Thrown to indicate a Http exception has occurred.
 */
class HttpException extends OperationFailedException {

    HasMetaData delegate

    HttpException(String statusCode, String statusLine, Map<String, Object> metaData) {
        super(statusLine)
        delegate = new HasMetaDataImpl(statusCode, statusLine, metaData)
    }

    String getStatusCode() {
        delegate.getId()
    }

    String getStatusLine() {
        delegate.getName()
    }

    Map<String, Object> getJsonBody() {
        delegate.getMetaData()
    }
}
