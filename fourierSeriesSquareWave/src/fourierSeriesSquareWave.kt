import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import kotlin.math.PI
import kotlin.math.sin


//GL STUFFS
const val WINDOW_SIZE_HEIGHT = 500
const val WINDOW_SIZE_WIDTH = 800
var window: Long = NULL

//WAVE STUFFS
const val TIME_STEP = 0.001 //how much do we increment our x value each tick. Controls the integrity/continuity of the generated waveform
// CARRIER WAVE VARS
const val F_CYCLES: Double = 2.0
const val F_PHASE_SHIFT_DEGREES: Double = 0.0 //Amount to shift the wave by in degrees (0-360). Negative number shift left, positive to the right.

fun init(windowSizeW: Int = WINDOW_SIZE_WIDTH, windowSizeH: Int = WINDOW_SIZE_HEIGHT) {
    if ( !glfwInit()) {
        throw Exception("Failed to initialize GLFW.")
    }
    glfwDefaultWindowHints()
    //Do not allow resize
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    window = glfwCreateWindow(windowSizeW, windowSizeH, "FourierSquareWave", 0, 0)
    if (window == MemoryUtil.NULL) {
        throw Exception("Failed to initialize window.")
    }
    glfwMakeContextCurrent(window)

    // GL configuration comes AFTER we make the window our current context, otherwise errors
    GL.createCapabilities()
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GL11.glOrtho(0.0, WINDOW_SIZE_WIDTH.toDouble(), -2.0, 2.0, -1.0, 1.0)
    GL11.glViewport(0, 0, WINDOW_SIZE_WIDTH, WINDOW_SIZE_HEIGHT)
    glfwShowWindow(window)
}

private fun drawSine() {
    glPointSize(1.0f)
    glColor3f(1.0f, 0.0f, 0.0f)
    var curstep: Int = 1
    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents()

        glClear(GL_COLOR_BUFFER_BIT)
        glBegin(GL_POINTS)
        var x: Double = 0.0
        while (x < WINDOW_SIZE_WIDTH) {
            if (x == 0.0) println("Creating sine waving using the fist $curstep terms of the fourier series...")
            //Well I don't even pretend to understand fourier analysis, but I do know how to do a simple sumation
            //And thats all it takes to do this simple fourier series. Lets have at it. First 6 series only...
            var finaly: Double = 0.0
            for (n in 1..curstep step 2) {
                val rightterm: Double = (n.toDouble() * PI * x)/(WINDOW_SIZE_WIDTH.toDouble()/2.0)
                val finalval: Double = (1.0/n.toDouble())*sin(rightterm)
                finaly += finalval
            }
            glVertex2d(x, finaly)
            x += TIME_STEP

        }
        glEnd()
        glfwSwapBuffers(window)
        curstep += 2
        Thread.sleep(2000)
    }
}

fun main(args: Array<String>) {
    init()
    println("Generating a square wave using the first 6 series of the Fourier series describing a square wave.")
    drawSine()

}