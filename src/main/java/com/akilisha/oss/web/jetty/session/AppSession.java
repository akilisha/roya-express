package com.akilisha.oss.web.jetty.session;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.DefaultSessionCacheFactory;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.server.session.FileSessionDataStoreFactory;
import org.eclipse.jetty.server.session.HouseKeeper;

import java.io.File;

public class AppSession {

    public static void configureSessionHandler(Server server) throws Exception {
        //There is one SessionCache per SessionHandler, and thus one per context.
        DefaultSessionCacheFactory cacheFactory = new DefaultSessionCacheFactory();
        //EVICT_ON_INACTIVE: evict a session after 60sec inactivity
        cacheFactory.setEvictionPolicy(60);
        //Only useful with the EVICT_ON_INACTIVE policy
        cacheFactory.setSaveOnInactiveEvict(true);
        cacheFactory.setFlushOnResponseCommit(true);
        cacheFactory.setInvalidateOnShutdown(false);
        cacheFactory.setRemoveUnloadableSessions(true);
        cacheFactory.setSaveOnCreate(true);

        //Add the factory as a bean to the server, now whenever a
        //SessionHandler starts -> it will consult the bean to create a new DefaultSessionCache
        server.addBean(cacheFactory);

        //Now, lets configure a FileSessionDataStoreFactory
        FileSessionDataStoreFactory storeFactory = new FileSessionDataStoreFactory();
        storeFactory.setStoreDir(new File("/tmp/sessions"));
        storeFactory.setGracePeriodSec(3600);
        storeFactory.setSavePeriodSec(0);

        //Add the factory as a bean on the server; now whenever a
        //SessionHandler starts, it will consult the bean to create a new FileSessionDataStore
        //for use by the DefaultSessionCache
        server.addBean(storeFactory);
    }

    public static void configureSessionIdManager(Server server) throws Exception {
        DefaultSessionIdManager idMgr = new DefaultSessionIdManager(server);
        //you must set the workerName unless you set the env viable JETTY_WORKER_NAME
        idMgr.setWorkerName("server3");
        server.setSessionIdManager(idMgr);

        HouseKeeper houseKeeper = new HouseKeeper();
        houseKeeper.setSessionIdManager(idMgr);
        //set the frequency of scavage cycles
        houseKeeper.setIntervalSec(600L);
        idMgr.setSessionHouseKeeper(houseKeeper);
    }


}
