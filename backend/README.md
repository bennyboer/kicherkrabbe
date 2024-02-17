# Backend

## Start for development

```
.\gradlew.bat :app:run --args="dev"
```

## Start for production

First and foremost you'll need to generate a certificate (See section about certificates).

```
.\gradlew.bat :app:run
```

## Build and run Jar

First and foremost you'll need to generate a certificate (See section about certificates).

```
.\gradlew.bat :app:build
java -jar .\app\build\libs\app.jar
```

## Generate a certificate

```
TODO
```
