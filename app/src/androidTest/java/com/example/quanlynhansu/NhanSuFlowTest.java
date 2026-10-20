package com.example.quanlynhansu;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.View;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

/** Chạy các Activity thật; chỉ dọn bản ghi có mã kiểm thử riêng. */
@SuppressWarnings("deprecation")
public class NhanSuFlowTest extends InstrumentationTestCase {
    private static final String TEST_ID = "__UI_TEST_087__";
    private MainActivity main;
    private Activity current;

    private void clearTestRecords(DatabaseHelper database) {
        for (String suffix : new String[]{"", "_A", "_B", "_C", "_D"}) {
            NhanSu person = database.getNhanSuById(TEST_ID + suffix);
            if (person != null) PhotoHelper.delete(getInstrumentation().getTargetContext(), person.getAnh());
            database.deleteNhanSu(TEST_ID + suffix);
        }
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            clearTestRecords(database);
        }
        Intent intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        main = (MainActivity) getInstrumentation().startActivitySync(intent);
        current = main;
        getInstrumentation().waitForIdleSync();
        waitForFocus(main);
    }

    @Override protected void tearDown() throws Exception {
        getInstrumentation().runOnMainSync(() -> {
            if (current != null) current.finish();
            main.finish();
        });
        getInstrumentation().waitForIdleSync();
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            clearTestRecords(database);
        }
        super.tearDown();
    }

    private void click(Activity activity, int id) {
        getInstrumentation().runOnMainSync(() -> activity.findViewById(id).performClick());
        getInstrumentation().waitForIdleSync();
        if (activity.isFinishing()) waitForClosed(activity);
    }

    private void waitForClosed(Activity activity) {
        long deadline = SystemClock.uptimeMillis() + 10000;
        while (!activity.isDestroyed() && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(100);
            getInstrumentation().waitForIdleSync();
        }
        assertTrue("Màn hình chưa đóng", activity.isDestroyed());
    }

    private void waitForFocus(Activity activity) {
        boolean[] focused = {false};
        long deadline = SystemClock.uptimeMillis() + 10000;
        do {
            getInstrumentation().runOnMainSync(() -> focused[0] = activity.hasWindowFocus());
            if (!focused[0]) SystemClock.sleep(100);
        } while (!focused[0] && SystemClock.uptimeMillis() < deadline);
        assertTrue("Màn hình chưa sẵn sàng nhận thao tác", focused[0]);
    }

    private void enter(Activity activity, int id, String value) {
        getInstrumentation().runOnMainSync(() -> ((EditText) activity.findViewById(id)).setText(value));
        getInstrumentation().waitForIdleSync();
    }

    private Activity open(Class<? extends Activity> type, Activity from, int button) {
        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(type.getName(), null, false);
        click(from, button);
        Activity opened = monitor.waitForActivityWithTimeout(5000);
        getInstrumentation().removeMonitor(monitor);
        assertNotNull("Không mở được " + type.getSimpleName(), opened);
        getInstrumentation().waitForIdleSync();
        current = opened;
        waitForFocus(opened);
        return opened;
    }

    private void fillValid(Activity form) {
        enter(form, R.id.edtEmployeeId, TEST_ID);
        enter(form, R.id.edtFullName, "Nguyễn Thị Hà");
        enter(form, R.id.edtBirthYear, "2000");
        enter(form, R.id.edtHometown, "Hà Nội");
        selectEducation(form, "Đại học");
        enter(form, R.id.edtSalary, "12000000");
    }

    private void selectEducation(Activity form, String value) {
        getInstrumentation().runOnMainSync(() -> {
            Spinner spinner = form.findViewById(R.id.spinnerEducation);
            for (int i = 0; i < spinner.getCount(); i++) {
                if (value.equals(spinner.getItemAtPosition(i))) {
                    spinner.setSelection(i);
                    return;
                }
            }
            fail("Không có lựa chọn trình độ " + value);
        });
        getInstrumentation().waitForIdleSync();
    }

    public void testValidationResetAndCancel() {
        Activity add = open(AddNhanSuActivity.class, main, R.id.btnAddNhanSu);
        click(add, R.id.btnSave);
        assertNotNull(((EditText) add.findViewById(R.id.edtEmployeeId)).getError());
        fillValid(add);
        enter(add, R.id.edtFullName, "   ");
        click(add, R.id.btnSave);
        assertNotNull(((EditText) add.findViewById(R.id.edtFullName)).getError());
        enter(add, R.id.edtFullName, "Nguyễn Thị Hà");
        enter(add, R.id.edtBirthYear, "9999");
        click(add, R.id.btnSave);
        assertNotNull(((EditText) add.findViewById(R.id.edtBirthYear)).getError());
        enter(add, R.id.edtBirthYear, "2000");
        enter(add, R.id.edtSalary, "-1");
        click(add, R.id.btnSave);
        assertNotNull(((EditText) add.findViewById(R.id.edtSalary)).getError());
        click(add, R.id.btnReset);
        assertEquals("", ((EditText) add.findViewById(R.id.edtFullName)).getText().toString());
        assertEquals(8, ((Spinner) add.findViewById(R.id.spinnerEducation)).getCount());
        assertEquals(0, ((Spinner) add.findViewById(R.id.spinnerEducation)).getSelectedItemPosition());
        assertEquals(4, ((Spinner) add.findViewById(R.id.spinnerDepartment)).getCount());
        click(add, R.id.btnBack);
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNull(database.getNhanSuById(TEST_ID));
        }
    }

    public void testAddDetailEditAndRefresh() {
        Activity add = open(AddNhanSuActivity.class, main, R.id.btnAddNhanSu);
        fillValid(add);
        click(add, R.id.btnSave);
        enter(main, R.id.edtSearch, "nguyễn thị hà");
        ListView list = main.findViewById(R.id.listNhanSu);
        assertTrue(list.getAdapter().getCount() >= 1);
        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(
                DetailNhanSuActivity.class.getName(), null, false);
        getInstrumentation().runOnMainSync(() -> {
            for (int i = 0; i < list.getAdapter().getCount(); i++) {
                if (TEST_ID.equals(((NhanSu) list.getAdapter().getItem(i)).getMaNhanSu())) {
                    View row = list.getAdapter().getView(i, null, list);
                    list.performItemClick(row, i, i);
                    break;
                }
            }
        });
        Activity detail = monitor.waitForActivityWithTimeout(5000);
        getInstrumentation().removeMonitor(monitor);
        assertNotNull(detail);
        current = detail;
        getInstrumentation().waitForIdleSync();
        assertEquals(TEST_ID, ((TextView) detail.findViewById(R.id.tvEmployeeId)).getText().toString());
        Activity edit = open(EditNhanSuActivity.class, detail, R.id.btnEdit);
        assertFalse(edit.findViewById(R.id.edtEmployeeId).isEnabled());
        assertEquals("Đại học", ((Spinner) edit.findViewById(R.id.spinnerEducation)).getSelectedItem());
        selectEducation(edit, "Thạc sĩ");
        enter(edit, R.id.edtFullName, "Nguyễn Thị Hà Đã Sửa");
        enter(edit, R.id.edtSalary, "15000000,5");
        click(edit, R.id.btnSave);
        current = detail;
        assertEquals("Nguyễn Thị Hà Đã Sửa", ((TextView) detail.findViewById(R.id.tvName)).getText().toString());
        assertEquals("Thạc sĩ", ((TextView) detail.findViewById(R.id.tvEducation)).getText().toString());
        click(detail, R.id.btnBack);
        current = main;
        enter(main, R.id.edtSearch, TEST_ID);
        assertEquals(1, list.getAdapter().getCount());
        NhanSu updated = (NhanSu) list.getAdapter().getItem(0);
        assertEquals("Nguyễn Thị Hà Đã Sửa", updated.getHoTen());
        assertEquals(15000000.5, updated.getMucLuong(), 0.001);
        assertEquals("Thạc sĩ", updated.getTrinhDo());
        // Mã trùng phải bị chặn và giữ nguyên hồ sơ đã sửa.
        add = open(AddNhanSuActivity.class, main, R.id.btnAddNhanSu);
        fillValid(add);
        click(add, R.id.btnSave);
        assertNotNull(((EditText) add.findViewById(R.id.edtEmployeeId)).getError());
        click(add, R.id.btnBack);
        current = main;
    }

    private Activity openFirstDetail() {
        ListView list = main.findViewById(R.id.listNhanSu);
        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(
                DetailNhanSuActivity.class.getName(), null, false);
        getInstrumentation().runOnMainSync(() -> list.performItemClick(
                list.getAdapter().getView(0, null, list), 0, 0));
        Activity detail = monitor.waitForActivityWithTimeout(5000);
        getInstrumentation().removeMonitor(monitor);
        assertNotNull(detail);
        current = detail;
        getInstrumentation().waitForIdleSync();
        waitForFocus(detail);
        return detail;
    }

    public void testLegacyEducationIsPreservedWhenEditing() {
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            database.insertNhanSu(new NhanSu(TEST_ID, "Hồ sơ cũ", 2000, "Nam", "Hà Nội",
                    "Chứng chỉ nghề", "", 100, "Dự án"));
        }
        enter(main, R.id.edtSearch, TEST_ID);
        Activity detail = openFirstDetail();
        Activity edit = open(EditNhanSuActivity.class, detail, R.id.btnEdit);
        assertEquals("Chứng chỉ nghề", ((Spinner) edit.findViewById(R.id.spinnerEducation)).getSelectedItem());
        click(edit, R.id.btnSave);
        current = detail;
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertEquals("Chứng chỉ nghề", database.getNhanSuById(TEST_ID).getTrinhDo());
        }
    }

    private void assertSortOrder(int mode, String... suffixes) {
        getInstrumentation().runOnMainSync(() -> ((Spinner) main.findViewById(R.id.spinnerSort)).setSelection(mode));
        getInstrumentation().waitForIdleSync();
        ListView list = main.findViewById(R.id.listNhanSu);
        assertEquals(suffixes.length, list.getAdapter().getCount());
        for (int i = 0; i < suffixes.length; i++) {
            assertEquals(TEST_ID + suffixes[i], ((NhanSu) list.getAdapter().getItem(i)).getMaNhanSu());
        }
    }

    public void testAllSortModesWithSearchAndEqualSalaries() {
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            String[] suffixes = {"_D", "_B", "_A", "_C"};
            double[] amounts = {20.5, 9, 100, 20.5};
            for (int i = 0; i < suffixes.length; i++) {
                database.insertNhanSu(new NhanSu(TEST_ID + suffixes[i], "Kiểm thử sắp xếp", 2000,
                        "Nam", "Hà Nội", "Đại học", "", amounts[i], "Dự án"));
            }
        }
        enter(main, R.id.edtSearch, TEST_ID);
        assertSortOrder(0, "_A", "_B", "_C", "_D");
        assertSortOrder(1, "_B", "_C", "_D", "_A");
        assertSortOrder(2, "_A", "_C", "_D", "_B");
        assertSortOrder(0, "_A", "_B", "_C", "_D");
        enter(main, R.id.edtSearch, TEST_ID + "_C");
        assertSortOrder(1, "_C");
        assertSortOrder(2, "_C");
        enter(main, R.id.edtSearch, TEST_ID + "_NOT_FOUND");
        assertSortOrder(0);
        assertSortOrder(1);
        assertSortOrder(2);
        enter(main, R.id.edtSearch, TEST_ID);
        assertSortOrder(2, "_A", "_C", "_D", "_B");
    }

    private File createTestPhoto(String name, int color) throws Exception {
        File source = new File(getInstrumentation().getTargetContext().getCacheDir(), name);
        Bitmap bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        try (FileOutputStream output = new FileOutputStream(source)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output));
        } finally {
            bitmap.recycle();
        }
        return source;
    }

    private void chooseTestPhoto(Activity form, File source) throws Exception {
        IntentFilter filter = new IntentFilter(Intent.ACTION_OPEN_DOCUMENT);
        filter.addCategory(Intent.CATEGORY_OPENABLE);
        filter.addDataType("image/*");
        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(filter,
                new Instrumentation.ActivityResult(Activity.RESULT_OK, new Intent().setData(Uri.fromFile(source))), true);
        try {
            click(form, R.id.btnChoosePhoto);
            assertEquals("Không mở yêu cầu chọn ảnh", 1, monitor.getHits());
            assertTrue(((ImageView) form.findViewById(R.id.imgAvatar)).getDrawable() instanceof BitmapDrawable);
        } finally {
            getInstrumentation().removeMonitor(monitor);
        }
    }

    public void testPhotoSelectionSaveAndCancelReplacement() throws Exception {
        File source = createTestPhoto("ui-test-avatar.png", 0xFF155EEF);
        File replacement = createTestPhoto("ui-test-avatar-replacement.png", 0xFF22AA55);
        try {
            Activity add = open(AddNhanSuActivity.class, main, R.id.btnAddNhanSu);
            fillValid(add);
            chooseTestPhoto(add, source);
            assertTrue(source.delete());
            click(add, R.id.btnSave);
            String savedPath;
            try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
                savedPath = database.getNhanSuById(TEST_ID).getAnh();
                assertFalse(savedPath.isEmpty());
                assertTrue(new File(savedPath).exists());
            }
            enter(main, R.id.edtSearch, TEST_ID);
            Activity detail = openFirstDetail();
            assertTrue(((ImageView) detail.findViewById(R.id.imgAvatar)).getDrawable() instanceof BitmapDrawable);
            File directory = new File(getInstrumentation().getTargetContext().getFilesDir(), "avatars");
            int savedCount = directory.list().length;
            Activity edit = open(EditNhanSuActivity.class, detail, R.id.btnEdit);
            chooseTestPhoto(edit, replacement);
            assertEquals(savedCount + 1, directory.list().length);
            click(edit, R.id.btnBack);
            current = detail;
            assertEquals(savedCount, directory.list().length);
            try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
                assertEquals(savedPath, database.getNhanSuById(TEST_ID).getAnh());
            }
            edit = open(EditNhanSuActivity.class, detail, R.id.btnEdit);
            chooseTestPhoto(edit, replacement);
            click(edit, R.id.btnSave);
            current = detail;
            assertFalse(new File(savedPath).exists());
            try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
                String newPath = database.getNhanSuById(TEST_ID).getAnh();
                assertFalse(savedPath.equals(newPath));
                assertTrue(new File(newPath).exists());
            }
        } finally {
            source.delete();
            replacement.delete();
        }
    }

    private void clickDialogButton(String label) {
        boolean clicked = false;
        // Accessibility kết nối bất đồng bộ; chờ cửa sổ dialog thực sự sẵn sàng.
        long deadline = SystemClock.uptimeMillis() + 5000;
        while (!clicked && SystemClock.uptimeMillis() < deadline) {
            AccessibilityNodeInfo root = getInstrumentation().getUiAutomation().getRootInActiveWindow();
            if (root != null) {
                for (AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText(label)) {
                    if (label.equalsIgnoreCase(String.valueOf(node.getText()))) {
                        AccessibilityNodeInfo target = node;
                        while (target != null && !clicked) {
                            if (target.isClickable()) {
                                clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                            target = target.getParent();
                        }
                        if (clicked) break;
                    }
                }
            }
            if (!clicked) SystemClock.sleep(100);
        }
        assertTrue("Không nhấn được nút " + label, clicked);
        getInstrumentation().waitForIdleSync();
    }

    private void longPressFirstRow(ListView list) {
        waitForFocus(main);
        int[] point = new int[2];
        getInstrumentation().runOnMainSync(() -> {
            View row = list.getChildAt(0);
            assertNotNull(row);
            row.getLocationOnScreen(point);
            point[0] += row.getWidth() / 2;
            point[1] += row.getHeight() / 2;
        });
        long start = SystemClock.uptimeMillis();
        MotionEvent down = MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN, point[0], point[1], 0);
        down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try {
            assertTrue(getInstrumentation().getUiAutomation().injectInputEvent(down, true));
        } finally {
            down.recycle();
        }
        SystemClock.sleep(ViewConfiguration.getLongPressTimeout() + 200);
        MotionEvent up = MotionEvent.obtain(start, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, point[0], point[1], 0);
        up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try {
            assertTrue(getInstrumentation().getUiAutomation().injectInputEvent(up, true));
        } finally {
            up.recycle();
        }
        getInstrumentation().waitForIdleSync();
    }

    public void testBothDeletePathsRequireConfirmation() {
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            database.insertNhanSu(new NhanSu(TEST_ID, "Nhân sự kiểm thử", 2000, "Nam",
                    "Hà Nội", "Đại học", "", 100, "Dự án"));
        }
        enter(main, R.id.edtSearch, TEST_ID);
        ListView list = main.findViewById(R.id.listNhanSu);
        longPressFirstRow(list);
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNotNull(database.getNhanSuById(TEST_ID));
        }
        clickDialogButton("XÓA");
        clickDialogButton("HỦY");
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNotNull(database.getNhanSuById(TEST_ID));
        }
        // Xác nhận xóa qua thao tác nhấn giữ.
        longPressFirstRow(list);
        clickDialogButton("XÓA");
        clickDialogButton("ĐỒNG Ý");
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNull(database.getNhanSuById(TEST_ID));
            database.insertNhanSu(new NhanSu(TEST_ID, "Nhân sự kiểm thử", 2000, "Nam",
                    "Hà Nội", "Đại học", "", 100, "Dự án"));
        }
        enter(main, R.id.edtSearch, "");
        enter(main, R.id.edtSearch, TEST_ID);
        Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(
                DetailNhanSuActivity.class.getName(), null, false);
        getInstrumentation().runOnMainSync(() -> list.performItemClick(
                list.getAdapter().getView(0, null, list), 0, 0));
        Activity detail = monitor.waitForActivityWithTimeout(5000);
        getInstrumentation().removeMonitor(monitor);
        assertNotNull(detail);
        current = detail;
        getInstrumentation().waitForIdleSync();
        click(detail, R.id.btnDelete);
        clickDialogButton("HỦY");
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNotNull(database.getNhanSuById(TEST_ID));
        }
        click(detail, R.id.btnDelete);
        clickDialogButton("ĐỒNG Ý");
        waitForClosed(detail);
        waitForFocus(main);
        current = main;
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNull(database.getNhanSuById(TEST_ID));
        }
        assertEquals(0, list.getAdapter().getCount());
    }
}
