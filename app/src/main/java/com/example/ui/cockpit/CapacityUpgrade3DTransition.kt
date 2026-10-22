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
import androidx.compose.ui.geometry.CornerRadius
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
 * Bright, Colorful 3D Isometric Workshop Expansion Stage with Multi-Part 3D Meshes.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun CapacityUpgrade3DTransition(
    celebration: LevelUpCelebration,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    var userRotationDeg by remember { mutableStateOf(-28f) }
    var userPitchDeg by remember { mutableStateOf(30f) }
    var wireframeMode by remember { mutableStateOf(false) }
    var replayTrigger by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(!GameSoundManager.isSoundEnabled) }

    val animProgress = remember { Animatable(0f) }
    val cameraZoom = remember { Animatable(0.80f) }
    val shockwaveAnim = remember { Animatable(0f) }

    LaunchedEffect(celebration, replayTrigger) {
        GameSoundManager.playCapacityUpgradeFanfare()

        animProgress.snapTo(0f)
        cameraZoom.snapTo(0.75f)
        shockwaveAnim.snapTo(0f)

        cameraZoom.animateTo(
            targetValue = 1.06f,
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        GameSoundManager.play3DExpansionWhoosh()

        cameraZoom.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(450, easing = LinearOutSlowInEasing)
        )
        shockwaveAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = EaseOutQuad)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "blender_3d_loop")
    val drumSpinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drum_spin"
    )
    val idleFloatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_float"
    )

    val currentCapKg = remember(animProgress.value) {
        val oldCap = celebration.oldCapacityKg
        val newCap = celebration.newCapacityKg
        oldCap + ((newCap - oldCap) * animProgress.value)
    }

    val baselineWasherMesh = remember {
        BlenderModelFactory.createChamferedWasherMesh(
            width = 30f,
            depth = 30f,
            height = 38f,
            bodyColor = Color(0xFFFFFFFF),
            topColor = Color(0xFFE0F2FE),
            accentColor = Color(0xFF0EA5E9)
        )
    }
    val commercialWasherMesh = remember {
        BlenderModelFactory.createChamferedWasherMesh(
            width = 36f,
            depth = 36f,
            height = 46f,
            bodyColor = Color(0xFFF0FDF4),
            topColor = Color(0xFFD1FAE5),
            accentColor = Color(0xFF10B981)
        )
    }
    val dryerGasMesh = remember {
        BlenderModelFactory.createBlenderDryerMesh(
            width = 32f,
            depth = 32f,
            height = 40f,
            primaryColor = Color(0xFFF59E0B)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(Color(0xFF0EA5E9), Color(0xFF10B981), Color(0xFFF59E0B))
            ),
            width = 2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("capacity_upgrade_3d_stage")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(275.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        userRotationDeg += dragAmount.x * 0.45f
                        userPitchDeg = (userPitchDeg + dragAmount.y * 0.25f).coerceIn(18f, 60f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Cheerful Daylight Sky & Mint Gradient Backdrop
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFFFBEB))
                    ),
                    size = size
                )

                drawBrightIsometricWorkshop3D(
                    w = w,
                    h = h,
                    progress = animProgress.value,
                    zoom = cameraZoom.value,
                    userRotDeg = userRotationDeg,
                    userPitchDeg = userPitchDeg,
                    drumSpin = drumSpinAngle,
                    shockwaveProgress = shockwaveAnim.value,
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

            // Top Overlay HUD: Clean Pastel Badges & Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎨", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (wireframeMode) "3D WIREFRAME" else "3D DIORAMA INTERAKTIF (GESER)",
                            style = TextStyle(
                                color = Color(0xFF0284C7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { wireframeMode = !wireframeMode },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, if (wireframeMode) Color(0xFF0EA5E9) else Color(0xFFCBD5E1), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = "Topology Wireframe",
                            tint = if (wireframeMode) Color(0xFF0EA5E9) else Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            GameSoundManager.isSoundEnabled = !isMuted
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Suara",
                            tint = if (isMuted) Color(0xFF94A3B8) else Color(0xFF0EA5E9),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = { replayTrigger++ },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFFF59E0B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ulangi 3D",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Bottom Center: Live Capacity Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9))))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🚀 KAPASITAS: ${celebration.oldCapacityKg.toInt()} kg ➔ ${currentCapKg.toInt()} kg/hari",
                        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawBrightIsometricWorkshop3D(
    w: Float,
    h: Float,
    progress: Float,
    zoom: Float,
    userRotDeg: Float,
    userPitchDeg: Float,
    drumSpin: Float,
    shockwaveProgress: Float,
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
    val centerY = h * 0.62f

    val yawRad = (userRotDeg * PI / 180f).toFloat()
    val pitchRad = (userPitchDeg * PI / 180f).toFloat()

    val baseRadius = 72f
    val targetRadius = 128f
    val currentRadius = baseRadius + ((targetRadius - baseRadius) * progress)

    val pNW = BlenderRenderer.projectIso(Vec3(-currentRadius, -currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val pNE = BlenderRenderer.projectIso(Vec3(currentRadius, -currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val pSE = BlenderRenderer.projectIso(Vec3(currentRadius, currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val pSW = BlenderRenderer.projectIso(Vec3(-currentRadius, currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)

    // 3D Floor Platform Polygon
    val floorPath = Path().apply {
        moveTo(pNW.x, pNW.y)
        lineTo(pNE.x, pNE.y)
        lineTo(pSE.x, pSE.y)
        lineTo(pSW.x, pSW.y)
        close()
    }
    drawPath(
        path = floorPath,
        brush = Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFE0F2FE), Color(0xFFD1FAE5)),
            start = pNW,
            end = pSE
        )
    )

    // 3D Slab Thickness
    val slabDepth = 12f * zoom
    val slabRightPath = Path().apply {
        moveTo(pNE.x, pNE.y)
        lineTo(pNE.x, pNE.y + slabDepth)
        lineTo(pSE.x, pSE.y + slabDepth)
        lineTo(pSE.x, pSE.y)
        close()
    }
    drawPath(path = slabRightPath, color = Color(0xFF0284C7))

    val slabFrontPath = Path().apply {
        moveTo(pSW.x, pSW.y)
        lineTo(pSW.x, pSW.y + slabDepth)
        lineTo(pSE.x, pSE.y + slabDepth)
        lineTo(pSE.x, pSE.y)
        close()
    }
    drawPath(path = slabFrontPath, color = Color(0xFF38BDF8))

    // Pastel Tile Grid Lines
    val gridStep = 28f
    var gx = -currentRadius
    while (gx <= currentRadius) {
        val s = BlenderRenderer.projectIso(Vec3(gx, -currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val e = BlenderRenderer.projectIso(Vec3(gx, currentRadius, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        drawLine(Color(0xFF0EA5E9).copy(alpha = 0.22f), s, e, strokeWidth = 1f)
        gx += gridStep
    }
    var gy = -currentRadius
    while (gy <= currentRadius) {
        val s = BlenderRenderer.projectIso(Vec3(-currentRadius, gy, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        val e = BlenderRenderer.projectIso(Vec3(currentRadius, gy, 0f), centerX, centerY, zoom, yawRad, pitchRad)
        drawLine(Color(0xFF0EA5E9).copy(alpha = 0.22f), s, e, strokeWidth = 1f)
        gy += gridStep
    }

    drawPath(path = floorPath, color = Color(0xFF0EA5E9), style = Stroke(width = 2f * zoom))

    // Shockwave Ring
    if (shockwaveProgress > 0f) {
        val waveRadius = currentRadius * shockwaveProgress * 1.25f
        val waveAlpha = (1f - shockwaveProgress).coerceIn(0f, 1f) * 0.65f
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
        drawPath(path = wavePath, color = Color(0xFF10B981).copy(alpha = waveAlpha), style = Stroke(width = 3f))
    }

    // Machine 1: Baseline 7kg Washer
    BlenderRenderer.renderMesh(
        drawScope = this,
        mesh = baselineMesh,
        worldOffset = Vec3(-42f, -18f, 0f),
        centerX = centerX,
        centerY = centerY,
        zoom = zoom,
        yawRad = yawRad,
        pitchRad = pitchRad,
        wireframeOverlay = wireframeMode
    )

    // Machine 2: Commercial 14kg Washer
    if (progress > 0.15f) {
        val dropProgress = ((progress - 0.15f) / 0.85f).coerceIn(0f, 1f)
        val dropZ = (1f - dropProgress) * 65f

        BlenderRenderer.renderMesh(
            drawScope = this,
            mesh = commercialMesh,
            worldOffset = Vec3(8f, -18f, dropZ),
            centerX = centerX,
            centerY = centerY,
            zoom = zoom,
            yawRad = yawRad,
            pitchRad = pitchRad,
            wireframeOverlay = wireframeMode
        )
    }

    // Machine 3: Sunny Amber Gas Dryer
    if (progress > 0.40f) {
        BlenderRenderer.renderMesh(
            drawScope = this,
            mesh = dryerMesh,
            worldOffset = Vec3(58f * progress, -10f, 0f),
            centerX = centerX,
            centerY = centerY,
            zoom = zoom,
            yawRad = yawRad,
            pitchRad = pitchRad,
            wireframeOverlay = wireframeMode
        )
    }

    // Floating Capacity Pillar
    val pillarBase = BlenderRenderer.projectIso(Vec3(-15f, 42f, 0f), centerX, centerY, zoom, yawRad, pitchRad)
    val maxH = 62f * zoom
    val curH = (maxH * (currentCap / newCap).toFloat()).coerceIn(12f, maxH)
    val tubeW = 24f * zoom

    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(pillarBase.x - tubeW / 2f, pillarBase.y - maxH),
        size = Size(tubeW, maxH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9))),
        topLeft = Offset(pillarBase.x - tubeW / 2f + 2f, pillarBase.y - curH),
        size = Size(tubeW - 4f, curH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color(0xFF059669),
        topLeft = Offset(pillarBase.x - tubeW / 2f, pillarBase.y - maxH),
        size = Size(tubeW, maxH),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.8f)
    )

    val capText = textMeasurer.measure(
        AnnotatedString("${currentCap.toInt()} KG"),
        style = TextStyle(color = Color(0xFF0F172A), fontSize = 10.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textLayoutResult = capText,
        topLeft = Offset(pillarBase.x - capText.size.width / 2f, pillarBase.y - maxH - 18f + idleFloatY)
    )
}
