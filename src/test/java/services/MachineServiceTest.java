package services;

import entities.Fournisseur;
import entities.Intervention;
import entities.Machine;
import entities.Salle;
import entities.TypeIntervention;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MachineServiceTest extends BaseServiceTest {

    private static final LocalDate ACHAT = LocalDate.of(2024, 3, 1);

    @Test
    void creerUneMachineDansUneSalle() {
        Salle salle = salle("S1", 2);

        Machine machine = machine("M-1", "Dell", ACHAT, salle);

        Machine relue = machineService.getById(machine.getId());
        assertEquals("Dell", relue.getFabricant());
        assertEquals("S1", relue.getSalleAffectee().getCodeSalle(), "salle chargée avec la machine (EAGER)");
    }

    @Test
    void uneSallePleineRefuseUneMachineDePlus() {
        Salle salle = salle("S1", 2);
        machine("M-1", "Dell", ACHAT, salle);
        machine("M-2", "Dell", ACHAT, salle);

        Machine enTrop = new Machine("M-3", "HP", "ProDesk", ACHAT, 5000);
        enTrop.setSalleAffectee(salle);

        assertFalse(machineService.create(enTrop));
        assertEquals(2, machineService.findBySalle("S1").size());
    }

    @Test
    void uneMachineEnStockNaPasBesoinDeSalle() {
        assertTrue(machineService.create(new Machine("M-STOCK", "Lenovo", "ThinkPad", ACHAT, 7000)));
    }

    @Test
    void unPrixNegatifEstRefuse() {
        assertFalse(machineService.create(new Machine("M-1", "Dell", "OptiPlex", ACHAT, -1)));
    }

    @Test
    void deuxMachinesNePeuventPasAvoirLaMemeReference() {
        machine("M-1", "Dell", ACHAT, null);

        assertFalse(machineService.create(new Machine("M-1", "HP", "Autre", ACHAT, 5000)));
    }

    @Test
    void modifierPuisSupprimerUneMachine() {
        Machine lue = machineService.getById(machine("M-1", "Dell", ACHAT, null).getId());
        lue.setModele("OptiPlex 9020");
        assertTrue(machineService.update(lue));
        assertEquals("OptiPlex 9020", machineService.getById(lue.getId()).getModele());

        assertTrue(machineService.delete(lue));
        assertNull(machineService.getById(lue.getId()));
    }

    @Test
    void supprimerUneMachineSupprimeSesInterventions() {
        Machine machine = machine("M-1", "Dell", ACHAT, null);
        Intervention intervention = new Intervention(ACHAT, TypeIntervention.INSTALLATION, "Mise en service", 200, "Omar");
        intervention.setMachineConcernee(machine);
        interventionService.create(intervention);

        assertTrue(machineService.delete(machineService.getById(machine.getId())));

        assertTrue(interventionService.getAll().isEmpty());
    }

    @Test
    void unFournisseurAvecDesMachinesNePeutPasEtreSupprime() {
        Fournisseur dell = fournisseur("Dell");
        Machine machine = new Machine("M-1", "Dell", "OptiPlex", ACHAT, 5000);
        machine.setFournisseur(dell);
        machineService.create(machine);

        assertFalse(fournisseurService.delete(dell));

        assertTrue(fournisseurService.delete(fournisseur("Sans machine")));
    }

    @Test
    void uneInterventionDansLeFuturEstRefusee() {
        Intervention future = new Intervention(LocalDate.now().plusDays(1), TypeIntervention.MAINTENANCE, "Prévue", 100, "Sara");
        future.setMachineConcernee(machine("M-1", "Dell", ACHAT, null));

        assertFalse(interventionService.create(future));
    }
}
