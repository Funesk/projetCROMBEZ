package projetCROMBEZ;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Singleton centralisant tous les parametres du jeu.
 *
 * Chaque setter qui modifie un parametre appelle automatiquement
 * SaveManager.save() pour persister le changement immediatement.
 * Ainsi, les options sont toujours sauvegardees sans action explicite.
 *
 * Contient :
 *  - La difficulte choisie par le joueur
 *  - L'affichage ou non de la portee du joueur
 *  - Le mode plein ecran
 *  - Des references a la JFrame et au GamePanel
 */
public class GameSettings {

    // =========================================================================
    // Singleton
    // =========================================================================

    private static GameSettings instance;

    public static GameSettings getInstance() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }

    // =========================================================================
    // Parametres
    // =========================================================================

    /** Difficulte selectionnee par le joueur. */
    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;

    /** Afficher le cercle de portee autour du joueur. */
    private boolean showPlayerRange = true;

    /** Mode plein ecran actif. */
    private boolean fullscreen = false;

    // =========================================================================
    // References externes
    // =========================================================================

    /** Fenetre principale (necessaire pour toggleFullscreen). */
    private JFrame window;

    /**
     * Panneau de jeu principal.
     * Utilise pour :
     *  1. Redonner le focus clavier apres toggleFullscreen.
     *  2. Acceder aux stats joueur dans SaveManager.save().
     */
    private GamePanel gamePanel;

    // =========================================================================
    // Constructeur prive
    // =========================================================================

    private GameSettings() {}

    // =========================================================================
    // Getters / Setters
    // =========================================================================

    public DifficultyLevel getDifficulty() { return difficulty; }

    /**
     * Change la difficulte et sauvegarde immediatement.
     * Appele par DifficultyScreen avant de lancer une partie.
     */
    public void setDifficulty(DifficultyLevel d) {
        this.difficulty = d;
        saveIfReady();
    }

    public boolean isShowPlayerRange() { return showPlayerRange; }

    /**
     * Active/desactive l'affichage de la portee et sauvegarde immediatement.
     * Appele par OptionsScreen.
     */
    public void setShowPlayerRange(boolean show) {
        this.showPlayerRange = show;
        saveIfReady();
    }

    public boolean isFullscreen() { return fullscreen; }

    public void setWindow(JFrame w)        { this.window    = w; }
    public void setGamePanel(GamePanel gp) { this.gamePanel = gp; }

    // =========================================================================
    // Multiplicateurs de difficulte
    // =========================================================================

    /**
     * Multiplicateur de HP ennemis.
     * EASY -> 0.7 | NORMAL -> 1.0 | HARD -> 1.5
     */
    public float getHpMultiplier() {
        switch (difficulty) {
            case EASY:  return 0.7f;
            case HARD:  return 1.5f;
            default:    return 1.0f;
        }
    }

    /**
     * Multiplicateur de degats ennemis.
     * EASY -> 0.6 | NORMAL -> 1.0 | HARD -> 1.4
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
     * EASY -> 0.6 | NORMAL -> 1.0 | HARD -> 1.5
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
     * Bascule plein ecran / fenetre.
     *
     * Apres le cycle dispose/setVisible (obligatoire pour changer la decoration),
     * restitue le focus clavier via invokeLater.
     * Sauvegarde le nouvel etat apres le toggle.
     */
    public void toggleFullscreen() {
        if (window == null) return;

        fullscreen = !fullscreen;

        window.dispose();
        if (fullscreen) {
            window.setUndecorated(true);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            window.setUndecorated(false);
            window.setExtendedState(JFrame.NORMAL);
            window.pack();
            window.setLocationRelativeTo(null);
        }
        window.setVisible(true);

        // Focus clavier restaure apres que la fenetre soit reellement affichee
        if (gamePanel != null) {
            SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
        }

        // Sauvegarde le nouvel etat plein ecran
        saveIfReady();
    }

    // =========================================================================
    // Sauvegarde
    // =========================================================================

    /**
     * Declenche une sauvegarde si le GamePanel est disponible.
     *
     * Protege contre les appels trop precoces (pendant le chargement
     * initial, avant que gamePanel soit assigne).
     */
    private void saveIfReady() {
        if (gamePanel != null) {
            SaveManager.save(gamePanel);
        }
    }
}
