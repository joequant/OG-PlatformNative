### @export "adjust-location"
cd ../../..

### @export "run-rd2pdf"
R CMD Rd2pdf --output=OpenGamma-RStats.pdf --no-preview --force package

### @export "ant"
ant rdocs
