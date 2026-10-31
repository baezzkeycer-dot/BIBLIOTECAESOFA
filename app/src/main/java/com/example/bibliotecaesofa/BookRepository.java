package com.example.bibliotecaesofa;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookRepository {

    public static final long RESERVATION_DURATION_MS = 15 * 60 * 1000L;

    private static final String KEY_RESERVATIONS = "reservations";

    private static BookRepository instance;

    private final SharedPreferences preferences;
    private final List<Book> books = new ArrayList<>();
    private final SecureRandom random = new SecureRandom();

    private BookRepository(Context context) {
        preferences = context.getSharedPreferences(SessionManager.PREFS_NAME, Context.MODE_PRIVATE);
        books.add(new Book("b1",
                context.getString(R.string.book1_title),
                context.getString(R.string.book1_author),
                context.getString(R.string.book1_synopsis),
                R.drawable.cover_arte_guerra));
        books.add(new Book("b2",
                context.getString(R.string.book2_title),
                context.getString(R.string.book2_author),
                context.getString(R.string.book2_synopsis),
                R.drawable.cover_manual_soldado));
        books.add(new Book("b3",
                context.getString(R.string.book3_title),
                context.getString(R.string.book3_author),
                context.getString(R.string.book3_synopsis),
                R.drawable.cover_historia_militar));
        books.add(new Book("b4",
                context.getString(R.string.book4_title),
                context.getString(R.string.book4_author),
                context.getString(R.string.book4_synopsis),
                R.drawable.cover_estrategia));
        books.add(new Book("b5",
                context.getString(R.string.book5_title),
                context.getString(R.string.book5_author),
                context.getString(R.string.book5_synopsis),
                R.drawable.cover_logistica));
        books.add(new Book("b6",
                context.getString(R.string.book6_title),
                context.getString(R.string.book6_author),
                context.getString(R.string.book6_synopsis),
                R.drawable.cover_banderas));
        books.add(new Book("b7",
                context.getString(R.string.book7_title),
                context.getString(R.string.book7_author),
                context.getString(R.string.book7_synopsis),
                R.drawable.cover_valor_honor));
        books.add(new Book("b8",
                context.getString(R.string.book8_title),
                context.getString(R.string.book8_author),
                context.getString(R.string.book8_synopsis),
                R.drawable.cover_armamento));

        loadReservations();
        releaseExpired();
    }

    public static synchronized BookRepository get(Context context) {
        if (instance == null) {
            instance = new BookRepository(context.getApplicationContext());
        }
        return instance;
    }

    public List<Book> getBooks() {
        return books;
    }

    public boolean reserve(Book book, String user) {
        releaseExpired();
        if (book.isReserved()) {
            return false;
        }
        book.setReservation(user, System.currentTimeMillis() + RESERVATION_DURATION_MS, generateCode());
        persistReservations();
        return true;
    }

    public void cancel(Book book, String user) {
        if (user.equals(book.getReservedBy())) {
            book.clearReservation();
            persistReservations();
        }
    }

    public void releaseExpired() {
        boolean changed = false;
        long now = System.currentTimeMillis();
        for (Book book : books) {
            if (book.getReservedBy() != null && book.getReservedUntil() <= now) {
                book.clearReservation();
                changed = true;
            }
        }
        if (changed) {
            persistReservations();
        }
    }

    private String generateCode() {
        while (true) {
            String code = String.format(Locale.US, "ESOFA-%04d", random.nextInt(10000));
            boolean duplicated = false;
            for (Book book : books) {
                if (book.isReserved() && code.equals(book.getPickupCode())) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated) {
                return code;
            }
        }
    }

    private void loadReservations() {
        String raw = preferences.getString(KEY_RESERVATIONS, null);
        if (raw == null) {
            return;
        }
        try {
            JSONObject stored = new JSONObject(raw);
            long now = System.currentTimeMillis();
            for (Book book : books) {
                JSONObject reservation = stored.optJSONObject(book.getId());
                if (reservation == null) {
                    continue;
                }
                long until = reservation.optLong("until", 0L);
                if (until <= now) {
                    continue;
                }
                book.setReservation(
                        reservation.optString("user", ""),
                        until,
                        reservation.optString("code", ""));
            }
        } catch (JSONException ignored) {
            // Si el almacenamiento está corrupto, se empieza sin reservas.
        }
    }

    private void persistReservations() {
        JSONObject stored = new JSONObject();
        try {
            for (Book book : books) {
                if (!book.isReserved()) {
                    continue;
                }
                JSONObject reservation = new JSONObject();
                reservation.put("user", book.getReservedBy());
                reservation.put("until", book.getReservedUntil());
                reservation.put("code", book.getPickupCode());
                stored.put(book.getId(), reservation);
            }
        } catch (JSONException ignored) {
            // No debería ocurrir con claves y valores simples.
        }
        preferences.edit().putString(KEY_RESERVATIONS, stored.toString()).apply();
    }
}
