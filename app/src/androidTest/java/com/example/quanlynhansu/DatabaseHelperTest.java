package com.example.quanlynhansu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/** Kiểm thử SQLite thật trên thiết bị, dùng database test_ riêng. */
@SuppressWarnings("deprecation")
public class DatabaseHelperTest extends AndroidTestCase {
    private DatabaseHelper database;
    private Context isolatedContext;

    @Override protected void setUp() throws Exception {
        super.setUp();
        isolatedContext = new RenamingDelegatingContext(getContext(), "test_") {
            @Override public Context getApplicationContext() { return this; }
        };
        isolatedContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        database = new DatabaseHelper(isolatedContext);
    }

    @Override protected void tearDown() throws Exception {
        database.close();
        isolatedContext.deleteDatabase(DatabaseHelper.DATABASE_NAME);
        super.tearDown();
    }

    private NhanSu person(String id, String name, double amount) {
        return new NhanSu(id, name, 2000, "Nữ", "Hà Nội", "Đại học", "", amount, "Phần mềm");
    }

    public void testCrudDuplicateAndPersistence() {
        assertTrue(database.insertNhanSu(person("NS001", "Nguyễn Duy Thắng", 12000000)));
        assertFalse(database.insertNhanSu(person("NS001", "Không được ghi đè", 0)));
        NhanSu saved = database.getNhanSuById("NS001");
        assertEquals("Nguyễn Duy Thắng", saved.getHoTen());
        assertEquals(2000, saved.getNamSinh());
        assertEquals("Nữ", saved.getGioiTinh());
        assertEquals("Hà Nội", saved.getQueQuan());
        assertEquals("Đại học", saved.getTrinhDo());
        assertEquals("Phần mềm", saved.getPhongBan());

        saved.setHoTen("Trần Thị Hà");
        saved.setMucLuong(15000000.5);
        assertTrue(database.updateNhanSu(saved));
        database.close();
        database = new DatabaseHelper(isolatedContext);
        saved = database.getNhanSuById("NS001");
        assertEquals("Trần Thị Hà", saved.getHoTen());
        assertEquals(15000000.5, saved.getMucLuong(), 0.001);
        assertTrue(database.deleteNhanSu("NS001"));
        assertNull(database.getNhanSuById("NS001"));
        assertFalse(database.deleteNhanSu("NS001"));
    }

    public void testUnicodeSearchAndNumericSalarySort() {
        database.insertNhanSu(person("NS001", "NGUYỄN THỊ HÀ", 100));
        database.insertNhanSu(person("NS002", "Nguyễn Văn An", 9));
        database.insertNhanSu(person("NS003", "Lê Minh", 20.5));
        assertEquals(1, database.searchNhanSu("nguyễn thị hà").size());
        assertEquals(1, database.searchNhanSu("ns002").size());
        assertEquals(2, database.searchNhanSu("Nguyễn").size());
        assertEquals(0, database.searchNhanSu("%' OR 1=1 --").size());
        ArrayList<NhanSu> ascending = database.sortNhanSuBySalaryASC();
        assertEquals("NS002", ascending.get(0).getMaNhanSu());
        assertEquals("NS003", ascending.get(1).getMaNhanSu());
        assertEquals("NS001", ascending.get(2).getMaNhanSu());
        assertEquals("NS001", database.sortNhanSuBySalaryDESC().get(0).getMaNhanSu());
        ArrayList<NhanSu> combined = database.getNhanSu("nguyễn", DatabaseHelper.SORT_SALARY_ASC);
        assertEquals(2, combined.size());
        assertEquals("NS002", combined.get(0).getMaNhanSu());
    }

    public void testPhotoCopySurvivesOriginalDeletion() throws Exception {
        Context context = getContext();
        File source = new File(context.getCacheDir(), "test-avatar.png");
        Bitmap original = Bitmap.createBitmap(1600, 1200, Bitmap.Config.ARGB_8888);
        original.eraseColor(0xFF155EEF);
        try (FileOutputStream output = new FileOutputStream(source)) {
            original.compress(Bitmap.CompressFormat.PNG, 100, output);
        }
        original.recycle();
        String imported = PhotoHelper.importPhoto(context, Uri.fromFile(source));
        try {
            assertTrue(source.delete());
            assertTrue(new File(imported).exists());
            Bitmap stored = BitmapFactory.decodeFile(imported);
            assertNotNull(stored);
            assertTrue(stored.getWidth() <= 1024);
            stored.recycle();
        } finally {
            PhotoHelper.delete(context, imported);
        }
        assertFalse(new File(imported).exists());
    }
}
