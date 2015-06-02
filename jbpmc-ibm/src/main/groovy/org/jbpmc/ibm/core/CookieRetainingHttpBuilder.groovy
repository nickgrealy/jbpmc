package org.jbpmc.ibm.core

import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.jbpmc.exception.OperationFailedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static groovyx.net.http.ContentType.*
/**
 * <p>
 *     This class provides a HttpBuilder, which retains cookies, returned from the server.
 * </p>
 */
class CookieRetainingHttpBuilder {

    Logger logger = LoggerFactory.getLogger(CookieRetainingHttpBuilder)

    URL url
    List<String> cookies = []
    Integer timeoutMillis = 30*1000 // default 30 seconds

    CookieRetainingHttpBuilder(URL url) {
        this.url = url
    }

    public def get(String path, Map<String, Serializable> queryParams) {
        request(Method.GET, path, [:], queryParams)
    }

    public def post(String path, Map<String, Serializable> queryParams) {
        request(Method.POST, path, [:], queryParams)
    }

    public def post(String path, Map<String, Serializable> overrideHeaders, String body) {
        request(Method.POST, path, overrideHeaders, [:], body)
    }

    void setTimeoutMillis(Integer timeoutMillis) {
        this.timeoutMillis = timeoutMillis
    }

    public def request(Method method,
                       String overridePath,
                       Map<String, Serializable> overrideHeaders,
                       Map<String, Serializable> queryParams,
                       String overrideBody = null) {
        try {
            def headersFinal = [
                    'Cookie': cookies.join(";")
            ]
            headersFinal.putAll(overrideHeaders)
            logger.info "Using headers: $headersFinal"

            def http = new HTTPBuilder()
            http.getClient().getParams().setParameter("http.connection.timeout", timeoutMillis)
            http.getClient().getParams().setParameter("http.socket.timeout", timeoutMillis)
            http.ignoreSSLIssues()
            http.request(url.toString(), method, TEXT) { req ->
                uri.path = overridePath
                if (queryParams) {
                    uri.query = queryParams
                }
                if (overrideBody) {
                    body = overrideBody
                }
                headers.putAll(headersFinal)
                response.success = { resp, reader ->
                    resp.getHeaders('Set-Cookie').each {
                        cookies.addAll it.value.split(";")
                    }
                    return reader.text
                }
                response.failure = { HttpResponseDecorator resp, reader ->
                    Map meta = [:]
                    try {
                        meta = new JsonSlurper().parseText(reader.text.toString())
                    } catch (Throwable t){
                        logger.error("Error occurred parsing HTTP response.", t)
                    }
                    String twErrorMessge = meta?.Data?.errorMessage
                    String status = resp.status.toString()
                    String statusLine = twErrorMessge ? twErrorMessge : resp.statusLine.toString()
                    new HttpException(status, statusLine, meta)
                }
            }
        } catch (Throwable t){
            throw new OperationFailedException("HTTP Request error.", t)
        }
    }
}