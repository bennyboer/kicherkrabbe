# Kicherkrabbe server

## Getting started

- Install Java 21
- Generate a key pair for generating JWTs for authentication (for example by using
  OpenSSL): `openssl ecparam -genkey -name secp521r1 -noout -out ./server/app/src/main/resources/keys/key_pair.pem`
