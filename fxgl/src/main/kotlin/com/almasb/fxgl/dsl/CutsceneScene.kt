/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.dsl

import com.almasb.fxgl.animation.Animation
import com.almasb.fxgl.app.SceneStack
import com.almasb.fxgl.cutscene.Cutscene
import com.almasb.fxgl.cutscene.CutsceneDialogLine
import com.almasb.fxgl.dsl.FXGL.Companion.centerTextBind

import com.almasb.fxgl.input.UserAction
import com.almasb.fxgl.scene.Scene
import com.almasb.fxgl.scene.SubScene
import javafx.geometry.Point2D
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration
import java.util.*

/**
 * TODO: This only temporarily lives here. Once SubScene moves to core, this will move to fxgl-cutscene
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class CutsceneScene(private val sceneStack: SceneStack, appWidth: Int, appHeight: Int) : SubScene() {

    private val animation: Animation<*>
    private val animation2: Animation<*>

    private val textRPG = Text()

    private lateinit var cutscene: Cutscene

    init {
        val topLine = Rectangle(appWidth.toDouble(), 150.0)
        topLine.translateY = -150.0

        val botLine = Rectangle(appWidth.toDouble(), 200.0)
        botLine.translateY = appHeight.toDouble()

        contentRoot.children.addAll(topLine, botLine)

        animation = animationBuilder()
                .duration(Duration.seconds(0.5))
                .translate(topLine)
                .from(Point2D(0.0, -150.0))
                .to(Point2D.ZERO)
                .build()

        animation2 = animationBuilder()
                .duration(Duration.seconds(0.5))
                .translate(botLine)
                .from(Point2D(0.0, appHeight.toDouble()))
                .to(Point2D(0.0, appHeight.toDouble() - 200.0))
                .build()

        textRPG.fill = Color.WHITE
        textRPG.font = Font.font(18.0)
        textRPG.wrappingWidth = appWidth.toDouble() - 50.0
        textRPG.translateX = 50.0
        textRPG.translateY = appHeight.toDouble() - 100.0

        centerTextBind(textRPG, appWidth / 2.0, appHeight - 100.0)

        contentRoot.children.addAll(textRPG)

        input.addAction(object : UserAction("Next RPG Line") {
            override fun onActionBegin() {
                nextLine()
            }
        }, KeyCode.ENTER)
    }

    override fun onEnter(prevState: Scene) {
        animation2.onFinished = Runnable {
            onOpen()
        }

        animation.start()
        animation2.start()
    }

    override fun onUpdate(tpf: Double) {
        animation.onUpdate(tpf)
        animation2.onUpdate(tpf)

        if (message.isNotEmpty()) {
            textRPG.text += message.poll()
        }
    }

    internal fun endCutscene() {
        animation2.onFinished = Runnable {
            sceneStack.popSubScene()
            onClose()
        }
        animation.startReverse()
        animation2.startReverse()
    }

    fun start(cutscene: Cutscene) {
        this.cutscene = cutscene

        nextLine()

        sceneStack.pushSubScene(this)
    }

    private var currentLine = 0
    private lateinit var dialogLine: CutsceneDialogLine
    private val message = ArrayDeque<Char>()

    private fun nextLine() {
        // do not allow to move to next line while the text animation is going
        if (message.isNotEmpty())
            return

        if (currentLine < cutscene.lines.size) {
            dialogLine = cutscene.lines[currentLine]
            dialogLine.data.forEach { message.addLast(it) }

            textRPG.text = dialogLine.owner + ": "
            currentLine++
        } else {
            endCutscene()
        }
    }

    fun onOpen() {

    }

    fun onClose() {
        currentLine = 0
        message.clear()
    }
}