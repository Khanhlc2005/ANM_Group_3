# DES Studio

DES Studio là ứng dụng desktop Java Swing dùng cho đồ án môn học về mã hóa và giải mã DES thủ công. Ứng dụng cho phép nhập bản rõ hoặc bản mã, tạo hoặc nhập khóa DES, mã hóa/giải mã dữ liệu, xem thông tin khóa, tải dữ liệu từ file văn bản, lưu kết quả, lưu/tải khóa và sao chép kết quả.

## Yêu cầu

- JDK 21 hoặc mới hơn
- Maven 3.9 hoặc mới hơn

Nếu `mvn` chưa có trong `PATH`, hãy thêm Maven vào `PATH` hoặc dùng Maven đi kèm IntelliJ IDEA tại `plugins/maven/lib/maven3/bin`.

## Build

```powershell
mvn clean package
```

## Kiểm thử

```powershell
mvn test
```

## Chạy ứng dụng

```powershell
mvn exec:java
```

Lệnh chạy bằng Maven sẽ khởi động `app.Main`, cấu hình FlatLaf và mở cửa sổ Swing chính.

## Luồng demo

1. Bấm `Tạo ngẫu nhiên` hoặc nhập thủ công khóa DES Hex gồm 16 ký tự.
2. Nhập bản rõ vào vùng `Dữ liệu vào` và chọn `Văn bản`.
3. Chọn `Base64` hoặc `Hex` làm định dạng kết quả khi mã hóa.
4. Bấm `Mã hóa`.
5. Sao chép kết quả, dán lại vào `Dữ liệu vào`, chọn đúng định dạng dữ liệu vào, chọn định dạng kết quả `Hex` hoặc `Base64`, rồi bấm `Giải mã`.
6. Dùng `Tải file` để đọc các file `.txt`, `.csv`, `.json`, `.xml`, `.docx` hoặc file `.pdf` dạng văn bản vào vùng dữ liệu vào. Dùng `Lưu file` để ghi kết quả.
7. Dùng `Lưu khóa` và `Tải khóa` cho file khóa.
8. Mở `Thông tin khóa` để xem khóa hiện tại, độ dài khóa, trạng thái hợp lệ, kích thước khối, độ dài khóa hiệu dụng và số vòng DES.

## Ghi chú

- DES được cài đặt thủ công trong `src/main/java/des`; mã chính không dùng API mã hóa của Java như `javax.crypto.Cipher`.
- File văn bản được đọc và ghi bằng UTF-8.
- Việc tải DOCX/PDF chỉ trích xuất phần văn bản. PDF scan cần OCR nên chưa được hỗ trợ.
- Ứng dụng phục vụ mục đích minh họa và học DES, không dùng cho mã hóa trong môi trường sản xuất.
