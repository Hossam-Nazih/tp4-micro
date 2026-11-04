package dao;

/**
 * Échec d'une opération de persistance, transaction déjà annulée.
 * Non vérifiée (RuntimeException) : l'appelant la traite seulement s'il sait quoi en faire.
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
