package CryptoProvider;

import UserConfig.UserGlobals;
import Utils.Logging;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AES {

    public static SecretKey generateKey(int n) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(n);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            Logging.logError(e);
            System.out.println("Failed to generate AES key");
            return null;
        }
    }

    public static Map<String, Object> generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("ivBytes", iv);
        resultMap.put("ivSpec", ivSpec);

        return resultMap;
    }


    public static String encryptMessageFromHistory(String message) { // (ciphertext)

        SecretKey key = UserGlobals.getSecretKey();
        IvParameterSpec iv = UserGlobals.getSecretIv();

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key,iv);
            byte[] encMessage = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encMessage);

        }
        catch (Exception e) {
            Logging.logError(e);
            System.out.println("Failed to encrypt message to history");
            return null;
        }
    }

    public static String decryptMessageFromHistory(String encMessage) { // (plaintext)
        SecretKey key = UserGlobals.getSecretKey();
        IvParameterSpec iv = UserGlobals.getSecretIv();

        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key,iv);
            byte[] message = cipher.doFinal(Base64.getDecoder().decode(encMessage));
            return new String(message);
        }
        catch (Exception e) {
            Logging.logError(e);
            System.out.println("Failed to decrypt message from history");
            return null;
        }
    }


}