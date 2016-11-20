package edu.umd.cs.impressionistpainter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private boolean imagesDownloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        imagesDownloaded = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Image download button handler
    public void onButtonClickDownloadImages(View v){

        // TODO: DOWNLOAD THE IMAGES

        new AlertDialog.Builder(v.getContext())
                .setTitle("Message")
                .setMessage("Images Downloaded.")
                .setPositiveButton("Okay.", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        imagesDownloaded = true;
    }

    // Load image button handler
    public void onButtonClickLoadImage(View v){
        if(imagesDownloaded){}    // do x
        else {} // do y
    }

    // Brush change button handler
    public void onButtonClickSetBrush(View v){

    }

    // Clear button handler
    public void onButtonClickClear(View v){
        new AlertDialog.Builder(v.getContext())
                .setTitle("Warning!")
                .setMessage("Really clear your painting?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // clear the screen
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
