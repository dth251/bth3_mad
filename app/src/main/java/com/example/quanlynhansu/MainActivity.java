package com.example.quanlynhansu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {
    public static final String EXTRA_EMPLOYEE_ID = "maNhanSu";
    private DatabaseHelper database;
    private NhanSuAdapter adapter;
    private EditText search;
    private Spinner sort;
    private TextView count, emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = new DatabaseHelper(this);
        search = findViewById(R.id.edtSearch);
        sort = findViewById(R.id.spinnerSort);
        count = findViewById(R.id.tvCount);
        emptyText = findViewById(R.id.tvEmpty);
        ListView list = findViewById(R.id.listNhanSu);
        adapter = new NhanSuAdapter(this);
        list.setAdapter(adapter);
        list.setEmptyView(emptyText);

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.salary_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(sortAdapter);
        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int before, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int after) {
                refreshList();
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
        findViewById(R.id.btnAddNhanSu).setOnClickListener(view ->
                startActivity(new Intent(this, AddNhanSuActivity.class)));
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, DetailNhanSuActivity.class);
            intent.putExtra(EXTRA_EMPLOYEE_ID, adapter.getItem(position).getMaNhanSu());
            startActivity(intent);
        });
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteMenu(view, adapter.getItem(position));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        try {
            ArrayList<NhanSu> people = database.getNhanSu(search.getText().toString(),
                    Math.max(0, sort.getSelectedItemPosition()));
            adapter.updateData(people);
            count.setText(getString(R.string.result_count, people.size()));
            emptyText.setText(search.getText().toString().trim().isEmpty()
                    ? R.string.empty_list : R.string.empty_search);
        } catch (SQLiteException error) {
            adapter.updateData(new ArrayList<>());
            count.setText(getString(R.string.result_count, 0));
            emptyText.setText(R.string.load_failed);
        }
    }

    private void showDeleteMenu(View anchor, NhanSu person) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(R.string.delete);
        menu.setOnMenuItemClickListener(item -> {
            confirmDelete(person);
            return true;
        });
        menu.show();
    }

    private void confirmDelete(NhanSu person) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(getString(R.string.delete_message, person.getHoTen(), person.getMaNhanSu()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.agree, (dialog, which) -> {
                    try {
                        if (database.deleteNhanSu(person.getMaNhanSu())) {
                            PhotoHelper.delete(this, person.getAnh());
                            Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                            refreshList();
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
