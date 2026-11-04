package services;

import dao.IDao;
import dao.SalleDao;
import entities.Salle;

import java.util.List;
import java.util.logging.Logger;

/**
 * Règles métier sur les salles, puis délégation au DAO.
 * Une règle non respectée renvoie false et l'explique dans les logs, comme un échec en base.
 */
public class SalleService implements IDao<Salle> {

    private static final Logger LOGGER = Logger.getLogger(SalleService.class.getName());

    private final SalleDao dao;

    public SalleService() {
        this(new SalleDao());
    }

    public SalleService(SalleDao dao) {
        this.dao = dao;
    }

    @Override
    public boolean create(Salle o) {
        if (!estValide(o)) {
            return false;
        }
        boolean cree = dao.create(o);
        if (cree) {
            LOGGER.info("Salle créée : " + o);
        }
        return cree;
    }

    // cascade ALL sur machinesInstallees : les machines de la salle, et leurs interventions, sont supprimées aussi
    @Override
    public boolean delete(Salle o) {
        return dao.delete(o);
    }

    @Override
    public boolean update(Salle o) {
        return estValide(o) && dao.update(o);
    }

    @Override
    public Salle getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<Salle> getAll() {
        return dao.getAll();
    }

    public Salle findByCode(String codeSalle) {
        return dao.findByCode(codeSalle);
    }

    public Salle getAvecMachines(int id) {
        return dao.getAvecMachines(id);
    }

    private static boolean estValide(Salle salle) {
        if (salle.getCodeSalle() == null || salle.getCodeSalle().trim().isEmpty()) {
            LOGGER.warning("Salle refusée : le code est obligatoire");
            return false;
        }
        if (salle.getCapacitePostes() <= 0) {
            LOGGER.warning("Salle " + salle.getCodeSalle() + " refusée : la capacité doit être positive");
            return false;
        }
        return true;
    }
}
