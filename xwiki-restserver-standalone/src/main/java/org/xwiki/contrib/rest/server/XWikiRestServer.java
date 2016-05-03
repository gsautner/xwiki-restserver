/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.rest.server;

import javax.net.ssl.SSLContext;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.xwiki.contrib.rest.XWikiJaxRsApplication;
import org.xwiki.contrib.rest.XWikiRestServerException;
import org.xwiki.contrib.rest.server.internal.IsRunningApplication;

import io.undertow.Undertow;

/**
 * @version $Id: $
 * @since 1.0
 */
public class XWikiRestServer implements Runnable
{
    private int portNumber;

    private SSLContext sslContext;

    private XWikiJaxRsApplication application;

    private Thread thread;

    private UndertowJaxrsServer server;

    private String host;

    public XWikiRestServer(int portNumber, XWikiJaxRsApplication application, String host)
    {
        this.portNumber = portNumber;
        this.application = application;
        this.host = host;
    }

    public XWikiRestServer(int portNumber, SSLContext sslContext, XWikiJaxRsApplication application, String host)
    {
        this.portNumber = portNumber;
        this.application = application;
        this.sslContext = sslContext;
        this.host = host;
    }

    public void start() throws XWikiRestServerException
    {
        if (thread != null) {
            throw new XWikiRestServerException("XWikiRestServer is already running.");
        }
        thread = new Thread(this);
        thread.start();
    }

    public void join() throws XWikiRestServerException, InterruptedException
    {
        if (thread == null) {
            throw new XWikiRestServerException("XWikiRestServer is not running.");
        }
        thread.join();
    }

    public void stop(boolean wait) throws XWikiRestServerException, InterruptedException
    {
        if (thread == null) {
            throw new XWikiRestServerException("XWikiRestServer is not running.");
        }
        server.stop();
        if (wait) {
            join();
        }
    }

    @Override
    public void run()
    {
        // We use Undertow as an embedded server: simple, light and efficient.
        server = new UndertowJaxrsServer();

        // We manually create the Undertow Builder to set the port that we want
        Undertow.Builder undertowBuilder = Undertow.builder();

        if (isUsingSSL()) {
            undertowBuilder.addHttpsListener(this.portNumber, host, this.sslContext);
        } else {
            undertowBuilder.addHttpListener(this.portNumber, host);
        }

        // We deploy the applications
        server.deploy(application);
        server.deploy(new IsRunningApplication(), "isrunning");

        // We start the server
        server.start(undertowBuilder);
    }

    public XWikiJaxRsApplication getApplication()
    {
        return application;
    }

    public void setApplication(XWikiJaxRsApplication application)
    {
        this.application = application;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    public void setPortNumber(int portNumber)
    {
        this.portNumber = portNumber;
    }

    public boolean isUsingSSL()
    {
        return sslContext != null;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getHost()
    {
        return host;
    }

    public void setSslContext(SSLContext sslContext)
    {
        this.sslContext = sslContext;
    }
}
