package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ecran de sélection de la difficulté.
 *
 * Affiché quand le joueur clique sur "Jouer" depuis le menu principal.
 * Présente trois boutons (Facile / Normal / Difficile) avec une description
 * des modificateurs appliquÃé, et un bouton "Retour".
 *
 * Validé via clic souris. La difficulté choisie est enregistrée dans
 * {@link GameSettings} avant de dÃ©marrer la partie.
 */
public class DifficultyScreen {

    // -------------------------------------------------------------------------
    // RÃ©fÃ©rences
    // -------------------------------------------------------------------------

    /** Référence au GamePanel pour changer l'état et accéder aux dimensions. */
    private GamePanel gp;

    // -------------------------------------------------------------------------
    // Boutons
    // -------------------------------------------------------------------------

    /** Zone cliquable pour le niveau Facile. */
    private Rectangle btnEasy;

    /** Zone cliquable pour le niveau Normal. */
    private Rectangle btnNormal;

    /** Zone cliquable pour le niveau Difficile. */
    private Rectangle btnHard;

    /** Zone cliquable pour revenir au menu principal. */
    private Rectangle btnBack;

    /** Index du bouton actuellement survolé (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons de difficulté
    private static final int BTN_W = 300;
    private static final int BTN_H = 70;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée l'écran de difficulté et enregistre les listeners souris.
     *
     * @param gp Référence au GamePanel
     */
    public DifficultyScreen(GamePanel gp) {
        this.gp = gp;

        // Calcul des positions des boutons (centrÃ©s horizontalement)
        int cx = gp.screenWidth / 2;
        int startY = 300;
        int gap = 90;

        btnEasy   = new Rectangle(cx - BTN_W / 2, startY,           BTN_W, BTN_H);
        btnNormal = new Rectangle(cx - BTN_W / 2, startY + gap,     BTN_W, BTN_H);
        btnHard   = new Rectangle(cx - BTN_W / 2, startY + gap * 2, BTN_W, BTN_H);
        btnBack   = new Rectangle(cx - 80,        startY + gap * 3 + 10, 160, 40);

        // Listener de survol
        gp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getPoint());
            }
        });

        // Listener de clic
        gp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gp.gameState != GameState.DIFFICULTY) return;

                Point p = e.getPoint();
                if (btnEasy.contains(p))   selectDifficulty(DifficultyLevel.EASY);
                if (btnNormal.contains(p)) selectDifficulty(DifficultyLevel.NORMAL);
                if (btnHard.contains(p))   selectDifficulty(DifficultyLevel.HARD);
                if (btnBack.contains(p))   gp.gameState = GameState.MENU;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Logique
    // -------------------------------------------------------------------------

    /**
     * Enregistre la difficulté choisie et lance la partie.
     *
     * @param level Niveau de difficulté sélectionné
     */
    private void selectDifficulty(DifficultyLevel level) {
        GameSettings.getInstance().setDifficulty(level);
        gp.gameState = GameState.PLAYING;
        gp.resetGame();
    }

    /**
     * Met à  jour l'index du bouton survolé en fonction de la position de la souris.
     * Utilisé pour l'effet de hover.
     *
     * @param p Position de la souris
     */
    private void updateHover(Point p) {
        if (gp.gameState != GameState.DIFFICULTY) return;
        if      (btnEasy.contains(p))   hoveredButton = 0;
        else if (btnNormal.contains(p)) hoveredButton = 1;
        else if (btnHard.contains(p))   hoveredButton = 2;
        else if (btnBack.contains(p))   hoveredButton = 3;
        else                            hoveredButton = -1;
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine l'écran de sélection de difficulté.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // --- Fond ---
        GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 10, 30),
                0, gp.screenHeight, new Color(30, 10, 60));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // --- Titre ---
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 40f)
                                             : new Font("Arial", Font.BOLD, 40);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        String title = "Choisissez votre difficulté";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, 220);

        // --- Bouton Facile ---
        drawDifficultyButton(g2, btnEasy, "Facile",
                "0.7 HP | 0.6 Dégats | Moins d'ennemis",
                new Color(50, 150, 50), new Color(80, 210, 80), 0);

        // --- Bouton Normal ---
        drawDifficultyButton(g2, btnNormal, "Normal",
                " 1.0 HP | 1.0 Dégats | Ennemis normaux",
                new Color(180, 130, 0), new Color(255, 190, 0), 1);

        // --- Bouton Difficile ---
        drawDifficultyButton(g2, btnHard, "Difficile",
                " 1.5 HP | 1.4 Dégats | Plus d'ennemis",
                new Color(160, 30, 30), new Color(230, 60, 60), 2);

        // --- Bouton Retour ---
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
        String back = " Retour";
        g2.drawString(back, btnBack.x + btnBack.width / 2 - fm.stringWidth(back) / 2,
                      btnBack.y + btnBack.height / 2 + fm.getAscent() / 2 - 2);
    }

    /**
     * Dessine un bouton de difficultÃ© avec son titre, sa description et ses effets visuels.
     *
     * @param g2          Contexte graphique
     * @param btn         Rectangle du bouton
     * @param label       Nom de la difficultÃ©
     * @param desc        Description des modificateurs
     * @param colorBase   Couleur de fond normale
     * @param colorHover  Couleur de fond au survol
     * @param index       Index du bouton (pour la dÃ©tection de hover)
     */
    private void drawDifficultyButton(Graphics2D g2, Rectangle btn, String label,
                                      String desc, Color colorBase, Color colorHover, int index) {
        boolean hovered = (hoveredButton == index);

        // Fond
        g2.setColor(hovered ? colorHover : colorBase);
        g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 18, 18);

        // Contour
        g2.setColor(hovered ? Color.white : colorHover);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        // Label de difficultÃ©
        Font labelFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 22f)
                                             : new Font("Arial", Font.BOLD, 22);
        g2.setFont(labelFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, btn.x + btn.width / 2 - fm.stringWidth(label) / 2,
                      btn.y + 28);

        // Description des modificateurs
        Font descFont = gp.gameFont != null ? gp.gameFont.deriveFont(13f)
                                            : new Font("Arial", Font.PLAIN, 13);
        g2.setFont(descFont);
        g2.setColor(new Color(220, 220, 220));
        fm = g2.getFontMetrics();
        g2.drawString(desc, btn.x + btn.width / 2 - fm.stringWidth(desc) / 2,
                      btn.y + 52);
    }
}
