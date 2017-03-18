package ch.bildspur.luminance

import ch.bildspur.luminance.controller.SyphonController
import ch.bildspur.luminance.fx.PostFX
import ch.bildspur.luminance.util.format
import ch.bildspur.luminance.util.imageRect
import controlP5.ControlP5
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.opengl.PJOGL

/**
 * Created by cansik on 04.02.17.
 */
class Sketch : PApplet() {
    companion object {
        @JvmStatic val FRAME_RATE = 60f

        @JvmStatic val OUTPUT_WIDTH = 100
        @JvmStatic val OUTPUT_HEIGHT = 100

        @JvmStatic val WINDOW_WIDTH = 400
        @JvmStatic val WINDOW_HEIGHT = 400

        @JvmStatic val NAME = "Luminance Filter"

        @JvmStatic var instance = PApplet()

        @JvmStatic fun map(value: Double, start1: Double, stop1: Double, start2: Double, stop2: Double): Double {
            return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
        }
    }

    val syphon = SyphonController(this)

    var fpsOverTime = 0f

    lateinit var outputCanvas: PGraphics

    lateinit var cp5: ControlP5

    lateinit var fx : PostFX

    var brightPass = 0.8f
    var blur = 0
    var sigma = 1f
    var brightness = 0.0f

    init {

    }

    override fun settings() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT, PConstants.P2D)
        PJOGL.profile = 1
    }

    override fun setup() {
        instance = this

        smooth()
        frameRate(FRAME_RATE)

        surface.setTitle(NAME)
        syphon.setupSyphonOutput(NAME)
        syphon.setupSyphonInput()

        cp5 = ControlP5(this)
        setupUI()

        fx = PostFX(this, OUTPUT_WIDTH, OUTPUT_HEIGHT)
        outputCanvas = createGraphics(OUTPUT_WIDTH, OUTPUT_HEIGHT, PConstants.P2D)
    }

    override fun draw() {
        background(55f)

        if (frameCount < 2) {
            text("starting application...", width / 2 - 50f, height / 2f - 50f)
            return
        }

        // do luminance filter
        val input = syphon.getGraphics()

        // check if input size changed
        if(input.width != fx.width || input.height != fx.height) {
            fx.initSize(input.width, input.height)
            outputCanvas = createGraphics(input.width, input.height, P2D)
        }


        // create filtered image
        fx.filter(input)
                .brightPass(brightPass)
                .blur(blur, sigma, false)
                .blur(blur, sigma, true)
                .luminosity(brightness)
                .close(outputCanvas)

        syphon.sendImageToSyphon(outputCanvas)


        // draw output
        g.imageRect(outputCanvas, 0f, 0f, 400f, 280f)
        cp5.draw()
        drawFPS()
    }

    fun drawFPS() {
        // draw fps
        fpsOverTime += frameRate
        val averageFPS = fpsOverTime / frameCount.toFloat()

        textAlign(LEFT, BOTTOM)
        fill(255)
        text("FPS: ${frameRate.format(2)}\nFOT: ${averageFPS.format(2)}", 10f, height - 5f)
    }

    fun setupUI() {
        val h = 295f
        val w = 20f

        val w2 = 200f

        cp5.addSlider("bright pass")
                .setPosition(w, h)
                .setSize(120, 15)
                .setValue(brightPass)
                .setRange(0f, 1f)
                .onChange { e ->
                    brightPass = e.controller.value
                }

        cp5.addSlider("blur")
                .setPosition(w, h + 25)
                .setSize(120, 15)
                .setValue(blur.toFloat())
                .setRange(0f, 50f)
                .onChange { e ->
                    blur = e.controller.value.toInt()
                }

        cp5.addSlider("sigma")
                .setPosition(w, h + 50)
                .setSize(120, 15)
                .setValue(sigma)
                .setRange(1f, 50f)
                .onChange { e ->
                    sigma = e.controller.value
                }

        cp5.addSlider("luminosity")
                .setPosition(w2, h)
                .setSize(120, 15)
                .setValue(brightness)
                .setRange(0f, 1f)
                .onChange { e ->
                    brightness = e.controller.value
                }
    }
}