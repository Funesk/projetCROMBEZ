package projetCROMBEZ;

import java.awt.*;
import java.util.List;

public class TankEnemy extends Enemy {

    private int attackCooldown = 0;
    private static final int ATTACK_RATE = 90;

    public TankEnemy(double x, double y) {
        super(x, y, 200, 20, 0.8, 44, new Color(100, 60, 180));
        this.hpBarColor = new Color(130, 80, 220);
    }

    @Override
    public void update(Player player, List<Projectile> projectiles) {
        moveToward(player.x, player.y);

        if (attackCooldown > 0) attackCooldown--;

        if (getBounds().intersects(player.getBounds())) {
            if (attackCooldown <= 0) {
                player.takeDamage(damage);
                attackCooldown = ATTACK_RATE;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Corps principal
        if (invincibleTimer > 0) {
            invincibleTimer--;
            g2.setColor(Color.white);
        } else {
            g2.setColor(color);
        }
        g2.fillRect((int) x - size / 2, (int) y - size / 2, size, size);

        // Bordure épaisse pour le différencier
        g2.setColor(new Color(60, 30, 130));
        g2.setStroke(new BasicStroke(3));
        g2.drawRect((int) x - size / 2, (int) y - size / 2, size, size);
        g2.setStroke(new BasicStroke(1));

        drawHpBar(g2);
    }
}
