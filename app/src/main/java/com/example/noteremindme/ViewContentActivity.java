package com.example.noteremindme;

import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewContentActivity extends AppCompatActivity {
    private EditText etContent;
    private TextView tvViewTitle;
    private ImageButton btnSetReminder;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content);

        etContent = findViewById(R.id.etContent);
        tvViewTitle = findViewById(R.id.tvViewTitle);
        btnSetReminder = findViewById(R.id.btnSetReminder);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        note = (Note) getIntent().getSerializableExtra("note");

        if (note == null) {
            int noteId = getIntent().getIntExtra("noteId", -1);
            if (noteId != -1) {
                loadNoteFromDatabase(noteId);
            }
        } else {
            displayNote();
        }

        btnSetReminder.setOnClickListener(v -> showTimePicker());
    }

    private void loadNoteFromDatabase(int id) {
        AsyncTask.execute(() -> {
            note = AppDatabase.getInstance(this).noteDao().getNoteById(id);
            if (note != null) {
                runOnUiThread(() -> {
                    tvViewTitle.setText(note.getTitle());
                    etContent.setText(note.getContent());
                });
            }
        });
    }

    private void displayNote() {
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

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            scheduleAlarm(calendar.getTimeInMillis());
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        timePicker.show();
    }

    private void scheduleAlarm(long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Check if we can schedule exact alarms (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Request permission from the user
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        try {
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("noteTitle", note.getTitle());
            intent.putExtra("noteId", note.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, note.getId(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // This is the call that triggers the SecurityException if not checked
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

            // Save to database
            note.setReminderTimestamp(timeInMillis);
            saveContent();

            android.widget.Toast.makeText(this, "Reminder set!", android.widget.Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            // Fallback for unexpected security issues
            e.printStackTrace();
        }
    }
}