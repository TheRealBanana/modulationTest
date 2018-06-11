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
const val AMPLITUDE = 2
const val CYCLES: Double = 10.0
const val PHASE_SHIFT_DEGREES: Double = 0.0 //Amount to shift the wave by in degrees (0-360). Negative number shift left, positive to the right.



fun init(s: sineObject, windowSizeW: Int = WINDOW_SIZE_WIDTH, windowSizeH: Int = WINDOW_SIZE_HEIGHT) {
    if ( !glfwInit()) {
        throw Exception("Failed to initialize GLFW.")
    }
    glfwDefaultWindowHints()
    //Do not allow resize
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE)
    window = glfwCreateWindow(windowSizeW, windowSizeH, "SimpleSine", 0, 0)
    if (window == MemoryUtil.NULL) {
        throw Exception("Failed to initialize window.")
    }
    glfwMakeContextCurrent(window)

    //Key callbacks
    glfwSetKeyCallback(window, s::glfwKeypressCallback)

    // GL configuration comes AFTER we make the window our current context, otherwise errors
    GL.createCapabilities()
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GL11.glOrtho(0.0, WINDOW_SIZE_WIDTH.toDouble(), -3.0, 3.0, -1.0, 1.0)
    GL11.glViewport(0, 0, WINDOW_SIZE_WIDTH, WINDOW_SIZE_HEIGHT)
    glfwShowWindow(window)
}
//Gotta go object so we can affect variable with callbacks more easily
class sineObject {
    var cycles = CYCLES
    var timeperiod: Double = WINDOW_SIZE_WIDTH / cycles
    var carrierfreq: Double = 1.0 / timeperiod
    var phaseshiftdegrees: Double = PHASE_SHIFT_DEGREES
    var phaseshift: Double = phaseshiftdegrees * PI / 180.0
    var amplitude: Double = AMPLITUDE.toDouble()

    private fun updateFreq(amount: Double) {
        cycles += amount
        timeperiod = WINDOW_SIZE_WIDTH / cycles
        carrierfreq = 1.0/timeperiod
    }

    private fun updatePhaseShift(amount: Double) {
        phaseshiftdegrees += amount
        phaseshift = phaseshiftdegrees * PI / 180.0
    }

    private fun updateAmplitude(amount: Double) {
        amplitude += amount
        if (amplitude < 0.0) amplitude = 0.0
    }

    private fun resetAll() {
        cycles = CYCLES
        timeperiod = WINDOW_SIZE_WIDTH / cycles
        carrierfreq = 1.0 / timeperiod
        phaseshiftdegrees = PHASE_SHIFT_DEGREES
        phaseshift = phaseshiftdegrees * PI / 180.0
        amplitude = AMPLITUDE.toDouble()
    }

    fun glfwKeypressCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (action == GLFW_PRESS || action == GLFW_REPEAT)
            when (key) {
                GLFW_KEY_UP -> updateAmplitude(0.05)
                GLFW_KEY_DOWN -> updateAmplitude(-0.05)
                GLFW_KEY_LEFT -> updatePhaseShift(10.0)
                GLFW_KEY_RIGHT -> updatePhaseShift(-10.0)
                GLFW_KEY_KP_ADD -> updateFreq(0.05)
                GLFW_KEY_KP_SUBTRACT -> updateFreq(-0.05)
                GLFW_KEY_KP_0 -> resetAll()
            }
    }

    fun drawSine() {
        glPointSize(1.0f)
        glColor3f(1.0f, 0.0f, 0.0f)
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents()
            glBegin(GL_POINTS)
            glClear(GL_COLOR_BUFFER_BIT)

            var x: Double = 0.0
            while (x < WINDOW_SIZE_WIDTH) {
                val y: Double = amplitude * sin(2 * PI * carrierfreq * x + phaseshift)
                glVertex2d(x, y)
                x += TIME_STEP
            }
            glEnd()
            //glfwSwapBuffers(window)
            glFlush()
            glClear(GL_COLOR_BUFFER_BIT) //Fixes weird double rendering issues we were having. Would rather just using single bffering.
            Thread.sleep(10)
            glfwPollEvents()
        }
    }
}
fun main(args: Array<String>) {
    val s = sineObject()
    init(s)
    println("Generating simple sine wave")
    s.drawSine()
}