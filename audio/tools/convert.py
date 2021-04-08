#!/bin/python3
'''
Since moving audio playback to `fmedia` some files have been problematic - with non-standard sample rates being a common issue. There are also other issues like different volumes which is not a problem with the program itself but inherent to the files.
This script uses `sox` to resample, normalize, convert and potentially do any other future necessary standardization steps.
While it would be ideal to just drop-in original files to prevent any loss of quality, this step is hardly prohibitive and could probably be performed on every added file just for the sake of uniformity and convenience - especially when the WAV sample rate which seems to work best with fmedia is higher than the more typical one (44.1kHz).
WAV files seem to have noticeably less playback latency than OGG.
'''
import sys,os

TARGETS=sys.argv[1:]

if len(TARGETS)==0:
  print(f'Usage: {sys.argv[0]} file1.wav [file2.wav ...]')
  sys.exit(1)

for t in TARGETS:
  to=f'new.{t[:t.rfind(".")]}.wav'
  print(f'{t} to {to}')
  if os.system(f'sox {t} -r 48000 {to} norm -3')!=0:
    print('Aborting!')
    sys.exit(2)
