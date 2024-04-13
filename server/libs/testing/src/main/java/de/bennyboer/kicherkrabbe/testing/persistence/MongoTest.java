package de.bennyboer.kicherkrabbe.testing.persistence;

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@DataMongoTest
@ContextConfiguration(initializers = {MongoTestSupport.Initializer.class})
public @interface MongoTest {

}
