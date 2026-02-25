package projetCROMBEZ;

import java.io.*;
import java.util.Properties;

/**
 * Gestionnaire de sauvegarde du jeu.
 * Fichier : save/survivor.properties
 *
 * Donnees sauvegardees :
 *  settings.*      : options (difficulte, showRange, fullscreen)
 *  player.gold     : or disponible
 *  upgrade.*       : niveaux d'amelioration (0-5 chacun)
 *  records.*       : meilleur temps de survie
 *
 * Note : les stats effectives (maxHp, damage, etc.) ne sont PAS
 * sauvegardees directement. Elles sont recalculees depuis les niveaux
 * via Player.applyUpgrades() apres chaque chargement.
 */
public class SaveManager {

    private static final String SAVE_PATH = "save/survivor.properties";

    // =========================================================================
    // Chargement
    // =========================================================================

    public static void load(GamePanel gp) {
        File file = new File(SAVE_PATH);
        if (!file.exists()) {
            System.out.println("[SaveManager] Premier lancement - creation de " + SAVE_PATH);
            save(gp);
            return;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            System.out.println("[SaveManager] Sauvegarde chargee : " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("[SaveManager] Erreur de lecture : " + e.getMessage());
            return;
        }

        applySettings(props);
        applyUpgrades(props, gp.player);
        applyRecords(props, gp);
    }

    private static void applySettings(Properties props) {
        GameSettings s = GameSettings.getInstance();
        String diff = props.getProperty("settings.difficulty", "NORMAL");
        try   { s.setDifficulty(DifficultyLevel.valueOf(diff)); }
        catch (IllegalArgumentException e) { s.setDifficulty(DifficultyLevel.NORMAL); }
        s.setShowPlayerRange(Boolean.parseBoolean(props.getProperty("settings.showRange", "true")));

        // Charge le flag fullscreen. La valeur est appliquee par Main apres setVisible()
        // via toggleFullscreen(). On utilise setFullscreenFlag() et non toggleFullscreen()
        // car la fenetre n'est pas encore affichee a ce stade.
        s.setFullscreenFlag(Boolean.parseBoolean(props.getProperty("settings.fullscreen", "false")));
    }

    private static void applyUpgrades(Properties props, Player p) {
        // Or
        p.gold = getInt(props, "player.gold", 0);

        // Niveaux d'upgrade
        p.upgradeHp        = getInt(props, "upgrade.hp",        0);
        p.upgradeDamage    = getInt(props, "upgrade.damage",    0);
        p.upgradeRange     = getInt(props, "upgrade.range",     0);
        p.upgradeSpeed     = getInt(props, "upgrade.speed",     0);
        p.upgradeLifeSteal = getInt(props, "upgrade.lifeSteal", 0);
        p.upgradeCrit      = getInt(props, "upgrade.crit",      0);
        p.upgradeCritDmg   = getInt(props, "upgrade.critDmg",   0);

        // Recalcule toutes les stats depuis les niveaux
        p.applyUpgrades();
        p.hp = p.maxHp; // vie pleine au chargement
    }

    private static void applyRecords(Properties props, GamePanel gp) {
        gp.bestTimeTicks = getInt(props, "records.bestTimeTicks", 0);
    }


    // =========================================================================
    // Sauvegarde
    // =========================================================================

    public static void save(GamePanel gp) {
        File dir = new File("save");
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        GameSettings s = GameSettings.getInstance();
        Player       p = gp.player;

        // Options
        props.setProperty("settings.difficulty", s.getDifficulty().name());
        props.setProperty("settings.showRange",  String.valueOf(s.isShowPlayerRange()));
        props.setProperty("settings.fullscreen", String.valueOf(s.isFullscreen()));

        // Or
        props.setProperty("player.gold", String.valueOf(p.gold));

        // Niveaux d'upgrade
        props.setProperty("upgrade.hp",        String.valueOf(p.upgradeHp));
        props.setProperty("upgrade.damage",     String.valueOf(p.upgradeDamage));
        props.setProperty("upgrade.range",      String.valueOf(p.upgradeRange));
        props.setProperty("upgrade.speed",      String.valueOf(p.upgradeSpeed));
        props.setProperty("upgrade.lifeSteal",  String.valueOf(p.upgradeLifeSteal));
        props.setProperty("upgrade.crit",       String.valueOf(p.upgradeCrit));
        props.setProperty("upgrade.critDmg",    String.valueOf(p.upgradeCritDmg));

        // Records
        props.setProperty("records.bestTimeTicks",  String.valueOf(gp.bestTimeTicks));


        try (FileOutputStream fos = new FileOutputStream(SAVE_PATH)) {
            props.store(fos, "Survivor - Sauvegarde automatique");
            System.out.println("[SaveManager] Sauvegarde : " + new File(SAVE_PATH).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("[SaveManager] ERREUR : " + e.getMessage());
        }
    }


    // =========================================================================
    // Reinitialisation
    // =========================================================================

    /**
     * Reinitialise completement la sauvegarde a l'etat "premier lancement".
     *
     * Remet a zero :
     *  - L'or du joueur
     *  - Tous les niveaux d'upgrade
     *  - Le meilleur temps de survie
     * Conserve :
     *  - Les options (difficulte, showRange, fullscreen)
     *
     * Appele depuis MenuScreen apres confirmation du joueur.
     *
     * @param gp Reference au GamePanel
     */
    public static void resetSave(GamePanel gp) {
        Player p = gp.player;

        // Remise a zero de l'or
        p.gold = 0;

        // Remise a zero de tous les niveaux d'upgrade
        p.upgradeHp        = 0;
        p.upgradeDamage    = 0;
        p.upgradeRange     = 0;
        p.upgradeSpeed     = 0;
        p.upgradeLifeSteal = 0;
        p.upgradeCrit      = 0;
        p.upgradeCritDmg   = 0;

        // Recalcule les stats depuis les niveaux remis a zero
        p.applyUpgrades();
        p.hp = p.maxHp;

        // Remise a zero des records et de l'etat de partie
        gp.bestTimeTicks = 0;
        gp.hasActiveGame = false;

        // Sauvegarde le nouvel etat reinitialise
        save(gp);

        System.out.println("[SaveManager] Sauvegarde reinitalisee.");
    }

    // =========================================================================
    // Utilitaire
    // =========================================================================

    private static int getInt(Properties props, String key, int def) {
        try   { return Integer.parseInt(props.getProperty(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }
}
