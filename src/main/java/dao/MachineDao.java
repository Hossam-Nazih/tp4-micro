package dao;

import entities.Machine;
import entities.Salle;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Requêtes HQL sur les machines (étape 10). HQL parle d'entités et d'attributs Java
 * (Machine, m.fabricant, m.salleAffectee.codeSalle), jamais de tables ni de colonnes :
 * la même requête fonctionne sur MySQL comme sur H2.
 * Les valeurs passent toujours par des paramètres nommés (:fabricant), ce qui empêche l'injection SQL.
 */
public class MachineDao extends AbstractDao<Machine> {

    public MachineDao() {
        super(Machine.class);
    }

    // Équivalent du findByMarque de l'énoncé, l'attribut s'appelant ici "fabricant"
    public List<Machine> findByFabricant(String fabricant) {
        return lire(session -> session
                .createQuery("from Machine m where m.fabricant = :fabricant order by m.reference", Machine.class)
                .setParameter("fabricant", fabricant)
                .list());
    }

    // Jointure sur l'association : HQL génère seul la condition machines.salle_id = salles.id
    public List<Machine> findBySalle(String codeSalle) {
        return lire(session -> session
                .createQuery("select m from Machine m join m.salleAffectee s where s.codeSalle = :code"
                        + " order by m.reference", Machine.class)
                .setParameter("code", codeSalle)
                .list());
    }

    public List<Machine> findAcheteesEntre(LocalDate debut, LocalDate fin) {
        return lire(session -> session
                .createQuery("from Machine m where m.dateAchat between :debut and :fin order by m.dateAchat",
                        Machine.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .list());
    }

    // Recherche partielle, insensible à la casse, sur le fabricant ou le modèle
    public List<Machine> rechercher(String motCle) {
        return lire(session -> session
                .createQuery("from Machine m where lower(m.fabricant) like :motif or lower(m.modele) like :motif"
                        + " order by m.reference", Machine.class)
                .setParameter("motif", "%" + motCle.toLowerCase() + "%")
                .list());
    }

    // "is empty" sur une collection : Hibernate le traduit en sous-requête NOT EXISTS
    public List<Machine> findSansIntervention() {
        return lire(session -> session
                .createQuery("from Machine m where m.interventions is empty order by m.reference", Machine.class)
                .list());
    }

    /** Nombre de machines par code de salle, salles vides comprises (left join), dans l'ordre des codes. */
    public Map<String, Long> compterParSalle() {
        List<Object[]> lignes = lire(session -> session
                .createQuery("select s.codeSalle, count(m) from Salle s left join s.machinesInstallees m"
                        + " group by s.codeSalle order by s.codeSalle", Object[].class)
                .list());
        Map<String, Long> parSalle = new LinkedHashMap<>();
        for (Object[] ligne : lignes) {
            parSalle.put((String) ligne[0], (Long) ligne[1]);
        }
        return parSalle;
    }

    public long compterDansSalle(int salleId) {
        return lire(session -> session
                .createQuery("select count(m) from Machine m where m.salleAffectee.id = :id", Long.class)
                .setParameter("id", salleId)
                .uniqueResult());
    }

    /**
     * Déplace plusieurs machines vers une salle en une seule transaction (étape 11).
     * Si une seule machine est introuvable ou si la salle n'a pas assez de place, aucune n'est déplacée.
     */
    public void reaffecter(List<Integer> machineIds, int salleCibleId) {
        enTransaction("réaffectation de " + machineIds.size() + " machine(s)", session -> {
            Salle cible = session.get(Salle.class, salleCibleId);
            if (cible == null) {
                throw new DaoException("Salle cible introuvable : " + salleCibleId);
            }
            long placesOccupees = session
                    .createQuery("select count(m) from Machine m where m.salleAffectee.id = :id", Long.class)
                    .setParameter("id", salleCibleId)
                    .uniqueResult();
            if (placesOccupees + machineIds.size() > cible.getCapacitePostes()) {
                throw new DaoException("La salle " + cible.getCodeSalle() + " n'a que "
                        + (cible.getCapacitePostes() - placesOccupees) + " place(s) libre(s)");
            }
            for (Integer id : machineIds) {
                Machine machine = session.get(Machine.class, id);
                if (machine == null) {
                    // Les machines précédentes ont déjà changé de salle en mémoire : le rollback les remet en place
                    throw new DaoException("Machine introuvable : " + id);
                }
                machine.setSalleAffectee(cible);
            }
            return null;
        });
        logger.info(machineIds.size() + " machine(s) déplacée(s) vers la salle #" + salleCibleId);
    }
}
