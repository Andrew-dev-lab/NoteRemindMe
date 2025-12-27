package com.example.noteremindme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.io.Serializable;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAddNote = findViewById(R.id.btnAddNote);
        rvNotes = findViewById(R.id.rvNotes);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));

        btnAddNote.setOnClickListener(v ->
                startActivity(new Intent(this, AddNoteActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> notes =
                    AppDatabase.getInstance(this)
                            .noteDao()
                            .getAllNotes();

            runOnUiThread(() -> {
                rvNotes.setAdapter(new NoteAdapter(notes, note -> {
                    Intent intent =
                            new Intent(MainActivity.this, EditNoteActivity.class);
                    intent.putExtra("note", (Serializable) note);
                    startActivity(intent);
                }));
            });
        }).start();
    }
}
