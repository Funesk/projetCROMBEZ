package projetCROMBEZ;

import java.awt.*;

/**
 * Ecran de selection de la difficulte.
 *
 * Affiche quand le joueur clique sur "Jouer" depuis le menu principal.
 * Presente trois boutons (Facile / Normal / Difficile) avec une description
 * des modificateurs appliques, et un bouton "Retour".
 *
 * --- Architecture listener ---
 * Aucun MouseListener n'est enregistre ici. GamePanel appelle
 * handleClick() et handleHover() uniquement quand gameState == DIFFICULTY.
 */
public class DifficultyScreen {

    // =========================================================================
    // References
    // =========================================================================

    /** Reference au GamePanel. */
    private GamePanel gp;

    // =========================================================================
    // Boutons
    // =========================================================================

    /** Zone cliquable pour le niveau Facile. */
    private Rectangle btnEasy;

    /** Zone cliquable pour le niveau Normal. */
    private Rectangle btnNormal;

    /** Zone cliquable pour le niveau Difficile. */
    private Rectangle btnHard;

    /** Zone cliquable pour revenir au menu principal. */
    private Rectangle btnBack;

    /** Index du bouton survole (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons
    private static final int BTN_W = 300;
    private static final int BTN_H = 70;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree l'ecran de difficulte et calcule les positions des boutons.
     * Aucun MouseListener n'est enregistre ici.
     *
     * @param gp Reference au GamePanel
     */
    public DifficultyScreen(GamePanel gp) {
        this.gp = gp;

        int cx     = gp.screenWidth / 2;
        int startY = 300;
        int gap    = 90;

        btnEasy   = new Rectangle(cx - BTN_W / 2, startY,            BTN_W, BTN_H);
        btnNormal = new Rectangle(cx - BTN_W / 2, startY + gap,      BTN_W, BTN_H);
        btnHard   = new Rectangle(cx - BTN_W / 2, startY + gap * 2,  BTN_W, BTN_H);
        btnBack   = new Rectangle(cx - 80,        startY + gap * 3 + 10, 160, 40);
    }

    // =========================================================================
    // Gestion des evenements (appeles par GamePanel)
    // =========================================================================

    /**
     * Traite un clic souris sur cet ecran.
     * Appele par GamePanel UNIQUEMENT quand gameState == DIFFICULTY.
     *
     * @param p Position du clic en coordonnees ecran
     */
    public void handleClick(Point p) {
        if (btnEasy.contains(p))   { selectDifficulty(DifficultyLevel.EASY);   return; }
        if (btnNormal.contains(p)) { selectDifficulty(DifficultyLevel.NORMAL); return; }
        if (btnHard.contains(p))   { selectDifficulty(DifficultyLevel.HARD);   return; }
        if (btnBack.contains(p))   { gp.gameState = GameState.MENU; }
    }

    /**
     * Met a jour le bouton survole.
     * Appele par GamePanel UNIQUEMENT quand gameState == DIFFICULTY.
     *
     * @param p Position de la souris en coordonnees ecran
     */
    public void handleHover(Point p) {
        if      (btnEasy.contains(p))   hoveredButton = 0;
        else if (btnNormal.contains(p)) hoveredButton = 1;
        else if (btnHard.contains(p))   hoveredButton = 2;
        else if (btnBack.contains(p))   hoveredButton = 3;
        else                            hoveredButton = -1;
    }

    // =========================================================================
    // Logique
    // =========================================================================

    /**
     * Enregistre la difficulte choisie et lance la partie.
     *
     * @param level Niveau de difficulte selectionne
     */
    private void selectDifficulty(DifficultyLevel level) {
        GameSettings.getInstance().setDifficulty(level);
        gp.gameState = GameState.PLAYING;
        gp.resetGame();
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine l'ecran de selection de difficulte.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Fond degrade
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30),
                0, gp.screenHeight, new Color(30, 10, 60));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // Titre
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 40f)
                                             : new Font("Arial", Font.BOLD, 40);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "Choisissez votre difficulte";
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, 220);

        // Boutons de difficulte
        drawDifficultyButton(g2, btnEasy,
                "Facile",    "x0.7 HP  |  x0.6 Degats  |  Moins d'ennemis",
                new Color(50, 150, 50),   new Color(80, 210, 80),  0);

        drawDifficultyButton(g2, btnNormal,
                "Normal",    "x1.0 HP  |  x1.0 Degats  |  Ennemis normaux",
                new Color(180, 130, 0),   new Color(255, 190, 0),  1);

        drawDifficultyButton(g2, btnHard,
                "Difficile", "x1.5 HP  |  x1.4 Degats  |  Plus d'ennemis",
                new Color(160, 30, 30),   new Color(230, 60, 60),  2);

        // Bouton Retour
        boolean backHover = (hoveredButton == 3);
        g2.setColor(backHover ? new Color(80, 80, 100) : new Color(40, 40, 60));
        g2.fillRoundRect(btnBack.x, btnBack.y, btnBack.width, btnBack.height, 10, 10);
        g2.setColor(new Color(150, 150, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(btnBack.x, btnBack.y, btnBack.width, btnBack.height, 10, 10);
        g2.setStroke(new BasicStroke(1f));

        Font backFont = gp.gameFont != null ? gp.gameFont.deriveFont(16f)
                                            : new Font("Arial", Font.PLAIN, 16);
        g2.setFont(backFont);
        g2.setColor(Color.white);
        fm = g2.getFontMetrics();
        String back = "<- Retour";
        g2.drawString(back, btnBack.x + btnBack.width / 2 - fm.stringWidth(back) / 2,
                      btnBack.y + btnBack.height / 2 + fm.getAscent() / 2 - 2);
    }

    /**
     * Dessine un bouton de difficulte avec son titre, sa description et son effet hover.
     *
     * @param g2         Contexte graphique
     * @param btn        Rectangle du bouton
     * @param label      Nom de la difficulte
     * @param desc       Description des modificateurs
     * @param colorBase  Couleur de fond normale
     * @param colorHover Couleur de fond au survol
     * @param index      Index du bouton (pour la detection de hover)
     */
    private void drawDifficultyButton(Graphics2D g2, Rectangle btn, String label,
                                      String desc, Color colorBase, Color colorHover, int index) {
        boolean hovered = (hoveredButton == index);

        g2.setColor(hovered ? colorHover : colorBase);
        g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 18, 18);

        g2.setColor(hovered ? Color.white : colorHover);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        // Label
        Font labelFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 22f)
                                             : new Font("Arial", Font.BOLD, 22);
        g2.setFont(labelFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, btn.x + btn.width / 2 - fm.stringWidth(label) / 2, btn.y + 28);

        // Description
        Font descFont = gp.gameFont != null ? gp.gameFont.deriveFont(13f)
                                            : new Font("Arial", Font.PLAIN, 13);
        g2.setFont(descFont);
        g2.setColor(new Color(220, 220, 220));
        fm = g2.getFontMetrics();
        g2.drawString(desc, btn.x + btn.width / 2 - fm.stringWidth(desc) / 2, btn.y + 52);
    }
}
