# DES Studio

DES Studio is a Java Swing desktop app for a course project about manual DES encryption and decryption. The app lets you enter plaintext or ciphertext, generate or enter a DES key, encrypt/decrypt data, inspect the DES key schedule, load input from text files, save output, save/load keys, and copy results.

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer

If `mvn` is not on `PATH`, either add Maven to `PATH` or use the Maven bundled with IntelliJ IDEA from `plugins/maven/lib/maven3/bin`.

## Build

```powershell
mvn clean package
```

## Test

```powershell
mvn test
```

## Run

```powershell
mvn exec:java
```

The Maven run command starts `app.Main`, configures FlatLaf, and opens the main Swing window.

## Demo Flow

1. Click `Generate Random` or enter a 16-character hex DES key manually.
2. Enter plaintext in the `Input` area and select `TEXT`.
3. Select `BASE64` or `HEX` as the output format for encryption.
4. Click `Encrypt`.
5. Copy the output, paste it back into `Input`, choose the matching input format, choose `HEX` or `BASE64` output, and click `Decrypt`.
6. Use `Load File` to read `.txt`, `.csv`, `.json`, `.xml`, `.docx`, or text-based `.pdf` files into the input area. Use `Save File` to write output.
7. Use `Save Key` and `Load Key` for key files.
8. Open `Key Info` to inspect PC-1, C0/D0, shifts, and all 16 DES round keys.

## Notes

- DES is implemented manually in `src/main/java/des`; the main code does not use Java crypto APIs such as `javax.crypto.Cipher`.
- Text files are read and written as UTF-8.
- DOCX/PDF loading extracts text only. Scanned PDFs need OCR and are not supported.
- The app is intended for DES demonstration and learning, not production cryptography.
