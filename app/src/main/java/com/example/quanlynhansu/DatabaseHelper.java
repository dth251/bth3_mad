package com.example.quanlynhansu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/** Mọi thao tác dữ liệu đều đi qua SQLiteOpenHelper này. */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "QuanLyNhanSu.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE = "NhanSu";
    public static final int SORT_DEFAULT = 0;
    public static final int SORT_SALARY_ASC = 1;
    public static final int SORT_SALARY_DESC = 2;

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "maNhanSu TEXT PRIMARY KEY NOT NULL, "
                + "hoTen TEXT NOT NULL, namSinh INTEGER, gioiTinh TEXT, "
                + "queQuan TEXT, trinhDo TEXT, anh TEXT, mucLuong REAL, phongBan TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Bản đầu chỉ có schema v1. Không DROP TABLE để tránh mất dữ liệu.
        // Khi phát hành schema mới cần viết migration ALTER TABLE tương ứng.
        throw new SQLiteException("Chưa hỗ trợ nâng cấp cơ sở dữ liệu từ "
                + oldVersion + " lên " + newVersion);
    }

    private ContentValues toValues(NhanSu person, boolean includeId) {
        ContentValues values = new ContentValues();
        if (includeId) values.put("maNhanSu", person.getMaNhanSu());
        values.put("hoTen", person.getHoTen());
        values.put("namSinh", person.getNamSinh());
        values.put("gioiTinh", person.getGioiTinh());
        values.put("queQuan", person.getQueQuan());
        values.put("trinhDo", person.getTrinhDo());
        values.put("anh", person.getAnh());
        values.put("mucLuong", person.getMucLuong());
        values.put("phongBan", person.getPhongBan());
        return values;
    }

    public boolean insertNhanSu(NhanSu person) {
        // CONFLICT_IGNORE giữ nguyên bản ghi cũ nếu khóa chính bị trùng.
        return getWritableDatabase().insertWithOnConflict(TABLE, null, toValues(person, true),
                SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public ArrayList<NhanSu> getAllNhanSu() {
        return readList("maNhanSu COLLATE NOCASE ASC");
    }

    public NhanSu getNhanSuById(String id) {
        try (Cursor cursor = getReadableDatabase().query(TABLE, null,
                "maNhanSu = ?", new String[]{id}, null, null, null)) {
            return cursor.moveToFirst() ? fromCursor(cursor) : null;
        }
    }

    public boolean updateNhanSu(NhanSu person) {
        return getWritableDatabase().update(TABLE, toValues(person, false),
                "maNhanSu = ?", new String[]{person.getMaNhanSu()}) == 1;
    }

    public boolean deleteNhanSu(String id) {
        return getWritableDatabase().delete(TABLE, "maNhanSu = ?", new String[]{id}) == 1;
    }

    public ArrayList<NhanSu> searchNhanSu(String keyword) {
        return getNhanSu(keyword, SORT_DEFAULT);
    }

    public ArrayList<NhanSu> sortNhanSuBySalaryASC() {
        return readList("mucLuong ASC, maNhanSu COLLATE NOCASE ASC");
    }

    public ArrayList<NhanSu> sortNhanSuBySalaryDESC() {
        return readList("mucLuong DESC, maNhanSu COLLATE NOCASE ASC");
    }

    /** Kết hợp lọc và sắp xếp để đổi kiểu sắp xếp không làm mất từ khóa. */
    public ArrayList<NhanSu> getNhanSu(String keyword, int sortMode) {
        ArrayList<NhanSu> source;
        if (sortMode == SORT_SALARY_ASC) source = sortNhanSuBySalaryASC();
        else if (sortMode == SORT_SALARY_DESC) source = sortNhanSuBySalaryDESC();
        else source = getAllNhanSu();

        String query = normalize(keyword == null ? "" : keyword.trim());
        if (query.isEmpty()) return source;
        ArrayList<NhanSu> matches = new ArrayList<>();
        // SQLite NOCASE mặc định chỉ xử lý ASCII; Java xử lý cả chữ tiếng Việt.
        for (NhanSu person : source) {
            if (normalize(person.getMaNhanSu()).contains(query)
                    || normalize(person.getHoTen()).contains(query)) {
                matches.add(person);
            }
        }
        return matches;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFC)
                .toLowerCase(Locale.forLanguageTag("vi-VN"));
    }

    private ArrayList<NhanSu> readList(String orderBy) {
        ArrayList<NhanSu> people = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE, null,
                null, null, null, null, orderBy)) {
            while (cursor.moveToNext()) people.add(fromCursor(cursor));
        }
        return people;
    }

    private NhanSu fromCursor(Cursor cursor) {
        return new NhanSu(
                cursor.getString(cursor.getColumnIndexOrThrow("maNhanSu")),
                cursor.getString(cursor.getColumnIndexOrThrow("hoTen")),
                cursor.getInt(cursor.getColumnIndexOrThrow("namSinh")),
                cursor.getString(cursor.getColumnIndexOrThrow("gioiTinh")),
                cursor.getString(cursor.getColumnIndexOrThrow("queQuan")),
                cursor.getString(cursor.getColumnIndexOrThrow("trinhDo")),
                cursor.getString(cursor.getColumnIndexOrThrow("anh")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("mucLuong")),
                cursor.getString(cursor.getColumnIndexOrThrow("phongBan")));
    }
}
