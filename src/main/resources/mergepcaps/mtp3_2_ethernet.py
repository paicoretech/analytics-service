#!/usr/local/bin/python
import argparse
import os
import sys
from scapy.layers.l2 import Ether, CookedLinux
from scapy.all import rdpcap, wrpcap, Raw, hexdump
from scapy.layers.inet import IP, TCP
from scapy.layers.sctp import SCTP, SCTPChunkData
def convert_mtp3_to_mtp3_ad_layer(raw_packet):
    mtp3_ad_fake_data = (
        b"\x01\x00\x01\x01\x00\x00\x00\x8c\x02\x00\x00\x08\x00\x00\x00\x66"
        b"\x00\x06\x00\x08\x00\x00\x00\x65\x02\x10\x00\x73\x00\x00\x00\x02"
        b"\x00\x00\x00\x01\x03\x02\x00\x07"
    )
    ''' process ocp and dcp
    '''
    # Extract DPC and OPC from the original packet
    opc_dpc = raw_packet[Raw].load[1:5][::-1] #bit calculate from right to left
    print(opc_dpc)
    # Extract the first 14 bits for OPC and the last 14 bits for DCP
    opc_bits = int.from_bytes(opc_dpc[0:3], byteorder='big')
    print(opc_bits)
    new_opc = (opc_bits & 0x0FFFC0) >> 6 #remove last 5 bit then remove first 4 bit then remove 2 bit
    print(new_opc)
    dpc_bits = int.from_bytes(opc_dpc[2:4], byteorder='big')
    new_dpc = (dpc_bits & 0x3FFF)
    # add new vpc and new dpc
    mtp3_with_new_opc_new_vpc = (
        mtp3_ad_fake_data[:28] + new_opc.to_bytes(4, byteorder='big') + new_dpc.to_bytes(4, byteorder='big') + mtp3_ad_fake_data[36:]
    )
    """
        calculate mtp3 layer length
    """
    new_length_value = (len(raw_packet) + 37)
    new_length_value_hex = new_length_value.to_bytes(4, byteorder='big')
    # Modify the length field
    modified_data_tmp = (
            mtp3_with_new_opc_new_vpc[:4] + new_length_value_hex + mtp3_with_new_opc_new_vpc[8:]
    )
    new_parameter_length_hex = (new_length_value - 40 + 14).to_bytes(2, byteorder='big')
    modified_data = (modified_data_tmp[:26] + new_parameter_length_hex + modified_data_tmp[28:])
    sccp_part = raw_packet[Raw].load[5:]
    new_packet = Raw(load=modified_data) / sccp_part
    new_packet.time = raw_packet.time
    return new_packet
def process_pcap(input_file_name, output_file_name):
    print('Opening {}...'.format(input_file_name))
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
            new_packet = p
            new_packet.time = p.time
            all_packets.append(new_packet)
        if p.haslayer(TCP):
            tc = p[TCP]
            print(f"Source Port: {tc.sport}, Destination Port: {tc.dport}")
            hexdump(tc.payload)
            print("\n" + "=" * 50 + "\n")
        # get last 4 bit of first byte
        if (p[Raw].load[0] & 0x0F) == 0x03:
            mtp3_ad = convert_mtp3_to_mtp3_ad_layer(p)
            sctp = SCTP(sport=12345, dport=54321, tag=0x0)
            sctp_data_payload = mtp3_ad
            sctp_data_hdr = SCTPChunkData(data=sctp_data_payload, proto_id=0x03, beginning=1, ending=1)
            new_packet = Ether(src="00:11:22:33:44:55", dst="66:77:88:99:aa:bb") / IP(src="192.168.1.1", dst="192.168.1.2") / sctp / sctp_data_hdr
            new_packet.time = p.time
            all_packets.append(new_packet)
        counter += 1
    print('Processed {} packets...'.format(counter))
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
        ofn = "/Users/trannhan/Desktop/nhan/test.pcap"
    for file in pcap_files:
        if not os.path.isfile(file):
            print('"{}" does not exist, skip'.format(file), file=sys.stderr)
            continue
        pkts = process_pcap(file, ofn)
        output_array.extend(pkts)
    print("write to file {}".format(ofn))
    wrpcap(ofn, output_array)
    sys.exit(0)
