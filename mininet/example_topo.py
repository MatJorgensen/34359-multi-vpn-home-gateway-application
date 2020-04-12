#!/usr/bin/python                                                                            
 
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import RemoteController
from mininet.node import Host
from mininet.node import OVSKernelSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel
from mininet.log import info
from mininet.link import TCLink

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
	self.addLink(h1, s1, cls=TCLink)
	self.addLink(s1, s3, cls=TCLink)
	self.addLink(s1, s4, cls=TCLink)
	self.addLink(s2, s3, cls=TCLink)
	self.addLink(s2, s4, cls=TCLink)

class ONOSController(RemoteController):
    "ONOS Controller"
    def build(self, name):
	self.ip = '127.0.0.1'
	self.port = 6633
	self.protocol = 'tcp'	


topos = {'mytopo': ( lambda: ExampleTopo() ) }
controllers = {'onos': ONOSController}

if __name__ == '__main__':
    setLogLevel( 'info' )
    net = Mininet(topo=ExampleTopo(), controller=lambda name: RemoteController(name, ip='127.0.0.1', port=6633, protocol='tcp'))
    net.start()
    CLI(net)
    net.stop()
