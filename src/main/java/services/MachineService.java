package services;

import dao.IDao;
import dao.MachineDao;
import entities.Machine;
import entities.Salle;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MachineService implements IDao<Machine> {

    private static final Logger LOGGER = Logger.getLogger(MachineService.class.getName());

    private final MachineDao dao;

    public MachineService() {
        this(new MachineDao());
    }

    public MachineService(MachineDao dao) {
        this.dao = dao;
    }

    /** Refuse une machine sans référence, à prix négatif, ou destinée à une salle déjà pleine. */
    @Override
    public boolean create(Machine o) {
        if (o.getReference() == null || o.getReference().trim().isEmpty()) {
            LOGGER.warning("Machine refusée : la référence est obligatoire");
            return false;
        }
        if (o.getPrixAchat() < 0) {
            LOGGER.warning("Machine " + o.getReference() + " refusée : prix d'achat négatif");
            return false;
        }
        Salle salle = o.getSalleAffectee();
        if (salle != null && dao.compterDansSalle(salle.getId()) >= salle.getCapacitePostes()) {
            LOGGER.warning("Machine " + o.getReference() + " refusée : la salle " + salle.getCodeSalle()
                    + " est pleine (" + salle.getCapacitePostes() + " postes)");
            return false;
        }
        boolean creee = dao.create(o);
        if (creee) {
            LOGGER.info("Machine créée : " + o);
        }
        return creee;
    }

    @Override
    public boolean delete(Machine o) {
        return dao.delete(o);
    }

    @Override
    public boolean update(Machine o) {
        return dao.update(o);
    }

    @Override
    public Machine getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<Machine> getAll() {
        return dao.getAll();
    }

    public List<Machine> findByFabricant(String fabricant) {
        return dao.findByFabricant(fabricant);
    }

    public List<Machine> findBySalle(String codeSalle) {
        return dao.findBySalle(codeSalle);
    }

    public List<Machine> findAcheteesEntre(LocalDate debut, LocalDate fin) {
        return dao.findAcheteesEntre(debut, fin);
    }

    public List<Machine> rechercher(String motCle) {
        return dao.rechercher(motCle);
    }

    public List<Machine> findSansIntervention() {
        return dao.findSansIntervention();
    }

    public Map<String, Long> compterParSalle() {
        return dao.compterParSalle();
    }

    /** Tout ou rien : lève une DaoException, et ne déplace aucune machine, au moindre problème. */
    public void deplacerVers(List<Integer> machineIds, Salle cible) {
        dao.reaffecter(machineIds, cible.getId());
    }
}
