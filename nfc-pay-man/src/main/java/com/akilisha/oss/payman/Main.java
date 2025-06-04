package com.akilisha.oss.payman;

import com.akilisha.oss.payman.handler.*;
import com.akilisha.oss.payman.service.DeviceService;
import com.akilisha.oss.payman.service.PaymentService;
import com.akilisha.oss.payman.service.QrCodeService;
import com.akilisha.oss.payman.sse.SsePublisher;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.DefaultSessionCacheFactory;
import org.eclipse.jetty.server.session.FileSessionDataStoreFactory;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ServiceLoader;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        // 1. Configure Thread Pool
        QueuedThreadPool threadPool = new QueuedThreadPool(200, 8);
        threadPool.setName("jetty-server");

        // 2. Create Jetty Server
        Server server = new Server(threadPool);

        // 3. Configure Server Connector
        ServerConnector http = new ServerConnector(server);
        http.setPort(8080);
        server.addConnector(http);

        // 4. Context Handlers Collection
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        // --- Dependency Injection Setup (Manual for demo, illustrates CDI/ServiceLoader concept) ---
        // In a full CDI setup, these services would be @ApplicationScoped or @Dependent
        // and injected automatically.
        PaymentService paymentService = new PaymentService();
        DeviceService deviceService = new DeviceService();
        QrCodeService qrCodeService = new QrCodeService(); // Assuming it depends on PaymentService or just generates codes
        SsePublisher ssePublisher = new SsePublisher(); // SSE publisher instance

        // 5. Configure Session Handler using H2 JDBC Session Manager
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        contexts.addHandler(servletContextHandler);

        //First let's configure a DefaultSessionCacheFactory
        DefaultSessionCacheFactory cacheFactory = new DefaultSessionCacheFactory();
        //NEVER_EVICT
        cacheFactory.setEvictionPolicy(SessionCache.NEVER_EVICT);
        cacheFactory.setFlushOnResponseCommit(true);
        cacheFactory.setInvalidateOnShutdown(false);
        cacheFactory.setRemoveUnloadableSessions(true);
        cacheFactory.setSaveOnCreate(true);
        //Add the factory as a bean to the server
        //Now whenever a SessionManager starts, it will consult the bean to create a new DefaultSessionCache
        server.addBean(cacheFactory);

        //Now, let's configure a FileSessionDataStoreFactory
        FileSessionDataStoreFactory storeFactory = new FileSessionDataStoreFactory();
        storeFactory.setStoreDir(new File("/tmp/sessions"));
        storeFactory.setGracePeriodSec(3600);
        storeFactory.setSavePeriodSec(0);

        //Add the factory as a bean on the server, now whenever a
        //SessionManager starts, it will consult the bean to create a new FileSessionDataStore
        //for use by the DefaultSessionCache
        server.addBean(storeFactory);

        LOGGER.info("H2 JDBC Session Manager configured and started.");

        // 6. Configure Resource Handler for static resources
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true); // For testing, list directory contents
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});

        // Get the base directory for static resources (e.g., 'src/main/resources/static')
        // This makes it work both from IDE and packaged JAR
        URL webRootLocation = Main.class.getResource("/static");
        if (webRootLocation == null) {
            throw new IllegalStateException("WebRoot resource not found: /static. Ensure it exists in src/main/resources/static");
        }
        resourceHandler.setBaseResource(Resource.newResource(webRootLocation.toExternalForm()));
        servletContextHandler.setHandler(resourceHandler); // Add resource handler to the context

        LOGGER.info("Static resources configured from: {}", webRootLocation.toExternalForm());

        // --- Dynamically Load Handlers using ServiceLoader ---
        // This demonstrates a pluggable architecture.
        // Each handler (servlet) implementation should have a META-INF/services/jakarta.servlet.Servlet
        // file containing its fully qualified class name.
        ServiceLoader<BaseHandler> handlers = ServiceLoader.load(BaseHandler.class);
        for (BaseHandler handler : handlers) {
            // Manually inject dependencies for demo purposes.
            // In a real CDI app, these would be @Inject'ed.
            if (handler instanceof OAuthHandler oAuthHandler) {
                oAuthHandler.setDeviceService(deviceService);
            } else if (handler instanceof DeviceHandler deviceHandler) {
                deviceHandler.setDeviceService(deviceService);
            } else if (handler instanceof PaymentMethodHandler paymentMethodHandler) {
                paymentMethodHandler.setPaymentService(paymentService);
            } else if (handler instanceof QrCodeHandler qrCodeHandler) {
                qrCodeHandler.setQrCodeService(qrCodeService);
            } else if (handler instanceof PaymentProcessingHandler paymentProcessingHandler) {
                paymentProcessingHandler.setPaymentService(paymentService);
                paymentProcessingHandler.setSsePublisher(ssePublisher); // Inject SSE publisher
            } else if (handler instanceof SseHandler sseHandler) {
                sseHandler.setSsePublisher(ssePublisher); // Inject SSE publisher
            } else if (handler instanceof CommandHandler) {
                // CommandHandler might need a way to dispatch commands to other services
                // For simplicity, it will just log the command for now.
            }

            servletContextHandler.addServlet(new ServletHolder(handler), handler.getPath());
            LOGGER.info("Loaded handler: {}", handler.getClass().getSimpleName() + " at path: " + handler.getPath());
        }

        // Start the server
        server.start();
        LOGGER.info("Jetty server started on port 8080");
        server.join();
    }
}