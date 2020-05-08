/*
 * Copyright 2020-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meowster.app.sample;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {
    private ApplicationId appId;
    private static String H1_MAC = "00:00:00:00:00:01";
    private static String H2_MAC = "00:00:00:00:00:02";
    private static String S1_ID = "of:0000000000000001";
    private static String S2_ID = "of:0000000000000002";
    private static long H1_S1_PORT = 1;
    private static long H2_S2_PORT = 1;
    private static long S1_S2_PORT = 2;
    private static long S2_S1_PORT = 2;
    //static boolean VlanOn = false;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.student.multi-vpn-app");
        TrafficSelector packetSelector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4).build();
        packetService.requestPackets(packetSelector, PacketPriority.REACTIVE, appId);
        packetService.addProcessor(processor, PacketProcessor.director(2));
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        processor = null;
        log.info("Stopped");
    }

    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            //Discard if packet is null.
            if (ethPkt == null) {
                log.info("Discarding null packet");
                return;
            }

            if (ethPkt.getEtherType() != Ethernet.TYPE_IPV4) {
                return;
            }

            log.info("Processing packet request");

            DeviceId deviceId = pkt.receivedFrom().deviceId();
            MacAddress dstMac = ethPkt.getDestinationMAC();
            short vlanCheck = ethPkt.getVlanID();
            PortNumber outPort = PortNumber.FLOOD;
            VlanId vlanId = VlanId.vlanId(VlanId.UNTAGGED);

            TrafficSelector.Builder packetSelector1 = DefaultTrafficSelector.builder();
            packetSelector1.matchEthType(Ethernet.TYPE_IPV4);

            TrafficSelector.Builder packetSelector2 = DefaultTrafficSelector.builder();
            packetSelector2.matchEthType(Ethernet.TYPE_IPV4);

            TrafficSelector.Builder packetSelector3 = DefaultTrafficSelector.builder();
            packetSelector3.matchEthType(Ethernet.TYPE_IPV4);

            TrafficSelector.Builder packetSelector4 = DefaultTrafficSelector.builder();
            packetSelector4.matchEthType(Ethernet.TYPE_IPV4);

            TrafficTreatment.Builder packetTreatment1 = DefaultTrafficTreatment.builder();
            TrafficTreatment.Builder packetTreatment2 = DefaultTrafficTreatment.builder();
            TrafficTreatment.Builder packetTreatment3 = DefaultTrafficTreatment.builder();
            TrafficTreatment.Builder packetTreatment4 = DefaultTrafficTreatment.builder();

            if (deviceId.equals(DeviceId.deviceId("of:0000000000000001"))) {
                if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:01"))) {
                    outPort = PortNumber.portNumber(1);
                    log.info("Setting output port to: " + outPort);
                    packetSelector1.matchVlanId(VlanId.vlanId((short) 201));
                    packetSelector1.matchEthDst(dstMac);
                    packetTreatment1.popVlan();
                    log.info("Popping VLAN tag");
                    packetTreatment1.setOutput(outPort);
                    forwardRequest(context, packetSelector1, packetTreatment1, deviceId, outPort);
                } else if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:02"))) {
                    outPort = PortNumber.portNumber(2);
                    vlanId = VlanId.vlanId((short) 202);
                    log.info("Setting output port to: " + outPort);
                    log.info("Tagging packet with vlan tag " + vlanId);
                    packetSelector1.matchEthDst(dstMac);
                    if (AppUiMessageHandler.getVlan()) {
                        packetTreatment1.pushVlan().setVlanId(vlanId).setOutput(outPort);
                        log.info("S1, destination = h2, PUSHING VLAN TAG");
                    } else {
                        packetTreatment1.setOutput(outPort);
                        log.info("S1, destination = h2, NOT PUSHING VLAN TAG");
                    }
                    forwardRequest(context, packetSelector1, packetTreatment1, deviceId, outPort);
                } else {
                    log.info("Unknown destination host, ignoring");
                    return;
                }
            } else if (deviceId.equals(DeviceId.deviceId("of:0000000000000002"))) {
                log.info("s2");
                if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:01"))) {
                    outPort = PortNumber.portNumber(2);
                    vlanId = VlanId.vlanId((short) 201);
                    log.info("Setting output port to: " + outPort);
                    log.info("Tagging packet with vlan tag " + vlanId);
                    packetSelector2.matchEthDst(dstMac);
                    if (AppUiMessageHandler.getVlan()) {
                        packetTreatment2.pushVlan().setVlanId(vlanId).setOutput(outPort);
                    } else {
                        packetTreatment2.setOutput(outPort);
                    }
                    forwardRequest(context, packetSelector2, packetTreatment2, deviceId, outPort);
                } else if (vlanCheck == (short) 202) {
                    outPort = PortNumber.portNumber(3);
                    log.info("Setting output port to: " + outPort);
                    packetSelector2.matchVlanId(VlanId.vlanId((short) 202));
                    packetSelector2.matchEthDst(dstMac);
                    packetTreatment2.popVlan();
                    log.info("popping vlan tag");
                    packetTreatment2.setOutput(outPort);
                    forwardRequest(context, packetSelector2, packetTreatment2, deviceId, outPort);
                } else {
                    log.info("Unknown destination host, ignoring");
                    return;
                }
            } else if (deviceId.equals(DeviceId.deviceId("of:0000000000000003"))) {
                log.info("s3");
                if (vlanCheck == (short) 201) {
                    createRequest(packetSelector3, packetTreatment3, 1, 3, dstMac, deviceId, context, VlanId.vlanId(vlanCheck));
                } else if (vlanCheck == (short) 202) {
                    createRequest(packetSelector3, packetTreatment3, 2, 3, dstMac, deviceId, context, VlanId.vlanId(vlanCheck));
                } else {
                    log.info("Unknown destination host, ignoring");
                    return;
                }

            } else if (deviceId.equals(DeviceId.deviceId("of:0000000000000004"))) {
                log.info("s4");
                if (vlanCheck == (short) 201) {
                    createRequest(packetSelector4, packetTreatment4, 1, 4, dstMac, deviceId, context, VlanId.vlanId(vlanCheck));
                } else if (vlanCheck == (short) 202) {
                    createRequest(packetSelector4, packetTreatment4, 2, 4, dstMac, deviceId, context, VlanId.vlanId(vlanCheck));
                } else {
                    log.info("Unknown destination host, ignoring");
                    return;
                }
            }
        }

        public void createRequest(TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, int portNumberOut, int switchNumber, MacAddress dstMac, DeviceId devId, PacketContext context, VlanId vlanId) {
            PortNumber outPort = PortNumber.portNumber(portNumberOut);
            log.info("switch " + switchNumber + "sending packet on port: " + outPort);
            selector.matchEthDst(dstMac);
            selector.matchVlanId(vlanId);
            treatment.setOutput(outPort);
            forwardRequest(context, selector, treatment, devId, outPort);
        }

        public void forwardRequest(PacketContext context, TrafficSelector.Builder
                selector, TrafficTreatment.Builder treatment, DeviceId deviceId, PortNumber outPort) {
            ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                    .withSelector(selector.build())
                    .withTreatment(treatment.build())
                    .withPriority(5000)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(5)
                    .add();

            if (outPort != PortNumber.FLOOD) flowObjectiveService.forward(deviceId, forwardingObjective);
            context.treatmentBuilder().addTreatment(treatment.build());
            context.send(); // TJEK DENNE...!
        }
    }
    }

