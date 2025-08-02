package com.momcare.todoservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.momcare.todoservice.model.*;

import java.util.List;

public interface ToDoRepository extends JpaRepository<ToDo, Long> {
    List<ToDo> findByUserId(Long userId);
}
