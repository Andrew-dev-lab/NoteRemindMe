package com.example.noteremindme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNotes;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvNotes = findViewById(R.id.rvNotes);
        FloatingActionButton btnAddNote = findViewById(R.id.btnAddNote);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));

        btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> notes = AppDatabase.getInstance(this).noteDao().getAllNotes();

            runOnUiThread(() -> {
                // Konfigurasi Adapter dengan OnNoteItemClickListener
                adapter = new NoteAdapter(notes, new NoteAdapter.OnNoteItemClickListener() {

                    @Override
                    public void onEdit(Note note) {
                        // Dipanggil saat klik "Edit" di menu titik tiga
                        // Membuka EditNoteActivity (Judul & Deskripsi saja)
                        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                        intent.putExtra("note", note);
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(Note note) {
                        // Dipanggil saat klik "Delete" di menu titik tiga
                        deleteNoteFromDb(note);
                    }

                    @Override
                    public void onItemClick(Note note) {
                        // Dipanggil saat BADAN/ISI catatan diklik di list
                        // Membuka ViewContentActivity untuk menulis konten
                        Intent intent = new Intent(MainActivity.this, ViewContentActivity.class);
                        intent.putExtra("note", note);
                        startActivity(intent);
                    }
                });
                rvNotes.setAdapter(adapter);
            });
        }).start();
    }

    private void deleteNoteFromDb(Note note) {
        new Thread(() -> {
            AppDatabase.getInstance(this).noteDao().delete(note);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Catatan dihapus", Toast.LENGTH_SHORT).show();
                loadNotes();
            });
        }).start();
    }
}