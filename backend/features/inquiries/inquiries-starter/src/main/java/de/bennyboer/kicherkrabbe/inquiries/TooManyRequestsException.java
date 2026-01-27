package de.bennyboer.kicherkrabbe.inquiries;

public class TooManyRequestsException extends Exception {

    public TooManyRequestsException() {
        super("Too many requests");
    }

}
