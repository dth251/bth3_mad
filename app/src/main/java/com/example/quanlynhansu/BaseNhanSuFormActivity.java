package com.example.quanlynhansu;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

/** Dùng chung nhập liệu, chọn ảnh và kiểm tra dữ liệu cho màn hình thêm/sửa. */
public abstract class BaseNhanSuFormActivity extends Activity {
    private static final int REQUEST_PHOTO = 100;
    protected DatabaseHelper database;
    protected EditText employeeId, fullName, birthYear, hometown, salary;
    protected RadioGroup gender;
    protected Spinner education, department;
    private ArrayAdapter<String> educationAdapter;
    private ImageView avatar;
    private String selectedPhoto = "";
    private String originalPhoto = "";
    private String editingId;
    private boolean committed;

    protected boolean initializeForm(int layout, boolean editing, Bundle state) {
        setContentView(layout);
        database = new DatabaseHelper(this);
        employeeId = findViewById(R.id.edtEmployeeId);
        fullName = findViewById(R.id.edtFullName);
        birthYear = findViewById(R.id.edtBirthYear);
        hometown = findViewById(R.id.edtHometown);
        education = findViewById(R.id.spinnerEducation);
        salary = findViewById(R.id.edtSalary);
        gender = findViewById(R.id.radioGender);
        department = findViewById(R.id.spinnerDepartment);
        avatar = findViewById(R.id.imgAvatar);
        educationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        educationAdapter.addAll(getResources().getStringArray(R.array.education_levels));
        educationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        education.setAdapter(educationAdapter);
        ArrayAdapter<CharSequence> departments = ArrayAdapter.createFromResource(this,
                R.array.departments, android.R.layout.simple_spinner_item);
        departments.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        department.setAdapter(departments);

        if (editing) {
            editingId = getIntent().getStringExtra(MainActivity.EXTRA_EMPLOYEE_ID);
            try {
                NhanSu person = editingId == null ? null : database.getNhanSuById(editingId);
                if (person == null) {
                    Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return false;
                }
                fillForm(person);
                employeeId.setEnabled(false);
                findViewById(R.id.tvIdLocked).setVisibility(View.VISIBLE);
            } catch (SQLiteException error) {
                Toast.makeText(this, R.string.load_failed, Toast.LENGTH_SHORT).show();
                finish();
                return false;
            }
        }
        if (state != null) selectedPhoto = state.getString("selectedPhoto", originalPhoto);
        PhotoHelper.display(avatar, selectedPhoto);
        findViewById(R.id.btnChoosePhoto).setOnClickListener(view -> choosePhoto());
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        return true;
    }

    private void fillForm(NhanSu person) {
        employeeId.setText(person.getMaNhanSu());
        fullName.setText(person.getHoTen());
        birthYear.setText(String.valueOf(person.getNamSinh()));
        hometown.setText(person.getQueQuan());
        String savedEducation = person.getTrinhDo();
        if (savedEducation != null && !savedEducation.isEmpty()) {
            int position = educationAdapter.getPosition(savedEducation);
            // Giữ trình độ tự nhập của hồ sơ cũ khi mở và lưu lại màn hình sửa.
            if (position < 0) {
                educationAdapter.add(savedEducation);
                position = educationAdapter.getCount() - 1;
            }
            education.setSelection(position);
        }
        salary.setText(java.math.BigDecimal.valueOf(person.getMucLuong()).stripTrailingZeros().toPlainString());
        gender.check("Nữ".equals(person.getGioiTinh()) ? R.id.radioFemale
                : "Khác".equals(person.getGioiTinh()) ? R.id.radioOther : R.id.radioMale);
        String[] choices = getResources().getStringArray(R.array.departments);
        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(person.getPhongBan())) department.setSelection(i);
        }
        originalPhoto = person.getAnh() == null ? "" : person.getAnh();
        selectedPhoto = originalPhoto;
    }

    protected void savePerson(boolean editing) {
        NhanSu person = readValidatedPerson();
        if (person == null) return;
        try {
            if (!editing && database.getNhanSuById(person.getMaNhanSu()) != null) {
                showFieldError(employeeId, getString(R.string.duplicate_id));
                return;
            }
            boolean success = editing ? database.updateNhanSu(person) : database.insertNhanSu(person);
            if (!success) {
                Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            committed = true;
            if (!originalPhoto.equals(selectedPhoto)) PhotoHelper.delete(this, originalPhoto);
            Toast.makeText(this, editing ? R.string.update_success : R.string.add_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (SQLiteException error) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private NhanSu readValidatedPerson() {
        employeeId.setError(null);
        fullName.setError(null);
        birthYear.setError(null);
        salary.setError(null);
        String id = editingId == null ? text(employeeId) : editingId;
        if (id.isEmpty()) {
            showFieldError(employeeId, getString(R.string.id_required));
            return null;
        }
        String name = text(fullName);
        if (name.isEmpty()) {
            showFieldError(fullName, getString(R.string.name_required));
            return null;
        }
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int year;
        try {
            year = Integer.parseInt(text(birthYear));
            if (year < 1900 || year > currentYear) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            showFieldError(birthYear, getString(R.string.invalid_year, currentYear));
            return null;
        }
        double amount;
        try {
            String raw = text(salary);
            if (!raw.matches("[0-9]+([.,][0-9]+)?")) throw new NumberFormatException();
            amount = Double.parseDouble(raw.replace(',', '.'));
            if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            showFieldError(salary, getString(R.string.invalid_salary));
            return null;
        }
        RadioButton selectedGender = findViewById(gender.getCheckedRadioButtonId());
        String selectedEducation = education.getSelectedItemPosition() == 0
                ? "" : education.getSelectedItem().toString();
        return new NhanSu(id, name, year, selectedGender.getText().toString(), text(hometown),
                selectedEducation, selectedPhoto, amount, department.getSelectedItem().toString());
    }

    private String text(EditText input) { return input.getText().toString().trim(); }

    private void showFieldError(EditText input, String message) {
        input.setError(message);
        input.requestFocus();
    }

    protected void resetForm() {
        if (!selectedPhoto.equals(originalPhoto)) PhotoHelper.delete(this, selectedPhoto);
        selectedPhoto = originalPhoto;
        for (EditText input : new EditText[]{employeeId, fullName, birthYear, hometown, salary}) {
            input.setText("");
            input.setError(null);
        }
        gender.check(R.id.radioMale);
        education.setSelection(0);
        department.setSelection(0);
        PhotoHelper.display(avatar, selectedPhoto);
        employeeId.requestFocus();
    }

    @SuppressWarnings("deprecation")
    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_PHOTO);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, R.string.no_photo_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                String newPhoto = PhotoHelper.importPhoto(this, data.getData());
                if (!selectedPhoto.equals(originalPhoto)) PhotoHelper.delete(this, selectedPhoto);
                selectedPhoto = newPhoto;
                PhotoHelper.display(avatar, selectedPhoto);
            } catch (IOException | RuntimeException error) {
                Toast.makeText(this, R.string.photo_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putString("selectedPhoto", selectedPhoto);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        // Xoay màn hình giữ ảnh đang nhập; thoát/hủy thì dọn ảnh chưa lưu.
        if (isFinishing() && !committed && !selectedPhoto.equals(originalPhoto)) {
            PhotoHelper.delete(this, selectedPhoto);
        }
        if (database != null) database.close();
        super.onDestroy();
    }
}
