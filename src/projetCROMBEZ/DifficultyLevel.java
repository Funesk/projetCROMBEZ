package projetCROMBEZ;

/**
 * Enumération des niveaux de difficulté disponibles dans le jeu.
 * Chaque niveau modifie les multiplicateurs de vie, de dégats
 * et la fréquence d'apparition des ennemis.
 */
public enum DifficultyLevel {

    /** Ennemis plus faibles, moins nombreux  idéal pour découvrir le jeu. */
    EASY("Facile"),

    /** Difficulté équilibrée  valeurs de base. */
    NORMAL("Normal"),

    /** Ennemis plus réistants et agressifs  pour les joueurs expérimentés. */
    HARD("Difficile");

    // Libellé affiché dans l'interface
    private final String label;

    DifficultyLevel(String label) {
        this.label = label;
    }

    /** Retourne le nom lisible pour l'affichage à l'écran. */
    public String getLabel() {
        return label;
    }
}
