var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var contactSchema = new Schema({
    name: String,
    phoneNumber: String
});

var accountSchema = new Schema({
    userName: String,
    password: String,
    contacts : [contactSchema],
    photoCount: {type: Number, default: 0}
});

module.exports = mongoose.model('account', accountSchema);