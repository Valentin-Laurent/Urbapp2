/**
 * Classe DataElemRequest
 * -----------------------------------------------
 * Requête exécutée pour récupérer la liste des matériaux et celle des types d'élément,
 * utiles à la définition des éléments sur une photo.
 */


package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.StringTokenizer;

import fr.turfu.urbapp2.DB.ElementBDD;
import fr.turfu.urbapp2.DB.ElementType;
import fr.turfu.urbapp2.DB.Material;
import fr.turfu.urbapp2.ElementDefinitionPopUp;

public class DataElemRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante
     */
    private Activity a;

    public DataElemRequest(Activity a) {
        this.a = a;
    }

    @Override
    protected String doInBackground(String[] url) {
        return Util.httpRequest(url[0]);
    }

    @Override
    protected void onPostExecute(String result) {

        //On récupère les données
        StringTokenizer st = new StringTokenizer(result, "#");
        String types = st.nextToken();
        String mater = st.nextToken();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        ElementType[] et = gson.fromJson(types, ElementType[].class);
        Material[] mat = gson.fromJson(mater, Material[].class);

        //On les enregistre dans la base de données
        ElementBDD ebdd = new ElementBDD(a);
        ebdd.open();
        ebdd.insertTypes(et);
        ebdd.insertMater(mat);
        ebdd.close();

        //On affiche
        ((ElementDefinitionPopUp) a).display();

    }

}



