package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Laura on 24/09/2016.
 */
public class CustomPopUp extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;

    public CustomPopUp(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.localisation_pop_up);
        yes = (Button) findViewById(R.id.btn_ok);
        no = (Button) findViewById(R.id.btn_cancel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    //TODO : Quand on clique sur le bouton ok, valider les changements
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                //TODO
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}