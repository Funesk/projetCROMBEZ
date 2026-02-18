package projetCROMBEZ;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestionnaire des ennemis et des vagues.
 *
 * Responsabilités :
 *  - Faire spawner les ennemis sur les bords de l'écran à  intervalles réguliers.
 *  - Gérer la progression par vagues (de 1 à  5).
 *  - Appliquer les modificateurs de difficulté aux ennemis au spawn.
 *  - Détecter la mort du boss pour déclencher la victoire.
 *  - Déléguer la mise à  jour et le dessin à  chaque ennemi.
 *
 * Progression des vagues :
 *  Vague 1  Mélée uniquement
 *  Vague 2-3  Mélée + Distance
 *  Vague 4  Mélée + Distance + Tank
 *  Vague 5  Boss final (après avoir éliminé tous les ennemis normaux)
 */
public class EnemyManager {

    // -------------------------------------------------------------------------
    // références
    // -------------------------------------------------------------------------

    /** Référence au panneau de jeu (pour les dimensions de l'écran). */
    private GamePanel gp;

    // -------------------------------------------------------------------------
    // état des ennemis
    // -------------------------------------------------------------------------

    /** Liste de tous les ennemis actuellement en vie sur la map. */
    public List<Enemy> enemies = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Gestion des vagues
    // -------------------------------------------------------------------------

    /** Générateur de nombres aléatoires (positions de spawn, type d'ennemi). */
    private Random rand = new Random();

    /** Compteur de frames avant le prochain spawn. */
    private int spawnTimer = 0;

    /** Intervalle entre deux spawns, en frames. Diminue avec les vagues. */
    private int spawnRate = 120;

    /** Numéro de la vague actuelle (commence Ã  1). */
    private int wave = 1;

    /** Nombre d'ennemis déjà  apparus dans la vague actuelle. */
    private int enemiesSpawnedThisWave = 0;

    /** Nombre maximum d'ennemis à  faire spawner dans cette vague. */
    private int maxEnemiesPerWave = 10;

    /** true si le boss de la vague 5 a déjà  été spawn. */
    private boolean bossSpawned = false;

    /** Compteur de frames du délai entre deux vagues (affichage du message). */
    private int waveDelayTimer = 0;

    /** true si on attend la fin du délai inter-vague. */
    private boolean waitingForNextWave = false;

    // -------------------------------------------------------------------------
    // Progression / fin de partie
    // -------------------------------------------------------------------------

    /** Devient true quand le boss est éliminé  déclenche la victoire dans GamePanel. */
    public boolean bossDefeated = false;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée le gestionnaire d'ennemis.
     *
     * @param gp Référence au GamePanel pour les dimensions de l'écran
     */
    public EnemyManager(GamePanel gp) {
        this.gp = gp;
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Réinitialise complétement l'état du gestionnaire pour une nouvelle partie.
     * Appelé depuis GamePanel.resetGame().
     */
    public void reset() {
        enemies.clear();
        spawnTimer      = 0;
        wave            = 1;
        enemiesSpawnedThisWave = 0;
        maxEnemiesPerWave      = computeMaxEnemies(1);
        spawnRate       = 120;
        bossSpawned     = false;
        bossDefeated    = false;
        waitingForNextWave = false;
        waveDelayTimer  = 0;
    }

    // -------------------------------------------------------------------------
    // Mise à jour principale
    // -------------------------------------------------------------------------

    /**
     * Mise à  jour de tous les ennemis et de la logique de vague.
     * Appelé chaque frame par GamePanel.update() quand l'état est PLAYING.
     *
     * @param player      Référence au joueur
     * @param projectiles Liste partagée des projectiles
     */
    public void update(Player player, List<Projectile> projectiles) {
        // Supprime proprement les ennemis morts de la liste
        enemies.removeIf(e -> !e.alive);

        // Vérifie si le boss vient d'être éliminé
        if (bossSpawned && !hasBoss() && !enemies.isEmpty() == false) {
            bossDefeated = true;
        }
        // Alternative : le flag alive du boss
        // (géré dans GamePanel via la liste après removeIf)

        // Met à  jour chaque ennemi vivant
        for (Enemy e : enemies) {
            e.update(player, projectiles);
        }

        // --- Attente inter-vague ---
        if (waitingForNextWave) {
            waveDelayTimer--;
            if (waveDelayTimer <= 0) {
                startNextWave(); // passe Ã  la vague suivante
            }
            return;
        }

        // --- Spawn boss à  la vague 5 ---
        if (wave == 5 && !bossSpawned
                && enemiesSpawnedThisWave >= maxEnemiesPerWave
                && enemies.isEmpty()) {
            spawnBoss();
            bossSpawned = true;
            return;
        }

        // --- Spawn ennemis normaux ---
        if (enemiesSpawnedThisWave < maxEnemiesPerWave) {
            spawnTimer--;
            if (spawnTimer <= 0) {
                spawnEnemy();
                enemiesSpawnedThisWave++;
                spawnTimer = spawnRate;
            }

        } else if (enemies.isEmpty() && !bossSpawned) {
            // Tous les ennemis de la vague sont éliminés : inter-vague
            if (wave < 5) {
                waitingForNextWave = true;
                waveDelayTimer = 180; // 3 secondes de pause
            }
        }
    }

    // -------------------------------------------------------------------------
    // Logique de vague
    // -------------------------------------------------------------------------

    /**
     * IncrÃ©mente la vague et recalcule les paramÃ¨tres de spawn.
     */
    private void startNextWave() {
        wave++;
        enemiesSpawnedThisWave = 0;
        maxEnemiesPerWave = computeMaxEnemies(wave);

        // Le spawn rate diminue avec les vagues (ennemi plus agressif)
        spawnRate = Math.max(40, 120 - wave * 10);
        bossSpawned = false;
        waitingForNextWave = false;
    }

    /**
     * Calcule le nombre d'ennemis maximum pour une vague donnée,
     * en tenant compte du multiplicateur de difficulté.
     *
     * @param waveNum Numéro de la vague
     * @return Nombre d'ennemis pour cette vague
     */
    private int computeMaxEnemies(int waveNum) {
        float base = 8 + waveNum * 3; // 11, 14, 17, 20, 23...
        return Math.max(1, (int)(base * GameSettings.getInstance().getWaveSizeMultiplier()));
    }

    // -------------------------------------------------------------------------
    // Spawn des ennemis
    // -------------------------------------------------------------------------

    /**
     * Fait apparaitre un ennemi aléatoire adapté Ã  la vague courante,
     * Ã  une position aléatoire hors de l'écran.
     * Applique les modificateurs de difficulté aprés création.
     */
    private void spawnEnemy() {
        double[] pos = getSpawnPosition();
        double x = pos[0];
        double y = pos[1];

        int r = rand.nextInt(100); // Tirage aléatoire
        Enemy e;

        // Composition des vagues par type d'ennemi
        if (wave == 1) {
            // Vague 1 : mélée uniquement (tutoriel implicite)
            e = new MeleeEnemy(x, y);

        } else if (wave <= 3) {
            // Vagues 2-3 : 70% mélée, 30% distance
            e = (r < 70) ? new MeleeEnemy(x, y) : new RangedEnemy(x, y);

        } else {
            // Vagues 4+ : mix des trois types
            if      (r < 45) e = new MeleeEnemy(x, y);
            else if (r < 75) e = new RangedEnemy(x, y);
            else             e = new TankEnemy(x, y);
        }

        // Applique les multiplicateurs de difficulté
        applyDifficulty(e);

        enemies.add(e);
    }

    /**
     * Fait apparaitre le boss au centre du bord haut de l'écran.
     * Applique également les modificateurs de difficulté.
     */
    private void spawnBoss() {
        // Spawn en haut au centre
        BossEnemy boss = new BossEnemy(gp.screenWidth / 2.0, -80);
        applyDifficulty(boss);
        enemies.add(boss);
    }

    /**
     * Applique les multiplicateurs de HP et de dégats de la difficulté
     * sélectionnée sur un ennemi après sa création.
     *
     * @param e Ennemi a modifier
     */
    private void applyDifficulty(Enemy e) {
        GameSettings s = GameSettings.getInstance();
        e.maxHp   = Math.max(1, (int)(e.maxHp  * s.getHpMultiplier()));
        e.hp      = e.maxHp; // remet la vie au max aprÃ¨s scaling
        e.damage  = Math.max(1, (int)(e.damage * s.getDamageMultiplier()));
    }

    // -------------------------------------------------------------------------
    // Utilitaire de spawn
    // -------------------------------------------------------------------------

    /**
     * Génère une position de spawn aléatoire sur l'un des 4 bords de l'écran,
     * légèrement en dehors (pour que l'ennemi entre progressivement).
     *
     * @return Tableau [x, y]
     */
    private double[] getSpawnPosition() {
        int side = rand.nextInt(4); // 0=haut, 1=droite, 2=bas, 3=gauche
        double x, y;
        switch (side) {
            case 0:  x = rand.nextInt(gp.screenWidth); y = -60;                      break;
            case 1:  x = gp.screenWidth + 60;          y = rand.nextInt(gp.screenHeight); break;
            case 2:  x = rand.nextInt(gp.screenWidth); y = gp.screenHeight + 60;     break;
            default: x = -60;                          y = rand.nextInt(gp.screenHeight); break;
        }
        return new double[]{x, y};
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------

    /**
     * Dessine tous les ennemis vivants et l'indicateur de vague en haut de l'écran.
     */
    public void draw(Graphics2D g2) {
        // Dessine chaque ennemi
        for (Enemy e : enemies) {
            e.draw(g2);
        }

        // --- Indicateur de vague ---
        Font waveFont = gp.gameFont != null ? gp.gameFont.deriveFont(Font.BOLD, 16f)
                                            : new Font("Arial", Font.BOLD, 16);
        g2.setFont(waveFont);
        g2.setColor(Color.white);

        String waveText = (wave == 5 && bossSpawned) ? "BOSS " : "Vague " + wave + " / 5";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(waveText, gp.screenWidth / 2 - fm.stringWidth(waveText) / 2, 25);

        // --- Message inter-vague ---
        if (waitingForNextWave) {
            Font bigFont = gp.gameFont != null ? gp.gameFont.deriveFont(26f)
                                               : new Font("Arial", Font.BOLD, 26);
            g2.setFont(bigFont);
            g2.setColor(new Color(255, 220, 50));
            String msg = "Vague " + (wave + 1) + " dans...";
            fm = g2.getFontMetrics();
            g2.drawString(msg, gp.screenWidth / 2 - fm.stringWidth(msg) / 2, gp.screenHeight / 2 - 50);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Vérifie si un BossEnemy est encore présent dans la liste des ennemis.
     *
     * @return true si le boss est encore vivant sur le terrain
     */
    public boolean hasBoss() {
        for (Enemy e : enemies) {
            if (e instanceof BossEnemy) return true;
        }
        return false;
    }
}
