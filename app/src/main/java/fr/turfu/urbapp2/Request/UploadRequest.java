/**
 * Classe UploadRequest
 * -----------------------------------------------
 * Requête exécutée lorsqu'un utilisateur ajoute une nouvelle photo.
 * Elle sert à envoyer cette photo sur le serveur
 */

package fr.turfu.urbapp2.Request;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadRequest extends AsyncTask<String, Void, String> {

    /**
     * Activité appelante
     */
    private Activity a;

    /**
     * Path de la photo à envoyer
     */
    private String photopath;

    public UploadRequest(Activity a, String path) {
        this.a = a;
        this.photopath = path;
    }

    @Override
    protected String doInBackground(String[] url) {
        try {
            Log.v("Upload de la photo:", photopath);
            //Photo
            Bitmap bitmap = PhotoUtil.getPhotoBitmap(photopath);
            byte[] pixels = PhotoUtil.bitmapToBytes(bitmap);

            //Constantes
            String attachmentName = photopath;
            String attachmentFileName = photopath;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            URL urlObj = new URL(url[0]);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) urlObj.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);

            //Content wrapper
            DataOutputStream request = new DataOutputStream(
                    httpUrlConnection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
            request.writeBytes(crlf);

            //Pixels
            String b = Base64.encodeToString(pixels, Base64.DEFAULT);
            request.writeChars(b);

            //End content wrapper
            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            request.flush();
            request.close();

            int response = httpUrlConnection.getResponseCode();
            Log.d("RESPONSE ", "The response is: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


}



