/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.examples.conn;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;

/**
 * How to open a direct connection using
 * {@link ClientConnectionOperator ClientConnectionOperator}.
 * This exemplifies the <i>opening</i> of the connection only.
 * The subsequent message exchange in this example should not
 * be used as a template.
 *
 * @since 4.0
 */
public class OperatorConnectDirect {

    public static void main(String[] args) throws Exception {
        HttpHost target = new HttpHost("jakarta.apache.org", 80, "http");

        // some general setup
        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        SchemeRegistry supportedSchemes = new SchemeRegistry();
        supportedSchemes.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        // Prepare parameters.
        // Since this example doesn't use the full core framework,
        // only few parameters are actually required.
        HttpParams params = new SyncBasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUseExpectContinue(params, false);

        // one operator can be used for many connections
        ClientConnectionOperator scop = new DefaultClientConnectionOperator(supportedSchemes);

        HttpRequest req = new BasicHttpRequest("OPTIONS", "*", HttpVersion.HTTP_1_1);
        req.addHeader("Host", target.getHostName());

        HttpContext ctx = new BasicHttpContext();

        OperatedClientConnection conn = scop.createConnection();
        try {
            System.out.println("opening connection to " + target);
            scop.openConnection(conn, target, null, ctx, params);
            System.out.println("sending request");
            conn.sendRequestHeader(req);
            // there is no request entity
            conn.flush();

            System.out.println("receiving response header");
            HttpResponse rsp = conn.receiveResponseHeader();

            System.out.println("----------------------------------------");
            System.out.println(rsp.getStatusLine());
            Header[] headers = rsp.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                System.out.println(headers[i]);
            }
            System.out.println("----------------------------------------");
        } finally {
            System.out.println("closing connection");
            conn.close();
        }
    }

}

