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
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.google.common.collect.ImmutableSet;
import org.onosproject.net.DeviceId;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;

import java.util.Collection;
import java.util.Map;

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
            AppComponent.switchTable.forEach((k,v) -> {
                v.forEach((k1,v1) -> {
                    log.info("switch: " + k.toString() + " host: " + k1.toString() + " outport: " + v1.getValue0().toString() + " vlan ID: " + v1.getValue1().toString());
                    if (k1.toString().equals(hostMac.toString())){ //if mac in hosttable is the one we are trying to set
                        if (v1.getValue1().equals(vlanId)){ // if the macs attached VLAN is equal to the one we are trying to set
                            v1.setAt1(VlanId.vlanId(VlanId.UNTAGGED));
                        } else if (v1.getValue1().equals(VlanId.UNTAGGED)){ // if the VLAN TAG is EMPTY then we set it
                            v1.setAt1(vlanId);
                        } else {

                            // we should create a new entry, so each host can have several VLANs
                        }
                    }
                    log.info("MODIFIED: switch: " + k.toString() + " host: " + k1.toString() + " outport: " + v1.getValue0().toString() + " vlan ID: " + v1.getValue1().toString());

                });
            });
            //log.info(AppComponent.switchTable.elements());
        }
    }
}
