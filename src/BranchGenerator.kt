import java.io.File
import kotlin.math.*
import kotlin.system.exitProcess

/**
 * @brief create 2-way neuron branches and linear cables
 * @author stephanmg <stephan@syntaktischer-zucker.de>
 * @param args collected command line arguments in an array of type String
 */
fun main(args: Array<String>) {
    // usage for program
    val usage = {
        val program = System.getProperty("sun.java.command").split(".")[0]
        println("Usage: $program --method CONSTANT,TAPERING --filename FILENAME --angle ANGLE --n NUM_POINTS")
        println("       Additional options for the methods:")
        println("          1. CONSTANT: --d0 DIAMETER, --l0 PARENT_LENGTH -l1 RIGHT_CHILD_LENGTH --l2 LEFT_CHILD_LENGTH")
        println("          2. TAPERING: --d1 DIAMETER_RIGHT_CHILD_END_POINT --d2 DIAMETER_LEFT_CHILD_END_POINT")
        println("          3. RALL: --r1 LEFT_CHILD_RADIUS --r2 RIGHT_CHILD_RADIUS (Parent branch radius will be calculated)")
        println("          4. LINEAR: --r0 START_RADIUS --r1 END_RADIUS --l0 LINEAR_CABLE_LENGTH --n NUM_POINTS")
    }

    // parse CLI arguments
    fun getArguments(args: Array<String>): Map<String, String?> = args.fold(mutableListOf()) {
            acc: MutableList<MutableList<String>>, s: String ->
                acc.apply {
                    if (s.startsWith('-')) add(mutableListOf(s))
                    else last().add(s)
              }
    }.associate { it[0] to it.drop(1).firstOrNull() }

    // no argument given, show usage
    if (args.isEmpty()) {
        usage()
        exitProcess(1)
    }

    // otherwise get arguments by applying name mapping to variable
    val mapping = getArguments(args)
    val msg: (String) -> String = { argument ->
        usage()
        println()
        "Missing value for CLI argument $argument"
    }

    // check which grid generation method to use
    when (mapping["--method"]) {
        // constant radii method for two way branches
        "constant" -> {
            generateTwoWayBranch(
               (mapping["--filename"] ?: error(msg("--filename"))).toString(),
               (mapping["--l0"] ?: error(msg("--l0"))).toDouble(),
               (mapping["--l1"] ?: error(msg("--l1"))).toDouble(),
               (mapping["--l2"] ?: error(msg("--l2"))).toDouble(),
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble(),
               (mapping["--angle"] ?: error(msg("--angle"))).toDouble(),
               (mapping["--n"] ?: error(msg("--n"))).toInt(),
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble(), // was / 2
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble(), // was / 10
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble(), // was / 10
               false
           )
        }

        // tapering from soma to branching point to tip of neurites
        "tapering" -> {
            generateTwoWayBranch(
                (mapping["--filename"] ?: error(msg("--filename"))).toString(),
                (mapping["--l0"] ?: error(msg("--l0"))).toDouble(),
                (mapping["--l1"] ?: error(msg("--l1"))).toDouble(),
                (mapping["--l2"] ?: error(msg("--l2"))).toDouble(),
                (mapping["--d0"] ?: error(msg("--d0"))).toDouble(),
                (mapping["--angle"] ?: error(msg("--angle"))).toDouble(),
                (mapping["--n"] ?: error(msg("--n"))).toInt(),
                (mapping["--d0"] ?: error(msg("--d0"))).toDouble(),
                (mapping["--d1"] ?: error(msg("--d1"))).toDouble(),
                (mapping["--d2"] ?: error(msg("--d2"))).toDouble(),
                true
            )
        }

        // Rall's 3/2 power rule for child neurites wrt parent's radius
        "rall" -> {
            val leftChildRadius = (mapping["--d1"] ?: error(msg("--d1"))).toDouble()
            val rightChildRadius = (mapping["--d2"] ?: error(msg("--d2"))).toDouble()
            val parentBranchRadius = (leftChildRadius.pow(3/2) + leftChildRadius.pow(3/2)).pow(2/3)
            generateTwoWayBranch(
                (mapping["--filename"] ?: error(msg("--filename"))).toString(),
                (mapping["--l0"] ?: error(msg("--l0"))).toDouble(),
                (mapping["--l1"] ?: error(msg("--l1"))).toDouble(),
                (mapping["--l2"] ?: error(msg("--l2"))).toDouble(),
                parentBranchRadius,
                (mapping["--angle"] ?: error(msg("--angle"))).toDouble(),
                (mapping["--n"] ?: error(msg("--n"))).toInt(),
                parentBranchRadius,
                leftChildRadius,
                rightChildRadius,
                false
            )
        }

        // linear strategy will create an unbranched cable
        "linear" -> {
            generateLinearCable(
                (mapping["--filename"] ?: error(msg("--filename"))).toString(),
                (mapping["--r1"] ?: error(msg("--r1"))).toDouble(),
                (mapping["--r0"] ?: error(msg("--r0"))).toDouble(),
                (mapping["--l0"] ?: error(msg("--l0"))).toDouble(),
                (mapping["--n"] ?: error(msg("--n"))).toInt()
            )
        }

        // if not any available options chosen exit gracefully and print usage
        else -> {
            usage()
        }
    }
}

/**
 * @brief available SWC types
 * For now dendrite and soma can be chosen
 * @param id
 */
enum class SWCType(val id: Int) {
    SOMA(1),
    DEND(3)
}

/**
 * @brief generate a linear (unbranched) cable
 * @param filename output name
 * @param startRadius radius at start point
 * @param endRadius radius at end point
 * @param lengthCable length of cable
 * @param numPoints refinement
 */
fun generateLinearCable(filename: String, endRadius: Double, startRadius: Double, lengthCable: Double, numPoints: Int) {
    print("Processing |=============   |\r")
    File("${filename}_r0=${startRadius}_r1=${endRadius}_l0=${lengthCable}_n=${numPoints}.swc").printWriter().use { out ->
        out.println("1 ${SWCType.SOMA.id} 0 0 0 $startRadius -1") // soma points
        val radIncrement = (endRadius - startRadius) / numPoints
        val distIncrement = lengthCable / numPoints
        for (i in 2 until numPoints) { // dendrite points
            out.println("$i ${SWCType.DEND.id} ${distIncrement*(i-1)} 0 0 ${startRadius+radIncrement*(i-1)} ${i - 1}")
        }
        print("Done       |================|\n")
    }
}

/**
 * @brief generates a symmetric Y-structured dendritic branch (2-way branch) in SWC file format
 * @author stephanmg <stephan@syntaktischer-zucker.de>
 * @see <a href="http://www.neuronland.org/NLMorphologyConverter/MorphologyFormats/SWC/Spec.htm">SWC Specification</a>
 * @param filename output name
 * @param lengthParent length of the branches
 * @param lengthLeftChild length of left child
 * @param lengthRightChild length of right child
 * @param diamParent diameter of the parent branch
 * @param angle branching angle in degree
 * @param numPoints additional points after branching point
 * @param diamBranchingPoint branching point diameter
 * @param diamLeftChild left child diameter
 * @param diamRightChild right child diameter
 * @param tapering if true then dendrites are tapering off towards the tips
 */
fun generateTwoWayBranch(filename: String, lengthParent:Double, lengthLeftChild: Double, lengthRightChild: Double,
                            diamParent:Double, angle:Double, numPoints:Int, diamBranchingPoint: Double,
                            diamLeftChild: Double, diamRightChild: Double, tapering: Boolean) {
    /// check for a consistent user input
    require(0 < abs(angle) && abs(angle) <= 180) { "Angle (deg) must be in range ]0, 180] U [-180, -0[" }
    require(diamLeftChild <= lengthLeftChild) { "Diameter of left child expected to be smaller than corresponding length"}
    require(diamRightChild <= lengthLeftChild) { "Diameter of left child expected to be smaller than corresponding length"}
    require(diamBranchingPoint >= diamLeftChild && diamBranchingPoint >= diamRightChild) { "Branching point diameter expected to be greater or equal then childs' diameters"}
    require(diamParent >= diamBranchingPoint) { "Diameter of parent branch expected to be greater or equal than branching point's diameter"}
    print("Processing |======         |\r")

    /// angle
    val angleInRad = (angle/2.0 * PI / 180.0)
    val angleSign = sign(angle)

    /// positions
    val center = doubleArrayOf(0.0, 0.0, 0.0)
    val a = doubleArrayOf(0.0, -lengthParent, 0.0)
    val b = doubleArrayOf(lengthParent*cos(angleInRad+angleSign*PI/2.0), lengthParent*sin(angleInRad+angleSign*PI/2.0), 0.0);
    val c = doubleArrayOf(lengthParent*cos(-angleInRad+angleSign*PI/2.0), lengthParent*sin(-angleInRad+angleSign*PI/2.0), 0.0);

    /// tapering
    val taperDiameters: (Boolean, Double, Double, Int, Int) -> Double = { onOff, startDiameter, diam, pointsPerBranch, currentPoint ->
        if (onOff) startDiameter-(startDiameter-diam)/pointsPerBranch * currentPoint else diam
    }

    /// writing file
    print("Processing |=============   |\r")
    File("${filename}_angle=$angle.swc").printWriter().use { out ->
        /// parent branch ("root") before bifurcation
        out.println("" + 1 + " ${SWCType.SOMA.id} "  + a[0] + " " + (a[1]-lengthParent) + " " + a[2] + " " + diamParent + " " +  "-1") // soma
        out.println("" + 2 + " ${SWCType.DEND.id} "  + a[0] + " " + ((2.5/3.0)*a[1]-lengthParent) + " " + a[2] + " " + diamParent + " " +  "1") // neurite before BP
        out.println("" + 3 + " ${SWCType.DEND.id} "  + a[0] + " " + ((1.0/3.0)*a[1]-lengthParent) + " " + a[2] + " " + diamParent + " " +  "2") // additional point for measurement
        out.println("" + 4 + " ${SWCType.DEND.id} "  + a[0] + " " + (-((1.0/3.0)*a[1]+lengthParent)) + " " + a[2] + " " + diamParent + " " +  "3") // additional point for measurement
        out.println("" + 5 + " ${SWCType.DEND.id} " + center[0] + " " + center[1] + " " + center[2] + " " + diamBranchingPoint + " " + "4") // BP

        /// first branch ("left child")
        var currentOffset = 6
        for (i in 0 until numPoints) {
            out.println("" + (currentOffset + i) + " ${SWCType.DEND.id} " + lengthLeftChild/numPoints*(i+1) * b[0] / lengthParent + " " +
                    lengthLeftChild/numPoints*(i+1) * b[1] / lengthParent + " " + lengthLeftChild/numPoints*(i+1) * b[2] / lengthParent
                    + " " + taperDiameters(tapering, diamBranchingPoint, diamLeftChild, numPoints, i) + " " + (currentOffset + i - 1))
        }

        currentOffset+=numPoints
        /// second branch ("right child")
        for (i in 0 until numPoints) {
            out.println(
                "" + (currentOffset + i) + " ${SWCType.DEND.id} " + lengthRightChild / numPoints * (i + 1) * c[0] / lengthParent + " " +
                        lengthRightChild / numPoints * (i + 1) * c[1] / lengthParent + " " + lengthRightChild / numPoints * (i + 1) * c[2] / lengthParent
                        + " " + taperDiameters(tapering, diamBranchingPoint, diamRightChild, numPoints, i) + " " + (currentOffset + i - 1 - (if (i == 0) numPoints else 0)))
        }
    }
    print("Done       |================|\n")
}
