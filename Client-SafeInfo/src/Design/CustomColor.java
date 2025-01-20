package Design;

import java.awt.*;

public class CustomColor{
    // Light Theme Colors
    private static final Color lightBackgroundColor = Color.decode("#F5FFFA");   // Mint Cream
    private static final Color lightPrimaryColor = Color.decode("#009999");      // Persian Green
    private static final Color lightSecondaryColor = Color.decode("#483D8B");    // Dark Slate Blue
    private static final Color lightButtonTextColor = Color.decode("#FFFFF0");   // Ivory
    private static final Color lightTextFieldBackground = Color.decode("#F0FFF0"); // Honeydew

    // Dark Theme Colors
    private static final Color darkBackgroundColor = Color.decode("#222831");    // Charcoal
    private static final Color darkPrimaryColor = Color.decode("#006666");       // Dark Persian Green
    private static final Color darkSecondaryColor = Color.decode("#2C274F");     // Darker Slate Blue
    private static final Color darkButtonTextColor = Color.decode("#F5F5F5");    // Light Gray (almost white)
    private static final Color darkTextFieldBackground = Color.decode("#404B55"); // Darker Honeydew

    public static boolean isDarkTheme = false;

    public static Color getBackgroundColor() {
        return isDarkTheme ? darkBackgroundColor : lightBackgroundColor;
    }
    public static Color getPrimaryColor() {
        return isDarkTheme ? darkPrimaryColor : lightPrimaryColor;
    }

    public static Color getSecondaryColor() {
        return isDarkTheme ? darkSecondaryColor : lightSecondaryColor;
    }

    public static Color getButtonTextColor() {
        return isDarkTheme ? darkButtonTextColor : lightButtonTextColor;
    }

    public static Color getTextFieldBackground() {
        return isDarkTheme ? darkTextFieldBackground : lightTextFieldBackground;
    }
}

