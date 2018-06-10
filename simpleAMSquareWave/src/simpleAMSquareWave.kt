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

private fun drawSine() {
    glPointSize(1.0f)
    glColor3f(1.0f, 0.0f, 0.0f)
    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents()
        glBegin(GL_POINTS)
        glClear(GL_COLOR_BUFFER_BIT)
        var amplitude = 0.5
        val cycles = 0.2
        var x: Double = -1000.0

        //only thing I could think of, prolly not the best idea tho
        var last: Double = amplitude * sin(cycles * x)
        var updown = true

        while (x < 1000.0) {
            val y: Double = amplitude * sin(cycles * x)
            glVertex2d(x, y)
            x += 0.01
            //check if we pass zero on the way up
            if (y > 0 && last < 0) {
                if (updown)
                    amplitude += 0.1
                else
                    amplitude -= 0.1
                //only swap direction on the correct cycle
                if (amplitude > 2)
                    updown = false
                else if (amplitude < 0.5)
                    updown = true
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
    println("Generating simple sine wave")
    drawSine()

}