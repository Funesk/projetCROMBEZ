package projetCROMBEZ;

/**
 * Enumération de tous les états possibles du jeu.
 * GamePanel utilise cet état pour savoir quoi afficher et mettre à jour.
 */
public enum GameState {

    /** écran d'accueil principal. */
    MENU,

    /** écran de sélection de la difficulté(avant de lancer une partie). */
    DIFFICULTY,
    
    OPTIONS,

    /** Partie en cours. */
    PLAYING,

    /** Jeu mis en pause (menu pause affiché par-dessus le jeu). */
    PAUSED,

    /** Le joueur est mort écran de game over. */
    GAME_OVER,

    /** Le boss a été vaincu écran de victoire. */
    VICTORY
}
