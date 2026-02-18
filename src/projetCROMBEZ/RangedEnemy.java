package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Ennemi à  distance â€“ tire des projectiles depuis un endroit sûr.
 *
 *  STATS DE BASE (avant modificateurs de difficulté)
 *  HP              : 25
 *  Dégà¢ts/proj.    : 8
 *  Vitesse         : 1.5 px/frame
 *  Taille          : 24px
 *  Distance préf.  : 300px du joueur
 *  Cadence de tir  : 1 projectile toutes les 2s (120 frames)
 *
 * Comportement :
 *  - Maintient une distance préférentielle avec le joueur.
 *    - Trop proche  recule.
 *    - Trop loin    avance.
 *  - Tire un projectile toutes les 2 secondes vers le joueur.
 *  - Fragile mais dangereux en groupe.
 *
 */
public class RangedEnemy extends Enemy {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    /** Intervalle entre deux tirs, en frames. */
    private static final int SHOOT_RATE = 120;

    /** Distance idéale à  maintenir avec le joueur (en pixels). */
    private static final double PREFERRED_DIST = 300;

    /** Zone de tolérance autour de la distance préférée (Â±30px). */
    private static final double DIST_MARGIN = 30;

    // -------------------------------------------------------------------------
    // à‰tat interne
    // -------------------------------------------------------------------------

    /** Compteur de cooldown avant prochain tir. */
    private int shootCooldown = 60; // petit délai initial pour ne pas tirer au spawn

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée un ennemi à  distance à  la position donnée.
     *
     * @param x Position de spawn X
     * @param y Position de spawn Y
     */
    public RangedEnemy(double x, double y) {
        // HP=25 | damage=8 | speed=1.5 | size=24 | cyan lointain
        super(x, y, 25, 8, 1.5, 24, new Color(0, 180, 220));
        this.hpBarColor = new Color(0, 210, 255);
    }

    // -------------------------------------------------------------------------
    // IA
    // -------------------------------------------------------------------------

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        double dist = Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));

        // --- Repositionnement : maintient la distance préférentielle ---
        if (dist < PREFERRED_DIST - DIST_MARGIN) {
            // Trop proche â†’ recule (direction opposée au joueur)
            x -= (player.x - x) / dist * speed;
            y -= (player.y - y) / dist * speed;
        } else if (dist > PREFERRED_DIST + DIST_MARGIN) {
            // Trop loin â†’ avance vers le joueur
            moveToward(player.x, player.y);
        }
        // Dans la zone de tolérance â†’ reste en place

        // --- Tir ---
        if (shootCooldown > 0) shootCooldown--;

        if (shootCooldown <= 0) {
            // Crée un projectile visant la position actuelle du joueur
            projectiles.add(new Projectile(x, y, player.x, player.y, damage, false));
            shootCooldown = SHOOT_RATE;
        }
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    @Override
    public void draw(Graphics2D g2) {
        // --- Losange (4 points) centré sur (x, y) ---
        int h = size / 2; // demi-taille

        Polygon diamond = new Polygon(
            new int[]{ (int)x,      (int)x + h, (int)x,      (int)x - h },
            new int[]{ (int)y - h,  (int)y,     (int)y + h,  (int)y     },
            4
        );

        // Corps (flash blanc si hit)
        g2.setColor(getDrawColor(color));
        g2.fillPolygon(diamond);

        // Contour sombre
        g2.setColor(new Color(0, 90, 140));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(diamond);
        g2.setStroke(new BasicStroke(1f));

        // Petite pastille centrale pour différencier visuellement
        g2.setColor(new Color(180, 240, 255));
        g2.fillOval((int)x - 4, (int)y - 4, 8, 8);

        drawHpBar(g2);
    }
}
