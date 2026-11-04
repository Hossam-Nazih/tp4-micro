package test;

import dao.DaoException;
import entities.*;
import services.FournisseurService;
import services.InterventionService;
import services.MachineService;
import services.SalleService;
import util.HibernateUtil;
import util.JeuDeDonnees;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Remplit le parc dans MySQL puis enchaîne les opérations du TP : CRUD, règles métier, HQL, transactions.
 * Les tables salles, machines, fournisseurs et interventions sont visibles dans Workbench après exécution.
 */
public class MainTest {

    public static void main(String[] args) {
        SalleService salleService = new SalleService();
        MachineService machineService = new MachineService();
        FournisseurService fournisseurService = new FournisseurService();
        InterventionService interventionService = new InterventionService();

        try {
            JeuDeDonnees.reinitialiser();

            titre("1. Insertion des données");
            Fournisseur dell = new Fournisseur("Dell Maroc", "0522001122", "pro@dell.ma");
            Fournisseur hp = new Fournisseur("HP Distribution", "0537334455", "ventes@hp-distribution.ma");
            Fournisseur lenovo = new Fournisseur("Lenovo Partenaire", "0524667788", "contact@lenovo-partenaire.ma");
            fournisseurService.create(dell);
            fournisseurService.create(hp);
            fournisseurService.create(lenovo);

            Salle info = new Salle("INFO-1", "Salle Informatique", 1, 6);
            Salle reseau = new Salle("RES-2", "Salle Réseau", 2, 3);
            Salle labo = new Salle("LAB-3", "Laboratoire IA", 3, 2);
            salleService.create(info);
            salleService.create(reseau);
            salleService.create(labo);

            Machine m101 = machine("M-101", "Dell", "OptiPlex 7090", LocalDate.of(2023, 9, 1), 8900, dell, info);
            Machine m102 = machine("M-102", "Dell", "OptiPlex 7090", LocalDate.of(2023, 9, 1), 8900, dell, info);
            Machine m103 = machine("M-103", "HP", "EliteDesk 800", LocalDate.of(2024, 2, 15), 7600, hp, info);
            Machine m201 = machine("M-201", "Lenovo", "ThinkCentre M70", LocalDate.of(2022, 11, 20), 6800, lenovo, reseau);
            Machine m202 = machine("M-202", "HP", "ProDesk 400", LocalDate.of(2024, 6, 3), 5900, hp, reseau);
            Machine m301 = machine("M-301", "Dell", "Precision 3660", LocalDate.of(2025, 1, 10), 21500, dell, labo);
            for (Machine m : Arrays.asList(m101, m102, m103, m201, m202, m301)) {
                machineService.create(m);
            }
            // En stock : achetée mais pas encore installée dans une salle
            machineService.create(machine("M-900", "Lenovo", "ThinkPad E14", LocalDate.of(2025, 3, 5), 7200, lenovo, null));

            intervention(interventionService, m101, LocalDate.of(2023, 9, 2), TypeIntervention.INSTALLATION, "Installation et image système", 300, "Omar Lahlou");
            intervention(interventionService, m101, LocalDate.of(2024, 5, 14), TypeIntervention.REPARATION, "Remplacement du disque", 950, "Omar Lahlou");
            intervention(interventionService, m201, LocalDate.of(2024, 1, 8), TypeIntervention.REPARATION, "Carte réseau défectueuse", 420, "Sara Kettani");
            intervention(interventionService, m201, LocalDate.of(2025, 2, 20), TypeIntervention.MISE_A_JOUR, "Passage à Windows 11", 150, "Sara Kettani");
            intervention(interventionService, m301, LocalDate.of(2025, 1, 11), TypeIntervention.INSTALLATION, "Pilotes GPU et CUDA", 500, "Omar Lahlou");
            System.out.println("Données insérées avec succès !");

            titre("2. Règles métier");
            // LAB-3 a 2 postes et déjà 1 machine : la 2e passe, la 3e est refusée
            System.out.println("2e machine dans LAB-3 (2 postes) : "
                    + resultat(machineService.create(machine("M-302", "HP", "Z2 G9", LocalDate.of(2025, 1, 10), 18000, hp, labo))));
            System.out.println("3e machine dans LAB-3 (2 postes) : "
                    + resultat(machineService.create(machine("M-303", "HP", "Z2 G9", LocalDate.of(2025, 1, 10), 18000, hp, labo))));
            System.out.println("Salle sans code : " + resultat(salleService.create(new Salle(" ", "Sans nom", 0, 10))));
            System.out.println("Code de salle en double (contrainte unique) : "
                    + resultat(salleService.create(new Salle("INFO-1", "Doublon", 0, 10))));
            System.out.println("Supprimer Dell Maroc, qui a des machines au parc : " + resultat(fournisseurService.delete(dell)));

            titre("3. Lecture et modification");
            for (Salle salle : salleService.getAll()) {
                System.out.println(salle);
            }
            Salle infoComplete = salleService.getAvecMachines(info.getId());
            System.out.println("Machines de " + infoComplete.getCodeSalle() + " : " + infoComplete.getMachinesInstallees());

            Salle aRenommer = salleService.getById(reseau.getId());
            aRenommer.setDesignation("Salle Réseau et Télécoms");
            salleService.update(aRenommer);
            System.out.println("Après modification : " + salleService.getById(reseau.getId()));

            titre("4. Requêtes HQL");
            System.out.println("Fabricant Dell        : " + machineService.findByFabricant("Dell"));
            System.out.println("Salle INFO-1          : " + machineService.findBySalle("INFO-1"));
            System.out.println("Achetées en 2024      : "
                    + machineService.findAcheteesEntre(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));
            System.out.println("Recherche \"desk\"      : " + machineService.rechercher("desk"));
            System.out.println("Sans intervention     : " + machineService.findSansIntervention());
            System.out.println("Machines par salle    : " + machineService.compterParSalle());
            System.out.println("Réparations           : " + interventionService.findByType(TypeIntervention.REPARATION));
            System.out.println("Coût d'entretien :");
            interventionService.bilanCoutParMachine().forEach(bilan -> System.out.println("   " + bilan));

            titre("5. Transactions : tout ou rien");
            try {
                // INFO-1 a la place, M-201 et M-202 existent, mais 99999 non : elles ont déjà changé de salle
                // en mémoire quand l'erreur survient, le rollback annule tout et aucune ne bouge en base
                machineService.deplacerVers(Arrays.asList(m201.getId(), m202.getId(), 99999), info);
            } catch (DaoException e) {
                System.out.println("Annulé : " + e.getMessage());
            }
            System.out.println("Toujours dans RES-2   : " + machineService.findBySalle("RES-2"));
            try {
                machineService.deplacerVers(Arrays.asList(m101.getId(), m102.getId()), labo);
            } catch (DaoException e) {
                System.out.println("Annulé : " + e.getMessage());
            }
            machineService.deplacerVers(Arrays.asList(m103.getId()), reseau);
            System.out.println("M-103 déplacée        : " + machineService.findBySalle("RES-2"));

            titre("6. Suppression en cascade");
            System.out.println("Suppression de LAB-3 et de ses machines : " + resultat(salleService.delete(salleService.getById(labo.getId()))));
            System.out.println("Machines par salle    : " + machineService.compterParSalle());
        } finally {
            HibernateUtil.getSessionFactory().close();
        }
    }

    private static Machine machine(String reference, String fabricant, String modele, LocalDate dateAchat,
                                   double prix, Fournisseur fournisseur, Salle salle) {
        Machine machine = new Machine(reference, fabricant, modele, dateAchat, prix);
        machine.setFournisseur(fournisseur);
        machine.setSalleAffectee(salle);
        return machine;
    }

    private static void intervention(InterventionService service, Machine machine, LocalDate date, TypeIntervention type,
                                     String compteRendu, double cout, String technicien) {
        Intervention intervention = new Intervention(date, type, compteRendu, cout, technicien);
        intervention.setMachineConcernee(machine);
        service.create(intervention);
    }

    private static String resultat(boolean reussi) {
        return reussi ? "accepté" : "refusé";
    }

    private static void titre(String texte) {
        System.out.println();
        System.out.println("===== " + texte + " =====");
    }
}
