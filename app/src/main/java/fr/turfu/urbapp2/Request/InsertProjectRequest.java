/**
 * Classe InsertProjectRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur crée un nouveau projet.
 * On ajoute alors ce projet sur le serveur : on envoie les données
 * du projet, on les sauvgarde dans la base du serveur et on récupère ces données.
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.Sync;

public class InsertProjectRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante
     */
    private Activity a;

    public InsertProjectRequest(Activity a) {
        this.a = a;
    }

    @Override
    protected String doInBackground(String[] url) {
        return Util.httpRequest(url[0]);
    }

    @Override
    protected void onPostExecute(String result) {

        if (result != null && !result.equals("")) {
            //Synchronisation des données avec la base sqllite
            Data d = new Data(result);
            d.saveLocal(a);
            //Mise à jour de la vue de l'activité
            ((Sync) a).updateView();
        }
    }

}



