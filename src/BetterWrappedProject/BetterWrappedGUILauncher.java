package BetterWrappedProject;

/**
 * A standard workaround to launch JavaFX 11+ applications 
 * without triggering strict module-path errors in the IDE.
 * 
 * Since this is out of the scope of the class, 
 * we used LLMs to assist us in building the GUI. 
 * The full conversation transcript detailing this assistance can be found here: 
 * https://claude.ai/share/60df2777-4181-44d5-8081-8332fc97ae03
 */
public class BetterWrappedGUILauncher {
    public static void main(String[] args) {
        BetterWrappedGUI.main(args);
    }
}