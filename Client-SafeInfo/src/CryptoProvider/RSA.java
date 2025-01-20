package CryptoProvider;

import UserConfig.UserGlobals;
import Utils.Logging;

import javax.crypto.Cipher;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class RSA {
        private static PublicKey publicKey;
        private  static PrivateKey privateKey;

        public RSA(PublicKey publicKey, PrivateKey privateKey) {
            RSA.publicKey = publicKey;
            RSA.privateKey = privateKey;
        }

    public static void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            new RSA(publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            Logging.logError(e);
            System.out.println("Failed to generate RSA key");
        }
    }



    public static String getBase64PublicKey() {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }

        public static String getBase64PrivateKey() {
            return Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }


        public static String encryptMessageFromServer(String message) {

            PublicKey publicKey = UserGlobals.getPublicKey();
            int chunkSize = 245;

            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                // split the message into chunks
                byte[] messageBytes = message.getBytes();
                int messageLength = messageBytes.length;

                List<String> encryptedChunks = new ArrayList<>();

                for (int i = 0; i < messageLength; i += chunkSize) {
                    int end = Math.min(messageLength, i + chunkSize);
                    byte[] chunk = Arrays.copyOfRange(messageBytes, i, end);

                    // encrypt the chunk with RSA
                    byte[] encryptedChunk = cipher.doFinal(chunk);
                    String encodedChunk = Base64.getEncoder().encodeToString(encryptedChunk);

                    // add the encoded chunk to the list
                    encryptedChunks.add(encodedChunk);
                }

                // merge chunks into a single string, separated by ":"
                return String.join(":", encryptedChunks);

            } catch (Exception e) {
                Logging.logError(e);
                System.out.println("Failed to encrypt message for server");
                return null;
            }

        }

        public static String decryptMessageFromServer(String message) {

            PrivateKey privateKey = UserGlobals.getPrivateKey();
            String delimiter = ":";  // same delimiter used in encryption process

            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                // split the message into encrypted chunks (split by the delimiter)
                String[] encryptedChunks = message.split(delimiter);
                StringBuilder decryptedMessage = new StringBuilder();

                for (String encryptedChunk : encryptedChunks) {
                    // decode the Base64-encoded chunk
                    byte[] encryptedBytes = Base64.getDecoder().decode(encryptedChunk);

                    // decrypt the chunk with RSA
                    byte[] decryptedChunk = cipher.doFinal(encryptedBytes);

                    // append the decrypted chunk to the final message
                    decryptedMessage.append(new String(decryptedChunk));
                }

                // return the complete decrypted message
                return decryptedMessage.toString();

            } catch (Exception e) {
                Logging.logError(e);
                System.out.println("Failed to decrypt message from server");
                return null;
            }
        }
}