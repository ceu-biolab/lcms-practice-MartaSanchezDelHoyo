package lipid;

import adduct.AdductList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import adduct.Adduct ;



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

    /**
     * The method compares the monoisotopic masses of the posibles adducts, and then selects
     * between the possibles ones which correspond to the peak indicated in the annotation
     *
     * @param tolerance tolerance for the difference between monoisotopic masses
     */

    public void detectAdductFromPeaks(double tolerance) {

        Map<String, Double> adductMap;
        if (ionizationMode == IoniationMode.POSITIVE) {
            adductMap = AdductList.MAPMZPOSITIVEADDUCTS;
        } else {
            adductMap = AdductList.MAPMZNEGATIVEADDUCTS;
        }

        List<Peak> peaks = new ArrayList<>(groupedSignals);
        double bestDiff = Double.MAX_VALUE;

        for (int i = 0; i < peaks.size(); i++) {

            for (int j = i + 1; j < peaks.size(); j++) {
                Peak p1 = peaks.get(i);
                Peak p2 = peaks.get(j);

                // We try all possible combinations of adducts in a pair of peaks
                for (String ad1 : adductMap.keySet()) {
                    System.out.println(ad1);
                    double m1 = Adduct.getMonoisotopicMassFromMZ(p1.getMz(), ad1, this.ionizationMode);
                    for (String ad2 : adductMap.keySet()) {
                        if (ad1.equals(ad2)) continue;
                        double m2 = Adduct.getMonoisotopicMassFromMZ(p2.getMz(), ad2, this.ionizationMode);
                        double diff = Math.abs(m1 - m2);
                        System.out.println(ad1+m1 + " - " + ad2 +m2+ " = Difference is " + diff);
                        if (diff < bestDiff && diff <= tolerance) {

                            if (Math.abs(p1.getMz() - this.mz) < 1e-6) {
                                setAdduct(ad1);
                             return;
                            } else if (Math.abs(p2.getMz() - this.mz) < 1e-6) {
                                System.out.println(ad2.toString());
                                setAdduct(ad2);
                                return;
                            } else {
                                //neigther of both is exactly this.mz;
                                this.adduct = "Unknown";
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    public void detectAdductFromPeaksWithMZ () {
            double tolerance = 0.01d;
            Map<String, Double> adductMap;
            if (ionizationMode == IoniationMode.POSITIVE) {
                adductMap = AdductList.MAPMZPOSITIVEADDUCTS;
            } else {
                adductMap = AdductList.MAPMZNEGATIVEADDUCTS;
            }

            List<Peak> peaks = new ArrayList<>(groupedSignals);
            double bestDiff = Double.MAX_VALUE;
            for (int i = 0; i < peaks.size(); i++) {

                for (int j = i + 1; j < peaks.size(); j++) {
                    Peak p1 = peaks.get(i);
                    Peak p2 = peaks.get(j);

                    for (String ad1 : adductMap.keySet()) {
                        double m1 = Adduct.getMZFromMonoisotopicMass(p1.getMz(), ad1, this.ionizationMode);
                        for (String ad2 : adductMap.keySet()) {
                            if (ad1.equals(ad2)) continue;
                            double m2 = Adduct.getMZFromMonoisotopicMass(p2.getMz(), ad2, this.ionizationMode);
                            double diff = Math.abs(m1 - m2);
                            System.out.println(ad1 + " - " + ad2 + " = Difference is " + diff);
                            if (diff < bestDiff && diff <= tolerance) {

                                if (Math.abs(p1.getMz() - this.mz) < 1e-6) {
                                    setAdduct(ad1);
                                    return;
                                } else if (Math.abs(p2.getMz() - this.mz) < 1e-6) {
                                    setAdduct(ad2);
                                    return;
                                } else {
                                    this.adduct = "Unknown";
                                    return;
                                }
                            }
                        }
                    }
                }
            }
    }

}
