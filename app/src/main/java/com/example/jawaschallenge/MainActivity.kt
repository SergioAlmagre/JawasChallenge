package com.example.jawaschallenge

import Factories.Factory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jawaschallenge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnAction.setOnClickListener {
//            Factories.Factory.fillWarehouses()

            for (i in 1..10)
                Store.JewelsStore.jewelsList.add(Factory.createJewel())

//            binding.txtItem.text = Store.ItemsStore.itemsList.toString()
            binding.textView.text  = Store.JewelsStore.jewelsList.toString()
        }













    }
}