package dao;

import entities.Fournisseur;

public class FournisseurDao extends AbstractDao<Fournisseur> {

    public FournisseurDao() {
        super(Fournisseur.class);
    }

    public long compterMachines(int fournisseurId) {
        return lire(session -> session
                .createQuery("select count(m) from Machine m where m.fournisseur.id = :id", Long.class)
                .setParameter("id", fournisseurId)
                .uniqueResult());
    }
}
