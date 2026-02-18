package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Ennemi tank lent mais extrêmement résistant.
 *
 *  STATS DE BASE (avant modificateurs de difficulté)
 *  HP       : 200   (5x le mêlée)
 *  Dégats   : 20    (2x le mêlée)
 *  Vitesse  : 0.8   px/frame  (trà¨s lent)
 *  Taille   : 44px
 *
 * Comportement :
 *  - Avance lentement mais inexorablement vers le joueur.
 *  - Attaque lourde au contact avec un cooldown plus long.
 *  - Priorité cible : doit être éliminé avant de s'approcher.
 *
 * Forme : Hexagone vert sombre évoque une armure lourde.
 */
public class TankEnemy extends Enemy {

    // -------------------------------------------------------------------------
    // Constantes d'attaque
    // -------------------------------------------------------------------------

    /** Cooldown entre deux attaques (90 frames = 1.5s à  60 FPS). */
    private static final int ATTACK_RATE = 90;

    // -------------------------------------------------------------------------
    // à‰tat interne
    // -------------------------------------------------------------------------

    /** Compteur de cooldown d'attaque. */
    private int attackCooldown = 0;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée un ennemi tank à  la position donnée.
     *
     * @param x Position de spawn X
     * @param y Position de spawn Y
     */
    public TankEnemy(double x, double y) {
        // HP=200 | damage=20 | speed=0.8 | size=44 | vert sombre
        super(x, y, 200, 20, 0.8, 44, new Color(40, 130, 60));
        this.hpBarColor = new Color(80, 200, 100);
    }

    // -------------------------------------------------------------------------
    // IA
    // -------------------------------------------------------------------------

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        // Avance toujours vers le joueur (pas de repositionnement)
        moveToward(player.x, player.y);

        // --- Attaque au contact ---
        if (attackCooldown > 0) attackCooldown--;

        if (getBounds().intersects(player.getBounds()) && attackCooldown <= 0) {
            player.takeDamage(damage);
            attackCooldown = ATTACK_RATE;
        }
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    @Override
    public void draw(Graphics2D g2) {
        // --- Hexagone régulier centré sur (x, y) ---
        Polygon hex = buildHexagon((int) x, (int) y, size / 2);

        // Corps (flash blanc si hit)
        g2.setColor(getDrawColor(color));
        g2.fillPolygon(hex);

        // Contour épais pour accentuer la robustesse
        g2.setColor(new Color(20, 70, 30));
        g2.setStroke(new BasicStroke(3f));
        g2.drawPolygon(hex);
        g2.setStroke(new BasicStroke(1f));

        // Rivets décoratifs aux coins de l'hexagone
        g2.setColor(new Color(180, 220, 180));
        int radius = size / 2;
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 6 + i * Math.PI / 3; // décalage de 30Â° pour hexagone "plat"
            int rx = (int) (x + radius * Math.cos(a));
            int ry = (int) (y + radius * Math.sin(a));
            g2.fillOval(rx - 3, ry - 3, 6, 6);
        }

        drawHpBar(g2);
    }

    // -------------------------------------------------------------------------
    // Utilitaire géométrique
    // -------------------------------------------------------------------------

    /**
     * Construit un hexagone régulier (pointe en haut) centré sur (cx, cy).
     *
     * @param cx     Centre X
     * @param cy     Centre Y
     * @param radius Rayon circumscrit (de centre à  sommet)
     * @return       Polygone hexagonal
     */
    private Polygon buildHexagon(int cx, int cy, int radius) {
        int[] xp = new int[6];
        int[] yp = new int[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 6 + i * Math.PI / 3; // hex "à  plat"
            xp[i] = cx + (int) (radius * Math.cos(angle));
            yp[i] = cy + (int) (radius * Math.sin(angle));
        }
        return new Polygon(xp, yp, 6);
    }
}
