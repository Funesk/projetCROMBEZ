package projetCROMBEZ;

import javax.swing.JFrame;

/**
 * Singleton centralisant tous les paramètres du jeu.
 * Accessible depuis n'importe quelle classe via GameSettings.getInstance().
 *
 * Contient :
 *  - La difficulté choisie par le joueur
 *  - L'affichage ou non de la portée du joueur
 *  - Le mode plein écran
 *  - Une référence à  la JFrame pour le redimensionnement
 */
public class GameSettings {

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    /** Instance unique (pattern Singleton). */
    private static GameSettings instance;

    /** Retourne l'instance unique, la crÃ©e si elle n'existe pas encore. */
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Paramètres
    // -------------------------------------------------------------------------

    /** Difficulté sélectionnée par le joueur (par défaut : Normal). */
    private DifficultyLevel difficulty = DifficultyLevel.NORMAL;

    /** Afficher ou masquer le cercle de portée autour du joueur. */
    private boolean showPlayerRange = true;

    /** Indique si le jeu est en mode plein écran. */
    private boolean fullscreen = false;

    /** Référence a  la fenetre principale pour gérer le plein écran. */
    private JFrame window;

    // -------------------------------------------------------------------------
    // Constructeur privé (Singleton)
    // -------------------------------------------------------------------------

    private GameSettings() {}

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel d) { this.difficulty = d; }

    public boolean isShowPlayerRange() { return showPlayerRange; }
    public void setShowPlayerRange(boolean show) { this.showPlayerRange = show; }

    public boolean isFullscreen() { return fullscreen; }

    /** Enregistre la JFrame pour pouvoir l'utiliser lors du toggle plein écran. */
    public void setWindow(JFrame window) { this.window = window; }

    // -------------------------------------------------------------------------
    // Multiplicateurs de difficulté (utilisés par EnemyManager)
    // -------------------------------------------------------------------------

    /**
     * Retourne le multiplicateur de points de vie ennemis selon la difficulté.
     * EASY  0.7x | NORMAL  1.0x | HARD 1.5x
     */
    public float getHpMultiplier() {
        switch (difficulty) {
            case EASY:   return 0.7f;
            case HARD:   return 1.5f;
            default:     return 1.0f;
        }
    }

    /**
     * Retourne le multiplicateur de dégats ennemis.
     * EASY  0.6x | NORMAL  1.0x | HARD  1.4x
     */
    public float getDamageMultiplier() {
        switch (difficulty) {
            case EASY:   return 0.6f;
            case HARD:   return 1.4f;
            default:     return 1.0f;
        }
    }

    /**
     * Retourne le nombre d'ennemis maximum par vague selon la difficulté.
     * (Multiplie le nombre de base calculé dans EnemyManager)
     * EASY  0.6x | NORMAL  1.0x | HARD  1.5x
     */
    public float getWaveSizeMultiplier() {
        switch (difficulty) {
            case EASY:   return 0.6f;
            case HARD:   return 1.5f;
            default:     return 1.0f;
        }
    }

    // -------------------------------------------------------------------------
    // Plein écran
    // -------------------------------------------------------------------------

    /**
     * Bascule entre le mode plein écran et le mode fenétré.
     * Nécessite que setWindow() ait été appelé au préalable.
     */
    public void toggleFullscreen() {
        if (window == null) return;

        fullscreen = !fullscreen;

        // On dispose et recrée la fenêtre en mode exclusif
        window.dispose();
        if (fullscreen) {
            window.setUndecorated(true);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            window.setUndecorated(false);
            window.setExtendedState(JFrame.NORMAL);
        }
        window.setVisible(true);
        window.requestFocus();
    }
}
