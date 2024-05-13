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
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.ChargingProfile;

import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.utils.mapper.ChargingProfileDetailsMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.rwth.idsg.steve.web.api.ApiControllerAdvice.ApiErrorResponse;
import de.rwth.idsg.steve.web.api.dto.ApiChargePointList;
import de.rwth.idsg.steve.web.api.dto.ApiChargingProfile;
import de.rwth.idsg.steve.web.api.dto.ApiChargingProfileAssignments;
import de.rwth.idsg.steve.web.api.dto.ApiChargingProfilesInfo;
import de.rwth.idsg.steve.web.api.dto.ApiGetCompositSchedule;
import de.rwth.idsg.steve.web.dto.ChargePointQueryForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileAssignmentQueryForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileQueryForm;
import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileFilterType;
import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.GetCompositeScheduleParams;
import de.rwth.idsg.steve.web.dto.ocpp.SetChargingProfileParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author fnkbsi
 * @since 18.10.2023
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/chargingprofiles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ChargingProfileRestController {

    @Autowired protected ChargePointHelperService chargePointHelperService;
    @Autowired private ChargePointRepository chargePointRepository;
    @Autowired private ChargingProfileRepository chargingProfileRepository;

    @Autowired
    @Qualifier("ChargePointService16_Client")
    private ChargePointService16_Client client16;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

     private String getOcppProtocol(String chargeBoxId) {
        ChargePointQueryForm form = new ChargePointQueryForm();
        form.setChargeBoxId(chargeBoxId);
        return chargePointRepository.getOverview(form).get(0).getOcppProtocol().toUpperCase();
     }

     private Integer setProfile(String chargeBoxId, SetChargingProfileParams setProfileParams) {
        String ocppProtocol = getOcppProtocol(chargeBoxId);
        Integer taskId;
        taskId = switch (ocppProtocol) {
            case "OCPP1.6J", "OCPP1.6S" -> client16.setChargingProfile(setProfileParams, "SteveWebApi");
            case "OCPP1.5J", "OCPP1.5S", "OCPP1.5" -> null;
            case "OCPP1.2" -> null;
            default -> null;
        };
        return taskId;
    }

     private Integer clearProfile(String chargeBoxId, ClearChargingProfileParams clearProfileParams) {
        String ocppProtocol = getOcppProtocol(chargeBoxId);
        Integer taskId;
        taskId = switch (ocppProtocol) {
            case "OCPP1.6J", "OCPP1.6S" -> client16.clearChargingProfile(clearProfileParams, "SteveWebApi");
            case "OCPP1.5J", "OCPP1.5S", "OCPP1.5" -> null;
            case "OCPP1.2" -> null;
            default -> null;
        };
         return taskId;
    }
    
    private Integer getCompositeSchedule(String chargeBoxId, GetCompositeScheduleParams compositeScheduleParams) {
        String ocppProtocol = getOcppProtocol(chargeBoxId);
        Integer taskId;
        taskId = switch (ocppProtocol) {
            case "OCPP1.6J", "OCPP1.6S" -> client16.getCompositeSchedule(compositeScheduleParams, "SteveWebApi");
            case "OCPP1.5J", "OCPP1.5S", "OCPP1.5" -> null;
            case "OCPP1.2" -> null;
            default -> null;
        };
         return taskId;
    }

    private ApiChargePointList getChargePoints() {
        //get ChargePoints which are compatible to ChargeProfiles (ocpp v1.6)
        List<ChargePointSelect> chargePoints = chargePointHelperService.getChargePoints(OcppVersion.V_16);
        ApiChargePointList lsCp = new ApiChargePointList();
        try {
            for (ChargePointSelect chargeBox : chargePoints) {
                List<Integer> conList = chargePointRepository.getNonZeroConnectorIds(chargeBox.getChargeBoxId());
                if (!conList.isEmpty()) {
                    lsCp.addCP(chargeBox.getChargeBoxId(), conList);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return lsCp;
    }

    private List<ChargingProfile.Overview> getProfilesOverview(ChargingProfileQueryForm queryForm) {
        return chargingProfileRepository.getOverview(queryForm);
    }

    // -------------------------------------------------------------------------
    // Http methods (GET)
    // -------------------------------------------------------------------------

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @GetMapping(value = "")
    @ResponseBody
    public ApiChargingProfilesInfo getChargingProfiles() {
        ApiChargingProfilesInfo profilesInfo= new ApiChargingProfilesInfo();
        profilesInfo.setChargePointList(getChargePoints());
        profilesInfo.setChargingProfiles(getProfilesOverview(new ChargingProfileQueryForm()));
        return profilesInfo;
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @GetMapping(value = "details")
    @ResponseBody
    public ChargingProfileForm getDetails(@Valid Integer chargingProfilePk) {
        ChargingProfile.Details details = chargingProfileRepository.getDetails(chargingProfilePk);
        return ChargingProfileDetailsMapper.mapToForm(details);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @GetMapping(value = "assignment")
    @ResponseBody
    public ApiChargingProfileAssignments getAssignments(@Valid ChargingProfileAssignmentQueryForm form) {
        ApiChargingProfileAssignments assignmentInfo = new ApiChargingProfileAssignments();
        assignmentInfo.setProfilesBasicInfo(chargingProfileRepository.getBasicInfo());
        assignmentInfo.setChargeBoxIds(chargePointRepository.getChargeBoxIds());
        assignmentInfo.setAssignments(chargingProfileRepository.getAssignments(form));
        return assignmentInfo;
    }

    // -------------------------------------------------------------------------
    // Http methods (POST)
    // the methods return the taskID, check the sucess with the TaskRestController
    // -------------------------------------------------------------------------

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @PostMapping(value = "setchargingprofile")
    @ResponseBody
    public Integer postSetChargingProfile(@Valid ApiChargingProfile params) {
        SetChargingProfileParams setProfileParams = new SetChargingProfileParams();
        setProfileParams.setChargePointSelectList(chargePointRepository.getChargePointSelect(params.getChargeBoxId()));
        setProfileParams.setConnectorId(params.getConnectorId());
        setProfileParams.setChargingProfilePk(params.getProfilePk());
        return setProfile(params.getChargeBoxId(), setProfileParams);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @PostMapping(value = "clearchargingprofile")
    @ResponseBody
    public Integer postClearChargingProfile(@Valid ApiChargingProfile params) {
        ClearChargingProfileParams clearProfileParams = new ClearChargingProfileParams();
        clearProfileParams.setChargePointSelectList(chargePointRepository.getChargePointSelect(params.getChargeBoxId()));
        if (params.getFilterType() == ClearChargingProfileFilterType.ChargingProfileId) {
            clearProfileParams.setChargingProfilePk(params.getProfilePk());
        } else {
            clearProfileParams.setConnectorId(params.getConnectorId());
            clearProfileParams.setChargingProfilePurpose(params.getChargingProfilePurpose());
            clearProfileParams.setStackLevel(params.getStackLevel());
        }
        return clearProfile(params.getChargeBoxId(), clearProfileParams);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @PostMapping(value = "getcompositeschedule")
    @ResponseBody
    public Integer postGetCompositeSchedule(@Valid ApiGetCompositSchedule params) {
        GetCompositeScheduleParams compositeScheduleParams = new GetCompositeScheduleParams();
        compositeScheduleParams.setChargePointSelectList(chargePointRepository.getChargePointSelect(params.getChargeBoxId()));
        compositeScheduleParams.setConnectorId(params.getConnectorId());
        compositeScheduleParams.setDurationInSeconds(params.getDurationInSeconds());
        compositeScheduleParams.setChargingRateUnit(params.getChargingRateUnit());
        
        return getCompositeSchedule(params.getChargeBoxId(), compositeScheduleParams);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @PostMapping(value = "add")
    @ResponseBody
    public Integer addPost(@Valid ChargingProfileForm form) {
        return chargingProfileRepository.add(form);
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @PostMapping(value = "update")
    @ResponseBody
    public ChargingProfileForm update(@Valid ChargingProfileForm form) {
        chargingProfileRepository.update(form);
        return getDetails(form.getChargingProfilePk());
    }

    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad Request", response = ApiErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ApiErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ApiErrorResponse.class)}
    )
    @PostMapping(value = "delete")
    @ResponseBody
    public ApiChargingProfilesInfo delete(@Valid Integer chargingProfilePk) {
        chargingProfileRepository.delete(chargingProfilePk);
        return getChargingProfiles();
    }
}