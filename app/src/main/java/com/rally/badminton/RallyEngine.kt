package com.rally.badminton

import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

/** Court coordinates are normalized. Player is at y=.88, opponent at y=.12. */
class RallyEngine(private val random: Random = Random.Default) {
    enum class State { MENU, READY, PLAYING, PAUSED, OVER }
    var state = State.MENU
    var difficulty = 1
    var player = .5f
    var playerTarget = .5f
    var opponent = .5f
    var shuttleX = .5f
    var shuttleY = .88f
    var flight = 0f
    var incoming = false
    var playerScore = 0
    var opponentScore = 0
    var rally = 0
    var bestRally = 0
    var message = "Your court. Your rhythm."
    var smash = false
    var hitSerial = 0
    private var startX = .5f
    private var startY = .88f
    var targetX = .5f
        private set
    private var duration = 1.4f
    private var aiAim = .5f
    private var cooldown = 0f
    private var beforePause = State.READY

    fun newMatch() {
        playerScore = 0; opponentScore = 0; rally = 0
        player = .5f; playerTarget = .5f; opponent = .5f
        ready("Tap SERVE to begin")
    }
    private fun ready(text: String) {
        state = State.READY; message = text; rally = 0
        shuttleX = player; shuttleY = .88f; flight = 0f; cooldown = 0f
    }
    fun pause() {
        if (state == State.PLAYING || state == State.READY) { beforePause = state; state = State.PAUSED }
    }
    fun resume() { if (state == State.PAUSED) state = beforePause }
    fun serve() {
        if (state != State.READY) return
        shuttleX = player; shuttleY = .88f
        state = State.PLAYING
        launch(false, .25f + random.nextFloat() * .5f, false)
        message = "Move into position"
    }
    fun hit(power: Boolean): Boolean {
        if (state != State.PLAYING || !incoming || cooldown > 0f) return false
        cooldown = .18f
        if (shuttleY < .70f || shuttleY > .98f || abs(player - shuttleX) > .17f) {
            message = "Get closer · wait for the ring"; return false
        }
        val aim = if (player < .5f) .78f else .22f
        launch(false, aim, power)
        rally++; bestRally = max(bestRally, rally); hitSerial++
        message = if (power) "SMASH!" else "Beautiful clear"
        return true
    }
    private fun launch(toPlayer: Boolean, aim: Float, power: Boolean) {
        startX = shuttleX; startY = shuttleY; targetX = aim
        incoming = toPlayer; smash = power; flight = 0f
        duration = if (power) .72f else 1.38f
        if (toPlayer) duration = floatArrayOf(1.65f, 1.35f, 1.10f)[difficulty]
        aiAim = aim + (random.nextFloat() - .5f) * floatArrayOf(.24f, .12f, .04f)[difficulty]
    }
    fun step(dt: Float) {
        if (state != State.PLAYING && state != State.READY) return
        player += (playerTarget - player).coerceIn(-dt * 1.5f, dt * 1.5f)
        if (state == State.READY) { shuttleX = player; return }
        cooldown = (cooldown - dt).coerceAtLeast(0f)
        val aim = if (!incoming) aiAim else .5f
        val speed = floatArrayOf(.35f, .53f, .73f)[difficulty]
        opponent = (opponent + (aim - opponent).coerceIn(-dt * speed, dt * speed)).coerceIn(.08f, .92f)
        flight = (flight + dt / duration).coerceAtMost(1f)
        shuttleX = startX + (targetX - startX) * flight
        shuttleY = startY + ((if (incoming) 1.03f else .10f) - startY) * flight
        if (!incoming && flight >= 1f) {
            if (abs(opponent - shuttleX) < .16f) {
                rally++; bestRally = max(bestRally, rally); hitSerial++
                launch(true, .10f + random.nextFloat() * .8f, false)
                message = "Watch the landing ring"
            } else point(true)
        } else if (incoming && flight >= 1f) point(false)
    }
    private fun point(won: Boolean) {
        if (won) playerScore++ else opponentScore++
        if ((max(playerScore, opponentScore) >= 11 && abs(playerScore - opponentScore) >= 2) || max(playerScore, opponentScore) == 15) {
            state = State.OVER
            message = if (playerScore > opponentScore) "Court conquered." else "One more match?"
        } else ready(if (won) "Point to you! · Serve again" else "Point to Club AI · Your serve")
    }
}
