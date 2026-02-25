package projetCROMBEZ;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Gestionnaire des ennemis et des vagues.
 *
 * Progression :
 *  Vague 1   : Melee uniquement
 *  Vague 2-3 : Melee 70% + Distance 30%
 *  Vague 4   : Melee 45% + Distance 30% + Tank 25%
 *  Vague 5   : Boss final (apres elimination de tous les ennemis normaux)
 *
 * Correctif compteur : waveDelayTimer est en frames (180 = 3 sec a 60 FPS).
 * L'affichage divise par 60 pour montrer des secondes entieres.
 *
 * Gold drop : chaque ennemi tue rapporte de l'or au joueur.
 *  - MeleeEnemy  : 5 or
 *  - RangedEnemy : 8 or
 *  - TankEnemy   : 15 or
 *  - BossEnemy   : 50 or
 */
public class EnemyManager {

    private GamePanel     gp;
    public  List<Enemy>   enemies = new ArrayList<>();
    private Random        rand    = new Random();

    private int  spawnTimer = 0, spawnRate = 120, wave = 1;
    private int  enemiesSpawnedThisWave = 0, maxEnemiesPerWave;
    private boolean bossSpawned = false, waitingForNextWave = false;
    private int  waveDelayTimer = 0;

    public boolean bossDefeated = false;

    // =========================================================================

    public EnemyManager(GamePanel gp) {
        this.gp = gp;
        maxEnemiesPerWave = computeMax(1);
    }

    public void reset() {
        enemies.clear();
        wave = 1;
        spawnTimer = 0;
        enemiesSpawnedThisWave = 0;
        maxEnemiesPerWave = computeMax(1);
        spawnRate = 120;
        bossSpawned = bossDefeated = waitingForNextWave = false;
        waveDelayTimer = 0;
    }

    // =========================================================================

    public void update(Player player, List<Projectile> projectiles) {
        enemies.removeIf(e -> !e.alive);

        // Detection mort du boss
        if (bossSpawned && !hasBoss()) bossDefeated = true;

        for (Enemy e : enemies) e.update(player, projectiles);

        if (waitingForNextWave) {
            if (--waveDelayTimer <= 0) nextWave();
            return;
        }

        // Spawn boss vague 5
        if (wave == 5 && !bossSpawned
                && enemiesSpawnedThisWave >= maxEnemiesPerWave
                && enemies.isEmpty()) {
            BossEnemy boss = new BossEnemy(gp.screenWidth / 2.0, -80);
            applyDiff(boss);
            enemies.add(boss);
            bossSpawned = true;
            return;
        }

        // Spawn ennemis normaux
        if (enemiesSpawnedThisWave < maxEnemiesPerWave) {
            if (--spawnTimer <= 0) {
                spawnEnemy();
                enemiesSpawnedThisWave++;
                spawnTimer = spawnRate;
            }
        } else if (enemies.isEmpty() && !bossSpawned && wave < 5) {
            waitingForNextWave = true;
            waveDelayTimer = 180; // 3 secondes a 60 FPS
        }
    }

    private void nextWave() {
        wave++;
        enemiesSpawnedThisWave = 0;
        maxEnemiesPerWave = computeMax(wave);
        spawnRate = Math.max(40, 120 - wave * 10);
        bossSpawned = waitingForNextWave = false;
    }

    private int computeMax(int w) {
        return Math.max(1, (int)((8 + w * 3) * GameSettings.getInstance().getWaveSizeMultiplier()));
    }

    private void spawnEnemy() {
        double[] p = spawnPos();
        int r = rand.nextInt(100);
        Enemy e;
        if      (wave == 1) e = new MeleeEnemy(p[0], p[1]);
        else if (wave <= 3) e = (r < 70) ? new MeleeEnemy(p[0], p[1]) : new RangedEnemy(p[0], p[1]);
        else {
            if      (r < 45) e = new MeleeEnemy(p[0], p[1]);
            else if (r < 75) e = new RangedEnemy(p[0], p[1]);
            else             e = new TankEnemy(p[0], p[1]);
        }
        applyDiff(e);
        enemies.add(e);
    }

    private void applyDiff(Enemy e) {
        GameSettings s = GameSettings.getInstance();
        e.maxHp  = Math.max(1, (int)(e.maxHp  * s.getHpMultiplier()));
        e.hp     = e.maxHp;
        e.damage = Math.max(1, (int)(e.damage * s.getDamageMultiplier()));
    }

    /**
     * Retourne l'or gagne en tuant cet ennemi.
     * Appele depuis GamePanel quand un ennemi passe alive=false.
     */
    public static int goldForEnemy(Enemy e) {
        if      (e instanceof BossEnemy)   return 50;
        else if (e instanceof TankEnemy)   return 15;
        else if (e instanceof RangedEnemy) return 8;
        else                               return 5; // MeleeEnemy
    }

    private double[] spawnPos() {
        int side = rand.nextInt(4);
        double x, y;
        switch (side) {
            case 0:  x = rand.nextInt(gp.screenWidth);  y = -60;                           break;
            case 1:  x = gp.screenWidth  + 60;          y = rand.nextInt(gp.screenHeight); break;
            case 2:  x = rand.nextInt(gp.screenWidth);  y = gp.screenHeight + 60;          break;
            default: x = -60;                            y = rand.nextInt(gp.screenHeight); break;
        }
        return new double[]{x, y};
    }

    // =========================================================================

    public void draw(Graphics2D g2) {
        for (Enemy e : enemies) e.draw(g2);

        // Indicateur de vague (centree en haut)
        Font wf = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 16f)
                                      : new Font("Arial", Font.BOLD, 16);
        g2.setFont(wf);
        g2.setColor(Color.white);
        String wt = (wave == 5 && bossSpawned) ? "BOSS !" : "Vague " + wave + " / 5";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(wt, gp.screenWidth / 2 - fm.stringWidth(wt) / 2, 25);

        // Compte a rebours inter-vague
        if (waitingForNextWave) {
            Font bf = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 30f)
                                          : new Font("Arial", Font.BOLD, 30);
            g2.setFont(bf);
            g2.setColor(new Color(255, 220, 50));

            // CORRECTIF : divise waveDelayTimer par 60 pour obtenir les secondes
            // waveDelayTimer = 180 frames -> 3s, 120 -> 2s, 60 -> 1s, 0 -> 0s
            int secondsLeft = (waveDelayTimer + 59) / 60; // arrondi superieur

            String msg = "Vague " + (wave + 1) + " dans " + secondsLeft + "...";
            fm = g2.getFontMetrics();
            g2.drawString(msg, gp.screenWidth / 2 - fm.stringWidth(msg) / 2,
                          gp.screenHeight / 2 - 50);
        }
    }

    public boolean hasBoss() {
        for (Enemy e : enemies) if (e instanceof BossEnemy) return true;
        return false;
    }
}
