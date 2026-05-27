#!/usr/local/bin/python
import argparse
import os
import sys
import time
from scapy.layers.l2 import Ether, CookedLinux
from scapy.all import rdpcap, wrpcap, Raw
from scapy.layers.inet import IP
def process_pcap(input_file_name, output_file_name):
    print('Opening {}...'.format(input_file_name))
    start_time = time.time()
    packets = rdpcap(input_file_name)
    all_packets = []
    counter = 0
    for p in packets:
        if p.haslayer(CookedLinux):
            payload = p.payload
            new_packet = Ether() / payload
            new_packet.time = p.time
            all_packets.append(new_packet)
        if p.haslayer(Ether):
            print('Found an Ether packet')
            new_packet = p
            new_packet.time = p.time
            all_packets.append(new_packet)
        if p.haslayer(Raw) and p[Raw].load[0] == 0x83:
            print('Found a Raw packet with 0x83 at the beginning')
            new_packet = Ether() / IP() / p[Raw]
            new_packet.time = p.time
            all_packets.append(new_packet)
        counter += 1
    end_time = time.time()
    elapsed_time = end_time - start_time
    print('Processed {} packets in {:.2f} seconds.'.format(counter, elapsed_time))
    return all_packets
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='PCAP reader')
    parser.add_argument('--pcap', metavar='<pcap file name>',
                        help='pcap file to parse', required=True)
    parser.add_argument('--output', metavar='<pcap output file name>',
                        help='output file', required=True)
    args = parser.parse_args()
    file_names = args.pcap
    ofn = args.output
    pcap_files = file_names.split(",")
    output_array = []
    if ofn == "":
        ofn = "/opt/paic/nbm_ingestion/encapsulation-test/out-test.pcap"
    for file in pcap_files:
        if not os.path.isfile(file):
            print('"{}" does not exist, skip'.format(file), file=sys.stderr)
            continue
        pkts = process_pcap(file, ofn)
        output_array.extend(pkts)
    print("write to file {}".format(ofn))
    wrpcap(ofn, output_array)
    sys.exit(0)
