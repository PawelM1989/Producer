package org.example.service;

import org.example.exceptions.TaskNotFoundExceptions;
import org.example.model.Task;
import org.example.model.dto.TaskDTO;
import org.example.repository.TaskRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService{

    @Autowired
    TaskRepository repository;
    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<TaskDTO> findAll() {
        return repository.findAll().stream()
                .map(task -> modelMapper.map(task, TaskDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TaskDTO findById(Integer id) {
        return modelMapper.map(repository.findById(id).orElseThrow(() -> new TaskNotFoundExceptions()), TaskDTO.class);
    }

    @Override
    public TaskDTO create(TaskDTO task) {
        Task newTask  = modelMapper.map(task, Task.class);
        return modelMapper.map(repository.saveAndFlush(newTask), TaskDTO.class);

    }
}
