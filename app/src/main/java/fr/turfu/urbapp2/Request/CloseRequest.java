/**
 * Classe CloseRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur ferme son projet et ne souhaite pas enregistrer les
 * modifications qu'il a faites.
 * On rend alors le projet available pour les autres utilisateurs.
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

public class CloseRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante
     */
    private Activity a;

    public CloseRequest(Activity a) {
        this.a = a;
    }

    @Override
    protected String doInBackground(String[] url) {
        return Util.httpRequest(url[0]);
    }
}
