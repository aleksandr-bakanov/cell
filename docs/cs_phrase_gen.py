# -*- coding: utf-8 -*-

import sys

fname = sys.argv[1]

with open(fname) as f:
    content = f.readlines()
content = [x.strip() for x in content]

for idx, val in enumerate(content):
    print('    <string name="cs_introduction_{}">{}</string>'.format(idx, val))