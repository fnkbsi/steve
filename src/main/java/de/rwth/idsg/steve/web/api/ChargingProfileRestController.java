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

import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.ChargingProfile;

import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.service.ChargePointServiceClient;
import de.rwth.idsg.steve.utils.mapper.ChargingProfileDetailsMapper;

import org.springframework.beans.factory.annotation.Autowired;

import de.rwth.idsg.steve.web.api.ApiControllerAdvice.ApiErrorResponse;
import de.rwth.idsg.steve.web.api.dto.ApiChargePointList;
import de.rwth.idsg.steve.web.api.dto.ApiChargingProfile;
import de.rwth.idsg.steve.web.api.dto.ApiChargingProfileAssignments;
import de.rwth.idsg.steve.web.api.dto.ApiChargingProfilesInfo;
import de.rwth.idsg.steve.web.api.dto.ApiGetCompositSchedule;

import de.rwth.idsg.steve.web.dto.ChargingProfileAssignmentQueryForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import de.rwth.idsg.steve.web.dto.ChargingProfileQueryForm;
import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileFilterType;
import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.GetCompositeScheduleParams;
import de.rwth.idsg.steve.web.dto.ocpp.SetChargingProfileParams;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
    private ChargePointServiceClient client;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))})}
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
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @GetMapping(value = "details")
    @ResponseBody
    public ChargingProfileForm getDetails(@Valid Integer chargingProfilePk) {
        ChargingProfile.Details details = chargingProfileRepository.getDetails(chargingProfilePk);
        return ChargingProfileDetailsMapper.mapToForm(details);
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @GetMapping(value = "assignment")
    @ResponseBody
    public ApiChargingProfileAssignments getAssignments(@RequestBody @Valid ChargingProfileAssignmentQueryForm form) {
        ApiChargingProfileAssignments assignmentInfo = new ApiChargingProfileAssignments();
        assignmentInfo.setProfilesBasicInfo(chargingProfileRepository.getBasicInfo());
        assignmentInfo.setChargeBoxIds(chargePointRepository.getChargeBoxIds());
        assignmentInfo.setAssignments(chargingProfileRepository.getAssignments(form));
        return assignmentInfo;
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @GetMapping(value = "compositeschedule")
    @ResponseBody
    public Integer getCompositeSchedule(@RequestBody @Valid ApiGetCompositSchedule params) {
        GetCompositeScheduleParams compositeScheduleParams = new GetCompositeScheduleParams();
        compositeScheduleParams.setChargePointSelectList(chargePointRepository.getChargePointSelect(params.getChargeBoxId()));
        compositeScheduleParams.setConnectorId(params.getConnectorId());
        compositeScheduleParams.setDurationInSeconds(params.getDurationInSeconds());
        compositeScheduleParams.setChargingRateUnit(params.getChargingRateUnit());

        return client.getCompositeSchedule(compositeScheduleParams, "SteveWebApi");
    }

    // -------------------------------------------------------------------------
    // Http methods (POST)
    // the methods return the taskID, check the sucess with the TaskRestController
    // -------------------------------------------------------------------------

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "422", description = "Unprocessable Entity",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Not Found",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @PostMapping(value = "setchargingprofile")
    @ResponseBody
    public Integer postSetChargingProfile(@RequestBody @Valid ApiChargingProfile params) {

        // set charging profile supports only set by Profile ID
        if (!params.getFilterType().equals(ClearChargingProfileFilterType.ChargingProfileId)) {
            return -1;
        }

        SetChargingProfileParams setProfileParams = new SetChargingProfileParams();
        setProfileParams.setChargePointSelectList(chargePointRepository.getChargePointSelect(params.getChargeBoxId()));
        setProfileParams.setConnectorId(params.getConnectorId());
        setProfileParams.setChargingProfilePk(params.getProfilePk());

        return client.setChargingProfile(setProfileParams, "SteveWebApi");
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @PostMapping(value = "clearchargingprofile")
    @ResponseBody
    public Integer postClearChargingProfile(@RequestBody @Valid ApiChargingProfile params) {
        ClearChargingProfileParams clearProfileParams = new ClearChargingProfileParams();
        clearProfileParams.setChargePointSelectList(chargePointRepository.getChargePointSelect(params.getChargeBoxId()));
        if (params.getFilterType() == ClearChargingProfileFilterType.ChargingProfileId) {
            clearProfileParams.setChargingProfilePk(params.getProfilePk());
        } else {
            clearProfileParams.setConnectorId(params.getConnectorId());
            clearProfileParams.setChargingProfilePurpose(params.getChargingProfilePurpose());
            clearProfileParams.setStackLevel(params.getStackLevel());
        }
        return client.clearChargingProfile(clearProfileParams, "SteveWebApi");
    }

    // -------------------------------------------------------------------------
    // Http methods (POST)
    // the methods return database information
    // -------------------------------------------------------------------------

    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @PostMapping(value = "add")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Integer addPost(@RequestBody @Valid ChargingProfileForm form) {
        return chargingProfileRepository.add(form);   // return the profilePk of the added profile
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @PostMapping(value = "update")
    @ResponseBody
    public ChargingProfileForm update(@RequestBody @Valid ChargingProfileForm form) {
        chargingProfileRepository.update(form);
        return getDetails(form.getChargingProfilePk()); // returns the updated profile details
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                     content = {@Content(mediaType = "application/json",
                     schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @PostMapping(value = "delete")
    @ResponseBody
    public ApiChargingProfilesInfo delete(@Valid Integer chargingProfilePk) {
        chargingProfileRepository.delete(chargingProfilePk);
        return getChargingProfiles(); // return the list of the remaining profiles
    }
}
