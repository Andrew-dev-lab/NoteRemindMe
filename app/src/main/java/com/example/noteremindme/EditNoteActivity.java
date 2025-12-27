package com.example.noteremindme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnUpdate, btnDelete;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        note = (Note) getIntent().getSerializableExtra("note");

        etTitle.setText(note.getTitle());
        etContent.setText(note.getContent());

        btnUpdate.setOnClickListener(v -> updateNote());
        btnDelete.setOnClickListener(v -> deleteNote());
    }

    private void updateNote() {
        note.setTitle(etTitle.getText().toString());
        note.setContent(etContent.getText().toString());

        AsyncTask.execute(() -> {
            AppDatabase.getInstance(this).noteDao().update(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteNote() {
        AsyncTask.execute(() -> {
            AppDatabase.getInstance(this).noteDao().delete(note);
            runOnUiThread(() -> {
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
