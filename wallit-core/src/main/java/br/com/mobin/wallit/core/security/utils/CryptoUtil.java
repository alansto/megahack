package br.com.mobin.wallit.core.security.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String CHIPHER = "AES/ECB/PKCS5Padding";
    private static final Integer KEY_SIZE = 256;

    public static String generateKey() throws Exception {
        KeyGenerator keygenerator = KeyGenerator.getInstance( ALGORITHM );
        keygenerator.init( KEY_SIZE );
        SecretKey encryptKey = keygenerator.generateKey();

        return Base64.getEncoder().encodeToString(encryptKey.getEncoded());
    }

    public static String encryptWithKey(final String textToEncrypt, final String encodedKey ) throws Exception {

        Cipher cipherInstance = Cipher.getInstance( CHIPHER );
        byte[] decodedKey = Base64.getDecoder().decode( encodedKey );

        cipherInstance.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(decodedKey, ALGORITHM));

        return Base64.getEncoder().encodeToString(cipherInstance.doFinal( textToEncrypt.getBytes() ));
    }

    public static String decryptWithKey(final String encryptText, final String encodedKey ) throws Exception {

        Cipher cipherInstance = Cipher.getInstance( CHIPHER );
        byte[] decodedKey = Base64.getDecoder().decode( encodedKey );

        cipherInstance.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decodedKey, ALGORITHM));
        byte[] decodedText = Base64.getDecoder().decode( encryptText );

        return new String( cipherInstance.doFinal( decodedText ) );
    }

    public static String encrypt(final String textToEncrypt) throws Exception {

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(textToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(md.digest());
    }
}
