package Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static Service.Chat.*;
import static Service.ForgotPassword.handleForgotPasswordRequest;
import static Service.Login.handleLoginRequest;
import static Service.NewChat.handleNewChat;
import static Service.NewPassword.handleNewPasswordRequest;
import static Service.NewPhoneNumber.handleNewPhoneRequest;
import static Service.Registration.handleRegistrationRequest;
import static Service.TwoFactorAuth.handleTwoFactorOTP;
import static Service.TwoFactorAuth.handleTwoFactorPhoneRequest;
import static Service.User.*;

public class Controller {

    public enum ServiceID {
        LoginRequest,
        RegisterRequest,
        ForgotPasswordRequest,
        TwoFactorPhoneRequest,
        TwoFactorOTP,
        ReadUserKeys,
        GetNewMessages,
        GetPublicKeyServer,
        LoadChats,
        LoadChatHistory,
        NewPhoneRequest,
        NewPasswordRequest,
        GetSession,
        DestroyCachedSession,
        NewChat,
        SendMessage,
        GetNewChatIds
    }


    private static final Map<ServiceID, Boolean> serviceControlFlags = new ConcurrentHashMap<>();

    public static void setServiceControlFlag(ServiceID serviceID, boolean value) {
        serviceControlFlags.put(serviceID, value);
    }

    public static boolean getServiceControlFlag(ServiceID serviceID) {
        return serviceControlFlags.getOrDefault(serviceID, false);
    }

    static {
        for (ServiceID serviceID : ServiceID.values()) {
            serviceControlFlags.put(serviceID, false);
        }
    }


    public static void processRequest(ServiceID serviceID, Consumer<String> onFailure, String... args) {
        CompletableFuture.runAsync(() -> {
            try {
                switch (serviceID) {
                    case LoginRequest -> handleLoginRequest(args[0], args[1], onFailure);
                    case RegisterRequest -> handleRegistrationRequest(args[0], args[1], args[2], args[3], onFailure);
                    case ForgotPasswordRequest -> handleForgotPasswordRequest(args[0], onFailure);
                    case TwoFactorPhoneRequest -> handleTwoFactorPhoneRequest(onFailure);
                    case TwoFactorOTP -> handleTwoFactorOTP(args[0], onFailure);
                    case ReadUserKeys -> handleReadUserKeys(onFailure);
                    case NewPhoneRequest -> handleNewPhoneRequest(args[0], args[1], onFailure);
                    case NewPasswordRequest -> handleNewPasswordRequest(args[0], onFailure);
                    case DestroyCachedSession -> destroyCachedSession(onFailure);
                    case GetSession -> getSession(onFailure);
                    case NewChat -> handleNewChat(args[0], onFailure);
                    case LoadChats -> handleLoadChats(onFailure);
                    case GetNewChatIds -> handleNewChatIds(onFailure);
                    case GetPublicKeyServer -> handlePublicKeyServer(args[0],onFailure);
                    case SendMessage  -> handleMessage(args[0], Instant.parse(args[1]),onFailure);
                    case GetNewMessages -> handleNewMessages(onFailure);
                    case LoadChatHistory -> handleChatHistory(onFailure);
                    default -> onFailure.accept("Invalid Service ID");
                }
            } catch (Exception e) {
                onFailure.accept("Error processing request: " + e.getMessage());
            }
        });
    }
}
