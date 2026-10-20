package com.example.quanlynhansu;

import android.os.Bundle;

public class EditNhanSuActivity extends BaseNhanSuFormActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (initializeForm(R.layout.activity_edit_nhan_su, true, savedInstanceState)) {
            findViewById(R.id.btnSave).setOnClickListener(view -> savePerson(true));
        }
    }
}
