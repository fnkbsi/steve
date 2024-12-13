/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2024 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.ocpp.TaskOrigin;
import de.rwth.idsg.steve.ocpp.CommunicationTask;
import de.rwth.idsg.steve.ocpp.task.RemoteStartTransactionTask;
import de.rwth.idsg.steve.repository.TaskStore;
import de.rwth.idsg.steve.repository.dto.TaskOverview;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStartTransactionParams;
//import de.rwth.idsg.steve.web.api.dto.ApiTaskList;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.List;


import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 17.09.2022
 */
@ExtendWith(MockitoExtension.class)
public class TaskRestControllerTest extends AbstractControllerTest {

    @Mock
    private TaskStore taskStore;

    @InjectMocks
    @Resource
    private TaskRestController taskRestController;


    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskRestController)
            .setControllerAdvice(new ApiControllerAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .alwaysExpect(content().contentType("application/json"))
            .build();
    }

    @Test
    @DisplayName("Test taskoverview with one task, expected 200")
    public void test1() throws Exception {
        // given
        List<TaskOverview> results = new ArrayList<>();
        results.add(TaskOverview.builder()
                                    .taskId(1)
                                    .origin(TaskOrigin.EXTERNAL)
                                    .start(DateTime.now())
                                    .end(null)
                                    .responseCount(1)
                                    .requestCount(1)
                                    .build());


        // when
        when(taskStore.getOverview()).thenReturn(results);

        // then
        mockMvc.perform(get("/api/v1/tasks/taskoverview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks", hasSize(1)));
    }

    @Test
    @DisplayName("Test clearfinishedtasks with empty results, expected 200")
    public void test2() throws Exception {
        // given
        List<TaskOverview> results = new ArrayList<>();

        // when
        when(taskStore.getOverview()).thenReturn(results);

        // then
        mockMvc.perform(post("/api/v1/tasks/clearfinishedtasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks", hasSize(0)));
    }


    @Test
    @DisplayName("Test task with empty results, expected 200")
    public void test3() throws Exception {
        // given

        //RemoteStartTransactionTask(OcppVersion ocppVersion, RemoteStartTransactionParams params, String caller)
        //ChargePointSelect chargePointSelect = new ChargePointSelect(OcppTransport.JSON, "chargeBoxId", "127.0.0.3");

        RemoteStartTransactionParams params = new RemoteStartTransactionParams();

        CommunicationTask results = new RemoteStartTransactionTask(OcppVersion.V_16, params, "Test3");

        // when
        when(taskStore.get(anyInt())).thenReturn(results);

        // then
        mockMvc.perform(get("/api/v1/tasks/task")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.caller").value("Test3"));
    }

//    private static ResultMatcher[] errorJsonMatchers() {
//        return new ResultMatcher[] {
//            jsonPath("$.timestamp").exists(),
//            jsonPath("$.status").exists(),
//            jsonPath("$.error").exists(),
//            jsonPath("$.message").exists(),
//            jsonPath("$.path").exists()
//        };
//    }
}
