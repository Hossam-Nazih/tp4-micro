# TP : Gestion d'un parc informatique avec Hibernate (DAO générique, HQL, transactions)

Hibernate 5.6 en API native (`Session`, `SessionFactory`, `hibernate.cfg.xml`), MySQL et JUnit 5.
Architecture en couches : entités → DAO (`IDao<T>`) → services (règles métier) → `MainTest`.

## Modèle

| Entité         | Champs                                                                                                   | Relations |
|----------------|----------------------------------------------------------------------------------------------------------|-----------|
| `Salle`        | `codeSalle` (unique), `designation`, `etage`, `capacitePostes`                                           | `machinesInstallees` → Machine (1-N, cascade ALL) |
| `Machine`      | `reference` (unique), `fabricant`, `modele`, `dateAchat`, `prixAchat`                                    | `salleAffectee` → Salle, `fournisseur` → Fournisseur, `interventions` (1-N) |
| `Fournisseur`  | `raisonSociale` (unique), `telephone`, `emailContact`                                                    | `machinesFournies` → Machine (1-N, sans cascade) |
| `Intervention` | `dateIntervention`, `typeIntervention` (enum), `compteRendu`, `coutIntervention`, `nomTechnicien`         | `machineConcernee` → Machine |

## Structure

```
src/main/java
├── entities/   Salle, Machine, Fournisseur, Intervention, TypeIntervention
├── dao/        IDao<T>, AbstractDao<T> (CRUD + transactions), SalleDao, MachineDao, FournisseurDao,
│               InterventionDao, BilanCoutMachine (résultat HQL), DaoException
├── services/   SalleService, MachineService, FournisseurService, InterventionService
├── util/       HibernateUtil (SessionFactory unique), JeuDeDonnees (remise à zéro des tables)
└── test/       MainTest
src/main/resources/hibernate.cfg.xml
src/test/java/services   SalleServiceTest, MachineServiceTest, HqlEtTransactionTest
```

## Base MySQL

Serveur local de MySQL Workbench : `localhost:3306`, `root`, mot de passe vide (`hibernate.cfg.xml`).
La base `parc_informatique_db` et ses tables sont créées automatiquement (`hbm2ddl.auto=update`).

## Lancer

```bash
mvn test                  # 28 tests, sur H2 en mémoire : MySQL n'est pas nécessaire
mvn compile exec:java     # MainTest, sur MySQL
```

## Règles métier

- Une salle a un code et une capacité positive ; une machine ne peut pas entrer dans une salle pleine.
- Codes de salle et références de machine uniques (contraintes en base).
- Un fournisseur qui a encore des machines au parc ne peut pas être supprimé.
- Une intervention a déjà eu lieu (pas de date future) et son coût n'est pas négatif.
- Supprimer une salle supprime ses machines, et supprimer une machine supprime ses interventions.

## Requêtes HQL (étape 10)

`findByFabricant`, `findBySalle` (jointure), `findAcheteesEntre` (between), `rechercher` (like, insensible à la casse),
`findSansIntervention` (`is empty`), `compterParSalle` (group by + left join), `bilanCoutParMachine`
(`select new`, sum, count), `findByType` (enum).

## Transactions (étape 11)

`MachineService.deplacerVers` déplace plusieurs machines en une seule transaction : si une machine est introuvable
ou si la salle n'a pas assez de place, le rollback annule tout et aucune machine ne change de salle.

## Écarts avec l'énoncé

- **Rollback après fermeture de la session** : avec `try (Session session = ...)`, la session est fermée *avant* le
  `catch`, et `tx.rollback()` échoue alors avec `IllegalStateException` en masquant l'erreur d'origine.
  La session est donc fermée dans un `finally`, après le rollback (`AbstractDao.enTransaction`).
- Le bloc session / transaction est écrit une seule fois dans `AbstractDao`, au lieu d'être recopié dans chaque méthode
  de chaque DAO ; `SalleDao` et les autres héritent de `AbstractDao` et implémentent donc toujours `IDao<T>`.
- `junit-jupiter` remplace `junit-jupiter-api`, qui ne contient pas le moteur d'exécution : sans lui, Maven ne lance aucun test.
- `com.mysql:mysql-connector-j` remplace `mysql:mysql-connector-java`, ancien nom du même pilote.
- Dans `hibernate.cfg.xml`, les `&` de l'URL sont écrits `&amp;`, sinon le fichier XML est invalide.

## Captures d'écran

<img width="1223" height="753" alt="image" src="https://github.com/user-attachments/assets/8b508489-11d6-4ff4-ab71-68354746f330" />
<img width="896" height="715" alt="image" src="https://github.com/user-attachments/assets/c28e427c-e95d-4fce-87f3-59f7eb0f2bd2" />
<img width="1291" height="460" alt="image" src="https://github.com/user-attachments/assets/fee3a7ba-8852-400d-94a3-25ef6848b44b" />
<img width="1488" height="616" alt="image" src="https://github.com/user-attachments/assets/077717f1-b713-4b8e-8b6f-6801565c7c39" />


