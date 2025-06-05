const express = require('express');
const {paymentMethods} = require('../handler/paymentMethods');
const {paymentSession, paymentConfirmed} = require('../handler/paymentSession');
const router = express.Router();

/* POST accept webhooks */
router.post('/webhook', function (req, res, next) {
    const body = req.body;
    console.log(body);
    res.status(201).end();
});

router.post('/paymentMethods', paymentMethods)
router.post('/paymentSession', paymentSession)
router.post('/paymentConfirmed', paymentConfirmed);

module.exports = router;
