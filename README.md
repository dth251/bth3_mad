# Quản lý nhân sự — Bài thực hành 3

Ứng dụng Android ngoại tuyến bằng **Java + XML + SQLite**, dành cho bài tập của sinh viên **Nguyễn Duy Thắng — B23DCCE087**. Thông tin sinh viên xuất hiện ở cả bốn màn hình. Project đã được triển khai trực tiếp trong thư mục này; không cần sao chép các đoạn mã từ tài liệu.

## 1. Tạo / mở project Android Studio

Với project đã có: chọn **File → Open**, chọn thư mục `bth3` chứa `settings.gradle`, chờ Gradle Sync, sau đó chọn module `app` và nhấn Run.

Nếu muốn tự làm lại từ đầu: tạo **Empty Views Activity**, chọn Java, package `com.example.quanlynhansu`, minimum SDK **24**, rồi dùng cấu trúc và các file trong project này. Không chọn template Compose.

Cấu hình đã sử dụng:

| Thành phần | Giá trị |
| --- | --- |
| Ngôn ngữ / giao diện | Java 17 / XML Views |
| Database | SQLiteOpenHelper, `QuanLyNhanSu.db` |
| Android Gradle Plugin | 8.4.0 |
| Gradle Wrapper | 8.7, đã có JAR và script |
| SDK biên dịch / SDK đích | 34 / 34 |
| Android tối thiểu | API 24 (Android 7.0) |
| Thư viện ngoài | Không có |

Trong **Settings → Build, Execution, Deployment → Build Tools → Gradle**, chọn **Gradle JDK 17** (JDK 21 cũng chạy được với Gradle 8.7). Nếu Android Studio hiện tại dùng JBR 25/26, hãy chọn JDK 17 riêng. Trong SDK Manager cần cài **Android SDK Platform 34**, **Build-Tools 34.0.0**, **Platform-Tools**, và một system image cho máy ảo.

`local.properties` chứa vị trí Android SDK trên máy hiện tại và được bỏ qua bởi Git. Khi chuyển máy, Android Studio có thể tạo lại file này. `android.overridePathCheck=true` trong `gradle.properties` cho phép đường dẫn bài tập có dấu. Wrapper Windows đã dùng classpath tương đối để chạy được trong đường dẫn này.

Trên máy hiện tại, `.idea/gradle.xml` và `.gradle/config.properties` đã cấu hình Gradle dùng JDK 17 đã cài. Đây là cấu hình cục bộ được bỏ qua bởi Git; khi chuyển máy cần chọn lại Gradle JDK phù hợp.

Tham khảo cấu hình tương thích: [Android Gradle Plugin 8.4](https://developer.android.com/build/releases/agp-8-4-0-release-notes).

## 2. Cấu trúc thư mục

```text
bth3/
├── prompt_quan_ly_nhan_su.md          Yêu cầu gốc
├── README.md                        Hướng dẫn 14 bước
├── KIEM_THU.md                      Kết quả kiểm tra thực tế
├── settings.gradle                  Repository, tên project, module app
├── build.gradle                     Phiên bản Android Gradle Plugin
├── gradle.properties                Bộ nhớ Gradle, hỗ trợ đường dẫn có dấu
├── gradlew / gradlew.bat             Khởi chạy Gradle Wrapper
├── gradle/wrapper/                   Cấu hình và JAR của Wrapper
└── app/
    ├── build.gradle                 SDK, Java, applicationId, bộ kiểm thử
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/quanlynhansu/
        │   │   ├── NhanSu.java
        │   │   ├── DatabaseHelper.java
        │   │   ├── NhanSuAdapter.java
        │   │   ├── PhotoHelper.java
        │   │   ├── BaseNhanSuFormActivity.java
        │   │   ├── MainActivity.java
        │   │   ├── AddNhanSuActivity.java
        │   │   ├── DetailNhanSuActivity.java
        │   │   └── EditNhanSuActivity.java
        │   └── res/
        │       ├── layout/          Bốn màn hình, dòng ListView, form dùng chung, thông tin SV
        │       ├── drawable/        Nền card, nút, ô nhập, ảnh mặc định
        │       └── values/          strings.xml, colors.xml, styles.xml
        └── androidTest/java/com/example/quanlynhansu/
            ├── DatabaseHelperTest.java
            └── NhanSuFlowTest.java
```

## 3. Model NhanSu.java

`app/src/main/java/com/example/quanlynhansu/NhanSu.java` chứa đủ chín thuộc tính: mã, tên, năm sinh, giới tính, quê quán, trình độ, ảnh, lương và phòng ban; có constructor đầy đủ, getter và setter. `getLuongHienThi()` định dạng tiền theo tiếng Việt và thêm đơn vị VNĐ; giá trị gốc vẫn là `double`.

## 4. DatabaseHelper.java

Kế thừa `SQLiteOpenHelper`; `onCreate()` tạo bảng:

```sql
CREATE TABLE NhanSu (
    maNhanSu TEXT PRIMARY KEY NOT NULL,
    hoTen TEXT NOT NULL,
    namSinh INTEGER,
    gioiTinh TEXT,
    queQuan TEXT,
    trinhDo TEXT,
    anh TEXT,
    mucLuong REAL,
    phongBan TEXT
);
```

| Phương thức | Chức năng |
| --- | --- |
| `insertNhanSu()` | ContentValues + INSERT; không ghi đè mã trùng |
| `getAllNhanSu()` | Đọc danh sách bằng Cursor |
| `getNhanSuById()` | SELECT theo khóa chính, tham số `?` |
| `updateNhanSu()` | UPDATE các thuộc tính; giữ nguyên mã |
| `deleteNhanSu()` | DELETE theo mã |
| `searchNhanSu()` | Tìm chuỗi con theo mã hoặc tên |
| `sortNhanSuBySalaryASC()` | SQL ORDER BY mucLuong ASC |
| `sortNhanSuBySalaryDESC()` | SQL ORDER BY mucLuong DESC |
| `getNhanSu(keyword, sortMode)` | Kết hợp lọc và sắp xếp |

Tìm kiếm chuẩn hóa Unicode NFC và chuyển chữ thường bằng Locale tiếng Việt, vì SQLite `NOCASE` mặc định chỉ xử lý ASCII. Tìm kiếm có phân biệt dấu: `hà` tìm `HÀ`, nhưng `ha` không tự chuyển thành `hà`. Ký tự `%`, `_`, dấu nháy được hiểu là văn bản tìm kiếm bình thường. Lương được sắp xếp trong SQLite theo kiểu REAL, có mã làm thứ tự phụ khi hai mức lương bằng nhau.

Cursor được đóng bằng try-with-resources. Database thuộc quyền quản lý của helper; Activity đóng helper trong `onDestroy()`, không đóng SQLiteDatabase giữa chừng khi Cursor còn dùng. Dữ liệu ở bộ nhớ riêng của ứng dụng, không mất khi đóng/mở hoặc force-stop. Gỡ ứng dụng / xóa dữ liệu ứng dụng sẽ xóa database và ảnh.

Schema hiện là phiên bản 1. `onUpgrade()` chủ động báo lỗi nếu có yêu cầu nâng cấp chưa được hỗ trợ, giữ nguyên dữ liệu thay vì DROP TABLE. Khi bổ sung schema trong tương lai, phải thêm migration `ALTER TABLE` rồi mới tăng `DATABASE_VERSION`.

## 5. Các layout XML

| File trong `res/layout/` | Chức năng |
| --- | --- |
| `activity_main.xml` | Tiêu đề, sinh viên, tìm kiếm, Spinner sắp xếp, ListView, trạng thái trống, nút thêm |
| `activity_add_nhan_su.xml` | ScrollView cho form, nút thêm / làm mới / quay lại |
| `activity_detail_nhan_su.xml` | Đầy đủ chín thông tin, nút chỉnh sửa / xóa / quay lại |
| `activity_edit_nhan_su.xml` | ScrollView cho form sửa, nút cập nhật / hủy |
| `item_nhan_su.xml` | Ảnh, mã, họ tên, phòng ban, mức lương của một dòng |
| `include_nhan_su_form.xml` | Các trường nhập dùng chung cho thêm và sửa |
| `include_student.xml` | MSSV và họ tên sinh viên dùng chung cả bốn màn hình |

`strings.xml` chứa văn bản tiếng Việt và đúng bốn phòng ban: **Hành chính, Phần mềm, Kỹ thuật, Dự án**. `colors.xml` chứa bảng màu xanh–trắng. `styles.xml` chứa theme, style nhãn, ô nhập, nút và phần chi tiết. Các file `drawable/bg_*.xml` tạo nền bo góc; `ic_people.xml` là ảnh mặc định dạng vector, không cần tải ảnh qua mạng.

`values-v27/styles.xml` bổ sung cấu hình thanh điều hướng cho API 27 trở lên; Android 7.0 dùng theme cơ sở. `xml/data_extraction_rules.xml` cấu hình không sao lưu / chuyển dữ liệu riêng của ứng dụng trên Android 12 trở lên.

## 6. NhanSuAdapter.java

Kế thừa `BaseAdapter`, dùng `ArrayList<NhanSu>` và layout `item_nhan_su.xml`. ViewHolder tái sử dụng các view của dòng. `updateData()` thay nội dung và gọi `notifyDataSetChanged()`. Mỗi lần bind đều đặt ảnh mặc định trước khi đọc ảnh của nhân sự để tránh hiển thị nhầm ảnh khi tái sử dụng dòng.

## 7. MainActivity.java

`onCreate()` khởi tạo helper, adapter, ListView, bộ lắng nghe tìm kiếm và Spinner. Nhấn thêm mở Add; nhấn một dòng mở Detail bằng Intent chứa `maNhanSu`. Nhấn giữ mở AlertDialog, chọn Đồng ý mới gọi DELETE. Khi Hủy, dữ liệu giữ nguyên.

`onResume()` đọc lại dữ liệu, nên khi trở về từ màn hình thêm/sửa/xóa, danh sách được làm mới ngay. Từ khóa và kiểu sắp xếp đang chọn cùng tham gia truy vấn. ListView dùng empty view cho trường hợp chưa có dữ liệu hoặc không có kết quả tìm kiếm.

## 8. AddNhanSuActivity.java và form dùng chung

Add sử dụng `BaseNhanSuFormActivity` để tránh lặp toàn bộ mã nhập liệu trong Add và Edit. Lớp cơ sở ánh xạ ID XML, thiết lập RadioGroup, Spinner, chọn ảnh, kiểm tra và lưu dữ liệu.

Kiểm tra đầu vào:

- Mã và tên bắt buộc; bỏ khoảng trắng ở đầu/cuối.
- Mã là khóa chính, phân biệt hoa/thường theo schema SQLite; trùng đúng mã sẽ bị từ chối. Tìm kiếm vẫn không phân biệt hoa/thường.
- Năm sinh bắt buộc, là số nguyên từ **1900 đến năm hiện tại**.
- Lương bắt buộc, là số hữu hạn không âm; chấp nhận dấu chấm hoặc dấu phẩy thập phân, ví dụ `12000000` hoặc `12000000,5`. Không nhập dấu phân cách hàng nghìn.
- Quê quán, trình độ và ảnh có thể để trống; mặc định giới tính Nam, phòng ban Hành chính.

Nút THÊM lưu SQLite, hiện Toast rồi quay lại Main. LÀM MỚI xóa form, lỗi nhập, ảnh đang chọn và khôi phục các lựa chọn mặc định. QUAY LẠI hủy nhập.

`PhotoHelper.java` mở bộ chọn tài liệu hệ thống bằng `ACTION_OPEN_DOCUMENT` với MIME `image/*`. Ảnh được đọc nhỏ lại tối đa 1024 px, xử lý hướng EXIF rồi lưu JPEG trong `files/avatars/`. SQLite lưu đường dẫn nội bộ, không cần quyền đọc toàn bộ thư viện hay kết nối Internet. Hủy bộ chọn giữ ảnh cũ; ảnh lỗi sẽ hiện Toast. Ảnh không tồn tại dùng ảnh mặc định. Ảnh mới chưa lưu được dọn khi hủy/thoát form; ảnh cũ được dọn sau khi cập nhật thành công hoặc xóa nhân sự.

Tham khảo API chọn tài liệu: [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).

## 9. DetailNhanSuActivity.java

Nhận mã qua Intent, đọc hồ sơ và hiển thị toàn bộ thông tin. `onResume()` luôn đọc lại để cập nhật sau khi sửa. CHỈNH SỬA truyền mã sang Edit; XÓA yêu cầu AlertDialog xác nhận; QUAY LẠI gọi `finish()`. Nếu bản ghi không còn tồn tại, thông báo rồi quay lại.

## 10. EditNhanSuActivity.java

Đọc dữ liệu và điền form, khóa EditText mã nhân sự. Sau kiểm tra hợp lệ, `updateNhanSu()` dùng UPDATE với WHERE theo mã gốc. Cập nhật thành công trở về Detail, Detail đọc lại dữ liệu. Khi tiếp tục quay về Main, danh sách cũng đọc lại. HỦY không ghi thay đổi; ảnh cũ được giữ nguyên nếu chưa cập nhật.

Android tự lưu các trường có ID khi xoay màn hình; đường dẫn ảnh đang nhập được lưu thêm vào Bundle bằng `onSaveInstanceState()`.

## 11. AndroidManifest.xml và Gradle

Manifest khai báo đầy đủ bốn Activity; chỉ Main có `exported=true` và bộ lọc MAIN/LAUNCHER. Form dùng `adjustResize` kết hợp ScrollView. Không khai báo quyền Internet, camera hay storage. Ứng dụng không có đăng nhập, Firebase, Room, Kotlin hoặc Compose.

`app/build.gradle` cấu hình SDK, Java và bộ kiểm thử native Android. Hai thư viện `android.test.runner` / `android.test.base` là framework kiểm thử có sẵn của Android; mã ứng dụng không phụ thuộc thư viện UI ngoài.

## 12. Chạy ứng dụng

**Android Studio:** mở thư mục gốc, Sync, chọn thiết bị trong Device Manager hoặc điện thoại thật rồi Run `app`. Điện thoại thật cần bật Developer options → USB debugging và chấp nhận khóa RSA khi kết nối.

**PowerShell:** đặt `JAVA_HOME` trỏ tới JDK 17 đã cài, sau đó:

```powershell
# Ví dụ đường dẫn JDK 17 trên máy hiện tại; điều chỉnh khi chuyển máy.
$env:JAVA_HOME = Join-Path $env:USERPROFILE '.jdks\ms-17.0.19'
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`. Có thể cài bằng `adb install -r` hoặc chép APK sang điện thoại để cài. Máy phải có SDK để build; lần đầu Gradle có thể cần mạng để tải công cụ, còn ứng dụng chạy hoàn toàn ngoại tuyến.

## 13. Kiểm thử chức năng

Chạy kiểm tra tĩnh và bộ kiểm thử trên một máy ảo/điện thoại đang kết nối:

```powershell
.\gradlew.bat assembleDebug lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

Bộ kiểm thử database dùng database có tiền tố `test_`, độc lập dữ liệu ứng dụng. Bộ kiểm thử Activity tạo và dọn duy nhất mã `__UI_TEST_087__`.

| Thử thủ công | Kết quả mong đợi |
| --- | --- |
| Mở lần đầu | Hiện trạng thái chưa có nhân sự và thông tin sinh viên |
| Thêm NS001 / Nguyễn Duy Thắng / 2000 / 12000000 / Phần mềm | Lưu thành công, xuất hiện trong danh sách |
| Thêm lại đúng NS001 | Báo mã đã tồn tại, không ghi đè |
| Mã/tên toàn dấu cách; năm trống, 1899, năm tương lai; lương trống, âm, chữ | Chỉ rõ ô sai và không lưu |
| Lương 0 hoặc 12000000,5 | Lưu được, hiển thị tiền đúng |
| Tìm `ns001`, tên có chữ hoa/thường tiếng Việt | Đúng hồ sơ; xóa từ khóa để hiện lại danh sách |
| Có các mức lương 9 / 20,5 / 100; sắp xếp hai chiều | Thứ tự số đúng, vẫn giữ bộ lọc tìm kiếm |
| Mở chi tiết, sửa tên/lương/phòng ban | Mã bị khóa; Detail và Main hiển thị dữ liệu mới |
| Sửa nhưng nhấn HỦY | Dữ liệu cũ và ảnh cũ giữ nguyên |
| Nhấn giữ dòng hoặc nhấn XÓA trong Detail | Luôn có xác nhận; Hủy giữ hồ sơ, Đồng ý xóa và cập nhật danh sách |
| Chọn ảnh từ thư viện; đóng và mở app | Ảnh còn hiển thị |
| Chọn ảnh mới rồi làm mới/hủy; hủy bộ chọn | Không lưu ảnh mới vào hồ sơ |
| Đóng app/force-stop, mở lại | Hồ sơ và ảnh đã lưu vẫn còn |
| Form ở màn hình nhỏ, bàn phím mở | Có thể cuộn đến các trường và nút |
| Xoay thiết bị khi nhập hoặc sửa | Giữ dữ liệu đang nhập và ảnh đã chọn |
| Kiểm tra từng màn hình | Đủ MSSV và họ tên sinh viên |

Không dùng Clear data / gỡ cài đặt để kiểm tra khả năng giữ dữ liệu, vì các thao tác đó được Android thiết kế để xóa dữ liệu riêng của ứng dụng.

## 14. Luồng hoạt động

```text
Nhập form → kiểm tra hợp lệ → NhanSu (Model)
         → DatabaseHelper → SQLiteDatabase → file QuanLyNhanSu.db

Main.onResume / tìm kiếm / sắp xếp
         → DatabaseHelper → Cursor → ArrayList<NhanSu>
         → NhanSuAdapter → ListView → các dòng item_nhan_su.xml

Main → Intent(maNhanSu) → Detail → Intent(maNhanSu) → Edit
                                                    ↓ UPDATE
Main ← đọc lại danh sách ← Detail ← đọc lại hồ sơ ← finish()
```

Model biểu diễn một hồ sơ; SQLiteOpenHelper quản lý việc tạo/mở database; DatabaseHelper thực hiện CRUD và chuyển Cursor thành Model; Adapter chuyển Model thành View; ListView hiển thị các View. Activity điều phối giao diện, sự kiện và Intent. Ảnh nằm trong bộ nhớ nội bộ; cột `anh` chỉ lưu đường dẫn để database nhẹ và dễ giải thích khi bảo vệ bài tập.
