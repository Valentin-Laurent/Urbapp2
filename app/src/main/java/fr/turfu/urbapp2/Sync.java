/**
 * Interface Sync
 * --------------------------------------
 * Méthodes appelées après l'exécution de requetes http.
 */
package fr.turfu.urbapp2;

public interface Sync {

    /**
     * Après l'exécution d'une requête http : On synchronise la base de données locale en ajoutant
     * les données renvoyeés par la requête et on met à jour la vue en appelant cette méthode.
     * Elle va alors mettre à jour les attributs de l'activité dans laquelle elle est appelée et rafraîchit
     * l'affichage
     */
    public void updateView();
}
