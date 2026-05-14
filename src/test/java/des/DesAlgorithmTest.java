package des;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DesAlgorithmTest {
    @Test
    void desCoreIsExplicitlyNotImplementedInSkeleton() {
        DesAlgorithm algorithm = new DesAlgorithm();

        assertThrows(UnsupportedOperationException.class, () -> algorithm.encrypt(new byte[8], new byte[8]));
    }
}
