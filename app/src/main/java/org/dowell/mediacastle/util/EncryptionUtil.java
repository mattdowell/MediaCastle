package org.dowell.mediacastle.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class
 *
 * @author mjdowell
 */
public class EncryptionUtil {

    private static final int COPY_BYTES_BUFFER_BYTES = 1024 * 4;

    private static byte[] desKeyBytes = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};

    private static SecretKeySpec key = new SecretKeySpec(desKeyBytes, "DES");
    private static String CIPHER_INST = "DES";

    // Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
    // Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    // Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
    // Cipher cipher = Cipher.getInstance("AES/CBC128/PKCS5Padding");

    /**
     * Gets a cipher in the given mode
     */
    private static Cipher getCipher(int inMode) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_INST);
        //cipher.init(inMode, key, ivSpec);
        cipher.init(inMode, key);
        return cipher;
    }

    public static byte[] getImage(String inFileName) throws Exception {
        File file = new File(inFileName);

        FileInputStream fis = new FileInputStream(file);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
            bos.write(buf, 0, readNum);
        }

        return bos.toByteArray();
    }

    public static void encryptImage(String newFile, String oldFile) throws Exception {
        encryptImage(newFile, getImage(oldFile));
    }

    public static void encryptImage(String newFile, byte[] image) throws Exception {
        CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(new File(newFile)), getCipher(Cipher.ENCRYPT_MODE));
        for (int i = 0; i < image.length; i++) {
            cos.write(image[i]);
        }

    }

    public static InputStream decryptImageStream(String encryptedFilePath) throws Exception {
        return new CipherInputStream(new FileInputStream(new File(encryptedFilePath)), getCipher(Cipher.DECRYPT_MODE));
    }

    public static void decryptImage(String encryptedFilePath, String decryptedFilePath) throws Exception {

        CipherInputStream cis = new CipherInputStream(new FileInputStream(new File(encryptedFilePath)), getCipher(Cipher.DECRYPT_MODE));
        FileOutputStream fos = new FileOutputStream(new File(decryptedFilePath));
        copyBytes(cis, fos);
    }

    private static void copyBytes(InputStream in, OutputStream out) throws IOException {

        byte[] buf = new byte[COPY_BYTES_BUFFER_BYTES];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }
}
