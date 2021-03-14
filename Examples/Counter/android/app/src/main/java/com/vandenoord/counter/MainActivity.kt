package com.vandenoord.counter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vandenoord.counter.databinding.ActivityMainBinding
import dev.strohmnative.strohm.Strohm
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var subscription: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.txtCounter.setOnEditorActionListener { v, _, _ -> onEnterCount(v) }
    }

    fun decrement(@Suppress("UNUSED_PARAMETER") src: View) {
        Strohm.getInstance().dispatch("decrement")
    }

    fun increment(@Suppress("UNUSED_PARAMETER") src: View) {
        Strohm.getInstance().dispatch("increment")
    }

    fun setCounter(count: Int) {
        Strohm.getInstance().dispatch("setCounter", mapOf("count" to count))
    }

    fun reload(@Suppress("UNUSED_PARAMETER") src: View) {
        Strohm.getInstance().reload()
    }

    fun subscribe(@Suppress("UNUSED_PARAMETER") src: View) {
        Strohm.getInstance().subscribe(mapOf("count" to listOf()),
        handler = { props ->
            val count = (props["count"] as Number).toInt()
            runOnUiThread {
                binding.txtCounter.setText("$count")
            }
        },
        completion = { subscription ->
            this.subscription = subscription
        })
    }

    fun unsubscribe(@Suppress("UNUSED_PARAMETER") src: View) {
        subscription?.let { Strohm.getInstance().unsubscribe(it) }
    }

    private fun onEnterCount(v: TextView?): Boolean {
        Log.d("app", "onEditorAction")
        hideSoftKeyBoard()
        v?.clearFocus()
        if (v?.text != null) {
            val count = Integer.valueOf(v.text.toString())
            setCounter(count.toInt())
        }
        return true
    }

    private fun hideSoftKeyBoard() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) {
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}