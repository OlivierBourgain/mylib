# Mylib
Mylib is a personnal tool I developed to manage my books.

It uses Spring boot 2, with an H2 database, a search index using Lucene, and thymeleaf for the front end.

You can add a new book with its ISBN number (information is scrapped from amazon), and associate tags to books.
The application also provides basic export, and some statistics. 

The application runs with java 13

## Authentication
Authentication is done through Oauth2 with Google, so the application needs two variables.
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_CLIENT_ID`        

You can connect with your google account, and search book by ISBN.

## Storage
The data is stored in a local h2 database. 
The application also download pictures from books, which are stored as files in `~/mylib/store`.

## Starting the application
To start application, you need java 13.

```
source secret.sh
mvn spring-boot:run
```

where `secret.sh` file exports `GOOGLE_CLIENT_SECRET` and `GOOGLE_CLIENT_ID`

## Deployment
Run `mvn package` and deploy the `target\mylib-x.y.jar` on your server.
Then run the app with `java -jar mylib-x.y.jar`.

The application will create 3 subdirectories : `db`, `store` and `search`.



## REST API

The REST API is available under `/api` and uses JSON. It requires a Google
**ID token** issued for `GOOGLE_CLIENT_ID` in every request:

```http
Authorization: Bearer <google-id-token>
```

Obtain the ID token through Google OpenID Connect (for example, Google Identity
Services using this application's client ID). Google access tokens and the web
UI's session cookie do not authenticate API requests. Tokens are checked against
Google's signing keys, issuer, expiration and client ID audience; the Google
`sub` identifies the owner, matching the existing web application. No additional
credentials or application-issued tokens are needed. Keep using
`GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` for the existing web sign-in.

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/books` | List the authenticated user's books |
| GET | `/api/books/{id}` | Get one of the user's books |
| POST | `/api/books` | Create a book |
| GET | `/api/readings` | List the authenticated user's readings |
| POST | `/api/readings` | Add a reading for one of the user's books |

Both list endpoints accept zero-based `page` (default `0`) and `size` (default
`100`, allowed range `1`–`100`), ordered by ID ascending. They return
`{"content": [...], "page": 0, "size": 100, "totalElements": 1, "totalPages": 1}`.
Book list entries and book details contain the book's metadata, timestamps and
`tags`. Each tag contains `id`, `text`, `color`, `backgroundColor`, `borderColor`
and `priority`. Tags are ordered by ID ascending; untagged books return `tags: []`.
Reading responses contain `id`, `bookId`, `date`, `year`, `month`, `created` and
`updated`. Internal user IDs and tag-to-book back references are not serialized.

Create a book with a required nonblank `title` and optional `subtitle`, `author`,
`isbn`, `publisher`, `publicationDate`, `status`, `pages`, `lang`, `comment` and
`summary`. `pages` defaults to zero and must be nonnegative. `status` accepts
`TO_READ`, `DISCARDED`, `STORED` or `ON_SHELF`. Text fields allow up to 255
characters except `comment`, which allows 4096. IDs, ownership and timestamps
are assigned by the server. Creation also updates the existing Lucene index.

```sh
curl 'http://localhost:2017/api/books?page=0&size=20' \
  -H "Authorization: Bearer $GOOGLE_ID_TOKEN"

curl 'http://localhost:2017/api/books' \
  -H "Authorization: Bearer $GOOGLE_ID_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Dune","author":"Frank Herbert","pages":412,"status":"ON_SHELF"}'

curl 'http://localhost:2017/api/readings' \
  -H "Authorization: Bearer $GOOGLE_ID_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"bookId":1,"date":"2026-11-17"}'
```

Both POST endpoints return `201` with the created resource. Book creation also
returns a `Location` header. Dates use ISO `YYYY-MM-DD`. Invalid requests return
`400` problem details; missing/invalid tokens return `401` with a bearer challenge.
A missing book or a book belonging to another user returns `404` for both detail
and reading creation.

The current build targets Java 23 and Spring Boot 3.2.4 (the older Java 13 /
Spring Boot 2 references above are historical). Run `mvn test` and `mvn package`
with JDK 23 or newer. Byte Buddy is pinned to a version compatible with JDK 25,
so tests do not require experimental JVM flags.
