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

def svg_rectangles(svgdom):
    """
    Returns list of (x, y, width, height) tuples representing rectangles
    extracted from given SVG DOM tree.
    """
    rects = []
    for svgrect in svgdom.getElementsByTagName("rect"):
        x      = float(svgrect.attributes["x"].value)
        y      = float(svgrect.attributes["y"].value)
        width  = float(svgrect.attributes["width"].value)
        height = float(svgrect.attributes["height"].value)
        rects.append((x, y, width, height))
    return rects

def appendRectangleShape(doc, node, rect):
    """
    Appends to node a <shape> with a nested <rect> with the given coordintes.
    The shape and rect elements are created as children of doc.
    """
    x, y, width, height = rect
    shape = doc.createElement("shape")
    rect  = doc.createElement("rect")
    rect.setAttribute("x", str(x))
    rect.setAttribute("y", str(y))
    rect.setAttribute("width", str(width))
    rect.setAttribute("height", str(height))
    shape.appendChild(rect)
    node.appendChild(shape)

def create_sim_map(width, height, rectangles):
    """
    Takes width, height, list of (x, y, width, height) tuples representing
    rectangles, and produces a DOM tree of the simulator map containing the
    given rectangles as walls.
    """
    doc = minidom.Document()
    config = doc.createElement("config")

    # World tag.
    world  = doc.createElement("world")
    appendRectangleShape(doc, world, (0, 0, width, height))
    config.appendChild(world)

    # Init-robot-positions tag.
    pose = doc.createElement("init-robot-positions")
    pos1 = doc.createElement("position")
    pos1.setAttribute("x", "1")
    pos1.setAttribute("y", "1")
    pos1.setAttribute("theta", "0")
    pose.appendChild(pos1)
    config.appendChild(pose)

    # All the rectangle walls:
    for rect in rectangles:
        wall = doc.createElement("wall")
        appendRectangleShape(doc, wall, rect)
        config.appendChild(wall)
    doc.appendChild(config)
    return doc

if __name__=="__main__":
    if len(sys.argv) < 2:
        print "Usage: %s file.xml\nWill produce file.cfg" % sys.argv(0)
        exit(1)

    # Parse SVG, create simulator map.
    svg_filename = sys.argv[1]
    svgdom        = minidom.parse(svg_filename)
    width, height = svg_size(svgdom)
    rectangles    = svg_rectangles(svgdom)
    print "Width %lf, height %lf, %d rectangles." %\
            (width, height, len(rectangles))
    simdom = create_sim_map(width, height, rectangles)

    # Write simulator map.
    if svg_filename[-4:] == ".svg":
        sim_filename = svg_filename[:-4] + ".cfg"
    else:
        sim_filename = svg_filename + ".cfg"
    sim_file = open(sim_filename, "w")
    sim_file.write(simdom.toprettyxml(indent="    "))
    print "Wrote %s." % sim_filename
