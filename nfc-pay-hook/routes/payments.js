const express = require('express');
const {paymentMethods} = require('../handler/paymentMethods');
const router = express.Router();

/* POST accept webhooks */
router.post('/webhook', function (req, res, next) {
    const body = req.body;
    console.log(body);
    res.status(201).end();
});

router.post('/paymentMethods', paymentMethods)

module.exports = router;
