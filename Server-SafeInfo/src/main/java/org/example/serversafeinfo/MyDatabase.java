package org.example.serversafeinfo;

import com.twilio.*;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.enterprise.context.ApplicationScoped;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;
import java.util.*;

import static org.example.serversafeinfo.TwoFactorPhoneNumberServlet.ACCOUNT_SID;
import static org.example.serversafeinfo.TwoFactorPhoneNumberServlet.AUTH_TOKEN;

public class MyDatabase {

    String jdbcUrl = "jdbc:mysql://mysql:3306/mysql";
    String dbUser = "root";
    String dbPassword = "rootroot";


    ///  ------------------------------  AUTHENTICATION  -------------------------------------

    public String register(String username, String passwordHash, String phoneNumber) {
        String pubKey = "0";

        String registrationQuery = "INSERT INTO users (username, passwordHash, phoneNumber,publicKey) VALUES (?, ?, ?,?)";
        String verifyQuery = "SELECT username FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {

            PreparedStatement preparedStatement = connection.prepareStatement(verifyQuery);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            // check if user already in database
            if (resultSet.next()) {
                return "User already exists";
            }
            else {
                preparedStatement = connection.prepareStatement(registrationQuery);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, passwordHash);
                preparedStatement.setString(3, phoneNumber);
                preparedStatement.setString(4, pubKey);


                int rowAdded = preparedStatement.executeUpdate();

                if (rowAdded > 0) {
                    return "success";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
        return "something";
    }


    public String login(String username, String passwordHash) {
        String loginQuery = "SELECT passwordHash,phoneNumber FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(loginQuery);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("passwordHash");
                // Compare the provided hash
                if (storedHash.equals(passwordHash)) {
                    return resultSet.getString("phoneNumber"); // success
                } else {
                    return "Wrong username or password"; // wrong username or password
                }
            } else {
                return "User not found";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    public String forgotPassword(String username) {
        String forgotPasswordQuery = "SELECT phoneNumber FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(forgotPasswordQuery);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String phoneNumber = resultSet.getString("phoneNumber");

                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                Verification verification = Verification.creator("VA65ac97e306fdc87a371c29c36709b9b7", phoneNumber, "sms").create();
                System.out.println("Verification SID: " + verification.getSid());
                System.out.println("OTP sent to "+phoneNumber);

                return "success";
            } else {
                return "User not found";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    public String twoFactorAuth(String username) {
        String twoFactorAuthQuery = "SELECT phoneNumber FROM users WHERE username = ?";

        try(Connection connection = DriverManager.getConnection(jdbcUrl,dbUser,dbPassword)){
            PreparedStatement preparedStatement = connection.prepareStatement(twoFactorAuthQuery);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("phoneNumber");
            }
            else{
                return "Phone not found";
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }


    public String twoFactorAuthVerifyOTP(String username, String otp) {
        String twoFactorAuthQuery = "SELECT phoneNumber FROM users WHERE username = ?";

        String phoneNumber;
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(twoFactorAuthQuery);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                phoneNumber = resultSet.getString("phoneNumber");
            } else {
                return "Phone not found";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }

        VerificationCheck verificationCheck = VerificationCheck.creator("VA65ac97e306fdc87a371c29c36709b9b7").setTo(phoneNumber).setCode(otp).create();
        if (verificationCheck.getStatus().equals("approved")) {
            return "success"; // OTP verified successfully
        } else {
            return "OTP verification failed. Please try again.";
        }
    }

    public String validateCreds(String username, String passwordHash) {
        String loginQuery = "SELECT passwordHash FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(loginQuery);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("passwordHash");
                // Compare the provided hash
                if (storedHash.equals(passwordHash)) {
                    return "success";
                } else {
                    return "Wrong username or password";
                }
            } else {
                return "User not found";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    public String setPubKey(String username,String pubKey) {

        String pubKeyQuery = "UPDATE users SET publicKey = ? WHERE username = ?";

        try(Connection connection = DriverManager.getConnection(jdbcUrl,dbUser,dbPassword)){
            PreparedStatement preparedStatement = connection.prepareStatement(pubKeyQuery);
            preparedStatement.setString(1,pubKey);
            preparedStatement.setString(2,username);

            int rowUpdated = preparedStatement.executeUpdate();

            if(rowUpdated>0){
                return "success";
            }
            else {
                return "Failed to update pubKey field";
            }
        }catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }

    }


    ///  ----------------------------------- USER MANAGEMENT ---------------------------------

    public String getPubKey(String username) {

        String pubKeyQuery = "SELECT publicKey FROM users WHERE username = ?";

        try(Connection connection = DriverManager.getConnection(jdbcUrl,dbUser,dbPassword)){
            PreparedStatement preparedStatement = connection.prepareStatement(pubKeyQuery);
            preparedStatement.setString(1,username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return resultSet.getString("publicKey");
            }
            else {
                return "Failed to get pubKey from database";
            }
        }catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }

    }

    public String getChatId(String user, String otherUser) {

        String getChatIdQuery = "SELECT chat_id FROM chat WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
        String setChatIdQuery = "INSERT INTO chat (chat_id,user1,ua1,prevReqTs1,user2,ua2,prevReqTs2) VALUES (?,?,?,?,?,?,?)";

        try(Connection connection = DriverManager.getConnection(jdbcUrl,dbUser,dbPassword)){

            // check if chat-id already exists in database
            PreparedStatement preparedStatement = connection.prepareStatement(getChatIdQuery);
            preparedStatement.setString(1,user);
            preparedStatement.setString(2,otherUser);
            preparedStatement.setString(3,otherUser);
            preparedStatement.setString(4,user);

            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("chat_id");
            }
            else {
                // if no chat id was found between these 2 users, create one

                // generate a new chat id
                String chatid = UUID.randomUUID().toString();

                preparedStatement = connection.prepareStatement(setChatIdQuery);
                preparedStatement.setString(1, chatid);
                preparedStatement.setString(2, user);
                preparedStatement.setString(3, "1");
                preparedStatement.setString(4, "1970-01-01T00:00:00Z"); // very old timestamp
                preparedStatement.setString(5, otherUser);
                preparedStatement.setString(6, "0");
                preparedStatement.setString(7, "1970-01-01T00:00:00Z"); // very old timestamp


                int rowAdded = preparedStatement.executeUpdate();

                if (rowAdded > 0) {
                    return chatid;
                } else {
                    return "Failed to get chat id from database";
                }

            }
        }catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }

    }


    public String getNewChatIdsHandler(String username) {
        String getNewChatIdQuery = "SELECT * FROM chat WHERE user1 = ? OR user2 = ?";
        String setUserAware1Query = "UPDATE chat SET ua1 = ? WHERE chat_id = ?";
        String setUserAware2Query = "UPDATE chat SET ua2 = ? WHERE chat_id = ?";
        JSONArray newChat = new JSONArray(); // an array that will contain any new (chat id, other user) pair

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            // get chats for the user
            PreparedStatement preparedStatement = connection.prepareStatement(getNewChatIdQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            // check if any results were returned
            if (!resultSet.isBeforeFirst()) {
                return "Failed to find new chat ids in database";
            }

            // iterate over all the chat records
            while (resultSet.next()) {
                String user1 = resultSet.getString("user1");
                String user2 = resultSet.getString("user2");
                String chatId = resultSet.getString("chat_id");
                String ua1 = resultSet.getString("ua1");
                String ua2 = resultSet.getString("ua2");

                // check if input username has already "flag aware" set
                if ((user1.equals(username) && ua1.equals("1")) || (user2.equals(username) && ua2.equals("1"))) {
                    // it means that input user is already aware of this chat id
                    continue; // skip
                } else {
                    // prepare a JSON object to store the chat ID and the other user
                    JSONObject json = new JSONObject();

                    PreparedStatement updateStatement; // for updating user awareness flags
                    if (user1.equals(username)) {
                        // if the user is user1, update ua1 flag
                        updateStatement = connection.prepareStatement(setUserAware1Query);
                        updateStatement.setString(1, "1");
                        updateStatement.setString(2, chatId);

                        // add the other user (user2) to the JSON object
                        json.put("otherUser", user2);
                        json.put("chat_id", chatId);
                    } else {
                        // else, the user is user2, so update ua2 flag
                        updateStatement = connection.prepareStatement(setUserAware2Query);
                        updateStatement.setString(1, "1");
                        updateStatement.setString(2, chatId);

                        // add the other user (user1) to the JSON object
                        json.put("otherUser", user1);
                        json.put("chat_id", chatId);
                    }

                    //  update for user awareness
                    int rowUpdated = updateStatement.executeUpdate();
                    if (rowUpdated <= 0) {
                        return "Failed to set 'user aware' flag for chat: " + chatId;
                    }

                    // add the chat info (other user and chat id) to the array
                    newChat.put(json);

                    // close the update statement after use to free resources
                    updateStatement.close();
                }

                // return the chat information as a JSON string
                return newChat.toString();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }

        return "";
    }

    public String changePhone(String username, String newPhone) {
        String changePhoneQuery = "UPDATE users SET phoneNumber = ? WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(changePhoneQuery);
            preparedStatement.setString(1, newPhone);
            preparedStatement.setString(2, username);

            // Execute the update and get the number of affected rows
            int rowsUpdated = preparedStatement.executeUpdate();

            // Check if the update was successful
            if (rowsUpdated > 0) {
                return "success";
            } else {
                return "Failed to change phone number; user may not exist.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }


    public String changePassword(String username, String password) {
        String changePasswordQuery = "UPDATE users SET passwordHash = ? WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(changePasswordQuery);
            preparedStatement.setString(1, password);
            preparedStatement.setString(2, username);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                return "success";
            } else {
                return "Failed to change password; user may not exist.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    public String getPasswordHash(String username){
        String getPasswordHashQuery = "SELECT passwordHash FROM users WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(getPasswordHashQuery);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("passwordHash");
            } else {
                return "Failed to get password hash from database";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    public String getOtherUser(String username, String chatId){
        String getOtherUserQuery = "SELECT user1,user2 FROM chat WHERE chat_id = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(getOtherUserQuery);
            preparedStatement.setString(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String user1=  resultSet.getString("user1");
                String user2=  resultSet.getString("user2");

                if(username.equals(user1)){
                    return user2;
                }
                else if(username.equals(user2)){
                    return user1;
                }
                else{
                    return  "Failed to retrieve the other user";
                }
            } else {
                return "Failed to retrieve the other user";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }


    public String getPrevReqTs(String username, String chatId){
        String getPrevReqTsQuery = "SELECT * FROM chat WHERE chat_id = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(getPrevReqTsQuery);
            preparedStatement.setString(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String user1=  resultSet.getString("user1");
                String user2=  resultSet.getString("user2");

                if(username.equals(user1)){
                   return resultSet.getString("prevReqTs1");
                }
                else if(username.equals(user2)){
                    return resultSet.getString("prevReqTs2");
                }
                else{
                    return  "Failed to retrieve the last request timestamp";
                }
            } else {
                return "Failed to find chat id";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }

    public String updateUserTs(String username, String chatId, String newPrevReqTs) {
        String getUserForPrevReqTsQuery = "SELECT * FROM chat WHERE chat_id = ?";
        String updatePrevReqTs1Query = "UPDATE chat SET prevReqTs1 = ? WHERE chat_id = ?";
        String updatePrevReqTs2Query = "UPDATE chat SET prevReqTs2 = ? WHERE chat_id = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(getUserForPrevReqTsQuery);
            preparedStatement.setString(1, chatId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String user1=  resultSet.getString("user1");
                String user2=  resultSet.getString("user2");

                if(username.equals(user1)){
                    // set prev request timestamp for user 1
                    preparedStatement = connection.prepareStatement(updatePrevReqTs1Query);
                    preparedStatement.setString(1, newPrevReqTs);
                    preparedStatement.setString(2, chatId);

                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        return "success";
                    }else{
                        return "Failed to update the previous request timestamp";
                    }
                }
                else if(username.equals(user2)){
                    // else, set prev request timestamp for user 2
                    preparedStatement = connection.prepareStatement(updatePrevReqTs2Query);
                    preparedStatement.setString(1, newPrevReqTs);
                    preparedStatement.setString(2, chatId);

                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        return "success";
                    }else{
                        return "Failed to update the previous request timestamp";
                    }
                }else{
                    return "error";
                }
            }else{
                return "Failed to find chat id";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error";
        }
    }
}

