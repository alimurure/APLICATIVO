package com.example.aplicativodebus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Configuración de datos estáticos de la unidad
const val CORREO_CONDUCTOR = "conductor@bus.com"
const val PLACA_BUS_ACTUAL = "ABC-123"

enum class RolUsuario {
    NINGUNO,
    PASAJERO,
    CONDUCTOR
}

enum class DiscapacidadTipo(
    val titulo: String,
    val icon: ImageVector,
    val colorBackground: Color
) {
    EMBARAZADA("Embarazada", Icons.Default.PregnantWoman, Color(0xFFE91E63)),
    MAYOR_EDAD("Adulto Mayor", Icons.Default.Elderly, Color(0xFFFF9800)),
    DISCAPACIDAD("Discapacidad Física/Visual/Otra", Icons.Default.Accessible, Color(0xFF2196F3)),
    CON_BEBE("Con Bebé en Brazos/Coche", Icons.Default.ChildFriendly, Color(0xFF9C27B0))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var rolActual by remember { mutableStateOf(RolUsuario.NINGUNO) }
    var usuarioEmail by remember { mutableStateOf("") }

    var conteoEspeciales by remember {
        mutableStateOf(
            mapOf(
                DiscapacidadTipo.EMBARAZADA to 0,
                DiscapacidadTipo.MAYOR_EDAD to 0,
                DiscapacidadTipo.DISCAPACIDAD to 0,
                DiscapacidadTipo.CON_BEBE to 0
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (rolActual) {
                            RolUsuario.CONDUCTOR -> "Panel Conductor (Placa: $PLACA_BUS_ACTUAL)"
                            RolUsuario.PASAJERO -> "Registro de Pasajero"
                            RolUsuario.NINGUNO -> "Acceso al Sistema de Bus"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (rolActual != RolUsuario.NINGUNO) {
                        IconButton(onClick = {
                            rolActual = RolUsuario.NINGUNO
                            usuarioEmail = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (rolActual) {
                RolUsuario.NINGUNO -> PantallaLogin(
                    onLoginExitoso = { email, rol ->
                        usuarioEmail = email
                        rolActual = rol
                    }
                )
                RolUsuario.PASAJERO -> PantallaPasajero(
                    conteoEspeciales = conteoEspeciales,
                    onRegistrar = { tipo ->
                        conteoEspeciales = conteoEspeciales.toMutableMap().apply {
                            this[tipo] = (this[tipo] ?: 0) + 1
                        }
                    },
                    onBajar = { tipo ->
                        conteoEspeciales = conteoEspeciales.toMutableMap().apply {
                            val actual = this[tipo] ?: 0
                            if (actual > 0) {
                                this[tipo] = actual - 1
                            }
                        }
                    }
                )
                RolUsuario.CONDUCTOR -> PantallaConductor(conteoEspeciales = conteoEspeciales)
            }
        }
    }
}

// -------------------------------------------------------------
// PANTALLA DE LOGIN
// -------------------------------------------------------------
@Composable
fun PantallaLogin(onLoginExitoso: (String, RolUsuario) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Bienvenido al Transporte Público",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Ingrese sus credenciales para continuar",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMsg = "" },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = "" },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMsg = "Por favor ingrese correo y contraseña."
                } else {
                    val rol = if (email.trim().lowercase() == CORREO_CONDUCTOR) {
                        RolUsuario.CONDUCTOR
                    } else {
                        RolUsuario.PASAJERO
                    }
                    onLoginExitoso(email, rol)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Iniciar Sesión", fontSize = 16.sp)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Cuentas de prueba:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("• Conductor: $CORREO_CONDUCTOR", fontSize = 12.sp)
                Text("• Pasajero: cualquier otro correo", fontSize = 12.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PANTALLA DEL PASAJERO
// -------------------------------------------------------------
@Composable
fun PantallaPasajero(
    conteoEspeciales: Map<DiscapacidadTipo, Int>,
    onRegistrar: (DiscapacidadTipo) -> Unit,
    onBajar: (DiscapacidadTipo) -> Unit
) {
    var busValidado by remember { mutableStateOf(false) }
    var metodoIngreso by remember { mutableStateOf(0) } // 0: Escaneo QR, 1: Digitar Placa
    var placaIngresada by remember { mutableStateOf("") }
    var errorPlaca by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf("") }
    var asiento by remember { mutableStateOf("") }
    var tieneNecesidadEspecial by remember { mutableStateOf<Boolean?>(null) }
    var tipoSeleccionado by remember { mutableStateOf<DiscapacidadTipo?>(null) }
    var registrado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!busValidado) {
            // Sección para seleccionar método de vinculación a la unidad
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Identifique la Unidad de Bus",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector entre QR y Placa
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = metodoIngreso == 0,
                            onClick = { metodoIngreso = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Escanear QR")
                        }
                        SegmentedButton(
                            selected = metodoIngreso == 1,
                            onClick = { metodoIngreso = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Ingresar Placa")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (metodoIngreso == 0) {
                        // Opción 1: Escaneo de Código QR
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Escanee el código QR exhibido dentro del bus para ingresar automáticamente.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { busValidado = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simular Escaneo QR")
                        }
                    } else {
                        // Opción 2: Ingreso manual por Placa del Bus
                        Icon(
                            imageVector = Icons.Default.Pin,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ingrese el número de placa impreso en los costados o ventanas del vehículo:",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = placaIngresada,
                            onValueChange = {
                                placaIngresada = it.uppercase()
                                errorPlaca = ""
                            },
                            label = { Text("Ejemplo: $PLACA_BUS_ACTUAL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorPlaca.isNotEmpty()) {
                            Text(
                                text = errorPlaca,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (placaIngresada.trim() == PLACA_BUS_ACTUAL) {
                                    busValidado = true
                                } else {
                                    errorPlaca = "Placa no encontrada. Intente con: $PLACA_BUS_ACTUAL"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Validar Placa")
                        }
                    }
                }
            }
        } else if (!registrado) {
            Text(
                "Por favor complete sus datos para abordar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = asiento,
                onValueChange = { asiento = it },
                label = { Text("Número de Asiento (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "¿Tiene alguna discapacidad, condición o atención preferencial?",
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = tieneNecesidadEspecial == true,
                            onClick = { tieneNecesidadEspecial = true },
                            label = { Text("SÍ") }
                        )
                        FilterChip(
                            selected = tieneNecesidadEspecial == false,
                            onClick = {
                                tieneNecesidadEspecial = false
                                tipoSeleccionado = null
                            },
                            label = { Text("NO") }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = tieneNecesidadEspecial == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Seleccione el tipo de condición:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DiscapacidadTipo.values().forEach { tipo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = tipoSeleccionado == tipo,
                                    onClick = { tipoSeleccionado = tipo }
                                )
                                Icon(
                                    imageVector = tipo.icon,
                                    contentDescription = null,
                                    tint = tipo.colorBackground,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(tipo.titulo)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (tieneNecesidadEspecial == true && tipoSeleccionado != null) {
                        onRegistrar(tipoSeleccionado!!)
                    }
                    registrado = true
                },
                enabled = nombre.isNotBlank() && (tieneNecesidadEspecial == false || tipoSeleccionado != null),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Abordaje")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "¡Viaje Registrado!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Pasajero: $nombre", fontSize = 16.sp)

                    if (tipoSeleccionado != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Categoría solicitada: ${tipoSeleccionado?.titulo}",
                            fontWeight = FontWeight.Bold,
                            color = tipoSeleccionado!!.colorBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (tipoSeleccionado != null) {
                                onBajar(tipoSeleccionado!!)
                            }
                            nombre = ""
                            asiento = ""
                            tieneNecesidadEspecial = null
                            tipoSeleccionado = null
                            registrado = false
                            busValidado = false
                            placaIngresada = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voy a Bajarme (Desocupar Asiento)")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANTALLA DEL CONDUCTOR
// -------------------------------------------------------------
@Composable
fun PantallaConductor(conteoEspeciales: Map<DiscapacidadTipo, Int>) {
    val totalEspeciales = conteoEspeciales.values.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tarjeta con Código QR y Placa asignada para los Pasajeros
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Datos de Abordaje para Pasajeros",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Muestre el código QR o proporcione la placa",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Renderizado del Código QR
                GeneradorQRVector(
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Muestra la placa que pueden digitar los pasajeros
                Surface(
                    color = Color(0xFFECEFF1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLACA: $PLACA_BUS_ACTUAL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Resumen General
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (totalEspeciales > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL PERSONAS PREFERENCIALES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "$totalEspeciales",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalEspeciales > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                )
            }
        }

        Text(
            text = "Detalle por Categorías:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        DiscapacidadTipo.values().forEach { tipo ->
            val cantidad = conteoEspeciales[tipo] ?: 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tipo.colorBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = tipo.icon,
                                    contentDescription = null,
                                    tint = tipo.colorBackground,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = tipo.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Badge(
                        containerColor = if (cantidad > 0) tipo.colorBackground else Color.Gray,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "$cantidad",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENTE CANVAS PARA DIBUJAR UN QR
// -------------------------------------------------------------
@Composable
fun GeneradorQRVector(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val gridSize = 15
        val cellSize = sizePx / gridSize

        drawRect(color = Color.White, size = Size(sizePx, sizePx))

        val matrix = Array(gridSize) { BooleanArray(gridSize) }

        fun fillSquare(startR: Int, startC: Int, size: Int) {
            for (r in 0 until size) {
                for (c in 0 until size) {
                    val isBorder = r == 0 || r == size - 1 || c == 0 || c == size - 1
                    val isCenter = r in 2..4 && c in 2..4
                    matrix[startR + r][startC + c] = isBorder || isCenter
                }
            }
        }

        fillSquare(0, 0, 7)
        fillSquare(0, 8, 7)
        fillSquare(8, 0, 7)

        val randomSeed = listOf(
            Pair(2, 7), Pair(3, 7), Pair(5, 7), Pair(7, 2), Pair(7, 3), Pair(7, 5),
            Pair(9, 9), Pair(10, 11), Pair(12, 10), Pair(11, 13), Pair(13, 8),
            Pair(8, 12), Pair(10, 8), Pair(12, 12), Pair(14, 14), Pair(9, 14)
        )
        randomSeed.forEach { (r, c) -> if (r < gridSize && c < gridSize) matrix[r][c] = true }

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (matrix[r][c]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}