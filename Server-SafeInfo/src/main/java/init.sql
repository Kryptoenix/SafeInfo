-- Table for storing user information
CREATE TABLE users (
                       id INT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(20),
                       passwordHash VARCHAR(65),
                       phoneNumber VARCHAR(14),
                       publicKey TEXT NOT NULL,
                       PRIMARY KEY (id)
);

-- Table for storing chat logic information
CREATE TABLE chat (
                      chat_id VARCHAR(64) PRIMARY KEY,
                      user1 VARCHAR(20),
                      ua1 BOOLEAN,
                      prevReqTs1 VARCHAR(30),
                      user2 VARCHAR(20),
                      ua2 BOOLEAN,
                      prevReqTs2 VARCHAR(30)
);