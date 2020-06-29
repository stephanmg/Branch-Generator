# Branch-Generator
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

## Usage (tapering)
`java -jar Branch-Generator.jar --method tapering --filename test --l0 100 --l1 100 --l2 100 --d0 10 --angle 90 --n 2 --d1 5 --d2 5`

## Usage (Rall)
According to Rall and Rinzel 1973 the sum of radii of child branches at each branching point raised to the power of 3/2 must equal
the parent branch radius also raised to the power of 3/2 (Rall's 3/2 power rule). The user can specify these radii with method *Rall*.

## Usage (linear)
`java -jar Branch-Generator.jar method linear --filename unbranched --l0 10 --r0 2 --r1 1 --n 10`.

## Options
For explanation of command line arguments, execute: `java -jar Y-Generator.jar`:

Usage:

`Branch_Generator_jar/Branch-Generator --method CONSTANT,TAPERING --filename FILENAME --angle ANGLE --n NUM_POINTS`

       Additional options for the methods:
       
          1. CONSTANT: --d0 DIAMETER, --l0 PARENT_LENGTH -l1 RIGHT_CHILD_LENGTH -l2 LEFT_CHILD_LENGTH
          
          2. TAPERING: --d1 DIAMETER_RIGHT_CHILD_END_POINT --d2 DIAMETER_LEFT_CHILD_END_POINT

          3. RALL: --r0 RADIUS_PARENT_BRANCHR --r1 RADIUS_LEFT_CHILD --r2 RADIUS_RIGHT_CHILD

          4. LINEAR: --r0 START_RADIUS --r1 END_RADIUS --l0 LINEAR_CABLE_LENGTH --n NUM_POINTS 


## Postprocessing
The generated **SWC** file contains the 1D line graph geometry and can be 
converted to a full 3D geometry with embedded ER (scale factor 0.5 for example and 2 refinements) 
in the **UGX** file format by invoking the following `ugshell` **call**:

` ../bin/ugshell -call  ../bin/ugshell -call "create_two_way_branch_from_swc(\"test_angle=90.0.swc\", 0.5, 2)" `

# Geometries
Two way branch geometries are stored [here](https://temple.app.box.com/folder/116285138468) and linear cables [there](https://temple.app.box.com/folder/116511573967).

# References
Rall W, Rinzel J. Branch input resistance and steady attenuation for input to one branch of a dendritic neuron model. Biophys J. 1973;13(7):648-687. doi:10.1016/S0006-3495(73)86014-X
