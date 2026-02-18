package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyManager {

    private GamePanel gp;
    public List<Enemy> enemies = new ArrayList<>();
    private Random rand = new Random();

    private int spawnTimer = 0;
    private int spawnRate = 120; // intervalle en frames entre spawn
    private int wave = 1;
    private int enemiesSpawnedThisWave = 0;
    private int maxEnemiesPerWave = 10;
    private boolean bossSpawned = false;
    private int waveDelayTimer = 0;
    private boolean waitingForNextWave = false;

    public boolean bossDefeated = false;

    public EnemyManager(GamePanel gp) {
        this.gp = gp;
    }

    public void reset() {
        enemies.clear();
        spawnTimer = 0;
        wave = 1;
        enemiesSpawnedThisWave = 0;
        bossSpawned = false;
        bossDefeated = false;
        waitingForNextWave = false;
        waveDelayTimer = 0;
    }

    public void update(Player player, List<Projectile> projectiles) {
        // Retire les ennemis morts
        enemies.removeIf(e -> !e.alive);

        // Vérifie si le boss est mort
        for (Enemy e : enemies) {
            if (e instanceof BossEnemy && !e.alive) {
                bossDefeated = true;
            }
        }

        // Update chaque ennemi
        for (Enemy e : enemies) {
            e.update(player, projectiles);
        }

        // Vérification fin de vague
        if (waitingForNextWave) {
            waveDelayTimer--;
            if (waveDelayTimer <= 0) {
                waitingForNextWave = false;
                wave++;
                enemiesSpawnedThisWave = 0;
                maxEnemiesPerWave = 10 + wave * 3;
                spawnRate = Math.max(40, 120 - wave * 10);
                bossSpawned = false;
            }
            return;
        }

        // Spawn boss à  la vague 5
        if (wave == 5 && !bossSpawned && enemiesSpawnedThisWave >= maxEnemiesPerWave && enemies.isEmpty()) {
            spawnBoss();
            bossSpawned = true;
            return;
        }

        // Spawn des ennemis normaux
        if (enemiesSpawnedThisWave < maxEnemiesPerWave) {
            spawnTimer--;
            if (spawnTimer <= 0) {
                spawnEnemy();
                enemiesSpawnedThisWave++;
                spawnTimer = spawnRate;
            }
        } else if (enemies.isEmpty() && !bossSpawned) {
            // Vague terminée
            waitingForNextWave = true;
            waveDelayTimer = 180; // 3 secondes avant prochaine vague
        }
    }

    private void spawnEnemy() {
        double[] pos = getSpawnPosition();
        double x = pos[0];
        double y = pos[1];

        int r = rand.nextInt(100);
        Enemy e;

        if (wave == 1) {
            e = new MeleeEnemy(x, y);
        } else if (wave <= 3) {
            e = (r < 70) ? new MeleeEnemy(x, y) : new RangedEnemy(x, y);
        } else {
            if (r < 50) e = new MeleeEnemy(x, y);
            else if (r < 75) e = new RangedEnemy(x, y);
            else e = new TankEnemy(x, y);
        }

        enemies.add(e);
    }

    private void spawnBoss() {
        double[] pos = getSpawnPosition();
        enemies.add(new BossEnemy(pos[0], pos[1]));
    }

    private double[] getSpawnPosition() {
        int side = rand.nextInt(4);
        double x, y;
        switch (side) {
            case 0: x = rand.nextInt(gp.screenWidth); y = -50; break;
            case 1: x = gp.screenWidth + 50; y = rand.nextInt(gp.screenHeight); break;
            case 2: x = rand.nextInt(gp.screenWidth); y = gp.screenHeight + 50; break;
            default: x = -50; y = rand.nextInt(gp.screenHeight); break;
        }
        return new double[]{x, y};
    }

    public void draw(Graphics2D g2) {
        for (Enemy e : enemies) {
            e.draw(g2);
        }

        // Affichage de la vague en haut
        g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(16f) : new Font("Arial", Font.PLAIN, 16));
        g2.setColor(Color.white);
        String waveText = (wave == 5 && bossSpawned) ? "BOSS !" : "Vague " + wave;
        g2.drawString(waveText, gp.screenWidth / 2 - 30, 25);

        if (waitingForNextWave) {
            g2.setFont(gp.gameFont != null ? gp.gameFont.deriveFont(24f) : new Font("Arial", Font.BOLD, 24));
            g2.setColor(new Color(255, 220, 50));
            String msg = "Vague " + (wave + 1) + " arrive...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, gp.screenWidth / 2 - fm.stringWidth(msg) / 2, gp.screenHeight / 2 - 50);
        }
    }

    public boolean hasBoss() {
        for (Enemy e : enemies) {
            if (e instanceof BossEnemy) return true;
        }
        return false;
    }
}
