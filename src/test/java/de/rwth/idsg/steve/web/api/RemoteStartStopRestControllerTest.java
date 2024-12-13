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

import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.ocpp.OcppTransport;
import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.TaskStore;
import de.rwth.idsg.steve.repository.TransactionRepository;
import de.rwth.idsg.steve.repository.dto.ChargePoint;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.ConnectorStatus;
import de.rwth.idsg.steve.repository.dto.TaskOverview;
import de.rwth.idsg.steve.repository.dto.Transaction;
import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.service.ChargePointService12_Client;
import de.rwth.idsg.steve.service.ChargePointService15_Client;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.api.dto.ApiChargePointList;
import de.rwth.idsg.steve.web.dto.OcppJsonStatus;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStartTransactionParams;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private ChargePointService12_Client client12;

    @Mock
    private ChargePointService15_Client client15;

    @Mock
    private ChargePointService16_Client client16;

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
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15");
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
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15s");
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
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        //ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15s");
        results_v12.add(chargePointSelect_v12);
        //results_v15.add(chargePointSelect_v15s);
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
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15s");
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
        //List<ChargePointSelect> results_v12 = new ArrayList();
        //List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        //ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        //ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15s");
        //results_v12.add(chargePointSelect_v12);
        //results_v15.add(chargePointSelect_v15s);
        results_v16.add(chargePointSelect_v16j);

        List<ChargePoint.Overview> cpOverviewList = new ArrayList();
        ChargePoint.Overview overview = ChargePoint.Overview.builder()
                .chargeBoxId("testBoxId")
                .chargeBoxPk(1)
                .description("testdesc")
                .lastHeartbeatTimestamp(DateTime.now().toString("YYYY-MM-DD hh:mm:ss"))
                .lastHeartbeatTimestampDT(DateTime.now())
                .ocppProtocol("ocpp1.6j")
                .build();
        cpOverviewList.add(overview);
        List<TaskOverview> taskList = new ArrayList(); //taskStore.getOverview();
        Integer valNull = null;

        // when
        //when(transactionRepository.getActiveTransactionId(anyString(),anyInt())).thenReturn(1);
        when(chargePointRepository.getChargePointSelect(anyString())).thenReturn(results_v16);
        when(client16.remoteStartTransaction(any(), anyString())).thenReturn(16);
        //when(client15.remoteStartTransaction(any(), anyString())).thenReturn(15);
        //when(client12.remoteStartTransaction(any(), anyString())).thenReturn(12);
        when(chargePointRepository.getOverview(any())).thenReturn(cpOverviewList);
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
        //List<ChargePointSelect> results_v12 = new ArrayList();
        //List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        //ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        //ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15s");
        //results_v12.add(chargePointSelect_v12);
        //results_v15.add(chargePointSelect_v15s);
        results_v16.add(chargePointSelect_v16j);

        List<ChargePoint.Overview> cpOverviewList = new ArrayList();
        ChargePoint.Overview overview = ChargePoint.Overview.builder()
                .chargeBoxId("testBoxId")
                .chargeBoxPk(1)
                .description("testdesc")
                .lastHeartbeatTimestamp(DateTime.now().toString("YYYY-MM-DD hh:mm:ss"))
                .lastHeartbeatTimestampDT(DateTime.now())
                .ocppProtocol("ocpp1.6j")
                .build();
        cpOverviewList.add(overview);
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
        //when(client16.remoteStartTransaction(any(), anyString())).thenReturn(16);
        when(client16.remoteStopTransaction(any(), anyString())).thenReturn(16);
        //when(client15.remoteStartTransaction(any(), anyString())).thenReturn(15);
        //when(client12.remoteStartTransaction(any(), anyString())).thenReturn(12);
        when(chargePointRepository.getOverview(any())).thenReturn(cpOverviewList);
        when(taskStore.getOverview()).thenReturn(taskList);
        //when(transactionRepository.getActiveTransactionId(any(),any())).thenReturn(valNull);

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
        //List<ChargePointSelect> results_v12 = new ArrayList();
        //List<ChargePointSelect> results_v15 = new ArrayList();
        List<ChargePointSelect> results_v16 = new ArrayList();
        ChargePointSelect chargePointSelect_v16j = new ChargePointSelect(OcppTransport.JSON, "testBoxId_v16j");
        //ChargePointSelect chargePointSelect_v12 = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v12");
        //ChargePointSelect chargePointSelect_v15s = new ChargePointSelect(OcppTransport.SOAP, "testBoxId_v15s");
        //results_v12.add(chargePointSelect_v12);
        //results_v15.add(chargePointSelect_v15s);
        results_v16.add(chargePointSelect_v16j);

        List<ChargePoint.Overview> cpOverviewList = new ArrayList();
        ChargePoint.Overview overview = ChargePoint.Overview.builder()
                .chargeBoxId("testBoxId")
                .chargeBoxPk(1)
                .description("testdesc")
                .lastHeartbeatTimestamp(DateTime.now().toString("YYYY-MM-DD hh:mm:ss"))
                .lastHeartbeatTimestampDT(DateTime.now())
                .ocppProtocol("ocpp1.6j")
                .build();
        cpOverviewList.add(overview);
        List<TaskOverview> taskList = new ArrayList(); //taskStore.getOverview();
        Integer valNull = null;

//        Transaction transaction = Transaction.builder()
//                .id(16)
//                .chargeBoxId("testBoxId_v16j")
//                .chargeBoxPk(1)
//                .connectorId(1)
//                .ocppIdTag("12345678")
//                .ocppTagPk(1)
//                .startTimestamp(DateTime.now())
//                .startValue("1234")
//                .build();

        // when
        //when(transactionRepository.getTransaction(anyInt())).thenReturn(transaction);
        //when(transactionRepository.getActiveTransactionId(anyString(),anyInt())).thenReturn(16);
        when(transactionRepository.getActiveTransactionId(any(),any())).thenReturn(valNull);
        when(chargePointRepository.getChargePointSelect(anyString())).thenReturn(results_v16);
        //when(client16.remoteStartTransaction(any(), anyString())).thenReturn(16);
        when(client16.unlockConnector(any(), anyString())).thenReturn(16);
        //when(client15.unlockConnector(any(), anyString())).thenReturn(15);
        //when(client12.unlockConnector(any(), anyString())).thenReturn(12);
        when(chargePointRepository.getOverview(any())).thenReturn(cpOverviewList);
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
