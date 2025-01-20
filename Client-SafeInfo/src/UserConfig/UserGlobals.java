package UserConfig;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;


public class UserGlobals {

    private static String username ;
    private static String password ;
    private static String hashedPassword;
    private static String phoneNumber;
    private static String session;
    private static SecretKey secretKey;
    private static IvParameterSpec secretIv;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static String currentChatId;
    private static String chatWith;
    private static boolean useSecret=true;

    public static void showUserConfig(){
        System.out.println("==== USER CONFIGURATION ====");
        System.out.println("Username: "+username);
        System.out.println("Password: "+password);
        System.out.println("HashedPassword: "+hashedPassword);
        System.out.println("PhoneNumber: "+phoneNumber);
        System.out.println("Session: "+session);
        System.out.println("SecretKey: " + (secretKey != null ? Base64.getEncoder().encodeToString(secretKey.getEncoded()) : "null"));
        System.out.println("SecretIv: " + (secretIv != null ? Base64.getEncoder().encodeToString(secretIv.getIV()) : "null"));
        System.out.println("PrivateKey: " + (privateKey != null ? Base64.getEncoder().encodeToString(privateKey.getEncoded()) : "null"));
        System.out.println("PublicKey: " + (publicKey != null ? Base64.getEncoder().encodeToString(publicKey.getEncoded()) : "null"));
        System.out.println("CurrentChatId: "+currentChatId);
        System.out.println("ChatWith: "+chatWith);
    }

    public static void resetUserConfig(){
        username = null;
        password = null;
        hashedPassword = null;
        phoneNumber = null;
        session = null;
        secretKey = null;
        secretIv = null;
        privateKey = null;
        publicKey = null;
        currentChatId = null;
        chatWith = null;
        useSecret=true;
    }

    public static PublicKey getPublicKey() {
        return publicKey;
    }

    public static void setPublicKey(PublicKey publicKey) {
        UserGlobals.publicKey = publicKey;
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        UserGlobals.privateKey = privateKey;
    }

    public static IvParameterSpec getSecretIv() {
        return secretIv;
    }

    public static void setSecretIv(IvParameterSpec secretIv) {
        UserGlobals.secretIv = secretIv;
    }

    public static SecretKey getSecretKey() {
        return secretKey;
    }

    public static void setSecretKey(SecretKey secretKey) {
        UserGlobals.secretKey = secretKey;
    }

    public static boolean getUseSecret(){
        return useSecret;
    }

    public static void setUseSecret(boolean useSecret){
        UserGlobals.useSecret=useSecret;
    }

    public static String getUsername(){
        return username;
    }

    public static void setUsername(String newUsername){
        username = newUsername;
    }

    public static String getPassword(){
        return password;
    }

    public static void setPassword(String newPassword){
        password = newPassword;
    }

    public static String getHashedPassword(){
        return hashedPassword;
    }

    public static void setHashedPassword(String newHashedPassword){
        hashedPassword = newHashedPassword;
    }

    public static String getPhoneNumber(){
        return phoneNumber;
    }

    public static void setPhoneNumber(String newPhoneNumber){
        phoneNumber = newPhoneNumber;
    }

    public static String geSession(){
        return session;
    }

    public static void setSession(String newSession){
        session = newSession;
    }

    public static String getCurrentChatId(){
        return currentChatId;
    }

    public static void setCurrentChatId(String newCurrentChatId){
        currentChatId = newCurrentChatId;
    }

    public static String getChatWith(){
        return chatWith;
    }

    public static void setChatWith(String newChatWith){
        chatWith = newChatWith;
    }

}