package Utils;

import java.io.File;

public class FILE{
    public static final String base =new File("src/UserConfig").getAbsolutePath()+"/";
    public static final String chatHistory = base + "chatHistory_";
    public static final String keys = base + "keys_";
    public static final String logger = base + "logger.txt";
    public static final String session = base + "session.txt";
}


