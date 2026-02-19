package projetCROMBEZ;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Singleton centralisant tous les parametres du jeu.
 * Accessible depuis n'importe quelle classe via GameSettings.getInstance().
 *
 * Contient :
 *  - La difficulte choisie par le joueur
 *  - L'affichage ou non de la portee du joueur
 *  - Le mode plein ecran
 *  - Des references a la JFrame et au GamePanel pour le toggle plein ecran
 */
public class GameSettings {

    // =========================================================================
    // Singleton
    // =========================================================================

    /** Instance unique (pattern Singleton). */
    private static GameSettings instance;

    /** Retourne l'instance unique, la cree si elle n'existe pas encore. */
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    // =========================================================================
    // Parametres
    // =========================================================================

    /** Difficulte selectionnee par le joueur (par defaut : Normal). */
    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;

    /** Afficher ou masquer le cercle de portee autour du joueur. */
    private boolean showPlayerRange = true;

    /** Indique si le jeu est actuellement en mode plein ecran. */
    private boolean fullscreen = false;

    // =========================================================================
    // References externes
    // =========================================================================

    /**
     * Reference a la fenetre principale.
     * Doit etre fournie via setWindow() dans Main avant tout toggle.
     */
    private JFrame window;

    /**
     * Reference au GamePanel.
     *
     */
    private GamePanel gamePanel;

    // =========================================================================
    // Constructeur prive (Singleton)
    // =========================================================================

    private GameSettings() {}

    // =========================================================================
    // Getters / Setters
    // =========================================================================

    public DifficultyLevel getDifficulty()        { return difficulty; }
    public void setDifficulty(DifficultyLevel d)  { this.difficulty = d; }

    public boolean isShowPlayerRange()            { return showPlayerRange; }
    public void setShowPlayerRange(boolean show)  { this.showPlayerRange = show; }

    public boolean isFullscreen()                 { return fullscreen; }

    /** Enregistre la JFrame. A appeler une fois dans Main. */
    public void setWindow(JFrame w)        { this.window    = w; }

    /** Enregistre le GamePanel. A appeler une fois dans Main. */
    public void setGamePanel(GamePanel gp) { this.gamePanel = gp; }

    // =========================================================================
    // Multiplicateurs de difficulte (utilises par EnemyManager)
    // =========================================================================

    /**
     * Multiplicateur de HP des ennemis selon la difficulte.
     * EASY -> 0.7x | NORMAL -> 1.0x | HARD -> 1.5x
     */
    public float getHpMultiplier() {
        switch (difficulty) {
            case EASY:  return 0.7f;
            case HARD:  return 1.5f;
            default:    return 1.0f;
        }
    }

    /**
     * Multiplicateur de degats des ennemis.
     * EASY -> 0.6x | NORMAL -> 1.0x | HARD -> 1.4x
     */
    public float getDamageMultiplier() {
        switch (difficulty) {
            case EASY:  return 0.6f;
            case HARD:  return 1.4f;
            default:    return 1.0f;
        }
    }

    /**
     * Multiplicateur du nombre d'ennemis par vague.
     * EASY -> 0.6x | NORMAL -> 1.0x | HARD -> 1.5x
     */
    public float getWaveSizeMultiplier() {
        switch (difficulty) {
            case EASY:  return 0.6f;
            case HARD:  return 1.5f;
            default:    return 1.0f;
        }
    }

    // =========================================================================
    // Plein ecran
    // =========================================================================

    /**
     * Bascule entre mode plein ecran et mode fenetre.
     *
     */
    public void toggleFullscreen() {
        if (window == null) return;

        fullscreen = !fullscreen;

        // Obligatoire avant setUndecorated sur une fenetre deja visible
        window.dispose();

        if (fullscreen) {
            window.setUndecorated(true);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            window.setUndecorated(false);
            window.setExtendedState(JFrame.NORMAL);
            // Reajuste la taille de la fenetre a la taille preferee du GamePanel
            window.pack();
            window.setLocationRelativeTo(null);
        }

        window.setVisible(true);

        // Redonner le focus clavier au GamePanel apres que la fenetre
        // soit reellement rendue et dans un etat focusable.
        // invokeLater garantit que cet appel se fait APRES le repaint initial.
        if (gamePanel != null) {
            SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
        }
    }
}
