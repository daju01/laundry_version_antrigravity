package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundManager
import com.example.sim.LevelUpCelebration
import com.example.ui.scene.BlenderMesh
import com.example.ui.scene.BlenderModelFactory
import com.example.ui.scene.BlenderRenderer
import com.example.ui.scene.Vec3
import kotlin.math.*

/**
 * Dynamic 3D Isometric Workshop Expansion Stage with Blender-Style PBR Lighting and Meshes.
 * Renders high-fidelity 3D models with true polygonal topology, Phong shading,
 * rotating drum portholes, and dynamic expansion animation when capacity increases (e.g. 20kg -> 40kg).
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun CapacityUpgrade3DTransition(
    celebration: LevelUpCelebration,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Interactive user rotation around the 3D model
    var userRotationDeg by remember { mutableStateOf(0f) }
    var userPitchDeg by remember { mutableStateOf(30f) }
    var wireframeMode by remember { mutableStateOf(false) }
    var replayTrigger by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(!GameSoundManager.isSoundEnabled) }

    // Multi-phase dynamic expansion timeline
    val animProgress = remember { Animatable(0f) }
    val cameraZoom = remember { Animatable(0.75f) }
    val shockwaveAnim = remember { Animatable(0f) }
    val drumSpinAnim = remember { Animatable(0f) }

    LaunchedEffect(celebration, replayTrigger) {
        GameSoundManager.playCapacityUpgradeFanfare()

        animProgress.snapTo(0f)
        cameraZoom.snapTo(0.70f)
        shockwaveAnim.snapTo(0f)
        drumSpinAnim.snapTo(0f)

        // Phase 1: Camera zoom and expansion swell
        cameraZoom.animateTo(
            targetValue = 1.08f,
            animationSpec = tween(1100, easing = FastOutSlowInEasing)
        )
        // Phase 2: Room expansion animation (0f to 1f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        GameSoundManager.play3DExpansionWhoosh()

        // Settle camera into majestic vantage point
        cameraZoom.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(500, easing = LinearOutSlowInEasing)
        )

        // Phase 3: Energy floor shockwaves
        shockwaveAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(900, easing = EaseOutQuad)
        )
    }

    // Continuous drum spin & ambient bounce
    val infiniteTransition = rememberInfiniteTransition(label = "blender_3d_loop")
    val drumSpinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drum_spin"
    )
    val laserGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_glow"
    )
    val idleFloatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_float"
    )

    // Current animated capacity value
    val currentCapKg = remember(animProgress.value) {
        val oldCap = celebration.oldCapacityKg
        val newCap = celebration.newCapacityKg
        oldCap + ((newCap - oldCap) * animProgress.value)
    }

    // Pre-build Blender Meshes with Subdivision and Chamfers
    val baselineWasherMesh = remember {
        BlenderModelFactory.createChamferedWasherMesh(
            width = 30f,
            depth = 30f,
            height = 36f,
            bodyColor = Color(0xFFF1F5F9),
            topColor = Color(0xFFE2E8F0),
            accentColor = Color(0xFF38BDF8)
        )
    }
    val commercialWasherMesh = remember {
        BlenderModelFactory.createChamferedWasherMesh(
            width = 38f,
            depth = 38f,
            height = 46f,
            bodyColor = Color(0xFF0284C7),
            topColor = Color(0xFF38BDF8),
            accentColor = Color(0xFFFDE047)
        )
    }
    val dryerGasMesh = remember {
        BlenderModelFactory.createBlenderDryerMesh(
            width = 32f,
            depth = 32f,
            height = 38f,
            primaryColor = Color(0xFFEF4444)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(Color(0xFF38BDF8), Color(0xFFF59E0B), Color(0xFF10B981))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("capacity_upgrade_3d_stage")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        userRotationDeg += dragAmount.x * 0.5f
                        userPitchDeg = (userPitchDeg + dragAmount.y * 0.25f).coerceIn(15f, 65f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawBlenderIsometricWorkshop(
                    w = w,
                    h = h,
                    progress = animProgress.value,
                    zoom = cameraZoom.value,
                    userRotDeg = userRotationDeg,
                    userPitchDeg = userPitchDeg,
                    drumSpin = drumSpinAngle,
                    shockwaveProgress = shockwaveAnim.value,
                    laserGlow = laserGlow,
                    idleFloatY = idleFloatY,
                    wireframeMode = wireframeMode,
                    oldCap = celebration.oldCapacityKg,
                    newCap = celebration.newCapacityKg,
                    currentCap = currentCapKg,
                    baselineMesh = baselineWasherMesh,
                    commercialMesh = commercialWasherMesh,
                    dryerMesh = dryerGasMesh,
                    textMeasurer = textMeasurer
                )
            }

            // Top Overlay HUD: Interactive Tag & Blender Mode Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive 3D tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD0F172A))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎨", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (wireframeMode) "BLENDER MESH WIREFRAME" else "BLENDER CYCLES 3D SHADER",
                            style = TextStyle(
                                color = if (wireframeMode) Color(0xFFFBBF24) else Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                // Action Controls: Wireframe Toggle, Mute, Replay
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Wireframe Topology Mode Toggle
                    IconButton(
                        onClick = { wireframeMode = !wireframeMode },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD0F172A))
                            .border(1.dp, if (wireframeMode) Color(0xFFFBBF24) else Color(0xFF64748B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = "Topology Wireframe",
                            tint = if (wireframeMode) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Mute/Unmute Toggle
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            GameSoundManager.isSoundEnabled = !isMuted
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD0F172A))
                            .border(1.dp, Color(0xFF64748B), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Suara",
                            tint = if (isMuted) Color(0xFF94A3B8) else Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Replay 3D Transition
                    IconButton(
                        onClick = { replayTrigger++ },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD0F172A))
                            .border(1.dp, Color(0xFFF59E0B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ulangi 3D",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom Center: Live Capacity Indicator Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF0D9488))))
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🚀 KAPASITAS BERTAMBAH: ",
                        style = TextStyle(color = Color(0xFFA7F3D0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${celebration.oldCapacityKg.toInt()} kg",
                        style = TextStyle(color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = " ➔ ",
                        style = TextStyle(color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = "${currentCapKg.toInt()} kg/hari",
                        style = TextStyle(color = Color(0xFFFDE68A), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}

/**
 * 3D Isometric Drawing Engine with Blender Lighting & Mesh Shading.
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawBlenderIsometricWorkshop(
    w: Float,
    h: Float,
    progress: Float,
    zoom: Float,
    userRotDeg: Float,
    userPitchDeg: Float,
    drumSpin: Float,
    shockwaveProgress: Float,
    laserGlow: Float,
    idleFloatY: Float,
    wireframeMode: Boolean,
    oldCap: Double,
    newCap: Double,
    currentCap: Double,
    baselineMesh: BlenderMesh,
    commercialMesh: BlenderMesh,
    dryerMesh: BlenderMesh,
    textMeasurer: TextMeasurer
) {
    val centerX = w * 0.5f
    val centerY = h * 0.58f

    val yawRad = (userRotDeg * PI / 180f).toFloat()
    val pitchRad = (userPitchDeg * PI / 180f).toFloat()

    // -------------------------------------------------------------
    // 1. DYNAMIC EXPANDING 3D FLOOR PLANE WITH BEVELED EDGES
    // -------------------------------------------------------------
    val baseRadius = 75f
    val targetRadius = 140f
    val currentRadius = baseRadius + ((targetRadius - baseRadius) * progress)

    val pNW = BlenderRenderer.projectIso(Vec3(-currentRadius, -currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val pNE = BlenderRenderer.projectIso(Vec3(currentRadius, -currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val pSE = BlenderRenderer.projectIso(Vec3(currentRadius, currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val pSW = BlenderRenderer.projectIso(Vec3(-currentRadius, currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)

    // Ceramic floor polygon
    val floorPath = Path().apply {
        moveTo(pNW.x, pNW.y)
        lineTo(pNE.x, pNE.y)
        lineTo(pSE.x, pSE.y)
        lineTo(pSW.x, pSW.y)
        close()
    }

    val floorBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            if (progress > 0.5f) Color(0xFF0369A1) else Color(0xFF1E293B)
        ),
        start = pNW,
        end = pSE
    )
    drawPath(path = floorPath, brush = floorBrush)

    // 3D Slab Thickness with Blender Occlusion Shading
    val slabDepth = 14f * zoom
    val slabNE = Offset(pNE.x, pNE.y + slabDepth)
    val slabSE = Offset(pSE.x, pSE.y + slabDepth)
    val slabSW = Offset(pSW.x, pSW.y + slabDepth)

    // Right Slab Face
    val slabRightPath = Path().apply {
        moveTo(pNE.x, pNE.y)
        lineTo(slabNE.x, slabNE.y)
        lineTo(slabSE.x, slabSE.y)
        lineTo(pSE.x, pSE.y)
        close()
    }
    drawPath(path = slabRightPath, color = Color(0xFF09122C))

    // Front Slab Face
    val slabFrontPath = Path().apply {
        moveTo(pSW.x, pSW.y)
        lineTo(slabSW.x, slabSW.y)
        lineTo(slabSE.x, slabSE.y)
        lineTo(pSE.x, pSE.y)
        close()
    }
    drawPath(path = slabFrontPath, color = Color(0xFF131D3F))

    // Glowing Blender Grid Lines & Chamfer Trim
    val gridStep = 30f
    var gx = -currentRadius
    while (gx <= currentRadius) {
        val s = BlenderRenderer.projectIso(Vec3(gx, -currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val e = BlenderRenderer.projectIso(Vec3(gx, currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        drawLine(
            color = Color(0xFF38BDF8).copy(alpha = 0.16f * laserGlow),
            start = s,
            end = e,
            strokeWidth = 1f
        )
        gx += gridStep
    }
    var gy = -currentRadius
    while (gy <= currentRadius) {
        val s = BlenderRenderer.projectIso(Vec3(-currentRadius, gy, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val e = BlenderRenderer.projectIso(Vec3(currentRadius, gy, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        drawLine(
            color = Color(0xFF38BDF8).copy(alpha = 0.16f * laserGlow),
            start = s,
            end = e,
            strokeWidth = 1f
        )
        gy += gridStep
    }

    // Glowing Perimeter Outline
    val perimeterColor = if (progress >= 0.8f) Color(0xFF38BDF8) else Color(0xFFF59E0B)
    drawPath(path = floorPath, color = perimeterColor, style = Stroke(width = 2f * zoom))

    // -------------------------------------------------------------
    // 2. SHOCKWAVE EXPANSION PULSE
    // -------------------------------------------------------------
    if (shockwaveProgress > 0f) {
        val waveRadius = currentRadius * shockwaveProgress * 1.35f
        val waveAlpha = (1f - shockwaveProgress).coerceIn(0f, 1f) * 0.7f
        val wNW = BlenderRenderer.projectIso(Vec3(-waveRadius, -waveRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val wNE = BlenderRenderer.projectIso(Vec3(waveRadius, -waveRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val wSE = BlenderRenderer.projectIso(Vec3(waveRadius, waveRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val wSW = BlenderRenderer.projectIso(Vec3(-waveRadius, waveRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)

        val wavePath = Path().apply {
            moveTo(wNW.x, wNW.y)
            lineTo(wNE.x, wNE.y)
            lineTo(wSE.x, wSE.y)
            lineTo(wSW.x, wSW.y)
            close()
        }
        drawPath(
            path = wavePath,
            color = Color(0xFFFBBF24).copy(alpha = waveAlpha),
            style = Stroke(width = 3.5f * (1f - shockwaveProgress + 0.2f))
        )
    }

    // -------------------------------------------------------------
    // 3. 3D BLENDER MESH MACHINES RENDERING
    // -------------------------------------------------------------

    // Machine 1: Baseline Washer 7kg (Home Workshop Equipment)
    BlenderRenderer.renderMesh(
        drawScope = this,
        mesh = baselineMesh,
        worldOffset = Vec3(-40f, -25f, 0f),
        centerX = centerX,
        centerY = centerY,
        zoom = zoom,
        yawRad = yawRad,
        pitchRad = pitchRad,
        wireframeOverlay = wireframeMode
    )

    // Animated Drum Water Swirl on Baseline Washer
    val m1FrontPos = BlenderRenderer.projectIso(Vec3(-40f, -10f, 16f), centerX, centerY, zoom, yawRad, pitchRad)
    drawCircle(
        color = Color(0xFF0284C7).copy(alpha = 0.85f),
        radius = 8f * zoom,
        center = m1FrontPos
    )
    drawArc(
        color = Color(0xFF38BDF8),
        startAngle = drumSpin,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(m1FrontPos.x - 7f * zoom, m1FrontPos.y - 7f * zoom),
        size = Size(14f * zoom, 14f * zoom),
        style = Stroke(width = 2.5f * zoom)
    )

    // Label Machine 1
    val m1Text = textMeasurer.measure(
        AnnotatedString("Cuci 1 (7kg)"),
        style = TextStyle(color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    )
    drawText(m1Text, topLeft = Offset(m1FrontPos.x - m1Text.size.width / 2f, m1FrontPos.y - 28f * zoom))

    // Machine 2: NEW COMMERCIAL UPGRADE (40kg Capacity Expansion!)
    // Dynamically drops from above into the expanded bay
    if (progress > 0.15f) {
        val dropProgress = ((progress - 0.15f) / 0.85f).coerceIn(0f, 1f)
        val dropZ = (1f - dropProgress) * 75f

        // Drop shadow onto floor
        val m2FloorPos = BlenderRenderer.projectIso(Vec3(15f, -25f, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        drawCircle(
            color = Color.Black.copy(alpha = 0.45f * dropProgress),
            radius = 24f * zoom * dropProgress,
            center = m2FloorPos
        )

        // Render Commercial Heavy-Duty Washer with Blender PBR shading
        BlenderRenderer.renderMesh(
            drawScope = this,
            mesh = commercialMesh,
            worldOffset = Vec3(15f, -25f, dropZ),
            centerX = centerX,
            centerY = centerY,
            zoom = zoom,
            yawRad = yawRad,
            pitchRad = pitchRad,
            wireframeOverlay = wireframeMode
        )

        // Spinning Drum on New Commercial Washer
        val m2FrontPos = BlenderRenderer.projectIso(Vec3(15f, -7f, dropZ + 20f), centerX, centerY, zoom, yawRad, pitchRad)
        drawCircle(
            color = Color(0xFF0369A1).copy(alpha = 0.9f),
            radius = 11f * zoom,
            center = m2FrontPos
        )
        drawArc(
            color = Color(0xFFFDE047),
            startAngle = drumSpin * 1.5f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = Offset(m2FrontPos.x - 9f * zoom, m2FrontPos.y - 9f * zoom),
            size = Size(18f * zoom, 18f * zoom),
            style = Stroke(width = 3f * zoom)
        )

        // High-Tech Badge Label
        val m2Text = textMeasurer.measure(
            AnnotatedString("⚡ Komersial (14kg)"),
            style = TextStyle(color = Color(0xFFFDE68A), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        )
        drawText(m2Text, topLeft = Offset(m2FrontPos.x - m2Text.size.width / 2f, m2FrontPos.y - 34f * zoom))
    }

    // Machine 3: Gas Dryer on Expanded Wing
    if (progress > 0.45f) {
        val subProg = ((progress - 0.45f) / 0.55f).coerceIn(0f, 1f)
        BlenderRenderer.renderMesh(
            drawScope = this,
            mesh = dryerMesh,
            worldOffset = Vec3(70f * progress, 5f, 0f),
            centerX = centerX,
            centerY = centerY,
            zoom = zoom,
            yawRad = yawRad,
            pitchRad = pitchRad,
            wireframeOverlay = wireframeMode
        )

        val m3Pos = BlenderRenderer.projectIso(Vec3(70f * progress, 20f, 18f), centerX, centerY, zoom, yawRad, pitchRad)
        val m3Text = textMeasurer.measure(
            AnnotatedString("🔥 Gas Dryer"),
            style = TextStyle(color = Color(0xFFFCA5A5), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        )
        drawText(m3Text, topLeft = Offset(m3Pos.x - m3Text.size.width / 2f, m3Pos.y - 28f * zoom))
    }

    // -------------------------------------------------------------
    // 4. HOLOGRAPHIC 3D CAPACITY CYLINDER (THE CAPACITY SURGE!)
    // -------------------------------------------------------------
    val cylinderBase = BlenderRenderer.projectIso(Vec3(-20f, 40f, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val maxCylinderHeight = 75f * zoom
    val currentEnergyHeight = (maxCylinderHeight * (currentCap / newCap).toFloat()).coerceIn(10f, maxCylinderHeight)

    val tubeRadius = 14f * zoom
    drawCircle(
        color = Color(0xFF0F172A).copy(alpha = 0.8f),
        radius = tubeRadius,
        center = cylinderBase
    )

    val energyTopCenter = Offset(cylinderBase.x, cylinderBase.y - currentEnergyHeight)
    val energyBrush = Brush.verticalGradient(
        listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)),
        startY = energyTopCenter.y,
        endY = cylinderBase.y
    )
    drawRoundRect(
        brush = energyBrush,
        topLeft = Offset(cylinderBase.x - tubeRadius + 2f, energyTopCenter.y),
        size = Size((tubeRadius - 2f) * 2f, currentEnergyHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    drawRoundRect(
        color = Color(0xFF6EE7B7).copy(alpha = 0.6f * laserGlow),
        topLeft = Offset(cylinderBase.x - tubeRadius, cylinderBase.y - maxCylinderHeight),
        size = Size(tubeRadius * 2f, maxCylinderHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
        style = Stroke(width = 2f)
    )

    // Old & New Level Ticks
    val oldMarkY = cylinderBase.y - (maxCylinderHeight * (oldCap / newCap).toFloat())
    drawLine(
        color = Color(0xFFFBBF24),
        start = Offset(cylinderBase.x - tubeRadius - 4f, oldMarkY),
        end = Offset(cylinderBase.x + tubeRadius + 4f, oldMarkY),
        strokeWidth = 2f
    )
    val oldText = textMeasurer.measure(
        AnnotatedString("${oldCap.toInt()}kg"),
        style = TextStyle(color = Color(0xFFFBBF24), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    )
    drawText(oldText, topLeft = Offset(cylinderBase.x + tubeRadius + 6f, oldMarkY - 5f))

    // Top Holographic Floating Badge with Live Animated Weight
    val badgePos = Offset(cylinderBase.x, energyTopCenter.y - 18f * zoom + idleFloatY)
    drawCircle(
        color = Color(0xFF10B981).copy(alpha = 0.35f * laserGlow),
        radius = 18f * zoom,
        center = badgePos
    )
    val capText = textMeasurer.measure(
        AnnotatedString("${currentCap.toInt()} KG"),
        style = TextStyle(color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textLayoutResult = capText,
        topLeft = Offset(badgePos.x - capText.size.width / 2f, badgePos.y - capText.size.height / 2f)
    )

    // -------------------------------------------------------------
    // 5. 3D CELEBRATORY POLYGONAL DIAMONDS / CONFETTI IN 3D SPACE
    // -------------------------------------------------------------
    val confettiColors = listOf(
        Color(0xFFFBBF24), Color(0xFF38BDF8), Color(0xFF34D399),
        Color(0xFFF472B6), Color(0xFFA78BFA)
    )
    val particleCount = 18
    for (i in 0 until particleCount) {
        val px = sin(i * 1.3f + progress * 2f) * (currentRadius * 0.9f)
        val py = cos(i * 1.7f + progress * 2f) * (currentRadius * 0.9f)
        val pz = (30f + (i * 7f) + (idleFloatY * 3f)) * zoom
        val screenPos = BlenderRenderer.projectIso(Vec3(px, py, pz), centerX, centerY, zoom, yawRad, pitchRad)

        val pColor = confettiColors[i % confettiColors.size]
        val sizeC = (4f + (i % 3) * 2f) * zoom
        val pPath = Path().apply {
            moveTo(screenPos.x, screenPos.y - sizeC)
            lineTo(screenPos.x + sizeC, screenPos.y)
            lineTo(screenPos.x, screenPos.y + sizeC)
            lineTo(screenPos.x - sizeC, screenPos.y)
            close()
        }
        drawPath(path = pPath, color = pColor.copy(alpha = 0.85f))
    }
}
