/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2025 SteVe Community Team
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

import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.TaskStore;
import de.rwth.idsg.steve.repository.TransactionRepository;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.TaskOverview;
import de.rwth.idsg.steve.repository.dto.Transaction;
import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.service.ChargePointServiceClient;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.when;
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
public class RemoteStartStopRestControllerTest extends AbstractControllerTest {

    @Mock
    private ChargePointHelperService chargePointHelperService;
    @Mock
    private ChargePointRepository chargePointRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TaskStore taskStore;

    @Mock
    private ChargePointServiceClient client16;

    @InjectMocks
    @Resource
    private RemoteStartStopRestController remoteStartStopRestController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(remoteStartStopRestController)
            .setControllerAdvice(new ApiControllerAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .alwaysExpect(content().contentType("application/json"))
            .build();
    }

    @Test
    @DisplayName("Test with empty results, expected 200")
    public void test1() throws Exception {
        // given
        List<ChargePointSelect> chargePoints = Collections.emptyList();

        // when
        when(chargePointHelperService.getChargePoints(any())).thenReturn(chargePoints);

        // then
        mockMvc.perform(get("/api/v1/remote"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargePointList", hasSize(0)));
    }

    @Test
    @DisplayName("Test with results, expected 200")
    public void test2() throws Exception {
        // given
        List<ChargePointSelect> results_v12 = new ArrayList();
        List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppProtocol.V_12_SOAP, "testBoxId_v12");
        ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppProtocol.V_15_SOAP, "testBoxId_v15");
        results_v12.add(chargePointSelect_v12);
        results_v15.add(chargePointSelect_v15s);
        results_v16.add(chargePointSelect_v16j);

        List<Integer> conList = Arrays.asList(1,2,3);

        // when
        when(chargePointRepository.getNonZeroConnectorIds(any())).thenReturn(conList);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_12)).thenReturn(results_v12);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_15)).thenReturn(results_v15);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_16)).thenReturn(results_v16);

        // then
        mockMvc.perform(get("/api/v1/remote")
                .param("chargeBoxId", "testBoxId")
                .param("status","Status")
                .param("strategy", "PreferZero"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargePointList", hasSize(3)));
    }

    @Test
    @DisplayName("Test get start path with results, expected 200")
    public void test3() throws Exception {
       // given
        List<ChargePointSelect> results_v12 = new ArrayList();
        List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppProtocol.V_12_SOAP, "testBoxId_v12");
        ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppProtocol.V_15_SOAP, "testBoxId_v15s");
        results_v12.add(chargePointSelect_v12);
        results_v15.add(chargePointSelect_v15s);
        results_v16.add(chargePointSelect_v16j);

        List<Integer> conList = Arrays.asList(1,2,3);

        // when
        when(chargePointRepository.getNonZeroConnectorIds(any())).thenReturn(conList);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_12)).thenReturn(results_v12);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_15)).thenReturn(results_v15);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_16)).thenReturn(results_v16);

        // then
        mockMvc.perform(get("/api/v1/remote/start"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargePointList", hasSize(3)))
            .andExpect(jsonPath("$.chargePointList[0].chargeBoxId").value("testBoxId_v12"))
            .andExpect(jsonPath("$.chargePointList[1].chargeBoxId").value("testBoxId_v15s"))
            .andExpect(jsonPath("$.chargePointList[2].chargeBoxId").value("testBoxId_v16j"));
    }

    @Test
    @DisplayName("Test get stop path with results, expected 200")
    public void test4() throws Exception {
       // given
        List<ChargePointSelect> results_v12 = new ArrayList();
        List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppProtocol.V_12_SOAP, "testBoxId_v12");
        results_v12.add(chargePointSelect_v12);
        results_v16.add(chargePointSelect_v16j);

        List<Integer> conList = Arrays.asList(1,2,3);

        // when
        when(chargePointRepository.getNonZeroConnectorIds(any())).thenReturn(conList);
        // getChargePoints with any an ony one results ArrayList produces not expectet responses
        when(chargePointHelperService.getChargePoints(OcppVersion.V_12)).thenReturn(results_v12);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_15)).thenReturn(results_v15);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_16)).thenReturn(results_v16);

        // then
        mockMvc.perform(get("/api/v1/remote/stop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargePointList", hasSize(2)))
            .andExpect(jsonPath("$.chargePointList[0].chargeBoxId").value("testBoxId_v12"))
            //.andExpect(jsonPath("$.chargePointList[1].chargeBoxId").value("testBoxId_v15s"))
            .andExpect(jsonPath("$.chargePointList[1].chargeBoxId").value("testBoxId_v16j"));
    }

    @Test
    @DisplayName("Test get unlock path with results, expected 200")
    public void test5() throws Exception {
       // given
        List<ChargePointSelect> results_v12 = new ArrayList();
        List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppProtocol.V_12_SOAP, "testBoxId_v12");
        ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppProtocol.V_15_SOAP, "testBoxId_v15s");
        results_v12.add(chargePointSelect_v12);
        results_v15.add(chargePointSelect_v15s);
        results_v16.add(chargePointSelect_v16j);

        //results.add(chargePointSelect1);

        List<Integer> conList = Arrays.asList(1,2,3);

        // when
        when(chargePointRepository.getNonZeroConnectorIds(any())).thenReturn(conList);
        // getChargePoints with any an ony one results ArrayList produces not expectet responses
        when(chargePointHelperService.getChargePoints(OcppVersion.V_12)).thenReturn(results_v12);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_15)).thenReturn(results_v15);
        when(chargePointHelperService.getChargePoints(OcppVersion.V_16)).thenReturn(results_v16);

        // then
        mockMvc.perform(get("/api/v1/remote/unlock"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargePointList", hasSize(3)))
            .andExpect(jsonPath("$.chargePointList[0].chargeBoxId").value("testBoxId_v12"))
            .andExpect(jsonPath("$.chargePointList[1].chargeBoxId").value("testBoxId_v15s"))
            .andExpect(jsonPath("$.chargePointList[2].chargeBoxId").value("testBoxId_v16j"));
    }

    @Test
    @DisplayName("Test post start path with results, expected 200")
    public void test6() throws Exception {
       // given
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        results_v16.add(chargePointSelect_v16j);

        List<TaskOverview> taskList = new ArrayList(); //taskStore.getOverview();
        Integer valNull = null;

        // when
        when(chargePointRepository.getChargePointSelect(anyString())).thenReturn(results_v16);
        when(client16.remoteStartTransaction(any(), anyString())).thenReturn(16);
        when(taskStore.getOverview()).thenReturn(taskList);
        when(transactionRepository.getActiveTransactionId(any(),any())).thenReturn(valNull);

        // then
        mockMvc.perform(post("/api/v1/remote/start")
                .param("chargeBoxId", "testBoxId") //ApiChargePointStart
                .param("connectorId", "1")
                .param("ocppTag", "12345678"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(16));
    }

    @Test
    @DisplayName("Test post stop path with results, expected 200")
    public void test7() throws Exception {
       // given
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        results_v16.add(chargePointSelect_v16j);

        List<TaskOverview> taskList = new ArrayList(); //taskStore.getOverview();
        Integer valNull = null;

        Transaction transaction = Transaction.builder()
                .id(16)
                .chargeBoxId("testBoxId_v16j")
                .chargeBoxPk(1)
                .connectorId(1)
                .ocppIdTag("12345678")
                .ocppTagPk(1)
                .startTimestamp(DateTime.now())
                .startValue("1234")
                .build();

        // when
        when(transactionRepository.getTransaction(anyInt())).thenReturn(transaction);
        when(transactionRepository.getActiveTransactionId(anyString(),anyInt())).thenReturn(16);
        when(chargePointRepository.getChargePointSelect(anyString())).thenReturn(results_v16);
        when(client16.remoteStopTransaction(any(), anyString())).thenReturn(16);
        when(taskStore.getOverview()).thenReturn(taskList);

        // then
        mockMvc.perform(post("/api/v1/remote/stop")
                .param("chargeBoxId", "testBoxId") //ApiChargePointStart
                .param("connectorId", "1")
                .param("ocppTag", "12345678"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(16));
    }


    @Test
    @DisplayName("Test post unlock path with results, expected 200")
    public void test8() throws Exception {
       // given
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppProtocol.V_16_JSON, "testBoxId_v16j");
        results_v16.add(chargePointSelect_v16j);

        List<TaskOverview> taskList = new ArrayList(); //taskStore.getOverview();
        Integer valNull = null;

        when(transactionRepository.getActiveTransactionId(any(),any())).thenReturn(valNull);
        when(chargePointRepository.getChargePointSelect(anyString())).thenReturn(results_v16);
        when(client16.unlockConnector(any(), anyString())).thenReturn(16);
        when(taskStore.getOverview()).thenReturn(taskList);


        // then
        mockMvc.perform(post("/api/v1/remote/unlock")
                .param("chargeBoxId", "testBoxId") //ApiChargePointStart
                .param("connectorId", "1")
                .param("ocppTag", "12345678"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(16));
    }

    private static ResultMatcher[] errorJsonMatchers() {
        return new ResultMatcher[] {
            jsonPath("$.timestamp").exists(),
            jsonPath("$.status").exists(),
            jsonPath("$.error").exists(),
            jsonPath("$.message").exists(),
            jsonPath("$.path").exists()
        };
    }
}
