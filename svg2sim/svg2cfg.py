#!/usr/bin/python
#
# Takes a .svg file, extracts its size and all the rectangles it describes, and
# produces a .cfg file for ADESim, with all the rectangles as walls.

from xml.dom import minidom
import sys

def svg_size(svgdom):
    """Returns (width, height) tuple from given SVG DOM tree."""
    svg = svgdom.getElementsByTagName("svg")
    assert len(svg) == 1
    attrs  = svg[0].attributes
    width  = float(attrs["width"].value)
    height = float(attrs["height"].value)
    return (width, height)

if __name__=="__main__":
    if len(sys.argv) < 2:
        print "Usage: %s file.xml\nWill produce file.cfg" % sys.argv(0)
        exit(1)
    svgdom        = minidom.parse(sys.argv[1])
    width, height = svg_size(svgdom)
    #rectangles    = svg_rects(svgdom)
    print "Width %lf, height %lf, %d rectangles." % (width, height, 0)
    pass
