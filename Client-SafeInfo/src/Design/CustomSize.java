package Design;

import java.awt.*;

public class CustomSize{
    public static Dimension getDimension(Integer... size) {
        int width = (size.length > 0) ? size[0] : 100;
        int height = (size.length > 1) ? size[1] : 50;
        return new Dimension(width, height);
    }
}

