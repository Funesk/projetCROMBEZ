package projetCROMBEZ;

import java.awt.*;

/**
 * Représente un projectile en déplacement sur l'écran.
 *
 * Un projectile est créé soit par le joueur (attaque automatique),
 * soit par un ennemi à  distance ou le boss.
 * Il se déplace en ligne droite vers sa cible et disparaà®t
 * dà¨s qu'il sort de l'écran ou touche sa cible.
 */
public class Projectile {

    // -------------------------------------------------------------------------
    // Position et mouvement
    // -------------------------------------------------------------------------

    /** Position X actuelle du projectile (en pixels). */
    public double x;

    /** Position Y actuelle du projectile (en pixels). */
    public double y;

    /** Vitesse horizontale (pixels par frame). */
    public double dx;

    /** Vitesse verticale (pixels par frame). */
    public double dy;

    // -------------------------------------------------------------------------
    // Attributs de combat
    // -------------------------------------------------------------------------

    /** Dégats infligés lors de l'impact. */
    public int damage;

    /** Rayon de collision (en pixels). */
    public int size;

    /**
     * true  â†’ projectile tiré par le joueur (frappe les ennemis).
     * false â†’ projectile ennemi (frappe le joueur).
     */
    public boolean fromPlayer;

    /** false dà¨s que le projectile doit être supprimé (impact ou hors écran). */
    public boolean alive = true;

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /** Couleur principale du projectile. */
    private Color color;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée un projectile partant de (x, y) et visant (targetX, targetY).
     *
     * @param x          Position de départ X
     * @param y          Position de départ Y
     * @param targetX    Position cible X
     * @param targetY    Position cible Y
     * @param damage     Dégats infligés à  l'impact
     * @param fromPlayer true si le tir vient du joueur
     */
    public Projectile(double x, double y, double targetX, double targetY,
                      int damage, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.fromPlayer = fromPlayer;

        // Les projectiles du joueur sont plus petits et plus rapides
        this.size = fromPlayer ? 8 : 10;
        this.color = fromPlayer ? new Color(255, 220, 0) : new Color(255, 60, 60);

        // Calcul de la direction normalisée puis application de la vitesse
        double speed = fromPlayer ? 10 : 5;
        double dist = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        if (dist > 0) {
            dx = (targetX - x) / dist * speed;
            dy = (targetY - y) / dist * speed;
        }
    }

    // -------------------------------------------------------------------------
    // Mise à  jour
    // -------------------------------------------------------------------------

    /**
     * Déplace le projectile d'un frame et le marque mort s'il quitte l'écran.
     *
     * @param screenWidth  Largeur de l'écran de jeu
     * @param screenHeight Hauteur de l'écran de jeu
     */
    public void update(int screenWidth, int screenHeight) {
        x += dx;
        y += dy;

        // Marge de 20px pour que le projectile disparaisse proprement hors du bord
        if (x < -20 || x > screenWidth + 20 || y < -20 || y > screenHeight + 20) {
            alive = false;
        }
    }

    // -------------------------------------------------------------------------
    // Collision
    // -------------------------------------------------------------------------

    /**
     * Retourne la zone de collision du projectile (carré centré sur sa position).
     */
    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine le projectile avec un halo semi-transparent et un noyau plein.
     */
    public void draw(Graphics2D g2) {
        // Halo (semi-transparent, double taille)
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2.fillOval((int) x - size, (int) y - size, size * 2, size * 2);

        // Corps principal
        g2.setColor(color);
        g2.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
    }
}
