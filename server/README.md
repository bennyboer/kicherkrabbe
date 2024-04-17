# Kicherkrabbe server

## Getting started

- Install Java 21
- Make sure to have Docker installed if you want to run the tests
- Generate a key pair for generating JWTs for authentication (for example by using
  OpenSSL): `openssl ecparam -genkey -name secp521r1 -noout -out ./server/app/src/main/resources/keys/key_pair.pem`
- If you want to run the server locally you will have to have a MongoDB instance running (currently on localhost:27017)
