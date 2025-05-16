package adduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param mz mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return the monoisotopic mass of the experimental mass mz with the adduct @param adduct
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        if (mz == null || adduct == null) return null;

        // Regex para extraer multímero y carga
        Pattern pattern = Pattern.compile("\\[([0-9]*)?M.*?([+-])(\\d*)?\\]([+-])");
        Matcher matcher = pattern.matcher(adduct);

        int multimer = 1;
        int charge = 1;

        if (matcher.find()) {
            String multimerStr = matcher.group(1);
            String sign = matcher.group(2); // + o -
            String chargeStr = matcher.group(3);
            // matcher.group(4) es el signo final

            if (multimerStr != null && !multimerStr.isEmpty()) {
                multimer = Integer.parseInt(multimerStr);
            }
            if (chargeStr != null && !chargeStr.isEmpty()) {
                charge = Integer.parseInt(chargeStr);
            }
        }

        // Buscar masa del aducto en listas
        Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        if (adductMass == null) {
            adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        if (adductMass == null) return null;

        // Fórmula general para multímero y carga
        return ((mz * charge) - adductMass) / multimer;
    }

    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param monoisotopicMass
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {//a partir de una masa conocida, quiere calcular qué m/z esperas ver si usas un aducto
        //Tienes una molécula con masa 500, y sabes que el aducto es [M+H]+, entonces esperas:
        //mz = 500 + 1.007276 = 501.007
        Double massToSearch;

        if (monoisotopicMass == null || adduct == null) return null;

        Pattern pattern = Pattern.compile("\\[([0-9]*)?M.*?([+-])(\\d*)?\\]([+-])");
        Matcher matcher = pattern.matcher(adduct);

        int multimer = 1;
        int charge = 1;

        if (matcher.find()) {
            String multimerStr = matcher.group(1);
            String chargeStr = matcher.group(3);

            if (multimerStr != null && !multimerStr.isEmpty()) {
                multimer = Integer.parseInt(multimerStr);
            }
            if (chargeStr != null && !chargeStr.isEmpty()) {
                charge = Integer.parseInt(chargeStr);
            }
        }

        massToSearch = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        if (massToSearch == null) {
            massToSearch = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        if (massToSearch == null) return null;

        return (monoisotopicMass * multimer + massToSearch) / charge;
    }
    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param measuredMass    Mass measured by MS
     * @param ppm ppm of tolerance
     */
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        double deltaPPM;
        deltaPPM =  Math.round(Math.abs((experimentalMass * ppm) / 1000000));
        return deltaPPM;

    }




}
