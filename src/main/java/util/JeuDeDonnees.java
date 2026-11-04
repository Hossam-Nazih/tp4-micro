package util;

import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Vide les quatre tables. Avec hbm2ddl.auto=update les données restent d'un lancement à l'autre :
 * sans ce nettoyage, relancer MainTest échouerait sur les codes de salle et références déjà présents.
 */
public final class JeuDeDonnees {

    private JeuDeDonnees() {}

    public static void reinitialiser() {
        // Session fermée dans finally et non par try-with-resources : le rollback doit se faire avant la fermeture
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Suppressions HQL en masse : elles ne déclenchent pas les cascades,
            // d'où l'ordre imposé par les clés étrangères (tables qui référencent d'abord)
            session.createQuery("delete from Intervention").executeUpdate();
            session.createQuery("delete from Machine").executeUpdate();
            session.createQuery("delete from Salle").executeUpdate();
            session.createQuery("delete from Fournisseur").executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }
}
