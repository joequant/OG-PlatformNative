#!/usr/bin/perl

# Kills any programs with exact matching executables. For example:
#
#   perl exe-kill-win.pl foo\bar.exe
#
# Will kill any processes that are running the %CWD%\foo\bar.exe
# image and leave any other processes running something called
# bar.exe alone.

$processes = `wmic process get executablepath,processid`;
$cwd = `cd`;
$cwd =~ s/\s+$//;
foreach $i (0 .. $#ARGV) {
  $path = $cwd . "\\" . $ARGV[$i];
  foreach $process (split (/\r\n/, $processes)) {
    if ($process =~ /^\Q$path\E\s+(\d+)\s*$/i) {
      $pid = $1;
      print "Killing process $pid\n";
      `taskkill /pid $pid`;
    }
  }
}
