
public class Main {

    public static void main(String[] args) {
        Thread guiThread = new Thread(GUI.Main::init);

        guiThread.start();

        try {
            guiThread.join();
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted: " + e.getMessage());
        }
    }
}
