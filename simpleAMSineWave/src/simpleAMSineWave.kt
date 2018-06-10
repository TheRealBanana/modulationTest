import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import kotlin.math.sin


//GL STUFFS
const val WINDOW_SIZE_HEIGHT = 500
const val WINDOW_SIZE_WIDTH = 500
var window: Long = NULL


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
    GL11.glOrtho(-1000.0, 1000.0, -3.0, 3.0, -1.0, 1.0)
    GL11.glViewport(0, 0, WINDOW_SIZE_WIDTH, WINDOW_SIZE_HEIGHT)
    glfwShowWindow(window)
}

//so I dont have to keep changing this in multiple places
//I really dont like this code because it uses so many hard-coded values
//It does, however, produce results.
private fun genSine1(x: Double, amplitude: Double, cycles: Double): Double {return amplitude * sin(cycles * x)}
private fun genSine2(x: Double): Double {return 2*sin(2*x)}

private fun drawSine() {
    glPointSize(1.0f)
    glColor3f(1.0f, 0.0f, 0.0f)
    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents()
        glBegin(GL_POINTS)
        glClear(GL_COLOR_BUFFER_BIT)

        val cycles = 0.005
        var x: Double = -1000.0
        var amplitude = genSine2(x)

        var p = 0.0
        while (x < 1000.0) {
            val y: Double = genSine1(x, amplitude, cycles)
            glVertex2d(x, y)
            x += 0.01
            //Our amplitude is defined by the y-value of a sine wave of a lower frequency than our carrier wave
            amplitude = genSine2(p)
            p += 0.001

        }
        glEnd()
        glfwSwapBuffers(window)
        Thread.sleep(50)
    }
}

fun main(args: Array<String>) {
    init()
    println("Generating simple sine wave")
    drawSine()

}