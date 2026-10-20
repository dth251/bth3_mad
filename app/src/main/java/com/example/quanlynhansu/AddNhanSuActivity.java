package com.example.quanlynhansu;

import android.os.Bundle;

public class AddNhanSuActivity extends BaseNhanSuFormActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (initializeForm(R.layout.activity_add_nhan_su, false, savedInstanceState)) {
            findViewById(R.id.btnSave).setOnClickListener(view -> savePerson(false));
            findViewById(R.id.btnReset).setOnClickListener(view -> resetForm());
        }
    }
}
