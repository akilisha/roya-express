// Require the parts of the module you want to use.
const {Client, CheckoutAPI, Types} = require("@adyen/api-library");

// Set up the client and service.
const {ADYEN_API_KEY, ADYEN_ENV} = process.env;
console.log(`ADYEN_ENV ===============> ${ADYEN_ENV}`);
const client = new Client({apiKey: ADYEN_API_KEY, environment: ADYEN_ENV});

module.exports.checkoutAPI = new CheckoutAPI(client);

// Include your idempotency key when you make an API request.
module.exports.requestOptions = {idempotencyKey: "YOUR_IDEMPOTENCY_KEY"};
