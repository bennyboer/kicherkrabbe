package de.bennyboer.kicherkrabbe.mailing.mail;

public class RateLimitExceededError extends RuntimeException {

    public RateLimitExceededError() {
        super("Rate limit exceeded");
    }

}
