var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var reviewSchema = new Schema({
    userName: String,
    rating: Number,
    content: String
});

module.exports = mongoose.model('review', reviewSchema);