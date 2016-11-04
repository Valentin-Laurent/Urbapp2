/**
 * Classe ListProjectsRequest
 * -----------------------------------------------
 * Requête exécutée à l'ouverture de l'application.
 * On récupère la liste des projets du serveur et on l'affiche.
 */

package fr.turfu.urbapp2.Request;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.MainActivity;

public class ListProjectsRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante (ici MainActivity)
     */
    private MainActivity ma;

    public ListProjectsRequest(MainActivity ma) {
        this.ma = ma;
    }


    @Override
    protected String doInBackground(String[] url) {
        return Util.httpRequest(url[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        ArrayList<Project> p = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        if (result != null && !result.equals("")) {

            //On récupère la liste des projets
            Project[] pr = gson.fromJson(result, Project[].class);
            for (Project pro : pr) {
                p.add(pro);
            }
        }

        //On charge cette liste dans l'actitité
        MainActivity.projects = p;

        //On l'affiche
        ma.refreshList();
    }
}



