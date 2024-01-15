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
package de.rwth.idsg.steve.web.api.dto;

import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileFilterType;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ocpp.cp._2015._10.ChargingProfilePurposeType;

/**
 * @author fnkbsi
 * @since 03.11.2023
 */

@Getter
@Setter
@RequiredArgsConstructor
public class ApiChargingProfile {

    @ApiModelProperty(value = "Charge Box ID")
    String chargeBoxId;

    @ApiModelProperty(value = "Filter Type is required")
    @NotNull(message = "Filter Type is required")
    private ClearChargingProfileFilterType filterType; // = ClearChargingProfileFilterType.ChargingProfileId;

    @ApiModelProperty(value = "Profile ID")
    Integer profilePk;

    @ApiModelProperty(value = "Connector ID")
    @Min(value = 0, message = "Connector ID must be at least {value}")
    private Integer connectorId;

    @ApiModelProperty(value = "Charging Profil Purpose")
    private ChargingProfilePurposeType chargingProfilePurpose;

    @ApiModelProperty(value = "Stack Level")
    private Integer stackLevel;
}
