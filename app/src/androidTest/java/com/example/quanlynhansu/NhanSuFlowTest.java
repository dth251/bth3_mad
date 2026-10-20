package com.example.quanlynhansu;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/** Chạy các Activity thật; chỉ dọn bản ghi có mã kiểm thử riêng. */
@SuppressWarnings("deprecation")
public class NhanSuFlowTest extends InstrumentationTestCase {
    private static final String TEST_ID = "__UI_TEST_087__";
    private MainActivity main;
    private Activity current;

    @Override protected void setUp() throws Exception {
        super.setUp();
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            database.deleteNhanSu(TEST_ID);
        }
        Intent intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        main = (MainActivity) getInstrumentation().startActivitySync(intent);
        current = main;
        getInstrumentation().waitForIdleSync();
    }

    @Override protected void tearDown() throws Exception {
        getInstrumentation().runOnMainSync(() -> {
            if (current != null) current.finish();
            main.finish();
        });
        getInstrumentation().waitForIdleSync();
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            database.deleteNhanSu(TEST_ID);
        }
        super.tearDown();
    }

    private void click(Activity activity, int id) {
        getInstrumentation().runOnMainSync(() -> activity.findViewById(id).performClick());
        getInstrumentation().waitForIdleSync();
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
        return opened;
    }

    private void fillValid(Activity form) {
        enter(form, R.id.edtEmployeeId, TEST_ID);
        enter(form, R.id.edtFullName, "Nguyễn Thị Hà");
        enter(form, R.id.edtBirthYear, "2000");
        enter(form, R.id.edtHometown, "Hà Nội");
        enter(form, R.id.edtEducation, "Đại học");
        enter(form, R.id.edtSalary, "12000000");
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
        enter(edit, R.id.edtFullName, "Nguyễn Thị Hà Đã Sửa");
        enter(edit, R.id.edtSalary, "15000000,5");
        click(edit, R.id.btnSave);
        current = detail;
        assertEquals("Nguyễn Thị Hà Đã Sửa", ((TextView) detail.findViewById(R.id.tvName)).getText().toString());
        click(detail, R.id.btnBack);
        current = main;
        enter(main, R.id.edtSearch, TEST_ID);
        assertEquals(1, list.getAdapter().getCount());
        NhanSu updated = (NhanSu) list.getAdapter().getItem(0);
        assertEquals("Nguyễn Thị Hà Đã Sửa", updated.getHoTen());
        assertEquals(15000000.5, updated.getMucLuong(), 0.001);
        // Mã trùng phải bị chặn và giữ nguyên hồ sơ đã sửa.
        add = open(AddNhanSuActivity.class, main, R.id.btnAddNhanSu);
        fillValid(add);
        click(add, R.id.btnSave);
        assertNotNull(((EditText) add.findViewById(R.id.edtEmployeeId)).getError());
        click(add, R.id.btnBack);
        current = main;
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
                        clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (clicked) break;
                    }
                }
            }
            if (!clicked) SystemClock.sleep(100);
        }
        assertTrue("Không nhấn được nút " + label, clicked);
        getInstrumentation().waitForIdleSync();
    }

    public void testBothDeletePathsRequireConfirmation() {
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            database.insertNhanSu(new NhanSu(TEST_ID, "Nhân sự kiểm thử", 2000, "Nam",
                    "Hà Nội", "Đại học", "", 100, "Dự án"));
        }
        enter(main, R.id.edtSearch, TEST_ID);
        ListView list = main.findViewById(R.id.listNhanSu);
        getInstrumentation().runOnMainSync(() -> list.getOnItemLongClickListener()
                .onItemLongClick(list, list.getAdapter().getView(0, null, list), 0, 0));
        getInstrumentation().waitForIdleSync();
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNotNull(database.getNhanSuById(TEST_ID));
        }
        clickDialogButton("HỦY");
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNotNull(database.getNhanSuById(TEST_ID));
        }
        // Xác nhận xóa qua thao tác nhấn giữ.
        getInstrumentation().runOnMainSync(() -> list.getOnItemLongClickListener()
                .onItemLongClick(list, list.getAdapter().getView(0, null, list), 0, 0));
        getInstrumentation().waitForIdleSync();
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
        current = main;
        try (DatabaseHelper database = new DatabaseHelper(getInstrumentation().getTargetContext())) {
            assertNull(database.getNhanSuById(TEST_ID));
        }
        assertEquals(0, list.getAdapter().getCount());
    }
}
