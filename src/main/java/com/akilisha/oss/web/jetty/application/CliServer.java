package com.akilisha.oss.web.jetty.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import com.akilisha.oss.web.shared.logging.CliAppender;
import org.apache.commons.cli.*;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CliServer {

    private final ContextHandlerCollection collectionHandler = new ContextHandlerCollection();
    private Server server;

    public static void main(String[] args) throws Exception {
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();

        Options options = new Options();
        options.addOption(new Option("h", "host", true, "host domain name or ip address"));
        options.addOption(new Option("p", "port", true, "port number"));
        options.addOption(new Option("s", "secure", true, "secure port number"));
        options.addOption(new Option("k", "keystore", true, "keystore file path"));
        options.addOption(new Option("w", "secret", true, "keystore password"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String host = cmd.getOptionValue("h", "127.0.0.1");
        int port = Integer.parseInt(cmd.getOptionValue("p", "8080"));
        int securePort = Integer.parseInt(cmd.getOptionValue("s", "8443"));
        String keystore = cmd.getOptionValue("k");
        String secret = cmd.getOptionValue("w");

        configureLogback();

        CliServer cliServer = container.select(CliServer.class).get();

        //setup server
        cliServer.setupHttpServer();

//        // load route handlers
//        for (RouteHandler service : container.select(RouteHandler.class).stream().toList()) {
//            ContextHandler contextHandler = new ContextHandler(service.context());
//            contextHandler.setHandler((Handler) service);
//            cliServer.addContextHandler(contextHandler);
//        }

        cliServer.start(host, port, securePort, keystore, secret);
    }

    private static void configureLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();
        CliAppender appender = new CliAppender();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.start();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.DEBUG);
    }

    private static ServerConnector createSecureConnector(Server server, String host, int securePort, String keyStorePath, String keyStoreSecret, HttpConnectionFactory http11, int acceptors, int selectors, HTTP2CServerConnectionFactory h2c) {
        // The ALPN ConnectionFactory.
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        // The default protocol to use in case there is no negotiation.
        alpn.setDefaultProtocol(http11.getProtocol());

        // Configure the SslContextFactory with the keyStore information.
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStoreSecret);

        // The ConnectionFactory for TLS.
        SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());

        // The ServerConnector instance.
        ServerConnector secureConnector = new ServerConnector(server, acceptors, selectors, tls, alpn, h2c, http11);
        // The port to listen to.
        secureConnector.setPort(securePort);
        // The address to bind to.
        secureConnector.setHost(host);
        // The TCP accept queue size.
        secureConnector.setAcceptQueueSize(128);
        return secureConnector;
    }

    private static ServerConnector createConnector(Server server, String host, int port, int acceptors, int selectors, HttpConnectionFactory http11, HTTP2CServerConnectionFactory h2c) {
        // Create a ServerConnector to accept connections from clients.
        ServerConnector plainConnector = new ServerConnector(server, acceptors, selectors, http11, h2c);
        // The port to listen to.
        plainConnector.setPort(port);
        // The address to bind to.
        plainConnector.setHost(host);
        // The TCP accept queue size.
        plainConnector.setAcceptQueueSize(128);
        return plainConnector;
    }

    private void setupHttpServer() throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("cli-server");

        // Create a Server instance.
        this.server = new Server(threadPool);

        // Static resources
        ServletContextHandler servletResourceHandler = createResourceHandler();
        // Use HandlersList so that handling is passed to the next handler until match is found
        server.setHandler(new HandlerList(collectionHandler, servletResourceHandler, new DefaultHandler()));
    }

    private void start(String host, int port, int securePort, String keyStorePath, String keyStoreSecret) throws Exception {
        // The number of acceptor threads.
        int acceptors = 1;
        // The number of selectors.
        int selectors = 1;
        // The HTTP configuration object.
        HttpConfiguration httpConfig = new HttpConfiguration();
        // Configure the HTTP support, for example:
        httpConfig.setSendServerVersion(false);
        // The ConnectionFactory for HTTP/1.1.
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);
        // The ConnectionFactory for clear-text HTTP/2.
        HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(httpConfig);
        // Create a plain ServerConnector instance.
        ServerConnector connector = createConnector(server, host, port, acceptors, selectors, http11, h2c);
        server.addConnector(connector);

        // Create a secure ServerConnector instance
        if (keyStorePath != null && keyStoreSecret != null) {
            ServerConnector secureConnector = createSecureConnector(server, host, securePort, keyStorePath, keyStoreSecret, http11, acceptors, selectors, h2c);
            server.addConnector(secureConnector);
        }

        // Start the server
        server.start();
        System.out.printf("Server started on %s:%d\n", host, port);
    }

    public void addContextHandler(ContextHandler contextHandler) {
        collectionHandler.addHandler(contextHandler);
    }

    public ServletContextHandler createResourceHandler() throws IOException {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        // Create and configure a ResourceHandler.
        ResourceHandler handler = new ResourceHandler();
        // Configure the directory where static resources are located.
        handler.setBaseResource(Resource.newResource("./dist"));
        // Configure directory listing.
        handler.setDirectoriesListed(false);
        // Configure welcome files.
        handler.setWelcomeFiles(new String[]{"index.html"});
        // Configure whether to accept range requests.
        handler.setAcceptRanges(true);
        context.setHandler(handler);
        return context;
    }
}
