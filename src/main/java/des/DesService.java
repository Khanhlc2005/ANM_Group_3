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

    public String encrypt(String input, InputFormat inputFormat, String hexKey, EncodingFormat outputFormat) {
        byte[] key = EncodingUtils.decodeDesKeyHex(normalizeKey(hexKey));
        byte[] plainBytes = decodeInput(input, inputFormat);
        byte[] cipherBytes = algorithm.encrypt(plainBytes, key);
        return EncodingUtils.encode(cipherBytes, outputFormat);
    }

    public String decrypt(String input, InputFormat inputFormat, String hexKey, EncodingFormat outputFormat) {
        byte[] key = EncodingUtils.decodeDesKeyHex(normalizeKey(hexKey));
        byte[] cipherBytes = decodeInput(input, inputFormat);
        byte[] plainBytes = algorithm.decrypt(cipherBytes, key);
        return EncodingUtils.encode(plainBytes, outputFormat);
    }

    public String describeKey(String hexKey) {
        byte[] key = EncodingUtils.decodeDesKeyHex(normalizeKey(hexKey));
        long[] roundKeys = keyGenerator.generateRoundKeys(key);
        StringBuilder builder = new StringBuilder();
        builder.append("Key Hex: ").append(EncodingUtils.encodeHex(key)).append(System.lineSeparator());
        builder.append("Key Bytes: 8").append(System.lineSeparator());
        builder.append("Round Keys").append(System.lineSeparator());
        for (int index = 0; index < roundKeys.length; index++) {
            builder.append(String.format("K%02d: %012X%n", index + 1, roundKeys[index]));
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
}
