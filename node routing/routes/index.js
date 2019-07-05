module.exports = function(app, Account)
{
    //GET ALL BOOKS
   app.get('/api/accounts', function(req,res){
      Account.find(function(err, accounts){
         if(err) return res.status(500).send({error: 'database failure'});
         res.json(accounts);
     })
  });

  // GET SINGLE BOOK
  app.get('/api/books/:book_id', function(req, res){
    Account.findOne({_id: req.params.book_id}, function(err, book){
        if(err) return res.status(500).json({error: err});
        if(!book) return res.status(404).json({error: 'book not found'});
        res.json(book);
    })
  });

  // GET BOOK BY AUTHOR
  app.get('/api/books/author/:author', function(req, res){
    Account.find({author: req.params.author}, {_id: 0, title: 1, published_date: 1},  function(err, books){
        if(err) return res.status(500).json({error: err});
        if(books.length === 0) return res.status(404).json({error: 'book not found'});
        res.json(books);
    })
  });

  // register account
  app.post('/register', function(req, res){
      var account = new Account();
      account.email = req.body.email;
      account.name = req.body.author;
      account.password = req.body.author;
      account.

      

      account.save(function(err){
         if(err){
            console.error(err);
            res.json({result: 0});
            return;
         }

         res.json({result: 1});

      });
  });

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