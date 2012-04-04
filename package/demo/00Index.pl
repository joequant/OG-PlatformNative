#!/usr/bin/perl
##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Reads the demo scripts and creates an 00Index file automatically.

opendir (DIR, ".") || die $!;
my @files = ();
while (my $file = readdir (DIR)) {
  push (@files, $file);
}
closedir (DIR);
foreach my $file (sort (@files)) {
  next if ($file !~ /^(.*)\.R$/);
  my $buffer = $1;
  open (FILE, $file) || die $!;
  my $state = 0;
  while (my $line = <FILE>) {
    if ($state == 0) {
      $state = 1 if ($line =~ /^ ##\r?$/);
    } elsif ($state == 1) {
      $state = 2 if ($line =~ /^\r?$/);
    } elsif ($state >= 2) {
      if ($line =~ /^# (.*?)\r?$/) {
        if ($state == 2) {
          $state = 3;
          $buffer .= "\t";
        } else {
          $buffer .= " ";
        }
        $buffer .= $1;
      } else {
        last;
      }
    }
  }
  close (FILE);
  print "$buffer\n";
}
