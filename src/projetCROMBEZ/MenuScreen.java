package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecran du menu principal.
 *
 * Boutons disponibles :
 *  - Reprendre    -> visible UNIQUEMENT si une partie est en pause (gp.hasActiveGame)
 *                    reprend directement la partie sans passer par la difficulte
 *  - Jouer        -> ouvre l'ecran de selection de difficulte (nouvelle partie)
 *  - Options      -> ouvre l'ecran des options (retour MENU)
 *  - Infos        -> affiche le panneau d'informations et statistiques
 *  - Quitter      -> ferme l'application
 *
 * La liste des boutons est recalculee a chaque draw() pour s'adapter
 * dynamiquement a la presence ou non du bouton "Reprendre".
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

    /**
     * Zones cliquables des boutons.
     * Reconstruites a chaque frame via buildButtons() selon hasActiveGame.
     */
    private List<Rectangle> buttons = new ArrayList<>();

    /**
     * Labels correspondant aux boutons construits.
     * Synchronises avec la liste buttons.
     */
    private List<String> buttonLabels = new ArrayList<>();

    /** Index du bouton survole (-1 = aucun). */
    private int hoveredButton = -1;

    // Dimensions des boutons
    private static final int BTN_W   = 220;
    private static final int BTN_H   = 50;
    private static final int BTN_GAP = 20;

    // =========================================================================
    // Panneau Infos
    // =========================================================================

    /** Panneau d'informations partage avec PauseScreen. */
    private InfoPanel infoPanel;

    /** true quand le panneau d'informations est affiche. */
    private boolean showInfo = false;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Cree le menu.
     * Les boutons sont construits dynamiquement dans buildButtons().
     *
     * @param gp Reference au GamePanel
     */
    public MenuScreen(GamePanel gp) {
        this.gp        = gp;
        this.infoPanel = new InfoPanel(gp);
    }

    // =========================================================================
    // Construction dynamique des boutons
    // =========================================================================

    /**
     * Reconstruit la liste des boutons selon l'etat courant.
     * Appele au debut de draw() et handleClick()/handleHover().
     *
     * Si gp.hasActiveGame est true, le bouton "Reprendre" est insere en premier.
     * Sinon la liste commence directement par "Jouer".
     */
    private void buildButtons() {
        buttons.clear();
        buttonLabels.clear();

        // Bouton "Reprendre" uniquement si une partie est en cours
        if (gp.hasActiveGame) {
            buttonLabels.add("Reprendre");
        }
        buttonLabels.add("Jouer");
        buttonLabels.add("Options");
        buttonLabels.add("Infos");
        buttonLabels.add("Quitter");

        // Centrage vertical de la liste
        int totalH = buttonLabels.size() * (BTN_H + BTN_GAP) - BTN_GAP;
        int btnX   = gp.screenWidth  / 2 - BTN_W / 2;
        int startY = gp.screenHeight / 2 - totalH / 2 + 40; // +40 pour laisser de la place au titre

        for (int i = 0; i < buttonLabels.size(); i++) {
            buttons.add(new Rectangle(btnX, startY + i * (BTN_H + BTN_GAP), BTN_W, BTN_H));
        }
    }

    // =========================================================================
    // Gestion des evenements (appeles par GamePanel)
    // =========================================================================

    /**
     * Traite un clic.
     * Reconstruit la liste des boutons avant de tester les collisions
     * pour etre synchronise avec le dernier draw().
     *
     * @param p Position du clic
     */
    public void handleClick(Point p) {
        if (showInfo) {
            showInfo = false;
            return;
        }
        buildButtons(); // sync avec draw()
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) {
                handleButton(buttonLabels.get(i));
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
        buildButtons();
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
     * Execute l'action correspondant au bouton clique, identifie par son label.
     * Utiliser le label (et non l'index) garantit la coherence meme quand
     * la liste varie selon hasActiveGame.
     *
     * @param label Label du bouton clique
     */
    private void handleButton(String label) {
        switch (label) {
            case "Reprendre":
                // Reprend la partie en pause sans reinitialiser
                gp.gameState = GameState.PLAYING;
                break;

            case "Jouer":
                // Nouvelle partie : passe par la selection de difficulte
                gp.hasActiveGame = false; // efface la partie precedente
                gp.gameState = GameState.DIFFICULTY;
                break;

            case "Options":
                gp.optionsScreen.setReturnState(GameState.MENU);
                gp.gameState = GameState.OPTIONS;
                break;

            case "Infos":
                showInfo = true;
                break;

            case "Quitter":
                SaveManager.save(gp);
                System.exit(0);
                break;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    /**
     * Dessine le menu principal.
     * Reconstruit les boutons a chaque frame pour reflechir hasActiveGame.
     *
     * @param g2 Contexte graphique
     */
    public void draw(Graphics2D g2) {
        // Reconstruction des boutons (adapte au contexte courant)
        buildButtons();

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
        int    titleY = buttons.get(0).y - 80; // au-dessus du premier bouton

        g2.setColor(new Color(120, 0, 0));
        g2.drawString(title, titleX + 3, titleY + 3);
        g2.setColor(new Color(220, 50, 50));
        g2.drawString(title, titleX, titleY);

        // Sous-titre
        Font subFont = gp.gameFont != null ? gp.gameFont.deriveFont(18f)
                                           : new Font("Arial", Font.PLAIN, 18);
        g2.setFont(subFont);
        fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 150, 50));
        String sub = "Rogue-lite";
        g2.drawString(sub, gp.screenWidth / 2 - fm.stringWidth(sub) / 2, titleY + 34);

        // Boutons
        Font btnFont = gp.gameFont != null ? gp.gameFont.deriveFont(20f)
                                           : new Font("Arial", Font.PLAIN, 20);
        g2.setFont(btnFont);
        fm = g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn     = buttons.get(i);
            String    lbl     = buttonLabels.get(i);
            boolean   hovered = (i == hoveredButton);

            // "Reprendre" : couleur verte pour le distinguer de "Jouer"
            boolean isResume = lbl.equals("Reprendre");
            Color bgNormal  = isResume ? new Color(20, 70, 30)  : new Color(60, 20, 20);
            Color bgHovered = isResume ? new Color(40, 140, 60) : new Color(180, 40, 40);
            Color border    = isResume ? new Color(60, 180, 80) : new Color(120, 40, 40);
            Color borderHov = isResume ? new Color(100, 220, 120) : new Color(255, 100, 100);

            g2.setColor(hovered ? bgHovered : bgNormal);
            g2.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            g2.setColor(hovered ? borderHov : border);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(Color.white);
            int textX = btn.x + btn.width  / 2 - fm.stringWidth(lbl) / 2;
            int textY = btn.y + btn.height / 2 + fm.getAscent() / 2 - 2;
            g2.drawString(lbl, textX, textY);
        }

        // Indication "partie en cours" sous le bouton Reprendre
        if (gp.hasActiveGame) {
            Font subBtnFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                                  : new Font("Arial", Font.PLAIN, 12);
            g2.setFont(subBtnFont);
            g2.setColor(new Color(100, 200, 120));
            Rectangle resumeBtn = buttons.get(0);
            fm = g2.getFontMetrics();
            String info = "Vague en cours - progression conservee";
            g2.drawString(info,
                    gp.screenWidth / 2 - fm.stringWidth(info) / 2,
                    resumeBtn.y + resumeBtn.height + 14);
        }

        // Version
        Font versionFont = gp.gameFont != null ? gp.gameFont.deriveFont(12f)
                                               : new Font("Arial", Font.PLAIN, 12);
        g2.setFont(versionFont);
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("v0.2 - Alpha", 10, gp.screenHeight - 10);

        // Panneau infos par-dessus si actif
        if (showInfo) {
            infoPanel.draw(g2);
        }
    }
}
