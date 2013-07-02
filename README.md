OpenGamma platform native
-------------------------
This repository contains the native extensions to the OpenGamma Platform.

This repository is focussed on non-Java projects that build on the Java
platform. Projects located in this repository use a combination of Ant and
Maven to build and deploy.

You will need Ant and Maven to build these packages.

The nature of the build will depend on the platform and tools available. To get
started, run `ant configure` with one or more of the following parameters to
set up the local environment.

<dl>
  <dt>-Dprofile.nix=true</dt>
  <dd>Build all Posix-style artifacts (for Max and Linux users)</dd>
  <dt>-Dprofile.windows=true</dt>
  <dd>Build all Windows artifacts</dd>
  <dt>-Dtool.ai=true</dt>
  <dd>Advanced Installer is available for building Windows installation packages</dd>
  <dt>-Dtool.cpptasks=true</dt>
  <dd>The Ant CPPTasks component is available for C/C++ compilation</dd>
  <dt>-Dtool.msvc=true</dt>
  <dd>Microsoft Visual Studio is available for C/C++ compilation</dd>
  <dt>-Dtool.r=true</dt>
  <dd>R tools are available for building the OpenGamma R plugin</dd>
</dl>

For details of additional `profile.*` properties that can further refine the
targets, for example `profile.debug.windows` please refer to the `build.xml`
file.

After the `configure` task has been run, Maven can be invoked with the correct
parameters using `ant install`.

Note that the default build target will perform the `configure` action before
launching Maven to perform its `install` action to compile and deploy the
available projects. For example, to build all `debug` 32-bit Windows components
with Visual Studio and R available, use:

    ant -Dprofile.debug.windows.win32=true -Dtool.msvc=true -Dtool.r=true

[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://developers.opengamma.com)
