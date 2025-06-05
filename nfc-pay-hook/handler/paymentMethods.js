const {checkoutAPI, requestOptions} = require('../client');

module.exports.paymentMethods = function(req, res) {

    const {amount, currency, countryCode} = req.body;
    // console.log(amount, currency, countryCode);

    // Create the request object(s)
    const paymentMethodsRequest = {
        merchantAccount: process.env.MERCHANT_ACCOUNT,
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