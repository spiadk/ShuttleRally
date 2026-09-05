package com.rally.badminton

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class RallyView(context: Context) : View(context) {
    private val game = RallyEngine()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val prefs = context.getSharedPreferences("rally", Context.MODE_PRIVATE)
    private var record = prefs.getInt("best", 0)
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var last = 0L
    private var active = true
    private var movingPointer = -1
    private val ink = Color.rgb(12, 28, 31)
    private val lime = Color.rgb(208, 249, 118)
    private val white = Color.rgb(238, 246, 232)
    private val muted = Color.rgb(148, 177, 169)
    private var now = 0f

    init { isFocusable = true; contentDescription = "Badminton court. Drag to move; tap Clear or Smash to return the shuttle." }
    fun suspendGame() { game.pause(); active = false; movingPointer = -1; last = 0L; saveRecord() }
    fun resumeFrames() { active = true; last = 0L; invalidate() }
    private fun saveRecord() {
        if (game.bestRally > record) { record = game.bestRally; prefs.edit().putInt("best", record).apply() }
    }
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val time = System.nanoTime()
        val dt = if (last == 0L) 0f else ((time - last) / 1e9f).coerceAtMost(.04f)
        last = time; now += dt
        if (active) game.step(dt)
        saveRecord()
        c.drawColor(ink)
        scale = min(width / 400f, height / 850f)
        offsetX = (width - 400 * scale) / 2; offsetY = (height - 850 * scale) / 2
        c.save(); c.translate(offsetX, offsetY); c.scale(scale, scale)
        text(c, "RALLY / CLUB", 24f, 39f, 17f, lime, true)
        text(c, "BADMINTON • VOL. 01", 24f, 60f, 9f, muted, true)
        if (game.state != RallyEngine.State.MENU) {
            box(c, 328f, 22f, 376f, 65f, Color.rgb(31, 51, 52))
            text(c, "Ⅱ", 352f, 50f, 23f, white, true, true)
        }
        scoreboard(c)
        court(c)
        if (game.state == RallyEngine.State.MENU) menu(c)
        else {
            text(c, game.message, 200f, 689f, 13f, lime, false, true)
            text(c, "DRAG ON COURT TO MOVE", 200f, 713f, 9f, muted, true, true)
            button(c, 24f, 735f, 194f, 799f, if (game.state == RallyEngine.State.READY) "SERVE" else "CLEAR", false)
            button(c, 206f, 735f, 376f, 799f, "SMASH", true)
            text(c, "HIGH & STEADY", 109f, 821f, 8f, muted, true, true)
            text(c, "FAST & CROSSCOURT", 291f, 821f, 8f, muted, true, true)
        }
        if (game.state == RallyEngine.State.PAUSED || game.state == RallyEngine.State.OVER) overlay(c)
        c.restore()
        if (active) postInvalidateOnAnimation()
    }
    private fun scoreboard(c: Canvas) {
        box(c, 24f, 84f, 376f, 171f, Color.rgb(26, 46, 47))
        text(c, "YOU", 67f, 108f, 10f, lime, true, true)
        text(c, "CLUB AI", 330f, 108f, 10f, muted, true, true)
        text(c, "%02d".format(game.playerScore), 67f, 151f, 37f, white, true, true)
        text(c, "%02d".format(game.opponentScore), 330f, 151f, 37f, white, true, true)
        text(c, "FIRST TO 11", 200f, 120f, 10f, muted, true, true)
        text(c, "RALLY  ${game.rally.toString().padStart(2, '0')}", 200f, 145f, 13f, lime, true, true)
    }
    private fun px(x: Float, y: Float) = 200f + (x - .5f) * (220f + 112f * y)
    private fun py(y: Float) = 235f + y * 390f
    private fun court(c: Canvas) {
        text(c, "SINGLES COURT", 24f, 201f, 9f, muted, true)
        text(c, "BEST RALLY  ${max(record, game.bestRally)}", 376f, 201f, 9f, muted, true, false, true)
        val path = Path().apply { moveTo(px(0f,0f),py(0f)); lineTo(px(1f,0f),py(0f)); lineTo(px(1f,1f),py(1f)); lineTo(px(0f,1f),py(1f)); close() }
        paint.color = Color.rgb(30, 95, 77); paint.style = Paint.Style.FILL; c.drawPath(path, paint)
        for (i in 0..7) {
            val y = i / 8f
            line(c, px(0f,y),py(y),px(1f,y),py(y),Color.argb(16,255,255,255), 1f)
        }
        paint.color = Color.rgb(167, 200, 170); paint.style = Paint.Style.STROKE; paint.strokeWidth = 2f; c.drawPath(path,paint); paint.style = Paint.Style.FILL
        for (x in listOf(.08f,.92f)) line(c,px(x,0f),py(0f),px(x,1f),py(1f),muted,1f)
        for (y in listOf(.08f,.34f,.66f,.92f)) line(c,px(0f,y),py(y),px(1f,y),py(y),muted,1f)
        line(c,px(.5f,0f),py(0f),px(.5f,.34f),py(.34f),muted,1f)
        line(c,px(.5f,.66f),py(.66f),px(.5f,1f),py(1f),muted,1f)
        player(c,game.opponent,.12f,false)
        // Net mesh and tape.
        for (i in 0..22) { val x = 57f + i*13f; line(c,x,408f,x,430f,Color.argb(65,220,240,230),.6f) }
        for (y in listOf(410f,417f,424f,430f)) line(c,55f,y,345f,y,muted,.6f)
        line(c,53f,406f,347f,406f,white,3f)
        line(c,53f,402f,53f,446f,white,3f); line(c,347f,402f,347f,446f,white,3f)
        if (game.incoming && game.state == RallyEngine.State.PLAYING) {
            paint.style=Paint.Style.STROKE; paint.strokeWidth=2f; paint.color=lime
            c.drawOval(px(game.targetX,.88f)-19,py(.88f)-7,px(game.targetX,.88f)+19,py(.88f)+7,paint)
            paint.style=Paint.Style.FILL
        }
        player(c,game.player,.88f,true)
        if (game.state != RallyEngine.State.MENU) {
            val x=px(game.shuttleX,game.shuttleY); val ground=py(game.shuttleY)
            val lift=if(game.state==RallyEngine.State.READY) 36f else 22f+sin(game.flight*PI).toFloat()*(if(game.smash) 35f else 95f)
            paint.color=Color.argb(70,0,0,0); c.drawOval(x-7,ground-3,x+7,ground+3,paint)
            if(game.incoming && game.shuttleY in .70f.. .98f) {
                paint.color=lime; paint.style=Paint.Style.STROKE; paint.strokeWidth=2f
                c.drawCircle(x,ground-lift,17f,paint); paint.style=Paint.Style.FILL
            }
            val feather=Path().apply { moveTo(x,ground-lift); lineTo(x-8,ground-lift-16); lineTo(x+8,ground-lift-16); close() }
            paint.color=white; c.drawPath(feather,paint); paint.color=lime; c.drawCircle(x,ground-lift,3.5f,paint)
        }
    }
    private fun player(c:Canvas,x:Float,y:Float,you:Boolean) {
        val sx=px(x,y); val sy=py(y)
        paint.color=Color.argb(65,0,0,0); c.drawOval(sx-20,sy-5,sx+20,sy+6,paint)
        val color=if(you) lime else Color.rgb(246,168,128)
        line(c,sx-4,sy-13,sx-9,sy,color,5f); line(c,sx+4,sy-13,sx+10,sy,color,5f)
        box(c,sx-10,sy-37,sx+10,sy-12,color,7f)
        paint.color=white; c.drawCircle(sx,sy-47,8f,paint)
        line(c,sx+8,sy-29,sx+25,sy-37,color,4f)
        line(c,sx+25,sy-37,sx+32,sy-48,white,2f)
        paint.color=white; paint.style=Paint.Style.STROKE; paint.strokeWidth=2f
        c.drawOval(sx+26,sy-67,sx+43,sy-47,paint); paint.style=Paint.Style.FILL
    }
    private fun menu(c:Canvas) {
        box(c,40f,301f,360f,585f,Color.argb(242,12,28,31),22f)
        text(c,"FIND YOUR",200f,348f,28f,white,true,true)
        text(c,"RALLY.",200f,391f,43f,lime,true,true)
        text(c,"Drag to move. Time your return.",200f,425f,13f,white,false,true)
        text(c,"Hit when the shuttle ring lights up.",200f,447f,12f,muted,false,true)
        text(c,"CHOOSE YOUR PACE",200f,485f,9f,muted,true,true)
        listOf("EASY","CLUB","PRO").forEachIndexed { i,s ->
            val x=57f+i*98
            box(c,x,502f,x+90,545f,if(game.difficulty==i) lime else Color.rgb(37,59,57),12f)
            text(c,s,x+45,529f,11f,if(game.difficulty==i) ink else white,true,true)
        }
        button(c,24f,735f,376f,799f,"LET’S PLAY  →",true)
        text(c,"11 POINTS · WIN BY 2 · CAP AT 15",200f,824f,9f,muted,true,true)
    }
    private fun overlay(c:Canvas) {
        box(c,0f,75f,400f,850f,Color.argb(225,12,28,31),0f)
        val over=game.state==RallyEngine.State.OVER
        text(c,if(over) "MATCH COMPLETE" else "TAKE A BREATHER",200f,320f,11f,lime,true,true)
        text(c,if(over) game.message else "Paused.",200f,369f,28f,white,true,true)
        text(c,if(over) "${game.playerScore}  :  ${game.opponentScore}" else "Your court will be waiting.",200f,412f,20f,muted,false,true)
        button(c,60f,459f,340f,519f,if(over) "PLAY AGAIN" else "RESUME",true)
        button(c,60f,539f,340f,599f,"MAIN MENU",false)
    }
    override fun onTouchEvent(event:MotionEvent):Boolean {
        val i=event.actionIndex
        val x=(event.getX(i)-offsetX)/scale; val y=(event.getY(i)-offsetY)/scale
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_POINTER_DOWN -> {
                when(game.state) {
                    RallyEngine.State.MENU -> {
                        if(y in 502f..545f && x in 57f..343f) game.difficulty=((x-57)/98).toInt().coerceIn(0,2)
                        if(y in 735f..799f) game.newMatch()
                    }
                    RallyEngine.State.PAUSED,RallyEngine.State.OVER -> {
                        if(x in 60f..340f && y in 459f..519f) { if(game.state==RallyEngine.State.OVER) game.newMatch() else game.resume() }
                        if(x in 60f..340f && y in 539f..599f) game.state=RallyEngine.State.MENU
                    }
                    else -> {
                        if(x>328 && y in 22f..65f) game.pause()
                        else if(y in 735f..799f) {
                            if(game.state==RallyEngine.State.READY) game.serve()
                            else if(game.hit(x>200)) performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        } else if(y in 440f..725f) { movingPointer=event.getPointerId(i); move(x) }
                    }
                }
                performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                val index=event.findPointerIndex(movingPointer)
                if(index>=0) move((event.getX(index)-offsetX)/scale)
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP -> if(event.getPointerId(i)==movingPointer) movingPointer=-1
            MotionEvent.ACTION_CANCEL -> movingPointer=-1
        }
        invalidate(); return true
    }
    private fun move(x:Float) { game.playerTarget=((x-200)/318.56f+.5f).coerceIn(.06f,.94f) }
    override fun performClick():Boolean { super.performClick(); return true }
    private fun box(c:Canvas,l:Float,t:Float,r:Float,b:Float,color:Int,radius:Float=16f) { paint.color=color; paint.style=Paint.Style.FILL; c.drawRoundRect(l,t,r,b,radius,radius,paint) }
    private fun line(c:Canvas,x:Float,y:Float,x2:Float,y2:Float,color:Int,w:Float) { paint.color=color; paint.strokeWidth=w; c.drawLine(x,y,x2,y2,paint) }
    private fun button(c:Canvas,l:Float,t:Float,r:Float,b:Float,label:String,primary:Boolean) { box(c,l,t,r,b,if(primary) lime else Color.rgb(38,60,59)); text(c,label,(l+r)/2,(t+b)/2+5,14f,if(primary) ink else white,true,true) }
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,bold:Boolean=false,center:Boolean=false,right:Boolean=false) {
        paint.color=color; paint.textSize=size; paint.typeface=if(bold) Typeface.create("sans-serif",Typeface.BOLD) else Typeface.create("sans-serif",Typeface.NORMAL)
        paint.textAlign=if(center) Paint.Align.CENTER else if(right) Paint.Align.RIGHT else Paint.Align.LEFT
        c.drawText(s,x,y,paint)
    }
}
