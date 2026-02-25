package projetCROMBEZ;

import java.awt.*;

/**
 * Panneau d'informations detaillees.
 * Affiche les stats EFFECTIVES du joueur (apres upgrades) en temps reel.
 */
public class InfoPanel {

    private GamePanel gp;

    private static final Color COLOR_MELEE  = new Color(220,  80,  80);
    private static final Color COLOR_RANGED = new Color(  0, 180, 220);
    private static final Color COLOR_TANK   = new Color( 80, 200, 100);
    private static final Color COLOR_BOSS   = new Color(255, 140,   0);

    public InfoPanel(GamePanel gp) { this.gp = gp; }

    // =========================================================================
    // Rendu principal
    // =========================================================================

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        int pw = 900, ph = 600;
        int px = gp.screenWidth/2 - pw/2, py = gp.screenHeight/2 - ph/2;

        g2.setColor(new Color(12, 12, 32, 252));
        g2.fillRoundRect(px, py, pw, ph, 20, 20);
        g2.setColor(new Color(90, 90, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(px, py, pw, ph, 20, 20);
        g2.setStroke(new BasicStroke(1f));

        // Titre
        Font tf = gp.gameFont!=null?gp.gameFont.deriveFont(Font.BOLD,26f):new Font("Arial",Font.BOLD,26);
        g2.setFont(tf); g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String title = "Informations & Statistiques";
        g2.drawString(title, gp.screenWidth/2-fm.stringWidth(title)/2, py+38);

        g2.setColor(new Color(70,70,120));
        g2.drawLine(px+20, py+48, px+pw-20, py+48);

        drawLeftColumn(g2,  px+28,            py+62);
        drawRightColumn(g2, px+pw/2+10, py+62);

        Font hf = gp.gameFont!=null?gp.gameFont.deriveFont(13f):new Font("Arial",Font.PLAIN,13);
        g2.setFont(hf); g2.setColor(new Color(140,140,160)); fm=g2.getFontMetrics();
        String hint="Cliquer n'importe ou pour fermer";
        g2.drawString(hint, gp.screenWidth/2-fm.stringWidth(hint)/2, py+ph-12);
    }

    // =========================================================================
    // Colonne gauche : stats joueur + controles
    // =========================================================================

    private void drawLeftColumn(Graphics2D g2, int x, int y) {
        int lh = 21;
        Player p = gp.player;

        drawSectionTitle(g2, "Joueur (stats actuelles)", new Color(80,200,120), x, y);
        y += 26;

        // Stats effectives (refletent les upgrades en cours)
        String[][] stats = {
            { "Points de vie",          p.maxHp + " HP" },
            { "Degats / tir",           p.damage + " dgts" },
            { "Portee d'attaque",       p.attackRange + " px" },
            { "Cadence de tir",         String.format("%.1f tirs/sec", 60.0/p.attackRate) },
            { "Vol de vie",             p.lifeStealPct + " %" },
            { "Chance critique",        p.critChancePct + " %" },
            { "Multiplicateur crit",    String.format("x%.2f", p.critMultiplier) },
            { "Vitesse deplacement",    "5 px/frame" },
            { "Invincibilite post-hit", "1 sec" },
        };

        for (String[] row : stats) {
            drawStatRow(g2, x+16, y, row[0], row[1], new Color(180,240,180));
            y += lh;
        }

        // Note upgrades
        Font nf = gp.gameFont!=null?gp.gameFont.deriveFont(11f):new Font("Arial",Font.PLAIN,11);
        g2.setFont(nf); g2.setColor(new Color(160,160,80));
        g2.drawString("* Stats ameliorees via la Boutique", x+16, y+8);

        y += 28;
        drawSectionTitle(g2, "Controles", new Color(180,180,255), x, y);
        y += 26;

        String[][] controls = {
            {"Z / Fleche haut",   "Monter"},
            {"S / Fleche bas",    "Descendre"},
            {"Q / Fleche gauche", "Gauche"},
            {"D / Fleche droite", "Droite"},
            {"ECHAP",             "Pause"},
            {"ENTREE",            "Confirmer"},
        };
        for (String[] row : controls) {
            drawStatRow(g2, x+16, y, row[0], row[1], new Color(200,200,255));
            y += lh;
        }
    }

    // =========================================================================
    // Colonne droite : ennemis + vagues
    // =========================================================================

    private void drawRightColumn(Graphics2D g2, int x, int y) {
        int lh = 21;

        drawSectionTitle(g2, "Ennemis (stats de base)", new Color(220,120,80), x, y);
        y += 26;

        drawTableHeader(g2, x, y);
        y += lh + 2;
        g2.setColor(new Color(60,60,90));
        g2.drawLine(x, y-4, x+400, y-4);

        drawEnemyRow(g2, x, y, "Melee",   "Triangle",  "40",   "10",  "2.0", "1/s",    COLOR_MELEE);  y+=lh;
        drawEnemyRow(g2, x, y, "Distance","Losange",   "25",    "8",  "1.5", "0.5/s",  COLOR_RANGED); y+=lh;
        drawEnemyRow(g2, x, y, "Tank",    "Hexagone", "200",   "20",  "0.8", "0.7/s",  COLOR_TANK);   y+=lh;
        drawEnemyRow(g2, x, y, "Boss",    "Etoile",  "1000",   "25",  "1.2", "1/s*",   COLOR_BOSS);   y+=lh+2;

        Font nf = gp.gameFont!=null?gp.gameFont.deriveFont(11f):new Font("Arial",Font.PLAIN,11);
        g2.setFont(nf); g2.setColor(new Color(160,160,160));
        g2.drawString("* Boss : tirs radiaux (phase 2 : x8)", x, y); y+=15;
        g2.setColor(new Color(255,200,80));
        g2.drawString("La difficulte multiplie HP et degats ennemis", x, y); y+=22;

        drawSectionTitle(g2, "Vagues (Normal)", new Color(200,180,80), x, y);
        y += 26;

        String[][] waves = {
            {"Vague 1", "11", "100% Melee"},
            {"Vague 2", "14", "70% Melee  +  30% Distance"},
            {"Vague 3", "17", "70% Melee  +  30% Distance"},
            {"Vague 4", "20", "45% Melee + 30% Distance + 25% Tank"},
            {"Vague 5", "23", "45% Melee + 30% Distance + 25% Tank + BOSS"},
        };
        for (String[] w : waves) {
            drawWaveRow(g2, x, y, w[0], w[1], w[2]);
            y += lh;
        }
        y += 6;
        g2.setFont(nf); g2.setColor(new Color(160,160,160));
        g2.drawString("Facile: x0.6 ennemis  |  Difficile: x1.5 ennemis", x, y);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void drawSectionTitle(Graphics2D g2, String text, Color color, int x, int y) {
        g2.setColor(color);
        g2.fillRect(x, y-12, 3, 16);
        Font f = gp.gameFont!=null?gp.gameFont.deriveFont(Font.BOLD,15f):new Font("Arial",Font.BOLD,15);
        g2.setFont(f); g2.drawString(text, x+8, y);
    }

    private void drawStatRow(Graphics2D g2, int x, int y, String key, String val, Color vc) {
        Font f = gp.gameFont!=null?gp.gameFont.deriveFont(13f):new Font("Arial",Font.PLAIN,13);
        g2.setFont(f);
        g2.setColor(new Color(160,160,180)); g2.drawString(key, x, y);
        g2.setColor(vc);                     g2.drawString(val, x+195, y);
    }

    private void drawTableHeader(Graphics2D g2, int x, int y) {
        Font f = gp.gameFont!=null?gp.gameFont.deriveFont(Font.BOLD,12f):new Font("Arial",Font.BOLD,12);
        g2.setFont(f); g2.setColor(new Color(180,180,200));
        g2.drawString("Nom",     x,       y);
        g2.drawString("Forme",   x+80,    y);
        g2.drawString("HP",      x+165,   y);
        g2.drawString("Dgts",    x+210,   y);
        g2.drawString("Vitesse", x+260,   y);
        g2.drawString("Atk/s",   x+340,   y);
    }

    private void drawEnemyRow(Graphics2D g2, int x, int y,
                               String name, String shape, String hp,
                               String dmg, String speed, String rate, Color c) {
        Font f = gp.gameFont!=null?gp.gameFont.deriveFont(13f):new Font("Arial",Font.PLAIN,13);
        g2.setFont(f);
        g2.setColor(c); g2.fillOval(x, y-9, 8, 8);
        g2.drawString(name,  x+12,  y);
        g2.setColor(new Color(210,210,210));
        g2.drawString(shape, x+80,  y);
        g2.drawString(hp,    x+165, y);
        g2.drawString(dmg,   x+210, y);
        g2.drawString(speed, x+260, y);
        g2.drawString(rate,  x+340, y);
    }

    private void drawWaveRow(Graphics2D g2, int x, int y,
                              String wave, String count, String comp) {
        Font f = gp.gameFont!=null?gp.gameFont.deriveFont(13f):new Font("Arial",Font.PLAIN,13);
        g2.setFont(f);
        g2.setColor(new Color(255,210,80)); g2.drawString(wave,        x,      y);
        g2.setColor(Color.white);           g2.drawString(count+" ennemis", x+68, y);
        g2.setColor(new Color(180,180,200));g2.drawString(comp,         x+160,  y);
    }
}
