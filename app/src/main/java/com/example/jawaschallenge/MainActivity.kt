package com.example.jawaschallenge

import Connections.FireStore
import Factories.Factory
import Model.Hardware.BatchInfo
import Model.Jewels.Jewel
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

        lifecycleScope.launch {
            Connections.FireStore.chargeDataBase()
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnInsertarUser.setOnClickListener {
            Connections.FireStore.addUser(Factory.createUser())
        }

        binding.btnAddJewelCatalog.setOnClickListener {
            Connections.FireStore.addJewelToCatalog(Factory.createJewel())
        }


        binding.btnShowAllJewels.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getAllJewels()
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = Store.JewelsCatalog.jewelsList.toString()
                        Log.d("Jewels", result.toString())

                    }
                } catch (e: Exception) {
                    Log.e("Jewels", "Error: $e")
                }
            }
        }

        binding.btnInsertarItemInBatch.setOnClickListener {//Recuerda que no podrás insertar items a no se que esté recibido en el virgen
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.addItemToBatch(
                            "DANIEL.MILLER@EXAMPLE.COM",
                            "ed2d4ae1-9513-41d1-8056-dcb26187bf4c",
                            Factory.createItem()
                        )
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
            Log.d("idItemDepues", Constants.Ids.idItem.toString())
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



        binding.btnShowItemsInvertory.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getItemsInventory()
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
            Connections.FireStore.addDonor(Factory.createDonor())
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
                        binding.textView.text = Store.ItemsTypes.allTypesList.toString()
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
                    var info: BatchInfo? = null
                    val result = withContext(Dispatchers.Default) {
                        info = Connections.FireStore.getBatchInfoById(
                            "DANIEL.MILLER@EXAMPLE.COM",
                            "ed2d4ae1-9513-41d1-8056-dcb26187bf4c"
                        )
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

        binding.btnAddType.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.addNewTypeToFirebase(Factory.itemTypes.random())
                    }
                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = result.toString()
                        Log.d("Count", result.toString())
                    }
                } catch (e: Exception) {
                }
            }
        }


//    ------------------------- NUMBER OF COMPONENTS------------------------------- //


        binding.btnEndBatch.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.endBatch(
                            "DANIEL.MILLER@EXAMPLE.COM",
                            "ed2d4ae1-9513-41d1-8056-dcb26187bf4c"
                        )
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = "Batch ended"
                        Log.d("Count", result.toString())
                    }
                } catch (e: Exception) {
                    Log.e("Count", "Error: $e")
                }
            }
        }

        binding.btnInsertarBatch.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.addOrUpdateBatchToDonor(
                            "DANIEL.MILLER@EXAMPLE.COM",
                            Factory.createBatch("DANIEL.MILLER@EXAMPLE.COM")
                        )
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

        binding.btnCheckDoJewel.setOnClickListener {
            lifecycleScope.launch {
                try {
                    var newJewel = FireStore.getJewelByName("GARGANTILLA DE ENGRANAJES")
                    var isPosible = false
                    val result = withContext(Dispatchers.Default) {
                        isPosible = checkIfDoJewelIsPosible(newJewel!!)
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = isPosible.toString()
                        Log.d("Count", result.toString())
                    }
                } catch (e: Exception) {
                    Log.e("Count", "Error: $e")
                }
            }
        }


        binding.btnDoJewel.setOnClickListener {
            lifecycleScope.launch {
                try {

                    val result = withContext(Dispatchers.Default) {
                        var selectedJewel = FireStore.getJewelByName("GARGANTILLA DE ENGRANAJES")
                        FireStore.deleteItemsForJewel(selectedJewel!!)
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

//    ------------------------- TO CHECK THINGS ------------------------------- //
        binding.btnAddItemFireSuelto.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.addItemToFireStore(Factory.createItem())
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




        // END OF ONCREATE
    }
}





suspend fun checkIfDoJewelIsPosible(jewel: Jewel): Boolean {
    var isPosible = false
    var actualInvertoryItems = FireStore.getItemsInventory()
    var quantityOfItems = 0

    for (component in actualInvertoryItems.sumarize) {
        for (componentJewel in jewel.components) {
            if (componentJewel.name == component.name) {
                if (component.quantity >= componentJewel.quantity) {
                    isPosible = true
                    quantityOfItems++
                } else {
                    isPosible = false
                    break
                }
            }
        }
    }
    if(quantityOfItems != jewel.components.size) isPosible = false
    return isPosible
}

fun checkIfNameOfJewelIsUnique(nameJewel: String): Boolean {
    var isUnique = true
    for (jewelInCatalog in Store.JewelsCatalog.jewelsList) {
        if (jewelInCatalog.name == nameJewel) {
            isUnique = false
            break
        }
    }
    return isUnique
}


