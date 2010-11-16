PDFS = $(patsubst %.tex,%.pdf,$(wildcard *.tex))

all: $(PDFS)

%.pdf: %.tex
	pdflatex $<

clean: 
	#Tex temp Files
	rm -f *.log *.out *.aux
	#Tex output files
	rm -f *.pdf *.dvi
	#Emacs temp files
	rm -f *~ \#*\#
