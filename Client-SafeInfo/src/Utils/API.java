package Utils;

public class API{
    public static String base = "http://localhost:8080/Server-SafeInfo-1.0-SNAPSHOT/api/";
    public static final String login = base + "login";
    public static final String registration = base + "registration";
    public static final String forgotPassword = base + "forgotPassword";
    public static final String verifyPhone = base + "verifyPhoneNumber";  // TwoFactorPhoneNumber servlet
    public static final String verifyOTP = base +"verifyOTP";   // TwoFactorOTP servlet
    public static final String newChatIds = base +"newChatIds";
    public static final String getPubKey = base + "getPubKey";
    public static final String sendPubKey  = base +"sendPubKey";
    public static final String getSession = base + "getSession";
    public static final String getChatId = base +"getChatId";
    public static final String changePhone = base +"changePhone";
    public static final String changePassword = base +"changePassword";
    public static final String chat = "http://localhost:8080/Server-SafeInfo-1.0-SNAPSHOT/chat/";
}