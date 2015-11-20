package org.dowell.mediacastle.user;

/**
 * This class instances represents a logged in session.
 *
 * @author Matt
 */
public class Session {

    private static String passcode = null;

    public static String getPasscode() {
        return passcode;
    }

    public static void setPasscode(String passcode) {
        Session.passcode = passcode;
    }
}
