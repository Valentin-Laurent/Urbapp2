/**
 * Classe Request
 * -----------------------------
 * Ensemble de méthodes statiques qui servent à exécuter des requêtes http pour transmettre
 * ou récupérer des données.
 */
package fr.turfu.urbapp2.Request;

import android.app.Activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.MainActivity;

public class Request {


    /**
     * Récupérer la liste des porjets
     *
     * @param ma Activité appelante
     */
    public static void getProjects(MainActivity ma) {
        String[] urls = new String[1];
        urls[0] = Constantes.urlServer + "ProjectListRequest";
        ListProjectsRequest r = new ListProjectsRequest(ma);
        r.execute(urls);
    }

    /**
     * Ouverture d'un projet : on récupère toutes les données du serveur
     *
     * @param a  Activité
     * @param id Id du projet à ouvrir
     */
    public static void openProject(Activity a, long id) {
        String[] urls = new String[1];
        urls[0] = Constantes.urlServer + "OpenProjectRequest?projectid=" + id;
        GetDataRequest r = new GetDataRequest(a);
        r.execute(urls);
    }

    /**
     * Sauvegarde d'un projet
     *
     * @param a Activité
     * @param d Data à sauvegarder
     */
    public static void saveProject(Activity a, Data d) {
        String[] urls = new String[1];
        try {
            urls[0] = Constantes.urlServer + "SaveDataRequest?data=" + URLEncoder.encode(d.DataToJson(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SaveDataRequest r = new SaveDataRequest(a);
        r.execute(urls);
    }

    /**
     * Sauvegarde d'un projet et fermeture de celui-ci
     *
     * @param a Activité
     * @param d Data à sauvegarder
     */
    public static void closeProject(Activity a, Data d) {
        String[] urls = new String[1];
        try {
            urls[0] = Constantes.urlServer + "CloseProjectRequest?data=" + URLEncoder.encode(d.DataToJson(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CloseProjectRequest r = new CloseProjectRequest(a);
        r.execute(urls);
    }


    /**
     * Fermeture d'un projet
     *
     * @param a  Activité appelante
     * @param id du projet à fermer
     */
    public static void close(Activity a, long id) {
        String[] urls = new String[1];
        urls[0] = Constantes.urlServer + "CloseRequest?projectid=" + id;
        CloseRequest r = new CloseRequest(a);
        r.execute(urls);
    }

    /**
     * Récupérer la liste des types d'élément et les materiaux
     *
     * @param a Activité appelante
     */
    public static void getDataElem(Activity a) {
        String[] urls = new String[1];
        urls[0] = Constantes.urlServer + "GetDataElemRequest";
        DataElemRequest r = new DataElemRequest(a);
        r.execute(urls);
    }


    /**
     * Insertion d'un nouveau projete dans la base de données du serveur
     *
     * @param a Activité appelante
     * @param d Données du projet
     */
    public static void insert(Activity a, Data d) {
        String[] urls = new String[1];
        try {
            urls[0] = Constantes.urlServer + "InsertProjectRequest?data=" + URLEncoder.encode(d.DataToJson(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        InsertProjectRequest r = new InsertProjectRequest(a);
        r.execute(urls);
    }


    /**
     * Upload d'une photo sur le serveur à son ajout dans un projet
     *
     * @param a         Activité appelante
     * @param photopath path de la photo à envoyer
     */
    public static void upload(Activity a, String photopath) {
        String[] urls = new String[1];
        urls[0] = Constantes.urlServer + "UploadPhotos";
        UploadRequest r = new UploadRequest(a, photopath);
        r.execute(urls);
    }

    /**
     * Téléchargement d'une photo du serveur
     *
     * @param a       Activité appelante
     * @param photoid Id de la photo à télécharger
     */
    public static void download(Activity a, long photoid) {
        String[] urls = new String[1];
        urls[0] = Constantes.urlServer + "DownloadPhoto?photoid=" + photoid;
        DownloadRequest r = new DownloadRequest(a);
        r.execute(urls);
    }

}
