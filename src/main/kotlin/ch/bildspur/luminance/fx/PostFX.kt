package ch.bildspur.luminance.fx

import processing.core.PApplet
import processing.core.PGraphics
import java.nio.file.Paths
import processing.opengl.PShader

/**
 * Created by cansik on 18.03.17.
 */
class PostFX(val applet : PApplet, var width: Int, var height: Int) {
    private val PASS_NUMBER = 2
    private val SHADER_PATH = Paths.get(applet.sketchPath(), "shader")
    private val resolution: IntArray = intArrayOf(width, height)

    private var passIndex = -1

    // shaders
    private var brightPassShader: PShader? = null
    private var blurShader: PShader? = null
    private var sobelShader: PShader? = null

    // frameBuffer
    private val passBuffers: Array<PGraphics>

    init {

        // init temp pass buffer
        passBuffers = (0 .. PASS_NUMBER).map {
            val g = applet.createGraphics(width, height, PApplet.P2D)
            g.noSmooth()
            g
        }.toTypedArray()

        // load shaders
        loadShaders()
    }

    fun initSize(width: Int, height: Int)
    {
        this.width = width
        this.height = height

        resolution[0] = width
        resolution[1] = height

        (0 .. PASS_NUMBER).forEach {
            val g = applet.createGraphics(width, height, PApplet.P2D)
            g.noSmooth()
            passBuffers[it] = g
        }
    }

    private fun loadShaders() {
        brightPassShader = applet.loadShader(Paths.get(SHADER_PATH.toString(), "brightPassFrag.glsl").toString())
        blurShader = applet.loadShader(Paths.get(SHADER_PATH.toString(), "blurFrag.glsl").toString())
        sobelShader = applet.loadShader(Paths.get(SHADER_PATH.toString(), "sobelFrag.glsl").toString())
    }

    private fun increasePass() {
        passIndex = (passIndex + 1) % passBuffers.size
    }

    private val nextPass: PGraphics
        get() {
            val nextIndex = (passIndex + 1) % passBuffers.size
            return passBuffers[nextIndex]
        }

    private val currentPass: PGraphics
        get() = passBuffers[passIndex]

    private fun clearPass(pass: PGraphics) {
        // clear pass buffer
        pass.beginDraw()
        pass.background(0, 0f)
        pass.resetShader()
        pass.endDraw()
    }

    fun filter(pg: PGraphics): PostFX {
        val pass = nextPass
        clearPass(pass)

        pass.beginDraw()
        pass.image(pg, 0f, 0f)
        pass.endDraw()

        increasePass()
        return this
    }

    fun close(): PGraphics {
        return currentPass
    }

    fun close(result: PGraphics) {
        clearPass(result)

        result.beginDraw()
        result.image(currentPass, 0f, 0f)
        result.endDraw()
    }

    fun brightPass(luminanceTreshold: Float): PostFX {
        val pass = nextPass
        clearPass(pass)

        brightPassShader!!.set("brightPassThreshold", luminanceTreshold)

        pass.beginDraw()
        pass.shader(brightPassShader)
        pass.image(currentPass, 0f, 0f)
        pass.endDraw()

        increasePass()

        return this
    }

    fun blur(blurSize: Int, sigma: Float, horizonatal: Boolean): PostFX {
        val pass = nextPass
        clearPass(pass)

        blurShader!!.set("blurSize", blurSize)
        blurShader!!.set("sigma", sigma)
        blurShader!!.set("horizontalPass", if (horizonatal) 1 else 0)

        pass.beginDraw()
        pass.shader(blurShader)
        pass.image(currentPass, 0f, 0f)
        pass.endDraw()

        increasePass()

        return this
    }

    fun sobel(): PostFX {
        val pass = nextPass
        clearPass(pass)

        sobelShader!!.set("resolution", resolution)

        pass.beginDraw()
        pass.shader(sobelShader)
        pass.image(currentPass, 0f, 0f)
        pass.endDraw()

        increasePass()

        return this
    }
}