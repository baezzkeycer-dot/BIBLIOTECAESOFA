package com.example.bibliotecaesofa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CatalogActivity extends AppCompatActivity {

    private BookRepository repository;
    private SessionManager sessionManager;
    private BookAdapter adapter;
    private String currentUser;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            repository.releaseExpired();
            adapter.refresh();
            updateEmptyState();
            updateSummary();
            handler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_catalog);
        repository = BookRepository.get(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText(getString(R.string.catalog_welcome, currentUser));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        RecyclerView recyclerView = findViewById(R.id.rvBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(repository.getBooks(), currentUser, new BookAdapter.Listener() {
            @Override
            public void onReserve(Book book) {
                reserveBook(book);
            }

            @Override
            public void onCancel(Book book) {
                cancelBook(book);
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.chipAll).setOnClickListener(v -> applyFilter(BookAdapter.Filter.ALL));
        findViewById(R.id.chipAvailable).setOnClickListener(v -> applyFilter(BookAdapter.Filter.AVAILABLE));
        findViewById(R.id.chipMine).setOnClickListener(v -> applyFilter(BookAdapter.Filter.MINE));

        applyFilter(BookAdapter.Filter.ALL);
    }

    private void applyFilter(BookAdapter.Filter filter) {
        adapter.setFilter(filter);
        updateEmptyState();
        updateSummary();
    }

    private void reserveBook(Book book) {
        if (repository.reserve(book, currentUser)) {
            adapter.refresh();
            updateEmptyState();
            updateSummary();
            showReservationDialog(book);
        } else {
            Toast.makeText(this, R.string.reserve_taken, Toast.LENGTH_SHORT).show();
            adapter.refresh();
            updateEmptyState();
            updateSummary();
        }
    }

    private void cancelBook(Book book) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cancel_title)
                .setMessage(R.string.cancel_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.cancel_action, (dialog, which) -> {
                    repository.cancel(book, currentUser);
                    adapter.refresh();
                    updateEmptyState();
                    updateSummary();
                    Toast.makeText(this, R.string.cancel_done, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showReservationDialog(Book book) {
        View view = getLayoutInflater().inflate(R.layout.dialog_reservation, null);

        TextView tvBook = view.findViewById(R.id.tvDialogBook);
        tvBook.setText(getString(R.string.reserve_success_book, book.getTitle()));

        TextView tvCode = view.findViewById(R.id.tvDialogCode);
        tvCode.setText(book.getPickupCode());

        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-CO"));
        String until = formatter.format(new Date(book.getReservedUntil()));
        int minutes = (int) (BookRepository.RESERVATION_DURATION_MS / 60000L);

        TextView tvValidity = view.findViewById(R.id.tvDialogValidity);
        tvValidity.setText(getResources().getQuantityString(
                R.plurals.reserve_validity, minutes, until, minutes));

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton(R.string.reserve_ok, null)
                .show();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        TextView emptyView = findViewById(R.id.tvEmpty);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (!empty) {
            return;
        }
        if (adapter.getFilter() == BookAdapter.Filter.AVAILABLE) {
            emptyView.setText(R.string.empty_available);
        } else if (adapter.getFilter() == BookAdapter.Filter.MINE) {
            emptyView.setText(R.string.empty_mine);
        } else {
            emptyView.setText(R.string.empty_all);
        }
    }

    private void updateSummary() {
        int available = 0;
        for (Book book : repository.getBooks()) {
            if (!book.isReserved()) {
                available++;
            }
        }
        TextView summary = findViewById(R.id.tvSummary);
        summary.setText(getString(R.string.catalog_summary, available, repository.getBooks().size()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }
}
