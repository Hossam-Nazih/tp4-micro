package services;

import dao.FournisseurDao;
import dao.IDao;
import entities.Fournisseur;

import java.util.List;
import java.util.logging.Logger;

public class FournisseurService implements IDao<Fournisseur> {

    private static final Logger LOGGER = Logger.getLogger(FournisseurService.class.getName());

    private final FournisseurDao dao;

    public FournisseurService() {
        this(new FournisseurDao());
    }

    public FournisseurService(FournisseurDao dao) {
        this.dao = dao;
    }

    @Override
    public boolean create(Fournisseur o) {
        return dao.create(o);
    }

    /**
     * Interdit de supprimer un fournisseur dont des machines sont encore au parc : on perdrait
     * l'information de garantie. Vérifié ici pour donner un message clair plutôt qu'une erreur de clé étrangère.
     */
    @Override
    public boolean delete(Fournisseur o) {
        long machines = dao.compterMachines(o.getId());
        if (machines > 0) {
            LOGGER.warning("Suppression de " + o.getRaisonSociale() + " refusée : " + machines + " machine(s) au parc");
            return false;
        }
        return dao.delete(o);
    }

    @Override
    public boolean update(Fournisseur o) {
        return dao.update(o);
    }

    @Override
    public Fournisseur getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<Fournisseur> getAll() {
        return dao.getAll();
    }
}
