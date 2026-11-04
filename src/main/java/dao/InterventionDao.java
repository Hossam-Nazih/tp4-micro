package dao;

import entities.Intervention;
import entities.TypeIntervention;

import java.util.List;

public class InterventionDao extends AbstractDao<Intervention> {

    public InterventionDao() {
        super(Intervention.class);
    }

    public List<Intervention> findByMachine(String referenceMachine) {
        return lire(session -> session
                .createQuery("from Intervention i where i.machineConcernee.reference = :reference"
                        + " order by i.dateIntervention", Intervention.class)
                .setParameter("reference", referenceMachine)
                .list());
    }

    public List<Intervention> findByType(TypeIntervention type) {
        return lire(session -> session
                .createQuery("from Intervention i where i.typeIntervention = :type order by i.dateIntervention",
                        Intervention.class)
                .setParameter("type", type)
                .list());
    }

    /** Coût d'entretien cumulé par machine, de la plus coûteuse à la moins coûteuse. */
    public List<BilanCoutMachine> bilanCoutParMachine() {
        return lire(session -> session
                .createQuery("select new dao.BilanCoutMachine(m.reference, sum(i.coutIntervention), count(i))"
                        + " from Intervention i join i.machineConcernee m"
                        + " group by m.reference order by sum(i.coutIntervention) desc", BilanCoutMachine.class)
                .list());
    }
}
