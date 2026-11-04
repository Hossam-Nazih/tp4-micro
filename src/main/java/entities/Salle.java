package entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Salle du parc informatique, qui accueille un nombre limité de postes.
 * Côté "un" de la relation avec Machine : la clé étrangère est dans machines.salle_id.
 */
@Entity
@Table(name = "salles")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Code affiché sur la porte, par exemple "INFO-1" : unique pour retrouver une salle sans connaître son id
    @Column(name = "code_salle", nullable = false, unique = true, length = 20)
    private String codeSalle;

    @Column(nullable = false, length = 100)
    private String designation;

    private int etage;

    // Nombre de postes que la salle peut accueillir : MachineService refuse d'en installer davantage
    @Column(name = "capacite_postes", nullable = false)
    private int capacitePostes;

    // cascade ALL : enregistrer ou supprimer une salle fait de même pour ses machines.
    // LAZY : la liste n'est chargée que si on la parcourt, et seulement tant que la session est ouverte
    // (SalleDao.getAvecMachines la charge exprès pour pouvoir l'utiliser ensuite).
    @OneToMany(mappedBy = "salleAffectee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Machine> machinesInstallees = new ArrayList<>();

    public Salle() {}

    public Salle(String codeSalle, String designation, int etage, int capacitePostes) {
        this.codeSalle = codeSalle;
        this.designation = designation;
        this.etage = etage;
        this.capacitePostes = capacitePostes;
    }

    // Les deux côtés de la relation sont mis à jour ensemble ; seul celui de Machine est enregistré en base
    public void installer(Machine machine) {
        machinesInstallees.add(machine);
        machine.setSalleAffectee(this);
    }

    public int getId() { return id; }
    public String getCodeSalle() { return codeSalle; }
    public void setCodeSalle(String codeSalle) { this.codeSalle = codeSalle; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }
    public int getCapacitePostes() { return capacitePostes; }
    public void setCapacitePostes(int capacitePostes) { this.capacitePostes = capacitePostes; }
    public List<Machine> getMachinesInstallees() { return machinesInstallees; }

    // N'affiche pas machinesInstallees : la lire hors session lèverait une LazyInitializationException
    @Override
    public String toString() {
        return codeSalle + " - " + designation + " (étage " + etage + ", " + capacitePostes + " postes)";
    }
}
