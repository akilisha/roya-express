// Require the parts of the module you want to use.
const {Client, CheckoutAPI, Types} = require("@adyen/api-library");

// Set up the client and service.
const {ADYEN_API_KEY, ADYEN_ENV} = process.env;
const client = new Client({apiKey: ADYEN_API_KEY, environment: ADYEN_ENV});

const checkoutAPI = new CheckoutAPI(client);

// Include your idempotency key when you make an API request.
const requestOptions = {idempotencyKey: "ws_231961@Company.CraftedOn"};

module.exports = {
    checkoutAPI,
    requestOptions,
}