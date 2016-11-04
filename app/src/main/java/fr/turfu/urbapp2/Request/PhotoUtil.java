/**
 * Classe PhotoUtil
 * -----------------------------------------------------------------
 * Conversion d'images en bytes[] et vice versa pour les transferts
 * de photos entre le serveur et l'appli android
 */

package fr.turfu.urbapp2.Request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class PhotoUtil {

    /**
     * Obtenir une photo avec le path
     *
     * @param path path de la photo
     * @return photo
     */
    public static Bitmap getPhotoBitmap(String path) {
        File imgFile = new File(Environment.getExternalStorageDirectory(), path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        return myBitmap;
    }

    /**
     * Récupérer la taille d'une image (= le nombre de bytes)
     *
     * @param bitmap Bitmap
     * @return length
     */
    public static int getLength(Bitmap bitmap) {
        return bitmap.getWidth() * bitmap.getHeight();
    }

    /**
     * Convertir un bitmap en byte array
     *
     * @param bitmap Bitmap à convertir
     * @return tableau de bytes correspondant
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] pixels = stream.toByteArray();
        return pixels;
    }

}
