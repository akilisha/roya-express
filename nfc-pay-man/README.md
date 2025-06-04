## Java Payment Backend with Embedded Jetty 11

This project demonstrates a Java-based payment backend application using embedded Jetty 11, designed to illustrate various features for a payment processing system.

### Features:
1.  **Embedded Jetty 11:** The application uses Jetty 11 directly embedded within the `Main` class.
2.  **Dynamic Handler Loading:** Service endpoints (servlets) are dynamically loaded using Java's `ServiceLoader` mechanism, promoting a pluggable architecture.
3.  **Dependency Injection (Manual/ServiceLoader):** Dependencies for handlers (e.g., `PaymentService`) are manually injected after loading via `ServiceLoader`. In a full-fledged enterprise application, this would typically be managed by a CDI (Contexts and Dependency Injection) framework like Weld SE or Spring.
4.  **Resource Handler:** Serves static HTML content from `src/main/resources/static`.
5.  **Session Management:** Configured with Jetty's `SessionHandler` using `JDBCSessionManager` backed by an H2 database. This ensures session persistence across server restarts (for the H2 database file).
6.  **Server-Sent Events (SSE) Endpoint:** (`/sse`) Allows clients to subscribe for real-time, targeted updates (e.g., payment status notifications).
7.  **OAuth Endpoint:** (`/auth/oauth`) Simulates single-sign-on authentication with an OAuth provider (like Google or Auth0). It demonstrates initiating the flow and handling a mock callback.
8.  **Device Management Endpoint:** (`/device`) Provides endpoints for registering, updating, and unregistering phone devices, uniquely identifying them for payment transactions. (Uses an in-memory store for demo).
9.  **Command Endpoint:** (`/command`) A generic endpoint to accept JSON requests as commands, which can be dispatched to various services (simulated processing).
10. **Payment Methods Endpoint:** (`/payment-methods`) Simulates requesting available payment methods from a payment service provider (PSP).
11. **QR Code Endpoint:** (`/qr-code`) Simulates requesting a QR code with encoded payment details and selected payment options.
12. **Payment Processing Endpoint:** (`/process-payment`) Accepts payment requests for processing through a simulated payment service provider. It also triggers an SSE update to the relevant client upon completion.

### Project Structure:
```
java-payment-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/payment/
│   │   │       ├── Main.java
│   │   │       ├── handler/
│   │   │       │   ├── BaseHandler.java
│   │   │       │   ├── CommandHandler.java
│   │   │       │   ├── DeviceHandler.java
│   │   │       │   ├── OAuthHandler.java
│   │   │       │   ├── PaymentMethodHandler.java
│   │   │       │   ├── PaymentProcessingHandler.java
│   │   │       │   └── QrCodeHandler.java
│   │   │       │   └── SseHandler.java
│   │   │       ├── model/
│   │   │       │   └── Device.java
│   │   │       ├── service/
│   │   │       │   ├── DeviceService.java
│   │   │       │   ├── PaymentService.java
│   │   │       │   └── QrCodeService.java
│   │   │       └── sse/
│   │   │           └── SsePublisher.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html
│   │       └── META-INF/
│   │           └── services/
│   │               └── com.akilisha.oss.payman.handler.BaseHandler
│   └── test/
│       └── ... (for unit tests)
└── README.md
```

### Setup and Running:

1.  **Prerequisites:**
    * Java Development Kit (JDK) 11 or higher
    * Maven (version 3.6.0 or higher)

2.  **Build the Project:**
    Navigate to the `java-payment-backend` directory in your terminal and run:
    ```bash
    mvn clean package
    ```
    This command compiles the code, runs tests, and packages the application into a single executable JAR file in the `target/` directory. The `maven-shade-plugin` is configured to create a "fat JAR" with all dependencies included.

3.  **Run the Application:**
    After a successful build, run the JAR file:
    ```bash
    java -jar target/java-payment-backend-1.0-SNAPSHOT.jar
    ```
    You should see console output indicating that Jetty server has started on port 8080.

4.  **Access the Application:**
    Open your web browser and go to:
    ```
    http://localhost:8080/
    ```
    You will see the `index.html` static page.

### Testing Endpoints:

You can use tools like `curl`, Postman, Insomnia, or even your browser's developer console (for GET requests and SSE) to test the endpoints.

**Example `curl` commands:**

* **Access Static Page:**
    ```bash
    curl http://localhost:8080/
    ```

* **Initiate OAuth (Simulated):**
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"provider": "Google"}' http://localhost:8080/auth/oauth
    ```
  (Look for the `redirectUrl` in the response, which you'd normally navigate to in a browser).

* **Register Device:**
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"deviceId": "phone-123", "userId": "user-abc", "deviceName": "My Android Phone"}' http://localhost:8080/device
    ```

* **Get Device:**
    ```bash
    curl http://localhost:8080/device?deviceId=phone-123
    ```

* **Request Payment Methods:**
    ```bash
    curl http://localhost:8080/payment-methods
    ```

* **Request QR Code:**
    ```bash
    curl "http://localhost:8080/qr-code?amount=10.50&description=Coffee&paymentOption=Visa"
    ```

* **Process Payment (Simulated):**
    ```bash
    curl -X POST -H "Content-Type: application/json" -d '{"amount": 25.00, "currency": "USD", "paymentToken": "mock_token_xyz", "description": "Lunch"}' http://localhost:8080/process-payment
    ```

* **Subscribe to SSE (in browser):**
  Open `http://localhost:8080/sse` in your browser. You will see a blank page, but check the browser's developer console (Network tab, EventStream filter) for incoming events after you process a payment.

### H2 Database:
The H2 database file (`sessions.mv.db` and `sessions.trace.db`) will be created in a `./h2db` directory relative to where you run the JAR. This stores session data persistently.

### CDI Integration (Conceptual):
While the example manually injects dependencies, in a real application, you would add a CDI implementation (like Weld SE) to your `pom.xml`. You would then annotate your services (`@ApplicationScoped`, `@Dependent`) and handlers (`@Inject`) and use Weld's bootstrapping mechanism in `Main.java` to manage the lifecycle and injection of your components. This would remove the need for manual `setService` calls in `Main`.

### Security Notes:
* This is a **demonstration** application.
* **NO REAL SECURITY MEASURES** are implemented for OAuth token validation, secure session handling (beyond basic JDBC persistence), or payment data encryption.
* The mock encryption (Base64) is **NOT secure**.
* **DO NOT** use this code in a production environment without implementing proper authentication, authorization, input validation, error handling, logging, and robust security practices.
* Payment processing should always involve PCI DSS compliant solutions and secure server-to-server communication with PSPs.
