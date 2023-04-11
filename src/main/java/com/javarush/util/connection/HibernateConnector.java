package com.javarush.util.connection;

import com.javarush.domain.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;

public final class HibernateConnector {
    private static HibernateConnector instance;
    private final SessionFactory sessionFactory;

    private HibernateConnector() {
        try {
            Configuration configuration = new Configuration();
            configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());

            sessionFactory = configuration
                    .addAnnotatedClass(Actor.class)
                    .addAnnotatedClass(Address.class)
                    .addAnnotatedClass(Category.class)
                    .addAnnotatedClass(City.class)
                    .addAnnotatedClass(Country.class)
                    .addAnnotatedClass(Customer.class)
                    .addAnnotatedClass(Film.class)
                    .addAnnotatedClass(FilmText.class)
                    .addAnnotatedClass(Inventory.class)
                    .addAnnotatedClass(Language.class)
                    .addAnnotatedClass(Payment.class)
                    .addAnnotatedClass(Rental.class)
                    .addAnnotatedClass(Staff.class)
                    .addAnnotatedClass(Store.class)
                    .buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        if (instance == null) {
            instance = new HibernateConnector();
        }
        return instance.sessionFactory;
    }
}