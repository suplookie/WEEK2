//Import package
var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');
var fs = require('fs');

//PASSWORD UTILS
//CREATE FUNCTION TO RANDOM SALT
var genRandomString = function(length) {
    return crypto.randomBytes(Math.ceil(length/2))
        .toString('hex') /*convert to hexa format*/
        .slice(0, length);
}

var sha512 = function(password, salt) {
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt:salt,
        passwordHash:value
    };
};

function saltHashPassword(userPassword) {
    var salt = genRandomString(16); //Create 16 random character
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword, salt)
{
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

//Create Express Service
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

//Create MongoDB Client
var MongoClient = mongodb.MongoClient;

//Connection URL
var url = 'mongodb://localhost:27017' //27017 is default port

MongoClient.connect(url, {useNewUrlParser:true}, function(err, client) {
    if (err)
        console.log('Unable to connect to the mongoDB server.Error', err);
    else {

        //Register
        app.post('/register', (request, response, next)=>{
            var post_data = request.body;

            var plaint_password = post_data.password;
            var hash_data = saltHashPassword(plaint_password);

            var password = hash_data.passwordHash; //Save password hash
            var salt = hash_data.salt; //Save salt

            var name = post_data.name;
            var email = post_data.email;

            var insertJson = {
                'email': email,
                'password': password,
                'salt': salt,
                'name': name
            };
            var db = client.db('edmtdevnodejs');

            //Check exists email
            db.collection('user')
                .find({'email':email}).count(function(err, number) {
                    if (number != 0)
                    {
                        response.json('Email already exists');
                        console.log('Email already exists');
                    }
                    else {
                        //Insert data
                        db.collection('user')
                            .insertOne(insertJson, function(error, res) {
                                response.json('Registration success');
                                console.log('Registration success');
                            })
                    }
                })
        })

        app.post('/login', (request, response, next)=>{
            var post_data = request.body;

            
            var email = post_data.email;
            var userPassword = post_data.password;

           
            var db = client.db('edmtdevnodejs');

            //Check exists email
            db.collection('user')
                .find({'email':email}).count(function(err, number) {
                    if (number == 0)
                    {
                        response.json('Email does not exist');
                        console.log('Email does not exist');
                    }
                    else {
                        //Find data
                        db.collection('user')
                            .findOne({'email':email}, function(err,user) {
                                var salt = user.salt; // Get salt from user
                                var hashed_password = checkHashPassword(userPassword, salt).passwordHash; //Hash password with salt
                                var encrypted_password = user.password; //Get password from user
                                if (hashed_password == encrypted_password)
                                {
                                    //response.json('Login success');
                                    //response.send(user.name);
                                    //console.log('Login success');
                                    response.json(user.name);
                                    console.log(user.name);
                                }
                                else {
                                    response.json('Wrong password');
                                    console.log('Wrong password');
                                }
                            })
                    }
                })
        })

        app.delete('/delete', (request, response, next)=>{
            var post_data = request.body;

            var email = post_data.email;
            var userPassword = post_data.password;

            var db = client.db('edmtdevnodejs');

                //Check exists email
                db.collection('user')
                .find({'email':email}).count(function(err, number) {
                    if (number == 0)
                    {
                        response.json('Email does not exist');
                        console.log('Email does not exist');
                    }
                    else {
                        //Insert data
                        db.collection('user')
                            .findOne({'email':email}, function(err,user) {
                                var salt = user.salt; // Get salt from user
                                var hashed_password = checkHashPassword(userPassword, salt).passwordHash; //Hash password with salt
                                var encrypted_password = user.password; //Get password from user
                                if (hashed_password == encrypted_password)
                                {
                                    db.collection('user').findOneAndDelete({'email':email}, function(err, user) {
                                        if (err) {
                                            response.json('Delete failed');
                                            console.log('Delete failed');
                                        }
                                        else {
                                            response.json('Delete succeeded');
                                            console.log('Delete succeeded');
                                        }
                                    });
                                }
                                else {
                                    response.json('Wrong password');
                                    console.log('Wrong password');
                                }
                            })
                    }
                })
        })



        //Second Fragment

        app.post('/upload', function(req, res) {
            console.log(req.files.image.originalFilename);
            console.log(req.files.image.path);
                fs.readFile(req.files.image.path, function (err, data){
                var dirname = "/Users/ q/Desktop/images";
                var newPath = dirname + "/uploads/" + 	req.files.image.originalFilename;
                fs.writeFile(newPath, data, function (err) {
                if(err){
                res.json({'response':"Error"});
                console.log('response: Error');
                }else {
                res.json({'response':"Saved"});
                console.log('response: Saved');
        }
        });
        });
        });
        
        
        app.get('/uploads/:file', function (req, res){
                file = req.params.file;
                var dirname = "/home/rajamalw/Node/file-upload";
                var img = fs.readFileSync(dirname + "/uploads/" + file);
                res.writeHead(200, {'Content-Type': 'image/jpg' });
                res.end(img, 'binary');
        
        });


        //Start Web Server
        app.listen(3000, ()=>{
            console.log('Connected to MongoDB Server, WebService running on port 3000');
        })
    }
})