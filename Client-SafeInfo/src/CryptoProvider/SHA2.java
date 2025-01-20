package CryptoProvider;

import Utils.Logging;

import java.security.MessageDigest;

public class SHA2 {
    public static String generateHash(String message) {

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(message.getBytes());

            StringBuilder hexString = new StringBuilder();
            for(byte b: hash){
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }catch (Exception e) {
            Logging.logError(e);
            System.out.println("Failed to generate hash for password!");
            return "error";
        }
    }
}
