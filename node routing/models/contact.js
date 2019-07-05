var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var ObjectId = Schema.Types.ObjectId;

var contactSchema = new Schema({
    name: String,
    phoneNumber: String
});

module.exports = mongoose.model('contact', contactSchema);