module.exports = function(app, Account)
{
  var Contact = require('../models/contact');
    //get all accounts
   app.get('/accounts', function(req, res){
      Account.find(function(err, accounts){
         if(err) return res.status(500).send({error: 'database failure'});
         res.json(accounts);
     })
  });

  // register account
  app.post('/register', function(req, res){
    var account = new Account();
    account.userName = req.body.userName;
    account.password = req.body.password;

    account.save(function(err){
       if(err){
          console.error(err);
          res.json({result: 0});
          return;
       }

       res.json({result: 1});

    });
  });

  // login and return username
  app.post('/login', function(req, res){
    Account.findOne({userName: req.body.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(404).json({error: 'account not found'})
      if(account.password != req.body.password) return res.status(401).json({error: 'password incorrect'});
      res.json(account.userName);
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
    // var contact = new Contact();
    // contact.name = req.body.name;
    // contact.phoneNumber = req.body.phoneNumber;

    Account.findOne({userName: req.params.userName}, function(err, account){
      if(err) return res.status(500).json({error: 'database failure'});
      if(!account) return res.status(404).json({error: 'account not found'});
      
      var contact = new Contact();
      if(req.body.name) contact.name = req.body.name;
      if(req.body.phoneNumber) contact.phoneNumber = req.body.phoneNumber;
      account.contacts.push(contact);
      
      account.save(function(err){
        if(err) res.status(500).json({error: 'failed to update'});
        res.json({message: 'updated'});
      })
    })
  });


  //   contact.save(function(err){
  //      if(err){
  //         console.error(err);
  //         res.json({result: 0});
  //         return;
  //      }

  //      res.json({result: 1});

  //   });
  // });

  

  // UPDATE THE BOOK
  app.put('/api/books/:book_id', function(req, res){
    Book.findById(req.params.book_id, function(err, book){
        if(err) return res.status(500).json({ error: 'database failure' });
        if(!book) return res.status(404).json({ error: 'book not found' });

        if(req.body.title) book.title = req.body.title;
        if(req.body.author) book.author = req.body.author;
        if(req.body.published_date) book.published_date = req.body.published_date;

        book.save(function(err){
            if(err) res.status(500).json({error: 'failed to update'});
            res.json({message: 'book updated'});
        });

    });
  });

  // DELETE BOOK
  app.delete('/api/books/:book_id', function(req, res){
    Book.remove({ _id: req.params.book_id }, function(err, output){
        if(err) return res.status(500).json({ error: "database failure" });

        /* ( SINCE DELETE OPERATION IS IDEMPOTENT, NO NEED TO SPECIFY )
        if(!output.result.n) return res.status(404).json({ error: "book not found" });
        res.json({ message: "book deleted" });
        */

        res.status(204).end();
    })
  });
}