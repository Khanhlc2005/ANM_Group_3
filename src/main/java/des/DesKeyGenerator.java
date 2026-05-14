package des;

import java.security.SecureRandom;

public class DesKeyGenerator {
    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] generateKey() {
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);
        return key;
    }

    public long[] generateRoundKeys(byte[] key) {
        BitUtils.requireLength(key, 8, "DES key");

        long keyBits = BitUtils.bytesToLong(key);
        long permutedKey = BitUtils.permute(keyBits, 64, DesTables.PC1);
        int c = (int) ((permutedKey >>> 28) & 0x0fffffff);
        int d = (int) (permutedKey & 0x0fffffff);

        long[] roundKeys = new long[16];
        for (int round = 0; round < roundKeys.length; round++) {
            c = BitUtils.leftRotate28(c, DesTables.KEY_SHIFTS[round]);
            d = BitUtils.leftRotate28(d, DesTables.KEY_SHIFTS[round]);

            long combined = ((long) c << 28) | d;
            roundKeys[round] = BitUtils.permute(combined, 56, DesTables.PC2);
        }

        return roundKeys;
    }
}
