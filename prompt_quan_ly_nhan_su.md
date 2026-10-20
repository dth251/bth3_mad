Bạn là một lập trình viên Android chuyên nghiệp, có kinh nghiệm phát triển ứng dụng Android bằng Java, XML và SQLite trong Android Studio.

Hãy giúp tôi xây dựng một ứng dụng **QUẢN LÝ NHÂN SỰ** hoàn chỉnh, có thể chạy trực tiếp trên Android Studio, đáp ứng đầy đủ các yêu cầu sau.

# 1. Công nghệ sử dụng

- Ngôn ngữ lập trình: Java.
- Thiết kế giao diện: XML.
- Môi trường: Android Studio.
- Cơ sở dữ liệu: SQLite.
- Sử dụng lớp SQLiteOpenHelper để quản lý cơ sở dữ liệu.
- Sử dụng ListView, ArrayList và Custom Adapter để hiển thị danh sách nhân sự.
- Sử dụng Intent để chuyển giữa các Activity.
- Không sử dụng Firebase, Room Database, Kotlin hoặc Jetpack Compose.
- Code đơn giản, dễ hiểu, phù hợp với sinh viên đang học lập trình Android.

# 2. Thông tin nhân sự

Mỗi nhân sự bao gồm 9 thuộc tính:

1. Mã nhân sự (String): Không được trùng lặp, là khóa chính.
2. Họ và tên (String).
3. Năm sinh (int).
4. Giới tính (Nam/Nữ/Khác).
5. Quê quán (String).
6. Trình độ (String).
7. Ảnh đại diện (chọn ảnh từ thư viện điện thoại).
8. Mức lương (double).
9. Phòng ban (Spinner) gồm đúng 4 lựa chọn:
   - Hành chính
   - Phần mềm
   - Kỹ thuật
   - Dự án

# 3. Cơ sở dữ liệu SQLite

Tạo lớp DatabaseHelper kế thừa SQLiteOpenHelper.

Tên database: QuanLyNhanSu.db

Tên bảng: NhanSu

Các cột:

- maNhanSu TEXT PRIMARY KEY
- hoTen TEXT NOT NULL
- namSinh INTEGER
- gioiTinh TEXT
- queQuan TEXT
- trinhDo TEXT
- anh TEXT
- mucLuong REAL
- phongBan TEXT

Triển khai các phương thức:

- onCreate(): Tạo bảng nhân sự.
- onUpgrade(): Xử lý nâng cấp database.
- insertNhanSu(): Thêm nhân sự.
- getAllNhanSu(): Lấy danh sách.
- getNhanSuById(): Lấy thông tin một nhân sự.
- updateNhanSu(): Cập nhật thông tin.
- deleteNhanSu(): Xóa nhân sự.
- searchNhanSu(): Tìm kiếm theo mã hoặc họ tên.
- sortNhanSuBySalaryASC(): Sắp xếp mức lương tăng dần.
- sortNhanSuBySalaryDESC(): Sắp xếp mức lương giảm dần.

Sử dụng ContentValues, SQLiteDatabase và Cursor đúng cách.

Dữ liệu phải được lưu lâu dài trong SQLite. Sau khi đóng và mở lại ứng dụng, dữ liệu vẫn còn.

# 4. Thiết kế giao diện

Thiết kế giao diện đẹp, hiện đại, dễ sử dụng, bố cục rõ ràng nhưng không quá phức tạp.

Tất cả giao diện đều bắt buộc phải hiển thị thông tin sinh viên:

MSSV: B23DCCE087

Họ tên: Nguyễn Duy Thắng

Thông tin sinh viên phải xuất hiện trên tất cả màn hình, có thể đặt ở phần đầu hoặc cuối giao diện.

## Giao diện 1: MainActivity – Danh sách nhân sự

Hiển thị:

- Tiêu đề: QUẢN LÝ NHÂN SỰ.
- MSSV và họ tên sinh viên.
- Ô SearchView hoặc EditText để tìm kiếm.
- ListView hiển thị danh sách nhân sự.
- Mỗi dòng nhân sự gồm ảnh đại diện, mã nhân sự, họ tên, phòng ban và mức lương.
- Nút THÊM NHÂN SỰ.
- Chức năng sắp xếp theo mức lương tăng dần, giảm dần.

Các sự kiện:

- Nhấn nút THÊM để mở AddNhanSuActivity.
- Nhấn vào một nhân sự trong ListView để mở DetailNhanSuActivity.
- Nhấn giữ (Long Click) vào một nhân sự để hiển thị AlertDialog xác nhận xóa.
- Nếu chọn Đồng ý thì xóa nhân sự khỏi SQLite và cập nhật ListView.
- Nếu chọn Hủy thì đóng Dialog, không xóa.
- Khi nhập từ khóa tìm kiếm, danh sách được lọc theo mã hoặc họ tên.
- Khi chọn sắp xếp, ListView cập nhật theo mức lương.

## Giao diện 2: AddNhanSuActivity – Thêm nhân sự

Giao diện gồm:

- Tiêu đề: THÊM NHÂN SỰ.
- MSSV và họ tên sinh viên.
- EditText nhập mã nhân sự.
- EditText nhập họ và tên.
- EditText nhập năm sinh.
- RadioGroup chọn giới tính.
- EditText nhập quê quán.
- EditText nhập trình độ.
- ImageView hiển thị ảnh đại diện.
- Button chọn ảnh từ thư viện.
- EditText nhập mức lương, bàn phím số.
- Spinner chọn phòng ban.
- Button THÊM.
- Button LÀM MỚI.
- Button QUAY LẠI.

Yêu cầu xử lý:

- Không được bỏ trống mã nhân sự và họ tên.
- Mã nhân sự không được trùng trong SQLite.
- Kiểm tra năm sinh hợp lệ, không lớn hơn năm hiện tại.
- Mức lương phải là số và không được âm.
- Người dùng chọn ảnh từ thư viện điện thoại và hiển thị ảnh lên ImageView.
- Lưu tham chiếu ảnh phù hợp để vẫn hiển thị được sau khi mở lại ứng dụng.
- Nhấn THÊM để lưu vào SQLite.
- Hiển thị Toast thông báo thành công hoặc thất bại.
- Sau khi thêm thành công, quay về MainActivity và cập nhật danh sách.
- Nhấn LÀM MỚI để xóa dữ liệu đang nhập.
- Nhấn QUAY LẠI để trở về màn hình chính.

## Giao diện 3: DetailNhanSuActivity – Chi tiết nhân sự

Khi người dùng nhấn vào một dòng trong ListView, mở màn hình chi tiết.

Hiển thị toàn bộ thông tin:

- Ảnh đại diện.
- Mã nhân sự.
- Họ và tên.
- Năm sinh.
- Giới tính.
- Quê quán.
- Trình độ.
- Mức lương.
- Phòng ban.
- MSSV và họ tên sinh viên.

Có các nút:

- CHỈNH SỬA.
- XÓA.
- QUAY LẠI.

Nhấn CHỈNH SỬA để chuyển đến EditNhanSuActivity.

Nhấn XÓA phải xuất hiện hộp thoại xác nhận trước khi xóa.

## Giao diện 4: EditNhanSuActivity – Sửa thông tin nhân sự

Giao diện tương tự màn hình thêm nhân sự.

Yêu cầu:

- Tự động hiển thị thông tin nhân sự đang chọn.
- Có thể sửa họ tên, năm sinh, giới tính, quê quán, trình độ, ảnh, mức lương và phòng ban.
- Mã nhân sự không được sửa vì là khóa chính.
- Có nút CẬP NHẬT và HỦY.
- Kiểm tra dữ liệu đầu vào.
- Khi nhấn CẬP NHẬT, lưu thay đổi vào SQLite bằng câu lệnh UPDATE.
- Hiển thị thông báo cập nhật thành công.
- Sau khi cập nhật, màn hình chi tiết và danh sách phải hiển thị dữ liệu mới.

# 5. Lớp Model và Adapter

Tạo class NhanSu.java gồm đầy đủ 9 thuộc tính.

Yêu cầu:

- Constructor.
- Getter và Setter.
- Các phương thức cần thiết phục vụ hiển thị dữ liệu.

Tạo class NhanSuAdapter.java kế thừa BaseAdapter để:

- Hiển thị danh sách nhân sự bằng ListView.
- Hiển thị ảnh, mã, tên, phòng ban và mức lương.
- Sử dụng layout item_nhan_su.xml.
- Cập nhật danh sách sau khi thêm, sửa, xóa, tìm kiếm và sắp xếp.
- Xử lý hợp lý khi nhân sự không có ảnh.

# 6. Các chức năng bắt buộc

Ứng dụng phải thực hiện đầy đủ:

**CREATE – Thêm nhân sự**

- Nhập thông tin, kiểm tra hợp lệ và lưu vào SQLite.

**READ – Hiển thị nhân sự**

- Hiển thị danh sách nhân sự.
- Xem được thông tin chi tiết.

**UPDATE – Sửa nhân sự**

- Mở form chứa dữ liệu cũ.
- Chỉnh sửa và cập nhật vào database.

**DELETE – Xóa nhân sự**

- Có thể xóa bằng cách nhấn giữ một dòng trong ListView hoặc qua màn hình chi tiết.
- Bắt buộc xác nhận bằng AlertDialog.

**SEARCH – Tìm kiếm**

- Tìm kiếm nhân sự theo mã hoặc họ tên.
- Không phân biệt chữ hoa, chữ thường.
- Hiển thị danh sách phù hợp với từ khóa.

**SORT – Sắp xếp**

- Sắp xếp nhân sự theo mức lương tăng dần.
- Sắp xếp nhân sự theo mức lương giảm dần.
- Sắp xếp theo giá trị số, không phải chuỗi ký tự.

# 7. Cấu trúc project

Hãy tổ chức project thành các file rõ ràng, ví dụ:

Java:

- MainActivity.java
- AddNhanSuActivity.java
- DetailNhanSuActivity.java
- EditNhanSuActivity.java
- NhanSu.java
- NhanSuAdapter.java
- DatabaseHelper.java

XML:

- activity_main.xml
- activity_add_nhan_su.xml
- activity_detail_nhan_su.xml
- activity_edit_nhan_su.xml
- item_nhan_su.xml
- strings.xml
- colors.xml
- AndroidManifest.xml

Có thể bổ sung các file cần thiết khác, nhưng hãy giải thích chức năng của từng file.

# 8. Yêu cầu về giao diện

- Sử dụng bố cục LinearLayout, ConstraintLayout hoặc ScrollView hợp lý.
- Các form thêm và sửa sử dụng ScrollView để không bị che nội dung trên điện thoại nhỏ.
- Màu chủ đạo xanh dương và trắng.
- Nút bấm dễ nhìn, kích thước hợp lý.
- TextView và EditText có căn lề rõ ràng.
- Spinner hiển thị đầy đủ danh sách phòng ban.
- Màn hình danh sách có giao diện thân thiện.
- Có thông báo khi danh sách nhân sự trống.
- Không cần kết nối Internet.
- Giao diện hoàn toàn bằng tiếng Việt.

# 9. Yêu cầu về chất lượng code

- Code Java và XML phải đầy đủ, chạy được, không viết mã giả.
- Đảm bảo đúng các ID giữa XML và Java.
- Khai báo đầy đủ Activity trong AndroidManifest.xml.
- Import đầy đủ thư viện.
- Xử lý đúng Cursor, SQLiteDatabase và đóng tài nguyên khi cần.
- Không làm mất dữ liệu sau khi khởi động lại ứng dụng.
- Khi thêm, sửa, xóa, giao diện danh sách phải được cập nhật.
- Xử lý lỗi dữ liệu nhập vào.
- Xử lý việc chọn và hiển thị ảnh an toàn, phù hợp với phiên bản Android.
- Không cần đăng nhập, đăng ký tài khoản hoặc chức năng ngoài đề bài.
- Ưu tiên cách lập trình dễ hiểu, dễ giải thích khi bảo vệ bài tập.

# 10. Yêu cầu trình bày kết quả

Hãy hướng dẫn tôi xây dựng ứng dụng từ đầu đến cuối theo thứ tự sau:

Bước 1: Tạo project Android Studio bằng Java.

Bước 2: Trình bày cấu trúc thư mục project.

Bước 3: Viết class NhanSu.java.

Bước 4: Viết class DatabaseHelper.java, giải thích cách tạo bảng và các hàm CRUD.

Bước 5: Viết đầy đủ các layout XML.

Bước 6: Viết class NhanSuAdapter.java.

Bước 7: Viết MainActivity.java.

Bước 8: Viết AddNhanSuActivity.java.

Bước 9: Viết DetailNhanSuActivity.java.

Bước 10: Viết EditNhanSuActivity.java.

Bước 11: Cấu hình AndroidManifest.xml và các thành phần Gradle cần thiết.

Bước 12: Hướng dẫn chạy ứng dụng trên Android Emulator hoặc điện thoại thật.

Bước 13: Hướng dẫn kiểm thử từng chức năng thêm, sửa, xóa, tìm kiếm và sắp xếp.

Bước 14: Giải thích luồng hoạt động giữa SQLiteOpenHelper, Database, Model, Adapter, ListView và các Activity.

**LƯU Ý QUAN TRỌNG:**

- Viết code hoàn chỉnh cho từng file, không bỏ qua các phần xử lý.
- Không sử dụng các đoạn code minh họa chưa hoàn thiện.
- Không thay đổi công nghệ Java + XML + SQLite.
- Thực hiện đúng các chức năng trong yêu cầu.
- Tại tất cả các màn hình đều phải hiển thị MSSV và họ tên sinh viên.
- Đảm bảo ứng dụng có thể biên dịch và chạy thực tế trên Android Studio.
- Nếu câu trả lời quá dài, hãy chia thành nhiều phần theo thứ tự các bước và tiếp tục cho đến khi hoàn thành toàn bộ project.

Bây giờ hãy bắt đầu từ Bước 1 và hướng dẫn chi tiết, kèm toàn bộ code cần thiết.
