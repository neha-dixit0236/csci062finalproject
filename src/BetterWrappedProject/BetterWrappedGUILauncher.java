package BetterWrappedProject;

/**
 * A standard workaround to launch JavaFX 11+ applications 
 * without triggering strict module-path errors in the IDE.
 */
public class BetterWrappedGUILauncher {
    public static void main(String[] args) {
        BetterWrappedGUI.main(args);
    }
}