package services;

import dao.BilanCoutMachine;
import dao.IDao;
import dao.InterventionDao;
import entities.Intervention;
import entities.TypeIntervention;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

public class InterventionService implements IDao<Intervention> {

    private static final Logger LOGGER = Logger.getLogger(InterventionService.class.getName());

    private final InterventionDao dao;

    public InterventionService() {
        this(new InterventionDao());
    }

    public InterventionService(InterventionDao dao) {
        this.dao = dao;
    }

    /** Une intervention se rapporte à une machine, a déjà eu lieu, et ne coûte pas un montant négatif. */
    @Override
    public boolean create(Intervention o) {
        if (o.getMachineConcernee() == null) {
            LOGGER.warning("Intervention refusée : aucune machine concernée");
            return false;
        }
        if (o.getDateIntervention() == null || o.getDateIntervention().isAfter(LocalDate.now())) {
            LOGGER.warning("Intervention refusée : la date doit être aujourd'hui ou dans le passé");
            return false;
        }
        if (o.getCoutIntervention() < 0) {
            LOGGER.warning("Intervention refusée : coût négatif");
            return false;
        }
        return dao.create(o);
    }

    @Override
    public boolean delete(Intervention o) {
        return dao.delete(o);
    }

    @Override
    public boolean update(Intervention o) {
        return dao.update(o);
    }

    @Override
    public Intervention getById(int id) {
        return dao.getById(id);
    }

    @Override
    public List<Intervention> getAll() {
        return dao.getAll();
    }

    public List<Intervention> findByMachine(String referenceMachine) {
        return dao.findByMachine(referenceMachine);
    }

    public List<Intervention> findByType(TypeIntervention type) {
        return dao.findByType(type);
    }

    public List<BilanCoutMachine> bilanCoutParMachine() {
        return dao.bilanCoutParMachine();
    }
}
