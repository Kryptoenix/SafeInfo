package Utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Sound {
    private static boolean playLoginSound=true;

    public static boolean getPlayLoginSound() {
        return playLoginSound;
    }

    public static void setPlayLoginSound(boolean playLoginSound) {
        Sound.playLoginSound = playLoginSound;
    }

    public static void logout() {
        try {
            File soundFile = new File(ASSETS.logoutSound);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            // prepare login sound for next login
            if(!getPlayLoginSound()){
                setPlayLoginSound(true);
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            Logging.logError(e);
            System.out.println("Failed to play logout sound");
        } catch (IOException e) {
            Logging.logError(e);
            System.out.println("Failed to find sound file");
        }
    }

    public static void login(){
        try {
            // if sound has already played once, stop it
            if(getPlayLoginSound()){

                File soundFile = new File(ASSETS.startSound);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();

                setPlayLoginSound(false);
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            Logging.logError(e);
            System.out.println("Failed to play logout sound");
        } catch (IOException e) {
            Logging.logError(e);
            System.out.println("Failed to find sound file");
        }
    }
}