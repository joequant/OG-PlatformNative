SCI_W="11.375in"
SCI_H="17in"

echo "
\\renewcommand{\\papertype}{custom}
\\renewcommand{\\fontpointsize}{14pt}
\\setlength{\\paperwidth}{$SCI_W}
\\setlength{\\paperheight}{$SCI_H}
\\renewcommand{\\setpspagesize}{
  \\ifthenelse{\\equal{\\orientation}{portrait}}{
    \\special{papersize=$SCI_W,$SCI_H}
    }{\\special{papersize=$SCI_H,$SCI_W}
    }
  }
" > papercustom.cfg

sudo mv papercustom.cfg /usr/local/texlive/2011/texmf-dist/tex/latex/sciposter/papercustom.cfg

