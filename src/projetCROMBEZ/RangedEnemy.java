package projetCROMBEZ;

import java.awt.*;
import java.util.List;

public class RangedEnemy extends Enemy {

    private int shootCooldown = 0;
    private static final int SHOOT_RATE = 120; // tir toutes les 2s
    private static final double PREFERRED_DIST = 300; // distance préférée

    public RangedEnemy(double x, double y) {
        super(x, y, 25, 8, 1.5, 24, new Color(80, 160, 220));
    }

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        double dist = Math.sqrt(Math.pow(player.x - x, 2) + Math.pow(player.y - y, 2));

        // Maintient une distance avec le joueur
        if (dist < PREFERRED_DIST - 30) {
            // Trop proche : reculer
            x -= (player.x - x) / dist * speed;
            y -= (player.y - y) / dist * speed;
        } else if (dist > PREFERRED_DIST + 30) {
            // Trop loin : avancer
            moveToward(player.x, player.y);
        }

        // Tire sur le joueur
        if (shootCooldown > 0) shootCooldown--;
        if (shootCooldown <= 0) {
            projectiles.add(new Projectile(x, y, player.x, player.y, damage, false));
            shootCooldown = SHOOT_RATE;
        }
    }
}
