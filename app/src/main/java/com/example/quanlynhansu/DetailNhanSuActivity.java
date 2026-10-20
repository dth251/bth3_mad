package com.example.quanlynhansu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailNhanSuActivity extends Activity {
    private DatabaseHelper database;
    private String employeeId;
    private NhanSu person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_nhan_su);
        database = new DatabaseHelper(this);
        employeeId = getIntent().getStringExtra(MainActivity.EXTRA_EMPLOYEE_ID);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnEdit).setOnClickListener(view -> {
            Intent intent = new Intent(this, EditNhanSuActivity.class);
            intent.putExtra(MainActivity.EXTRA_EMPLOYEE_ID, employeeId);
            startActivity(intent);
        });
        findViewById(R.id.btnDelete).setOnClickListener(view -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            person = employeeId == null ? null : database.getNhanSuById(employeeId);
            if (person == null) {
                Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            PhotoHelper.display((ImageView) findViewById(R.id.imgAvatar), person.getAnh());
            setText(R.id.tvEmployeeId, person.getMaNhanSu());
            setText(R.id.tvName, person.getHoTen());
            setText(R.id.tvBirthYear, String.valueOf(person.getNamSinh()));
            setText(R.id.tvGender, person.getGioiTinh());
            setText(R.id.tvHometown, person.getQueQuan());
            setText(R.id.tvEducation, person.getTrinhDo());
            setText(R.id.tvSalary, person.getLuongHienThi());
            setText(R.id.tvDepartment, person.getPhongBan());
        } catch (SQLiteException error) {
            Toast.makeText(this, R.string.load_failed, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setText(int viewId, String text) {
        ((TextView) findViewById(viewId)).setText(text == null || text.trim().isEmpty()
                ? getString(R.string.no_value) : text);
    }

    private void confirmDelete() {
        if (person == null) return;
        new AlertDialog.Builder(this).setTitle(R.string.confirm_delete)
                .setMessage(getString(R.string.delete_message, person.getHoTen(), person.getMaNhanSu()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.agree, (dialog, which) -> {
                    try {
                        if (database.deleteNhanSu(employeeId)) {
                            PhotoHelper.delete(this, person.getAnh());
                            Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                        }
                    } catch (SQLiteException error) {
                        Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
