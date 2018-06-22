import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import java.lang.Math.pow
import kotlin.math.PI
import kotlin.math.sin

//GL STUFFS
const val WINDOW_SIZE_HEIGHT = 500
const val WINDOW_SIZE_WIDTH = 800
var window: Long = NULL

//WAVE STUFFS
const val TIME_STEP = 0.001 //how much do we increment our x value each tick. Controls the integrity/continuity of the generated waveform
// CARRIER WAVE VARS
const val C_CYCLES: Double = 50.0
const val C_PHASE_SHIFT_DEGREES: Double = 0.0 //Amount to shift the wave by in degrees (0-360). Negative number shift left, positive to the right.
// INPUT SIGNAL WAVE VARS
//Amplitude is in percent of screen height (0.0 - 1.0)
const val I_AMPLITUDE_PCT = 0.4
const val I_CYCLES: Double = 3.0
const val I_PHASE_SHIFT_DEGREES: Double = 0.0
//dont mess with this one
const val I_AMPLITUDE = WINDOW_SIZE_HEIGHT * I_AMPLITUDE_PCT
//Percent of modulation (the modulation depth) in values between 0.0 and 1.0. Values larger than 1 will produce overmodulation, less than one will be undermodulated.
//Be aware that increasing the modulation depth may require a decrease in the input signal amplitude otherwise you will get clipping at the top and bottom of the window.
const val MODULATION_DEPTH: Double = 1.0
//FOURIER SERIES STUFFS
const val MAX_FOURIER_ITERS: Int = 20 //Number of iterations we run each fourier series. Higher numbers produce more accurate results up to a point.
const val L: Double = WINDOW_SIZE_WIDTH.toDouble()/(I_CYCLES*2)

fun init(windowSizeW: Int = WINDOW_SIZE_WIDTH, windowSizeH: Int = WINDOW_SIZE_HEIGHT) {
    if ( !glfwInit()) {
        throw Exception("Failed to initialize GLFW.")
    }
    glfwDefaultWindowHints()
    //Do not allow resize
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    window = glfwCreateWindow(windowSizeW, windowSizeH, "Amplitude Modulated Fourier Series Based Signals", 0, 0)
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


private fun fourierSquare(x: Double): Double {
    var finaly = 0.0
    for (n in 1..MAX_FOURIER_ITERS*2 step 2) {
        val rightterm: Double = (n.toDouble() * PI * x)/L
        val finalval: Double = (1.0/n.toDouble())*sin(rightterm)
        finaly += finalval
    }
    finaly *= 4/PI
    return finaly
}

private fun fourierSawtooth(x: Double): Double {
    var finaly = 0.0
    for (n in 1..MAX_FOURIER_ITERS) {
        val rightterm: Double = (n.toDouble() * PI * x)/L
        val finalval: Double = (1.0/n.toDouble())*sin(rightterm)
        finaly += finalval
    }
    finaly = 0.5-(1/PI) * finaly
    return finaly
}

private fun fourierTriangle(x: Double): Double {
    var finaly = 0.0
    for (n in 1..MAX_FOURIER_ITERS*2 step 2) {
        val rightterm: Double = (n.toDouble() * PI * x)/L
        val finalval: Double = (pow(-1.0, (n-1.0)/2.0)/pow(n.toDouble(), 2.0))*sin(rightterm)
        finaly += finalval
    }
    finaly *= 8/(PI*PI)
    return finaly
}

private fun drawSine(mode: Int, modulate: Boolean = true) {
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
            /*
            So the basic formula for a modulated carrier wave is pretty simple
            Its a straight forward multiplication of Fs(t)*Fc(t) where Fs is the signal function and Fc is the carrier function, with t being time or X in our case.
            The value of Fs(t) must be less than 1, otherwise you get modulation artifacts.
            In other words the current amplitude of the signal wave is used to modulate the amplitude of the carrier wave.
            For some reason this still produces artifacts in between cycles. The maths below are exactly as the formulas say they should be so I'm not sure whats going on.
            */
            val moddepth = WINDOW_SIZE_HEIGHT - (MODULATION_DEPTH*WINDOW_SIZE_HEIGHT)
            //Which signal should we modulate?
            var signalamplitude: Double = when(mode) {
                1 -> {
                    val fs = fourierSquare(x)
                    //Getting rid of the lower half of our square wave otherwise the result is just ugly.
                    if (fs < 0.0) 0.0
                    else fs
                }
                2 -> fourierSawtooth(x)
                3 -> fourierTriangle(x)
                else -> sin(2*PI * signalfreq * x + signalphaseshift)
            }
            signalamplitude *= I_AMPLITUDE

            //So all the math Ive read says talks about the definition of the modulation index but now how to affect it
            //This is the best I've come up with. Actually its all I've come up with lol.
            if (signalamplitude < 0)
                signalamplitude -= moddepth
            else
                signalamplitude += moddepth

            //Now modulate our chosen signal with our carrier wave.
            val y: Double = when(modulate) {
                true -> signalamplitude * sin(2 * PI * carrierfreq * x + carrierphaseshift)
                false -> signalamplitude
            }
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
    println("Producing modulated carrier wave...")
    drawSine(1, modulate=true)
    /*
    drawSine(0) = normal sine wave,
    drawSine(1) = Square wave
    drawSine(2) = Sawtooth wave
    drawSine(3) = Triangle wave
    drawSine(mode, modulate=false) shows the raw signal waveform unmodulated
    */
}