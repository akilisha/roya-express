const express = require('express');
const router = express.Router();

const pages = require('./pages');
const webhook = require('./payments');

router.use('/',pages);
router.use('/pay',webhook);

module.exports = router;
