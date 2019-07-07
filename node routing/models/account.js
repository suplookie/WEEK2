var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var contactSchema = new Schema({
    name: String,
    phoneNumber: String
});

var accountSchema = new Schema({
    userName: String,
    password: String,
    contacts : [contactSchema]
});

module.exports = mongoose.model('account', accountSchema);