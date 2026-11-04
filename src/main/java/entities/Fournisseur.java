package entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Société qui a vendu les machines. Pas de cascade vers les machines :
 * supprimer un fournisseur ne doit jamais supprimer le matériel qu'il a livré.
 */
@Entity
@Table(name = "fournisseurs")
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "raison_sociale", nullable = false, unique = true, length = 100)
    private String raisonSociale;

    @Column(length = 20)
    private String telephone;

    @Column(name = "email_contact", length = 120)
    private String emailContact;

    @OneToMany(mappedBy = "fournisseur")
    private List<Machine> machinesFournies = new ArrayList<>();

    public Fournisseur() {}

    public Fournisseur(String raisonSociale, String telephone, String emailContact) {
        this.raisonSociale = raisonSociale;
        this.telephone = telephone;
        this.emailContact = emailContact;
    }

    public void fournir(Machine machine) {
        machinesFournies.add(machine);
        machine.setFournisseur(this);
    }

    public int getId() { return id; }
    public String getRaisonSociale() { return raisonSociale; }
    public void setRaisonSociale(String raisonSociale) { this.raisonSociale = raisonSociale; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmailContact() { return emailContact; }
    public void setEmailContact(String emailContact) { this.emailContact = emailContact; }
    public List<Machine> getMachinesFournies() { return machinesFournies; }

    @Override
    public String toString() {
        return raisonSociale + " (" + emailContact + ")";
    }
}
