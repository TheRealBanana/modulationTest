import org.apache.commons.math3.special.BesselJ
import org.apache.commons.math3.util.CombinatoricsUtils.factorial
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import java.lang.Exception
import java.lang.Math.*

/*
    Fourier series for a Semi-Circle, based on the maths described here:
    http://mathworld.wolfram.com/FourierSeriesSemicircle.html

    I tried to implement this once before but came up short implementing the Bessel function.
    I've spent a considerable amount of time since then improving my math skills so I'm trying again.
    Based on the maths described here:
    http://mathworld.wolfram.com/BesselFunctionoftheFirstKind.html
    and here:
    https://www.boost.org/doc/libs/1_45_0/libs/math/doc/sf_and_dist/html/math_toolkit/special/bessel/bessel.html

    My naive implementation is only good between x=0 and around x=8 before it diverges significantly.
    To get a basic approximation we need to be able to handle at least x=PI*20

 */
//Use our crappy implementation of the bessel function or use the true implementation
//Our own implementation breaks down at x values greater than about 8 so its all but useless.
const val TRUEBESSEL = false
const val TRUEBESSELITERS = 50
const val WINDOW_WIDTH: Int = 1280
const val WINDOW_HEIGHT: Int = 720
const val STEP: Double = 0.001
const val L = 2.0

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

fun main(args: Array<String>) {
    //Bounds change dramatically based on the function
    var t = 2.1
    var b = 0.6
    if (TRUEBESSEL) {
        t = 2.0
        b = 0.0
    }
    val window = glinit(WINDOW_WIDTH, WINDOW_HEIGHT, L, b, t, "Fourier Semi-circle")

    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
    GL11.glBegin(GL11.GL_POINTS)
    var x = 0.0
    while (x < 10.0) {
        GL11.glColor3f(1.0f, 0.0f, 0.0f)
        GL11.glVertex2d(x, fourierSemiCircle(x))
        x += STEP
    }
    GL11.glEnd()
    GL11.glFlush()
    while (!GLFW.glfwWindowShouldClose(window)) {
        GLFW.glfwPollEvents()
        Thread.sleep(200)
    }
}


