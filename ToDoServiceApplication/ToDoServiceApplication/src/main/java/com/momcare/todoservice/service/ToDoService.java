package com.momcare.todoservice.service;


import java.util.List;
import com.momcare.todoservice.model.ToDo;

public interface ToDoService {
    ToDo createToDo(ToDo toDo);
    List<ToDo> getUserToDos(Long userId);
    ToDo updateToDo(Long id, ToDo toDo);
    void deleteToDo(Long id);
}
