import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
fun glinit(windowSizeW: Int, windowSizeH: Int, L: Double, B: Double, T: Double, windowTitle: String = "Untitled Window"): Long {
    if ( !GLFW.glfwInit()) {
        throw Exception("Failed to initialize GLFW.")
    }
    GLFW.glfwDefaultWindowHints()
    //Do not allow resize
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)
    GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_FALSE)
    val window = GLFW.glfwCreateWindow(windowSizeW, windowSizeH, windowTitle, 0, 0)
    if (window == MemoryUtil.NULL) {
        throw Exception("Failed to initialize window.")
    }
    GLFW.glfwMakeContextCurrent(window)
    // GL configuration comes AFTER we make the window our current context, otherwise errors
    GL.createCapabilities()
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f) //black background
    GL11.glViewport(0, 0, windowSizeW, windowSizeH)
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glOrtho(0.0, L*2, B, T, -1.0, 1.0)
    GLFW.glfwShowWindow(window)
    
    return window
}