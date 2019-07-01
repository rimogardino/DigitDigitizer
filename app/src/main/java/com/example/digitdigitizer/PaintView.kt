package com.example.digitdigitizer

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import android.graphics.Bitmap
import android.os.Build

//todo comments maybe??
class PaintView(context: Context) : View(context) {
    private lateinit var mCanvas: Canvas
    private lateinit var mBitmap: Bitmap
    private val mPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 65f
        isAntiAlias = true
        alpha = 255
        maskFilter = BlurMaskFilter(15f,BlurMaskFilter.Blur.SOLID)
    }

    private val mTextPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 25f
        isAntiAlias = true
        alpha = 255
        textSize = 450f
    }
    private var path = Path()
    private var mPaths = Path()
    private var mX = 0f
    private var mY = 0f
    private var mW: Int = 0
    private var mH: Int = 0



    init {



        setOnTouchListener { _, event ->
            when (event.actionMasked) {

                MotionEvent.ACTION_MOVE -> {
                    val dx = abs(event.x - mX)
                    val dy = abs(event.y - mY)

                    if (dx > 4 || dy > 4) {
                        path.lineTo(event.x, event.y)
                        //path.quadTo(mX, mY, (event.x + mX) / 2, (event.y + mY) / 2)
                        mX = event.x
                        mY = event.y
                        Log.d(Tag, "Movement detected ${event.downTime}")
                    }
                    invalidate()
                    true
                }
                MotionEvent.ACTION_DOWN -> {
                    path.reset()
                    path.moveTo(event.x, event.y)
                    invalidate()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    path.lineTo(event.x,event.y)
                    mPaths.addPath(path)
                    //mClear()
                    invalidate()
                    true
                }


                else -> true
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()

        if (!mPaths.isEmpty || !path.isEmpty) {
            mCanvas.drawColor(Color.WHITE)
            mCanvas.drawPath(path, mPaint)
            mCanvas.drawPath(mPaths, mPaint)
        }
        Log.d(Tag,"onDraw called")

        canvas.drawBitmap(mBitmap, 0f, 0f, mPaint)
        canvas.restore()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mW = w
        mH = w
        mBitmap = Bitmap.createBitmap(mW+16,mH+16, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        mCanvas.drawColor(Color.WHITE)
    }

    fun getBitmap(): Bitmap {
        return mBitmap
    }


    fun setPrediction(answer: String) {
        val answerLen = answer.length
        mCanvas.drawColor(Color.WHITE)
        mTextPaint.textSize = if (answerLen == 1) 1150f else 100f
        mCanvas.drawText(answer,mW/2f-(mTextPaint.textSize*answerLen/4f),mH/2f+(mTextPaint.textSize/3f),mTextPaint)
        invalidate()
    }


    fun mClear() {
        mPaths.reset()
        path.reset()
        invalidate()

        Log.d(Tag, "mClear has been called")
    }


    companion object {
        const val Tag = "MyView debugger"

    }



}


