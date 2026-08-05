package com.auca.library.util;

import com.auca.library.domain.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        Configuration config = new Configuration();

        // Database connection
        config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        config.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/auca_library_db");
        config.setProperty("hibernate.connection.username", "postgres");
        config.setProperty("hibernate.connection.password", "Elyse@1234");

        // Hibernate settings
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        config.setProperty("hibernate.hbm2ddl.auto", "update");
        config.setProperty("hibernate.show_sql", "true");
        config.setProperty("hibernate.format_sql", "true");

        // Entity mappings
        config.addAnnotatedClass(Location.class);
        config.addAnnotatedClass(User.class);
        config.addAnnotatedClass(Book.class);
        config.addAnnotatedClass(Shelf.class);
        config.addAnnotatedClass(Room.class);
        config.addAnnotatedClass(Borrower.class);
        config.addAnnotatedClass(Membership.class);
        config.addAnnotatedClass(MembershipType.class);

        return config.buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
