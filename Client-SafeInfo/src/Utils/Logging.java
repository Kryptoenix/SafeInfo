package Utils;

import Design.CustomLabel;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class Logging {
    public static void logError(Exception exception) {

        // use a string writer to redirect the print writer content => it allows the stack trace to be stored as a string in memory
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        String message = sw.toString();

        // file for logging
        String logFile = FILE.logger;

        // generate a timestamp associated with the error
        Instant ts = Instant.now();

        String error = "[" + ts.toString() + "] : " + message + "\n\n";

        try{
            Path logFilePath = Path.of(logFile);

            if(!Files.exists(logFilePath)){
                Files.createFile(logFilePath);
            }

            Files.write(logFilePath, error.getBytes());

        }catch (IOException e){
            JDialog dialog = new JDialog();
            dialog.setTitle("Error");
            dialog.setLocationRelativeTo(null);

            CustomLabel errorLabel = new CustomLabel(message);
            dialog.add(errorLabel);

            dialog.setVisible(true);
        }

    }
}