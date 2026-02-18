package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * à‰cran du menu principal.
 *
 * Premier écran affiché au lancement du jeu.
 * Boutons disponibles :
 *  1. Jouer          ouvre l'écran de sélection de difficulté
 *  2. Options        ouvre l'écran des options (retour au menu)
 *  3. Crédits        (TODO : à  implémenter)
 *  4. Quitter        ferme l'application
 *
 */
public class MenuScreen {

    // -------------------------------------------------------------------------
    // Références
    // -------------------------------------------------------------------------

    /** Référence au GamePanel pour la navigation et les dimensions. */
    private GamePanel gp;

    // -------------------------------------------------------------------------
    // Boutons
    // -------------------------------------------------------------------------

    /** Liste des rectangles cliquables dans l'ordre des boutons. */
    private List<Rectangle> buttons = new ArrayList<>();

    /** Labels affichés sur chaque bouton. */
    private String[] buttonLabels = { "Jouer", "Options", "Crédits", "Quitter" };

    /** Index du bouton actuellement survolé (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons
    private static final int BTN_W = 220;
    private static final int BTN_H = 50;
    private static final int BTN_GAP = 20;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée le menu et enregistre les listeners souris pour le hover et le clic.
     *
     * @param gp Référence au GamePanel
     */
    public MenuScreen(GamePanel gp) {
        this.gp = gp;

        // Calcul des positions de chaque bouton (centrés horizontalement)
        int btnX  = gp.screenWidth / 2 - BTN_W / 2;
        int startY = gp.screenHeight / 2 - 60;

        for (int i = 0; i < buttonLabels.length; i++) {
            buttons.add(new Rectangle(btnX, startY + i * (BTN_H + BTN_GAP), BTN_W, BTN_H));
        }

        // Listener de survol (effet visuel hover)
        gp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Ne traite le hover que si on est bien sur l'écran menu
                if (gp.gameState != GameState.MENU) return;

                hoveredButton = -1;
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).contains(e.getPoint())) {
                        hoveredButton = i;
                        break;
                    }
                }
            }
        });

        // Listener de clic
        gp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gp.gameState != GameState.MENU) return;

                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).contains(e.getPoint())) {
                        handleButton(i);
                        break;
                    }
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Actions des boutons
    // -------------------------------------------------------------------------

    /**
     * Exécute l'action correspondant au bouton cliqué.
     *
     * @param index Position du bouton dans {@link #buttonLabels}
     */
    private void handleButton(int index) {
        switch (index) {
            case 0: // Jouer  passe par la sélection de difficulté
                gp.gameState = GameState.DIFFICULTY;
                break;

            case 1: // Options  ouvre les options avec retour au menu
                gp.optionsScreen.setReturnState(GameState.MENU);
                gp.gameState = GameState.OPTIONS;
                break;

            case 2: // Crédits (à  implémenter)
                // TODO : créer un écran de crédits
                break;

            case 3: // Quitter
                System.exit(0);
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine le menu principal : fond dégradé, titre du jeu et boutons.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // --- Fond dégradé sombre ---
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(10, 10, 30),
                0, gp.screenHeight, new Color(30, 10, 60)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // --- Titre principal ---
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 52f)
                                             : new Font("Arial", Font.BOLD, 52);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();

        String title = "SURVIVOR";
        int titleX = gp.screenWidth / 2 - fm.stringWidth(title) / 2;
        int titleY = gp.screenHeight / 2 - 170;

        // Ombre portée (décalée de 3px)
        g2.setColor(new Color(120, 0, 0));
        g2.drawString(title, titleX + 3, titleY + 3);
        // Titre principal
        g2.setColor(new Color(220, 50, 50));
        g2.drawString(title, titleX, titleY);

        // --- Sous-titre ---
        Font subFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(subFont);
        fm = g2.getFontMetrics();
        String sub = "Rogue-lite";
        g2.setColor(new Color(200, 150, 50));
        g2.drawString(sub, gp.screenWidth / 2 - fm.stringWidth(sub) / 2, titleY + 36);

        // --- Boutons ---
        Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(20f)
                                           : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(btnFont);
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn    = buttons.get(i);
            boolean  hovered = (i == hoveredButton);

            // Fond du bouton (plus clair au survol)
            g2.setColor(hovered ? new Color(180, 40, 40) : new Color(60, 20, 20));
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            // Bordure du bouton
            g2.setColor(hovered ? new Color(255, 100, 100) : new Color(120, 40, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);
            g2.setStroke(new BasicStroke(1f));

            // Texte centré dans le bouton
            g2.setColor(Color.white);
            int textX = btn.x + btn.width  / 2 - fm.stringWidth(buttonLabels[i]) / 2;
            int textY = btn.y + btn.height / 2 + fm.getAscent() / 2 - 2;
            g2.drawString(buttonLabels[i], textX, textY);
        }

        // --- Version en bas à  gauche ---
        Font versionFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                               : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(versionFont);
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("v0.2 - Alpha", 10, gp.screenHeight - 10);
    }
}
