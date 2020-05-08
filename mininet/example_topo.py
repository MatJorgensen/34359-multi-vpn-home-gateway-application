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

class myTopo(Topo):
    "Example topology from assignment description with 4 switches and 1 host."
    def __init__(self):
	# Create hosts
	Topo.__init__(self)
	h1 = self.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')
	h2 = self.addHost('h2', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02')

	# Create switches
	s1 = self.addSwitch('s1', cls=OVSKernelSwitch, mac='00:00:00:00:00:03')
	s2 = self.addSwitch('s2', cls=OVSKernelSwitch, mac='00:00:00:00:00:04')
	s3 = self.addSwitch('s3', cls=OVSKernelSwitch, mac='00:00:00:00:00:05')
	s4 = self.addSwitch('s4', cls=OVSKernelSwitch, mac='00:00:00:00:00:06')

	# Create links
	self.addLink(h1, s1, cls=TCLink)
	self.addLink(s1, s3, cls=TCLink)
	self.addLink(s1, s4, cls=TCLink)
	self.addLink(s2, s3, cls=TCLink)
	self.addLink(s2, s4, cls=TCLink)
	self.addLink(h2, s2, cls=TCLink)

class ONOSController(RemoteController):
    "ONOS Controller"
    def build(self, name):
	self.ip = '127.0.0.1'
	self.port = 6633
	self.protocol = 'tcp'	


topos = {'myTopo': ( lambda: myTopo() ) }
controllers = {'onos': ONOSController}

#if __name__ == '__main__':
#    setLogLevel( 'info' )
#    net = Mininet(topo=ExampleTopo(), controller=lambda name: RemoteController(name, ip='127.0.0.1', #port=6633, protocol='tcp'))
#    net.start()
#    CLI(net)
#    net.stop()
