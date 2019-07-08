module.exports = function(app, Account, Place)
{
  var lastAccess = 0;
  const Contact = require('../models/contact');
  const Review = require('../models/review');
    //get all accounts
   app.get('/accounts', function(req, res){
      Account.find(function(err, accounts){
         if(err) return res.status(500).json({error: 'database failure'});
         res.json(accounts);
     })
  });

  // register account
  app.post('/register', function(req, res){
    Account.findOne({userName: req.body.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(account) return res.status(401).json({error: 'existing username'})
      
      var newAccount = new Account();
      newAccount.userName = req.body.userName;
      newAccount.password = req.body.password;

      newAccount.save(function(err){
        if(err) return res.status(500).json({error: 'failed to register'});
        return res.json({message: 'registered'});
      })
    })
  });

  // login and return username
  app.post('/login', function(req, res){
    Account.findOne({userName: req.body.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(404).json({error: 'account not found'});
      if(account.password != req.body.password) return res.status(401).json({error: 'password incorrect'});
      res.json({userName: account.userName, photoCount: account.photoCount});
    })
  });

  // get contact list
  app.get('/contacts/:userName', function(req, res){
    Account.findOne({userName: req.params.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(404).json({error: 'account not found'});
      res.json(account.contacts);
    })
  });

  // add contact
  app.post('/contacts/:userName', function(req, res){
    Account.findOne({userName: req.params.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(404).json({error: 'account not found'});
      
      var contact = new Contact();
      if(req.body.name) contact.name = req.body.name;
      if(req.body.phoneNumber) contact.phoneNumber = req.body.phoneNumber;
      account.contacts.push(contact);
      
      account.save(function(err){
        if(err) return res.status(500).json({error: 'failed to update'});
        res.json({message: 'updated'});
      })
    })
  });

//get all place info
  app.get('/places', function(req, res){
    today = new Date().getDay();
    if(lastAccess != today){
      deleteReviews('place1');
      deleteReviews('place2');
      deleteReviews('place3');
      deleteReviews('place4');
      deleteReviews('place5');
      lastAccess = today;
    }
    var query = Place.find().select("-reviews");
    query.exec(function(err, places){
      if(err) return res.status(500).send({error: 'database failure'});
      res.json(places);
    })
  });

  app.post('/places', function(req, res){
    var place = new Place();
    if(req.body.name) place.name = req.body.name;
    if(req.body.average) place.average = req.body.average;
    else place.average = 0;
    place.reviewCount = 0;

    place.save(function(err){
      if(err) return res.status(500).json({error: 'failed to update'});
      res.json({message: 'updated'});
    })
  })

//get all reviews of a certain place
  app.get('/:place/reviews', function(req, res){
    Place.findOne({name: req.params.place}, function(err, place){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!place) return res.status(404).json({error: 'place not found'});

      res.json(place.reviews);
    })
  });

  //add new review
  app.post('/:place/reviews', function(req, res){
    
    Place.findOne({name: req.params.place}, function(err, place){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!place) return res.status(404).json({error: 'place not found'});
      
      var review = new Review();
      if(req.body.userName) review.userName = req.body.userName;
      if(req.body.rating) review.rating = req.body.rating;
      if(req.body.content) review.content = req.body.content;
      const cnt = place.reviewCount;
      const avg = place.average;
  
      place.reviews.unshift(review);
      place.average = (avg * cnt + 1.0 * req.body.rating) / (cnt + 1);
      place.reviewCount = cnt + 1;
      // place.makeModified('reviews');
      place.save(function(err){
        if(err) return res.status(500).json({error: 'failed to add review'});
        res.json({message: 'updated'});
      })
      
    })
  });

  function deleteReviews(placeName){
    Place.findOne({name: placeName}, function(err, place){
      if(err) return 'database failure';
      if(!place) return 'place not found';
      place.reviews = [];
      place.reviewCount = 0;
      place.save(function(err){
        if(err) return 'failed to save';
        return 'saved';
      })
    })
  };

  // // delete all reviews
  // app.delete('/reviews', function(req, res){
  //   Place.find().forEach(function(err, place){
  //     if(err) return res.status(500).json({ error: 'database failure' });
  //     if(!place) return res.status(404).json({error: 'place not found'});
  //     place.reviews = [];
  //     place.reviewCount = 0;
  //     place.save(function(err){
  //       if(err) return res.status(500).json({error: 'failed to delete'});
  //       res.json({message: 'deleted'});
  //     })
  //   })
  // });

  const multer = require('multer');
  // const path = require('path');
  const fs = require('fs');

  app.post('/photos/:userName', function(req, res){
    Account.findOne({userName: req.params.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(401).json({error: 'no such username'})
      
      const storage = multer.diskStorage({
        destination: './public/' + req.params.userName,
        filename: function(req, file, cb){
            cb(null, 'image-' + account.photoCount + '.png'/*path.extname(file.originalname)*/);
        }
      });
      
      const upload = multer({
        storage: storage
      }).fields([
        { name: 'image', maxCount: 1}
      ]);
  
      upload(req, res, function(err){
        if(err) return res.status(500).json({ error: 'upload error'});
        
        account.photoCount = account.photoCount + 1;
  
        account.save(function(err){
          if(err) return res.status(500).json({error: 'photoCount error'});
          res.json({message: 'upload successful'});
        })
      })
    })
  });

  //get photo at index
  app.get('/photos/:userName/:index', function(req, res){
    const filepath = './public/' + req.params.userName + '/image-'+ req.params.index;
    file = fs.readFileSync(filepath);
    res.write(file);
    res.end();
  });

  //get photocount
  app.get('/count/:userName/', function(req, res){
    Account.findOne({userName: req.params.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(401).json({error: 'no such username'})
      res.json({count: account.photoCount});
    })
  });

  //delete photo at index
  app.delete('/photos/:userName/:index', function(req, res){
    const filepath = './public/' + req.params.userName + '/image-'+ req.params.index +'.png';
    fs.unlink(filepath, function(err){
      if(err) return res.status(500).json({error: 'unlink error'});
      Account.findOne({userName: req.params.userName}, function(err, account){
        if(err) return res.status(500).json({error: 'database failure'});
        if(!account) return res.status(401).json({error: 'no such username'})
        var photoCount = account.photoCount;
        var i = req.params.index*1 + 1;
        var flag = 0;
        while(i < photoCount){
          fs.rename('./public/' + req.params.userName + '/image-' + i + '.png', './public/' + req.params.userName + '/image-' + (i-1) + '.png', function(err){
            if(err){
              flag = 1;
              return;
            }
          });
          i++;
        }
        if(flag) return res.status(500).json({error: 'rename failure'});
        account.photoCount = photoCount - 1;
        account.save(function(err){
          if(err) return res.status(500).json({error: 'photoCount error'});
          res.json({message: 'delete successful'});
        })
      })
    })
    
  });

}



