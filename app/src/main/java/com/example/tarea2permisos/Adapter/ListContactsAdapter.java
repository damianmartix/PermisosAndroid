package com.example.tarea2permisos.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.tarea2permisos.R;

public class ListContactsAdapter extends CursorAdapter {
    private static final int CONTACT_ID = 0;
    private static final int DISPLAY_NAME = 1;

    public ListContactsAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.contactitem, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvIdContacto = view.findViewById(R.id.idContact);
        TextView tvNombre = view.findViewById(R.id.nameContact);

        int idnum = cursor.getInt(CONTACT_ID);
        String name = cursor.getString(DISPLAY_NAME);

        tvIdContacto.setText(String.valueOf(idnum));
        tvNombre.setText(name);
    }
}