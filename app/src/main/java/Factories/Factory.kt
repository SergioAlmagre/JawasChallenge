package Factories

import Auxiliaries.ObjectQuantity
import Model.Hardware.Batch
import Model.Hardware.Item
import Model.Jewels.Jewel
import Model.Users.Donor
import Model.Users.User
import kotlin.random.Random

object Factory {


    var itemTypes = mutableListOf(
        "Tarjeta Gráfica",
        "Disco Duro",
        "Portátil",
        "Radio",
        "Procesador",
        "Memoria RAM",
        "Monitor",
        "Placa Base",
        "Altavoces",
        "Impresora",
        "CPU",
        "Módulo de Memoria RAM",
        "SSD",
        "HDD",
        "Placa Madre",
        "Fuente de Alimentación",
        "Ventilador de CPU",
        "Tarjeta de Sonido",
        "Tarjeta de Red",
        "Conector SATA",
        "Conector PCIe",
        "Batería CMOS",
        "Cables de Alimentación",
        "Panel de Control",
        "Botones de Encendido/Apagado",
        "Pantalla LCD",
        "Panel Táctil",
        "Bisagras",
        "Cámara Frontal",
        "Altavoces Internos",
        "Micrófono",
        "Dispositivo de Refrigeración",
        "Sensor de Temperatura",
        "Cabezales de Impresión",
        "Rodillos de Papel",
        "Sistema de Escaneo",
        "Cartuchos de Tinta/Tóner",
        "Bandejas de Papel",
        "Conexiones USB",
        "Sensor Óptico",
        "Rueda de Desplazamiento",
        "Botones Laterales",
        "Batería Recargable",
        "Placa de Circuito Principal",
        "Componentes del Sistema de Carga",
        "Sensor de Ritmo Cardíaco",
        "Pantalla Táctil",
        "Batería Interna",
        "Altavoces Internos",
        "Micrófono Interno",
        "Motor de Vibración",
        "Tarjeta de Memoria",
        "Altavoz",
        "Micrófono",
        "Sensor de Proximidad",
        "Cámara Frontal",
        "Cámara Trasera",
        "Pantalla Táctil",
        "Batería Interna",
        "Bisel de la Pantalla",
        "Tecla Individual",
        "Interruptores",
        "Carcasa del Teclado",
        "Sensor de Luz Ambiental",
        "Conexiones de Red",
        "Conexiones USB",
        "Botones de Control",
        "Tarjeta de Sonido",
        "Batería Interna",
        "Botones de Control",
        "Conectores de Entrada/Salida",
        "Micrófono",
        "Altavoces Integrados",
        "Conectores USB",
        "Botones de Control"
    )


    var observations = mutableListOf(
        "Excelente estado",
        "Algunos arañazos leves",
        "Funciona perfectamente",
        "Necesita reparación",
        "Como nuevo",
        "Se vende por piezas",
        "Requiere actualización de software",
        "Batería agotada",
        "Poco uso",
        "Incluye accesorios originales",
        "Ligeros signos de desgaste",
        "Listo para usar",
        "Sin caja original",
        "Con garantía vigente",
        "Versión actualizada",
        "Falta cable de alimentación",
        "Ideal para proyectos",
        "Para piezas de repuesto",
        "Funciona con algunos problemas",
        "Entrega rápida"
    )

    fun randomObjectQuantity(): ObjectQuantity {
        var objectQuantity = ObjectQuantity(itemTypes.random(), Random.nextInt(1, 3))
        return objectQuantity
    }


    var picturesName = mutableListOf(
        "foto1",
        "foto2",
        "foto3",
        "foto4",
        "foto5",
        "foto6",
        "foto7",
        "foto8",
        "foto9",
        "foto10",
        "foto11",
        "foto12",
        "foto13",
        "foto14",
        "foto15",
        "foto16",
        "foto17",
        "foto18",
        "foto19",
        "foto20"
    )


    var jewelsNames = mutableListOf(
        "Circuito Brillante",
        "Collar de Cables Entrelazados",
        "Aretes de Transistores",
        "Anillo de Resistencia",
        "Pendientes de Placas de Circuito",
        "Broche de Memoria RAM",
        "Colgante de Teclas de Teclado",
        "Gargantilla de Engranajes",
        "Pulsera de Conectores USB",
        "Dije de Tarjeta Gráfica",
        "Sortija de Chips de Procesador",
        "Cadena de Discos Duros Reciclados",
        "Pendientes de Pines de Conexión",
        "Collar de Antiguos Componentes Electrónicos",
        "Anillo de Condensadores",
        "Aretes de LED Luminosos",
        "Colgante de Batería Reciclada",
        "Broche de Botones de Mouse",
        "Pulsera de Cable de Red Reutilizado",
        "Dije de Botellas de Tinta de Impresora"
    )

    var instructions = mutableListOf(
        "1. Toma un circuito antiguo y dale un acabado brillante para el collar 'Circuito Brillante'.",
        "2. Entrelaza cables reciclados para crear el collar 'Collar de Cables Entrelazados'.",
        "3. Utiliza transistores desechados para diseñar unos aros elegantes llamados 'Aretes de Transistores'.",
        "4. Une resistencias en un anillo único con el nombre 'Anillo de Resistencia'.",
        "5. Desmonta placas de circuito inutilizables y crea pendientes llamados 'Pendientes de Placas de Circuito'.",
        "6. Combina memorias RAM viejas para formar un broche de estilo moderno llamado 'Broche de Memoria RAM'.",
        "7. Reutiliza teclas de teclado para fabricar un colgante distintivo: 'Colgante de Teclas de Teclado'.",
        "8. Ensambla engranajes de hardware reciclado para formar una gargantilla llamada 'Gargantilla de Engranajes'.",
        "9. Utiliza conectores USB usados para crear una pulsera única: 'Pulsera de Conectores USB'.",
        "10. Diseña un dije vanguardista utilizando una tarjeta gráfica antigua llamado 'Dije de Tarjeta Gráfica'.",
        "11. Ensarta chips de procesador para formar una sortija futurista: 'Sortija de Chips de Procesador'.",
        "12. Convierte discos duros en una cadena elegante: 'Cadena de Discos Duros Reciclados'.",
        "13. Transforma pines de conexión en aros modernos llamados 'Pendientes de Pines de Conexión'.",
        "14. Mezcla componentes electrónicos antiguos para crear un collar llamado 'Collar de Antiguos Componentes Electrónicos'.",
        "15. Fusiona condensadores en un anillo elegante llamado 'Anillo de Condensadores'.",
        "16. Incorpora LED reciclados en aretes para un toque luminoso: 'Aretes de LED Luminosos'.",
        "17. Utiliza baterías recicladas para fabricar un colgante llamado 'Colgante de Batería Reciclada'.",
        "18. Desmonta botones de mouse y crea un broche único: 'Broche de Botones de Mouse'.",
        "19. Reutiliza cables de red para confeccionar una pulsera llamada 'Pulsera de Cable de Red Reutilizado'.",
        "20. Utiliza botellas de tinta de impresora para crear un dije innovador: 'Dije de Botellas de Tinta de Impresora'."
    )

    var donorsNames = mutableListOf(
        "Juan Pérez",
        "María González",
        "Carlos Rodríguez",
        "Laura Sánchez",
        "Pedro Martínez",
        "Ana López",
        "Alejandro García",
        "Isabel Fernández",
        "Miguel López",
        "Carmen Ramírez",
        "Francisco Díaz",
        "Luisa Torres",
        "Javier Ruiz",
        "Elena Castro",
        "Roberto Flores",
        "Sara Herrera",
        "Daniel Ramos",
        "Verónica Ortiz",
        "Ricardo Vargas",
        "Silvia Mendoza",
        "Empresa XYZ",
        "ABC Soluciones",
        "Inversiones Innovadoras",
        "Grupo Empresarial A&B",
        "Desarrollos Tecnológicos SA",
        "Consultoría Estratégica Global",
        "Servicios Logísticos Veloz",
        "Moda Elegante Boutique",
        "Viajes Mágicos",
        "Construcciones Progresivas",
        "Suministros Industriales del Valle",
        "Creativos Publicidad",
        "Energía Renovable Sostenible",
        "Laboratorio Médico Avanzado",
        "Fábrica de Sueños",
        "Turismo Aventura Extrema",
        "Soluciones Informáticas Integradas",
        "Inversiones Inmobiliarias",
        "EcoReciclaje",
        "Marketing Innovador Internacional"
    )

    val latitudes = mutableListOf(
        37.7749,   // San Francisco, CA
        34.0522,   // Los Angeles, CA
        40.7128,   // New York City, NY
        41.8781,   // Chicago, IL
        29.7604,   // Dallas, TX
        39.9526,   // Denver, CO
        25.7617,   // Miami, FL
        33.4484,   // Atlanta, GA
        32.7767,   // Houston, TX
        39.0997,   // Kansas City, MO
        36.7783,   // Seattle, WA
        45.5051,   // Portland, OR
        38.9072,   // Washington, D.C.
        33.6846,   // Phoenix, AZ
        42.3601,   // Boston, MA
        37.7749,   // San Diego, CA
        29.4241,   // San Antonio, TX
        44.9778,   // Minneapolis, MN
        41.8787,   // Detroit, MI
        36.1699    // Sacramento, CA
    )

    val longitudes = mutableListOf(
        -122.4194, // San Francisco, CA
        -118.2437, // Los Angeles, CA
        -74.0060,  // New York City, NY
        -87.6298,  // Chicago, IL
        -95.3698,  // Dallas, TX
        -104.9903, // Denver, CO
        -80.1918,  // Miami, FL
        -84.4200,  // Atlanta, GA
        -96.7970,  // Houston, TX
        -94.5786,  // Kansas City, MO
        -119.4179, // Seattle, WA
        -122.6750, // Portland, OR
        -77.0370,  // Washington, D.C.
        -112.0476, // Phoenix, AZ
        -71.0589,  // Boston, MA
        -122.4194, // San Diego, CA
        -98.4936,  // San Antonio, TX
        -93.2650,  // Minneapolis, MN
        -83.0458,  // Detroit, MI
        -121.8889  // Sacramento, CA
    )

    val emails = mutableListOf(
        "john.doe@example.com",
        "alice.smith@example.com",
        "bob.jones@example.com",
        "emily.wilson@example.com",
        "michael.robinson@example.com",
        "olivia.davis@example.com",
        "ryan.jackson@example.com",
        "sophia.harris@example.com",
        "daniel.miller@example.com",
        "emma.thomas@example.com",
        "alexander.white@example.com",
        "ava.martin@example.com",
        "christopher.hall@example.com",
        "mia.brown@example.com",
        "nathan.anderson@example.com",
        "olivia.taylor@example.com",
        "james.johnson@example.com",
        "samantha.jenkins@example.com",
        "william.clark@example.com",
        "ella.roberts@example.com"
    )

    val addresses = mutableListOf(
        "123 Main St, Cityville, State, 12345",
        "456 Elm St, Townsville, State, 54321",
        "789 Oak Ave, Villagetown, State, 98765",
        "101 Pine Ln, Hamletville, State, 67890",
        "202 Maple Dr, Countryside, State, 23456",
        "303 Birch Rd, Suburbia, State, 34567",
        "404 Cedar Blvd, Metropolis, State, 87654",
        "505 Spruce Ct, Riverside, State, 76543",
        "606 Redwood Pl, Mountainview, State, 43210",
        "707 Willow Ln, Beachtown, State, 10987",
        "808 Juniper St, Hilltop, State, 21098",
        "909 Aspen Ave, Lakeside, State, 87612",
        "111 Sycamore Dr, Meadowville, State, 54389",
        "222 Magnolia Rd, Parkside, State, 12398",
        "333 Cypress Ln, Downtown, State, 76589",
        "444 Pineapple Blvd, Uptown, State, 23409",
        "555 Oakwood Ave, Midtown, State, 89012",
        "666 Maple Rd, Westside, State, 34509",
        "777 Cherry Pl, Eastend, State, 67801",
        "888 Walnut Ct, Northville, State, 21098"
    )

    val phoneNumbers = mutableListOf(
        "+1 (555) 123-4567",
        "+1 (555) 234-5678",
        "+1 (555) 345-6789",
        "+1 (555) 456-7890",
        "+1 (555) 567-8901",
        "+1 (555) 678-9012",
        "+1 (555) 789-0123",
        "+1 (555) 890-1234",
        "+1 (555) 901-2345",
        "+1 (555) 012-3456",
        "+1 (555) 123-9876",
        "+1 (555) 234-8765",
        "+1 (555) 345-7654",
        "+1 (555) 456-6543",
        "+1 (555) 567-5432",
        "+1 (555) 678-4321",
        "+1 (555) 789-3210",
        "+1 (555) 890-2109",
        "+1 (555) 901-1098",
        "+1 (555) 210-9876"
    )


    fun createBatch(donorName:String):Batch{
        var name = donorName
        var latitud = latitudes.random()
        var longitude = longitudes.random()
        var picture = picturesName.random()
        var address = addresses.random()

        var newBatch = Batch(name,latitud,longitude,address,picture)

        for(i in 0..5)
            newBatch.itemsInside.add(createItem())

        return newBatch
    }


    fun createItem(): Item {

        var itemType = itemTypes.random()
        var obsevation = observations.random()
        var pictureName = picturesName.random()

        var item = Item()
        item.attributes[0].content = itemType
        item.attributes[1].content = obsevation
        item.attributes[2].content = pictureName


        return item
    }


    fun clearStores(){

    }


    fun addDefaultTypes(){
        Store.ItemsTypes.allTypesList = itemTypes
    }


    fun createJewel():Jewel{
        var name = jewelsNames.random()
        var instruction = instructions.random()
        var price = Random.nextDouble(0.5,65.0)

        var newJewel = Jewel(name,instruction,price, picturesName.random())

        for(i in 1..2){
            newJewel.components.add(randomObjectQuantity())
        }

        return newJewel
    }

    fun rolesRandom():String{
        var roles = mutableListOf("1","2","3")
        return roles.random()
    }

    fun createUser(): User {
        val newUser = User(
            name = donorsNames.random(),
            email = emails.random(),
            address = addresses.random(),
            phone = phoneNumbers.random(),
            picture = picturesName.random(),
            role = rolesRandom()
        )
        return newUser
    }

    fun createDonor(): Donor {
        val newDonor = Donor(
            name = donorsNames.random(),
            email = emails.random(),
            address = addresses.random(),
            phone = phoneNumbers.random(),
            picture = picturesName.random(),
            role = "0"
        )

        for (i in 0..5){
            newDonor.addBatch(createBatch(newDonor.name))
        }
        return newDonor
    }



















}