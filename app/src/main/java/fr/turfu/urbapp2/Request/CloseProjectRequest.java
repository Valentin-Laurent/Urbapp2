/**
 * Classe CloseProjectRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur ferme son projet.
 * On synchronise alors ce projet avec le serveur : on envoie les données
 * du projet, on les sauvgarde dans la base du serveur et on ferme le projet (on le rend available
 * pour les autres utilisateurs).
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.Sync;

public class CloseProjectRequest extends AsyncTask<String, Void, String> {

    private Activity a;

    public CloseProjectRequest(Activity a) {
        this.a = a;
    }

    @Override
    protected String doInBackground(String[] url) {
        return Util.httpRequest(url[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null && !result.equals("")) {
            //On enregistre les données en local
            Data d = new Data(result);
            d.saveLocal(a);
            //On met à jour la vue de l'activité
            ((Sync) a).updateView();
        }
    }
}
