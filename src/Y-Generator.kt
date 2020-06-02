import java.io.File
import kotlin.math.*

/**
 * @brief create 2-way neuron branches
 * @author stephanmg <stephan@syntaktischer-zucker.de>
 * @param args collected command line arguments in an array of type String
 */
fun main(args: Array<String>) {
    when (args.size) {
        6 -> {
            println("Generating 2-way branch in file ${args[0]}_angle=${args[5]}.swc")
            generateSWC(
                "${args[0]}", args[1].toDouble(), args[2].toDouble(), args[3].toDouble(), args[4].toDouble(),
                args[5].toDouble(), 2, args[4].toDouble() / 2, args[4].toDouble() / 10,
                args[4].toDouble() / 10
            )
        }
        10 -> {
            println("Generating 2-way branch in file ${args[0]}_angle=${args[5]}.swc")
            generateSWC(
                "${args[0]}", args[1].toDouble(), args[2].toDouble(), args[3].toDouble(), args[4].toDouble(),
                args[5].toDouble(), args[6].toInt(), args[4].toDouble() / 2, args[4].toDouble() / 10,
                args[4].toDouble() / 10
            )
        }
        else -> {
            val program = System.getProperty("sun.java.command").split(".")[0]
            println("Usage: $program FILENAME LENGTH_PARENT LENGTH_LEFT_CHILD LENGTH_RIGHT_CHILD " +
                    "DIAMETER_PARENT BRANCHING_ANGLE [NUM_POINTS] [DIAM_BRANCHING_POINT] [DIAM_LEFT_CHILD] [DIAM_RIGHT_CHILD]")
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
 */
fun generateSWC(filename: String, lengthParent:Double, lengthLeftChild: Double, lengthRightChild: Double,
                diamParent:Double, angle:Double, numPoints:Int, diamBranchingPoint: Double,
                diamLeftChild: Double, diamRightChild: Double) {
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

    /// writing file
    print("Processing |=============   |\r")
    File(filename + "_angle=" + angle + ".swc").printWriter().use { out ->
        /// parent branch ("root") before bifurcation
        /// TODO: Add more points here on the root branch see below
        out.println("" + 1 + " 1 "  + a[0] + " " + a[1] + " " + a[2] + " " + diamParent + " " +  "-1") // soma
        out.println("" + 2 + " 3 "  + a[0] + " " + (2.5/3.0)*a[1] + " " + a[2] + " " + diamParent + " " +  "1") // neurite before BP
        /// out.println("" + 2 + " 3 "  + a[0] + " " + (1.0/3.0)*a[1] + " " + a[2] + " " + diamParent + " " +  "1") // additional point for measuremnt
        out.println("" + 3 + " 3 " + center[0] + " " + center[1] + " " + center[2] + " " + diamBranchingPoint + " " + "2") // BP

        /// first branch ("left child")
        var currentOffset = 4
        for (i in 0 until numPoints) {
            out.println("" + (currentOffset + i) + " 3 " + lengthLeftChild/numPoints*(i+1) * b[0] / lengthParent + " " +
                    lengthLeftChild/numPoints*(i+1) * b[1] / lengthParent + " " + lengthLeftChild/numPoints*(i+1) * b[2] / lengthParent
                    + " " + diamLeftChild+ " " + (currentOffset + i - 1))
        }

        currentOffset+=numPoints
        /// second branch ("right child")
        for (i in 0 until numPoints) {
            out.println(
                "" + (currentOffset + i) + " 3 " + lengthRightChild / numPoints * (i + 1) * c[0] / lengthParent + " " +
                        lengthRightChild / numPoints * (i + 1) * c[1] / lengthParent + " " + lengthRightChild / numPoints * (i + 1) * c[2] / lengthParent
                        + " " + diamRightChild + " " + (currentOffset + i - 1 - (if (i == 0) numPoints else 0)))
        }
    }
    print("Done       |================|\n")
}
