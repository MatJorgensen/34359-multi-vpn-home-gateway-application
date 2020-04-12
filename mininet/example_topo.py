#!/usr/bin/python                                                                            
                                                                                             
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call

class ExampleTopo(Topo):
    "Example topology from assignment description with 4 switches and 1 host."
    def build(self):
	# Create hosts
	h1 = self.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')

	# Create switches
	s1 = self.addSwitch('s1', cls=OVSKernelSwitch)
	s2 = self.addSwitch('s2', cls=OVSKernelSwitch)
	s3 = self.addSwitch('s3', cls=OVSKernelSwitch)
	s4 = self.addSwitch('s4', cls=OVSKernelSwitch)

	# Create links
	net.addLink(h1, s1, cls=TCLink)
	net.addLink(s1, s3, cls=TCLink)
	net.addLink(s1, s4, cls=TCLink)
	net.addLink(s2, s3, cls=TCLink)
	net.addLink(s2, s4, cls=TCLink)

class ONOSController(RemoteController):
    "ONOS Controller"
    def build(self):
	self.name = 'ONOSController',
	self.ip = '127.0.0.1',
	self.protocol='tcp',
	self.port = 6633

topos = {'exampletopo': ExampleTopo}
controllers = {'onos': ONOSController}

if __name__ == '__main__':
    net.start()
    CLI(net)
    net.stop()
