import csv
import json
import re
from liquid import Liquid

def format(s):
    return '"{}"'.format(s)

def is_empty(value):
    return not value or value == ''

def process_enum(enum_value, info_element):
    """If a enum has been specifed in the enum override, then
    we'll use that. If not, we will use the value in the
    info_element with the caveat that if there is something
    whithin parathesis, we'll use that.

    E.g., in the spec the Information Element for IMSI is listed as:
    International Mobile Subscriber Identity (IMSI)

    so in that case we'll actually grab the IMSI part and use that
    as the enum
    """
    if is_empty(enum_value):
        m = re.search('(.*)\\((.*)\\)(.*)', info_element)
        if not m:
            # print(info_element, ' not matching regexp')
            return format_enum(info_element)
        return format_enum(m.group(2))

    return format_enum(enum_value)

def format_enum(enum):
    return enum.replace('-', '_').replace(' ', '_').strip().upper()

def process_octets(octets):
    """Some of the GTPv2 Info Elements have a fixed set of octets.
    In the specification, if a number has been specified, then
    this IE is of fixed length.

    The strategy is simply if there is a number, return it, if not
    return -1 as indication that the IE isn't of fixed length, hence,
    the IE is of variable length.
    """
    try:
        return int(octets.strip())
    except:
        return -1


def is_extendable(value):
    """An IE can be extendable, figure that out based on whether
    or not the comment in the table from the specification has
    the word extendable in it
    """
    if 'extendable' in value.lower():
        return 'true'
    return 'false'

def loadCsv(file_name = 'gtpv2_information_elements.tsv'):
    elements = []
    with open(file_name) as file:
        reader = csv.reader(file, delimiter = '\t')
        next(reader)
        for row in reader:
            if row[0] == 'x':
                continue

            type = int(row[1])
            enum = process_enum(row[2], row[3])
            info_element = row[3]
            comment = format(row[4])
            octets = process_octets(row[5])

            elements.append({
                'type' : type,
                'enum' : enum,
                'friendly_name' : info_element,
                'octets' : octets,
                'extendable' : is_extendable(comment),
                'comment' : comment,
            })
    return elements;


def render(elements, template):
    with open(template) as f:
        content = f.read()
        liq = Liquid(content)
        res = liq.render(elements = elements)

    return res

if __name__ == '__main__':
    elements = loadCsv()
    # print(json.dumps(elements, indent = 3) )
    res = render(elements, 'gtpv2_information_elements.liquid')
    # print(res)

    path = '../codec-gtp/src/main/java/io/snice/networking/codec/gtp/gtpc/v2'
    java_class = 'Gtp2InfoElement'
    java_file = path + '/' + java_class + ".java"
    # java_file = 'Test.java'
    with open(java_file, 'w') as java:
        java.write(res)
