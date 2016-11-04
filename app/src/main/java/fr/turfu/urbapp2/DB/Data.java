/**
 * CLASSE Data
 * ----------------------------------------------------------------------------------------------
 * Représentation des données relatives à un projet.
 * Cette classe est utilisée pour regrouper toutes les données d'un projet
 * et les envoyer sur le serveur.
 */
package fr.turfu.urbapp2.DB;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class Data {

    /**
     * Projet
     */
    private Project project;
    /**
     * Gpsgeom du projet
     */
    private GpsGeom project_gpsgeom;
    /**
     * Photos du projet
     */
    private List<Photo> photos;
    /**
     * Gpsgeom des photos
     */
    private List<GpsGeom> photos_gpsgeom;
    /**
     * Elements des photos
     */
    private ArrayList<ArrayList<Element>> elements;
    /**
     * Pixelgeom des éléments
     */
    private ArrayList<ArrayList<PixelGeom>> elements_pixelgeom;

    /**
     * Constructeur vide
     */
    public Data() {
    }

    /**
     * Constructeur à partir d'une chaîne json
     *
     * @param json Texte représentant la data à construire
     */
    public Data(String json) {
        Data d = DataFromJson(json);
        project = d.project;
        project_gpsgeom = d.project_gpsgeom;
        photos = d.photos;
        photos_gpsgeom = d.photos_gpsgeom;
        elements = d.elements;
        elements_pixelgeom = d.elements_pixelgeom;
    }

    /**
     * Conversion d'une data en json
     *
     * @return Teste json
     */
    public String DataToJson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(this);
    }

    /**
     * Conversion d'un texte json en Data
     *
     * @param json texte json
     * @return Data object
     */
    public static Data DataFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Log.v("data", json);
        return gson.fromJson(json, Data.class);
    }


    /**
     * Enregistrer les données d'un projet dans la base locale
     *
     * @param a Activité
     */
    public void saveLocal(Activity a) {

        //On vide la base de données locale
        ProjectBDD pbdd = new ProjectBDD(a);
        pbdd.open();
        pbdd.cleanDataBase();


        //On ajoute le gpsgeom du projet
        pbdd.insertGpsGeom(project_gpsgeom);

        //On ajoute les autres gpsgeom
        for (GpsGeom gps : photos_gpsgeom) {
            pbdd.insertGpsGeom(gps);
        }

        //On ajoute les pixelsgeom des éléments
        ElementBDD ebdd = new ElementBDD(a);
        ebdd.open();
        for (ArrayList<PixelGeom> l : elements_pixelgeom) {
            for (PixelGeom pix : l) {
                ebdd.insertPixelGeom(pix);

            }
        }

        //On ajoute les éléments
        for (ArrayList<Element> l : elements) {
            for (Element e : l) {
                ebdd.insertElement(e);
            }
        }

        //On ajoute les photos
        PhotoBDD phbdd = new PhotoBDD(a);
        phbdd.open();
        for (Photo ph : photos) {
            phbdd.insert(ph);

        }

        //On ajoute le projet
        pbdd.insertwithId(project);

        //On ferme les connexions à la base locale
        pbdd.close();
        ebdd.open();
        phbdd.close();
    }

    /**
     * Récupérer toutes les données d'un projet
     *
     * @param pid Id du projet
     * @param a   Activité
     * @return Données du projet sous la forme d'une Data
     */
    public static Data ToData(long pid, Activity a) {
        Data d = new Data();

        //Projet
        ProjectBDD pbdd = new ProjectBDD(a);
        pbdd.open();
        Project project = pbdd.getProjectById(pid);
        d.project = project;

        //Projet gps geom
        GpsGeom projetgps = pbdd.getGpsGeomById(project.getGpsGeom_id());
        d.project_gpsgeom = projetgps;

        //Photos
        PhotoBDD phbdd = new PhotoBDD(a);
        phbdd.open();
        d.photos = phbdd.getPhotos(pid);


        //Photo gpsgeom
        List<GpsGeom> photogps = new ArrayList<>();
        for (Photo phot : d.photos) {
            photogps.add(pbdd.getGpsGeomById(phot.getGpsGeom_id()));
        }
        d.photos_gpsgeom = photogps;

        //Elements
        ElementBDD ebdd = new ElementBDD(a);
        ebdd.open();
        ArrayList<ArrayList<Element>> elem = new ArrayList<>();
        for (Photo ph : d.photos) {
            elem.add(ebdd.getElement(ph.getPhoto_id()));
        }
        d.elements = elem;

        //Pixel geom
        ArrayList<ArrayList<PixelGeom>> pixels = new ArrayList<>();
        for (ArrayList<Element> e : elem) {
            pixels.add(ebdd.getPixelGeom(e));
        }
        d.elements_pixelgeom = pixels;


        pbdd.close();
        phbdd.close();
        ebdd.close();

        return d;
    }


}
