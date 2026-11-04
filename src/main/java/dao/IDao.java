package dao;

import java.util.List;

/**
 * Contrat CRUD commun à toutes les entités. Le code métier ne dépend que de cette interface,
 * jamais de Hibernate : changer de technique de persistance ne toucherait que les implémentations.
 */
public interface IDao<T> {
    boolean create(T o);
    boolean delete(T o);
    boolean update(T o);
    T getById(int id);
    List<T> getAll();
}
