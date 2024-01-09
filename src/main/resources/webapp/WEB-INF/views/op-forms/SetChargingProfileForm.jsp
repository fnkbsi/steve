<%--

    SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
    Copyright (C) 2013-2024 SteVe Community Team
    All Rights Reserved.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<form:form action="${ctxPath}/manager/operations/${opVersion}/SetChargingProfile" modelAttribute="params">
    <section><span>Charge Points with OCPP ${opVersion}</span></section>
    <%@ include file="../00-cp-multiple.jsp" %>
    <section><span>Parameters</span></section>
    <table class="userInput">
        <tr>
            <td>Charging Profile ID:</td>
            <td><form:select path="chargingProfilePk" title="Charging Profile Id">
                <form:options items="${profileList}" itemLabel="itemDescription" itemValue="chargingProfilePk"/>
            </form:select>
            </td>
        </tr>
        <tr>
            <td>Connector ID (integer):</td>
            <td><form:input path="connectorId" placeholder="0 = charge point as a whole" title="Connector Id"/></td>
        </tr>
        <tr>
            <td>Transaction ID (integer):</td>
            <td><form:input path="transactionId" placeholder="only necessary for TxProfile" title="Transaction Id"/>
                <a class="tooltip" href="#">
                    <img src="${ctxPath}/static/images/info.png" style="vertical-align:middle" alt="info_image">
                    <span>Only relevant for TxProfiles.
                        If the field is left blank, Steve will try to add the transaction ID automatically!
                    </span></a>
            </td>
        </tr>
        <tr><td></td><td><div class="submit-button"><input type="submit" value="Perform"></div></td><td></td></tr>
    </table>
</form:form>