package com.akilisha.oss.websig;

import com.akilisha.oss.web.express.application.Express;

public class ExpressDemo {

    public static void main(String[] args) {
        var app = Express.express();
        var port = 3000;

        app.get("/", (req, res, next) -> {
            res.send("Hello World");
        });

        app.get("/{name}", (req, res, next) -> {
                    res.locals().put("salute", "Mr");
                    next.ok();
                },
                (req, res, next) -> {
                    String salute = res.locals().get("salute").toString();
                    String name = req.param("name");
                    res.send(String.format("Hello %s %s", salute, name));
                });

        var router = app.Router();

        // simple logger for this router's requests
        // all requests to this router will first hit this middleware
        router.use((req, res, next) -> {
            System.out.printf("%s %s %s\n", req.method(), req.baseUrl(), req.path());
            next.ok();
        });

        // this will only be invoked if the path starts with /bar from the mount point
        router.use("/bar", (req, res, next) -> {
            // ... maybe some additional /bar logging ...
            System.out.printf("%s %s %s\n", req.method(), req.baseUrl(), req.path());
            next.ok();
        });

        // always invoked
        router.use((req, res, next) -> {
            res.send("Hello World");
        });

        // registering mount point for router
        app.use("/foo", router);

        app.listen(port, (console) -> {
            console.printf("Express demo app listening on port %s\n", port);
        });
    }
}
