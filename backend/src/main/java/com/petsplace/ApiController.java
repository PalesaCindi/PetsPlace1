package com.petsplace;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.sql.Timestamp;

/**
 * Every address under /api. The website (js/app.js) calls these.
 * Catalogue data comes from catalog.json; accounts, reviews, newsletter sign-ups,
 * messages and orders are saved in MySQL (database "petsplace").
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
public class ApiController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Map<String, Double> DELIVERY = Map.of("standard", 75.0, "collect", 0.0);
    private static final List<String> PAYMENTS = List.of("Credit card", "EFT", "Samsung Pay", "Apple Pay");
    private static final double VAT_RATE = 0.15;
    private static final SecureRandom RANDOM = new SecureRandom();
    // A wrong email should take as long to reject as a wrong password.
    private static final String DUMMY_HASH = Passwords.hash("not-a-real-password");

    private final JdbcTemplate db;
    private final CatalogService catalog;
    private Map<String, Object> session(String name, String email) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("name", name);
    out.put("email", email);
    out.put("token", UUID.randomUUID().toString());
    return out;
}

    public ApiController(JdbcTemplate db, CatalogService catalog) {
        this.db = db;
        this.catalog = catalog;
    }

    // ---------------------------------------------------------------- health + catalogue

    @GetMapping("/health")
    public Map<String, Object> health() {
        Integer result = db.queryForObject("SELECT 1", Integer.class);
        return Map.of("status", "running",
                "database", result != null && result == 1 ? "connected" : "not connected");
    }

    @GetMapping("/breeds")
    public Object breeds() { return catalog.breeds(); }

    @GetMapping("/categories")
    public Object categories() { return catalog.categories(); }

    @GetMapping("/puppies")
    public Object puppies() { return catalog.puppies(); }

    @GetMapping("/products")
    public List<Map<String, Object>> products(@RequestParam(required = false) String category) {
        if (category == null) return catalog.products();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> p : catalog.products()) {
            if (category.equals(p.get("category"))) list.add(p);
        }
        return list;
    }

    @GetMapping("/products/{id}")
    public Map<String, Object> product(@PathVariable String id) {
        Map<String, Object> p = catalog.product(id);
        if (p == null) throw new ApiError(404, "Product not found.");
        return p;
    }

    // ---------------------------------------------------------------- accounts

@PostMapping("/auth/register")
@ResponseStatus(HttpStatus.CREATED)
public Map<String, Object> register(@RequestBody Map<String, Object> in) {
    String name = text(in, "name", 100);
    String email = email(in);
    Object password = in.get("password");

    if (name.isEmpty()) {
        throw bad("Please enter your full name.");
    }

    if (!(password instanceof String)
            || ((String) password).length() < 8
            || ((String) password).length() > 255) {
        throw bad("Password must be at least 8 characters.");
    }

    try {
        db.update(
            "INSERT INTO signup (name, email, password) VALUES (?, ?, ?)",
            name,
            email,
           ((String) password)
        );
    } catch (DuplicateKeyException e) {
        throw new ApiError(409, "An account with that email already exists.");
    }

    return session(name, email);
}

   
@PostMapping("/auth/login")
public Map<String, Object> login(@RequestBody Map<String, Object> in) {
    String email = email(in);
    Object password = in.get("password");

    List<Map<String, Object>> rows = db.queryForList(
        "SELECT name, password FROM signup WHERE email = ?",
        email
    );

    Map<String, Object> user = rows.isEmpty() ? null : rows.get(0);

    String storedPassword = user == null
        ? DUMMY_HASH
        : String.valueOf(user.get("password"));

    boolean ok = password instanceof String
        && Passwords.matches((String) password, storedPassword)
        && user != null;

    if (!ok) {
        throw new ApiError(401, "Incorrect email or password.");
    }

    db.update(
        "INSERT INTO signin (email, password) VALUES (?, ?)",
        email,
        storedPassword
    );

    return session(String.valueOf(user.get("name")), email);
}


@PostMapping("/auth/forgot-password")
public Map<String, Object> forgotPassword(
        @RequestBody Map<String, Object> in) {

    String email = email(in);

    List<Map<String, Object>> users = db.queryForList(
        "SELECT email FROM signup WHERE email = ?",
        email
    );

    Map<String, Object> response = new HashMap<>();
    response.put("message",
        "If an account exists, password reset instructions will be provided.");

    if (users.isEmpty()) {
        return response;
    }

    String token = UUID.randomUUID().toString();

    db.update(
        "INSERT INTO password_reset_tokens " +
        "(email, token, expires_at, used) VALUES (?, ?, ?, FALSE)",
        email,
        token,
        Timestamp.valueOf(LocalDateTime.now().plusMinutes(15))
    );

    // Development only: use this token to test the reset flow.
    response.put("resetToken", token);

    return response;
}

    // ---------------------------------------------------------------- reviews

    @GetMapping("/reviews")
    public List<Map<String, Object>> reviews() {
        return db.query(
                "SELECT author, dogs, product, page_rating, comment_text, created_at FROM reviews ORDER BY id DESC",
                (rs, i) -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("author", rs.getString("author"));
                    r.put("dogs", rs.getInt("dogs"));
                    r.put("product", rs.getInt("product"));
                    r.put("page", rs.getInt("page_rating"));
                    r.put("comment", rs.getString("comment_text"));
                    r.put("createdAt", rs.getTimestamp("created_at").toInstant().toString());
                    return r;
                });
    }

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addReview(@RequestBody Map<String, Object> in) {
        int dogs = whole(in, "dogs"), product = whole(in, "product"), page = whole(in, "page");
        for (int rating : new int[]{dogs, product, page}) {
            if (rating < 1 || rating > 5) throw bad("Ratings must be between 1 and 5 stars.");
        }
        String author = text(in, "author", 60);
        if (author.isEmpty()) author = "Guest";
        String comment = text(in, "comment", 1000);
        db.update("INSERT INTO reviews (author, dogs, product, page_rating, comment_text) VALUES (?, ?, ?, ?, ?)",
                author, dogs, product, page, comment);
        Map<String, Object> review = new LinkedHashMap<>();
        review.put("author", author);
        review.put("dogs", dogs);
        review.put("product", product);
        review.put("page", page);
        review.put("comment", comment);
        return review;
    }

    // ---------------------------------------------------------------- newsletter + contact


@PostMapping("/newsletter")
public Map<String, Object> subscribe(@RequestBody Map<String, Object> in) {
    String first = text(in, "firstName", 100);
    if (first.isEmpty()) {
        first = text(in, "firstname", 100);
    }

    String last = text(in, "lastName", 100);
    if (last.isEmpty()) {
        last = text(in, "lastname", 100);
    }

    String emailAddress = email(in);
    String country = text(in, "country", 100);

    if (first.isEmpty()) {
        throw bad("Please enter your first name.");
    }

    if (last.isEmpty()) {
        throw bad("Please enter your last name.");
    }

    if (country.isEmpty()) {
        throw bad("Please select your country.");
    }

    db.update("""
        INSERT INTO newsletter_subscribers
            (firstname, lastname, email, country)
        VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            firstname = VALUES(firstname),
            lastname = VALUES(lastname),
            country = VALUES(country)
        """,
        first, last, emailAddress, country);

    return Map.of("status", "subscribed");
}


    
    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> contact(@RequestBody Map<String, Object> in) {
        String name = text(in, "name", 100);
        String message = text(in, "message", 2000);
        if (name.isEmpty()) throw bad("Please enter your name.");
        String email = email(in);
        if (message.isEmpty()) throw bad("Please type a message.");
        db.update("INSERT INTO contact_messages (name, email, message) VALUES (?, ?, ?)", name, email, message);
        return Map.of("status", "received");
    }

    // ---------------------------------------------------------------- orders

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> placeOrder(@RequestBody Map<String, Object> in) {
        Object rawItems = in.get("items");
        if (!(rawItems instanceof List) || ((List<Object>) rawItems).isEmpty()) throw bad("Your cart is empty.");
        List<Object> items = (List<Object>) rawItems;
        if (items.size() > 50) throw bad("That's too many different items in one order.");

        String method = text(in, "deliveryMethod", 20);
        if (!DELIVERY.containsKey(method)) throw bad("Please choose a delivery method.");
        String payment = text(in, "paymentMethod", 30);
        if (!PAYMENTS.contains(payment)) throw bad("Please choose a payment method.");

        String street = "", city = "", postal = "", coords = "";
        if (in.get("address") instanceof Map) {
            Map<String, Object> a = (Map<String, Object>) in.get("address");
            street = text(a, "street", 200);
            city = text(a, "city", 100);
            postal = text(a, "postal", 12);
            coords = text(a, "coords", 40);
        }
        if (method.equals("standard") && (street.isEmpty() || city.isEmpty())) {
            throw bad("Please enter your delivery address.");
        }

        // Prices always come from OUR catalogue, never from the browser.
        List<Object[]> lines = new ArrayList<>();
        double subtotal = 0;
        for (Object raw : items) {
            if (!(raw instanceof Map)) throw bad("Something is wrong with an item in your cart.");
            Map<String, Object> item = (Map<String, Object>) raw;
            Map<String, Object> product = catalog.product(text(item, "productId", 40));
            if (product == null) throw bad("An item in your cart is no longer available.");
            int quantity = whole(item, "quantity");
            if (quantity < 1 || quantity > 20) throw bad("Quantity must be between 1 and 20.");
            String size = text(item, "size", 30);
            List<Object> sizes = (List<Object>) product.get("sizes");
            if (sizes != null && !sizes.isEmpty() && !sizes.contains(size)) {
                throw bad("Please choose a valid size for " + product.get("name") + ".");
            }
            double price = ((Number) product.get("price")).doubleValue();
            double lineTotal = round(price * quantity);
            lines.add(new Object[]{product.get("id"), product.get("name"), size, quantity, price, lineTotal});
            subtotal += lineTotal;
        }

        double delivery = DELIVERY.get(method);
        double total = round(subtotal + delivery);
        double vat = round(total * VAT_RATE / (1 + VAT_RATE));   // VAT is already inside the total
        String orderRef = "PP-" + (100000 + RANDOM.nextInt(900000));
        String email = in.get("email") instanceof String ? ((String) in.get("email")).trim().toLowerCase() : "";
        if (email.length() > 120) email = email.substring(0, 120);

        db.update("INSERT INTO orders (order_ref, email, street, city, postal, coords, delivery_method, payment_method, "
                        + "subtotal, delivery, total, vat_included) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                orderRef, email, street, city, postal, coords, method, payment, round(subtotal), delivery, total, vat);
        for (Object[] l : lines) {
            db.update("INSERT INTO order_items (order_ref, product_id, product_name, size, quantity, unit_price, line_total) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)", orderRef, l[0], l[1], l[2], l[3], l[4], l[5]);
        }

        Map<String, Object> reply = new LinkedHashMap<>();
        reply.put("orderId", orderRef);
        reply.put("subtotal", round(subtotal));
        reply.put("delivery", delivery);
        reply.put("total", total);
        reply.put("vatIncluded", vat);
        return reply;
    }

    // ---------------------------------------------------------------- small helpers

    private static ApiError bad(String message) { return ApiError.badRequest(message); }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }

    /** A text field, trimmed and cut to a maximum length. Missing or not-text becomes "". */
    private static String text(Map<String, Object> map, String key, int max) {
        Object v = map.get(key);
        if (!(v instanceof String)) return "";
        String t = ((String) v).trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static int whole(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof Number ? ((Number) v).intValue() : 0;
    }

    private static String email(Map<String, Object> map) {
        String e = text(map, "email", 120).toLowerCase();
        if (!EMAIL.matcher(e).matches()) throw bad("Please enter a valid email address.");
        return e;
    }
}
