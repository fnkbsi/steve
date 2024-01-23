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
package de.rwth.idsg.steve.web.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ocpp.cp._2015._10.ChargingProfileKindType;
import ocpp.cp._2015._10.ChargingProfilePurposeType;
import ocpp.cp._2015._10.ChargingRateUnitType;
import ocpp.cp._2015._10.RecurrencyKindType;
import org.joda.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 12.11.2018
 */

@Getter
@Setter
@ToString
public class ChargingProfileForm {

    // Internal database id
    @ApiModelProperty(value = "Charging Profile Pk")
    private Integer chargingProfilePk;

    @ApiModelProperty(value = "Charging Profile description")
    private String description;
    @ApiModelProperty(value = "Charging Profile note")
    private String note;

    @ApiModelProperty(value = "Charging Profile Stack Level")
    @NotNull(message = "Stack Level has to be set")
    @PositiveOrZero(message = "Stack Level has to be a positive number or 0")
    private Integer stackLevel;

    @ApiModelProperty(value = "Charging Profile Purpose")
    @NotNull(message = "Charging Profile Purpose has to be set")
    private ChargingProfilePurposeType chargingProfilePurpose;

    @ApiModelProperty(value = "Charging Profile Kind")
    @NotNull(message = "Charging Profile Kind has to be set")
    private ChargingProfileKindType chargingProfileKind;

    @ApiModelProperty(value = "Recurrency Kind")
    private RecurrencyKindType recurrencyKind;

    @ApiModelProperty(value = "Starttime of the Profile")
    private LocalDateTime validFrom;

    @ApiModelProperty(value = "Endtime of the Profile")
    @Future(message = "Valid To must be in future")
    private LocalDateTime validTo;

    @ApiModelProperty(value = "Profile duration")
    @Positive(message = "Duration has to be a positive number")
    private Integer durationInSeconds;

    @ApiModelProperty(value = "Schedule Start")
    private LocalDateTime startSchedule;

    @ApiModelProperty(value = "Charging Rate Unit")
    @NotNull(message = "Charging Rate Unit has to be set")
    private ChargingRateUnitType chargingRateUnit;

    @ApiModelProperty(value = "Minimum Charging Rate")
    private BigDecimal minChargingRate;

    @ApiModelProperty(value = "Schedule Periods")
    @NotEmpty(message = "Schedule Periods cannot be empty")
    @Valid
    private Map<String, SchedulePeriod> schedulePeriodMap;

    @AssertTrue(message = "Valid To must be after Valid From")
    public boolean isFromToValid() {
        return !(validFrom != null && validTo != null) || validTo.isAfter(validFrom);
    }

    @AssertTrue(message = "Start schedule must be between Valid To and From")
    public boolean isStartScheduleValid() {
        if (validFrom != null && startSchedule != null && !startSchedule.isAfter(validFrom)) {
            return false;
        }

        if (validTo != null && startSchedule != null && !startSchedule.isBefore(validTo)) {
            return false;
        }

        return true;
    }

    @AssertTrue(message = "Valid From/To should not be used with the profile purpose 'TxProfile'")
    public boolean isFromToAndProfileSettingCorrect() {
        boolean isTxProfile = (chargingProfilePurpose == ChargingProfilePurposeType.TX_PROFILE);

        if (validFrom != null && isTxProfile) {
            return false;
        }

        if (validTo != null && isTxProfile) {
            return false;
        }

        return true;
    }

    @Getter
    @Setter
    @ToString
    public static class SchedulePeriod {

        @ApiModelProperty(value = "Schedule period default number of phases")
        private static final int defaultNumberPhases = 3;

        @ApiModelProperty(value = "Schedule Period Start")
        @NotNull(message = "Schedule period: Start Period has to be set")
        private Integer startPeriodInSeconds; // from the startSchedule

        @ApiModelProperty(value = "Schedule period power limit")
        @NotNull(message = "Schedule period: Power Limit has to be set")
        private BigDecimal powerLimit;

        @ApiModelProperty(value = "Schedule period number phases")
        private Integer numberPhases;

        public Integer getNumberPhases() {
            return Objects.requireNonNullElse(numberPhases, defaultNumberPhases);
        }

        public void setNumberPhases(Integer numberPhases) {
            this.numberPhases = Objects.requireNonNullElse(numberPhases, defaultNumberPhases);
        }
    }
}
