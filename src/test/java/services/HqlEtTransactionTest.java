package services;

import dao.BilanCoutMachine;
import dao.DaoException;
import entities.Intervention;
import entities.Machine;
import entities.Salle;
import entities.TypeIntervention;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Requêtes HQL (étape 10) et transactions tout-ou-rien (étape 11).
 * Données : S1 (3 postes) avec M-1 Dell et M-2 HP, S2 (2 postes) avec M-3 Dell, S3 vide, M-4 Lenovo en stock.
 */
class HqlEtTransactionTest extends BaseServiceTest {

    private Salle s1;
    private Salle s2;
    private Machine m1;
    private Machine m2;
    private Machine m3;

    @BeforeEach
    void remplir() {
        s1 = salle("S1", 3);
        s2 = salle("S2", 2);
        salle("S3", 5);
        m1 = machine("M-1", "Dell", LocalDate.of(2023, 5, 10), s1);
        m2 = machine("M-2", "HP", LocalDate.of(2024, 2, 1), s1);
        m3 = machine("M-3", "Dell", LocalDate.of(2024, 8, 20), s2);
        machine("M-4", "Lenovo", LocalDate.of(2025, 1, 5), null);

        intervention(m1, TypeIntervention.REPARATION, 900);
        intervention(m1, TypeIntervention.MISE_A_JOUR, 100);
        intervention(m3, TypeIntervention.REPARATION, 400);
    }

    @Test
    void findByFabricant() {
        assertEquals(Arrays.asList("M-1", "M-3"), references(machineService.findByFabricant("Dell")));
        assertTrue(machineService.findByFabricant("Apple").isEmpty());
    }

    @Test
    void findBySalle() {
        assertEquals(Arrays.asList("M-1", "M-2"), references(machineService.findBySalle("S1")));
    }

    @Test
    void findAcheteesEntre() {
        assertEquals(Arrays.asList("M-2", "M-3"),
                references(machineService.findAcheteesEntre(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))));
    }

    @Test
    void rechercheInsensibleALaCasse() {
        assertEquals(Arrays.asList("M-1", "M-3"), references(machineService.rechercher("DELL")));
    }

    @Test
    void machinesSansIntervention() {
        assertEquals(Arrays.asList("M-2", "M-4"), references(machineService.findSansIntervention()));
    }

    @Test
    void compterParSalleInclutLesSallesVides() {
        Map<String, Long> attendu = new LinkedHashMap<>();
        attendu.put("S1", 2L);
        attendu.put("S2", 1L);
        attendu.put("S3", 0L);

        assertEquals(attendu, machineService.compterParSalle());
    }

    @Test
    void bilanCoutDuPlusCherAuMoinsCher() {
        List<BilanCoutMachine> bilan = interventionService.bilanCoutParMachine();

        assertEquals(2, bilan.size());
        assertEquals("M-1", bilan.get(0).getReference());
        assertEquals(1000.0, bilan.get(0).getCoutTotal());
        assertEquals(2, bilan.get(0).getNombreInterventions());
    }

    @Test
    void interventionsParTypeEtParMachine() {
        assertEquals(2, interventionService.findByType(TypeIntervention.REPARATION).size());
        assertEquals(2, interventionService.findByMachine("M-1").size());
    }

    @Test
    void deplacerDesMachinesVersUneSalleAvecDeLaPlace() {
        machineService.deplacerVers(Arrays.asList(m1.getId(), m2.getId()), salleService.findByCode("S3"));

        assertEquals(Arrays.asList("M-1", "M-2"), references(machineService.findBySalle("S3")));
        assertTrue(machineService.findBySalle("S1").isEmpty());
    }

    @Test
    void uneMachineIntrouvableAnnuleToutLeDeplacement() {
        Salle s3 = salleService.findByCode("S3");

        assertThrows(DaoException.class, () ->
                machineService.deplacerVers(Arrays.asList(m1.getId(), m2.getId(), 99999), s3));

        // M-1 et M-2 avaient été modifiées en mémoire avant l'erreur : le rollback les a laissées en S1
        assertEquals(Arrays.asList("M-1", "M-2"), references(machineService.findBySalle("S1")));
        assertTrue(machineService.findBySalle("S3").isEmpty());
    }

    @Test
    void unDeplacementQuiDepasseLaCapaciteEstRefuse() {
        // S2 : 2 postes, déjà 1 occupé, on veut en ajouter 2
        DaoException refus = assertThrows(DaoException.class, () ->
                machineService.deplacerVers(Arrays.asList(m1.getId(), m2.getId()), s2));

        assertTrue(refus.getMessage().contains("1 place(s) libre(s)"));
        assertEquals(Arrays.asList("M-3"), references(machineService.findBySalle("S2")));
    }

    private void intervention(Machine machine, TypeIntervention type, double cout) {
        Intervention intervention = new Intervention(LocalDate.of(2025, 1, 15), type, "Test", cout, "Omar");
        intervention.setMachineConcernee(machine);
        interventionService.create(intervention);
    }

    private static List<String> references(List<Machine> machines) {
        return machines.stream().map(Machine::getReference).collect(Collectors.toList());
    }
}
