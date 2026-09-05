package com.rally.badminton

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

class MainActivity : Activity() {
    private lateinit var game: RallyView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        game = RallyView(this)
        setContentView(game)
    }
    override fun onPause() { super.onPause(); game.suspendGame() }
    override fun onResume() { super.onResume(); game.resumeFrames() }
}
