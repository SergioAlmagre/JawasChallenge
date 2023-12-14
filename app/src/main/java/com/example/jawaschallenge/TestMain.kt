package com.example.jawaschallenge

import Auxiliaries.InterWindows
import Connections.FireStore
import Controllers.Donor.DonorCrud_Controller
import Controllers.Administrator.ItemsType_Controller
import Controllers.Jeweler.JewelsCrud_Controller
import Controllers.Administrator.UserCrud_Controller
import Controllers.Classifier.ClassifierCrud_Controller
import Factories.Factory
import Model.Hardware.BatchInfo
import Model.Jewels.Jewel
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.jawaschallenge.databinding.TestMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestMain: AppCompatActivity() {
    lateinit var binding: TestMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            Connections.FireStore.updateItemsStore()
        }


        binding = TestMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnInsertarUser.setOnClickListener {
            Connections.FireStore.addUser(Factory.createUser("1"))
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
//            lifecycleScope.launch {
//                try {
//                    val result = withContext(Dispatchers.Default) {
//                        Connections.FireStore.addItemToBatch(
//                            "DANIEL.MILLER@EXAMPLE.COM",
//                            "ed2d4ae1-9513-41d1-8056-dcb26187bf4c",
//                            Factory.createItem()
//                        )
//                    }
//
//                    withContext(Dispatchers.Main) {
//                        // Actualizar vistas de la interfaz de usuario aquí
//                        binding.textView.text = InterWindows.iwPendingBatches.toString()
//                        Log.d("ItemsInBatch", result.toString())
//
//                    }
//                } catch (e: Exception) {
//                    Log.e("ItemsInBatch", "Error: $e")
//                }
//            }
        }

        binding.btnAllBatches.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.getAllPendingBatchesFromUsers()
                    }

                    withContext(Dispatchers.Main) {
                        // Actualizar vistas de la interfaz de usuario aquí
                        binding.textView.text = InterWindows.iwPendingBatches.toString()
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
            Connections.FireStore.addUser(Factory.createUser("2"))
        }
        //    ------------------------- DELETE------------------------------- //

        binding.btnDeleteUser.setOnClickListener {
            lifecycleScope.launch {
                val userEmailToDelete = "DANIEL.MILLER@EXAMPLE.COM"
                FireStore.deleteUserByEmail(userEmailToDelete)

            }
        }

        binding.btnDeleteJewel.setOnClickListener {
            lifecycleScope.launch {
                val jewelName = "ANILLO DE RESISTENCIA"
                FireStore.deleteJewelByName(jewelName)
            }
        }

        binding.btnDeleteItemsTypes.setOnClickListener {
            lifecycleScope.launch {
                val typeName = "Altavoces"
                FireStore.deleteItemsTypeByName(typeName)
            }
        }

        binding.btnDeleteItemById.setOnClickListener {
            lifecycleScope.launch {
                val itemId = "982271c6-014b-4f39-889e-ef0a17edfe95"
                FireStore.deleteItemById(itemId)
            }
        }


        //    ------------------------- UPDATES ------------------------------- //

        binding.btnChangeRole.setOnClickListener {
            lifecycleScope.launch {
                val userEmailToUpdate = "AVA.MARTIN@EXAMPLE.COM"
                val newRole = "2"
                FireStore.updateUserRoleByEmail(userEmailToUpdate, newRole)
            }
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
                        info = Connections.FireStore.getBatchInfoByIdAndEmail(
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
//            lifecycleScope.launch {
//                try {
//                    val result = withContext(Dispatchers.Default) {
//                        Connections.FireStore.endBatch(
//                            "DANIEL.MILLER@EXAMPLE.COM",
//                            "ed2d4ae1-9513-41d1-8056-dcb26187bf4c"
//                        )
//                    }
//
//                    withContext(Dispatchers.Main) {
//                        // Actualizar vistas de la interfaz de usuario aquí
//                        binding.textView.text = "Batch ended"
//                        Log.d("Count", result.toString())
//                    }
//                } catch (e: Exception) {
//                    Log.e("Count", "Error: $e")
//                }
//            }
        }

        binding.btnInsertarBatch.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.Default) {
                        Connections.FireStore.addOrUpdateBatchToDonor(
                            "SERGIOALMAGRE@GMAIL.COM",
                            Factory.createBatch("SERGIOALMAGRE@GMAIL.COM")
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

        binding.btnAdminUsers.setOnClickListener {
            var inte: Intent = Intent(this, UserCrud_Controller::class.java)
            startActivity(inte)
        }

        binding.btnViewJewelCatalog.setOnClickListener {
            var inte: Intent = Intent(this, JewelsCrud_Controller::class.java)
            startActivity(inte)
        }

        binding.btnGestType.setOnClickListener {
            var inte: Intent = Intent(this, ItemsType_Controller::class.java)
            startActivity(inte)
        }

        binding.btnDonorCrud.setOnClickListener {
            var inte: Intent = Intent(this, DonorCrud_Controller::class.java)
            startActivity(inte)
        }

        binding.btnClassifierCrud.setOnClickListener {
            var inte: Intent = Intent(this, ClassifierCrud_Controller::class.java)
            startActivity(inte)
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


