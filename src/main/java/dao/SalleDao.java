package dao;

import entities.Salle;

public class SalleDao extends AbstractDao<Salle> {

    public SalleDao() {
        super(Salle.class);
    }

    public Salle findByCode(String codeSalle) {
        return lire(session -> session
                .createQuery("from Salle s where s.codeSalle = :code", Salle.class)
                .setParameter("code", codeSalle)
                .uniqueResult());
    }

    /**
     * La salle avec ses machines déjà chargées. getById() laisse la liste LAZY non chargée :
     * la parcourir après la fermeture de la session lèverait une LazyInitializationException.
     */
    public Salle getAvecMachines(int id) {
        return lire(session -> session
                .createQuery("select distinct s from Salle s left join fetch s.machinesInstallees where s.id = :id",
                        Salle.class)
                .setParameter("id", id)
                .uniqueResult());
    }
}
