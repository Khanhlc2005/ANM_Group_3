# AGENT.md

## Project Overview

Dự án là bài tập lớn môn An toàn và bảo mật thông tin: xây dựng ứng dụng Java mã hóa và giải mã dữ liệu bằng thuật toán DES.

Ứng dụng có giao diện desktop trực quan, cho phép người dùng nhập văn bản, tạo/nhập khóa DES, mã hóa, giải mã, hiển thị kết quả, lưu khóa và lưu kết quả ra file.

Mục tiêu chính là học thuật: tự cài đặt thuật toán DES thủ công để hiểu chuẩn DES, không chỉ gọi thư viện có sẵn.

## Scope

Chỉ triển khai DES chuẩn.

Không triển khai:
- Double DES
- Triple DES
- AES
- Thuật toán mã hóa khác
- Server, database, đăng nhập

## Tech Stack

- Java 21+
- Maven
- Java Swing
- FlatLaf để giao diện đẹp hơn
- JUnit 5
- IntelliJ IDEA

## Critical Rule: Manual DES

Không dùng API mã hóa sẵn trong code chính:

- `javax.crypto.Cipher`
- `SecretKeyFactory`
- `DESKeySpec`
- `Cipher.getInstance("DES")`

DES core phải được tự cài thủ công, gồm:

- xử lý block 64 bit
- padding PKCS#5
- Initial Permutation IP
- Final Permutation IP^-1
- sinh 16 khóa con
- 16 vòng Feistel
- hàm F: Expansion E, XOR, S-Box, P-Box
- mã hóa và giải mã bằng thứ tự khóa con ngược nhau

## User Features

Ứng dụng cần có:

- nhập bản rõ
- nhập bản mã
- tạo khóa DES tự động
- nhập khóa thủ công
- mã hóa
- giải mã
- hiển thị kết quả
- chọn định dạng bản mã Base64 hoặc Hex
- tải dữ liệu từ file text
- lưu kết quả ra file
- lưu khóa ra file
- hiển thị lỗi/thông báo rõ ràng

## UI Rules

Dùng Swing với FlatLaf.

Giao diện nên hiện đại, gọn, dễ demo trước giảng viên.

Ưu tiên một cửa sổ chính gồm:

- vùng nhập dữ liệu
- vùng khóa
- vùng chọn Base64/Hex
- nút tạo khóa, lưu khóa
- nút mã hóa, giải mã
- vùng kết quả
- nút tải file, lưu file
- thanh trạng thái hoặc khu vực thông báo

Không để logic DES nằm trong class UI.

## Suggested Packages

```text
app/
  Main.java
des/
  DesAlgorithm.java
  DesTables.java
  DesKeyGenerator.java
  BitUtils.java
  PaddingUtils.java
  EncodingUtils.java
ui/
  DesFrame.java
file/
  FileService.java