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

import fr.turfu.urbapp2.DB.PixelGeom;
import fr.turfu.urbapp2.ElementDefinitionActivity;

/**
 * This class is used to draw the zones on the image
 */
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

    private static GeometryFactory gf = new GeometryFactory();
    private static WKTReader wktr = new WKTReader(gf);

    private float precx;
    private float precy;
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
     * Méthode on draw
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        Point p = new Point((int) touchX, (int) touchY);

        if (ElementDefinitionActivity.pen) {
            drawPoint(p);
        } else {
            if (distance(new Point((int) touchX, (int) touchY), new Point((int) precx, (int) precy)) > 13) {
                select(p);
            }
        }
        precx = touchX;
        precy = touchY;
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

        if (notAlreadyDraw(p)) {

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
                pixel.setPixelGeomId(ElementDefinitionActivity.polygones.size() + ElementDefinitionActivity.newPolygones.size() + 1);
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
     * Tracé d'une ligne entre le point en paramètre et e point précédent, si précédent il y a
     *
     * @param p
     */
    public void drawLine(Point p) {
        int x = p.x;
        int y = p.y;
        int i = ElementDefinitionActivity.actions.size() - 1;
        Log.v("i", ElementDefinitionActivity.actions.size() + "");
        while (i >= 0 && ElementDefinitionActivity.actions.get(i).equals("ELEMENT")) {
            i--;
        }
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

        //On parcout la liste des actions. Tant que c'est des elements, on passe
        while (i >= 0 && ElementDefinitionActivity.actions.get(i).equals("ELEMENT")) {
            i--;
        }
        // On remonte au premier point du polygone courant
        while (i >= 0 && ElementDefinitionActivity.actions.get(i).equals("POINT")) {
            i--;
            n++;
        }

        Log.v("OK", "OK");
        if (n > 0) { // si le point dessiné fait partie des sommet d'un polygone
            Point p = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - n);
            Log.v("dist", distance(p, new Point((int) x, (int) y)) + "");
            return distance(p, new Point((int) x, (int) y)) < 20 ? n : -1; // On regarde si la distance entre ce point et le premier sommet du polygone est faible
        } else {
            return -1;
        }

    }


    /**
     * Méthode pour remplir un polygone
     *
     * @param n Entiercorrespondant à l'index du premier point du polygone (en partant de la fin de la liste) dans la liste des points
     */
    public void fillPolygone(int n) {
        canvasPaint.setStyle(Paint.Style.FILL);

        canvasPaint.setARGB(80, 255, 255, 255);


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
        drawCanvas.drawPath(path, canvasPaint);
        canvasPaint.setStyle(Paint.Style.STROKE);
        canvasPaint.setColor(0xFF990000);
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
        for (int i = 1; i <= n; i++) {
            int x = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - i).x;
            int y = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - i).y;
            s = s + x + " ";
            s = s + y + ", ";
        }
        int x = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - 1).x;
        int y = ElementDefinitionActivity.newPoints.get(ElementDefinitionActivity.newPoints.size() - 1).y;
        s = s + x + " ";
        s = s + y + " ";

        s = s + "))";
        return s;
    }

/*
    public boolean isValidPolygon(int n) {
        //Sommets du polygone
        ArrayList<Point> pt = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            pt.add(newPoints.get(newPoints.size() - i));
        }

        //Verification : est ce que les segments se croisent
        int croisements = 0;
        for (int i = 0; i < pt.size() - 2; i++) {
            for (int j = i + 1; j < pt.size() - 1; j++) {
                if (intersect(pt.get(i), pt.get(i + 1), pt.get(j), pt.get(j + 1))) {
                    croisements++;
                }
            }

        }
        return croisements == 0;
    }
*/

    /**
     * Methode pour déterminer si les segment AB et CD se coupent
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
  /*  public boolean intersect(Point a, Point b, Point c, Point d) {

        //Calcul des droites
        float a1 = (a.x == b.x) ? 0 : (a.y - b.y) / (a.x - b.x);
        float b1 = a.y - a1 * a.x;

        float a2 = (c.x == d.x) ? 0 : (c.y - d.y) / (c.x - d.x);
        float b2 = c.y - a2 * c.x;

        //Abscisse de l'intersection
        Log.v("ax", a.x + "");
        Log.v("ay", a.y + "");
        Log.v("bx", b.x + "");
        Log.v("by", b.y + "");
        Log.v("cx", c.x + "");
        Log.v("cy", c.y + "");
        Log.v("dx", d.x + "");
        Log.v("dy", d.y + "");
        if (a1 == a2) {
            return false;
        } else {
            float x = (b1 - b2) / (a2 - a1);
            Log.v("x", x + "");
            float max1 = a.x > b.x ? a.x : b.x;
            float min1 = a.x < b.x ? a.x : b.x;
            float max2 = c.x > d.x ? c.x : d.x;
            float min2 = c.x < d.x ? c.x : d.x;
            return (max1 > x && x > min1) || (max2 > x && min2 < x);
        }

    }


    public void cancelPolygon(int n) {

        //Suppression des points
        int k = n;
        ArrayList<Point> pt = new ArrayList<>();
        while (k > 0) {
            pt.add(newPoints.get(newPoints.size() - 1));
            newPoints.remove(newPoints.size() - 1);
            k--;
        }
        Log.v("taille", newPoints.size() + "");

        //Suppression des actions
        k = n;
        int i = actions.size() - 1;
        while (k > 0) {
            if (actions.get(i).equals("POINT")) {
                actions.remove(i);
                i--;
                k--;
            } else {
                i--;
            }
        }

        //On efface les erreurs
        drawCanvas.drawColor(Color.TRANSPARENT);
        for (int z = 0; z < pt.size(); z++) {
            drawCanvas.drawCircle(pt.get(z).x, pt.get(z).y, 7, drawPaint);

            if (z < pt.size() - 1) {
                drawCanvas.drawLine(pt.get(z).x, pt.get(z).y, pt.get(z + 1).x, pt.get(z + 1).y, drawPaint);
            }
        }
    }*/

    /**
     * Tester si un point est dans une géométrie
     *
     * @param pg
     * @param p
     * @return
     */
    public boolean isInside(PixelGeom pg, Point p) {

        boolean b = false;
        try {
            //Obtention du polygone
            Geometry poly = wktr.read(pg.getPixelGeom_the_geom());

            //Transformation du point en géométrie
            Coordinate coord = new Coordinate(p.x, p.y);
            com.vividsolutions.jts.geom.Point geomPoint = gf.createPoint(coord);

            b = poly.contains(geomPoint);
            Log.v("b", b + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Sélection d'une forme
     *
     * @param p
     */
    public void select(Point p) {
        double area = 0;
        int index = -1;
        int tab = 0;

        try {
            for (PixelGeom pg : ElementDefinitionActivity.polygones) {

                if (isInside(pg, p) && (index == -1 || wktr.read(pg.getPixelGeom_the_geom()).getArea() < area)) {
                    area = wktr.read(pg.getPixelGeom_the_geom()).getArea();
                    tab = 1;
                    index = ElementDefinitionActivity.polygones.indexOf(pg);
                    Log.v("SELECT", pg.selected + "");
                }

            }
            for (PixelGeom pg : ElementDefinitionActivity.newPolygones) {
                Log.v("px", p.x + "");
                Log.v("py", p.y + "");
                Log.v("poly", pg.getPixelGeom_the_geom());

                if (isInside(pg, p) && (index == -1 || wktr.read(pg.getPixelGeom_the_geom()).getArea() < area)) {
                    area = wktr.read(pg.getPixelGeom_the_geom()).getArea();
                    tab = 2;
                    index = ElementDefinitionActivity.newPolygones.indexOf(pg);
                    Log.v("SELECT", pg.selected + "");
                }
            }

            if (index >= 0) {
                PixelGeom thePixel = null;
                if (tab == 1) {
                    ElementDefinitionActivity.polygones.get(index).selected = !ElementDefinitionActivity.polygones.get(index).selected;
                    thePixel = ElementDefinitionActivity.polygones.get(index);
                } else {
                    ElementDefinitionActivity.newPolygones.get(index).selected = !ElementDefinitionActivity.newPolygones.get(index).selected;
                    thePixel = ElementDefinitionActivity.newPolygones.get(index);
                }

                if (thePixel.selected) {
                    drawFilledPolygone(thePixel);
                } else {
                    erase();
                    refresh();
                }
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void drawFilledPolygone(PixelGeom pg) {
        try {
            if (pg.selected) {
                canvasPaint.setStyle(Paint.Style.FILL);
                canvasPaint.setARGB(80, 252, 217, 217);
            } else {
                canvasPaint.setStyle(Paint.Style.FILL);
                canvasPaint.setARGB(80, 255, 215, 215);
            }

            Path path = new Path();


            Geometry geom = wktr.read(pg.getPixelGeom_the_geom());
            Coordinate[] coord = geom.getCoordinates();

            for (int i = 0; i < coord.length; i++) {
                float x = (float) coord[i].x;
                float y = (float) coord[i].y;
                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }
            drawCanvas.drawPath(path, canvasPaint);
            canvasPaint.setStyle(Paint.Style.STROKE);
            canvasPaint.setColor(0xFF990000);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void refresh() {

        //Tracé des points et des lignes
        Point temp = null;
        boolean init = true;
        for (Point p : ElementDefinitionActivity.newPoints) {
            drawCanvas.drawCircle(p.x, p.y, 7, drawPaint);
            if (init) {
                temp = p;
                init = false;
            } else {

                int k = ElementDefinitionActivity.newPoints.indexOf(p);
                if (k < ElementDefinitionActivity.newPoints.size() - 1) {
                    Point q = ElementDefinitionActivity.newPoints.get(k + 1);
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
        }

        //Tracé des polygones non sauvegardés
        for (PixelGeom pg : ElementDefinitionActivity.newPolygones) {
            drawFilledPolygone(pg);
        }
    }

    public void erase() {
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        canvasPaint.setColor(Color.TRANSPARENT);
        drawCanvas.drawRect(0, 0, canvasBitmap.getWidth(), canvasBitmap.getHeight(), drawPaint);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        drawCanvas = new Canvas(canvasBitmap);
    }

}
