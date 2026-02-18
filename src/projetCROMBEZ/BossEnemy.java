package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Boss final  ennemi unique avec plusieurs patterns d'attaque et deux phases.
 *
 * 
 * 
 *
 *  PHASE 1 (HP > 50%) 
 *  - Se déplace vers le joueur.
 *  - Tire 4 projectiles en croix toutes les 2 secondes.
 *  - Attaque au contact.
 *
 * PHASE 2 (HP â‰¤ 50%) 
 *  - Tire 8 projectiles radiaux toutes les 1.3 secondes.
 *  - Effectue des charges rapides vers le joueur.
 *  - Attaque de charge : dégats x2.
 *
 * Forme : étoile à  8 branches orange 
 * Barre de vie : grande barre affichée en bas de l'écran.
 */
public class BossEnemy extends Enemy {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    /** Durée d'une charge en frames. */
    private static final int CHARGE_DURATION = 25;

    // -------------------------------------------------------------------------
    // état de combat
    // -------------------------------------------------------------------------

    /** Cooldown de l'attaque au contact (frames). */
    private int attackCooldown = 0;

    /** Cooldown avant le prochain tir radial (frames). */
    private int shootCooldown = 60;

    /** Phase actuelle (1 ou 2). Passe à  2 sous 50% de HP. */
    private int phase = 1;

    /** true pendant l'animation de charge. */
    private boolean charging = false;

    /** Composante X de la vitesse de charge. */
    private double chargeDX;

    /** Composante Y de la vitesse de charge. */
    private double chargeDY;

    /** Frames restants pour la charge en cours. */
    private int chargeTimer = 0;

    /** Cooldown entre deux charges (évite les charges en boucle). */
    private int chargeCooldown = 0;

    // -------------------------------------------------------------------------
    // Rotation cosmétique
    // -------------------------------------------------------------------------

    /** Angle de rotation de l'étoile (tourne lentement). */
    private double rotationAngle = 0;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée le boss à  la position donnée.
     *
     * @param spawnX Position de spawn X
     * @param spawnY Position de spawn Y
     */
    public BossEnemy(double spawnX, double spawnY) {
        // HP=1000 | damage=25 | speed=1.2 | size=70 | orange foncé
        super(spawnX, spawnY, 1000, 25, 1.2, 70, new Color(200, 100, 0));
        this.hpBarColor = new Color(255, 140, 0);
    }

    // -------------------------------------------------------------------------
    // IA
    // -------------------------------------------------------------------------

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        // --- Changement de phase ---
        if (hp <= maxHp / 2 && phase == 1) {
            phase = 2;
            // Accélèrere légèrement en phase 2
            speed = 1.8;
        }

        // Rotation cosmétique de l'étoile
        rotationAngle += 0.03;

        // --- Gestion de la charge ---
        if (charging) {
            x += chargeDX;
            y += chargeDY;
            chargeTimer--;

            // Frappe pendant la charge (double dégats)
            if (getBounds().intersects(player.getBounds())) {
                player.takeDamage(damage * 2);
                charging = false; // stoppe la charge au contact
            }

            if (chargeTimer <= 0) charging = false;

        } else {
            // Déplacement normal vers le joueur
            moveToward(player.x, player.y);
        }

        // --- Décrémente cooldowns ---
        if (attackCooldown  > 0) attackCooldown--;
        if (shootCooldown   > 0) shootCooldown--;
        if (chargeCooldown  > 0) chargeCooldown--;

        // --- Attaque de mélée ---
        if (getBounds().intersects(player.getBounds()) && attackCooldown <= 0) {
            player.takeDamage(damage);
            attackCooldown = 60;
        }

        // --- Tir radial ---
        if (shootCooldown <= 0) {
            int numShots = (phase == 1) ? 4 : 8;
            shootRadial(projectiles, numShots);
            shootCooldown = (phase == 1) ? 120 : 80; // plus rapide en phase 2
        }

        // --- Charge (phase 2 uniquement) ---
        if (phase == 2 && !charging && chargeCooldown <= 0) {
            triggerCharge(player);
        }
    }

    /**
     * Tire {@code count} projectiles répartis uniformément en cercle.
     *
     * @param projectiles Liste dans laquelle ajouter les projectiles
     * @param count       Nombre de projectiles à  tirer simultanément
     */
    private void shootRadial(List<Projectile> projectiles, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI / count * i;
            double tx = x + Math.cos(angle) * 200;
            double ty = y + Math.sin(angle) * 200;
            projectiles.add(new Projectile(x, y, tx, ty, 12, false));
        }
    }

    /**
     * Lance une charge rapide en direction du joueur.
     * Réinitialise un cooldown pour éviter les charges en rafale.
     *
     * @param player Référence au joueur pour calculer la direction
     */
    private void triggerCharge(Player player) {
        double dist = Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));
        if (dist > 0) {
            chargeDX = (player.x - x) / dist * 9; // vitesse de charge
            chargeDY = (player.y - y) / dist * 9;
        }
        charging = true;
        chargeTimer = CHARGE_DURATION;
        chargeCooldown = 200; // attend ~3.3s avant prochaine charge
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    @Override
    public void draw(Graphics2D g2) {
        // --- Aura pulsante autour du boss ---
        float auraAlpha = (phase == 2) ? 100 : 60;
        Color auraColor = (phase == 2) ? new Color(255, 80, 0, (int)auraAlpha)
                                       : new Color(200, 100, 0, (int)auraAlpha);
        g2.setColor(auraColor);
        int auraSize = size + 20 + (int)(Math.sin(rotationAngle * 3) * 5); // légà¨re pulsation
        g2.fillOval((int)x - auraSize / 2, (int)y - auraSize / 2, auraSize, auraSize);

        // --- étoile à  8 branches ---
        Polygon star = buildStar8((int)x, (int)y, size / 2, size / 4, rotationAngle);

        // Corps de l'étoile (flash blanc si hit, orange si charge)
        Color bodyColor = charging ? new Color(255, 200, 0) : color;
        g2.setColor(getDrawColor(bodyColor));
        g2.fillPolygon(star);

        // Contour sombre
        g2.setColor(new Color(100, 40, 0));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawPolygon(star);
        g2.setStroke(new BasicStroke(1f));

        // oeil central (rouge en phase 2)
        int eyeSize = 16;
        g2.setColor(phase == 2 ? new Color(255, 0, 0) : new Color(255, 200, 100));
        g2.fillOval((int)x - eyeSize/2, (int)y - eyeSize/2, eyeSize, eyeSize);
        g2.setColor(Color.black);
        g2.fillOval((int)x - 5, (int)y - 5, 10, 10); // pupille

        // Label "BOSS" au-dessus
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(phase == 2 ? new Color(255, 80, 0) : Color.orange);
        FontMetrics fm = g2.getFontMetrics();
        String label = "BOSS" + (phase == 2 ? " PHASE 2" : "");
        g2.drawString(label, (int)x - fm.stringWidth(label) / 2, (int)y - size / 2 - 14);

        // --- Barre de vie spéciale en bas de l'écran ---
        drawHpBar(g2);
    }

    /**
     * Dessine une grande barre de vie du boss en bas de l'écran (au lieu d'au-dessus).
     * Plus lisible pour un boss qui occupe beaucoup d'espace.
     */
    @Override
    protected void drawHpBar(Graphics2D g2) {
        int barWidth  = 400;
        int barHeight = 22;
        int barX = 408; // centré horizontalement pour un écran de 1216px
        int barY = 790; // proche du bas

        // Fond
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Vie restante avec dégradé orangeâ†rerouge
        int currentWidth = (int) (barWidth * ((double) hp / maxHp));
        GradientPaint grad = new GradientPaint(
                barX, barY,      new Color(255, 140, 0),
                barX + barWidth, barY, new Color(200, 0, 0)
        );
        g2.setPaint(grad);
        g2.fillRect(barX, barY, currentWidth, barHeight);
        g2.setPaint(null); // reset

        // Contour
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.setStroke(new BasicStroke(1f));

        // Texte centré
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.white);
        FontMetrics fm = g2.getFontMetrics();
        String txt = "BOSS  " + hp + " / " + maxHp;
        g2.drawString(txt, barX + barWidth / 2 - fm.stringWidth(txt) / 2, barY + 15);
    }

    // -------------------------------------------------------------------------
    // Utilitaire géométrique
    // -------------------------------------------------------------------------

    /**
     * Construit une étoile à 8 branches en alternant un rayon extérieur
     * et un rayon intérieur pour créer les pointes.
     *
     * @param cx           Centre X
     * @param cy           Centre Y
     * @param outerRadius  Rayon des pointes
     * @param innerRadius  Rayon des creux entre pointes
     * @param angleOffset  Rotation initiale en radians
     * @return Polygone en étoile
     */
    private Polygon buildStar8(int cx, int cy, int outerRadius, int innerRadius, double angleOffset) {
        int points = 8;
        int totalVertices = points * 2; // une pointe + un creux par branche
        int[] xp = new int[totalVertices];
        int[] yp = new int[totalVertices];

        for (int i = 0; i < totalVertices; i++) {
            double angle = angleOffset + i * Math.PI / points;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xp[i] = cx + (int) (radius * Math.cos(angle));
            yp[i] = cy + (int) (radius * Math.sin(angle));
        }
        return new Polygon(xp, yp, totalVertices);
    }
}
