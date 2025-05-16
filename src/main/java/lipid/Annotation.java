package lipid;

import adduct.AdductList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private static final Logger log = LoggerFactory.getLogger(Annotation.class);
    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private final IoniationMode ionizationMode;
    private String adduct; // !!TODO The adduct will be detected based on the groupedSignals
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;


    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode) {
        this(lipid, mz, intensity, retentionTime, ionizationMode, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionizationMode = ionizationMode;
        // !!TODO This set should be sorted according to help the program to deisotope the signals plus detect the adduct
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public IoniationMode getIonizationMode() {
        return ionizationMode;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }

    public int getTotalScoresApplied() {
        return totalScoresApplied;
    }

    public void setTotalScoresApplied(int totalScoresApplied) {
        this.totalScoresApplied = totalScoresApplied;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // !CHECK Take into account that the score should be normalized between -1 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    /**
     * @return The normalized score between 0 and 1 that consists on the final number divided into the times that the rule
     * has been applied.
     */
    public double getNormalizedScore() {
        if (totalScoresApplied == 0) return 0.0;
        return (double) score / totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    // !!TODO Detect the adduct with an algorithm or with drools, up to the user.
    public void detectAdductFromPeaks() {
        if (groupedSignals == null || groupedSignals.isEmpty()) return;

        Map<String, Double> knownAdductsPositive =AdductList.MAPMZPOSITIVEADDUCTS;
        Map<String, Double> knownAdductsNegative =AdductList.MAPMZNEGATIVEADDUCTS;

        // 🔹 Paso 2: Probar todas las combinaciones de pico y aducto
        for (Peak peak : groupedSignals) {
            double observedMz = peak.getMz();
            boolean matched=false;
            for (Map.Entry<String, Double> entry : knownAdductsPositive.entrySet()) {
                // 🔸 Estimar la masa neutra suponiendo que este pico corresponde a ese aducto
                double neutralMass = observedMz - entry.getValue();
                // 🔸 Con esa masa neutra estimada, genera m/z esperados para todos los aductos conocidos
                for (double otherAdductMass : knownAdductsPositive.values()) {
                    double expectedMz = neutralMass + otherAdductMass;
                    System.out.printf(expectedMz + entry.getKey()+"\t");
                    // 🔸 Verificar si ese m/z esperado está presente en los picos observados
                    matched = groupedSignals.stream().anyMatch(p -> Math.abs(p.getMz() - expectedMz) < 0.01);
                    if (matched) {
                        this.adduct = entry.getKey();
                        break;
                    }
                }
                if (matched) {break;}
            }
            if (!matched) {
                for (Map.Entry<String, Double> entry : knownAdductsNegative.entrySet()) {
                    // 🔸 Estimar la masa neutra suponiendo que este pico corresponde a ese aducto
                    double neutralMass = observedMz - entry.getValue();
                    for (double otherAdductMass : knownAdductsNegative.values()) {
                        double expectedMz = neutralMass + otherAdductMass;

                        matched = groupedSignals.stream().anyMatch(p ->
                                Math.abs(p.getMz() - expectedMz) < 0.01
                        );
                        if (matched) {this.adduct=entry.getKey();
                            System.out.printf( matched+this.adduct+"\t");
                            break;}
                    }
                }
            }
            if (matched) {break;}
        }
    }
}
