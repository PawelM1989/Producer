package org.example

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.example.service.TaskService
import org.springframework.http.MediaType
import org.example.contoller.TaskController
import org.example.exceptions.TaskNotFoundException
import org.example.model.dto.TaskDTO
import org.example.model.dto.UserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Unroll

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerSpec extends Specification {

    private static TaskDTO dto1 = new TaskDTO(1, "test", true, null, null)
    private static TaskDTO dto2 = new TaskDTO(2, "test2", false, 20, new UserDTO(1, 'Jan', 'Kowalski', 'jk@wp.pl'))
    private static TaskDTO dto3 = new TaskDTO("test3", true, 10, null)
    private static List<TaskDTO> dtos = [dto1, dto2]

    @Autowired
    MockMvc mvc
    @Autowired
    TaskController taskController;
    @Autowired
    TaskService taskService;

    private ObjectMapper objectMapper = new ObjectMapper()

    def "should return list of Tasks"() {
        when: "getting values from database by using endpoind /api/tasks"
        def result = mvc.perform(get("/api/tasks")).andReturn()
        def values = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<TaskDTO>>() {
        })
        then:
        values.containsAll(dtos)
    }

    @Unroll
    def "should return specific task for task with id: #id"() {

        when:
        def result = mvc.perform(get("/api/tasks/" + id)).andReturn()
        def value = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<TaskDTO>() {})
        then:
        value == task

        when:
        taskController.findById(3);
        then:
        def exception = thrown(TaskNotFoundException)
        exception.getMessage() == "Could not find searched Task"

        where:
        id | task
        1  | dto1
        2  | dto2
    }

    def "schould add new Task to the database"() {
        given:
        def request = objectMapper.writeValueAsString(dto3)
        when:

        expect:
        mvc.perform(post('/api/tasks')
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

        when:
        def taskFromDb = taskService.findLast()
        dto3.setId(taskFromDb.getId())
        then:
        dto3 == taskService.findLast();

    }

    def "schould update Task in the database"() {
        given:
        def request = objectMapper.writeValueAsString(task)
        when:
        def result = mvc.perform(put('/api/tasks/' + id)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()

        then:
        result.getResponse().status == 200;
        taskController.findById(id) == taskFromDatabase

        where:
        id | task                                                                                             | taskFromDatabase
        1  | new TaskDTO(1, "testChange", false, 10, new UserDTO("firstName", "LastName", "email@email.com")) | new TaskDTO(1, "testChange", false, 10, new UserDTO("firstName", "LastName", "email@email.com"))
        2  | new TaskDTO(2, "testChange2", true, null, null)                                                  | new TaskDTO(2, "testChange2", true, null, null)
    }

    def "schould delete Task with id id=3"() {
        when:
        def results = mvc.perform(delete("/api/tasks/3")).andReturn()
        then:
        results.getResponse().status == 200
        when:
        taskController.findById(3)
        then:
        def exception = thrown(TaskNotFoundException)
        exception.getMessage() == "Could not find searched Task"

    }

}
