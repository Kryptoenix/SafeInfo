package GUI;

import Service.Controller;
import UserConfig.UserGlobals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

import java.time.Instant;

import java.util.*;


import Design.*;
import Utils.*;

import static Service.Chat.insToTimeBubble;


public class Chat extends JPanel {
    public static GradientPanel centerPanel; // holds chat bubbles
    private JTextField messageField;
    public static JScrollPane scrollPane;
    private JButton goBackButton;
    private JButton logoutButton;
    private JButton addImageField;
    private JButton sendMessageButton;
    private JButton sendEmojiButton;

    public static GradientPanel chatBubbleWrapper;
    private boolean isEmojiPanelOpen = false;
    private CustomLabel otherUserLabel;

    private void initComponents() {


        // Go Back button
        goBackButton = CustomJButton.createButton("Go Back");
        goBackButton.addActionListener(e -> {
            Controller.setServiceControlFlag(Controller.ServiceID.GetNewMessages, false);
            centerPanel.removeAll();
            centerPanel.revalidate();
            Main.switchPanel("User");
        });

        // Log out button
        logoutButton = CustomJButton.createButton("Logout");
        logoutButton.addActionListener(e -> {
            Controller.setServiceControlFlag(Controller.ServiceID.GetNewMessages, false);
            Controller.processRequest(
                    Controller.ServiceID.DestroyCachedSession,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    }
            );
            centerPanel.removeAll();
            centerPanel.revalidate();
            Main.switchPanel("Home");
            Sound.logout();
        });

        // Message text field
        messageField = new CustomTextField("Enter your message", 300,50);
        // Add Image button
        addImageField = CustomJButton.createButton("Add Image");
        addImageField.addActionListener(e -> sendImage());

        // Send Message button
        sendMessageButton = CustomJButton.createButton("Send");
        sendMessageButton.addActionListener(e -> sendText(messageField.getText().trim()));

        // bind the enter key to trigger the "send message" action
        InputMap inputMap = sendMessageButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = sendMessageButton.getActionMap();

        // map the enter key to the "sendMessageAction" command
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"sendMessageAction");

        // define action for "sendMessageAction"
        actionMap.put("sendMessageAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendText(messageField.getText().trim());
            }
        });

        sendEmojiButton = CustomJButton.createButton("Send Emoji");
        sendEmojiButton.addActionListener(e -> handleEmojis());

        chatBubbleWrapper = new GradientPanel();

        otherUserLabel = new CustomLabel(UserGlobals.getChatWith());

        UserGlobals.showUserConfig();
    }

    public Chat() {
        setSize(800, 600);
        setLayout(new BorderLayout());

        initComponents();
        setUpLayout();

        Controller.processRequest(
                Controller.ServiceID.LoadChatHistory,
                (error) -> {
                    if (!error.isEmpty()) {
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        );

        Controller.processRequest(
                Controller.ServiceID.GetNewMessages,
                (error) -> {
                    if (!error.isEmpty()) {
                        ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                        errorDialog.setVisible(true);
                    }
                }
        );

    }

    private void setUpLayout(){
        GradientPanel topPanel = createTopPanel();
        GradientPanel centerPanel = createCenterPanel();
        GradientPanel bottomPanel = createBottomPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private GradientPanel createTopPanel(){
        // top panel
        GradientPanel topPanel = new GradientPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(CustomColor.getBackgroundColor());
        topPanel.add(goBackButton, BorderLayout.WEST);

        topPanel.add(otherUserLabel,BorderLayout.CENTER);

        topPanel.add(logoutButton, BorderLayout.EAST);

        return topPanel;
    }

    private GradientPanel createCenterPanel() {
        centerPanel = new GradientPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS)); // put bubbles vertically
        centerPanel.setBackground(CustomColor.getBackgroundColor());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // padding

        //  JScrollPane for scrolling
        scrollPane = new JScrollPane(centerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        GradientPanel wrapperPanel = new GradientPanel();
        wrapperPanel.setLayout(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        return wrapperPanel;
    }


    private GradientPanel createBottomPanel() {
        GradientPanel bottomPanel = new GradientPanel();
        bottomPanel.setBackground(CustomColor.getBackgroundColor());
        bottomPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);

        // add message text field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1; // allow the text field to grow
        gbc.fill = GridBagConstraints.HORIZONTAL; // make it fill the horizontal space
        bottomPanel.add(messageField, gbc);

        // add Send Message button
        gbc.gridx = 1;
        gbc.weightx = 0; // button does not need to grow
        gbc.fill = GridBagConstraints.NONE; // button does not fill
        bottomPanel.add(sendMessageButton, gbc);

        // add Add Image button
        gbc.gridx = 2;
        bottomPanel.add(addImageField, gbc);

        // Add Send Emoji button
        gbc.gridx = 3;
        bottomPanel.add(sendEmojiButton, gbc);

        return bottomPanel;
    }



    public void sendText(String message) {
        if (!message.isEmpty()) {
            boolean isSender = true; // sender's perspective

            // generate a timestamp for the message
            Instant timestamp = Instant.now();
            String time = insToTimeBubble(timestamp); // local date time based on system config


            // create and wrap a new ChatBubble
            ChatBubbleText chatBubbleText = new ChatBubbleText(message, time, isSender);
            chatBubbleWrapper.setLayout(new BoxLayout(chatBubbleWrapper, BoxLayout.Y_AXIS));
            chatBubbleWrapper.add(Box.createRigidArea(new Dimension(0, 10)));

            chatBubbleWrapper.setBackground(CustomColor.getBackgroundColor());
            chatBubbleWrapper.add(chatBubbleText);

            // add new chat bubble at the end (so it displays at the bottom)
            centerPanel.add(chatBubbleWrapper);
            centerPanel.revalidate();
            centerPanel.repaint();

            // send Message to server
            Controller.processRequest(
                    Controller.ServiceID.SendMessage,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    message,
                    timestamp.toString()
            );

            // add new chat bubble at the end (so it displays at the bottom)
            centerPanel.add(chatBubbleWrapper);
            centerPanel.revalidate();
            centerPanel.repaint();

            // scroll to the bottom after adding the new message
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });

            // clear the message field after sending
            messageField.setText("");

        }
    }

    public void sendImage(){
        // send message
        boolean isSender = true;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // open a dialog box
        int result = fileChooser.showOpenDialog(null); // null centers the dialog

        // generate a timestamp for the message
        Instant timestamp = Instant.now();
        String time = insToTimeBubble(timestamp); // local date time based on system config

        // check if the user selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File chosenFile = fileChooser.getSelectedFile();

            // store bytes from image file
            byte[] imageBytes = new byte[0];

            try (InputStream inputStream = new FileInputStream(chosenFile)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                imageBytes = outputStream.toByteArray();
                // create and wrap a new ChatBubble
                ChatBubble chatBubble = new ChatBubble(imageBytes, time, isSender);
                chatBubbleWrapper = new GradientPanel();
                chatBubbleWrapper.setLayout(new FlowLayout(FlowLayout.RIGHT));
                chatBubbleWrapper.add(Box.createRigidArea(new Dimension(0, 10))); // 10px vertical spacing


                chatBubbleWrapper.setBackground(CustomColor.getBackgroundColor());
                chatBubbleWrapper.add(chatBubble);

                // ensure layout updates correctly
                chatBubbleWrapper.revalidate();
                chatBubbleWrapper.repaint();

                // add new image bubble at the end
                centerPanel.add(chatBubbleWrapper);
                centerPanel.revalidate();
                centerPanel.repaint();

                // scroll to the bottom after adding the new image
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = scrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });

            }
            catch (IOException ex) {
                Logging.logError(ex);
                ShowErrorMessage error = new ShowErrorMessage(Main.mainFrame,"Failed to process image file");
                error.setVisible(true);
            }

            String base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);

            Controller.processRequest(
                    Controller.ServiceID.SendMessage,
                    (error) ->{
                        if (!error.isEmpty()){
                            ShowErrorMessage errorDialog = new ShowErrorMessage(Main.mainFrame, error);
                            errorDialog.setVisible(true);
                        }
                    },
                    base64EncodedImage,
                    timestamp.toString()
            );
        }
    }


    ChatBubbleEmoji chatBubbleEmoji=null;

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public void handleEmojis(){

        // list of emojis
        String[] emojis = {
                "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02", "\uD83E\uDD70", "\uD83D\uDE0D", "\uD83D\uDE0E",
                "\uD83D\uDE22", "\uD83D\uDE21", "\uD83D\uDE31", "\uD83D\uDC4D", "\uD83D\uDC4E", "\uD83D\uDC4C",
                "\u270C\uFE0F", "\u2708\uFE0F", "\uD83C\uDFD6\uFE0F", "\uD83C\uDF89", "\uD83C\uDF82", "\uD83C\uDF0D",
                "\uD83C\uDF08", "\u2615", "\uD83C\uDF55", "\uD83C\uDF54", "\uD83C\uDF63", "\uD83E\uDD73", "\uD83D\uDC36",
                "\uD83D\uDC31", "\uD83E\uDD81", "\uD83D\uDC22", "\uD83C\uDF38", "\uD83C\uDF40", "\u2728", "\uD83C\uDF0A",
                "\uD83C\uDF49", "\uD83D\uDCAA", "\uD83D\uDD25", "\uD83C\uDF1F", "\uD83C\uDFA4", "\uD83D\uDC83",
                "\uD83D\uDC83", "\uD83C\uDFA8", "\uD83C\uDFC6", "\u26BD", "\uD83C\uDFC0", "\uD83C\uDFAE", "\uD83D\uDE80",
                "\uD83D\uDEF6", "\uD83C\uDF04", "\uD83C\uDF05", "\uD83D\uDDF0\uFE0F", "\uD83C\uDFD6\uFE0F", "\uD83D\uDCF8",
                "\uD83D\uDDBC\uFE0F", "\uD83D\uDCDA", "\uD83E\uDDF8", "\uD83C\uDF3C", "\uD83C\uDF41", "\uD83C\uDF39",
                "\uD83C\uDF3B", "\uD83D\uDC07", "\uD83E\uDD8B", "\uD83D\uDC26", "\uD83D\uDC33", "\uD83D\uDC2C",
                "\uD83E\uDD8B", "\uD83C\uDF35", "\uD83C\uDF47", "\uD83E\uDD6D", "\uD83C\uDF4D", "\uD83C\uDF52",
                "\uD83C\uDF49", "\uD83C\uDF83", "\uD83E\uDD84", "\uD83D\uDC96", "\uD83C\uDF0C", "\uD83C\uDF86", "\uD83C\uDF87",
                "\uD83D\uDC51"
        };


        // if chat bubble is open, close it
        if(isEmojiPanelOpen){

            if(chatBubbleEmoji!=null) {
                remove(chatBubbleEmoji);

                chatBubbleEmoji=null;

                isEmojiPanelOpen = false; // reset button
            }
        }else { // otherwise open it

            if(chatBubbleEmoji==null) {
                chatBubbleEmoji = new ChatBubbleEmoji(emojis,messageField);
                add(chatBubbleEmoji,BorderLayout.EAST);
            }

            isEmojiPanelOpen = true;
        }

        revalidate();
        repaint();

    }

}