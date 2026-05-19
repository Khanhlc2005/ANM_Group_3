package des;

public class DesService {
    private final DesAlgorithm algorithm;
    private final DesKeyGenerator keyGenerator;

    public DesService() {
        this(new DesAlgorithm(), new DesKeyGenerator());
    }

    DesService(DesAlgorithm algorithm, DesKeyGenerator keyGenerator) {
        this.algorithm = algorithm;
        this.keyGenerator = keyGenerator;
    }

    public String generateRandomKeyHex() {
        return EncodingUtils.encodeHex(keyGenerator.generateKey());
    }

    // Chuẩn hóa dữ liệu từ giao diện, mã hóa DES và xuất ra Hex hoặc Base64.
    public String encrypt(String input, InputFormat inputFormat, String hexKey, EncodingFormat outputFormat) {
        byte[] key = EncodingUtils.decodeDesKeyHex(normalizeKey(hexKey));
        byte[] plainBytes = decodeInput(input, inputFormat);
        byte[] cipherBytes = algorithm.encrypt(plainBytes, key);
        return EncodingUtils.encode(cipherBytes, outputFormat);
    }

    // Chuẩn hóa bản mã từ giao diện, giải mã DES và xuất byte rõ theo định dạng đã chọn.
    public String decrypt(String input, InputFormat inputFormat, String hexKey, EncodingFormat outputFormat) {
        byte[] plainBytes = decryptToPlainBytes(input, inputFormat, hexKey);
        return EncodingUtils.encode(plainBytes, outputFormat);
    }

    public byte[] decryptToPlainBytes(String input, InputFormat inputFormat, String hexKey) {
        byte[] key = EncodingUtils.decodeDesKeyHex(normalizeKey(hexKey));
        byte[] cipherBytes = decodeInput(input, inputFormat);
        return algorithm.decrypt(cipherBytes, key);
    }

    // Tạo mô tả lịch sinh khóa để kiểm tra PC-1, C/D và 16 khóa vòng DES.
    public String describeKey(String hexKey) {
        byte[] key = EncodingUtils.decodeDesKeyHex(normalizeKey(hexKey));
        KeyScheduleInfo schedule = keyGenerator.generateKeySchedule(key);
        StringBuilder builder = new StringBuilder();
        builder.append("Key Hex: ").append(schedule.keyHex()).append(System.lineSeparator());
        builder.append("Key Bytes: 8").append(System.lineSeparator());
        builder.append("Key Bits: ").append(formatBinary(schedule.keyBits(), 64)).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("PC-1 (64 bits -> 56 bits, parity removed)").append(System.lineSeparator());
        builder.append("PC-1: ").append(formatBinary(schedule.pc1Key(), 56)).append(System.lineSeparator());
        builder.append("C0:   ").append(formatBinary(schedule.c0(), 28)).append(System.lineSeparator());
        builder.append("D0:   ").append(formatBinary(schedule.d0(), 28)).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("Round key schedule").append(System.lineSeparator());
        builder.append("Rnd Shift Cn                           Dn                           Kn (48-bit Hex)")
                .append(System.lineSeparator());
        for (KeyScheduleRound round : schedule.rounds()) {
            builder.append(String.format("%02d  %-5d %-28s %-28s %012X%n",
                    round.round(),
                    round.shifts(),
                    formatBinary(round.c(), 28),
                    formatBinary(round.d(), 28),
                    round.roundKey()));
        }
        return builder.toString();
    }

    private byte[] decodeInput(String input, InputFormat inputFormat) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input data must not be blank.");
        }
        if (inputFormat == null) {
            throw new IllegalArgumentException("Input format must be selected.");
        }

        return switch (inputFormat) {
            case TEXT -> EncodingUtils.utf8Bytes(input);
            case HEX -> EncodingUtils.decodeHex(removeWhitespace(input));
            case BASE64 -> EncodingUtils.decodeBase64(removeWhitespace(input));
        };
    }

    private String normalizeKey(String hexKey) {
        if (hexKey == null) {
            throw new IllegalArgumentException("Secret key must not be blank.");
        }
        return removeWhitespace(hexKey).toUpperCase();
    }

    private String removeWhitespace(String value) {
        return value.replaceAll("\\s+", "");
    }

    private String formatBinary(long value, int bitCount) {
        String binary = Long.toBinaryString(value);
        if (binary.length() > bitCount) {
            binary = binary.substring(binary.length() - bitCount);
        }
        return "0".repeat(bitCount - binary.length()) + binary;
    }
}
