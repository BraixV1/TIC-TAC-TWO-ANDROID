package com.example.tic_tac_two

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    companion object{
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun startGame(view: View) {
        val button = findViewById<Button>(R.id.Button_StartNewGame)

        val closeCurtains = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0f).apply {
            duration = 300
        }

        val openCurtains = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1f).apply {
            duration = 300
        }

        val animatorSet = AnimatorSet().apply {
            playSequentially(closeCurtains, openCurtains)
        }

        closeCurtains.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Start the GameActivity when the closing animation ends
                val intent = Intent(this@MainActivity, GameActivity::class.java)
                startActivity(intent)
            }
        })

        animatorSet.start()
    }

    fun loadGames(view: View) {
        val button = findViewById<Button>(R.id.Button_LoadGame)

        val closeCurtains = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0f).apply {
            duration = 300
        }

        val openCurtains = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1f).apply {
            duration = 300
        }

        val animatorSet = AnimatorSet().apply {
            playSequentially(closeCurtains, openCurtains)
        }

        closeCurtains.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Start the GameActivity when the closing animation ends
                val intent = Intent(this@MainActivity, LoadGameActivity::class.java)
                startActivity(intent)
            }
        })

        animatorSet.start()
    }
}
