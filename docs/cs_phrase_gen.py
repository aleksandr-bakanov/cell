# -*- coding: utf-8 -*-

import sys
import io

fname = sys.argv[1]
csname = sys.argv[2]

out = io.open('cs_{}.txt'.format(csname), 'w')

with io.open(fname, encoding="utf-8") as f:
    content = f.readlines()
content = [x.strip() for x in content]

for idx, val in enumerate(content):
    out.write('    <string name="cs_{}_{}">{}</string>\n'.format(csname, idx, val))

out.write('\n')

for idx, val in enumerate(content):
    out.write('    <string name="cs_{}_{}">cs_{}_{}</string>\n'.format(csname, idx, csname, idx))


out.write('\n')

for idx, val in enumerate(content):
    out.write("                '{}': {{ text: 'cs_{}_{}' }},\n".format(idx, csname, idx))


out.close()