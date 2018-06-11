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
const val C_CYCLES: Double = 75.0
const val C_PHASE_SHIFT_DEGREES: Double = 0.0 //Amount to shift the wave by in degrees (0-360). Negative number shift left, positive to the right.
// INPUT SIGNAL WAVE VARS
//Amplitude is in percent of screen height (0.0 - 1.0), just modify the last number
const val I_AMPLITUDE_PCT = 0.4
const val I_CYCLES: Double = 3.0
const val I_PHASE_SHIFT_DEGREES: Double = 0.0
//dont mess with this one
const val I_AMPLITUDE = WINDOW_SIZE_HEIGHT * I_AMPLITUDE_PCT
//Percent of modulation (the modulation depth) in values between 0.0 and 1.0. Values larger than 1 will produce overmodulation, less than one will be undermodulated.
//Be aware that increasing the modulation depth may require a decrease in the input signal amplitude otherwise you will get clipping at the top and bottom of the window.
const val MODULATION_DEPTH: Double = 1.0

fun init(windowSizeW: Int = WINDOW_SIZE_WIDTH, windowSizeH: Int = WINDOW_SIZE_HEIGHT) {
    if ( !glfwInit()) {
        throw Exception("Failed to initialize GLFW.")
    }
    glfwDefaultWindowHints()
    //Do not allow resize
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    window = glfwCreateWindow(windowSizeW, windowSizeH, "KtSnake", 0, 0)
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

        var x: Double = 0.0
        //carrier wave stuffs
        val carrierfreq: Double = 1.0/(WINDOW_SIZE_WIDTH/C_CYCLES)
        val carrierphaseshift: Double = C_PHASE_SHIFT_DEGREES * PI/180.0
        //input signal wave stuffs
        val signalfreq: Double = 1.0/(WINDOW_SIZE_WIDTH/I_CYCLES)
        val signalphaseshift: Double = I_PHASE_SHIFT_DEGREES * PI/180.0

        while (x < WINDOW_SIZE_WIDTH) {
            /*So the basic formula for a modulated carrier wave is pretty simple
            Its a straight forward multiplication of Fs(t)*Fc(t) where Fs is the signal function and Fc is the carrier function, with T being time or X in our case.
            In other words the current amplitude of the signal wave is used to modulate the amplitude of the carrier wave.
            For some reason this still produces artifacts in between cycles. The maths below are exactly as the formulas say they should be so I'm not sure whats going on.
            */
            val moddepth = WINDOW_SIZE_HEIGHT - (MODULATION_DEPTH*WINDOW_SIZE_HEIGHT)
            var signalamplitude = I_AMPLITUDE * sin(2*PI * signalfreq * x + signalphaseshift)

            //So all the math Ive read says talks about the definition of the modulation index but now how to affect it
            //This is the best I've come up with. Actually its all I've come up with lol.
            if (signalamplitude < 0)
                signalamplitude -= moddepth
            else
                signalamplitude += moddepth

            val y: Double = signalamplitude * sin(2*PI * carrierfreq * x + carrierphaseshift)
            glVertex2d(x, y)
            x += TIME_STEP

        }
        glEnd()
        glfwSwapBuffers(window)
        Thread.sleep(50)
    }
}

fun main(args: Array<String>) {
    init()
    println("Generating simple sine wave modulated sine wave.")
    drawSine()

}