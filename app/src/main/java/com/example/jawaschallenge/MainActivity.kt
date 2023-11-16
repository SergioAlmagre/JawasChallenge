package com.example.jawaschallenge

import Factories.Factory
import Model.Hardware.BatchInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.jawaschallenge.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnAction.setOnClickListener {
            binding.textView.text  = Store.PendingBatches.batchList.toString()
        }


        binding.btnInsertarUser.setOnClickListener {
            Connections.FireStore.registerUser(Factory.createUser())
        }


        binding.btnInsertarJewel.setOnClickListener {
            Connections.FireStore.registerJewel(Factory.createJewel())
        }

        binding.btnInsertarItemInBatch.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.addItemToBatch("EMILY.WILSON@EXAMPLE.COM", 1, Factory.createItem())
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = Store.PendingBatches.batchList.toString()
                        Log.d("ItemsInBatch", result.toString())

                    }
                } catch (e: Exception) {
                    Log.e("ItemsInBatch", "Error: $e")
                }
            }

        }

        binding.btnAllBatches.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getAllPendingBatchesFromUsers()
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = Store.PendingBatches.batchList.toString()
                        Log.d("Batches", result.toString())

                    }
                } catch (e: Exception) {
                    Log.e("Batches", "Error: $e")
                }
            }
        }

        binding.btnAllItems.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getAllItems()
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = Store.ItemsStore.itemsList.toString()
                        Log.d("Count", result.toString())
                    }
                } catch (e: Exception) {
                    Log.e("Count", "Error: $e")
                }
            }


        }



        binding.btnCountItems.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getCountTypesOfItem()
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = result.toString()
                        Log.d("Count", result.toString())

                    }
                } catch (e: Exception) {
                    Log.e("Count", "Error: $e")
                }
            }
        }

        binding.btnInsertDonor.setOnClickListener {
            Connections.FireStore.registerDonor(Factory.createDonor())
        }



        //    ------------------------- DIFFERENT FROM EACH------------------------------- //

        binding.btnDistinType.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getAllDistinctTypes()
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = Store.Types.allTypesList.toString()
                        Log.d("Count", result.toString())
                    }
                } catch (e: Exception) {
                    Log.e("Count", "Error: $e")
                }
            }
        }


        binding.btnInfoBatch.setOnClickListener {
            lifecycleScope.launch {
                try {
                    var info:BatchInfo? = null
                    val result = withContext(Dispatchers.Default) {
                    info =  Connections.FireStore.getBatchInfoById("EMILY.WILSON@EXAMPLE.COM",1)
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = info.toString()
                        Log.d("infoBatch", result.toString())
                    }
                } catch (e: Exception) {
                    Log.e("infoBatch", "Error: $e")
                }
            }
        }





//    ------------------------- NUMBER OF COMPONENTS------------------------------- //




        binding.btnEndBatch.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.endBatch("EMILY.WILSON@EXAMPLE.COM", 1)
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = result.toString()
                        Log.d("Count", result.toString())
                    }
                } catch (e: Exception) {
                    Log.e("Count", "Error: $e")
                }
            }
        }






    }





}