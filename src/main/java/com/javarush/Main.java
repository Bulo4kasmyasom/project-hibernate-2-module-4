package com.javarush;

import com.javarush.dao.*;
import com.javarush.domain.*;
import com.javarush.util.connection.HibernateConnector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    private final SessionFactory sessionFactory = HibernateConnector.getSessionFactory();
    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StoreDAO storeDAO;

    public Main() {
        actorDAO = new ActorDAO(sessionFactory);
        addressDAO = new AddressDAO(sessionFactory);
        categoryDAO = new CategoryDAO(sessionFactory);
        cityDAO = new CityDAO(sessionFactory);
        customerDAO = new CustomerDAO(sessionFactory);
        filmDAO = new FilmDAO(sessionFactory);
        filmTextDAO = new FilmTextDAO(sessionFactory);
        inventoryDAO = new InventoryDAO(sessionFactory);
        languageDAO = new LanguageDAO(sessionFactory);
        paymentDAO = new PaymentDAO(sessionFactory);
        rentalDAO = new RentalDAO(sessionFactory);
        storeDAO = new StoreDAO(sessionFactory);
    }

    public static void main(String[] args) {
        Main main = new Main();
        Customer customer = main.createCustomer();
        main.customerReturnInventoryToStore();
        main.customerRentInventory(customer);
        main.newFilmWasMade();
    }

    private void newFilmWasMade() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Language language = languageDAO.getItems(0, 20)
                    .stream()
                    .unordered()
                    .findAny()
                    .orElse(null);
            List<Category> categories = categoryDAO.getItems(0, 5);
            List<Actor> actors = actorDAO.getItems(0, 5);

            Film film = newFilm(language, categories, actors);

            FilmText filmText = newFilmText(film);
            filmTextDAO.save(filmText);

            session.getTransaction().commit();
        }
    }

    private FilmText newFilmText(Film film) {
        FilmText filmText = new FilmText();
        filmText.setFilm(film);
        filmText.setId(film.getId());
        filmText.setDescription("comedy");
        filmText.setTitle("My fairy tales");
        return filmText;
    }

    private Film newFilm(Language language, List<Category> categories, List<Actor> actors) {
        Film film = new Film();
        film.setActors(new HashSet<>(actors));
        film.setRating(Rating.NC17);
        film.setSpecialFeatures(Set.of(Feature.TRAILERS, Feature.COMMENTARIES));
        film.setLength((short) 123);
        film.setReplacementCost(BigDecimal.TEN);
        film.setRentalRate(BigDecimal.ONE);
        film.setLanguage(language);
        film.setDescription("comedy");
        film.setTitle("My fairy tales");
        film.setRentalDuration((byte) 4);
        film.setOriginalLanguage(language);
        film.setCategories(new HashSet<>(categories));
        film.setYear(Year.now());
        filmDAO.save(film);
        return film;
    }

    private void customerRentInventory(Customer customer) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Film film = filmDAO.getFirstAvailableFilmForRent();
            Store store = storeDAO.getItems(0, 1).get(0);

            Inventory inventory = newInventory(film, store);
            inventoryDAO.save(inventory);
            Staff staff = store.getStaff();
            Rental rental = newRental(customer, inventory, staff);
            rentalDAO.save(rental);
            Payment payment = newPayment(customer, staff, rental);
            paymentDAO.save(payment);
            session.getTransaction().commit();
        }
    }

    private Inventory newInventory(Film film, Store store) {
        Inventory inventory = new Inventory();
        inventory.setFilm(film);
        inventory.setStore(store);
        return inventory;
    }

    private Payment newPayment(Customer customer, Staff staff, Rental rental) {
        Payment payment = new Payment();
        payment.setCustomer(customer);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStaff(staff);
        payment.setAmount(BigDecimal.valueOf(100.25));
        payment.setRental(rental);
        return payment;
    }

    private Rental newRental(Customer customer, Inventory inventory, Staff staff) {
        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setInventory(inventory);
        rental.setRentalDate(LocalDateTime.now());
        rental.setStaff(staff);
        return rental;
    }

    private void customerReturnInventoryToStore() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Rental rental = rentalDAO.getAnyUnreturnedRental();
            rental.setReturnDate(LocalDateTime.now());
            rentalDAO.save(rental);

            session.getTransaction().commit();
        }
    }

    private Customer createCustomer() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Store store = storeDAO.getItems(0, 1).get(0);
            City city = cityDAO.getByName("Barcelona");
            Address address = newAddress(city);
            addressDAO.save(address);
            Customer customer = newCustomer(store, address);
            customerDAO.save(customer);
            session.getTransaction().commit();
            return customer;
        }
    }

    private Customer newCustomer(Store store, Address address) {
        Customer customer = new Customer();
        customer.setAddress(address);
        customer.setActive(true);
        customer.setEmail("name@mail.com");
        customer.setFirstName("John");
        customer.setLastName("Wood");
        customer.setStore(store);
        return customer;
    }

    private Address newAddress(City city) {
        Address address = new Address();
        address.setAddress("Freedom str, 50");
        address.setPhone("111-222-333");
        address.setCity(city);
        address.setDistrict("WildCats");
        return address;
    }
}