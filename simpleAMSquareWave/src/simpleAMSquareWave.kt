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
const val WINDOW_SIZE_WIDTH = 500
var window: Long = NULL

//WAVE STUFFS
//Instead of using a frequency in hz, we are counting the number of full cycles visible on the screen
const val TIME_STEP = 0.001 //how much do we increment our x value each tick. Controls the integrity/continuity of the generated waveform
//Amplitude is in percent of screen height (0.0 - 1.0), just modify the last number
const val AMPLITUDE_PCT = 0.8
const val CYCLES: Double = 50.0
const val PHASE_SHIFT_DEGREES: Double = 0.0 //Amount to shift the wave by in degrees (0-360). Negative number shift left, positive to the right.
//Percent of modulation (the modulation depth) in values between 0.0 and 1.0. Values larger than 1 will produce overmodulation, less than one will be undermodulated.
const val MODULATION_DEPTH: Double = 1.0
//dont mess with this one
const val AMPLITUDE = WINDOW_SIZE_HEIGHT * AMPLITUDE_PCT

fun init(windowSizeW: Int = WINDOW_SIZE_WIDTH, windowSizeH: Int = WINDOW_SIZE_HEIGHT) {
    if ( !glfwInit()) {
        throw Exception("Failed to initialize GLFW.")
    }
    glfwDefaultWindowHints()
    //Do not allow resize
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    window = glfwCreateWindow(windowSizeW, windowSizeH, "Simple Amplitude Modulated Saw Wave (NOT SQUARE lol)", 0, 0)
    if (window == MemoryUtil.NULL) {
        throw Exception("Failed to initialize window.")
    }
    glfwMakeContextCurrent(window)

    // GL configuration comes AFTER we make the window our current context, otherwise errors
    GL.createCapabilities()
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GL11.glOrtho(0.0, WINDOW_SIZE_WIDTH.toDouble(), -WINDOW_SIZE_HEIGHT.toDouble(), WINDOW_SIZE_HEIGHT.toDouble(), -1.0, 1.0)
    GL11.glViewport(0, 0, WINDOW_SIZE_WIDTH, WINDOW_SIZE_HEIGHT)
    glfwShowWindow(window)
}

private fun drawSine() {
    glPointSize(1.0f)
    glColor3f(1.0f, 0.0f, 0.0f)
    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents()
        glBegin(GL_POINTS)
        glClear(GL_COLOR_BUFFER_BIT)

        val timeperiod: Double = WINDOW_SIZE_WIDTH/CYCLES
        val carrierfreq: Double = 1.0/timeperiod
        val phaseshift: Double = PHASE_SHIFT_DEGREES * PI/180.0
        var amplitude: Double = 10.0
        var x: Double = 0.0
        //only thing I could think of, prolly not the best idea tho
        var updown = true
        var last: Double = amplitude * sin(2*PI * carrierfreq * x + phaseshift)
        val ampstep: Double = 2*AMPLITUDE/(WINDOW_SIZE_WIDTH/CYCLES*2.5) // change the leading 2 to increase or decrease the number of cycles. NOT 1:1
        while (x < WINDOW_SIZE_WIDTH) {
            val y: Double = amplitude * sin(2*PI * carrierfreq * x + phaseshift)
            glVertex2d(x, y)
            glVertex2d(x, y)
            x += TIME_STEP
            //check if we pass zero on the way up
            if (y > 0 && last < 0) {
                if (updown)
                    amplitude += ampstep//*ampstep2
                else
                    amplitude -= ampstep//*ampstep2
                //only swap direction on the correct cycle
                if (amplitude > AMPLITUDE)
                    updown = false
                else if (amplitude <= WINDOW_SIZE_HEIGHT - MODULATION_DEPTH*WINDOW_SIZE_HEIGHT + ampstep) {
                    updown = true
                }
            }

            last = y
        }
        glEnd()
        glfwSwapBuffers(window)
        Thread.sleep(50)
    }
}

fun main(args: Array<String>) {
    init()
    println("Generating simple sawtooth modulated sine wave")
    drawSine()

}