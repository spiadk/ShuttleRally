package com.rally.badminton

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class RallyEngineTest {
    @Test fun pauseFreezesFlightAndResumeContinues() {
        val g = RallyEngine(Random(4)); g.newMatch(); g.serve(); g.step(.2f)
        val y = g.shuttleY
        g.pause(); repeat(100) { g.step(.02f) }
        assertEquals(y, g.shuttleY, 0f)
        g.resume(); g.step(.1f)
        assertNotEquals(y, g.shuttleY)
    }
    @Test fun earlySwingDoesNotReturnShuttle() {
        val g = RallyEngine(Random(4)); g.newMatch(); g.serve()
        assertFalse(g.hit(true))
        assertEquals(0, g.rally)
    }
    @Test fun wellPositionedReturnStartsOutgoingFlight() {
        val g = RallyEngine(Random(4)); g.difficulty = 2; g.newMatch(); g.serve()
        repeat(400) {
            if (!g.incoming || g.shuttleY < .75f) g.step(.01f)
        }
        assertTrue(g.incoming)
        g.player = g.shuttleX
        assertTrue(g.hit(false))
        assertFalse(g.incoming)
        assertEquals(2, g.rally)
    }
    @Test fun missedReturnsFinishMatchAndRestartClearsScore() {
        val g = RallyEngine(Random(42)); g.difficulty = 2; g.newMatch()
        repeat(20000) {
            if (g.state == RallyEngine.State.READY) g.serve()
            g.step(.02f)
        }
        assertEquals(RallyEngine.State.OVER, g.state)
        assertTrue(g.opponentScore >= 11)
        g.newMatch()
        assertEquals(0, g.playerScore); assertEquals(0, g.opponentScore)
        assertEquals(RallyEngine.State.READY, g.state)
    }
}
