package fr.turfu.urbapp2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import fr.turfu.urbapp2.DB.Element;
import fr.turfu.urbapp2.DB.ElementBDD;
import fr.turfu.urbapp2.DB.PixelGeom;
import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;
import fr.turfu.urbapp2.Tools.DrawView;

/**
 * Created by Laura on 10/10/2016.
 */
public class ElementDefinitionActivity extends AppCompatActivity {

    /**
     * Id du projet ouvert
     */
    private long project_id;

    /**
     * Id de la photo ouverte
     */
    private long photo_id;

    /**
     * Path de la photo ouverte
     */
    private String photo_path;

    /**
     * Menu item pour le nom du projet
     */
    private MenuItem mi;

    /**
     * Booleen vrai si l'utilisateur à selectionner le crayon pour dessiner une zone
     */
    public static boolean pen;

    /**
     * Bouton crayon
     */
    private Button bPen;

    /**
     * Bitmap contenant la photo
     */
    private Bitmap photo;

    /**
     * Zone de dessin
     */
    private DrawView v;

    /*
     * Liste des pixelGeom de la photo
     */
    public static ArrayList<PixelGeom> polygones;

    /**
     * Liste des elements de la photo
     */
    public static ArrayList<Element> elements;

    public static Context context;

    /**
     * Listes des ajouts non enregistrés:
     * - newPoints : derniers points ajoutés, formant une ligne non fermée (pas encore polygone)
     * - newPolygones : dès que les points forment un polygone valide, on ajoute un polygone ici
     * - newElements : à chaque fois qu'un polygone est caractérisé, il est ajouté ici
     */
    public static ArrayList<PixelGeom> newPolygones;
    public static ArrayList<Element> newElements;
    public static ArrayList<Point> newPoints;
    public static ArrayList<String> actions; // Liste des actions faites par l'utilisateur, cette liste sert au bon fonctionnement des boutons annuler

    /**
     * Création de l'activité
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Creation
        super.onCreate(savedInstanceState);

        //Mise en place du layout
        setContentView(R.layout.activity_element_definition);

        //Paramètres
        final Intent intent = getIntent();
        photo_path = intent.getStringExtra("photo_path"); // Project ID
        project_id = intent.getLongExtra("project_id", 0);
        photo_id = intent.getLongExtra("photo_id", 0);

        //Initialisation
        actions = new ArrayList<>();
        newElements = new ArrayList<>();
        newPoints = new ArrayList<>();
        newPolygones = new ArrayList<>();
        ElementBDD ebdd = new ElementBDD(ElementDefinitionActivity.this);
        ebdd.open();
        elements = ebdd.getElement(photo_id);
        polygones = ebdd.getPixelGeom(elements);
        ebdd.close();
        context = ElementDefinitionActivity.this;

        //Photo
        File imgFile = new File(Environment.getExternalStorageDirectory(), photo_path);
        photo = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        RelativeLayout l = (RelativeLayout) findViewById(R.id.layoutBitmap);
        Drawable d = new BitmapDrawable(getResources(), photo);
        l.setBackground(d);
        int h = photo.getHeight();
        int w = photo.getWidth();
        v = new DrawView(ElementDefinitionActivity.this, w, h, photo_id);
        l.addView(v);

        //Mise en place de la toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        //Bouton aide
        Button help = (Button) findViewById(R.id.buttonHelp);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ElementDefinitionActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.help_element_pop_up, null);
                builder.setView(dialogView);

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);

                final AlertDialog dialog = builder.create();

                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        //Bouton crayon
        pen = false;
        bPen = (Button) findViewById(R.id.buttonPen);
        bPen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pen = !pen;

                if (pen) {
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.pen_over);
                    Drawable d = new BitmapDrawable(getResources(), b);
                    bPen.setBackground(d);
                } else {
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.pen);
                    Drawable d = new BitmapDrawable(getResources(), b);
                    bPen.setBackground(d);
                }
            }
        });

        //Bouton caractériser surface
        Button carac = (Button) findViewById(R.id.buttonDefine);
        carac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                // On trouve le pixelGeom selectionné
                int cpt = 0;
                PixelGeom select = null;

                for (PixelGeom p : polygones) {
                    if (p.selected) {
                        cpt++;
                        select = p;
                    }
                }
                for (PixelGeom p : newPolygones) {
                    if (p.selected) {
                        cpt++;
                        select = p;
                    }
                }

                // Si 0 ou plus de une zone sont selectionnées, on affiche une erreur, sinon, on lance la pop-up
                if (cpt == 0 || cpt > 1) {
                    Toast.makeText(ElementDefinitionActivity.this, R.string.one_zone, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(ElementDefinitionActivity.this, ElementDefinitionPopUp.class);
                    intent.putExtra("photo_id", photo_id);
                    intent.putExtra("pixelGeom_id", select.getPixelGeomId());
                    actions.add("ELEMENT");
                    startActivity(intent);
                }

            }
        });


        //Bouton Annuler tout
        Button cancelAll = (Button) findViewById(R.id.buttonCancelAll);
        cancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (actions.size() == 0) {
                    Toast.makeText(ElementDefinitionActivity.this, R.string.nothing_to_cancel, Toast.LENGTH_SHORT).show();
                } else {
                    v.cancelAll();
                    Toast.makeText(ElementDefinitionActivity.this, R.string.cancel_all_success, Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Bouton Annuler
        Button cancel = (Button) findViewById(R.id.buttonCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (actions.size() == 0) {
                    Toast.makeText(ElementDefinitionActivity.this, R.string.nothing_to_cancel, Toast.LENGTH_SHORT).show();
                } else {
                    v.cancelLast();
                    Toast.makeText(ElementDefinitionActivity.this, R.string.cancel_all_success, Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Bouton Grouper
        Button group = (Button) findViewById(R.id.buttonGroup);
        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                ArrayList<PixelGeom> selectedPix = new ArrayList<PixelGeom>();
                for (PixelGeom p : polygones) {
                    if (p.isSelected()) {
                        selectedPix.add(p);
                    }
                }
                for (PixelGeom p : newPolygones) {
                    if (p.isSelected()) {
                        selectedPix.add(p);
                    }
                }

                if (selectedPix.size() <= 1) {
                    Toast.makeText(ElementDefinitionActivity.this, R.string.group_error, Toast.LENGTH_SHORT).show();
                } else {
                    v.group(selectedPix);
                    //On ajoute l'action
                    ElementDefinitionActivity.actions.add("GROUP");
                    Toast.makeText(ElementDefinitionActivity.this, R.string.group_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Bouton Degrouper
        Button degroup = (Button) findViewById(R.id.buttonUngroup);
        degroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                ArrayList<PixelGeom> selectedPix = new ArrayList<PixelGeom>();
                for (PixelGeom p : polygones) {
                    if (p.isSelected()) {
                        selectedPix.add(p);
                    }
                }
                for (PixelGeom p : newPolygones) {
                    if (p.isSelected()) {
                        selectedPix.add(p);
                    }
                }

                if (selectedPix.size() != 1) {
                    Toast.makeText(ElementDefinitionActivity.this, R.string.one_zone, Toast.LENGTH_SHORT).show();
                } else {
                    int n = v.degroup(selectedPix.get(0));
                    //On ajoute l'action
                    ElementDefinitionActivity.actions.add("DEGROUP#" + n);
                    Toast.makeText(ElementDefinitionActivity.this, R.string.ungroup_success, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Bouton Récapitulatif
        Button recap = (Button) findViewById(R.id.buttonRecap);
        recap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ElementDefinitionActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.recap_pop_up, null);
                builder.setView(dialogView);

                //Récapitulatif
                TextView txt = (TextView) dialogView.findViewById(R.id.recap);
                int a = elements.size() + newElements.size();
                int b = polygones.size() + newPolygones.size();
                String s = getString(R.string.recap_txt);
                txt.setText(a + "/" + b + " " + s);

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);

                final AlertDialog dialog = builder.create();

                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });


        //Bouton Sauvegarde
        Button save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                save();
            }
        });

        //Bouton back
        Button back = (Button) findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ElementDefinitionActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.save_pop_up, null);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        dialog.dismiss();
                    }
                });

                //Bouton cancel
                Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel_change);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        v.cancelAll();
                        Intent i = new Intent(ElementDefinitionActivity.this, PhotoOpenActivity.class);
                        i.putExtra("photo_path", photo_path);
                        i.putExtra("photo_id", photo_id);
                        i.putExtra("project_id", project_id);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                //Bouton save
                Button save = (Button) dialogView.findViewById(R.id.btn_save_change);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        save();
                        Intent i = new Intent(ElementDefinitionActivity.this, PhotoOpenActivity.class);
                        i.putExtra("photo_path", photo_path);
                        i.putExtra("photo_id", photo_id);
                        i.putExtra("project_id", project_id);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }


    /**
     * Method to handle the clicks on the items of the toolbar
     *
     * @param item the item
     * @return true if everything went good
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:

                AlertDialog.Builder builder = new AlertDialog.Builder(ElementDefinitionActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.save_pop_up, null);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        dialog.dismiss();
                    }
                });

                //Bouton cancel
                Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel_change);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        v.cancelAll();
                        Intent i = new Intent(ElementDefinitionActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                //Bouton save
                Button save = (Button) dialogView.findViewById(R.id.btn_save_change);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        save();
                        Intent i = new Intent(ElementDefinitionActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;

            case R.id.settings:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(ElementDefinitionActivity.this);

                //Layout
                LayoutInflater inflater1 = getLayoutInflater();
                View dialogView1 = inflater1.inflate(R.layout.save_pop_up, null);
                builder1.setView(dialogView1);
                final AlertDialog dialog1 = builder1.create();

                //Bouton close
                Button closeBtn1 = (Button) dialogView1.findViewById(R.id.btn_close_pop);
                closeBtn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        dialog1.dismiss();
                    }
                });

                //Bouton cancel
                Button cancel1 = (Button) dialogView1.findViewById(R.id.btn_cancel_change);
                cancel1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        v.cancelAll();
                        Intent i = new Intent(ElementDefinitionActivity.this, SettingsActivity.class);
                        startActivity(i);
                        finish();
                        dialog1.dismiss();
                    }
                });

                //Bouton save
                Button save1 = (Button) dialogView1.findViewById(R.id.btn_save_change);
                save1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        save();
                        Intent i = new Intent(ElementDefinitionActivity.this, SettingsActivity.class);
                        startActivity(i);
                        finish();
                        dialog1.dismiss();
                    }
                });

                dialog1.show();
                return true;

            case R.id.seeDetails:
                Project p = getProject();
                popUpDetails(p.getProjectName(), p.getProjectDescription());
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method to inflate the xml menu file (Ajout des différents onglets dans la toolbar)
     *
     * @param menu the menu
     * @return true if everything went good
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //On sérialise le fichier menu.xml pour l'afficher dans la barre de menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //Display Username
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String u = preferences.getString("user_preference", "");
        MenuItem i = menu.findItem(R.id.connectedAs);
        i.setTitle(u);

        //Display ProjectName
        MenuItem i1 = menu.findItem(R.id.projectTitle);
        i1.setVisible(true);
        Resources res = getResources();
        String s = res.getString(R.string.project);
        Project p = getProject();
        i1.setTitle(s + " " + p.getProjectName());
        mi = i1;

        //Affichage du bouton pour voir les détails du projet
        MenuItem i2 = menu.findItem(R.id.seeDetails);
        i2.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Lancement de la pop up avec les détails du projet
     */
    public void popUpDetails(String name, String descr) {
        PopUpDetails pud = new PopUpDetails(ElementDefinitionActivity.this, name, descr, mi);
        pud.show();
    }

    /**
     * Obtenir le projet ouvert
     *
     * @return Projet ouvert
     */
    public Project getProject() {
        ProjectBDD pbdd = new ProjectBDD(ElementDefinitionActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Project p = pbdd.getProjectById(project_id); // Récupération du projet
        pbdd.close(); // Fermeture de la base de données
        return p;
    }

    public void save() {
        ElementBDD ebdd = new ElementBDD(ElementDefinitionActivity.this);
        ebdd.open();

        //On supprime les anciens éléments de la photo
        ebdd.deleteElement(photo_id);

        //On ajoute les nouveaux
        for (Element e : newElements) {
            elements.add(e);
        }
        for (PixelGeom p : newPolygones) {
            polygones.add(p);
        }

        for (PixelGeom p : polygones) {
            ebdd.insertPixelGeom(p);
        }
        for (Element e : elements) {
            ebdd.insertElement(e);
        }

        //On vide les tableaux
        newPoints.clear();
        actions.clear();
        newElements.clear();
        newPolygones.clear();

        ebdd.close();
        Toast.makeText(ElementDefinitionActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
    }

}
