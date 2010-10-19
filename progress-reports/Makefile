PDFS = $(patsubst %.tex,%.pdf,$(wildcard *.tex))

all: $(PDFS)

%.pdf: %.tex
	pdflatex $<

