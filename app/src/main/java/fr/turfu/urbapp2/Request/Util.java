/**
 * Classe Util
 * ------------------------
 * Exécution des requetes http et récupération de la réponse json
 */
package fr.turfu.urbapp2.Request;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Util {

    /**
     * Lecture de la réponse à une requête : conversion inputStream en String
     *
     * @param stream inputstream renvoyé par une requete http
     * @return String json
     * @throws IOException
     */
    public static String readIt(InputStream stream) throws IOException {
        Scanner s = new Scanner(stream);
        String r = "";
        String line  = s.hasNext() ? s.nextLine() : null;
        while (line != null) {
            r = r + line;
            line = s.hasNext() ? s.nextLine() : null;
        }
        return r;
    }


    /**
     * Méthode pour envoyer une requete http
     *
     * @param url Url à requeter
     * @return Réponse
     */
    public static String httpRequest(String url) {
        String contentAsString = "";
        try {
            URL urlObj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            int response = urlConnection.getResponseCode();
            Log.d("RESPONSE ", "The response is: " + response);
            InputStream is = urlConnection.getInputStream();

            // Convert the InputStream into a string
            contentAsString = Util.readIt(is);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentAsString;
    }


}
