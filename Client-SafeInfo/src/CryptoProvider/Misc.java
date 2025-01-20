package CryptoProvider;

import UserConfig.UserGlobals;
import Utils.Logging;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Misc{
    // set the private key in User.Globals
    public static void setPrivateKey(String decryptedPrivateKey) {
        try {
            // decode the decrypted base64-encoded private key
            byte[] keyBytes = Base64.getDecoder().decode(decryptedPrivateKey);

            // convert the decoded key bytes into a PrivateKey object
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // set the private key in User.Globals
            UserGlobals.setPrivateKey(privateKey);
        } catch (Exception e) {
            Logging.logError(e);
            System.out.println("Failed to set the private key.");
        }
    }

    // encrypt the master key with the data key
    public static String encryptMasterKey(String masterKey) {
        return decryptMasterKey(masterKey);
    }

    // decrypt the master key
    public static String decryptMasterKey(String encMasterKey) {
        String dataKey = deriveDataKey(UserGlobals.getHashedPassword());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < encMasterKey.length(); i++) {
            assert dataKey != null;
            sb.append((char) (encMasterKey.charAt(i) ^ dataKey.charAt(i % dataKey.length())));
        }
        return sb.toString();
    }

    public static SecretKey stringToSecretKey(String encodedKey, String algorithm) {
        // decode the Base64-encoded string to get the raw key bytes
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

        // rebuild the SecretKey object using the decoded key bytes
        return new SecretKeySpec(decodedKey, algorithm);
    }

    // derive a data key from the user's password
    public static String deriveDataKey(String passwordHash) {
        try {
            byte[] salt = {
                    (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d,
                    (byte) 0x5e, (byte) 0x6f, (byte) 0x74, (byte) 0x2a,
                    (byte) 0x19, (byte) 0x02, (byte) 0x1f, (byte) 0x21,
                    (byte) 0x33, (byte) 0x4e, (byte) 0x50, (byte) 0x6d
            };
            byte[] derivedDataKeyBytes = deriveKeyFromPassword(passwordHash, salt).getEncoded();
            return Base64.getEncoder().encodeToString(derivedDataKeyBytes);
        } catch (Exception e) {
            Logging.logError(e);
            System.out.println("Failed to derive data key.");
            return null;
        }
    }

    public static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        // define iteration count and key length (256 bits for AES)
        int iterationCount = 65536;  // large number (>= 65k)
        int keyLength = 256;         // key length for AES-256

        // generate the SecretKey using PBKDF2 with HMAC SHA-256
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(spec).getEncoded();

        // create the SecretKey from the derived key bytes
        return new SecretKeySpec(keyBytes, "AES");
    }
}