package projetCROMBEZ;

import java.io.*;
import java.util.Properties;

/**
 * Gestionnaire de sauvegarde du jeu.
 *
 * Ecrit et lit un fichier texte "save/survivor.properties".
 * Le fichier est cree dans le dossier de travail (repertoire du projet
 * sous Eclipse, ou dossier du .jar si execute en standalone).
 *
 * =========================================================================
 * DONNEES SAUVEGARDEES
 * =========================================================================
 *
 *  --- Options ---
 *  settings.difficulty     : EASY | NORMAL | HARD
 *  settings.showRange      : true | false
 *  settings.fullscreen     : true | false
 *
 *  --- Stats du joueur (pour la boutique future) ---
 *  player.maxHp            : entier (defaut 100)
 *  player.damage           : entier (defaut 15)
 *  player.attackRange      : entier (defaut 200)
 *  player.attackRate       : entier (defaut 60)
 *
 *  --- Progression boutique (gold pour la boutique future) ---
 *  player.gold             : entier (defaut 0)
 *
 *  --- Records ---
 *  records.bestScore       : entier (defaut 0)
 *  records.bestTimeTicks   : entier en frames (defaut 0)
 *
 *  --- Etat applicatif ---
 *  state.hasActiveGame     : true | false (partie en pause repristable)
 *
 * =========================================================================
 * QUAND LA SAUVEGARDE EST DECLENCHEE
 * =========================================================================
 *  - Automatiquement a chaque changement d'option (via GameSettings)
 *  - En fin de partie (mort ou victoire)
 *  - Clic sur "Sauvegarder" dans le menu pause
 *  - Clic sur "Quitter" dans un menu
 *  - Fermeture de la fenetre via WindowListener dans Main (garanti)
 *  - ShutdownHook comme filet de securite supplementaire
 */
public class SaveManager {

    // =========================================================================
    // Constante
    // =========================================================================

    /**
     * Chemin du fichier de sauvegarde relatif au repertoire de travail.
     * Sous Eclipse : dossier racine du projet.
     * En .jar      : dossier contenant le .jar.
     */
    private static final String SAVE_PATH = "save/survivor.properties";

    // =========================================================================
    // Chargement
    // =========================================================================

    /**
     * Charge la sauvegarde et applique les valeurs aux systemes du jeu.
     *
     * Si le fichier n'existe pas (premier lancement), les valeurs par defaut
     * sont conservees et un fichier initial est cree immediatement.
     *
     * DOIT etre appele APRES GameSettings.setGamePanel() et APRES la creation
     * de gamePanel.player (voir Main.main()).
     *
     * @param gp Reference au GamePanel
     */
    public static void load(GamePanel gp) {
        File file = new File(SAVE_PATH);

        if (!file.exists()) {
            System.out.println("[SaveManager] Premier lancement - creation de " + SAVE_PATH);
            save(gp); // cree le fichier avec les valeurs par defaut
            return;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            System.out.println("[SaveManager] Sauvegarde chargee depuis " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("[SaveManager] Impossible de lire " + SAVE_PATH + " : " + e.getMessage());
            return;
        }

        applySettings(props);
        applyPlayerStats(props, gp.player);
        applyRecords(props, gp);
        applyAppState(props, gp);
    }

    // =========================================================================
    // Application des donnees chargees
    // =========================================================================

    /** Applique les options de jeu lues depuis le fichier. */
    private static void applySettings(Properties props) {
        GameSettings s = GameSettings.getInstance();

        // Difficulte
        String diff = props.getProperty("settings.difficulty", "NORMAL");
        try {
            s.setDifficulty(DifficultyLevel.valueOf(diff));
        } catch (IllegalArgumentException e) {
            s.setDifficulty(DifficultyLevel.NORMAL);
        }

        // Affichage de la portee
        s.setShowPlayerRange(Boolean.parseBoolean(
                props.getProperty("settings.showRange", "true")));

        // Note : fullscreen n'est pas restaure au demarrage car la JFrame
        // n'est pas encore affichee a ce stade. L'etat est sauvegarde mais
        // pas reapplique automatiquement pour eviter des artefacts visuels.
    }

    /** Applique les statistiques du joueur lues depuis le fichier. */
    private static void applyPlayerStats(Properties props, Player p) {
        p.maxHp       = getInt(props, "player.maxHp",       100);
        p.damage      = getInt(props, "player.damage",       15);
        p.attackRange = getInt(props, "player.attackRange", 200);
        p.attackRate  = getInt(props, "player.attackRate",   60);
        p.gold        = getInt(props, "player.gold",          0);

        // La vie est remise au max au chargement (pas en milieu de partie)
        p.hp = p.maxHp;
    }

    /** Applique les records lus depuis le fichier. */
    private static void applyRecords(Properties props, GamePanel gp) {
        gp.bestScore     = getInt(props, "records.bestScore",     0);
        gp.bestTimeTicks = getInt(props, "records.bestTimeTicks", 0);
    }

    /** Applique l'etat applicatif (hasActiveGame) lu depuis le fichier. */
    private static void applyAppState(Properties props, GamePanel gp) {
        gp.hasActiveGame = Boolean.parseBoolean(
                props.getProperty("state.hasActiveGame", "false"));

        // Si hasActiveGame est true, on affiche le bouton "Reprendre" au menu.
        // La partie en cours n'est PAS restauree (position, ennemis, etc.)
        // car cela necessite une serialisation complete de l'etat de jeu.
        // Pour la boutique, seules les stats/upgrades du joueur comptent.
        if (gp.hasActiveGame) {
            System.out.println("[SaveManager] Partie precedente disponible (Reprendre).");
        }
    }

    // =========================================================================
    // Sauvegarde
    // =========================================================================

    /**
     * Sauvegarde l'etat complet du jeu dans le fichier.
     *
     * Cree le dossier "save/" si necessaire.
     * Thread-safe : peut etre appele depuis le ShutdownHook ou le EDT.
     *
     * @param gp Reference au GamePanel
     */
    public static void save(GamePanel gp) {
        // Creation du dossier save/ si absent
        File dir = new File("save");
        if (!dir.exists()) dir.mkdirs();

        Properties props = new Properties();
        GameSettings s   = GameSettings.getInstance();
        Player       p   = gp.player;

        // Options
        props.setProperty("settings.difficulty",  s.getDifficulty().name());
        props.setProperty("settings.showRange",   String.valueOf(s.isShowPlayerRange()));
        props.setProperty("settings.fullscreen",  String.valueOf(s.isFullscreen()));

        // Stats joueur (persistantes entre les parties, modifiables par la boutique)
        props.setProperty("player.maxHp",       String.valueOf(p.maxHp));
        props.setProperty("player.damage",      String.valueOf(p.damage));
        props.setProperty("player.attackRange", String.valueOf(p.attackRange));
        props.setProperty("player.attackRate",  String.valueOf(p.attackRate));
        props.setProperty("player.gold",        String.valueOf(p.gold));

        // Records
        props.setProperty("records.bestScore",     String.valueOf(gp.bestScore));
        props.setProperty("records.bestTimeTicks",  String.valueOf(gp.bestTimeTicks));

        // Etat applicatif
        props.setProperty("state.hasActiveGame", String.valueOf(gp.hasActiveGame));

        // Ecriture dans le fichier
        try (FileOutputStream fos = new FileOutputStream(SAVE_PATH)) {
            props.store(fos, "Survivor - Sauvegarde automatique");
            System.out.println("[SaveManager] Sauvegarde ecrite dans "
                    + new File(SAVE_PATH).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("[SaveManager] ERREUR d'ecriture : " + e.getMessage());
        }
    }

    // =========================================================================
    // Utilitaire
    // =========================================================================

    /**
     * Lit un entier depuis les proprietes avec une valeur de secours.
     * Ne leve pas d'exception si la cle est absente ou corrompue.
     */
    private static int getInt(Properties props, String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
