var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var contactSchema = new Schema({
    name: String,
    
});

module.exports = mongoose.model('account', accountSchema);