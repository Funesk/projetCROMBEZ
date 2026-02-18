package projetCROMBEZ;

import java.awt.*;
import java.util.List;

/**
 * Ennemi de mélée  le type le plus basique.
 *
 * Comportement :
 *  - Fonce droit vers le joueur sans s'arreter.
 *  - Attaque dès qu'il est en contact avec le joueur.
 *  - Cooldown d'attaque pour ne pas vider la barre de vie instantanément.
 *
 * Forme : Triangle rouge points vers le joueur (agression visuelle).
 */
public class MeleeEnemy extends Enemy {

    // -------------------------------------------------------------------------
    // Constantes d'attaque
    // -------------------------------------------------------------------------

    /** Nombre de frames entre deux attaques de mélée (60 = 1 attaque/seconde a  60 FPS). */
    private static final int ATTACK_RATE = 60;

    // -------------------------------------------------------------------------
    // État interne
    // -------------------------------------------------------------------------

    /** Compteur de cooldown d'attaque (en frames). */
    private int attackCooldown = 0;

    /** Angle de rotation du triangle, en radians (pointe vers le joueur). */
    private double angle = 0;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée un ennemi de mélée a  la position donnée.
     *
     * @param x Position de spawn X
     * @param y Position de spawn Y
     */
    public MeleeEnemy(double x, double y) {
        // HP=40 | damage=10 | speed=2.0 | size=28 | rouge agressif
        super(x, y, 40, 10, 2.0, 28, new Color(220, 50, 50));
        this.hpBarColor = new Color(255, 80, 80);
    }

    // -------------------------------------------------------------------------
    // IA
    // -------------------------------------------------------------------------

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        // --- Déplacement : droit vers le joueur ---
        moveToward(player.x, player.y);

        // Calcule l'angle pour faire pivoter le triangle vers le joueur
        angle = Math.atan2(player.y - y, player.x - x);

        // --- Attaque au contact ---
        if (attackCooldown > 0) attackCooldown--;

        if (getBounds().intersects(player.getBounds()) && attackCooldown <= 0) {
            player.takeDamage(damage);
            attackCooldown = ATTACK_RATE; // réinitialise le cooldown
        }
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    @Override
    public void draw(Graphics2D g2) {
        // --- Construction du triangle équilatéral ---
        // Le triangle est défini dans un repère local (centré en 0,0),
        // puis tourné vers le joueur et translaté à  la position de l'ennemi.
        int half = size / 2;
        int[] xPts = { half, -half, -half };  // pointe droite, angle gauche haut, angle gauche bas
        int[] yPts = { 0, -half, half };

        Polygon triangle = buildRotatedPolygon(xPts, yPts, angle);

        // Corps du triangle (flash blanc si hit)
        g2.setColor(getDrawColor(color));
        g2.fillPolygon(triangle);

        // Contour sombre
        g2.setColor(new Color(120, 20, 20));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(triangle);
        g2.setStroke(new BasicStroke(1f));

        drawHpBar(g2);
    }

    // -------------------------------------------------------------------------
    // Utilitaire géométrique
    // -------------------------------------------------------------------------

    /**
     * Construit un polygone à  partir de points locaux en appliquant
     * une rotation et une translation vers la position de l'ennemi.
     *
     * @param xPts Points X dans l'espace local
     * @param yPts Points Y dans l'espace local
     * @param rot  Angle de rotation en radians
     * @return Polygone prêt à  être dessiné
     */
    private Polygon buildRotatedPolygon(int[] xPts, int[] yPts, double rot) {
        int n = xPts.length;
        int[] px = new int[n];
        int[] py = new int[n];
        double cos = Math.cos(rot);
        double sin = Math.sin(rot);

        for (int i = 0; i < n; i++) {
            px[i] = (int) (x + xPts[i] * cos - yPts[i] * sin);
            py[i] = (int) (y + xPts[i] * sin + yPts[i] * cos);
        }
        return new Polygon(px, py, n);
    }
}
