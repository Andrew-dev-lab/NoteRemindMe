package com.example.noteremindme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditNoteActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Button btnUpdate;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnUpdate = findViewById(R.id.btnUpdate);

        note = (Note) getIntent().getSerializableExtra("note");

        if (note != null) {
            etTitle.setText(note.getTitle());
            etDescription.setText(note.getDescription());
        }

        btnUpdate.setOnClickListener(v -> updateNote());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            deleteNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNote() {
        if (note == null) return;

        note.setTitle(etTitle.getText().toString());
        note.setDescription(etDescription.getText().toString());

        AsyncTask.execute(() -> {
            AppDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteNote() {
        if (note == null) return;

        AsyncTask.execute(() -> {
            AppDatabase.getInstance(this).noteDao().delete(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}