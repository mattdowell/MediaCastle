package org.dowell.mediacastle.util;

public class ObfscateUtil {

    public static byte[] obfuscate(byte[] inImage) {
        return addBytesToImage(inImage);
    }

    public static byte[] deObfuscate(byte[] inImage) {
        return removeBytesFromImage(inImage);
    }


    private static byte[] addBytesToImage(byte[] inImage) {
        byte[] theReturn = new byte[inImage.length + 10];
        theReturn[0] = (byte) 0xFF;
        theReturn[1] = (byte) 0xD8;
        theReturn[2] = (byte) 0xFF;
        theReturn[3] = (byte) 0xC0;
        theReturn[4] = (byte) 0xFF;
        theReturn[5] = (byte) 0xFF;
        theReturn[6] = (byte) 0xFF;
        theReturn[7] = (byte) 0xFF;
        theReturn[8] = (byte) 0xFF;
        theReturn[9] = (byte) 0xD9;

        for (int i = 0; i < inImage.length; i++) {
            theReturn[i + 10] = inImage[i];
        }
        return theReturn;
    }

    private static byte[] removeBytesFromImage(byte[] inImage) {

        //System.out.println("Incoming image size: " + inImage.length);

        byte[] theReturn = new byte[inImage.length - 10];

        //System.out.println("New image size: " + theReturn.length);

        for (int i = 0; i < theReturn.length; i++) {
            theReturn[i] = inImage[i + 10];
        }
        //System.out.println("Done writing");

        return theReturn;
    }
}
