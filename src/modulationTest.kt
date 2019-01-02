import org.apache.commons.math3.special.BesselJ
import org.apache.commons.math3.util.CombinatoricsUtils.factorial
import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import java.lang.Math.pow
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

//GL STUFFS
const val WINDOW_SIZE_HEIGHT = 500
const val WINDOW_SIZE_WIDTH = 800
var window: Long = NULL




//Most of the below options can be changed with keyboard shortcuts, these are just starting values
//WAVE STUFFS
var TIME_STEP = 0.001 //how much do we increment our x value each tick. Controls the integrity/continuity of the generated waveform
// CARRIER WAVE VARS
var C_CYCLES: Double = 50.0
var C_PHASE_SHIFT_DEGREES: Double = 0.0 //Amount to shift the wave by in degrees (0-360). Negative number shift left, positive to the right.
// INPUT SIGNAL WAVE VARS
//Amplitude is in percent of screen height (0.0 - 1.0)
var I_AMPLITUDE_PCT = 0.4
var I_CYCLES: Double = 3.0
var I_PHASE_SHIFT_DEGREES: Double = 0.0
//Percent of modulation (the modulation depth) in values between 0.0 and 1.0. Values larger than 1 will produce overmodulation, less than one will be undermodulated.
//Be aware that increasing the modulation depth may require a decrease in the input signal amplitude otherwise you will get clipping at the top and bottom of the window.
var MODULATION_DEPTH: Double = 1.0
//FOURIER SERIES STUFFS
var MAX_FOURIER_ITERS: Int = 10 //Number of iterations we run each fourier series. Higher numbers produce more accurate results up to a point.
var L: Double = WINDOW_SIZE_WIDTH.toDouble()/(I_CYCLES*2)
//Semi-circle Stuffs:
var TRUEBESSEL = false
var TRUEBESSELITERS = 50
//General stuff
var MODULATE = false
var WAVEMODE = 0
//Couldnt figure out a better way to handle phase-shifting our fourier series based signals.
var OTHERPHASESHIFT = 0.0
/*
WAVEMODE 0 = normal sine wave
WAVEMODE 1 = Square wave
WAVEMODE 2 = Sawtooth wave
WAVEMODE 3 = Triangle wave
WAVEMODE 4 = Semi-circle
 */

fun fourierSquare(x: Double): Double {
    var finaly = 0.0
    for (n in 1..MAX_FOURIER_ITERS*2 step 2) {
        val rightterm: Double = (n.toDouble() * PI * x)/L
        val finalval: Double = (1.0/n.toDouble())*sin(rightterm)
        finaly += finalval
    }
    finaly *= 4/PI
    return finaly
}

fun fourierSawtooth(x: Double): Double {
    var finaly = 0.0
    for (n in 1..MAX_FOURIER_ITERS) {
        val rightterm: Double = (n.toDouble() * PI * x)/L
        val finalval: Double = (1.0/n.toDouble())*sin(rightterm)
        finaly += finalval
    }
    finaly = 0.5-(1/PI) * finaly
    return finaly
}

fun fourierTriangle(x: Double): Double {
    var finaly = 0.0
    for (n in 1..MAX_FOURIER_ITERS*2 step 2) {
        val rightterm: Double = (n.toDouble() * PI * x)/L
        val finalval: Double = (pow(-1.0, (n-1.0)/2.0)/pow(n.toDouble(), 2.0))*sin(rightterm)
        finaly += finalval
    }
    finaly *= 8/(PI*PI)
    return finaly
}
//Not an exact solution but the error is reasonably low for values between 0 and 10.
//Things go kaboom after 20 iterations but nothing gets any better after 11 iterations anyway.
//For small values of X we can use the summation series defined in here:
//https://www.boost.org/doc/libs/1_45_0/libs/math/doc/sf_and_dist/html/math_toolkit/special/bessel/bessel.html
//Unfortunately this function isn't enough for a proper semicircle because the values passed to Jv(x) from the
//fourier series are n*PI which breaks down after n=3. We need at least n=20 or n=30 to get things kinda smooth.
//Play with the TRUEBESSELITERS variable to see
fun crappyBesselFuncFirstKind(v: Int, x: Double, iter: Int = 11): Double {
    var answer = 0.0
    for (k in 0..iter) {
        try {
            val numerator = pow(-0.25*x*x, k.toDouble())
            val denominator = factorial(k)*factorial(v+k)
            answer += numerator/denominator.toDouble()
        } catch (e: Exception){
            println("Failed at $k")
            throw e
        }
    }
    return pow(0.5*x, v.toDouble()) * answer
}

fun fourierSemiCircle(x: Double): Double {
    var finaly = 0.0
    val harmonics = when (TRUEBESSEL) {
        true -> TRUEBESSELITERS
        else -> 3
    }
    for (n in 1..harmonics) {
        //Left hand term, (-1^n)*J1(n*pi)/n
        //Our bessel function doesn't return reliable results after ~x>8 so this whole function
        //breaks down after the first 3 harmonics. Not great but I don't think I can improve that
        //without far more advanced knowledge of the maths.
        val term1_num = when (TRUEBESSEL) {
            true -> pow(-1.0, n.toDouble()) * BesselJ.value(1.0, n*PI)
            else -> pow(-1.0, n.toDouble()) * crappyBesselFuncFirstKind(1, n*PI)
        }
        val term1 = term1_num/n

        //Right hand term, cos(pi*n*x/L)
        //I believe this controls the successive harmonics
        val term2_num = n*PI*x
        val term2 = cos(term2_num/L)

        finaly += term1*term2
    }
    finaly += 0.25*PI
    return finaly*L
}

fun drawSine() {
    glClear(GL_COLOR_BUFFER_BIT)
    glPointSize(1.0f)
    glColor3f(1.0f, 0.0f, 0.0f)
    glBegin(GL_POINTS)

    var x = 0.0+OTHERPHASESHIFT
    //carrier wave stuffs
    val carrierfreq = 1.0/(WINDOW_SIZE_WIDTH/C_CYCLES)
    val carrierphaseshift = C_PHASE_SHIFT_DEGREES * PI/180.0
    //input signal wave stuffs
    val signalfreq = 1.0/(WINDOW_SIZE_WIDTH/I_CYCLES)
    val signalphaseshift = I_PHASE_SHIFT_DEGREES * PI/180.0


    var semicirclecorrection = 1.0
    var loopendpoint = WINDOW_SIZE_WIDTH+OTHERPHASESHIFT
    var timestep = TIME_STEP

    //Semi-circle has special requirements
    if (WAVEMODE == 4) {
        loopendpoint = L*2.0
        semicirclecorrection = (WINDOW_SIZE_WIDTH+OTHERPHASESHIFT)/loopendpoint
        //Need the extra resolution when modulating this one
        if (MODULATE) timestep = TIME_STEP/semicirclecorrection
    }

    while (x < loopendpoint) {
        /*
    So the basic formula for a modulated carrier wave is pretty simple
    Its a straight forward multiplication of Fs(t)*Fc(t) where Fs is the signal function and Fc is the carrier function, with t being time or X in our case.
    The value of Fs(t) must be less than 1, otherwise you get modulation artifacts.
    In other words the current amplitude of the signal wave is used to modulate the amplitude of the carrier wave.
    For some reason this still produces artifacts in between cycles. The maths below are exactly as the formulas say they should be so I'm not sure whats going on.
    */

        val moddepth = WINDOW_SIZE_HEIGHT - (MODULATION_DEPTH * WINDOW_SIZE_HEIGHT)
        //Which signal should we modulate?
        var signalamplitude: Double = when (WAVEMODE) {
            1 -> {
                val fs = fourierSquare(x)
                //Getting rid of the lower half of our square wave otherwise the result is just ugly.
                if (fs < 0.0) 0.0
                else fs
            }
            2 -> fourierSawtooth(x)
            3 -> fourierTriangle(x)
            4, 5 -> fourierSemiCircle(x)
            else -> sin(2 * PI * signalfreq * x + signalphaseshift)
        }
        //Dont need to amplify our semicircle
        if (WAVEMODE != 4) signalamplitude *= WINDOW_SIZE_HEIGHT * I_AMPLITUDE_PCT

        //So all the math Ive read says talks about the definition of the modulation index but now how to affect it
        //This is the best I've come up with. Actually its all I've come up with lol.
        if (signalamplitude < 0)
            signalamplitude -= moddepth
        else
            signalamplitude += moddepth

        //Now modulate our chosen signal with our carrier wave.
        val y: Double = when (MODULATE) {
            true -> signalamplitude * sin(2 * PI * carrierfreq * semicirclecorrection * x + carrierphaseshift)
            false -> signalamplitude
        }

        //color right half green
        //if (x > loopendpoint/2) glColor3f(0.0f, 1.0f, 0.0f)

        glVertex2d(x, y)
        x += timestep

    }

    glEnd()
    glfwSwapBuffers(window)
}

@Suppress("UNUSED_PARAMETER")
fun glfwKeypressCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {

    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
        when (key) {
            //Carrier Wave Shortcuts
            GLFW_KEY_UP -> C_CYCLES += 1.0
            GLFW_KEY_DOWN -> {
                C_CYCLES -= 1.0
                if (C_CYCLES < 0) C_CYCLES = 0.0
            }
            GLFW_KEY_LEFT -> C_PHASE_SHIFT_DEGREES += 10.0
            GLFW_KEY_RIGHT -> C_PHASE_SHIFT_DEGREES -= 10.0
            //Input Signal Shortcuts
            GLFW_KEY_KP_8 -> I_CYCLES += 0.1
            GLFW_KEY_KP_2 -> {
                I_CYCLES -= 0.1
                if (I_CYCLES < 0.0) I_CYCLES = 0.0
            }
            GLFW_KEY_KP_4 -> I_PHASE_SHIFT_DEGREES += 1.0
            GLFW_KEY_KP_6 -> I_PHASE_SHIFT_DEGREES -= 1.0
            GLFW_KEY_KP_ADD -> I_AMPLITUDE_PCT += 0.01
            GLFW_KEY_KP_SUBTRACT -> I_AMPLITUDE_PCT -= 0.01
            //General Shortcuts
            GLFW_KEY_1 -> WAVEMODE = 0 // Sine wave
            GLFW_KEY_2 -> WAVEMODE = 1 // Square Wave
            GLFW_KEY_3 -> WAVEMODE = 2 // Sawtooth Wave
            GLFW_KEY_4 -> WAVEMODE = 3 // Triangle Wave
            // Semi-Circle (Our BesselJ Function)
            GLFW_KEY_5 -> {
                WAVEMODE = 4
                TRUEBESSEL = false
            }
            // Semi-Circle (Real BesselJ Function)
            GLFW_KEY_6 -> {
                WAVEMODE = 4
                TRUEBESSEL = true
            }
            GLFW_KEY_M -> MODULATE = MODULATE xor true
            //Fourier Series Shortcuts
            GLFW_KEY_EQUAL -> MAX_FOURIER_ITERS += 1
            GLFW_KEY_MINUS -> {
                MAX_FOURIER_ITERS -= 1
                if (MAX_FOURIER_ITERS < 0) MAX_FOURIER_ITERS = 0
            }
            GLFW_KEY_RIGHT_BRACKET -> MODULATION_DEPTH += 0.01
            GLFW_KEY_LEFT_BRACKET -> MODULATION_DEPTH -= 0.01
            else -> return
        }
        //Reset our ortho matrix if necessary
        if (WAVEMODE == 4) setOrthoMode(1)
        else setOrthoMode(0)
        drawSine()
    }
}

fun setOrthoMode(mode: Int) {
    glLoadIdentity()
    //Best way I could find to phaseshift our fourier-series based signals
    OTHERPHASESHIFT = when {
        WAVEMODE > 0 -> I_PHASE_SHIFT_DEGREES
        else -> 0.0
    }
    when (mode) {
        1 -> {
            L = 2.0
            var t = 2.1
            var b = 0.6
            if (TRUEBESSEL) {
                t = 2.0
                b = 0.0
            }
            GL11.glOrtho(0.0, L*2, b, t, -1.0, 1.0)
        }
        else -> {
            L = WINDOW_SIZE_WIDTH.toDouble()/(I_CYCLES*2) // This controls the frequency of fourier-series based signals
            GL11.glOrtho(0.0+OTHERPHASESHIFT, WINDOW_SIZE_WIDTH.toDouble()+OTHERPHASESHIFT, -WINDOW_SIZE_HEIGHT.toDouble(), WINDOW_SIZE_HEIGHT.toDouble(), -1.0, 1.0)
        }
    }
}

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
    glfwSetKeyCallback(window, ::glfwKeypressCallback)

    // GL configuration comes AFTER we make the window our current context, otherwise errors
    GL.createCapabilities()
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    if (WAVEMODE == 4) setOrthoMode(1)
    else setOrthoMode(0)
    //GL11.glOrtho(0.0, WINDOW_SIZE_WIDTH.toDouble(), -WINDOW_SIZE_HEIGHT.toDouble(), WINDOW_SIZE_HEIGHT.toDouble(), -1.0, 1.0)
    GL11.glViewport(0, 0, WINDOW_SIZE_WIDTH, WINDOW_SIZE_HEIGHT)
    glfwShowWindow(window)
}
fun main(args: Array<String>) {
    init()
    println("Starting Modulation Test...")
    drawSine()
    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents()
        Thread.sleep(50)
    }
}