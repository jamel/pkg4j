package org.jamel.pkg4j.gradle.utils;

/**
 * @author Sergey Polovko
 */
public final class PassphraseProvider {

    private static String passphrase;


    private PassphraseProvider() {
    }

    public static String provide() {
        if (passphrase == null) {
            return new String(System.console().readPassword("\n%s: ",
                    "You need enter a passphrase to unlock your secret key. Or press ENTER for skip signing"));
        }

        return passphrase;
    }

    public static void remember(String passphrase) {
        PassphraseProvider.passphrase = passphrase;
    }
}
