package services;

import entities.Fournisseur;
import entities.Machine;
import entities.Salle;
import org.junit.jupiter.api.BeforeEach;
import util.JeuDeDonnees;

import java.time.LocalDate;

/**
 * Chaque test part de tables vides, sur la base H2 en mémoire configurée dans le pom.xml :
 * l'ordre d'exécution des tests n'a aucune influence et MySQL n'est jamais touché.
 */
abstract class BaseServiceTest {

    protected final SalleService salleService = new SalleService();
    protected final MachineService machineService = new MachineService();
    protected final FournisseurService fournisseurService = new FournisseurService();
    protected final InterventionService interventionService = new InterventionService();

    @BeforeEach
    void viderLaBase() {
        JeuDeDonnees.reinitialiser();
    }

    protected Salle salle(String code, int capacite) {
        Salle salle = new Salle(code, "Salle " + code, 1, capacite);
        salleService.create(salle);
        return salle;
    }

    protected Machine machine(String reference, String fabricant, LocalDate dateAchat, Salle salle) {
        Machine machine = new Machine(reference, fabricant, "Modèle " + reference, dateAchat, 5000);
        machine.setSalleAffectee(salle);
        machineService.create(machine);
        return machine;
    }

    protected Fournisseur fournisseur(String raisonSociale) {
        Fournisseur fournisseur = new Fournisseur(raisonSociale, "0500000000", "contact@" + raisonSociale.toLowerCase() + ".ma");
        fournisseurService.create(fournisseur);
        return fournisseur;
    }
}
