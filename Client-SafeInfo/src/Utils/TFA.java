package Utils;

public class TFA{
    // mark what panel should follow after the 2FA verification successfully completes
    private static String switchTo;

    public static void setSwitchTo(String switchTo) {
        TFA.switchTo = switchTo;
    }

    public static String getSwitchTo() {
        return TFA.switchTo;
    }
}
