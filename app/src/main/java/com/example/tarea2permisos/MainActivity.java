package com.example.tarea2permisos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void cameraPressed(View v){
        startActivity(new Intent(this, CameraActivity.class));
    }

    public void mapsPressed(View v){

        startActivity(new Intent(this, MapsActivity.class));
    }

    public void contactsPressed (View v){
        startActivity(new Intent(this, ListContactsActivity.class));

    }


}