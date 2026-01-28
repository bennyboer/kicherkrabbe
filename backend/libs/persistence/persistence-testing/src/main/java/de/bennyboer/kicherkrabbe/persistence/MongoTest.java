package de.bennyboer.kicherkrabbe.persistence;

import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@DataMongoTest
@ContextConfiguration(classes = MongoTestConfig.class)
public @interface MongoTest {

}
