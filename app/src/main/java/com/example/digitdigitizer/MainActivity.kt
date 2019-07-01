package com.example.digitdigitizer


import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*


// Interpreter is deprecated in java, but that seems to be the way to do is so ¯\_(ツ)_/¯
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var tflite: Interpreter
    private lateinit var mPaintView: PaintView
    private lateinit var mButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initConstraintLayout()
        
        try {
            tflite = Interpreter(loadModelFile())
        }
        catch (e: Exception) {
            Log.d(TAG, "Loading model failed")
            e.printStackTrace()
        }


    }


    private fun predictDigit(bitmap: Bitmap) : String {
        // Initialize the input and output tensors for the model
        val inputBuffer = arrayOf(Array(28) { Array(28) { floatArrayOf(0f) }})
        val outputArray = Array(1) { FloatArray(11) { 0f }}


        //transfer the alpha values from the bitmap to the inputbuffer
        for ((i,ix) in inputBuffer[0].withIndex()) {
            for (n in ix.indices) {
                val pixelVal = bitmap.getPixel(n,i)
                inputBuffer[0][i][n][0] = if (Color.WHITE != pixelVal) (Color.alpha(pixelVal)).toFloat() else 0f
            }
        }


        // Normalize the data
        // the max alpha value is 255 and the min is 0
        for (ix in inputBuffer[0]) {
            for (iy in ix) {
                iy[0] = (iy[0] - 0f) / 255f
            }
        }

        //Log.d(TAG, "inputBuffer ${Arrays.deepToString(inputBuffer)}")
        //Log.d(TAG, "input tensor shape is ${tflite.getInputTensor(0).shape().toList()} also pixel value ${Color.alpha(bitmap.getPixel(0,4))}")

        tflite.run(inputBuffer, outputArray)

        // Display result in the button, not cool todo display the  results as pictures in the bitmap or smth
        val maxIndex = outputArray[0].indexOf(outputArray[0].max()!!)
        val result = if (outputArray[0].max()!! > 0.75 && maxIndex != 10) "$maxIndex" else "Am I a \rjoke to you??"


        Log.d(TAG, "Result is $maxIndex with a probability of ${outputArray[0].max()} and all probabilities ${Arrays.deepToString(outputArray)}")
        return result
    }


    private fun initConstraintLayout() {

        val conSet = ConstraintSet()

        mButton = Button(this)
        mButton.id = View.generateViewId()
        mButton.text = getString(R.string.btn_predict)

        mPaintView = PaintView(this)
        mPaintView.id = View.generateViewId()



        const_layout.addView(mPaintView)
        const_layout.addView(mButton)


        conSet.constrainWidth(mPaintView.id,ConstraintSet.WRAP_CONTENT)
        conSet.constrainHeight(mPaintView.id,mPaintView.width)
        conSet.constrainHeight(mButton.id,ConstraintSet.WRAP_CONTENT)
        conSet.constrainWidth(mButton.id,ConstraintSet.MATCH_CONSTRAINT)



        conSet.connect(mPaintView.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,150)

        conSet.connect(mButton.id,ConstraintSet.LEFT,ConstraintSet.PARENT_ID,ConstraintSet.LEFT,8)
        conSet.connect(mButton.id,ConstraintSet.RIGHT,ConstraintSet.PARENT_ID,ConstraintSet.RIGHT,8)
        conSet.connect(mButton.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,50)
        //conSet.connect(mButton.id,ConstraintSet.TOP,mPaintView.id,ConstraintSet.TOP, mPaintView.height)

        conSet.applyTo(const_layout)

        mButton.setOnClickListener {
            paintViewClear()
        }

    }


    private fun paintViewClear() {
        val bitmap = mPaintView.getBitmap()
        val scaledBitmap = createScaledBitmap(bitmap,28,28,false)
        val prediction = predictDigit(scaledBitmap)
        mPaintView.mClear()
        mPaintView.setPrediction(prediction)
        Log.d(TAG, "Clear has been called on the bitmap")
    }




    private fun loadModelFile(): MappedByteBuffer {
        Log.d(TAG, "Loadin model")
        val fileDescriptor = assets.openFd("mnist_plus_NotDigitClass.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        Log.d(TAG, "Loaded! model")
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,fileDescriptor.startOffset,fileDescriptor.declaredLength)
    }



    companion object {
        const val TAG = "tf: debugging"
    }
}
