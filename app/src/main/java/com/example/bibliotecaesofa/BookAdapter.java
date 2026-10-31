package com.example.bibliotecaesofa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface Listener {
        void onReserve(Book book);

        void onCancel(Book book);
    }

    public enum Filter {
        ALL,
        AVAILABLE,
        MINE
    }

    private final List<Book> books;
    private final List<Book> visibleBooks = new ArrayList<>();
    private final String currentUser;
    private final Listener listener;

    private Filter filter = Filter.ALL;

    public BookAdapter(List<Book> books, String currentUser, Listener listener) {
        this.books = books;
        this.currentUser = currentUser;
        this.listener = listener;
        refresh();
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        refresh();
    }

    public Filter getFilter() {
        return filter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        visibleBooks.clear();
        long now = System.currentTimeMillis();
        for (Book book : books) {
            if (filter == Filter.ALL) {
                visibleBooks.add(book);
            } else if (filter == Filter.AVAILABLE && !book.isReserved()) {
                visibleBooks.add(book);
            } else if (filter == Filter.MINE
                    && book.getReservedBy() != null
                    && book.getReservedUntil() > now
                    && currentUser.equals(book.getReservedBy())) {
                visibleBooks.add(book);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = visibleBooks.get(position);
        Context context = holder.itemView.getContext();

        holder.ivCover.setImageResource(book.getCoverRes());
        holder.ivCover.setContentDescription(
                context.getString(R.string.book_cover_description, book.getTitle()));
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvSynopsis.setText(book.getSynopsis());

        holder.btnReserve.setOnClickListener(null);
        holder.btnCancel.setOnClickListener(null);

        boolean reserved = book.isReserved();
        boolean mine = reserved && currentUser.equals(book.getReservedBy());

        if (!reserved) {
            holder.tvStatus.setText(R.string.book_available);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvCountdown.setVisibility(View.GONE);
            holder.tvCode.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);

            holder.btnReserve.setVisibility(View.VISIBLE);
            holder.btnReserve.setEnabled(true);
            holder.btnReserve.setText(R.string.book_reserve);
            holder.btnReserve.setOnClickListener(v -> listener.onReserve(book));
        } else {
            long remaining = Math.max(0L, book.getReservedUntil() - System.currentTimeMillis());
            String remainingText = formatRemaining(remaining);

            if (mine) {
                holder.tvStatus.setText(R.string.book_mine);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_mine);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.military_dark));
                holder.tvCountdown.setText(context.getString(R.string.book_pickup, remainingText));
                holder.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.military_gold));
                holder.tvCode.setText(context.getString(R.string.book_code, book.getPickupCode()));
                holder.tvCode.setVisibility(View.VISIBLE);
                holder.btnReserve.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setOnClickListener(v -> listener.onCancel(book));
            } else {
                holder.tvStatus.setText(R.string.book_reserved);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvCountdown.setText(context.getString(R.string.book_available_in, remainingText));
                holder.tvCountdown.setTextColor(ContextCompat.getColor(context, R.color.military_red));
                holder.tvCode.setVisibility(View.GONE);
                holder.btnReserve.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
            }

            holder.tvCountdown.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return visibleBooks.size();
    }

    private String formatRemaining(long remainingMs) {
        long totalSeconds = (remainingMs + 999L) / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivCover;
        final TextView tvTitle;
        final TextView tvAuthor;
        final TextView tvSynopsis;
        final TextView tvStatus;
        final TextView tvCountdown;
        final TextView tvCode;
        final MaterialButton btnReserve;
        final MaterialButton btnCancel;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvSynopsis = itemView.findViewById(R.id.tvSynopsis);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCountdown = itemView.findViewById(R.id.tvCountdown);
            tvCode = itemView.findViewById(R.id.tvCode);
            btnReserve = itemView.findViewById(R.id.btnReserve);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
