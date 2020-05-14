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

class TopoOne(Topo):
    """Simple test topology with architecture h1-s1-s2-h2. Used to test pushing and popping
     of VLAN tags in the switches."""
    def build(self):
	# Create hosts
	h1 = self.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')
	h2 = self.addHost('h2', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02')

	# Create switches
	s1 = self.addSwitch('s1', cls=OVSKernelSwitch)
	s2 = self.addSwitch('s2', cls=OVSKernelSwitch)

	# Create links
	self.addLink(h1, s1, cls=TCLink)
	self.addLink(h2, s2, cls=TCLink)
	self.addLink(s1, s2, cls=TCLink)


class TopoTwo(Topo):
    """A more complex topology with four hosts and three switches. Used to test functionality
       of VLAN tag handling in edge/core switches, and proper segregation between hosts
       part that are members of a VLAN vs. those that aren not."""
    def build(self):
	# Create hosts
	h1 = self.addHost('h1s1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')
	h2 = self.addHost('h2s1', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02')
	h3 = self.addHost('h3s3', cls=Host, ip='10.0.0.3', mac='00:00:00:00:00:03')
	h4 = self.addHost('h4s3', cls=Host, ip='10.0.0.4', mac='00:00:00:00:00:04')

	# Create switches
	s1 = self.addSwitch('s1', cls=OVSKernelSwitch)
	s2 = self.addSwitch('s2', cls=OVSKernelSwitch)
	s3 = self.addSwitch('s3', cls=OVSKernelSwitch)

	# Create links
	self.addLink(h1, s1, cls=TCLink)
	self.addLink(h2, s1, cls=TCLink)
	self.addLink(h3, s3, cls=TCLink)
	self.addLink(h4, s3, cls=TCLink)
	self.addLink(s1, s2, cls=TCLink)
	self.addLink(s2, s3, cls=TCLink)


class TopoThree(Topo):
    """Example topology from assignment description with 4 switches and 3 host. Note
     that the two "VLANs", red and green, respectively, are emulated by hosts."""
    def build(self):
	# Create hosts
	h1 = self.addHost('h1s1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')
	h2 = self.addHost('h2s2', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02')
	h3 = self.addHost('h3s3', cls=Host, ip='10.0.0.3', mac='00:00:00:00:00:03')

	# Create switches
	s1 = self.addSwitch('s1', cls=OVSKernelSwitch)
	s2 = self.addSwitch('s2', cls=OVSKernelSwitch)
	s3 = self.addSwitch('s3', cls=OVSKernelSwitch)
	s4 = self.addSwitch('s4', cls=OVSKernelSwitch)

	# Create links
	self.addLink(h1, s1, cls=TCLink)
	self.addLink(h2, s2, cls=TCLink)
	self.addLink(h3, s3, cls=TCLink)
	self.addLink(s1, s2, cls=TCLink)
	self.addLink(s1, s4, cls=TCLink)
	self.addLink(s2, s3, cls=TCLink)
	self.addLink(s3, s4, cls=TCLink)


class TopoFour(Topo):
    """The most complex topology. Contains loops, two different VLANs, and two untagged
       hosts."""
    def build(self):
	# Create hosts
	h1 = self.addHost('h1s1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')
	h2 = self.addHost('h2s1', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02')
	h3 = self.addHost('h3s2', cls=Host, ip='10.0.0.3', mac='00:00:00:00:00:03')
	h4 = self.addHost('h4s2', cls=Host, ip='10.0.0.4', mac='00:00:00:00:00:04')
	h5 = self.addHost('h5s3', cls=Host, ip='10.0.0.5', mac='00:00:00:00:00:05')
	h6 = self.addHost('h6s3', cls=Host, ip='10.0.0.6', mac='00:00:00:00:00:06')
	h7 = self.addHost('h7s4', cls=Host, ip='10.0.0.7', mac='00:00:00:00:00:07')

	# Create switches
	s1 = self.addSwitch('s1', cls=OVSKernelSwitch)
	s2 = self.addSwitch('s2', cls=OVSKernelSwitch)
	s3 = self.addSwitch('s3', cls=OVSKernelSwitch)
	s4 = self.addSwitch('s4', cls=OVSKernelSwitch)

	# Create links
	self.addLink(h1, s1, cls=TCLink)
	self.addLink(h2, s1, cls=TCLink)
	self.addLink(h3, s2, cls=TCLink)
	self.addLink(h4, s2, cls=TCLink)
	self.addLink(h5, s3, cls=TCLink)
	self.addLink(h6, s3, cls=TCLink)
	self.addLink(h7, s4, cls=TCLink)
	self.addLink(s1, s2, cls=TCLink)
	self.addLink(s1, s3, cls=TCLink)
	self.addLink(s1, s4, cls=TCLink)
	self.addLink(s2, s3, cls=TCLink)
	self.addLink(s2, s4, cls=TCLink)
	self.addLink(s3, s4, cls=TCLink)


class ONOSController(RemoteController):
    "ONOS Controller"
    def build(self, name):
	self.ip = '127.0.0.1'
	self.port = 6633
	self.protocol = 'tcp'	


topos = {'topo1': ( lambda: TopoOne() ), 
         'topo2': ( lambda: TopoTwo() ), 
         'topo3': ( lambda: TopoThree() ), 
         'topo4': ( lambda: TopoFour() )}

controllers = {'onos': ONOSController}

if __name__ == '__main__':
    setLogLevel( 'info' )
    net = Mininet(topo=TopoOne(), controller=lambda name: RemoteController(name, ip='127.0.0.1', port=6633, protocol='tcp'))
    net.start()
    CLI(net)
    net.stop()



