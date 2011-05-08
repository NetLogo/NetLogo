//
// Copyright (c)2008 by the National Geographic Society. All Rights Reserved.
// 

package org.myworldgis.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

/** */
public final class HttpClientManager {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static HttpClientManager _instance = null;
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static HttpClientManager getInstance () {
        if (_instance == null) {
            _instance = new HttpClientManager("NetLogo GIS Extension");
        }
        return _instance;
    }
    
    /** */
    public static String errorMsg (int statusCode, String url) {
        StringBuilder result = new StringBuilder();
        result.append("http result code ");
        result.append(statusCode);
        result.append(" (");
        result.append(HttpStatus.getStatusText(statusCode));
        result.append(")");
        if (url != null) {
            result.append(" (URL: ");
            result.append(url);
            result.append(")");
        }
        return result.toString();
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private HttpConnectionManager _connectionManager;
    
    /** */
    private HttpClient _client;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    private HttpClientManager (String userAgent) {
        _connectionManager = new MultiThreadedHttpConnectionManager();
        _client = new HttpClient(_connectionManager);
        HttpClientParams params = _client.getParams();
        params.setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(30000));
        params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, Boolean.TRUE);
        params.setParameter(HttpClientParams.COOKIE_POLICY, CookiePolicy.RFC_2109);
        params.setParameter(HttpMethodParams.USER_AGENT, userAgent + "/HttpClientManager");
        _client.setParams(params);
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public int execute (HttpMethod method) throws IOException {
        try {
            URI requestURI = new URI(method.getURI().getURI());
            List<Proxy> proxies = ProxySelector.getDefault().select(requestURI);
            if (proxies.size() == 0) {
                return _client.executeMethod(method);
            } else {
                IOException exception = null;
                for (int i = 0; i < proxies.size(); i += 1) {
                    HostConfiguration hc = new HostConfiguration();
                    InetSocketAddress addr = (InetSocketAddress)proxies.get(i).address();
                    if (addr != null) {
                        hc.setProxy(addr.getHostName(), addr.getPort());
                    }
                    try {
                        return _client.executeMethod(hc, method);
                    } catch (IOException e) {
                        if (addr != null) {
                            ProxySelector.getDefault().connectFailed(requestURI, addr, e);
                        }
                        exception = e;
                    }
                }
                throw exception;
            }
        } catch (URISyntaxException e) {
            IOException ex = new IOException("error parsing uri: " + method.getURI().getURI());
            ex.initCause(e);
            throw ex;
        }   
    }
}
