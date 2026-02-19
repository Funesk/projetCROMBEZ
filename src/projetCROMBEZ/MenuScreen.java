package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecran du menu principal.
 *
 * Premier ecran affiche au lancement du jeu.
 * Boutons disponibles :
 *  1. Jouer        -> ouvre l'ecran de selection de difficulte
 *  2. Options      -> ouvre l'ecran des options (retour au menu)
 *  3. Credits      -> (TODO : a implementer)
 *  4. Quitter      -> ferme l'application
 *
 * --- Architecture listener ---
 * Cette classe N'enregistre PLUS de MouseListener directement sur le GamePanel.
 * C'est GamePanel qui centralise tous les evenements souris et appelle
 * handleClick() et handleHover() uniquement quand gameState == MENU.
 * Cela evite les conflits entre les listeners des differents ecrans.
 */
public class MenuScreen {

    // =========================================================================
    // References
    // =========================================================================

    /** Reference au GamePanel pour la navigation et les dimensions. */
    private GamePanel gp;

    // =========================================================================
    // Boutons
    // =========================================================================

    /** Liste des rectangles cliquables dans l'ordre des boutons. */
    private List<Rectangle> buttons = new ArrayList<>();

    /** Labels affiches sur chaque bouton. */
    private String[] buttonLabels = { "Jouer", "Options", "Credits", "Quitter" };

    /** Index du bouton actuellement survole (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons
    private static final int BTN_W   = 220;
    private static final int BTN_H   = 50;
    private static final int BTN_GAP = 20;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le menu et calcule les positions des boutons.
     * Aucun MouseListener n'est enregistre ici : la gestion des evenements
     * est centralisee dans GamePanel.
     *
     * @param gp Reference au GamePanel
     */
    public MenuScreen(GamePanel gp) {
        this.gp = gp;

        // Calcul des positions de chaque bouton (centres horizontalement)
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
     * Traite un clic souris sur cet ecran.
     * Appele par GamePanel UNIQUEMENT quand gameState == MENU.
     *
     * @param p Position du clic en coordonnees ecran
     */
    public void handleClick(Point p) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) {
                handleButton(i);
                return; // un seul bouton par clic
            }
        }
    }

    /**
     * Met a jour le bouton survole en fonction de la position de la souris.
     * Appele par GamePanel UNIQUEMENT quand gameState == MENU.
     *
     * @param p Position de la souris en coordonnees ecran
     */
    public void handleHover(Point p) {
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
     * @param index Position du bouton dans buttonLabels
     */
    private void handleButton(int index) {
        switch (index) {
            case 0: // Jouer -> selection de difficulte
                gp.gameState = GameState.DIFFICULTY;
                break;

            case 1: // Options -> retour au menu apres
                gp.optionsScreen.setReturnState(GameState.MENU);
                gp.gameState = GameState.OPTIONS;
                break;

            case 2: // Credits (a implementer)
                // TODO : creer un ecran de credits
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
     * Dessine le menu principal : fond degrade, titre du jeu et boutons.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Fond degrade sombre
        GradientPaint gradient = new GradientPaint(
                0, 0,               new Color(10, 10, 30),
                0, gp.screenHeight, new Color(30, 10, 60)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // Titre principal avec ombre portee
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 52f)
                                             : new Font("Arial", Font.BOLD, 52);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();

        String title  = "SURVIVOR";
        int    titleX = gp.screenWidth  / 2 - fm.stringWidth(title) / 2;
        int    titleY = gp.screenHeight / 2 - 170;

        g2.setColor(new Color(120, 0, 0)); // ombre
        g2.drawString(title, titleX + 3, titleY + 3);
        g2.setColor(new Color(220, 50, 50)); // titre
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
    }
}
