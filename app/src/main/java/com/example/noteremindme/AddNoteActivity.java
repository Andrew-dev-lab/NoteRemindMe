package com.example.noteremindme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddNoteActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etContent;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Silakan masukkan judul", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncTask.execute(() -> {
            Note note = new Note();
            note.setTitle(title);
            note.setDescription(description);
            note.setContent(content);
            note.setSummary("");

            // Simpan ke Database
            AppDatabase.getInstance(getApplicationContext()).noteDao().insert(note);

            runOnUiThread(() -> {
                Toast.makeText(AddNoteActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}