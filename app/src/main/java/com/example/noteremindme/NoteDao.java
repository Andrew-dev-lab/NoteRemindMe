package com.example.noteremindme;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM Note ORDER BY id DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM Note WHERE id = :noteId LIMIT 1")
    Note getNoteById(int noteId);
}