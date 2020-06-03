# Y-Generator
Generates 2-way dendritic branches with variable branching angle and branch lengths starting from a simple point soma.
*Note*: If you had previously a local copy (clone) of this repository, this is not further maintained. The repository now reflects a clean from scratch code base.

## Build
- Clone this repository
- Open in IntelliJ IDEA
- `Build->Build Project` in IntelliJ IDEA

## Alternative Build (Ant)
- Navigate to `$CWD`
- Type `ant` in `$CWD`
Note that it might be necessary to edit *build.properties* in `$CWD`.

## Usage (constant radii)
`java -jar Branch-Generator.jar --method constant --filename test --l0 100 --l1 100 --l2 100 --d0 10 --angle 90 --n 2`

This will generate a **SWC** file: *Y-branch_angle=90.swc* with parent or root branch
of length 100, child branches (left and right) of length 100. Branching angle is 90°.
Parent branch's diameter is set to 10 corresponding length units and the branches
taper off at the branching point by a factor of 2 and the dendrites by a factor of
5 with two points per branch. Additionally parameters to adjust can be displaye via:
`java -jar Y-Generator.jar`.

## Options
For explanation of command line arguments, execute: `java -jar Y-Generator.jar`:

Usage:

`Branch_Generator_jar/Branch-Generator --method CONSTANT,TAPERING --filename FILENAME --angle ANGLE --n NUM_POINTS`

       Additional options for the methods:
       
          1. CONSTANT: --d0 DIAMETER, --l0 PARENT_LENGTH -l1 RIGHT_CHILD_LENGTH -l2 LEFT_CHILD_LENGTH
          
          2. TAPERING: --d1 DIAMETER_RIGHT_CHILD_END_POINT --d2 DIAMETER_LEFT_CHILD_END_POINT


## Postprocessing
The generated **SWC** file contains the 1D line graph geometry and can be 
converted to a full 3D geometry with embedded ER (scale factor 0.5 for example and 2 refinements) 
in the **UGX** file format by invoking the following `ugshell` **call**:
`../bin/ugshell -call  ../bin/ugshell -call "create_two_way_branch_from_swc(\"test_angle=90.0.swc\", 0.5, 2)"

