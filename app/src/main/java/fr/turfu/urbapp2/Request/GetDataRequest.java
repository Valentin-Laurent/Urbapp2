/**
 * Classe GetDataRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur ouvre un projet.
 * On récupère les données du projet qui sont sur le serveur et on les enregistre.
 * On bloque aussi l'accès à ce projet pour les autres utilisateurs.
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.ProjectOpenActivity;

public class GetDataRequest extends AsyncTask<String, Void, String> {

    private Activity a;

    public GetDataRequest(Activity a) {
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
            //Ajout du projet à la base de données locale
            d.saveLocal(a);
            //Chargementdes données pour ouverture du projet
            ((ProjectOpenActivity) a).loadData();
        }
    }

}



