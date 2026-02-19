package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecran du menu principal.
 *
 * Boutons disponibles :
 *  1. Jouer    -> ouvre l'ecran de selection de difficulte
 *  2. Options  -> ouvre l'ecran des options (retour MENU)
 *  3. Infos    -> affiche le panneau d'informations et statistiques (InfoPanel)
 *  4. Quitter  -> ferme l'application
 *
 * --- Gestion du panneau Infos ---
 * Quand showInfo est true, InfoPanel.draw() est appele par-dessus le menu.
 * N'importe quel clic ferme le panneau (gere dans handleClick).
 *
 * --- Architecture listener ---
 * Aucun MouseListener enregistre ici. GamePanel dispatche les evenements
 * via handleClick() et handleHover() uniquement quand gameState == MENU.
 */
public class MenuScreen {

    // =========================================================================
    // References
    // =========================================================================

    /** Reference au GamePanel. */
    private GamePanel gp;

    // =========================================================================
    // Boutons
    // =========================================================================

    /** Zones cliquables des boutons. */
    private List<Rectangle> buttons = new ArrayList<>();

    /** Labels affiches sur chaque bouton. */
    private String[] buttonLabels = { "Jouer", "Options", "Infos", "Quitter" };

    /** Index du bouton survole (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons
    private static final int BTN_W   = 220;
    private static final int BTN_H   = 50;
    private static final int BTN_GAP = 20;

    // =========================================================================
    // Panneau Infos
    // =========================================================================

    /**
     * Panneau d'informations partage avec PauseScreen.
     * Affiche les stats joueur, stats ennemis et composition des vagues.
     */
    private InfoPanel infoPanel;

    /** true quand le panneau d'informations est affiche par-dessus le menu. */
    private boolean showInfo = false;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le menu et calcule les positions des boutons.
     *
     * @param gp Reference au GamePanel
     */
    public MenuScreen(GamePanel gp) {
        this.gp        = gp;
        this.infoPanel = new InfoPanel(gp);

        int btnX   = gp.screenWidth  / 2 - BTN_W / 2;
        int startY = gp.screenHeight / 2 - 60;

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons.add(new Rectangle(btnX, startY + i * (BTN_H + BTN_GAP), BTN_W, BTN_H));
        }
    }

    // =========================================================================
    // Gestion des evenements (appeles par GamePanel)
    // =========================================================================

    /**
     * Traite un clic.
     * Si le panneau Infos est ouvert, n'importe quel clic le ferme.
     * Sinon, dispatch vers le bouton clique.
     *
     * @param p Position du clic
     */
    public void handleClick(Point p) {
        // Ferme le panneau d'infos si ouvert
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
     * Ignore le hover si le panneau Infos est ouvert.
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

    // =========================================================================
    // Actions des boutons
    // =========================================================================

    /**
     * Execute l'action correspondant au bouton clique.
     *
     * @param index Index dans buttonLabels
     */
    private void handleButton(int index) {
        switch (index) {
            case 0: // Jouer
                gp.gameState = GameState.DIFFICULTY;
                break;

            case 1: // Options
                gp.optionsScreen.setReturnState(GameState.MENU);
                gp.gameState = GameState.OPTIONS;
                break;

            case 2: // Infos - affiche le panneau par-dessus le menu
                showInfo = true;
                break;

            case 3: // Quitter
                System.exit(0);
                break;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine le menu principal.
     * Si showInfo est true, dessine aussi le panneau InfoPanel par-dessus.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Fond degrade
        GradientPaint gradient = new GradientPaint(
                0, 0,               new Color(10, 10, 30),
                0, gp.screenHeight, new Color(30, 10, 60)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // Titre avec ombre
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 52f)
                                             : new Font("Arial", Font.BOLD, 52);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();

        String title  = "SURVIVOR";
        int    titleX = gp.screenWidth  / 2 - fm.stringWidth(title) / 2;
        int    titleY = gp.screenHeight / 2 - 170;

        g2.setColor(new Color(120, 0, 0));
        g2.drawString(title, titleX + 3, titleY + 3);
        g2.setColor(new Color(220, 50, 50));
        g2.drawString(title, titleX, titleY);

        // Sous-titre
        Font subFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(subFont);
        fm = g2.getFontMetrics();
        String sub = "Rogue-lite";
        g2.setColor(new Color(200, 150, 50));
        g2.drawString(sub, gp.screenWidth / 2 - fm.stringWidth(sub) / 2, titleY + 36);

        // Boutons
        Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(20f)
                                           : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(btnFont);
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn     = buttons.get(i);
            boolean   hovered = (i == hoveredButton);

            g2.setColor(hovered ? new Color(180, 40, 40) : new Color(60, 20, 20));
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            g2.setColor(hovered ? new Color(255, 100, 100) : new Color(120, 40, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(Color.white);
            int textX = btn.x + btn.width  / 2 - fm.stringWidth(buttonLabels[i]) / 2;
            int textY = btn.y + btn.height / 2 + fm.getAscent() / 2 - 2;
            g2.drawString(buttonLabels[i], textX, textY);
        }

        // Version
        Font versionFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                               : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(versionFont);
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("v0.2 - Alpha", 10, gp.screenHeight - 10);

        // Panneau d'infos (affiche par-dessus si showInfo est true)
        if (showInfo) {
            infoPanel.draw(g2);
        }
    }
}
