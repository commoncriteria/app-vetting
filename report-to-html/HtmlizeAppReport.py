#/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys


def analyze(report, out):
    root = report.getroot()
    out.write("Root tag: " + root.tag)

for ii in range(1, len(sys.argv)):
    analyze(ET.parse(sys.argv[ii]), sys.stdout)
