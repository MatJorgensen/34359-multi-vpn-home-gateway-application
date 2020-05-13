/*
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meowster.app.sample;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;

import java.util.Collection;

/**
 * Copyright 2020-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Skeletal ONOS UI Custom-View message handler.
 */


public class AppUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "sampleCustomDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "sampleCustomDataResponse";
    private static final String CUST_DATA_REQ = "toggleVlanRequest";

    private static final int NoVlan = 0;
    private static final String MESSAGE = "message";
    private static final String MSG_FORMAT = "VLAN %s applied for host %s";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static boolean VlanOn = false;

    public static boolean getVlan() { return VlanOn; }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleCustomDataRequestHandler()
        );
    }

    private final class SampleCustomDataRequestHandler extends RequestHandler {

        private SampleCustomDataRequestHandler() {
            super(CUST_DATA_REQ);
            log.info("Enabling custom data request: " + CUST_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            log.info("Payload: " + payload);

            // Fetch MAC address from GUI
            String inputMac = payload.get("host").textValue();
            MacAddress hostMac = MacAddress.valueOf(inputMac);

            // Fetch VLAN ID from GUI
            String inputVlanId = payload.get("vlanId").textValue();
            log.info("Parsed vlanId: " + inputVlanId);
            VlanId vlanId = VlanId.vlanId(inputVlanId);

            // Generate result message
            ObjectNode result = objectNode();
            result.put(MESSAGE, String.format(MSG_FORMAT, vlanId, hostMac));
            log.info("Result: " + result);
            sendMessage(result);

            // Update value in backend
            AppComponent.VID = vlanId;
        }
    }
}
