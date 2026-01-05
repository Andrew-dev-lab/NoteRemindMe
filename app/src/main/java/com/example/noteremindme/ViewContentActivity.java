package com.example.noteremindme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class ViewContentActivity extends AppCompatActivity {
    private EditText etContent;
    private TextView tvViewTitle;
    private ImageButton btnSetReminder;
    private Button btnBullet;
    private Note note;

    private boolean isFormatting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content);

        etContent = findViewById(R.id.etContent);
        tvViewTitle = findViewById(R.id.tvViewTitle);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnBullet = findViewById(R.id.btnBullet);

        setupNoteEditing();

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

        btnBullet.setOnClickListener(v -> insertBullet());
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

    private static class BulletReplacementSpan extends android.text.style.ReplacementSpan {
        private final int bulletColor;
        private final int bulletRadius = 10; // Adjust size
        private final int gapWidth = 20;     // Space between bullet and text

        public BulletReplacementSpan(int bulletColor) {
            this.bulletColor = bulletColor;
        }

        @Override
        public int getSize(android.graphics.Paint paint, CharSequence text, int start, int end, android.graphics.Paint.FontMetricsInt fm) {
            // The span takes up the width of the bullet + gap
            return 2 * bulletRadius + gapWidth;
        }

        @Override
        public void draw(android.graphics.Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, android.graphics.Paint paint) {
            int originalColor = paint.getColor();
            paint.setColor(bulletColor);

            // Draw the bullet circle vertically centered
            // 'y' is the baseline. We adjust slightly up to center it on the text.
            canvas.drawCircle(x + bulletRadius, (top + bottom) / 2f, bulletRadius, paint);

            paint.setColor(originalColor);
        }
    }

    private void setupNoteEditing() {
        // 1. Markwon Initialization (Keep existing)
        final Markwon markwon = Markwon.create(this);
        final MarkwonEditor editor = MarkwonEditor.builder(markwon).build();
        etContent.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

        // 2. Custom Visual Bullets (From previous step - keep this!)
        etContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Re-apply the bullet visual style
                formatCustomBullets(s);
            }
        });

        // 3. Auto-Bullet Logic (Enter Key)
        etContent.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false; // Flag to prevent infinite loops

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;

                // Detect if a single newline character was just added
                if (count == 1 && s.charAt(start) == '\n') {
                    handleAutoBullet(s, start);
                }
            }

            @Override public void afterTextChanged(Editable s) {}

            private void handleAutoBullet(CharSequence s, int currentCursorPos) {
                isFormatting = true; // Lock
                try {
                    // Find the previous line bounds
                    int prevLineEnd = currentCursorPos;
                    int prevLineStart = lastIndexOf(s, '\n', prevLineEnd - 1) + 1;

                    if (prevLineStart >= 0 && prevLineStart < prevLineEnd) {
                        CharSequence prevLine = s.subSequence(prevLineStart, prevLineEnd);
                        String prevLineStr = prevLine.toString();

                        // Check if previous line starts with a bullet
                        if (prevLineStr.startsWith("* ")) {
                            Editable editable = etContent.getText();

                            // Case 1: Previous line was ONLY a bullet ("* ") -> End the list
                            if (prevLineStr.trim().equals("*")) {
                                // Delete the previous "* " and the newline we just typed
                                // Range: start of previous line to current cursor + 1
                                editable.delete(prevLineStart, currentCursorPos + 1);
                            }
                            // Case 2: Previous line had text -> Continue the list
                            else {
                                // Insert bullet on the new line
                                editable.insert(currentCursorPos + 1, "* ");
                            }
                        }
                    }
                } finally {
                    isFormatting = false; // Unlock
                }
            }
        });

        // 4. Smart Backspace Logic (Delete "* " in one go)
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // We detect a deletion when 'count' (old text) is 1 and 'after' (new text) is 0
                if (count == 1 && after == 0) {
                    // Check if the character being deleted is the space of a bullet point "* "
                    // The 'start' index is the position of the character being removed
                    if (s.length() >= 2 && start > 0 && s.charAt(start) == ' ' && s.charAt(start - 1) == '*') {
                        // Use a Runnable to delete the '*' because you cannot
                        // modify the Editable safely inside beforeTextChanged
                        etContent.post(() -> {
                            Editable text = etContent.getText();
                            if (text.length() >= start && text.charAt(start - 1) == '*') {
                                text.delete(start - 1, start);
                            }
                        });
                    }
                }
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void formatCustomBullets(Editable editable) {
        // Remove old spans to avoid duplicates (optional, but good for cleanliness)
        BulletReplacementSpan[] existingSpans = editable.getSpans(0, editable.length(), BulletReplacementSpan.class);
        for (BulletReplacementSpan span : existingSpans) {
            editable.removeSpan(span);
        }

        // Regex to find lines starting with "* " (Markdown bullet)
        // (?m) enables multiline mode so ^ matches start of line
        Pattern pattern = Pattern.compile("(?m)^\\*\\s+");
        Matcher matcher = pattern.matcher(editable);

        // Get the current text color to match the bullet color
        int color = etContent.getCurrentTextColor();

        while (matcher.find()) {
            // Apply the span to the characters "* "
            editable.setSpan(
                    new BulletReplacementSpan(color),
                    matcher.start(),
                    matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private int lastIndexOf(CharSequence s, char c, int start) {
        for (int i = start; i >= 0; i--) {
            if (s.charAt(i) == c) return i;
        }
        return -1;
    }

    private void insertBullet() {
        int start = etContent.getSelectionStart();
        Editable editable = etContent.getText();

        // Determine if we need a newline before the bullet
        String bulletText = "* ";
        if (start > 0 && editable.charAt(start - 1) != '\n') {
            bulletText = "\n* ";
        }

        editable.insert(start, bulletText);
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