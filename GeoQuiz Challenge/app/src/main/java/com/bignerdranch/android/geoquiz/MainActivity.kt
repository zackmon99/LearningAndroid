package com.bignerdranch.android.geoquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var questionTextView: TextView

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true)
    )

    private var currentIndex = 0
    private var correctAnswers = 0
    private var questionsAnswered = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)

        questionTextView = findViewById(R.id.question_text_view)

        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
            disableTrueFalseButtons()
            questionBank[currentIndex].answered = true
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
            disableTrueFalseButtons()
            questionBank[currentIndex].answered = true
        }

        nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        previousButton.setOnClickListener {
            currentIndex = (currentIndex + questionBank.size - 1) % questionBank.size
            updateQuestion()
        }

        questionTextView.setOnClickListener{
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        updateQuestion()
    }

    private fun disableTrueFalseButtons() {
        trueButton.isEnabled = false
        falseButton.isEnabled = false
    }

    private fun enableTrueFalseButtons() {
        trueButton.isEnabled = true
        falseButton.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onPause (){
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun updateQuestion() {
        val questionTextResId = questionBank[currentIndex].textRexId
        questionTextView.setText(questionTextResId)

        if(questionBank[currentIndex].answered) {
            disableTrueFalseButtons()
        }
        else {
            enableTrueFalseButtons()
        }
    }

    private fun checkAnswer(userAnswer: Boolean) {
        questionsAnswered++
        val correctAnswer = questionBank[currentIndex].answer

        val messageResId = if (userAnswer == correctAnswer) {
            R.string.correct_toast
        }
        else {
            R.string.incorrect_toast
        }

        if (userAnswer == correctAnswer) {
            correctAnswers++
        }


        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        if (questionsAnswered == questionBank.size) {
            val message = "You got " + ((correctAnswers.toDouble() / questionBank.size)*100).toInt() + "% correct!"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}