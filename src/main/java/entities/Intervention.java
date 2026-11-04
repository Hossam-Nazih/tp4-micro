package entities;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Passage d'un technicien sur une machine : installation, réparation, mise à jour...
 * Sert à suivre le coût d'entretien de chaque poste (voir les requêtes HQL de InterventionDao).
 */
@Entity
@Table(name = "interventions")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "date_intervention", nullable = false)
    private LocalDate dateIntervention;

    // STRING : la base contient "REPARATION" et non un numéro qui changerait si on réordonnait l'enum
    @Enumerated(EnumType.STRING)
    @Column(name = "type_intervention", nullable = false, length = 20)
    private TypeIntervention typeIntervention;

    @Column(name = "compte_rendu", length = 255)
    private String compteRendu;

    @Column(name = "cout_intervention")
    private double coutIntervention;

    @Column(name = "nom_technicien", length = 80)
    private String nomTechnicien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machineConcernee;

    public Intervention() {}

    public Intervention(LocalDate dateIntervention, TypeIntervention typeIntervention, String compteRendu,
                        double coutIntervention, String nomTechnicien) {
        this.dateIntervention = dateIntervention;
        this.typeIntervention = typeIntervention;
        this.compteRendu = compteRendu;
        this.coutIntervention = coutIntervention;
        this.nomTechnicien = nomTechnicien;
    }

    public int getId() { return id; }
    public LocalDate getDateIntervention() { return dateIntervention; }
    public void setDateIntervention(LocalDate dateIntervention) { this.dateIntervention = dateIntervention; }
    public TypeIntervention getTypeIntervention() { return typeIntervention; }
    public void setTypeIntervention(TypeIntervention typeIntervention) { this.typeIntervention = typeIntervention; }
    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }
    public double getCoutIntervention() { return coutIntervention; }
    public void setCoutIntervention(double coutIntervention) { this.coutIntervention = coutIntervention; }
    public String getNomTechnicien() { return nomTechnicien; }
    public void setNomTechnicien(String nomTechnicien) { this.nomTechnicien = nomTechnicien; }
    public Machine getMachineConcernee() { return machineConcernee; }
    public void setMachineConcernee(Machine machineConcernee) { this.machineConcernee = machineConcernee; }

    @Override
    public String toString() {
        return dateIntervention + " " + typeIntervention + " par " + nomTechnicien + " (" + coutIntervention + " DH)";
    }
}
