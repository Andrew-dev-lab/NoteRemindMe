package com.example.noteremindme;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton btnAddNote = findViewById(R.id.btnAddNote);
        rvNotes = findViewById(R.id.rvNotes);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));

        // Pindah ke AddNoteActivity saat tombol + diklik
        btnAddNote.setOnClickListener(v ->
                startActivity(new Intent(this, AddNoteActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data setiap kali kembali ke halaman ini
        loadNotes();
    }

    private void loadNotes() {
        // Mengambil data dari database di thread terpisah agar UI tidak lag
        new Thread(() -> {
            List<Note> notes = AppDatabase.getInstance(this).noteDao().getAllNotes();

            runOnUiThread(() -> {
                // Set adapter dengan listener untuk klik item
                rvNotes.setAdapter(new NoteAdapter(notes, note -> {
                    // Saat note diklik, buka EditNoteActivity dan kirim datanya
                    Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                    intent.putExtra("note", (Serializable) note);
                    startActivity(intent);
                }));
            });
        }).start();
    }
}