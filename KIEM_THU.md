# Kết quả kiểm thử — 20/10/2026

## Môi trường

- Windows, JDK Microsoft 17.0.19, Gradle 8.7, Android Gradle Plugin 8.4.0.
- Compile / target SDK 34; minimum SDK 24.
- Máy ảo Pixel 10 Pro XL, Android 17 / API 37, system image x86_64.
- Kiểm tra giao diện ở độ phân giải 720 × 1600, density 320 (rộng 360 dp).

## Kết quả build

`gradlew.bat assembleDebug lintDebug`: **BUILD SUCCESSFUL**.

APK: [app-debug.apk](app/build/outputs/apk/debug/app-debug.apk).

Lint: **0 lỗi, 1 cảnh báo** khuyến nghị dùng AndroidX ExifInterface thay cho `android.media.ExifInterface`. Project dùng API native từ Android 7.0 để giữ cấu trúc đơn giản và không có dependency ngoài. Cảnh báo này được giữ trong báo cáo, không tắt kiểm tra Lint toàn project.

## Kiểm thử tự động trên máy ảo

`gradlew.bat connectedDebugAndroidTest`: **6/6 đạt, 0 thất bại, 0 bỏ qua**.

| Bộ kiểm thử | Trường hợp | Kết quả |
| --- | --- | --- |
| DatabaseHelperTest | Thêm, khóa trùng, đọc đủ thuộc tính, sửa, đóng/mở helper, xóa | Đạt |
| DatabaseHelperTest | Tìm chữ hoa/thường tiếng Việt, tìm mã, chuỗi đặc biệt, lương 9 / 20,5 / 100, lọc kết hợp sắp xếp | Đạt |
| DatabaseHelperTest | Sao chép ảnh, thu nhỏ ảnh, xóa ảnh nguồn, đọc lại ảnh nội bộ, dọn ảnh | Đạt |
| NhanSuFlowTest | Thêm → danh sách → chi tiết → sửa → dữ liệu mới ở chi tiết và danh sách; mã trùng bị chặn | Đạt |
| NhanSuFlowTest | Cả nhấn giữ dòng và nút xóa chi tiết đều xác nhận; Hủy giữ dữ liệu, Đồng ý mới xóa | Đạt |
| NhanSuFlowTest | Mã/tên trống, năm tương lai, lương âm bị chặn; làm mới; đúng bốn phòng ban; quay lại không lưu | Đạt |

Báo cáo đầy đủ sau khi chạy: [androidTests](app/build/reports/androidTests/connected/debug/index.html), [Lint](app/build/reports/lint-results-debug.html).

## Kiểm tra giao diện và thao tác thực tế

- Đã mở và xem cả bốn màn hình, kiểm tra MSSV / họ tên sinh viên và thông tin nhân sự.
- Đã nhập hồ sơ bằng bàn phím máy ảo, chọn phòng ban và thêm bằng nút THÊM.
- Đã cuộn form đến các trường cuối và các nút; không có nội dung bị che cố định.
- Đã force-stop ứng dụng, mở lại và xác nhận hồ sơ vẫn xuất hiện.
- Đã mở sửa và xác nhận mã bị khóa; Hủy trở về chi tiết.
- Đã mở xác nhận xóa, chọn Hủy để giữ hồ sơ, sau đó xác nhận xóa thành công.
- Bản ghi dùng chụp ảnh minh họa đã được xóa; ứng dụng bắt đầu với danh sách trống.

| Ảnh chụp | Nội dung |
| --- | --- |
| [01 — Danh sách trống](docs/screenshots/01-danh-sach-trong.png) | Tiêu đề, sinh viên, tìm kiếm, sắp xếp và trạng thái trống |
| [02 — Thêm nhân sự](docs/screenshots/02-them-nhan-su.png) | Đầu form và thông tin sinh viên |
| [03 — Cuối form thêm](docs/screenshots/03-them-nhan-su-cuoi-form.png) | Các trường nhập cuối, Spinner, thêm / làm mới / quay lại |
| [04 — Có dữ liệu](docs/screenshots/04-danh-sach-co-du-lieu.png) | Dòng ListView đủ ảnh, mã, tên, phòng ban, lương |
| [05 — Chi tiết](docs/screenshots/05-chi-tiet-nhan-su.png) | Đầu màn hình chi tiết |
| [06 — Cuối chi tiết](docs/screenshots/06-chi-tiet-cuoi-form.png) | Đủ thuộc tính và các nút thao tác |
| [07 — Chỉnh sửa](docs/screenshots/07-chinh-sua-nhan-su.png) | Dữ liệu cũ được điền sẵn, mã không thể sửa |
| [08 — Xác nhận xóa](docs/screenshots/08-xac-nhan-xoa.png) | Hủy / Đồng ý trước khi xóa |

## Phạm vi đã xác minh

Kiểm thử thực tế được thực hiện trên máy ảo API 37 nêu trên. Chưa chạy riêng trên thiết bị thật hoặc máy ảo API 24. Bộ kiểm thử ảnh kiểm tra sao chép và lưu ảnh nội bộ; giao diện bộ chọn ảnh của từng hãng điện thoại cần thử thêm theo hướng dẫn trong README. Sau sáu kiểm thử thành công, thay đổi cuối chỉ bổ sung cấu hình backup và căn giữa trạng thái danh sách trống; bản APK cuối đã build và kiểm tra Lint lại thành công.


## Cập nhật hộp chọn trình độ và menu xóa — 20/10/2026

- Trình độ dùng Spinner trong cả màn hình thêm và sửa: THPT, Trung cấp, Cao đẳng, Đại học, Thạc sĩ, Tiến sĩ, Khác. Mục “Chọn trình độ” giữ khả năng để trống; Làm mới trả về mục này. Giá trị tự nhập của hồ sơ cũ vẫn được chọn và lưu nguyên khi chỉnh sửa.
- Nhấn giữ dòng nhân sự hiển thị menu XÓA. Chọn XÓA mới mở hộp thoại xác nhận; HỦY giữ dữ liệu, ĐỒNG Ý mới xóa. Kiểm thử gửi thao tác nhấn giữ bằng UiAutomation và chọn menu trên giao diện thật.
- Sắp xếp Mặc định (theo mã), Lương tăng dần và Lương giảm dần: đạt với lương 9 / 20,5 / 100, lương bằng nhau, đổi lại chế độ mặc định, tìm kiếm có một kết quả và không có kết quả.
- Luồng ảnh: chọn ảnh, lưu hồ sơ, hiển thị ở chi tiết, thay ảnh rồi hủy, thay ảnh rồi lưu, dọn ảnh cũ và đọc lại đường dẫn đã lưu đều đạt. Bộ kiểm thử cung cấp kết quả ảnh mẫu cho yêu cầu ACTION_OPEN_DOCUMENT; chưa thao tác bộ chọn ảnh hệ thống của từng thiết bị.

Môi trường chạy bổ sung: Pixel_10a, Android 17 / API 37, JDK 17, Gradle Wrapper 9.7.1 trong project hiện tại.

Build `assembleDebug assembleDebugAndroidTest lintDebug`: **thành công**. Lint: **0 lỗi, 1 cảnh báo ExifInterface đã có từ trước**.

Đã xác minh **9 trường hợp kiểm thử**: 8 trường hợp đạt ở lượt chạy bộ kiểm thử; trường hợp menu xóa được chạy lại riêng và đạt sau khi cập nhật cách gửi thao tác chạm và chọn phần tử menu. Mã ứng dụng không thay đổi giữa hai lượt này. Bộ kiểm thử chờ màn hình đóng trước khi kiểm tra kết quả lưu/hủy và dọn database kiểm thử riêng giữa các lần chạy.
