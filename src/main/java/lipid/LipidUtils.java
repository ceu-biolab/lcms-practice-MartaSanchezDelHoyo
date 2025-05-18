// En lipid/LipidUtils.java
package lipid;

import java.util.Map;

public class LipidUtils {
    private static final Map<String, Integer> ORDER = Map.of(
            "PG", 1,
            "PE", 2,
            "PI", 3,
            "PA", 4,
            "PS", 5,
            "PC", 6
    );

    public static int getLipidTypeRank(String lipidType) {
        return ORDER.getOrDefault(lipidType, Integer.MAX_VALUE);
    }
}


