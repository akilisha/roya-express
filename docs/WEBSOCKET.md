# WebSocket with Roya (Helidon v4)

Roya exposes WebSocket via a dedicated API:
- Use: `app.ws(path, listener)`

At `listen()`, Roya collects declared endpoints and installs them into Helidon `WsRouting`, which is added alongside the HTTP routing.

## Quick start

```java
var app = Roya.create();

app.ws("/ws/echo", new io.helidon.websocket.WsListener() {
  @Override public void onOpen(io.helidon.websocket.WsSession s) { s.send("connected"); }
  @Override public void onMessage(io.helidon.websocket.WsSession s, String text, boolean last) { s.send("echo: " + text); }
});

app.listen(3000);
```

WebSocket is not HTTP middleware; it is a separate protocol upgraded from HTTP. Use `app.ws(...)` to register endpoints.

## Notes
- Roya integrates WebSockets by building a `WsRouting` and adding it to the server routing during startup.
- Listener API comes from Helidon: `io.helidon.websocket.WsListener` and `io.helidon.websocket.WsSession`.
- For more advanced examples (broadcast, message board), see Helidon examples.

## References
- Helidon WebSocket docs: https://helidon.io/docs/v4/se/websocket
