package services;

import entities.Salle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalleServiceTest extends BaseServiceTest {

    // Test de l'énoncé (étape 9)
    @Test
    void testCreateSalle() {
        Salle salle = new Salle();
        salle.setCodeSalle("S2");
        salle.setDesignation("Salle Réseau");
        salle.setCapacitePostes(10);

        boolean result = salleService.create(salle);
        Assertions.assertTrue(result, "La création de la salle doit réussir");
        assertTrue(salle.getId() > 0, "save() renseigne l'identifiant généré par MySQL");
    }

    @Test
    void uneSalleSansCodeEstRefusee() {
        assertFalse(salleService.create(new Salle("  ", "Sans code", 0, 10)));
        assertTrue(salleService.getAll().isEmpty());
    }

    @Test
    void uneSalleSansCapaciteEstRefusee() {
        assertFalse(salleService.create(new Salle("S0", "Vide", 0, 0)));
    }

    @Test
    void deuxSallesNePeuventPasAvoirLeMemeCode() {
        salle("S1", 10);

        assertFalse(salleService.create(new Salle("S1", "Doublon", 2, 5)), "contrainte unique sur code_salle");
        assertEquals(1, salleService.getAll().size());
    }

    @Test
    void lireParIdEtParCode() {
        Salle creee = salle("S1", 10);

        assertEquals("S1", salleService.getById(creee.getId()).getCodeSalle());
        assertEquals(creee.getId(), salleService.findByCode("S1").getId());
        assertNull(salleService.getById(99999));
    }

    @Test
    void modifierUneSalle() {
        Salle lue = salleService.getById(salle("S1", 10).getId());
        lue.setDesignation("Salle rénovée");
        lue.setCapacitePostes(25);

        assertTrue(salleService.update(lue));

        Salle relue = salleService.getById(lue.getId());
        assertEquals("Salle rénovée", relue.getDesignation());
        assertEquals(25, relue.getCapacitePostes());
    }

    @Test
    void getAvecMachinesPermetDeParcourirLesMachinesApresLaSession() {
        Salle salle = salle("S1", 10);
        machine("M-1", "Dell", LocalDate.of(2024, 1, 1), salle);
        machine("M-2", "HP", LocalDate.of(2024, 1, 1), salle);

        assertEquals(2, salleService.getAvecMachines(salle.getId()).getMachinesInstallees().size());
    }

    @Test
    void supprimerUneSalleSupprimeSesMachines() {
        Salle salle = salle("S1", 10);
        machine("M-1", "Dell", LocalDate.of(2024, 1, 1), salle);

        assertTrue(salleService.delete(salleService.getById(salle.getId())));

        assertTrue(salleService.getAll().isEmpty());
        assertTrue(machineService.getAll().isEmpty(), "cascade ALL sur machinesInstallees");
    }
}
