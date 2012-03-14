OpenGamma Tools for R
---------------------

This repository contains the code for the OpenGamma R library. This should be
considered as a preview only and we do not recommend you attempt to clone or
fork it in its current state. We will soon be merging this code into the main
OG-Platform repository containing all of our other open source components, at
which point this repository will be deleted.

This package is dependent on the OG-Language component from the main
OG-Platform build. If these artifacts are not correctly generated then no
artifacts will be produced from here.

Assuming that OG-Language builds correctly then the R package should compile
and run from Linux as long as R is in the path and R-devel headers are in the
standard include path.

It is not currently possible to build the R package from a Windows environment
without configuring R to work with Microsoft Visual Studio. We will soon be
posting instructions on how to do this, but will be publishing MSI files as
part of the 1.0 release for those unable to perform a source build.
