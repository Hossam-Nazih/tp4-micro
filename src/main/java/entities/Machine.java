package entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Poste informatique, installé dans une salle et acheté chez un fournisseur.
 */
@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Numéro d'inventaire collé sur la machine : deux machines ne peuvent pas avoir le même
    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false, length = 50)
    private String fabricant;

    @Column(length = 80)
    private String modele;

    @Column(name = "date_achat")
    private LocalDate dateAchat;

    @Column(name = "prix_achat")
    private double prixAchat;

    // EAGER (valeur par défaut d'un @ManyToOne) : la salle arrive avec la machine. Chaque DAO fermant
    // sa session tout de suite, c'est ce qui permet d'afficher la salle d'une machine renvoyée par un DAO.
    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salleAffectee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    // orphanRemoval : une intervention retirée de la liste est supprimée de la base,
    // elle n'a pas de sens sans la machine sur laquelle elle a eu lieu
    @OneToMany(mappedBy = "machineConcernee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Intervention> interventions = new ArrayList<>();

    public Machine() {}

    public Machine(String reference, String fabricant, String modele, LocalDate dateAchat, double prixAchat) {
        this.reference = reference;
        this.fabricant = fabricant;
        this.modele = modele;
        this.dateAchat = dateAchat;
        this.prixAchat = prixAchat;
    }

    public void ajouterIntervention(Intervention intervention) {
        interventions.add(intervention);
        intervention.setMachineConcernee(this);
    }

    public int getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getFabricant() { return fabricant; }
    public void setFabricant(String fabricant) { this.fabricant = fabricant; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public LocalDate getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDate dateAchat) { this.dateAchat = dateAchat; }
    public double getPrixAchat() { return prixAchat; }
    public void setPrixAchat(double prixAchat) { this.prixAchat = prixAchat; }
    public Salle getSalleAffectee() { return salleAffectee; }
    public void setSalleAffectee(Salle salleAffectee) { this.salleAffectee = salleAffectee; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
    public List<Intervention> getInterventions() { return interventions; }

    @Override
    public String toString() {
        return reference + " " + fabricant + " " + modele
                + (salleAffectee == null ? " (en stock)" : " (salle " + salleAffectee.getCodeSalle() + ")");
    }
}
