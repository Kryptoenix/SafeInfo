package GUI;

import Service.Controller;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;

import Design.*;
import Utils.*;
import UserConfig.*;


public class User extends JPanel {

    private JButton sendMessageButton;
    private JButton settingsButton;
    private JButton logoutButton;
    private JToggleButton themeToggle;
    public static GradientPanel centerPanel;


    // store max 7 chat ids on the User panel
    public static final String[] panelChatIds = new String[7];
    static final Dictionary<String, String> chatButton = new Hashtable<>();

    private void initComponents() {
        UserGlobals.showUserConfig();

        // Send message button
        sendMessageButton = new CustomJButton("Send Message", "rect",150,45);
        sendMessageButton.setMaximumSize(new Dimension(250, 45));
        sendMessageButton.addActionListener(e -> Main.switchPanel("NewChat"));

        settingsButton = CustomJButton.createButton("Settings");
        settingsButton.addActionListener(e -> Main.switchPanel("Settings"));

        // Logout button (Top-right corner)
        logoutButton = CustomJButton.createButton("Logout");
        // Fixed syntax issue with logoutButton
        logoutButton.addActionListener(e -> {
            Main.switchPanel("Home");
            Controller.processRequest(
                    Controller.ServiceID.DestroyCachedSession,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    }
            );
            Sound.logout();
        });

        themeToggle = new CustomToggleButton(ASSETS.theme, 50, 50);
        themeToggle.setMaximumSize(new Dimension(50, 50));
        themeToggle.addActionListener(e -> {
            CustomColor.isDarkTheme = themeToggle.isSelected();
            revalidate();
            repaint();
        });


        centerPanel = new GradientPanel();
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(100, 100));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        centerPanel.setBackground(CustomColor.getBackgroundColor());
        centerPanel.setBorder(new LineBorder(Color.GRAY, 2));


        Sound.login();

        // Error label (hidden by default)
        JLabel errorLabel = new CustomLabel("Error");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
    }

    public User() {
        setSize(800, 600);
        setLayout(new BorderLayout());

        initComponents();
        setUpLayout();

        Controller.processRequest(
                Controller.ServiceID.ReadUserKeys,
                (error) ->{
                    if (!error.isEmpty()){
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        );

        Controller.processRequest(
                Controller.ServiceID.LoadChats,
                (error) ->{
                    if (!error.isEmpty()){
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        );

        Controller.processRequest(
                Controller.ServiceID.GetNewChatIds,
                (error) ->{
                    if (!error.isEmpty()){
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        );
    }

    private static void addUserChatButton(GradientPanel centerPanel, String chatId) {
        String otherUser = UserGlobals.getChatWith();  // Ensure this gets the correct user

        if (otherUser == null) {
            return; // Handle case where there is no other user
        }

        // associate each chat id with the other user (dictionary)
        chatButton.put(otherUser, chatId);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = centerPanel.getComponentCount();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton userChatButton = new CustomJButton(otherUser, "rect", 500);
        userChatButton.addActionListener(e -> {
            System.out.println("Opening chat for: " + chatId);
            // Ensure this method is called only if otherUser is not null
            String otherUserFromChatId = userChatButton.getText();
            UserGlobals.setChatWith(otherUserFromChatId);
            UserGlobals.setCurrentChatId(chatButton.get(otherUser));
            Controller.processRequest(  // Fetch the public key
                    Controller.ServiceID.GetPublicKeyServer,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    otherUserFromChatId
            );
            Main.switchPanel("Chat");
        });

        centerPanel.add(userChatButton, gbc);
        gbc.gridy++;
        centerPanel.revalidate();
        centerPanel.repaint();
    }


    private void setUpLayout() {
        GradientPanel topPanel = createTopPanel();
        GradientPanel centerPanel = createCenterPanel();
        GradientPanel bottomPanel = createBottomPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private GradientPanel createTopPanel() {
        GradientPanel topPanel = new GradientPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS)); // set horizontal alignment
        topPanel.setBackground(CustomColor.getBackgroundColor());

        // add the left (settings) button
        topPanel.add(settingsButton);

        // add horizontal glue to push the next components towards the middle
        topPanel.add(Box.createHorizontalGlue());

        // add the center (sendMessage) button
        topPanel.add(Box.createRigidArea(new Dimension(70, 0)));
        topPanel.add(sendMessageButton);

        // add horizontal glue to push the next components (right-aligned) towards the end
        topPanel.add(Box.createHorizontalGlue());

        // add the right (themeToggle and logout) buttons
        topPanel.add(themeToggle);
        topPanel.add(Box.createRigidArea(new Dimension(30, 0)));

        topPanel.add(logoutButton);

        return topPanel;
    }

    private GradientPanel createCenterPanel() {
        centerPanel = new GradientPanel();
        centerPanel.setBackground(CustomColor.getBackgroundColor());

        return centerPanel;
    }

    private GradientPanel createBottomPanel() {
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());
        return bottomPanel;
    }


    public static void addUserChat(String chatId){
        addUserChatButton(centerPanel, chatId);
    }


}