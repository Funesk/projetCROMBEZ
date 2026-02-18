package projetCROMBEZ;

import java.awt.*;
import java.util.List;

public class MeleeEnemy extends Enemy {

    private int attackCooldown = 0;
    private static final int ATTACK_RATE = 60; // 1 attaque/seconde à 60fps

    public MeleeEnemy(double x, double y) {
        super(x, y, 40, 10, 2.0, 28, new Color(200, 80, 80));
    }

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        moveToward(player.x, player.y);

        if (attackCooldown > 0) attackCooldown--;

        // Attaque au contact
        if (getBounds().intersects(player.getBounds())) {
            if (attackCooldown <= 0) {
                player.takeDamage(damage);
                attackCooldown = ATTACK_RATE;
            }
        }
    }
}
