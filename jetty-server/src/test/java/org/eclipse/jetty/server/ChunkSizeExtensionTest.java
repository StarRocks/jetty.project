//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.IO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ChunkSizeExtensionTest
{
    private Server server;
    private LocalConnector connector;

    @BeforeEach
    public void start() throws Exception
    {
        server = new Server();
        connector = new LocalConnector(server);
        server.addConnector(connector);
        server.setHandler(new AbstractHandler()
        {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
            {
                baseRequest.setHandled(true);
                InputStream in = request.getInputStream();
                IO.copy(in, IO.getNullStream());
            }
        });
        server.start();
    }

    @AfterEach
    public void stop() throws Exception
    {
        if (server != null)
            server.stop();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testExtensionNoValue(boolean bws) throws Exception
    {
        String w = bws ? "\t" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "10" + w + ";" + w + "ext\r\n" +
            "0123456789ABCDEF\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testExtensionWithValue(boolean bws) throws Exception
    {
        String w = bws ? "\t" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "1" + w + ";" + w + "ext" + w + "=" + w + "val\r\n" +
            "X\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testExtensionWithQuotedValue(boolean bws) throws Exception
    {
        String w = bws ? "\t" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "1" + w + ";" + w + "ext" + w + "=" + w + "\"val\"\r\n" +
            "X\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMultipleExtensionsNoValues(boolean bws) throws Exception
    {
        String w = bws ? "\t" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "4" + w + ";" + w + "ext1" + w + ";" + w + "ext2\r\n" +
            "0123\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMultipleExtensionsWithValues(boolean bws) throws Exception
    {
        String w = bws ? "\t" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "4" + w + ";" + w + "ext1" + w + "=" + w + "1" + w + ";" + w + "ext2" + w + "=" + w + "2\r\n" +
            "0123\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMultipleExtensionsWithQuotes(boolean bws) throws Exception
    {
        String w = bws ? "\t" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "4" + w + ";" + w + "ext1" + w + "=" + w + "\"1\"" + w + ";" + w + "ext2" + w + "=" + w + "\"2\"\r\n" +
            "0123\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @Test
    public void testEmptyExtension() throws Exception
    {
        // Cannot legally have an empty extension block after the ';' character.
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "a;\r\n" +
            "0123456789\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        // 9.4 reports bad chunk extension as 500 from the handler's input stream read;
        // either way, the request must not be treated as a successful 200.
        assertThat(response.getStatus(), org.hamcrest.Matchers.anyOf(
            equalTo(HttpStatus.BAD_REQUEST_400),
            equalTo(HttpStatus.INTERNAL_SERVER_ERROR_500)));
    }

    @Test
    public void testQuotesWithinQuotes() throws Exception
    {
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "1;a=\"\\\"val\\\"\"\r\n" +
            "X\r\n" +
            "0\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testTerminalChunkWithExtension(boolean quoted) throws Exception
    {
        String q = quoted ? "\"" : "";
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "a\r\n" +
            "0123456789\r\n" +
            "0;ext=" + q + "1" + q + "\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @Test
    public void testTerminalChunkWithExtensionNoValue() throws Exception
    {
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "a\r\n" +
            "0123456789\r\n" +
            "0;ext\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @Test
    public void testTerminalChunkWithExtensionWithTrailers() throws Exception
    {
        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "a\r\n" +
            "0123456789\r\n" +
            "0;ext\r\n" +
            "Trailer: value\r\n" +
            "\r\n";
        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        assertThat(response.getStatus(), equalTo(HttpStatus.OK_200));
    }

    @Test
    public void testExtensionBytesBoundedByRequestHeaderSize() throws Exception
    {
        // Rebuild the server with a tight requestHeaderSize so oversized
        // chunk extensions are rejected before the body is consumed.
        server.stop();
        server = new Server();
        HttpConfiguration config = new HttpConfiguration();
        config.setRequestHeaderSize(256);
        connector = new LocalConnector(server, new HttpConnectionFactory(config));
        server.addConnector(connector);
        server.setHandler(new AbstractHandler()
        {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
            {
                baseRequest.setHandled(true);
                InputStream in = request.getInputStream();
                IO.copy(in, IO.getNullStream());
            }
        });
        server.start();

        StringBuilder hugeExt = new StringBuilder("ext=");
        for (int i = 0; i < 4096; i++)
            hugeExt.append('a');

        String request = "POST / HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: close\r\n" +
            "\r\n" +
            "1;" + hugeExt + "\r\n" +
            "X\r\n" +
            "0\r\n" +
            "\r\n";

        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request));

        // The oversized chunk extension must be rejected rather than silently accepted.
        assertThat(response.getStatus(), org.hamcrest.Matchers.not(equalTo(HttpStatus.OK_200)));
    }
}
