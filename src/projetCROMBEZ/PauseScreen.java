package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu pause - affiche par-dessus le jeu quand le joueur appuie sur ECHAP.
 *
 * Boutons disponibles :
 *  1. Reprendre       -> reprend la partie (PLAYING)
 *  2. Options         -> ouvre les options avec retour a PAUSED
 *  3. Infos           -> affiche les controles et infos de jeu
 *  4. Menu principal  -> retour au menu (MENU)
 *  5. Quitter         -> ferme l'application
 *
 * --- Architecture listener ---
 * Aucun MouseListener n'est enregistre ici. GamePanel appelle
 * handleClick() et handleHover() UNIQUEMENT quand gameState == PAUSED.
 * Cela resout le bug ou un clic sur "Retour" dans OptionsScreen
 * etait aussi recu par PauseScreen car les deux boutons se superposaient
 * visuellement et les deux listeners etaient actifs en meme temps.
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
    private String[] labels = { "Reprendre", "Options", "Infos", "Menu principal", "Quitter" };

    /** Zones cliquables des boutons. */
    private List<Rectangle> buttons = new ArrayList<>();

    /** Index du bouton survole (-1 = aucun). */
    private int hoveredButton = -1;

    /** true si l'ecran d'infos (controles) est affiche par-dessus le menu pause. */
    private boolean showInfo = false;

    // Dimensions des boutons
    private static final int BTN_W = 240;
    private static final int BTN_H = 48;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le menu pause et calcule les positions des boutons.
     * Aucun MouseListener n'est enregistre ici.
     *
     * @param gp Reference au GamePanel
     */
    public PauseScreen(GamePanel gp) {
        this.gp = gp;

        int cx     = gp.screenWidth  / 2;
        int startY = gp.screenHeight / 2 - 110;
        int gap    = 60;

        for (int i = 0; i < labels.length; i++) {
            buttons.add(new Rectangle(cx - BTN_W / 2, startY + i * gap, BTN_W, BTN_H));
        }
    }

    // =========================================================================
    // Gestion des evenements (appeles par GamePanel)
    // =========================================================================

    /**
     * Traite un clic souris sur cet ecran.
     * Appele par GamePanel UNIQUEMENT quand gameState == PAUSED.
     *
     * Si l'ecran d'infos est ouvert, n'importe quel clic le ferme.
     *
     * @param p Position du clic en coordonnees ecran
     */
    public void handleClick(Point p) {
        // Si le panneau d'infos est affiche, un clic quelconque le ferme
        if (showInfo) {
            showInfo = false;
            return;
        }

        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) {
                handleButton(i);
                return; // un seul bouton par clic
            }
        }
    }

    /**
     * Met a jour le bouton survole.
     * Appele par GamePanel UNIQUEMENT quand gameState == PAUSED.
     *
     * @param p Position de la souris en coordonnees ecran
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
     * Reinitialise l'etat interne du menu pause (ferme l'ecran d'infos si ouvert).
     * Appele par GamePanel quand on entre dans l'etat PAUSED.
     */
    public void reset() {
        showInfo      = false;
        hoveredButton = -1;
    }

    // =========================================================================
    // Actions des boutons
    // =========================================================================

    /**
     * Execute l'action associee au bouton clique.
     *
     * @param index Index du bouton dans le tableau labels
     */
    private void handleButton(int index) {
        switch (index) {
            case 0: // Reprendre
                gp.gameState = GameState.PLAYING;
                break;

            case 1: // Options -> retour a PAUSED apres
                gp.optionsScreen.setReturnState(GameState.PAUSED);
                gp.gameState = GameState.OPTIONS;
                break;

            case 2: // Infos
                showInfo = true;
                break;

            case 3: // Menu principal
                gp.gameState = GameState.MENU;
                break;

            case 4: // Quitter
                System.exit(0);
                break;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine le menu pause par-dessus le jeu.
     * Fond semi-transparent, panneau central avec boutons.
     * Si showInfo est true, dessine le panneau d'informations a la place.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Overlay sombre par-dessus le jeu
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (showInfo) {
            drawInfoPanel(g2);
            return;
        }

        // Panneau central
        int panelW = BTN_W + 60;
        int panelH = labels.length * 60 + 80;
        int panelX = gp.screenWidth  / 2 - panelW / 2;
        int panelY = gp.screenHeight / 2 - panelH / 2 - 20;

        g2.setColor(new Color(15, 15, 35, 230));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setColor(new Color(80, 80, 140));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // Titre "PAUSE"
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 32f)
                                             : new Font("Arial", Font.BOLD, 32);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "PAUSE";
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, panelY + 45);

        // Boutons
        Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(btnFont);
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn     = buttons.get(i);
            boolean   hovered = (i == hoveredButton);

            // Couleur speciale pour "Quitter" (rouge)
            Color bgNormal  = (i == 4) ? new Color(80, 20, 20)  : new Color(40, 40, 70);
            Color bgHovered = (i == 4) ? new Color(160, 30, 30) : new Color(80, 80, 130);

            g2.setColor(hovered ? bgHovered : bgNormal);
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 12, 12);

            g2.setColor(hovered ? Color.white : new Color(80, 80, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 12, 12);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(Color.white);
            int tx = btn.x + btn.width  / 2 - fm.stringWidth(labels[i]) / 2;
            int ty = btn.y + btn.height / 2 + fm.getAscent() / 2 - 3;
            g2.drawString(labels[i], tx, ty);
        }

        // Hint en bas du panneau
        Font hintFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                            : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(hintFont);
        g2.setColor(new Color(150, 150, 150));
        fm = g2.getFontMetrics();
        String hint = "ECHAP pour reprendre";
        g2.drawString(hint, gp.screenWidth / 2 - fm.stringWidth(hint) / 2,
                      panelY + panelH + 22);
    }

    /**
     * Dessine le panneau d'informations sur les controles et les mecaniques.
     * Affiche quand l'utilisateur clique sur "Infos".
     * Un clic quelconque ferme ce panneau.
     *
     * @param g2 Contexte graphique
     */
    private void drawInfoPanel(Graphics2D g2) {
        int panelW = 500;
        int panelH = 400;
        int panelX = gp.screenWidth  / 2 - panelW / 2;
        int panelY = gp.screenHeight / 2 - panelH / 2;

        g2.setColor(new Color(10, 10, 30, 240));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setColor(new Color(100, 100, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // Titre
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 24f)
                                             : new Font("Arial", Font.BOLD, 24);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String t = "Controles & Infos";
        g2.drawString(t, gp.screenWidth / 2 - fm.stringWidth(t) / 2, panelY + 40);

        // Lignes de contenu
        Font infoFont = gp.gameFont != null ? gp.gameFont.deriveFont(15f)
                                            : new Font("Arial", Font.PLAIN, 15);
        g2.setFont(infoFont);
        g2.setColor(new Color(200, 200, 230));

        String[] lines = {
            "  Deplacement",
            "    Z / Fleche haut    ->  Monter",
            "    S / Fleche bas     ->  Descendre",
            "    Q / Fleche gauche  ->  Gauche",
            "    D / Fleche droite  ->  Droite",
            "",
            "  Mecaniques",
            "    Attaque  : automatique sur le plus proche",
            "    Vagues   : 4 vagues + 1 boss final",
            "    ECHAP    : ouvrir / fermer la pause",
            "",
            "         Cliquer pour fermer"
        };

        int lineY = panelY + 75;
        for (String line : lines) {
            g2.drawString(line, panelX + 15, lineY);
            lineY += 23;
        }
    }
}
