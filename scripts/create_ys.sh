#!/bin/bash
## Creates y structures with the Y-Generator
## Use bash getopt to parse arguments to generate a general helper script

# constant radii Y structures
METHOD=constant
FILENAME=Y
l0=100
l1=100
l2=100
d0=10
n=2

# create Y's with branching angle from 30 to 180 with incremeent of 5
MIN=30
MAX=180
INCREMENT=5

i=1
total=$(seq $MIN $INCREMENT $MAX | wc -l | tr -d ' ')
for angle in $(seq $MIN $INCREMENT $MAX); do
   echo -n "($i/$total) Creating file ${FILENAME}_angle=${angle}.swc now..."
   java -jar Branch-Generator.jar --method "$METHOD" --filename "$FILENAME" --l0 "$l0" --l1 "$l1" --l2 "$l2" --d0 "$d0" --angle "$angle" --n "$n" &> /dev/null
   echo " done."
   i=$(($i+1))
done
