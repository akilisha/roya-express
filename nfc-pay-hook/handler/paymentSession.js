const {checkoutAPI, requestOptions} = require('../client');

const paymentSession = async function(req, res) {
    
    console.log('Payment Session Request');
    const {amount, currency, countryCode} = req.body;
    const {ADYEN_MERCHANT, PAYMENT_RETURN_URL} = process.env;

    // Create the request object(s)
    const createCheckoutSessionRequest = {
        merchantAccount: ADYEN_MERCHANT,
        countryCode,
        amount: {
            currency,
            value: amount
        },
        returnUrl: PAYMENT_RETURN_URL,
        reference: crypto.randomUUID(),
    }

    // Send the request
    const response = await checkoutAPI.PaymentsApi.sessions(createCheckoutSessionRequest, requestOptions);
    res.status(200).send(response);
}

const paymentConfirmed = function(req, res) {
    console.log('Payment Confirmed', req.body);
    res.status(200).end();
}

module.exports = {
    paymentSession,
    paymentConfirmed
}