package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crée une seule SessionFactory pour toute l'application, au premier accès à la classe.
 * Elle est coûteuse à construire (lecture du mapping, pool de connexions) mais sûre à partager entre threads ;
 * les Session, elles, sont légères et ouvertes puis fermées à chaque opération par les DAO.
 */
public class HibernateUtil {

    // Doit rester une référence forte : java.util.logging oublie le réglage d'un logger que plus personne ne référence.
    // Déclaré avant sessionFactory pour être appliqué avant que Hibernate n'écrive ses messages de démarrage.
    private static final Logger LOGS_HIBERNATE = Logger.getLogger("org.hibernate");
    static {
        LOGS_HIBERNATE.setLevel(Level.WARNING);
    }

    // Propriétés de hibernate.cfg.xml remplaçables au lancement par -Dnom=valeur (les tests passent ainsi sur H2)
    private static final String[] PROPRIETES_SURCHARGEABLES = {
            "hibernate.connection.driver_class",
            "hibernate.connection.url",
            "hibernate.connection.username",
            "hibernate.connection.password",
            "hibernate.dialect",
            "hibernate.hbm2ddl.auto",
            "hibernate.show_sql"
    };

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration().configure();
            for (String nom : PROPRIETES_SURCHARGEABLES) {
                String valeur = System.getProperty(nom);
                if (valeur != null) {
                    configuration.setProperty(nom, valeur);
                }
            }
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Sans SessionFactory aucune opération n'est possible : on arrête net au lieu de continuer à moitié
            System.err.println("Échec de la création de la SessionFactory (MySQL est-il démarré ?) : " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
