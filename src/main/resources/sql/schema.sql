CREATE TABLE user
(
  user_id    INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  photo      LONGBLOB      NULL    ,
  first_name VARCHAR(64)   NOT NULL,
  last_name  VARCHAR(64)   NOT NULL,
  bio        VARCHAR(1024) NULL    ,
  created_at DATETIME      NOT NULL DEFAULT NOW(),
  updated_at DATETIME      NOT NULL DEFAULT NOW(),
  PRIMARY KEY (user_id)
);

CREATE TABLE book
(
  book_id     INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  cover       LONGBLOB      NULL    ,
  title       VARCHAR(128)  NOT NULL,
  author      VARCHAR(128)  NULL    ,
  description VARCHAR(1024) NULL    ,
  published   BOOLEAN       NOT NULL DEFAULT false,
  created_at DATETIME       NOT NULL DEFAULT NOW(),
  updated_at DATETIME       NOT NULL DEFAULT NOW(),
  PRIMARY KEY (book_id)
);

CREATE TABLE role
(
  role_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  type    VARCHAR(16)  NOT NULL,
  PRIMARY KEY (role_id)
);

CREATE TABLE book_metadata
(
  book_id      INT UNSIGNED NOT NULL,
  cover_type   VARCHAR(16)  NOT NULL,
  pages        INT UNSIGNED NOT NULL,
  publisher    VARCHAR(128) NOT NULL,
  publish_date VARCHAR(16)  NOT NULL,
  views        INT UNSIGNED NOT NULL DEFAULT 0,
  isbn10       VARCHAR(64)  NULL    ,
  isbn13       VARCHAR(64)  NULL    ,
  FOREIGN KEY (book_id) REFERENCES book (book_id)
);

CREATE TABLE user_role
(
  user_id INT UNSIGNED NOT NULL,
  role_id INT UNSIGNED NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user (user_id),
  FOREIGN KEY (role_id) REFERENCES role (role_id)
);

CREATE TABLE book_genre
(
  book_id INT UNSIGNED NOT NULL,
  type    VARCHAR(16)  NOT NULL,
  FOREIGN KEY (book_id) REFERENCES book (book_id)
);

CREATE TABLE user_history
(
  user_id    INT UNSIGNED NOT NULL,
  book_id    INT UNSIGNED NOT NULL,
  action     VARCHAR(16)  NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT NOW(),
  FOREIGN KEY (user_id) REFERENCES user (user_id),
  FOREIGN KEY (book_id) REFERENCES book (book_id)
);

CREATE TABLE user_metadata
(
  user_id  INT UNSIGNED NOT NULL,
  username VARCHAR(64)  NOT NULL,
  password VARCHAR(64)  NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE TABLE favourite_book
(
  user_id    INT UNSIGNED NOT NULL AUTO_INCREMENT,
  book_id    INT UNSIGNED NOT NULL DEFAULT 0,
  created_at DATETIME     NOT NULL DEFAULT NOW(),
  FOREIGN KEY (user_id) REFERENCES user (user_id),
  FOREIGN KEY (book_id) REFERENCES book (book_id)
);
