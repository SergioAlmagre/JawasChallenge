package Controllers.Donor

import Auxiliaries.InterWindows
import Connections.FireStore
import Constants.Routes
import Controllers.Shared.UserDetails_Controller
import Model.Hardware.Batch
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.jawaschallenge.R
import com.example.jawaschallenge.databinding.ActivityAddBatchBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.IOException


class AddBatch_Controller : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnPoiClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {// End of class AddBatch_Controller
    lateinit var binding: ActivityAddBatchBinding
    private lateinit var firebaseauth: FirebaseAuth
    private val cameraRequest = 1888
    private lateinit var bitmap: Bitmap
    var context = this

    val storage = Firebase.storage
    val storageRef = storage.reference


    private val LOCATION_REQUEST_CODE: Int = 0
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap

    var newBatch: Batch = Batch()
    private var markAdded = false
    private var marcador: Marker? = null

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_batch)

        binding = ActivityAddBatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseauth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        binding.btnRequestPickup.setOnClickListener {
            with(builder) {
                setTitle("Confirmación")
                setMessage("¿Quieres solicitar la recogida?")
                setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    if (newBatch.longitude == null && InterWindows.iwUser.address.isNullOrBlank()) {
                        val innerBuilder = AlertDialog.Builder(this@AddBatch_Controller)
                        innerBuilder.setTitle("Primero debes seleccionar una ubicación")
                        innerBuilder.setMessage("También puedes usar la dirección de tu perfil o llevarlo personalmente al taller")
                        innerBuilder.show()
                    }
                    else {
                        if(!InterWindows.iwUser.address.isNullOrBlank() || binding.chkDefaultAddress.isChecked){
                            newBatch.address = InterWindows.iwUser.address
                        }else{
                            newBatch.address = newBatch.longitude.toString() + "," + newBatch.latitude.toString()
                            Log.d("Location",newBatch.longitude.toString() + "," + newBatch.latitude.toString())
                        }
                        newBatch.userName = InterWindows.iwUser.name
                        newBatch.isClassifed = false
                        newBatch.received = false

                        if (newBatch.picture != Routes.defaultBatchPictureName) {
                            newBatch.picture = newBatch.idBatch
                            uploadPictureOK()
                        }
                        newBatch.aditionalInfo = binding.txtAditionalInfo.text.toString()
                        Log.d("AditionalInfo",binding.txtAditionalInfo.text.toString())
                        Log.d("AditionalInfo",newBatch.aditionalInfo.toString())
                        InterWindows.iwBatch.aditionalInfo = binding.txtAditionalInfo.text.toString()
//                        InterWindows.iwBatch = newBatch

                        runBlocking {
                            val trabajo: Job = launch(context = Dispatchers.Default) {
                                FireStore.addOrUpdateBatchToDonor(InterWindows.iwUser.email, newBatch)

                            }
                            trabajo.join()
                            Toast.makeText(this@AddBatch_Controller, "Solicitud enviada", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                })
                setNegativeButton("No") { dialog: DialogInterface, which: Int ->
                    // Acciones al pulsar "No"
                }
                show()
            }

        }

        binding.btnAddPhotoBatch.setOnClickListener {
            with(builder)
            {
                setTitle("Cual es el origen de la imagen")
                setMessage("Desea hacerla con la cámara o con la galería?")
                setPositiveButton(
                    "Cámara",
                    android.content.DialogInterface.OnClickListener(function = { dialog: DialogInterface, which: Int ->

                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
//                    saveImageToGallery(intent.getStringExtra("mail").toString() + "jpg")

                    })
                )
                setNegativeButton("Galería", ({ dialog: DialogInterface, which: Int ->

                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                }))
                show()
            }
        }


        binding.btnHomeAdmin.setOnClickListener {
            finish()
        }

        binding.btnUserAdmin.setOnClickListener {
            var inte: Intent = Intent(this, UserDetails_Controller::class.java)
            startActivity(inte)
        }

        val checkBox = binding.chkDefaultAddress
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                centerMapOnAddressWithMarker(InterWindows.iwUser.address!!,map,context)

            } else {
//                newBatch.address = newBatch.longitude.toString() + "," + newBatch.latitude.toString()
            }
        }







    }// End of onCreate




    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(37.7749, -122.4194) // Coordenadas de San Francisco, por ejemplo
        googleMap.addMarker(MarkerOptions().position(location).title("Marker in San Francisco"))
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        map = googleMap
        //Se pueden seleccionar varios tiops de mapas:
        //  None --> no muestra nada, solo los marcadores. (MAP_TYPE_NONE)
        //  Normal --> El mapa por defecto. (MAP_TYPE_NORMAL)
        //  Satélite --> Mapa por satélite.  (MAP_TYPE_SATELLITE)
        //  Híbrido --> Mapa híbrido entre Normal y Satélite. (MAP_TYPE_HYBRID) Muestra satélite y mapas de carretera, ríos, pueblos, etc... asociados.
        //  Terreno --> Mapa de terrenos con datos topográficos. (MAP_TYPE_TERRAIN)
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setOnPoiClickListener(this)
        map.setOnMapLongClickListener(this)
        map.setOnMarkerClickListener(this)
        enableMyLocation()  // Habilita la capa de "Mi ubicación"
        map.uiSettings.isMyLocationButtonEnabled = true  // Habilita el botón de "Mi ubicación"
        createMarker()
    }

    //-----------------------------------------------------------------------------------------------------

    /**
     * Método en el que crearemos algunos marcadores de ejemplo.
     */
    private fun createMarker() {
        val markerCIFP = LatLng(38.69332,-4.10860)
        /*
        Los markers se crean de una forma muy sencilla, basta con crear una instancia de un objeto LatLng() que recibirá dos
        parámetros, la latitud y la longitud. Yo en este ejemplo he puesto las coordenadas de mi playa favorita.
        */
        //map.addMarker(MarkerOptions().position(markerCIFP).title("Mi CIFP favorito!"))
        //Si queremos cambiar el color del icono, en este caso azul cyan, con un subtexto.
        val markCIFP = map.addMarker(
            MarkerOptions().position(markerCIFP).title("Taller de joyas").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).snippet("CIFP Virgen de Gracia"))


        //------------ Zoom hacia un marcador ------------
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(markerCIFP, 18f),
            4000,
            null
        )
    }

    /**
     * Con este método vamos a ajustar el tamaño de todos los iconos que usemos en los marcadores.
     */
    fun sizeIcon(idImage:Int): BitmapDescriptor {
        val altura = 60
        val anchura = 60

        var draw = ContextCompat.getDrawable(this,idImage) as BitmapDrawable
        val bitmap = draw.bitmap  //Aquí tenemos la imagen.

        //Le cambiamos el tamaño:
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, anchura, altura, false)
        return BitmapDescriptorFactory.fromBitmap(smallBitmap)

    }

    //----------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (isPermissionsGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    /**
     * función que usaremos a lo largo de nuestra app para comprobar si el permiso ha sido aceptado o no.
     */
    fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Método que solicita los permisos.
     */
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
        }
    }

    //-----------------------------------------------------------------------------------------------------


    /**
     * Nos coloca en la ubicación actual.
     */
    @SuppressLint("MissingPermission")
    private fun irubicacioActual() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val miUbicacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val latLng = LatLng(miUbicacion!!.latitude, miUbicacion.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f)) //--> Mueve la cámara a esa posición, sin efecto. El valor real indica el nivel de Zoom, de menos a más.
    }

    //------------------------------------------------------------------------------------------------------

    /**
     * Dibuja una línea recta desde nuestra ubicación actual al CIFP Virgen de Gracia.
     */
    @SuppressLint("MissingPermission")
    fun pintarRutaAlCentro(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val miUbicacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val latLng = LatLng(miUbicacion!!.latitude, miUbicacion.longitude)
        val markerCIFP = LatLng(38.69332,-4.10860)

        map.addPolyline(PolylineOptions().run{
            add(latLng, markerCIFP)
            color(Color.BLUE)
            width(9f)
        })

        val loc1 = Location("")
        loc1.latitude = latLng.latitude
        loc1.longitude = latLng.longitude
        val loc2 = Location("")
        loc2.latitude = markerCIFP.latitude
        loc2.longitude = markerCIFP.longitude
        val distanceInMeters = loc1.distanceTo(loc2)
        Log.e("Fernando", distanceInMeters.toString())
    }



    //Investigar los polígonos: triángulos...

    //-----------------------------------------------------------------------------------------------------
    //----------------------------------------- Eventos en el mapa ----------------------------------------
    //-----------------------------------------------------------------------------------------------------

    /**
     * Se dispara cuando pulsamos la diana que nos centra en el mapa (punto negro, arriba a la derecha en forma de diana).
     */
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Recentrando", Toast.LENGTH_SHORT).show()
        return false
    }

    /**
     * Se dispara cuando pulsamos en nuestra localización exacta donde estámos ahora (punto azul).
     */
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
        newBatch.latitude = p0.longitude
        newBatch.longitude = p0.latitude
    }

    /**
     * Con el parámetro podremos obtener información del punto de interés. Este evento se lanza cuando pulsamos en un POI.
     */
    override fun onPoiClick(p0: PointOfInterest) {
        Toast.makeText(this@AddBatch_Controller, "Pulsado.", Toast.LENGTH_LONG).show()
        val dialogBuilder = AlertDialog.Builder(this@AddBatch_Controller)
        dialogBuilder.run {
            setTitle("Información del lugar.")
            setMessage("Id: " + p0!!.placeId + "\nNombre: " + p0!!.name + "\nLatitud: " + p0!!.latLng.latitude.toString() + " \nLongitud: " + p0.latLng.longitude.toString())
            setPositiveButton("Aceptar"){ dialog: DialogInterface, i:Int ->
                Toast.makeText(this@AddBatch_Controller, "Salir", Toast.LENGTH_LONG).show()
            }
        }
        dialogBuilder.create().show()
        newBatch.latitude = p0.latLng.longitude
        newBatch.longitude = p0.latLng.latitude
    }

    /**
     * Con el parámetro crearemos un marcador nuevo. Este evento se lanzará al hacer un long click en alguna parte del mapa.
     */
    override fun onMapLongClick(p0: LatLng) {
        if (marcador == null) {
            // Si no hay un marcador, crea uno nuevo
            marcador = map.addMarker(MarkerOptions().position(p0).title("Nuevo marcador"))
            newBatch.latitude = p0.latitude
            newBatch.longitude = p0.longitude
            Toast.makeText(this, "¡Ubicación capturada!", Toast.LENGTH_SHORT).show()

        } else {
            // Si ya hay un marcador, actualiza sus coordenadas
            marcador?.position = p0
            newBatch.latitude = p0.latitude
            newBatch.longitude = p0.longitude
            Toast.makeText(this, "Ubicación actualizada", Toast.LENGTH_SHORT).show()

        }
    }

    /**
     * Este evento se lanza cuando hacemos click en un marcador.
     */
    override fun onMarkerClick(p0: Marker): Boolean {
//        newBatch.latitude = p0.position.longitude
//        newBatch.longitude = p0.position.latitude
//        p0.remove()  //---> Para borrarlo cuando hago click sobre él solo hay que descomentar esto.
//        markAdded = false
        return true;
    }


    fun centerMapOnAddressWithMarker(address: String, map: GoogleMap, context: Context) {
        val geocoder = Geocoder(context)

        try {
            val addressList = geocoder.getFromLocationName(address, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val location = addressList[0]
                val latLng = LatLng(location.latitude, location.longitude)

                // Centrar el mapa en la ubicación
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Agregar un marcador a la ubicación
                map.addMarker(MarkerOptions().position(latLng).title("Ubicación"))
            } else {
                // No se encontraron resultados para la dirección
                // Manejar el caso en consecuencia
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Manejar excepciones de geocodificación aquí
        }
    }


    //------------------------------CAMERA------------------------------------
    //__________________________CAMERA_______________________________
    //Segunda activity para lanzar la cámara.
    val openCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    // La imagen capturada está en el objeto `imageBitmap`
                    this.bitmap = imageBitmap
                    binding.pictureBatch.setImageBitmap(imageBitmap)

                    Log.d("ComprobacionFuera", InterWindows.iwUser.email)

                    Glide.with(this)
                        .asBitmap()
                        .load(result)
                        .apply(
                            RequestOptions().override(500, 500).centerCrop()
                        ) // Cambia las dimensiones según tus necesidades // Con el centerCrop la recortamos cuadrada
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                // Convierte el Bitmap en un ByteArray
                                val outputStream = ByteArrayOutputStream()
                                resource.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    80,
                                    outputStream
                                ) // Cambia la calidad según tus necesidades

                                val byteArray = outputStream.toByteArray()
                                Log.d("ComprobacionDentro", InterWindows.iwUser.toString())
                                // Sube el ByteArray a Firebase Storage
                                val Folder: StorageReference =
                                    FirebaseStorage.getInstance().reference.child(Routes.batchesPicturesPath)
                                val file_name: StorageReference = Folder.child(InterWindows.iwUser!!.email)
                                Log.d("actualizarDocumentoFotoCamara2", InterWindows.iwUser.toString())
                                file_name.putBytes(byteArray)
                                    .addOnSuccessListener { taskSnapshot ->
                                        file_name.downloadUrl.addOnSuccessListener { uri ->
                                            // La imagen se subió correctamente y puedes obtener la URL de descarga
                                            InterWindows.iwUser.picture = InterWindows.iwUser.email

                                            Log.d("actualizarDocumentoFotoCamara3", InterWindows.iwUser.toString()
                                            )
                                        }
                                    }
                            }
                        })

                } else {
                    Log.d("Sergio", "No media selected")
                }
            }
        }


    val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                openCamera.launch(intent)
            } else {
                Log.e("Sergio", "Permiso de cámara no concedido")
            }
        }


    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.pictureBatch.setImageURI(uri)// Coloca la imagen en el pictureBox
            // Redimensiona y comprime la imagen con Glide antes de subirla
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .apply(RequestOptions().override(500, 500).centerCrop()) // Cambia las dimensiones según tus necesidades // Con el centerCrop la recortamos cuadrada
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        // Convierte el Bitmap en un ByteArray
                        val outputStream = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Cambia la calidad según tus necesidades
                        val byteArray = outputStream.toByteArray()

                        // Sube el ByteArray a Firebase Storage
                        val Folder: StorageReference = FirebaseStorage.getInstance().getReference().child(Routes.batchesPicturesPath)
                        val file_name: StorageReference = Folder.child(InterWindows.iwUser!!.email)
                        file_name.putBytes(byteArray)
                            .addOnSuccessListener { taskSnapshot ->
                                file_name.downloadUrl.addOnSuccessListener { uri ->
                                    // La imagen se subió correctamente y puedes obtener la URL de descarga
//                                    Conexion.actualizarDocumento(u!!.mail, u!!.mail)
                                    InterWindows.iwUser.picture = InterWindows.iwUser.email
                                }
                            }
                    }
                })
        } else {
            Log.d("Sergio", "No media selected")
        }
    }


//
//    fun fileDownload(identificador: String) {
//
//        var spaceRef = storageRef.child(Routes.batchesPicturesPath + identificador)
//        val localfile = File.createTempFile(identificador, "jpg")
//        spaceRef.getFile(localfile).addOnSuccessListener {
//            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
//            binding.pictureBatch.setImageBitmap(bitmap)
//        }.addOnFailureListener {
//            Toast.makeText(this, "Algo ha fallado en la descarga", Toast.LENGTH_SHORT).show()
//        }
//    }

    fun uploadPictureOK(){
        binding.pictureBatch.isDrawingCacheEnabled = true
        binding.pictureBatch.buildDrawingCache()
        val bitmap = (binding.pictureBatch.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data2 = baos.toByteArray()

        val imagesRef = storageRef.child(Routes.batchesPicturesPath)
        var pictureName = newBatch.idBatch

        InterWindows.iwUser.picture = InterWindows.iwUser.email

        val uploadTask = imagesRef.child(pictureName).putBytes(data2)
        uploadTask.addOnSuccessListener {

        }.addOnFailureListener{
            //Toast.makeText(this@Registro, "Error en la subida de la imagen", Toast.LENGTH_SHORT).show()
        }
    }




}// End of class AddBatch_Controller