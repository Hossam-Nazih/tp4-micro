package dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implémentation commune de IDao. Le bloc session / transaction / rollback de l'énoncé n'est écrit qu'une fois,
 * dans enTransaction ; les DAO concrets n'ont plus qu'à fournir leurs requêtes spécifiques.
 */
public abstract class AbstractDao<T> implements IDao<T> {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final Class<T> typeEntite;

    protected AbstractDao(Class<T> typeEntite) {
        this.typeEntite = typeEntite;
    }

    @Override
    public boolean create(T o) {
        return ecrire("création", session -> session.save(o));
    }

    @Override
    public boolean delete(T o) {
        return ecrire("suppression", session -> session.delete(o));
    }

    // update() rattache à la nouvelle session un objet lu dans une session précédente, désormais fermée
    @Override
    public boolean update(T o) {
        return ecrire("modification", session -> session.update(o));
    }

    @Override
    public T getById(int id) {
        return lire(session -> session.get(typeEntite, id));
    }

    // Le nom simple de la classe est le nom de l'entité en HQL : "from Salle", "from Machine"...
    @Override
    public List<T> getAll() {
        return lire(session -> session.createQuery("from " + typeEntite.getSimpleName(), typeEntite).list());
    }

    /**
     * Étape 11 : toutes les opérations du bloc sont validées ensemble ou annulées ensemble.
     * En cas d'erreur, rollback puis DaoException : la base ne garde jamais une moitié d'opération.
     */
    protected <R> R enTransaction(String operation, Function<Session, R> travail) {
        // Pas de try-with-resources ici, contrairement à l'énoncé : il fermerait la session AVANT le catch,
        // et le rollback échouerait alors sur une connexion fermée (IllegalStateException) en masquant l'erreur.
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            R resultat = travail.apply(session);
            tx.commit();
            return resultat;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new DaoException("Erreur transactionnelle pendant la " + operation + " : " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    // Pour le contrat booléen de IDao : l'échec est journalisé puis signalé par false, sans exception
    private boolean ecrire(String operation, Consumer<Session> ecriture) {
        try {
            enTransaction(operation, session -> {
                ecriture.accept(session);
                return null;
            });
            return true;
        } catch (DaoException e) {
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
    }

    // Lecture seule : pas de transaction, mais la session est toujours fermée (try-with-resources)
    protected <R> R lire(Function<Session, R> lecture) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return lecture.apply(session);
        }
    }
}
