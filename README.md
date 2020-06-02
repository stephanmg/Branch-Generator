# Y-Generator
Generates 2-way dendritic branches with variable branching angle and branch lengths starting from a simple point soma.

## Build
- Clone this repository
- Open in IntelliJ IDEA
- `Build->Build Project` in IntelliJ IDEA

## Alternative Build (Ant)
- Navigate to `$CWD`
- Type `ant` in `$CWD`
Note that it might be necessary to edit *build.properties* in `$CWD`.

## Usage
`java -jar Y-Generator.jar`  Y-branch 100 100 100 10 90

This will generate a **SWC** file: *Y-branch_angle=90.swc* with parent or root branch
of length 100, child branches (left and right) of length 100. Branching angle is 90°.
Parent branch's diameter is set to 10 corresponding length units and the branches
taper off at the branching point by a factor of 2 and the dendrites by a factor of
5 with two points per branch. Additionally parameters to adjust can be displaye via:
`java -jar Y-Generator.jar`.

## Options
For explanation of command line arguments, execute: `java -jar Y-Generator.jar`.

## Postprocessing
The generated **SWC** file contains the 1D line graph geometry and can be 
converted to a full 3D geometry with embedded ER (scale factor 0.5) as an **UGX** file by:
`../bin/ugshell -call  ../bin/ugshell -call "test_import_swc_general_var(\"test_angle=90.0.swc\", false, 0.5, true, 1, 0, true, 1.0, false, false, 3)"`

