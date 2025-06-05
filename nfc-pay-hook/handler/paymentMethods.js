const {checkoutAPI, requestOptions} = require('../client');

module.exports.paymentMethods = function(req, res) {

    const {amount, currency, countryCode} = req.body;
    console.log(amount, currency, countryCode, process.env.ADYEN_MERCHANT, process.env.ADYEN_API_KEY, process.env.ADYEN_CLIENT_KEY);

    // Create the request object(s)
    const paymentMethodsRequest = {
        merchantAccount: process.env.ADYEN_MERCHANT,
        countryCode,
        amount: {
            currency,
            value: amount
        },
        channel: "Android",
        shopperLocale: "en-US"
    }

    // Send the request
    const response = checkoutAPI.PaymentsApi.paymentMethods(paymentMethodsRequest, requestOptions);
    res.status(200).send(response);
}