import numpy
import re
import sys

fname = sys.argv[1]

with open(fname) as f:
    content = f.readlines()

feColorMatrix = [[0.21, 0.72, 0.072, 0, 0], [0.21, 0.72, 0.072, 0, 0], [0.21, 0.72, 0.072, 0, 0], [0, 0, 0, 1, 0], [0, 0, 0, 0, 1]]

for line in content:
    matchObj = re.match(r'^(.+?style="fill:#)(.+?)(;.*)', line, re.M)
    if matchObj and matchObj.groups():
        initialHex = matchObj.group(2)
        rHex = int(initialHex[0:2], 16)
        gHex = int(initialHex[2:4], 16)
        bHex = int(initialHex[4:], 16)
        aHex = 255
        initialColorVector = [rHex, gHex, bHex, aHex, 1]
        resultColorVector = numpy.matmul(feColorMatrix, initialColorVector)
        r = int(resultColorVector[0])
        g = int(resultColorVector[1])
        b = int(resultColorVector[2])
        sys.stdout.write('{0}{1:02x}{2:02x}{3:02x}{4}\n'.format(matchObj.group(1), r, g, b, matchObj.group(3)))
    else:
        sys.stdout.write(line)
    sys.stdout.flush()
