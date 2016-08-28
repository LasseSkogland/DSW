package skogland.lasse.DSW.lwjglwrapper;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import skogland.lasse.DSW.lwjglwrapper.renderer.GL11Renderer;
import skogland.lasse.DSW.lwjglwrapper.renderer.GL21Renderer;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Window {

	private long windowIdentifier = -1;
	private GLCapabilities glCapabilities;

	public void initialize(String title, int width, int height) {
		glfwInit();
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		windowIdentifier = glfwCreateWindow(width, height, title, 0, 0);
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(windowIdentifier, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		glfwMakeContextCurrent(windowIdentifier);
		glfwSwapInterval(1);
		glCapabilities = GL.createCapabilities();
	}

	public Renderer getRenderer() {
		if (glCapabilities.OpenGL21) return new GL21Renderer();
		else return new GL11Renderer();
	}

	public Renderer getRenderer(int RENDER_VERSION) {
		if (RENDER_VERSION == Renderer.OPENGL_11) return new GL11Renderer();
		if (RENDER_VERSION == Renderer.OPENGL_21) return new GL21Renderer();
		else return null;
	}

	public void destroy() {
		glfwDestroyWindow(windowIdentifier);
	}

	public void show() {
		glfwShowWindow(windowIdentifier);
	}

	public boolean update() {
		glfwSwapBuffers(windowIdentifier);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		glfwPollEvents();
		return isClosing();
	}

	public boolean isClosing() {
		return glfwWindowShouldClose(windowIdentifier);
	}

	public void hide() {
		glfwHideWindow(windowIdentifier);
	}

	public long getWindowIdentifier() {
		return windowIdentifier;
	}

	public boolean isKeyPressed(int key) {
		return glfwGetKey(windowIdentifier, key) == GLFW_PRESS;
	}

	public double getMouseX() {
		DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
		glfwGetCursorPos(windowIdentifier, xBuffer, null);
		return xBuffer.get(0);
	}

	public double getMouseY() {
		DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
		glfwGetCursorPos(windowIdentifier, null, yBuffer);
		return yBuffer.get(0);
	}
}
