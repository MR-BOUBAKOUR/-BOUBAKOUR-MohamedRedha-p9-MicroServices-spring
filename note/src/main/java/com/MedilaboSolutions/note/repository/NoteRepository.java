package com.MedilaboSolutions.note.repository;

import com.MedilaboSolutions.note.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, Long> {

    List<Note> findByPatId(Long patId);

}
