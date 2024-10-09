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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ocpp.cp._2015._10.ChargingRateUnitType;

/**
 * @author fnkbsi
 * @since 23.01.2024
 */

@Getter
@Setter
@RequiredArgsConstructor
public class ApiGetCompositSchedule {

    @Schema(description = "Charge Box ID")
    String chargeBoxId;

    @Schema(description = "Connector ID")
    @Min(value = 0, message = "Connector ID must be at least {value}")
    private Integer connectorId;

    @Schema(description = "Duration in seconds")
    @NotNull
    @Positive
    private Integer durationInSeconds;

    @Schema(description = "Charging rate unit")
    private ChargingRateUnitType chargingRateUnit;
}
