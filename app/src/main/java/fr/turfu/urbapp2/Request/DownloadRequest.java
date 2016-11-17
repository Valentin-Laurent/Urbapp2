/**
 * Classe DownloadRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur ouvre une photo.
 * Elle sert à télécharger la photo depuis le serveur.
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

public class DownloadRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante
     */
    private Activity a;

    public DownloadRequest(Activity a) {
        this.a = a;
    }

    @Override
    protected String doInBackground(String[] url) {
        String u = url[0];
        //TODO
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        //TODO
    }
}





