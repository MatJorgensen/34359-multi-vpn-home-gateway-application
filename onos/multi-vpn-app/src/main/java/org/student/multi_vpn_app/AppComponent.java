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
package org.student.multi_vpn_app;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.*;
import org.onosproject.net.Host;
import org.onosproject.net.host.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skeletal ONOS application component.
 */

@Component(immediate = true)
public class AppComponent {

    private ApplicationId appId;
    public static VlanId VID = VlanId.vlanId(VlanId.UNTAGGED);

    public static ConcurrentHashMap<DeviceId, ConcurrentHashMap<MacAddress, Tuple<PortNumber, Set<VlanId>>>> switchTable = new ConcurrentHashMap<>();
    ConcurrentHashMap<DeviceId, String> switchType = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

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

            if (ethPkt == null) {
                log.info("Discarding null packet");
                return;
            }

            if(ethPkt.getEtherType() != Ethernet.TYPE_IPV4) return;
            log.info("Proccesing packet request.");

            // First step is to check if the packet came from a newly discovered switch.
            // Create a new entry if required.
            DeviceId deviceId = pkt.receivedFrom().deviceId();
            Set<Host> connectedHosts = hostService.getConnectedHosts(deviceId);

            if (!switchTable.containsKey(deviceId)){
                log.info("Adding new switch: " + deviceId.toString());
                ConcurrentHashMap<MacAddress, Tuple<PortNumber, Set<VlanId>>> hostTable = new ConcurrentHashMap<>();
                switchTable.put(deviceId, hostTable);
                log.info("PacketProcessor: Is switch table empty: " + switchTable.isEmpty());
            }

            // Now lets check if the source host is a known host. If it is not add it to the switchTable.
            ConcurrentHashMap<MacAddress, Tuple<PortNumber, Set<VlanId>>> hostTable = switchTable.get(deviceId);
            Tuple<PortNumber, Set<VlanId>> tableData = new Tuple<>(PortNumber.FLOOD, new HashSet<VlanId>());
            MacAddress srcMac = ethPkt.getSourceMAC();
            if (!hostTable.containsKey(srcMac)){
                log.info("Adding new host: " + srcMac.toString() + " for switch " + deviceId.toString() + " on port " + pkt.receivedFrom().port());
                tableData.setAt0(pkt.receivedFrom().port());

                // Define switch type.
                for (Host host : connectedHosts) {
                    if (srcMac.equals(host.mac()) || ethPkt.getDestinationMAC().equals(host.mac())) {
                        log.info("Setting switch type to 'EDGE'");
                        switchType.put(deviceId, "EDGE");
                    }
                }
                tableData.getValue1().add(VID);
                hostTable.put(srcMac, tableData);
                switchTable.replace(deviceId, hostTable);
            }

            // To take care of loops, we must drop the packet if the port from which it came from does not match the port that the source host should be attached to.
            if (!hostTable.get(srcMac).getValue0().equals(pkt.receivedFrom().port())){
                log.info("Dropping packet to break loop");
                return;
            }

            // Now lets check if we know the destination host. If we do asign the correct output port.
            // By default set the port to FLOOD.
            MacAddress dstMac = ethPkt.getDestinationMAC();
            PortNumber outPort = PortNumber.FLOOD;
            if (hostTable.containsKey(dstMac)){
                tableData = hostTable.get(dstMac);
                outPort = tableData.getValue0();
                log.info("Setting output port to: " + outPort);
            }

            // Generate the traffic selector based on the packet that arrived.
            TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
            selector.matchEthType(Ethernet.TYPE_IPV4);

            // Get VLANs of source and destination hosts.
            Set<VlanId> srcVlans = hostTable.get(srcMac).getValue1();
            Set<VlanId> commonVlans = new HashSet<>(srcVlans);

            if (!tableData.getValue1().contains(VlanId.vlanId(VlanId.UNTAGGED)) && !tableData.getValue1().isEmpty()) {
                log.info(srcMac.toString() + " has VLANs " + srcVlans.toString());
                Set<VlanId> dstVlans = hostTable.get(dstMac).getValue1();
                log.info(dstMac.toString() + " has VLANs " + dstVlans.toString());
                commonVlans.retainAll(dstVlans);
            }

            // ... and generate treatment for the selected traffic.
            TrafficTreatment.Builder treatment = DefaultTrafficTreatment.builder();

            // Logic of edge switches.
            if (switchType.containsKey(deviceId) && switchType.get(deviceId).equals("EDGE")) {
                log.info("EDGE");

                // For ingress packets push VLAN tags.
                if (hostService.getHostsByMac(srcMac).iterator().hasNext() && connectedHosts.contains(hostService.getHostsByMac(srcMac).iterator().next()) && !tableData.getValue1().contains(VlanId.vlanId(VlanId.UNTAGGED))) {

                    // Drop packet if source and host is not in same VLAN.
                    if (commonVlans.isEmpty()) {
                        log.info("Packet dropped. Host " + srcMac.toString() + " not in same VLAN as " + dstMac.toString());
                        return;
                    }
                    log.info("PUSH");
                    pushVlan(selector, treatment, ethPkt, srcMac, dstMac, outPort, commonVlans.iterator().next());

                // For egress packets pop VLAN tags.
                } else if (hostService.getHostsByMac(dstMac).iterator().hasNext() && connectedHosts.contains(hostService.getHostsByMac(dstMac).iterator().next()) && !tableData.getValue1().contains(VlanId.vlanId(VlanId.UNTAGGED))) {
                    log.info("POP");
                    popVlan(selector, treatment, srcMac, dstMac, outPort, commonVlans.iterator().next());

                // If neither simply forward the packet (used for generation of switch tables).
                } else {
                    log.info("FWD");
                    fwd(selector, treatment, srcMac, dstMac, outPort);
                }

            // Logic of core switches.
            } else {
                log.info("CORE");
                if (!tableData.getValue1().contains(VlanId.vlanId(VlanId.UNTAGGED))) {
                    log.info("FWDVLAN");
                    fwdVlan(selector, treatment, srcMac, dstMac, outPort, commonVlans.iterator().next());
                } else {
                    log.info("FWD");
                    fwd(selector, treatment, srcMac, dstMac, outPort);
                }
            }

            // Lastly, forward the request.
            forwardRequest(context, selector, treatment, deviceId, outPort);
        }

        public void pushVlan(TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, Ethernet ethPkt, MacAddress srcMac, MacAddress dstMac, PortNumber outPort, VlanId vlanId) {
            selector.matchEthSrc(srcMac);
            selector.matchEthDst(dstMac);
            ethPkt.setVlanID(vlanId.toShort());
            treatment.pushVlan();
            treatment.setVlanId(vlanId);
            treatment.setOutput(outPort);
        }

        public void popVlan(TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, MacAddress srcMac, MacAddress dstMac, PortNumber outPort, VlanId vlanId) {
            selector.matchEthSrc(srcMac);
            selector.matchEthDst(dstMac);
            selector.matchVlanId(vlanId);
            treatment.popVlan();
            treatment.setOutput(outPort);
        }

        public void fwdVlan(TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, MacAddress srcMac, MacAddress dstMac, PortNumber outPort, VlanId vlanId) {
            selector.matchEthSrc(srcMac);
            selector.matchEthDst(dstMac);
            selector.matchVlanId(vlanId);
            treatment.setOutput(outPort);
        }

        public void fwd(TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, MacAddress srcMac, MacAddress dstMac, PortNumber outPort) {
            selector.matchEthSrc(srcMac);
            selector.matchEthDst(dstMac);
            treatment.setOutput(outPort);
        }

        public void forwardRequest(PacketContext context, TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, DeviceId deviceId, PortNumber outPort) {
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
            context.send();
        }
    }
}


