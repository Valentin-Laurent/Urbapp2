/**
 * Classe DrawView
 * ---------------------------------
 * Vue utilisée pour dessiner des polygones (éléments) sur une photo. Gestion des tracés.
 */
package fr.turfu.urbapp2.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import fr.turfu.urbapp2.DB.Element;
import fr.turfu.urbapp2.DB.ElementBDD;
import fr.turfu.urbapp2.DB.PixelGeom;
import fr.turfu.urbapp2.ElementDefinitionActivity;

public class DrawView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF990000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    /**
     * Attributs utilisés pour la conversion de String en Geometry
     */
    private static GeometryFactory gf = new GeometryFactory();
    private static WKTReader wktr = new WKTReader(gf);

    /**
     * Dernier point touché par l'utilisateur
     */
    private float precx;
    private float precy;

    /**
     * Taille de l'image
     */
    private int w;
    private int h;

    /**
     * Constructeur
     *
     * @param context  Contexte de l'activité
     * @param w        largeur de la photo
     * @param h        hauteur de la photo
     * @param photo_id Identifiant de la photo
     */
    public DrawView(Context context, int w, int h, long photo_id) {
        super(context);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        drawCanvas = new Canvas(canvasBitmap);
        this.w = w;
        this.h = h;
        precx = 0;
        precy = 0;
        refresh();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Méthode onDraw
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);

    }

    /**
     * Méthode onTouch : lorsque l'utilisateur touche la vue :
     * si il est en mode "tracé", on place un point. Sinon, il est en mode sélection,
     * si le point touché est dans un polygone, on séléctionne celui-ci.
     *
     * @param event Evènement : touche de l'utilisateur
     * @return boolée true // Par défaut
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Point touché
        float touchX = event.getX();
        float touchY = event.getY();
        Point p = new Point((int) touchX, (int) touchY);

        if (ElementDefinitionActivity.pen) { // Si mode tracé
            //On trace le point
            drawPoint(p);
        } else { //Si mode sélection

            // Si le point touché, n'est pas "trop près" du précédent
            //(lorsqu'un utilisateur reste trop longtemp appuyé, la méthode onTouch va être
            // appelée plusieurs fois. La condition ci-dessous évite alors qu'une zone se sélectionne, déselectionne
            // et ainsi de suite plusieurs fois.)
            if (distance(new Point((int) touchX, (int) touchY), new Point((int) precx, (int) precy)) > 13) {
                select(p);
            }
        }

        //On sauvegarde le point touché
        precx = touchX;
        precy = touchY;

        //On affiche
        invalidate();
        return true;
    }

    /**
     * Dessin d'un point sur l'image :
     * Si le point est un sommet de polygone, on le relie au précédent et si il ferme le polygone, on crée le polygone
     *
     * @param p Point à dessiner
     */
    public void drawPoint(Point p) {

        //Si le point n'est pas déjà placé (ck method notAlreadyDrawn)
        if (notAlreadyDraw(p)) {

            //On regarde si le point ferme un polygone ou non
            int n = drawPolygone(p);

            //Si le point ne match pas avec la fermeture d'un polygone
            if (n == -1) {
                //On place un point
                drawCanvas.drawCircle(p.x, p.y, 7, drawPaint);

                //On relie ce point avec le précédent, si précédent, il y a
                drawLine(p);

                //On enregistre cette action
                ElementDefinitionActivity.actions.add("POINT");
                ElementDefinitionActivity.newPoints.add(new Point((int) p.x, (int) p.y));

                //Si le point marque la fin d'un polygone
            } else {
                //On récupère le premier point du polygone pour plus de précision
                p = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - n);

                //On relie ce point avec le précédent, si précédent, il y a
                drawLine(p);

                //On remplit le polygone
                fillPolygone(n);

                //On enregistre cette action
                ElementDefinitionActivity.actions.add("POLYGONE");
                ElementDefinitionActivity.newPoints.add(p);

                String pix = polygoneToString(n);
                PixelGeom pixel = new PixelGeom();
                pixel.setPixelGeom_the_geom(pix);

                ElementBDD ebdd = new ElementBDD(ElementDefinitionActivity.context);
                ebdd.open();
                long id = ebdd.getMaxPixelGeomId() + 1;
                ebdd.close();

                if (ElementDefinitionActivity.newPolygones.isEmpty()) {
                    id = id + 1;
                } else {
                    id = ElementDefinitionActivity.newPolygones.get(ElementDefinitionActivity.newPolygones.size() - 1).getPixelGeomId() + 1;
                }
                pixel.setPixelGeomId(id);
                ElementDefinitionActivity.newPolygones.add(pixel);
            }
        }
    }

    /**
     * Méthode pour vérifier que le point courant n'est pas trop proche du dernier point tracé (si oui, c'est que l'utilisateur est resté trop longtemp appuyé)
     *
     * @param p Point p
     * @return Booléen
     */
    public boolean notAlreadyDraw(Point p) {
        //On vérifie que le point tracé n'est pas juste à côté du précédent
        return (ElementDefinitionActivity.newPoints.size() == 0) || (distance(ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - 1), p) > 13);
    }

    /**
     * Tracé d'une ligne entre le point en paramètre et le point précédent, si précédent il y a
     *
     * @param p
     */
    public void drawLine(Point p) {

        int x = p.x;
        int y = p.y;

        //Dernière action effectuée
        int i = ElementDefinitionActivity.actions.size() - 1;

        //On remonte les actions jusqu'à tomber sur un polygone ou un point
        while (i >= 0 && (ElementDefinitionActivity.actions.get(i).equals("ELEMENT") || ElementDefinitionActivity.actions.get(i).equals("GROUP"))) {
            i--;
        }

        //Si on est tombé sur un point, on trace une ligne
        if (i >= 0 && ElementDefinitionActivity.newPoints.size() > 0 && ElementDefinitionActivity.actions.get(i).equals("POINT")) {
            Point p1 = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - 1);
            drawPaint.setStrokeWidth(5);
            drawCanvas.drawLine(x, y, p1.x, p1.y, drawPaint);
        }

    }

    /**
     * Méthode pour tester si un point correspond à la fermeture d'un polygone ou non.
     * Si oui, elle retourne un entier qui correspond à l'index (en partant de la fin de la liste) du premier sommet du polygone dans la liste des points.
     * Si non, elle retourne -1
     *
     * @param a
     * @return
     */
    public int drawPolygone(Point a) {
        int x = a.x;
        int y = a.y;
        int n = 0;
        int i = ElementDefinitionActivity.actions.size() - 1;

        //On parcourt la liste des actions. Tant que c'est des elements, on passe
        while (i >= 0 && ElementDefinitionActivity.actions.get(i).equals("ELEMENT")) {
            i--;
        }
        // On remonte au premier point du polygone courant
        while (i >= 0 && ElementDefinitionActivity.actions.get(i).equals("POINT")) {
            i--;
            n++;
        }

        if (n > 0) { // si le point dessiné fait partie des sommet d'un polygone
            Point p = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - n);
            return distance(p, new Point((int) x, (int) y)) < 20 ? n : -1; // On regarde si la distance entre ce point et le premier sommet du polygone est faible
        } else {
            return -1;
        }
    }


    /**
     * Méthode pour remplir un polygone
     *
     * @param n Entier correspondant à l'index du premier point du polygone (en partant de la fin de la liste) dans la liste des points
     */
    public void fillPolygone(int n) {

        //Mise en place du style de remplissage
        setFillStyle();

        //Création du polygone
        Path path = new Path();
        for (int i = 1; i <= n; i++) {
            int x = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - i).x;
            int y = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - i).y;
            if (i == 1) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        //Tracé du polygone
        drawCanvas.drawPath(path, canvasPaint);

        //Mise en place du style de tracé (contour)
        setStrokeStyle();
    }

    /**
     * Distance euclidienne entre deux points
     *
     * @param p Point 1
     * @param q Point 2
     * @return Distance
     */
    public static double distance(Point p, Point q) {
        return Math.sqrt((p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y));
    }

    /**
     * Conversion d'un polygone en String pour enregistrement dans la base de données
     *
     * @param n
     */
    public String polygoneToString(int n) {
        String s = "POLYGON((";

        //Ajout des sommets
        for (int i = 1; i <= n; i++) {
            int x = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - i).x;
            int y = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - i).y;
            s = s + x + " ";
            s = s + y + ", ";
        }

        //Ajout du premier sommet pour fermer le polygone
        int x = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - 1).x;
        int y = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - 1).y;
        s = s + x + " ";
        s = s + y + " ";
        s = s + "))";

        return s;
    }


    /**
     * Tester si un point est dans une géométrie
     *
     * @param pg
     * @param p
     * @return
     */
    public boolean isInside(PixelGeom pg, Point p) {

        boolean b = false;

        //Obtention du polygone
        ArrayList<Geometry> geoms = getPolygones(pg);

        //Transformation du point en géométrie
        Coordinate coord = new Coordinate(p.x, p.y);
        com.vividsolutions.jts.geom.Point geomPoint = gf.createPoint(coord);

        //On regarde si le point est dans une géométrie
        for (Geometry g : geoms) {
            b = b || g.contains(geomPoint);
        }

        return b;
    }

    /**
     * Sélection d'une forme
     *
     * @param p
     */
    public void select(Point p) {

        //Pixelgeom qui contient le point
        PixelGeom pix = null;
        //index : 1 si pix est un polygone enregistré dans la base de données
        // (il appartient au tableau polygones) et 2 sinon (il appartient au tableau newPolygones)
        int tab = 0;

        //On cherche parmi les pixelgeom, si il y en a un qui contient le point
        for (PixelGeom pg : ElementDefinitionActivity.polygones) {
            if (isInside(pg, p)) {
                pix = pg;
                tab = 1;
            }
        }
        for (PixelGeom pg : ElementDefinitionActivity.newPolygones) {
            if (isInside(pg, p)) {
                pix = pg;
                tab = 2;
            }
        }

        //Si le point est dans un pixelgeom
        if (pix != null) {
            int index = 0;

            //On sélection ce pixelgeom
            if (tab == 1) {
                index = ElementDefinitionActivity.polygones.indexOf(pix);
                pix.selected = !pix.selected;
                ElementDefinitionActivity.polygones.set(index, pix);
            } else {
                index = ElementDefinitionActivity.newPolygones.indexOf(pix);
                pix.selected = !pix.selected;
                ElementDefinitionActivity.newPolygones.set(index, pix);
            }
            //On rafraîchit la vue pour faire apparaitre la selection
            erase();
            refresh();

        }

    }

    /**
     * Méthode pour tracer un polygone, qu'il soit sélectionné ou non
     *
     * @param pg Pixelgeom à tracer
     */
    public void drawFilledPolygone(PixelGeom pg) {

        //Mise en place du style de remplissage
        if (pg.selected) {
            setFillSelectedStyle();
        } else {
            setFillStyle();
        }

        //Définition du polygone
        Path path = new Path();
        for (Geometry g : getPolygones(pg)) {
            Coordinate[] coord = g.getCoordinates();
            for (int i = 0; i < coord.length; i++) {
                float x = (float) coord[i].x;
                float y = (float) coord[i].y;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            //Tracé du polygone
            drawCanvas.drawPath(path, canvasPaint);
        }

        setStrokeStyle();
    }


    /**
     * Tracé de tous les points, lignes et polygones existants
     */
    public void refresh() {
        //Tracé des points et des lignes
        Point temp = null;
        boolean init = true;
        for (int i = 0; i < ElementDefinitionActivity.newPoints.size(); i++) {
            Point p = ElementDefinitionActivity.newPoints.get(i);
            drawCanvas.drawCircle(p.x, p.y, 7, drawPaint);
            if (init) {
                temp = p;
                init = false;
            } else {
                if (i > 0) {
                    Point q = ElementDefinitionActivity.newPoints.get(i - 1);
                    drawCanvas.drawLine(p.x, p.y, q.x, q.y, drawPaint);
                }
                if (temp == p) {
                    init = true;
                }
            }
        }

        //Tracé des polygones sauvegardés
        for (PixelGeom pg : ElementDefinitionActivity.polygones) {
            drawFilledPolygone(pg);
            drawBorder(pg);
        }

        //Tracé des polygones non sauvegardés
        for (PixelGeom pg : ElementDefinitionActivity.newPolygones) {
            drawFilledPolygone(pg);
        }
    }


    /**
     * Tracé de la bordure et des sommets d'un polygone
     *
     * @param pg Polygone
     */
    public void drawBorder(PixelGeom pg) {

        //Path pour le contour du polygone
        Path path = new Path();
        //Tracé du polygone
        for (Geometry g : getPolygones(pg)) {
            Coordinate[] coord = g.getCoordinates();
            for (int i = 0; i < coord.length; i++) {
                float x = (float) coord[i].x;
                float y = (float) coord[i].y;
                //Tracé du sommet
                drawCanvas.drawCircle(x, y, 7, drawPaint);
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            //Tracé du contour
            drawCanvas.drawPath(path, canvasPaint);
        }
    }


    /**
     * Méthode pour effacer tous les tracés de la vue
     */
    public void erase() {
        //Tracé d'un rectangle sur la vue pour masquer les tracés
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        canvasPaint.setColor(Color.TRANSPARENT);
        drawCanvas.drawRect(0, 0, canvasBitmap.getWidth(), canvasBitmap.getHeight(), drawPaint);

        //Réinitialisation des objets pour le tracé
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    /**
     * Méthode pour annuler toutes les modifications non enregistrées
     */
    public void cancelAll() {
        //On vide les listes
        ElementDefinitionActivity.newElements.clear();
        ElementDefinitionActivity.newPoints.clear();
        ElementDefinitionActivity.newPolygones.clear();
        ElementDefinitionActivity.actions.clear();

        //On nettoie le vue
        erase();

        //On retrace ce qui a été sauvegardé
        refresh();
    }

    /**
     * Méthode pour annuler la dernière action
     */
    public void cancelLast() {
        //Dernière action effectuée
        String lastAct = ElementDefinitionActivity.actions.get(ElementDefinitionActivity.actions.size() - 1);

        //On supprime la dernière action
        ElementDefinitionActivity.actions.remove(ElementDefinitionActivity.actions.size() - 1);

        switch (lastAct) {
            case "POINT":
                ElementDefinitionActivity.newPoints.remove(ElementDefinitionActivity.newPoints.size() - 1);
                break;
            case "POLYGONE":
                ElementDefinitionActivity.newPoints.remove(ElementDefinitionActivity.newPoints.size() - 1);
                ElementDefinitionActivity.newPolygones.remove(ElementDefinitionActivity.newPolygones.size() - 1);
                break;
            case "ELEMENT":
                ElementDefinitionActivity.newElements.remove(ElementDefinitionActivity.newElements.size() - 1);
                break;
            case "GROUP":
                PixelGeom last = ElementDefinitionActivity.newPolygones.get(ElementDefinitionActivity.newPolygones.size() - 1);
                degroup(last);
                break;
            default:
                //On récupère le nombre de polygones composant le multi
                StringTokenizer st = new StringTokenizer(lastAct, "#");
                st.nextToken();
                int n = Integer.parseInt(st.nextToken());

                //Liste des polygones
                ArrayList<PixelGeom> polys = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    polys.add(ElementDefinitionActivity.newPolygones.get(ElementDefinitionActivity.newPolygones.size() - i));
                }
                //Group
                group(polys);
                break;
        }
        //On nettoie le vue
        erase();

        //On retrace
        refresh();
    }


    /**
     * Groupement de plusieurs polygones
     *
     * @param selectedGeom liste des polygones à grouper
     */
    public void group(ArrayList<PixelGeom> selectedGeom) {

        //On supprime les polygones en questions
        Iterator<PixelGeom> iterator = ElementDefinitionActivity.polygones.iterator();
        while (iterator.hasNext()) {
            PixelGeom p = iterator.next();
            for (PixelGeom pi : selectedGeom) {
                if (p.getPixelGeomId() == pi.getPixelGeomId()) {
                    iterator.remove();
                }
            }
        }
        Iterator<PixelGeom> iterator1 = ElementDefinitionActivity.newPolygones.iterator();
        ArrayList<Integer> toerase = new ArrayList<>();
        int j = -1;
        while (iterator1.hasNext()) {
            PixelGeom p = iterator1.next();
            j++;
            long id = p.getPixelGeomId();
            boolean del = false;
            for (PixelGeom pi : selectedGeom) {
                if (!del && id == pi.getPixelGeomId()) {
                    toerase.add(j);
                    iterator1.remove();
                    del = true;
                }
            }
        }

        //On supprime les actions corespondant à la création de ces polygones
        Iterator<String> it = ElementDefinitionActivity.actions.iterator();
        int k = -1;
        while (it.hasNext()) {
            String s = it.next();
            if (s.equals("POLYGON")) {
                k++;
                for (int a : toerase) {
                    if (k == a) {
                        it.remove();
                    }
                }
            }
        }

        //On supprime les éléments qui ont des pixelgeom à grouper
        Iterator<Element> iterator3 = ElementDefinitionActivity.newElements.iterator();
        toerase.clear();
        while (iterator3.hasNext()) {
            Element e = iterator3.next();
            j++;
            long id = e.getPixelGeom_id();
            boolean del = false;
            for (PixelGeom pi : selectedGeom) {
                if (!del && id == pi.getPixelGeomId()) {
                    toerase.add(j);
                    iterator3.remove();
                    del = true;
                }
            }
        }

        Iterator<Element> iterator4 = ElementDefinitionActivity.elements.iterator();
        while (iterator4.hasNext()) {
            Element e = iterator4.next();
            for (PixelGeom pi : selectedGeom) {
                if (e.getPixelGeom_id() == pi.getPixelGeomId()) {
                    iterator4.remove();
                }
            }
        }

        //On supprime les actions corespondant à la création de ces  element
        Iterator<String> it2 = ElementDefinitionActivity.actions.iterator();
        k = -1;
        while (it2.hasNext()) {
            String s = it2.next();
            if (s.equals("ELEMENT")) {
                k++;
                for (int a : toerase) {
                    if (k == a) {
                        it2.remove();
                    }
                }
            }
        }

        //On ajoute un multipolygone
        ArrayList<String> selectedGeomConvert = new ArrayList<>();
        for (PixelGeom pg : selectedGeom) {
            if (pg.getPixelGeom_the_geom().charAt(0) == 'P') {
                selectedGeomConvert.add(pg.getPixelGeom_the_geom());
            } else {//C'est une collection de polygones, il faut les récupérer un par un
                String geom = pg.getPixelGeom_the_geom();
                geom = geom.substring(13, geom.length() - 1);
                StringTokenizer st = new StringTokenizer(geom, "(");
                while (st.hasMoreTokens()) {
                    String poly = st.nextToken();
                    poly = poly.substring(0, poly.length() - 2);
                    poly = "POLYGON((" + poly + "))";
                    selectedGeomConvert.add(poly);
                }
            }
        }

        //On forme un multipolygon avec la liste des polygones
        String s = "MULTIPOLYGON (";
        for (int i = 0; i < selectedGeomConvert.size(); i++) {
            String poly = selectedGeomConvert.get(i).substring(8, selectedGeomConvert.get(i).length() - 1);
            s = s + poly;
            if (i != selectedGeomConvert.size() - 1) {
                s = s + ",";
            }
        }
        s = s + ")";

        //On crée un nouveau pixelgeom avec ce multipolygone
        PixelGeom mult = new PixelGeom();
        mult.setPixelGeom_the_geom(s);
        mult.setSelected(true);

        // Id pour le nouveau pixelgeom
        ElementBDD ebdd = new ElementBDD(ElementDefinitionActivity.context);
        ebdd.open();
        long id = ebdd.getMaxPixelGeomId() + 1;
        ebdd.close();

        if (ElementDefinitionActivity.newPolygones.isEmpty()) {
            id = id + 1;
        } else {
            id = ElementDefinitionActivity.newPolygones.get(ElementDefinitionActivity.newPolygones.size() - 1).getPixelGeomId() + 1;
        }
        mult.setPixelGeomId(id);

        //Ajout du nouveau pixelgeom
        ElementDefinitionActivity.newPolygones.add(mult);
    }

    /**
     * A partir d'un pixelgeom, on récupère tous les polygones formant sa géométrie
     *
     * @param pg pixelgeom
     * @return liste de polygones
     */
    public ArrayList<Geometry> getPolygones(PixelGeom pg) {
        ArrayList<Geometry> list = new ArrayList<>();
        String geom = pg.getPixelGeom_the_geom();
        try {
            if (geom.charAt(0) == 'P') { //C'est juste un polygone, il suffit de le convertir en géométrie
                Log.v("geom", geom);
                Geometry poly = wktr.read(geom);
                list.add(poly);
            } else { //C'est une collection de polygones, il faut les récupérer un par un
                geom = geom.substring(13, geom.length() - 1);
                StringTokenizer st = new StringTokenizer(geom, "(");

                while (st.hasMoreTokens()) {
                    String poly = st.nextToken();
                    if (poly.substring(poly.length() - 2, poly.length()).equals("),")) {
                        poly = poly.substring(0, poly.length() - 2);
                    } else {
                        poly = poly.substring(0, poly.length() - 1);
                    }
                    poly = "POLYGON((" + poly + "))";
                    Log.v("geom", poly);
                    Geometry geopoly = wktr.read(poly);
                    list.add(geopoly);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Méthode pour dégrouper un pixelgeom : éclatement d'un multipolygone
     *
     * @param pix pixelgeom
     * @return nombre de polygones obtenus après éclatement
     */
    public int degroup(PixelGeom pix) {
        //Suppression du multipolygone
        ElementDefinitionActivity.newPolygones.remove(ElementDefinitionActivity.newPolygones.size() - 1);

        //Ajout des polygones composants le multipolygone
        String mult = pix.getPixelGeom_the_geom();
        mult = mult.substring(13, mult.length() - 1);
        StringTokenizer st = new StringTokenizer(mult, "(");
        int nbPoly = 0;
        while (st.hasMoreTokens()) {
            nbPoly++;
            String poly = st.nextToken();
            poly = poly.substring(0, poly.length() - 2);
            poly = "POLYGON((" + poly + "))";
            PixelGeom pg = new PixelGeom();
            pg.selected = true;
            pg.setPixelGeom_the_geom(poly);

            ElementBDD ebdd = new ElementBDD(ElementDefinitionActivity.context);
            ebdd.open();
            long id = ebdd.getMaxPixelGeomId() + 1;
            ebdd.close();
            if (ElementDefinitionActivity.newPolygones.isEmpty()) {
                id = id + 1;
            } else {
                id = ElementDefinitionActivity.newPolygones.get(ElementDefinitionActivity.newPolygones.size() - 1).getPixelGeomId() + 1;
            }
            pg.setPixelGeomId(id);

            ElementDefinitionActivity.newPolygones.add(pg);
        }
        return nbPoly;
    }

    /**
     * Mise en place du style de remplissage pour un polygone non sélectionné
     */
    public void setFillStyle() {
        canvasPaint.setStyle(Paint.Style.FILL);
        canvasPaint.setARGB(80, 255, 215, 215);

    }

    /**
     * Mise en place du style de tracé
     */
    public void setStrokeStyle() {
        canvasPaint.setStyle(Paint.Style.STROKE);
        canvasPaint.setColor(paintColor);
    }

    /**
     * Mise en place du style de remplissage pour un polygone sélectionné
     */
    public void setFillSelectedStyle() {
        canvasPaint.setStyle(Paint.Style.FILL);
        canvasPaint.setARGB(80, 230, 0, 0);
    }
}
