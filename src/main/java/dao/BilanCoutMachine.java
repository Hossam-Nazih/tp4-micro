package dao;

/**
 * Ligne de résultat d'une requête HQL d'agrégat, remplie par "select new dao.BilanCoutMachine(...)".
 * Plus lisible qu'un Object[] dont il faudrait connaître l'ordre des colonnes.
 */
public class BilanCoutMachine {

    private final String reference;
    private final double coutTotal;
    private final long nombreInterventions;

    public BilanCoutMachine(String reference, Double coutTotal, Long nombreInterventions) {
        this.reference = reference;
        this.coutTotal = coutTotal;
        this.nombreInterventions = nombreInterventions;
    }

    public String getReference() { return reference; }
    public double getCoutTotal() { return coutTotal; }
    public long getNombreInterventions() { return nombreInterventions; }

    @Override
    public String toString() {
        return reference + " : " + nombreInterventions + " intervention(s), " + coutTotal + " DH";
    }
}
