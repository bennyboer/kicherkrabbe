package de.bennyboer.kicherkrabbe.auth.password;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class PasswordEncoder {

    private Argon2PasswordEncoder encoder;

    private static final class InstanceHolder {

        static final PasswordEncoder INSTANCE = new PasswordEncoder();

    }

    private PasswordEncoder() {
        encoder = new Argon2PasswordEncoder(16, 32, 1, 16384, 2);
    }

    public static PasswordEncoder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * This method is only for testing purposes.
     * It enables a test profile with a faster encoder.
     */
    public void enableTestProfile() {
        encoder = new Argon2PasswordEncoder(16, 32, 1, 4096, 2);
    }

    public String encode(CharSequence password) {
        return encoder.encode(password);
    }

    public boolean matches(CharSequence password, String encodedPassword) {
        return encoder.matches(password, encodedPassword);
    }

    public org.springframework.security.crypto.password.PasswordEncoder getInternalEncoder() {
        return encoder;
    }

}