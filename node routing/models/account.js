var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var accountSchema = new Schema({
    email: String,
    name: String,
    password: String,
    details: {
        contact_id: String,
        gallery_id: String
    }
});

module.exports = mongoose.model('account', accountSchema);