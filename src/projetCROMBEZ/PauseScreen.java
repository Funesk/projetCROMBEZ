package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu pause - affiche par-dessus le jeu quand le joueur appuie sur ECHAP.
 *
 * Boutons disponibles :
 *  1. Reprendre      -> reprend la partie (PLAYING)
 *  2. Sauvegarder    -> sauvegarde manuellement la partie en cours
 *  3. Options        -> ouvre les options avec retour a PAUSED
 *  4. Infos          -> affiche le panneau InfoPanel (stats + controles)
 *  5. Menu principal -> retour au menu (MENU)
 *  6. Quitter        -> ferme l'application
 *
 * --- Position du hint ---
 * Le texte "ECHAP pour reprendre" est affiche SOUS le panneau (et non
 * a l'interieur) pour eviter toute superposition avec les boutons.
 *
 * --- Architecture listener ---
 * Aucun MouseListener enregistre ici. GamePanel dispatche les evenements
 * via handleClick() et handleHover() uniquement quand gameState == PAUSED.
 */
public class PauseScreen {

    // =========================================================================
    // References
    // =========================================================================

    /** Reference au GamePanel. */
    private GamePanel gp;

    // =========================================================================
    // Boutons
    // =========================================================================

    /** Labels des boutons dans l'ordre d'affichage. */
    private String[] labels = {
        "Reprendre", "Sauvegarder", "Options", "Infos", "Menu principal", "Quitter"
    };

    /** Zones cliquables des boutons. */
    private List<Rectangle> buttons = new ArrayList<>();

    /** Index du bouton survole (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons
    private static final int BTN_W = 240;
    private static final int BTN_H = 48;
    private static final int BTN_GAP = 60; // espacement entre boutons

    // =========================================================================
    // Panneau Infos
    // =========================================================================

    /** Panneau d'informations partage avec MenuScreen. */
    private InfoPanel infoPanel;

    /** true quand le panneau d'informations est affiche. */
    private boolean showInfo = false;

    // =========================================================================
    // Feedback sauvegarde
    // =========================================================================

    /**
     * Compteur de frames pour afficher le message "Sauvegarde !" apres un clic.
     * Decremente chaque frame, affiche le message tant qu'il est > 0.
     */
    private int saveMessageTimer = 0;

    /** Duree d'affichage du message de confirmation (2 secondes a 60 FPS). */
    private static final int SAVE_MESSAGE_DURATION = 120;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le menu pause et calcule les positions des boutons.
     *
     * @param gp Reference au GamePanel
     */
    public PauseScreen(GamePanel gp) {
        this.gp        = gp;
        this.infoPanel = new InfoPanel(gp);

        int cx     = gp.screenWidth  / 2;
        int startY = gp.screenHeight / 2 - (labels.length * BTN_GAP) / 2;

        for (int i = 0; i < labels.length; i++) {
            buttons.add(new Rectangle(cx - BTN_W / 2, startY + i * BTN_GAP, BTN_W, BTN_H));
        }
    }

    // =========================================================================
    // Gestion des evenements (appeles par GamePanel)
    // =========================================================================

    /**
     * Traite un clic.
     * Si le panneau Infos est ouvert, n'importe quel clic le ferme.
     *
     * @param p Position du clic
     */
    public void handleClick(Point p) {
        if (showInfo) {
            showInfo = false;
            return;
        }
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) {
                handleButton(i);
                return;
            }
        }
    }

    /**
     * Met a jour le bouton survole.
     *
     * @param p Position de la souris
     */
    public void handleHover(Point p) {
        if (showInfo) { hoveredButton = -1; return; }
        hoveredButton = -1;
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) {
                hoveredButton = i;
                return;
            }
        }
    }

    /**
     * Reinitialise l'etat interne (ferme Infos si ouvert, efface le feedback).
     * Appele par GamePanel quand on entre dans l'etat PAUSED.
     */
    public void reset() {
        showInfo         = false;
        hoveredButton    = -1;
        saveMessageTimer = 0;
    }

    /**
     * Decremente le timer du message de sauvegarde.
     * Appele par GamePanel.update() quand gameState == PAUSED.
     */
    public void tick() {
        if (saveMessageTimer > 0) saveMessageTimer--;
    }

    // =========================================================================
    // Actions des boutons
    // =========================================================================

    /**
     * Execute l'action associee au bouton clique.
     *
     * @param index Index dans le tableau labels
     */
    private void handleButton(int index) {
        switch (index) {
            case 0: // Reprendre
                gp.gameState = GameState.PLAYING;
                break;

            case 1: // Sauvegarder
                // Sauvegarde manuelle + feedback visuel
                SaveManager.save(gp);
                saveMessageTimer = SAVE_MESSAGE_DURATION;
                break;

            case 2: // Options -> retour a PAUSED apres
                gp.optionsScreen.setReturnState(GameState.PAUSED);
                gp.gameState = GameState.OPTIONS;
                break;

            case 3: // Infos
                showInfo = true;
                break;

            case 4: // Menu principal
                // Marque la partie comme sauvegardee/quittee proprement
                gp.hasActiveGame = true; // la partie peut etre reprise depuis le menu
                gp.gameState = GameState.MENU;
                break;

            case 5: // Quitter
                SaveManager.save(gp);
                System.exit(0);
                break;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine le menu pause par-dessus le jeu.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Overlay sombre par-dessus le jeu fige
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Panneau Infos (prend tout l'ecran si actif)
        if (showInfo) {
            infoPanel.draw(g2);
            return;
        }

        // Dimensions du panneau central
        // La hauteur est calculee pour contenir exactement les boutons
        // sans que le hint vienne s'y superposer (il est place EN DESSOUS)
        int panelW = BTN_W + 60;
        int panelH = labels.length * BTN_GAP + 50; // titre + boutons seulement
        int panelX = gp.screenWidth  / 2 - panelW / 2;
        int panelY = gp.screenHeight / 2 - panelH / 2 - 10;

        // Fond et bordure du panneau
        g2.setColor(new Color(15, 15, 35, 235));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setColor(new Color(80, 80, 140));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // Titre "PAUSE"
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 28f)
                                             : new Font("Arial", Font.BOLD, 28);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "PAUSE";
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, panelY + 30);

        // Boutons
        Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(17f)
                                           : new Font("Arial", Font.PLAIN, 17);
        g2.setFont(btnFont);
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn     = buttons.get(i);
            boolean   hovered = (i == hoveredButton);

            // Couleurs specifiques par bouton
            Color bgNormal, bgHovered;
            if (i == 1) {
                // Sauvegarder : bleu-vert
                bgNormal  = new Color(20, 60, 60);
                bgHovered = new Color(30, 110, 100);
            } else if (i == 5) {
                // Quitter : rouge
                bgNormal  = new Color(80, 20, 20);
                bgHovered = new Color(160, 30, 30);
            } else {
                // Autres : bleu sombre standard
                bgNormal  = new Color(40, 40, 70);
                bgHovered = new Color(80, 80, 130);
            }

            g2.setColor(hovered ? bgHovered : bgNormal);
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 12, 12);

            g2.setColor(hovered ? Color.white : new Color(80, 80, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 12, 12);
            g2.setStroke(new BasicStroke(1f));

            // Icone de coche verte sur "Sauvegarder" apres sauvegarde
            String displayLabel = labels[i];
            if (i == 1 && saveMessageTimer > 0) {
                displayLabel = "Sauvegarde !";
                g2.setColor(new Color(80, 220, 120));
            } else {
                g2.setColor(Color.white);
            }

            int tx = btn.x + btn.width  / 2 - fm.stringWidth(displayLabel) / 2;
            int ty = btn.y + btn.height / 2 + fm.getAscent() / 2 - 3;
            g2.drawString(displayLabel, tx, ty);
        }

        // Hint "ECHAP pour reprendre" place SOUS le panneau (jamais superpose)
        Font hintFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                            : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(hintFont);
        g2.setColor(new Color(140, 140, 160));
        fm = g2.getFontMetrics();
        String hint = "ECHAP pour reprendre";
        g2.drawString(hint,
                gp.screenWidth / 2 - fm.stringWidth(hint) / 2,
                panelY + panelH + 20); // 20px SOUS le bord inferieur du panneau
    }
}
