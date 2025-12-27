package com.example.noteremindme;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewContentActivity extends AppCompatActivity {
    private EditText etContent;
    private TextView tvViewTitle;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content); // Buat XML ini nanti

        etContent = findViewById(R.id.etContent);
        tvViewTitle = findViewById(R.id.tvViewTitle);

        note = (Note) getIntent().getSerializableExtra("note");

        if (note != null) {
            tvViewTitle.setText(note.getTitle());
            etContent.setText(note.getContent());
        }
    }

    @Override
    public void onBackPressed() {
        saveContent();
        super.onBackPressed();
    }

    private void saveContent() {
        if (note != null) {
            String newContent = etContent.getText().toString();

            note.setContent(newContent);

            AsyncTask.execute(() -> {
                AppDatabase.getInstance(this).noteDao().update(note);
            });
        }
    }
}