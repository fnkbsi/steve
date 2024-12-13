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
import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.dto.ConnectorStatus;
import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.web.dto.OcppJsonStatus;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 17.09.2022
 */
@ExtendWith(MockitoExtension.class)
public class ConnectorRestControllerTest extends AbstractControllerTest {

//    @Mock
//    private TransactionRepository transactionRepository;
    @Mock
    private ChargePointRepository chargePointRepository;
    @Mock
    private ChargePointHelperService chargePointHelperService;

    @InjectMocks
    @Resource
    private ConnectorRestController connectorRestController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(connectorRestController)
            .setControllerAdvice(new ApiControllerAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .alwaysExpect(content().contentType("application/json"))
            .build();
    }

    @Test
    @DisplayName("Test with empty results, expected 200")
    public void test1() throws Exception {
        // given
        List<String> results = Collections.emptyList();
        //List<String> results = chargePointRepository.getChargeBoxIds();
        //ConnectorStatusForm queryParams = new ConnectorStatusForm();
        List<ConnectorStatus> latestList = new ArrayList();
        ConnectorStatus conStat = ConnectorStatus.builder()
                                    .chargeBoxId("testBoxId")
                                    .connectorId(1)
                                    .status("SuspendedEV")
                                    .statusTimestamp(DateTime.now())
                                    .errorCode("")
                                    .ocppProtocol(OcppProtocol.V_16_JSON)
                                    .timeStamp(DateTime.now().toString("yyyy-mm-dd HH:MM:SS"))
                                .build();
        latestList.add(conStat);

        //chargePointHelperService.getChargePointConnectorStatus(queryParams);

        // when
        when(chargePointRepository.getChargeBoxIds()).thenReturn(results);
        when(chargePointHelperService.getChargePointConnectorStatus(any())).thenReturn(latestList);

        // then
        mockMvc.perform(get("/api/v1/connectors")
                .param("chargeBoxId", "testBoxId")
                .param("status","Status")
                .param("strategy", "PreferZero"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargeBoxList", hasSize(0)));
    }

    @Test
    @DisplayName("Test with results, expected 200")
    public void test2() throws Exception {
        // given
        List<String> results = new ArrayList();
        //String testId = "testBoxId";
        results.add("testBoxId");
        //List<String> results = chargePointRepository.getChargeBoxIds();
        //ConnectorStatusForm queryParams = new ConnectorStatusForm();
        List<ConnectorStatus> latestList = new ArrayList();
        ConnectorStatus conStat = ConnectorStatus.builder()
                                    .chargeBoxId("testBoxId")
                                    .connectorId(1)
                                    .status("SuspendedEV")
                                    .statusTimestamp(DateTime.now())
                                    .errorCode("")
                                    .ocppProtocol(OcppProtocol.V_16_JSON)
                                    .timeStamp(DateTime.now().toString("yyyy-mm-dd HH:MM:SS"))
                                .build();
        latestList.add(conStat);

        //chargePointHelperService.getChargePointConnectorStatus(queryParams);

        // when
        when(chargePointRepository.getChargeBoxIds()).thenReturn(results);
        when(chargePointHelperService.getChargePointConnectorStatus(any())).thenReturn(latestList);

        // then
        mockMvc.perform(get("/api/v1/connectors")
                .param("chargeBoxId", "testBoxId")
                .param("status","Status")
                .param("strategy", "PreferZero"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chargeBoxList", hasSize(1)));
    }

    @Test
    @DisplayName("Test OCPP_JSON_STATUS path with results, expected 200")
    public void test3() throws Exception {
        // given
        List<OcppJsonStatus> results = new ArrayList();
        OcppJsonStatus ocppStat = OcppJsonStatus.builder().chargeBoxId("testBoxId")
                .connectedSince(DateTime.now().toString("yyyy-mm-dd HH:MM:SS"))
                .connectedSinceDT(DateTime.now())
                .connectionDuration("1")
                .version(OcppVersion.V_16)
                .build();
        results.add(ocppStat);

        // when
        when(chargePointHelperService.getOcppJsonStatus()).thenReturn(results);

        // then
        mockMvc.perform(get("/api/v1/connectors/OCPP_JSON_STATUS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].chargeBoxId").value("testBoxId"));
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
