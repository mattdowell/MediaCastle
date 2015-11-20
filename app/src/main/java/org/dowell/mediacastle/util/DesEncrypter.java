package org.dowell.mediacastle.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author mjdowell
 */
public class DesEncrypter {

    // Buffer size
    private static final int COPY_BYTES_BUFFER_BYTES = 1024 * 4;
    //private static final String ALGORITHYM = "DES/CBC/PKCS5Padding";
    // private static final String ALGORITHYM = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHYM = "DES/ECB/PKCS5Padding";
    private static final DesEncrypter INSTANCE = new DesEncrypter();
    // Key bytes
    private static byte[] keyBytes = new byte[]{0x00, 0x01, 0x03, 0x02, 0x04, 0x05, 0x06, 0x07};
    //private static final String ALGORITHYM = "DES";
    //private static final String ALGORITHYM = "DES/ECB";
    //private static final String ALGORITHYM = "RSA";
    // Encryption classes
    private static final SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");

    private DesEncrypter() {
    }

    public static DesEncrypter getInstance() {
        return INSTANCE;
    }

    /**
     * This method returns all available services types.
     */
    public static String[] getServiceTypes() {
        java.util.HashSet<String> result = new HashSet<String>();
        // All all providers.
        java.security.Provider[] providers = java.security.Security.getProviders();
        for (int i = 0; i < providers.length; ++i) {
            // Get services provided by each provider.
            java.util.Set<Object> keys = providers[i].keySet();
            for (Iterator<Object> it = keys.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                key = key.split(" ")[0];
                if (key.startsWith("Alg.Alias.")) {
                    // Strip the alias.
                    key = key.substring(10);
                }
                int ix = key.indexOf('.');
                result.add(key.substring(0, ix));
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * This method returns the available implementations for a service type.
     */
    public static String[] getCryptoImpls(String serviceType) {
        java.util.HashSet<String> result = new java.util.HashSet<String>();
        // All all providers.
        java.security.Provider[] providers = java.security.Security.getProviders();
        for (int i = 0; i < providers.length; ++i) {
            // Get services provided by each provider.
            java.util.Set<Object> keys = providers[i].keySet();
            for (Object o : keys) {
                String key = ((String) o).split(" ")[0];
                if (key.startsWith(serviceType + ".")) {
                    result.add(key.substring(serviceType.length() + 1));
                } else if (key.startsWith("Alg.Alias." + serviceType + ".")) {
                    // This is an alias
                    result.add(key.substring(serviceType.length() + 11));
                }
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public void encrypt(InputStream in, OutputStream inOut) throws Exception {
        Cipher ecipher = Cipher.getInstance(ALGORITHYM);
        //ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        // Bytes written to out will be encrypted
        CipherOutputStream out = new CipherOutputStream(inOut, ecipher);

        // Buffer used to transport the bytes from one stream to another
        byte[] buf = new byte[COPY_BYTES_BUFFER_BYTES];

        // Read in the cleartext bytes and write to out to encrypt
        int numRead = 0;
        while ((numRead = in.read(buf)) >= 0) {
            out.write(buf, 0, numRead);
        }
        out.close();
    }

    public ByteArrayOutputStream decrypt(InputStream in) throws Exception {
        Cipher dcipher = Cipher.getInstance(ALGORITHYM);
        //dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        dcipher.init(Cipher.DECRYPT_MODE, key);
        // Bytes read from in will be decrypted
        CipherInputStream cin = new CipherInputStream(in, dcipher);

        // Buffer used to transport the bytes from one stream to another
        byte[] buf = new byte[COPY_BYTES_BUFFER_BYTES];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Read in the decrypted bytes and write the cleartext to out
        int numRead = 0;
        while ((numRead = cin.read(buf)) >= 0) {
            out.write(buf, 0, numRead);
        }
        return out;
    }
}
