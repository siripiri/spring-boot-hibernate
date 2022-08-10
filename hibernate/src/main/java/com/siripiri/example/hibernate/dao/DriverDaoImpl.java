package com.siripiri.example.hibernate.dao;

import com.siripiri.example.hibernate.domain.Driver;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

public class DriverDaoImpl implements DriverDao {

    private final EntityManagerFactory entityManagerFactory;

    public DriverDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /*
     * Query:
     *   Now we use hibernate to create a query normally
     */
    @Override
    public List<Driver> findDriverLikeLastName(String likeLastName) {

        EntityManager entityManager = getEntityManager();

        try {
            Query query = entityManager.createQuery("SELECT d from Driver d where d.lastName like :last_name");
            query.setParameter("last_name", likeLastName + "%");
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    /*
     * Typed Query:
     *   While querying we may get a casting problem while executing a query which returns single result.
     *   In that case we use TypedQuery
     */

    // Without TypedQuery
    @Override
    public Driver findByLastName(String lastName) {
        EntityManager entityManager = getEntityManager();
        try{
            Query query = entityManager.createQuery("SELECT d from Driver d WHERE d.lastName = :last_name");
            query.setParameter("last_name", lastName);
            Driver driver = (Driver) query.getSingleResult(); // Type Casting --> wrong practice
            return driver;
        } catch (NoResultException ee) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    // With TypedQuery
    @Override
    public Driver findByFirstName(String firstName) {
        EntityManager entityManager = getEntityManager();
        try{
            TypedQuery<Driver> query = entityManager.createQuery("SELECT d from Driver d WHERE d.firstName = :first_name", Driver.class);
            query.setParameter("first_name", firstName);
            return query.getSingleResult();
        } catch (NoResultException ee) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Driver saveDriver(Driver driver) {
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(driver);
        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.close();
        return driver;
    }

    /*
     * NamedQuery:
     *
     * 	We need to annotate the entity itself to specify the query. The annotation will be @NamedQuery(name  =  "driver_find_all",  query = "FROM Driver")
     * since there is no creteria or condition it simply will return all the Driver object from the Database.
     * To implement this we need to add the implementation steps in the DAO class
     * Just use the:
     *       TypedQuery<Driver> listTypedQuery = entityManager.createNamedQuery("driver_find_all",Driver.class);
     * So we either mention the query or name from the @NamedQuery annotation mentioned in the Entity.
     *
     * Uses:
     *  If you have multiple classes need to access standardized query we can make use of NamedQuery.
     *  Here we are using only one DAO. which is simple if we have big application it will be useful
     */

    //Without Parameter
    @Override
    public Optional<List<Driver>> findAll() {
        EntityManager entityManager = getEntityManager();
        try{
            TypedQuery<Driver> listTypedQuery = entityManager.createNamedQuery("driver_find_all", Driver.class);
            return Optional.of(listTypedQuery.getResultList());
        } catch (NoResultException ee) {
            return Optional.of(null);
        } finally{
            entityManager.close();
        }
    }

    // With Parameter
    @Override
    public Driver findByName(String firstName, String lastName) {
        EntityManager entityManager = getEntityManager();
        try{
            TypedQuery<Driver> typedQuery = entityManager.createNamedQuery("find_by_name", Driver.class);
            typedQuery.setParameter("first_name", firstName);
            typedQuery.setParameter("last_name", lastName);
            return typedQuery.getSingleResult();
        } catch (NoResultException ee){
            return null;
        } finally {
            entityManager.close();
        }
    }

    /*
     * Native Query:
     *    We can use the normal sql query for this.
     */

    @Override
    public Driver findById(Long id) {
        EntityManager entityManager = getEntityManager();
        try{
            Query query = entityManager.createNativeQuery("select * from driver d where d.id = ?", Driver.class);
            query.setParameter(1, id);
            Driver driver = (Driver) query.getSingleResult();
            return driver;
        } catch (NoResultException ee) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
}
