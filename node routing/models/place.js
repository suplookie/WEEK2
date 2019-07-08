var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var reviewSchema = new Schema({
    userName: String,
    rating: Number,
    content: String
});

var placeSchema = new Schema({
    name: String,
    average: Number,
    reviewCount: Number,
    reviews : [reviewSchema]
});

module.exports = mongoose.model('place', placeSchema);