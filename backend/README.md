# Kicherkrabbe backend

## Getting started

- Install Java 25
- Make sure to have Docker installed if you want to run the tests
- Generate a key pair for generating JWTs for authentication (for example by using
  OpenSSL): `openssl ecparam -genkey -name secp521r1 -noout -out ./backend/apps/api/src/main/resources/keys/key_pair.pem`
- If you want to run the server locally you will have to have a MongoDB instance running (currently on localhost:27017) and a RabbitMQ instance running (currently on localhost:5672)
  - For example by installing MongoDB locally and running `mongod` in the terminal (make sure to have replica set enabled with name `rs0`)
  - For RabbitMQ you can use the official Docker image: `docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4.0.5-management`
  - Or you simply navigate to `backend` and run `docker-compose up` to start both services
- Run the server by starting the `ServerApplication` class in the `apps/api` module. Make sure to activate the `dev` profile if you want to do some local frontend development.

## Build the server

- Make sure to build the frontend beforehand with `yarn build` in the `frontend/management` project.
- Run `./gradlew :apps:api:bootJar` in the `backend` project to build the server JAR file.
- The JAR file will be located in `backend/apps/api/build/libs`.
- You can run the server by executing `java -jar backend/apps/api/build/libs/api.jar`.