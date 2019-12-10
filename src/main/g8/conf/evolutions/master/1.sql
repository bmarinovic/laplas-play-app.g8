-- Users schema

-- !Ups
CREATE TABLE users (
  id            BIGSERIAL PRIMARY KEY,
  email         VARCHAR (255) NOT NULL UNIQUE,
  first_name    VARCHAR (255) NOT NULL,
  last_name     VARCHAR (255) NOT NULL,
  password      VARCHAR (255),
  role          VARCHAR (255) NOT NULL,
  created_at    TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  updated_at    TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);
INSERT INTO users (email, first_name, last_name, password, role) VALUES ('admin@laplacian.hr', 'Admin', 'Adminic', '\$2a\$10\$GqiG8fWtmajdNFJ4YhFvLu.86Oh3ITJb.0DWqsbPc5SWpptVQd/bG', 'Admin');

-- !Downs
DROP TABLE users;