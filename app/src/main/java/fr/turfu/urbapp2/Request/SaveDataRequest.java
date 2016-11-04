/**
 * Classe SaveDataRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur sauvegarde son projet.
 * On synchronise alors ce projet avec le serveur : on envoie les données
 * du projet, on les sauvgarde dans la base du serveur et on récupère ces données
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.Sync;

public class SaveDataRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante
     */
    private Activity a;

    public SaveDataRequest(Activity a) {
        this.a = a;
    }

    @Override
    protected String doInBackground(String[] url) {
        return Util.httpRequest(url[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null && !result.equals("")) {
            Data d = new Data(result);
            //On enregistre les données dans la base locale
            d.saveLocal(a);
            //On rafraîchit l'affichage de l'activité et ses attributs
            ((Sync) a).updateView();
        }

    }
}
