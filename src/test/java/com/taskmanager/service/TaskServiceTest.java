package com.taskmanager.service;

import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.Task.TaskPriority;
import com.taskmanager.model.Task.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setPriority(TaskPriority.MEDIUM);
    }

    @Test
    void testCreateTask_Success() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(10)).save(any(Task.class));
    }

    @Test
    void testCreateTask_WithNullStatus_SetsDefaultStatus() {
        testTask.setStatus(null);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTask_WithNullPriority_SetsDefaultPriority() {
        testTask.setPriority(null);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateTask_Success() {
        Task updatedData = new Task();
        updatedData.setTitle("Updated Title");
        updatedData.setDescription("Updated Description");
        updatedData.setStatus(TaskStatus.IN_PROGRESS);
        updatedData.setPriority(TaskPriority.HIGH);

        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTask(1L, updatedData);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateTask_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(999L, testTask);
        });

        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testCompleteTask_Success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.completeTask(1L);

        assertEquals(TaskStatus.COMPLETED, testTask.getStatus());
        assertNotNull(testTask.getCompletedAt());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCompleteTask_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.completeTask(999L);
        });

        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testCancelTask_Success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.cancelTask(1L);

        assertEquals(TaskStatus.CANCELLED, testTask.getStatus());
        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCancelTask_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.cancelTask(999L);
        });

        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask_Success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(any(Task.class));

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, times(1)).delete(any(Task.class));
    }

    @Test
    void testDeleteTask_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });

        verify(taskRepository, times(1)).findById(anyLong());
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void testFindTaskById_Success() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(testTask));

        Task result = taskService.findTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findById(anyLong());
    }

    @Test
    void testFindTaskById_NotFound_ThrowsException() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.findTaskById(999L);
        });

        verify(taskRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetAllTasks_Success() {
        List<Task> tasks = Arrays.asList(testTask, new Task());
        when(taskRepository.findByOrderByCreatedAtDesc()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByOrderByCreatedAtDesc();
    }

    @Test
    void testGetTasksByStatus_Success() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByStatusOrderByCreatedAtDesc(any(TaskStatus.class))).thenReturn(tasks);

        List<Task> result = taskService.getTasksByStatus(TaskStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByStatusOrderByCreatedAtDesc(any(TaskStatus.class));
    }

    @Test
    void testGetTasksByPriority_Success() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByPriorityOrderByCreatedAtDesc(any(TaskPriority.class))).thenReturn(tasks);

        List<Task> result = taskService.getTasksByPriority(TaskPriority.MEDIUM);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByPriorityOrderByCreatedAtDesc(any(TaskPriority.class));
    }

    @Test
    void testGetPendingTasks_Success() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.PENDING)).thenReturn(tasks);

        List<Task> result = taskService.getPendingTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByStatusOrderByCreatedAtDesc(TaskStatus.PENDING);
    }

    @Test
    void testGetCompletedTasks_Success() {
        testTask.setStatus(TaskStatus.COMPLETED);
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.COMPLETED)).thenReturn(tasks);

        List<Task> result = taskService.getCompletedTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByStatusOrderByCreatedAtDesc(TaskStatus.COMPLETED);
    }

    @Test
    void testCountTasks_Success() {
        when(taskRepository.count()).thenReturn(5L);

        long result = taskService.countTasks();

        assertEquals(5L, result);
        verify(taskRepository, times(1)).count();
    }

    @Test
    void testCountTasksByStatus_Success() {
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(3L);

        long result = taskService.countTasksByStatus(TaskStatus.PENDING);

        assertEquals(3L, result);
        verify(taskRepository, times(1)).countByStatus(any(TaskStatus.class));
    }
}
