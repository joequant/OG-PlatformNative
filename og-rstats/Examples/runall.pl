#!/usr/bin/perl
##
 # Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Runs all of the example scripts and creates the sample output. By default will generate
# files ending ".Rout". Supply a parameter to change this (e.g. give "tmp" to create ".Rtmp"
# so that the contents can be compared to the original as part of testing).

$outext = "out";
$Rargs = "--no-save";
if (($#ARGV == 1) && ($ARGV[0] ne "")) {
  print "Using output extension $outext\n";
  $outext = $ARGV[0];
}
open (TEMP, ">.tmp") || die $!;
print TEMP "OpenGamma::Init ()\n";
close (TEMP);
$cmd = "R $Rargs < .tmp";
system ($cmd);
opendir (DIR, ".") || die $!;
my @files = ();
while (my $file = readdir (DIR)) {
  push (@files, $file);
}
closedir (DIR);
foreach my $file (sort (@files)) {
  next if ($file !~ /^(.*)\.R$/);
  print "Running $file\n";
  open (TEMP, ">.tmp") || die $!;
  print TEMP "library (\"OpenGamma\")\n";
  print TEMP "source (\"$file\")\n";
  close (TEMP);
  $cmd = "R $Rargs < .tmp > $file$outext";
  system ($cmd);
}
unlink (".tmp");
