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
package de.rwth.idsg.steve.ocpp.task;

import de.rwth.idsg.steve.ocpp.Ocpp16AndAboveTask;
import de.rwth.idsg.steve.ocpp.OcppCallback;
import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.service.dto.EnhancedSetChargingProfileParams;
import jooq.steve.db.tables.records.ChargingProfileRecord;
import ocpp.cp._2015._10.ChargingProfile;
import ocpp.cp._2015._10.ChargingProfileKindType;
import ocpp.cp._2015._10.ChargingProfilePurposeType;
import ocpp.cp._2015._10.ChargingRateUnitType;
import ocpp.cp._2015._10.ChargingSchedule;
import ocpp.cp._2015._10.ChargingSchedulePeriod;
import ocpp.cp._2015._10.RecurrencyKindType;
import ocpp.cp._2015._10.SetChargingProfileRequest;

import jakarta.xml.ws.AsyncHandler;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;

import static java.util.Objects.isNull;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 13.03.2018
 */
public class SetChargingProfileTask extends Ocpp16AndAboveTask<EnhancedSetChargingProfileParams, String> {

    private final ChargingProfileRepository chargingProfileRepository;

    public SetChargingProfileTask(OcppVersion ocppVersion,
                                  EnhancedSetChargingProfileParams params,
                                  ChargingProfileRepository chargingProfileRepository) {
        super(ocppVersion, params);
        this.chargingProfileRepository = chargingProfileRepository;
    }


    public SetChargingProfileTask(OcppVersion ocppVersion,
                                  EnhancedSetChargingProfileParams params,
                                  ChargingProfileRepository chargingProfileRepository,
                                  String caller) {
        super(ocppVersion, params, caller);
        this.chargingProfileRepository = chargingProfileRepository;
    }

    @Override
    public OcppCallback<String> defaultCallback() {
        return new DefaultOcppCallback<String>() {
            @Override
            public void success(String chargeBoxId, String statusValue) {
                addNewResponse(chargeBoxId, statusValue);

                // if the ChargeBox accepted the Profile notice this in the DB, except it's a TxProfile
                Boolean isTxProfile =  params.getDetails().getProfile().getChargingProfilePurpose().contains("TxProfile");
                if ("Accepted".equalsIgnoreCase(statusValue) && !isTxProfile) {
                    int chargingProfilePk = params.getDetails().getProfile().getChargingProfilePk();
                    int connectorId = params.getDelegate().getConnectorId();
                    chargingProfileRepository.setProfile(chargingProfilePk, chargeBoxId, connectorId);
                }
            }
        };
    }

    @Override
    public ocpp.cp._2015._10.SetChargingProfileRequest getOcpp16Request() {
        ChargingProfileRecord profile = params.getDetails().getProfile();

        // if it's a TxProfile which misses the StartSchedule, then add the actual time as StartSchedule
        ChargingProfilePurposeType purpose = ChargingProfilePurposeType.fromValue(profile.getChargingProfilePurpose());
        if (ChargingProfilePurposeType.TX_PROFILE == purpose && isNull(profile.getStartSchedule())){
            profile.setStartSchedule(DateTime.now());
        }

        List<ChargingSchedulePeriod> schedulePeriods =
                params.getDetails().getPeriods()
                       .stream()
                       .map(k -> {
                           ChargingSchedulePeriod p = new ChargingSchedulePeriod();
                           p.setStartPeriod(k.getStartPeriodInSeconds());
                           p.setLimit(k.getPowerLimit());
                           p.setNumberPhases(k.getNumberPhases());
                           return p;
                       })
                       .collect(Collectors.toList());

        ChargingSchedule schedule = new ChargingSchedule()
                .withDuration(profile.getDurationInSeconds())
                .withStartSchedule(profile.getStartSchedule())
                .withChargingRateUnit(ChargingRateUnitType.fromValue(profile.getChargingRateUnit()))
                .withMinChargingRate(profile.getMinChargingRate())
                .withChargingSchedulePeriod(schedulePeriods);

        ChargingProfile ocppProfile = new ChargingProfile()
                .withChargingProfileId(profile.getChargingProfilePk())
                .withTransactionId(params.getDelegate().getTransactionId())
                .withStackLevel(profile.getStackLevel())
                .withChargingProfilePurpose(purpose)
                .withChargingProfileKind(ChargingProfileKindType.fromValue(profile.getChargingProfileKind()))
                .withRecurrencyKind(profile.getRecurrencyKind() == null ? null : RecurrencyKindType.fromValue(profile.getRecurrencyKind()))
                .withValidFrom(profile.getValidFrom())
                .withValidTo(profile.getValidTo())
                .withChargingSchedule(schedule);

        return new SetChargingProfileRequest()
                .withConnectorId(params.getDelegate().getConnectorId())
                .withCsChargingProfiles(ocppProfile);
    }

    @Override
    public AsyncHandler<ocpp.cp._2015._10.SetChargingProfileResponse> getOcpp16Handler(String chargeBoxId) {
        return res -> {
            try {
                success(chargeBoxId, res.get().getStatus().value());
            } catch (Exception e) {
                failed(chargeBoxId, e);
            }
        };
    }
}
