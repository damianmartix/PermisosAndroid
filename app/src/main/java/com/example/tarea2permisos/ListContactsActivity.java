package com.example.tarea2permisos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tarea2permisos.Adapter.ListContactsAdapter;

public class ListContactsActivity extends AppCompatActivity {

    ListView listContacts;
    String contactsPermission = Manifest.permission.READ_CONTACTS;
    public static final int CONTACTS_ID = 5;
    ListContactsAdapter adapter;

    String[] projection = new String[]{ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contacts);

        listContacts = findViewById(R.id.listContacts);
        adapter = new ListContactsAdapter(this, null, 0);
        listContacts.setAdapter(adapter);

        // requerir permiso
        myRequestPermission(this, contactsPermission, "se requiere para mostrar la lista de contactos", CONTACTS_ID);

        // Update screen
        updateUI();

    }

    private void updateUI(){
        if (ContextCompat.checkSelfPermission(this, contactsPermission) == PackageManager.PERMISSION_GRANTED){
            // QUERY
            Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null,null,null);
            adapter.changeCursor(cursor);
        } else {

        }
    }

    private void myRequestPermission(Activity context, String permission, String justification, int id){
        // Si no tengo permisos
        if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)){
                Toast.makeText(context, justification, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permission}, id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_ID){
            updateUI();
        }
    }
}