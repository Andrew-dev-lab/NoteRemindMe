package com.example.noteremindme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private List<Note> notes;
    private OnNoteItemClickListener listener;

    public interface OnNoteItemClickListener {
        void onEdit(Note note);
        void onDelete(Note note);

        void onItemClick(Note note);
    }

    public NoteAdapter(List<Note> notes, OnNoteItemClickListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDescription.setText(note.getDescription());

        // Klik pada titik tiga
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(note);
        });

// 2. Klik Menu Titik Tiga untuk Edit Judul
        holder.ivMenuMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.ivMenuMore);
            popup.inflate(R.menu.item_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_edit) {
                    listener.onEdit(note); // Ini lari ke EditNoteActivity
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return notes.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageButton ivMenuMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivMenuMore = itemView.findViewById(R.id.ivMenuMore);
        }
    }
}