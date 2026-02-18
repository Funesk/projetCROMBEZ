package projetCROMBEZ;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu pause  afficher par-dessus le jeu quand le joueur appuie sur ÉCHAP.
 *
 * Boutons disponibles :
 *  1. Reprendre   reprend la partie (GameState.PLAYING)
 *  2. Options     ouvre les options (GameState.OPTIONS) avec retour PAUSED
 *  3. Infos       affiche les contrôles et infos de jeu
 *  4. Menu principal  retour au menu (GameState.MENU)
 *  5. Quitter     quitte l'application
 *
 * Le fond du jeu reste visible sous le panneau semi-transparent.
 */
public class PauseScreen {

    // -------------------------------------------------------------------------
    // Références
    // -------------------------------------------------------------------------

    /** Référence au GamePanel. */
    private GamePanel gp;

    // -------------------------------------------------------------------------
    // Boutons
    // -------------------------------------------------------------------------

    /** Labels des boutons dans l'ordre. */
    private String[] labels = { "Reprendre", "Options", "Infos", "Menu principal", "Quitter" };

    /** Zones cliquables des boutons. */
    private List<Rectangle> buttons = new ArrayList<>();

    /** Index du bouton survolé. */
    private int hoveredButton = -1;

    /** true si on affiche l'écran d'infos. */
    private boolean showInfo = false;

    // Dimensions des boutons
    private static final int BTN_W = 240;
    private static final int BTN_H = 48;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée le menu pause et enregistre les listeners souris.
     *
     * @param gp Référence au GamePanel
     */
    public PauseScreen(GamePanel gp) {
        this.gp = gp;

        // Positionnement des boutons (centrés verticalement)
        int cx = gp.screenWidth / 2;
        int startY = gp.screenHeight / 2 - 110;
        int gap = 60;

        for (int i = 0; i < labels.length; i++) {
            buttons.add(new Rectangle(cx - BTN_W / 2, startY + i * gap, BTN_W, BTN_H));
        }

        // Listener souris : survol
        gp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (gp.gameState != GameState.PAUSED) return;
                hoveredButton = -1;
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).contains(e.getPoint())) {
                        hoveredButton = i;
                        break;
                    }
                }
            }
        });

        // Listener souris : clic
        gp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gp.gameState != GameState.PAUSED) return;

                // Si l'écran infos est ouvert, fermer au clic
                if (showInfo) {
                    showInfo = false;
                    return;
                }

                Point p = e.getPoint();
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i).contains(p)) {
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
     * Exécute l'action associée au bouton cliqué.
     *
     * @param index Index du bouton dans le tableau {@link #labels}
     */
    private void handleButton(int index) {
        switch (index) {
            case 0: // Reprendre
                gp.gameState = GameState.PLAYING;
                break;

            case 1: // Options
                gp.optionsScreen.setReturnState(GameState.PAUSED);
                gp.gameState = GameState.OPTIONS;
                break;

            case 2: // Infos
                showInfo = true; // affiche le panneau d'infos par-dessus
                break;

            case 3: // Menu principal
                gp.gameState = GameState.MENU;
                break;

            case 4: // Quitter
                System.exit(0);
                break;
        }
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine le menu pause par-dessus le jeu (fond semi-transparent + boutons).
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // --- Overlay sombre sur le jeu ---
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (showInfo) {
            drawInfoPanel(g2);
            return;
        }

        // --- Panneau central ---
        int panelW = BTN_W + 60;
        int panelH = labels.length * 60 + 80;
        int panelX = gp.screenWidth  / 2 - panelW / 2;
        int panelY = gp.screenHeight / 2 - panelH / 2 - 20;

        // Fond du panneau
        g2.setColor(new Color(15, 15, 35, 230));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setColor(new Color(80, 80, 140));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // --- Titre "PAUSE" ---
        Font titleFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 32f)
                                             : new Font("Arial", Font.BOLD, 32);
        g2.setFont(titleFont);
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "PAUSE";
        g2.drawString(title, gp.screenWidth / 2 - fm.stringWidth(title) / 2, panelY + 45);

        // --- Boutons ---
        Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(btnFont);
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn = buttons.get(i);
            boolean hovered = (i == hoveredButton);

            // Couleur spéciale pour "Quitter" (rouge)
            Color bgNormal  = (i == 4) ? new Color(80, 20, 20)  : new Color(40, 40, 70);
            Color bgHovered = (i == 4) ? new Color(160, 30, 30) : new Color(80, 80, 130);

            // Fond du bouton
            g2.setColor(hovered ? bgHovered : bgNormal);
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 12, 12);

            // Bordure
            g2.setColor(hovered ? Color.white : new Color(80, 80, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 12, 12);
            g2.setStroke(new BasicStroke(1f));

            // Texte centré
            g2.setColor(Color.white);
            int tx = btn.x + btn.width / 2 - fm.stringWidth(labels[i]) / 2;
            int ty = btn.y + btn.height / 2 + fm.getAscent() / 2 - 3;
            g2.drawString(labels[i], tx, ty);
        }

        // Hint ÉCHAP pour reprendre
        Font hintFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                            : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(hintFont);
        g2.setColor(new Color(150, 150, 150));
        String hint = "Appuie sur ÉCHAP pour reprendre";
        fm = g2.getFontMetrics();
        g2.drawString(hint, gp.screenWidth / 2 - fm.stringWidth(hint) / 2,
                      panelY + panelH - 12);
    }

    /**
     * Dessine un panneau d'informations sur les contrôles et mécaniques du jeu.
     * Affiché quand l'utilisateur clique sur le bouton "Infos".
     *
     * @param g2 Contexte graphique
     */
    private void drawInfoPanel(Graphics2D g2) {
        int panelW = 500;
        int panelH = 400;
        int panelX = gp.screenWidth  / 2 - panelW / 2;
        int panelY = gp.screenHeight / 2 - panelH / 2;

        // Fond
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
        String t = "Contrôles & Infos";
        g2.drawString(t, gp.screenWidth / 2 - fm.stringWidth(t) / 2, panelY + 40);

        // Contenu
        Font infoFont = gp.gameFont != null ? gp.gameFont.deriveFont(15f)
                                            : new Font("Arial", Font.PLAIN, 15);
        g2.setFont(infoFont);
        g2.setColor(new Color(200, 200, 230));

        String[] lines = {
            "  Déplacement ",
            "    Z / Flèche haut   Monter ”‚",
            "    S / Flèche bas     Descendre            â”‚",
            "  â”‚  Q / Flèche gauche â†’  Aller à  gauche       â”‚",
            "  â”‚  D / Flèche droite â†’  Aller à  droite       â”‚",
            "",
            "  Mécaniques ",
            "   Attaque   : automatique sur le + proche   ‚",
            "   Vagues    : 4 vagues + 1 boss final      ",
            "    ÉCHAP     : ouvrir / fermer la pause      ",
            "",
            "           Cliquer pour fermer"
        };

        int lineY = panelY + 80;
        for (String line : lines) {
            g2.drawString(line, panelX + 10, lineY);
            lineY += 22;
        }
    }
}
