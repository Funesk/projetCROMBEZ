package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecran du menu principal.
 *
 * Boutons :
 *  - Reprendre    (si hasActiveGame)
 *  - Jouer
 *  - Boutique     -> GameState.SHOP
 *  - Options
 *  - Infos
 *  - Quitter
 */
public class MenuScreen {

    private GamePanel gp;

    private List<Rectangle> buttons      = new ArrayList<>();
    private List<String>    buttonLabels = new ArrayList<>();
    private int hoveredButton = -1;

    private static final int BTN_W = 220, BTN_H = 50, BTN_GAP = 16;

    private InfoPanel infoPanel;
    private boolean   showInfo = false;

    // =========================================================================
    // Bouton reinitialisation (bas droite)
    // =========================================================================

    /** Rectangle du petit bouton "Reinitialiser" en bas a droite. */
    private Rectangle btnReset;

    /**
     * true apres un premier clic sur "Reinitialiser".
     * Affiche "Confirmer ?" et attend un second clic pour executer.
     * Un clic ailleurs annule.
     */
    private boolean confirmReset = false;

    public MenuScreen(GamePanel gp) {
        this.gp        = gp;
        this.infoPanel = new InfoPanel(gp);
    }

    // =========================================================================
    // Construction dynamique
    // =========================================================================

    private void buildButtons() {
        buttons.clear();
        buttonLabels.clear();

        if (gp.hasActiveGame) buttonLabels.add("Reprendre");
        buttonLabels.add("Jouer");
        buttonLabels.add("Boutique");
        buttonLabels.add("Options");
        buttonLabels.add("Infos");
        buttonLabels.add("Quitter");

        int totalH = buttonLabels.size() * (BTN_H + BTN_GAP) - BTN_GAP;
        int btnX   = gp.screenWidth  / 2 - BTN_W / 2;
        int startY = gp.screenHeight / 2 - totalH / 2 + 40;

        for (int i = 0; i < buttonLabels.size(); i++) {
            buttons.add(new Rectangle(btnX, startY + i*(BTN_H+BTN_GAP), BTN_W, BTN_H));
        }

        // Bouton reinitialiser : petit, colle en bas a droite
        btnReset = new Rectangle(gp.screenWidth - 185, gp.screenHeight - 36, 180, 26);
    }

    // =========================================================================
    // Evenements
    // =========================================================================

    public void handleClick(Point p) {
        if (showInfo) { showInfo = false; return; }

        // Gestion du bouton reinitialiser (avec confirmation)
        if (btnReset != null && btnReset.contains(p)) {
            if (confirmReset) {
                // Second clic : execution de la reinitialisation
                SaveManager.resetSave(gp);
                confirmReset = false;
            } else {
                // Premier clic : demande de confirmation
                confirmReset = true;
            }
            return;
        }
        // Clic ailleurs annule la confirmation en cours
        confirmReset = false;

        buildButtons();
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) { handleButton(buttonLabels.get(i)); return; }
        }
    }

    public void handleHover(Point p) {
        if (showInfo) { hoveredButton = -1; return; }
        buildButtons();
        hoveredButton = -1;
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).contains(p)) { hoveredButton = i; return; }
        }
        // Si la souris quitte le bouton reset pendant l'attente de confirmation, on annule
        if (btnReset != null && !btnReset.contains(p)) confirmReset = false;
    }

    private void handleButton(String label) {
        switch (label) {
            case "Reprendre": gp.gameState = GameState.PLAYING;                                    break;
            case "Jouer":     gp.hasActiveGame = false; gp.gameState = GameState.DIFFICULTY;       break;
            case "Boutique":  gp.gameState = GameState.SHOP;                                       break;
            case "Options":   gp.optionsScreen.setReturnState(GameState.MENU);
                              gp.gameState = GameState.OPTIONS;                                    break;
            case "Infos":     showInfo = true;                                                     break;
            case "Quitter":   SaveManager.save(gp); System.exit(0);                               break;
        }
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    public void draw(Graphics2D g2) {
        buildButtons();

        // Fond
        GradientPaint gradient = new GradientPaint(0,0,new Color(10,10,30),
                                                   0,gp.screenHeight,new Color(30,10,60));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setPaint(null);

        // Titre
        Font tf = gp.gameFont!=null ? gp.gameFont.deriveFont(Font.BOLD,52f) : new Font("Arial",Font.BOLD,52);
        g2.setFont(tf);
        FontMetrics fm = g2.getFontMetrics();
        String title = "SURVIVOR";
        int tx = gp.screenWidth/2 - fm.stringWidth(title)/2;
        int ty = buttons.get(0).y - 80;
        g2.setColor(new Color(120,0,0)); g2.drawString(title,tx+3,ty+3);
        g2.setColor(new Color(220,50,50)); g2.drawString(title,tx,ty);

        Font sf = gp.gameFont!=null ? gp.gameFont.deriveFont(18f) : new Font("Arial",Font.PLAIN,18);
        g2.setFont(sf); fm=g2.getFontMetrics();
        String sub="Rogue-lite";
        g2.setColor(new Color(200,150,50));
        g2.drawString(sub, gp.screenWidth/2-fm.stringWidth(sub)/2, ty+34);

        // Boutons
        Font bf = gp.gameFont!=null ? gp.gameFont.deriveFont(20f) : new Font("Arial",Font.PLAIN,20);
        g2.setFont(bf); fm=g2.getFontMetrics();

        for (int i = 0; i < buttons.size(); i++) {
            Rectangle btn  = buttons.get(i);
            String    lbl  = buttonLabels.get(i);
            boolean hov    = (i == hoveredButton);

            // Couleurs par bouton
            Color bgN, bgH, brN, brH;
            switch (lbl) {
                case "Reprendre":
                    bgN=new Color(20,70,30);  bgH=new Color(40,140,60);
                    brN=new Color(60,180,80); brH=new Color(100,220,120); break;
                case "Boutique":
                    bgN=new Color(60,40,0);   bgH=new Color(130,90,0);
                    brN=new Color(180,130,0); brH=new Color(255,200,50);  break;
                default:
                    bgN=new Color(60,20,20);  bgH=new Color(180,40,40);
                    brN=new Color(120,40,40); brH=new Color(255,100,100); break;
            }

            g2.setColor(hov ? bgH : bgN);
            g2.fillRoundRect(btn.x,btn.y,btn.width,btn.height,15,15);
            g2.setColor(hov ? brH : brN);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(btn.x,btn.y,btn.width,btn.height,15,15);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(Color.white);
            g2.drawString(lbl, btn.x+btn.width/2-fm.stringWidth(lbl)/2,
                               btn.y+btn.height/2+fm.getAscent()/2-2);
        }

        // Sous-texte Reprendre
        if (gp.hasActiveGame) {
            Font smf = gp.gameFont!=null?gp.gameFont.deriveFont(12f):new Font("Arial",Font.PLAIN,12);
            g2.setFont(smf); fm=g2.getFontMetrics();
            g2.setColor(new Color(100,200,120));
            String info="Vague en cours - progression conservee";
            Rectangle r0=buttons.get(0);
            g2.drawString(info, gp.screenWidth/2-fm.stringWidth(info)/2, r0.y+r0.height+13);
        }

        // Version
        Font vf=gp.gameFont!=null?gp.gameFont.deriveFont(12f):new Font("Arial",Font.PLAIN,12);
        g2.setFont(vf); g2.setColor(new Color(100,100,100));
        g2.drawString("v0.2 - Alpha", 10, gp.screenHeight-10);

        // Bouton reinitialiser (bas droite)
        if (btnReset != null) {

            // Couleur : orange en attente de confirmation, gris sinon
            Color resetBg  = confirmReset ? new Color(140, 60,  0) : new Color(35, 20, 20);
            Color resetBrd = confirmReset ? new Color(255,140, 50) : new Color(80, 40, 40);
            g2.setColor(resetBg);
            g2.fillRoundRect(btnReset.x, btnReset.y, btnReset.width, btnReset.height, 8, 8);
            g2.setColor(resetBrd);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(btnReset.x, btnReset.y, btnReset.width, btnReset.height, 8, 8);
            g2.setStroke(new BasicStroke(1f));

            Font rf = gp.gameFont!=null ? gp.gameFont.deriveFont(12f) : new Font("Arial",Font.PLAIN,12);
            g2.setFont(rf);
            g2.setColor(confirmReset ? new Color(255,180,80) : new Color(120,70,70));
            FontMetrics fm2 = g2.getFontMetrics();
            String rlbl = confirmReset ? "Confirmer reinitialisation ?" : "Reinitialiser la sauvegarde";
            g2.drawString(rlbl, btnReset.x + btnReset.width/2 - fm2.stringWidth(rlbl)/2,
                          btnReset.y + btnReset.height/2 + fm2.getAscent()/2 - 2);
        }

        if (showInfo) infoPanel.draw(g2);
    }
}
