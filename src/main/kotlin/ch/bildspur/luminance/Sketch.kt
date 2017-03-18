package ch.bildspur.luminance

import ch.bildspur.luminance.controller.SyphonController
import ch.bildspur.luminance.util.format
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
        @JvmStatic val FRAME_RATE = 30f

        @JvmStatic val OUTPUT_WIDTH = 500
        @JvmStatic val OUTPUT_HEIGHT = 250

        @JvmStatic val WINDOW_WIDTH = 640
        @JvmStatic val WINDOW_HEIGHT = 500

        @JvmStatic val NAME = "Luminance Filter"

        @JvmStatic var instance = PApplet()

        @JvmStatic fun map(value: Double, start1: Double, stop1: Double, start2: Double, stop2: Double): Double {
            return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
        }
    }

    val syphon = SyphonController(this)

    var fpsOverTime = 0f

    lateinit var output: PGraphics

    lateinit var cp5: ControlP5

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
        syphon.setupSyphon(NAME)

        cp5 = ControlP5(this)
        setupUI()

        // setup output
        output = createGraphics(OUTPUT_WIDTH, OUTPUT_HEIGHT, P2D)
    }

    override fun draw() {
        background(55f)

        if (frameCount < 2) {
            text("starting application...", width / 2 - 50f, height / 2f - 50f)
            return
        }

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
        val h = 400f
        val w = 20f

        cp5.addSlider("threshold")
                .setPosition(w, h)
                .setSize(120, 20)
                .setValue(0f)
                .setRange(0f, 255f)
                .onChange { e ->

                }
    }
}