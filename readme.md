# Mylib
Mylib is a tool I developed to manage my books.

You can add a new book with its ISBN number (information is scrapped from amazon), and associate tags to books.
The application also provides basic export, and some statistics. 


## Authentication
Authentication is done through Oauth2 with Google, so the application needs two variables.
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_CLIENT_ID`        

You can connect with your google account, and search book by ISBN.


## Storage
The data is stored in a local h2 database. 
The application also download pictures from books, which are stored as files in `~/mylib/store`.

## Starting the application
To start application:

```
source secret.sh
mvn spring-boot:run
```

where `secret.sh` file exports `GOOGLE_CLIENT_SECRET` and `GOOGLE_CLIENT_ID`

