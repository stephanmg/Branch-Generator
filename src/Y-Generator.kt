import java.io.File
import kotlin.math.*
import kotlin.system.exitProcess

/**
 * @brief create 2-way neuron branches
 * @author stephanmg <stephan@syntaktischer-zucker.de>
 * @param args collected command line arguments in an array of type String
 */
fun main(args: Array<String>) {
    // usage for program
    val usage = {
        val program = System.getProperty("sun.java.command").split(".")[0]
        println("Usage: $program --method CONSTANT,TAPERING --filename FILENAME --angle ANGLE --n NUM_POINTS")
        println("       Additional options for the methods:")
        println("          1. CONSTANT: --d0 DIAMETER, --l0 PARENT_LENGTH -l1 RIGHT_CHILD_LENGTH -l2 LEFT_CHILD_LENGTH")
        println("          2. TAPERING: --d1 DIAMETER_RIGHT_CHILD_END_POINT --d2 DIAMETER_LEFT_CHILD_END_POINT")
    }

    // parse arguments
    fun getopt(args: Array<String>): Map<String, String?> = args.fold(mutableListOf()) {
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

    // otherwise get arguments
    val mapping = getopt(args)
    val msg: (String) -> String = { argument ->
        usage()
        println()
        "Missing value for CLI argument $argument"
    }

    when (mapping["--method"]) {
        "constant" -> {
           generateSWC(
               (mapping["--filename"] ?: error(msg("--filename"))).toString(),
               (mapping["--l0"] ?: error(msg("--l0"))).toDouble(),
               (mapping["--l1"] ?: error(msg("--l1"))).toDouble(),
               (mapping["--l2"] ?: error(msg("--l2"))).toDouble(),
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble(),
               (mapping["--angle"] ?: error(msg("--angle"))).toDouble(),
               (mapping["--n"] ?: error(msg("--n"))).toInt(),
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble()/2,
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble()/10,
               (mapping["--d0"] ?: error(msg("--d0"))).toDouble()/10,
               false
           )
        }

        "tapering" -> {
            generateSWC(
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

        else -> {
            usage()
        }
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
fun generateSWC(filename: String, lengthParent:Double, lengthLeftChild: Double, lengthRightChild: Double,
                diamParent:Double, angle:Double, numPoints:Int, diamBranchingPoint: Double,
                diamLeftChild: Double, diamRightChild: Double, tapering: Boolean) {
    /// check for a consistent user input
    require(0 < abs(angle) && abs(angle) <= 180) { "Angle (deg) must be in range ]0, 180] U [-180, -0[" }
    require(diamLeftChild < lengthLeftChild) { "Diameter of left child expected to be smaller than corresponding length"}
    require(diamRightChild < lengthLeftChild) { "Diameter of left child expected to be smaller than corresponding length"}
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
    File(filename + "_angle=" + angle + ".swc").printWriter().use { out ->
        /// parent branch ("root") before bifurcation
        out.println("" + 1 + " 1 "  + a[0] + " " + a[1] + " " + a[2] + " " + diamParent + " " +  "-1") // soma
        out.println("" + 2 + " 3 "  + a[0] + " " + (2.5/3.0)*a[1] + " " + a[2] + " " + diamParent + " " +  "1") // neurite before BP
        /// TODO: Add more points here and add tapering for root branch too, add additional measuring subsets
        /// out.println("" + 2 + " 3 "  + a[0] + " " + (1.0/3.0)*a[1] + " " + a[2] + " " + diamParent + " " +  "1") // additional point for measurement
        out.println("" + 3 + " 3 " + center[0] + " " + center[1] + " " + center[2] + " " + diamBranchingPoint + " " + "2") // BP

        /// first branch ("left child")
        var currentOffset = 4
        for (i in 0 until numPoints) {
            out.println("" + (currentOffset + i) + " 3 " + lengthLeftChild/numPoints*(i+1) * b[0] / lengthParent + " " +
                    lengthLeftChild/numPoints*(i+1) * b[1] / lengthParent + " " + lengthLeftChild/numPoints*(i+1) * b[2] / lengthParent
                    + " " + taperDiameters(tapering, diamBranchingPoint, diamLeftChild, numPoints, i) + " " + (currentOffset + i - 1))
        }

        currentOffset+=numPoints
        /// second branch ("right child")
        for (i in 0 until numPoints) {
            out.println(
                "" + (currentOffset + i) + " 3 " + lengthRightChild / numPoints * (i + 1) * c[0] / lengthParent + " " +
                        lengthRightChild / numPoints * (i + 1) * c[1] / lengthParent + " " + lengthRightChild / numPoints * (i + 1) * c[2] / lengthParent
                        + " " + taperDiameters(tapering, diamBranchingPoint, diamRightChild, numPoints, i) + " " + (currentOffset + i - 1 - (if (i == 0) numPoints else 0)))
        }
    }
    print("Done       |================|\n")
}
