package com.example.ui.scene

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sim.*
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Compact, Coherent, Active & Reactive 3D Isometric Cutaway Diorama Scene.
 * - Tapping any 3D station directly boosts work at that station, spawns a 3D shockwave ring,
 *   and makes the 3D machine/table bounce elastically without blocking the view with a modal.
 * - Includes animated 3D conveyor tracks, moving laundry bundles, vibrating washing machines,
 *   gliding 3D steam iron, swinging worker arms, and interactive camera angle/zoom controls.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun Laundry3DScene(
    engine: SimulationEngine,
    selectedPhase: StationPhase?,
    onSelectPhase: (StationPhase) -> Unit,
    onInteractStation: ((StationPhase) -> Unit)? = null,
    onOpenInspector: ((StationPhase) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Interactive Camera Angle (-1 = Left View, 0 = Center Isometric, +1 = Right View) & Zoom
    var cameraViewIndex by remember { mutableIntStateOf(0) }
    var isZoomedIn by remember { mutableStateOf(false) }

    val targetSkew = when (cameraViewIndex) {
        -1 -> -0.14f
        1 -> 0.14f
        else -> 0f
    }
    val animatedSkew by animateFloatAsState(
        targetValue = targetSkew,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "camera_skew"
    )
    val animatedZoom by animateFloatAsState(
        targetValue = if (isZoomedIn) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "camera_zoom"
    )

    // Local reactive tap state for immediate 3D bounce & shockwave ring
    var tappedStation by remember { mutableStateOf<StationPhase?>(null) }
    var tapCounter by remember { mutableIntStateOf(0) }
    val tapPulseAnim = remember { Animatable(0f) }

    LaunchedEffect(tapCounter, engine.lastInteractionTimestamp) {
        if (tapCounter > 0 || engine.lastInteractionTimestamp > 0L) {
            if (engine.lastInteractedPhase != null) {
                tappedStation = engine.lastInteractedPhase
            }
            tapPulseAnim.snapTo(1f)
            tapPulseAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "diorama_3d_anim")
    val washerSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "washer_spin"
    )
    val conveyorFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "conveyor_flow"
    )
    val workerBob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "worker_bob"
    )
    val ironGlide by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iron_glide"
    )
    val steamFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam"
    )
    val clothesSway by infiniteTransition.animateFloat(
        initialValue = -0.18f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )
    val rainDropAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    val bottleneck = remember(engine.currentMinuteOfDay, engine.ordersList.size) {
        engine.diagnoseBottleneck()
    }

    // Coherent 9x9 Isometric Grid Centers for the 6 Stations
    val stationIsoCenters = remember {
        mapOf(
            StationPhase.MASUK to Pair(2.0f, 6.3f),
            StationPhase.CUCI to Pair(2.3f, 1.8f),
            StationPhase.JEMUR_KERING to Pair(6.5f, 1.8f),
            StationPhase.SETRIKA to Pair(4.9f, 4.6f),
            StationPhase.PACKING to Pair(7.5f, 5.2f),
            StationPhase.SELESAI to Pair(5.3f, 7.4f)
        )
    }

    val activeSelectedPhase = selectedPhase ?: bottleneck.phase

    val triggerStationBoost: (StationPhase) -> Unit = { phase ->
        tappedStation = phase
        tapCounter++
        if (onInteractStation != null) {
            onInteractStation(phase)
        } else {
            engine.interactWithStation(phase)
            onSelectPhase(phase)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(Color(0xFF7DD3FC), Color(0xFF6EE7B7), Color(0xFFFDE68A))
            ),
            width = 2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x330EA5E9))
            .clip(RoundedCornerShape(24.dp))
            .testTag("laundry_3d_scene")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. UNOBSTRUCTED 3D ISOMETRIC VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(285.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(animatedSkew, animatedZoom) {
                            detectTapGestures { offset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val originX = w * 0.50f
                                val originY = h * 0.14f
                                val scale = min(w / 15.6f, h / 10.6f) * animatedZoom
                                BlenderRenderer.cameraSkewX = animatedSkew

                                var closestPhase: StationPhase? = null
                                var minDist = Float.MAX_VALUE
                                for ((phase, coords) in stationIsoCenters) {
                                    val pt = BlenderRenderer.isoPoint(originX, originY, scale, coords.first, coords.second, 0.8f)
                                    val d = hypot(offset.x - pt.x, offset.y - pt.y)
                                    if (d < minDist) {
                                        minDist = d
                                        closestPhase = phase
                                    }
                                }

                                val hitPhase = closestPhase ?: StationPhase.CUCI
                                triggerStationBoost(hitPhase)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val originX = w * 0.50f
                    val originY = h * 0.14f
                    val scale = min(w / 15.6f, h / 10.6f) * animatedZoom
                    BlenderRenderer.cameraSkewX = animatedSkew

                    val bgColors = when (engine.weatherToday) {
                        Weather.CERAH -> listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFFFBEB))
                        Weather.MENDUNG -> listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9), Color(0xFFE0F2FE))
                        Weather.HUJAN -> listOf(Color(0xFFDBEAFE), Color(0xFFE2E8F0), Color(0xFFF1F5F9))
                    }
                    drawRect(
                        brush = Brush.verticalGradient(bgColors),
                        topLeft = Offset.Zero,
                        size = size
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66FDE68A), Color.Transparent),
                            center = Offset(w * 0.84f, h * 0.15f),
                            radius = w * 0.42f
                        ),
                        center = Offset(w * 0.84f, h * 0.15f),
                        radius = w * 0.42f
                    )

                    // A. 3D ISOMETRIC CUTAWAY DIORAMA ROOM (Compact 9x9 Grid)
                    drawIsoCutawayRoom3D(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        weather = engine.weatherToday,
                        rainAnim = rainDropAnim,
                        tycoonLevel = engine.tycoonLevel
                    )

                    // B. ANIMATED 3D FLOOR CONVEYOR TRACKS & MOVING LAUNDRY BUNDLES (1 -> 2 -> 3 -> 4 -> 5 -> 6)
                    drawConveyorNetworkAndBundles3D(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        engine = engine,
                        flowProgress = conveyorFlow,
                        stationCenters = stationIsoCenters
                    )

                    // C. REACTIVE 3D TAP SHOCKWAVE RING & SELECTED STATION HIGHLIGHT RING
                    val selCoords = stationIsoCenters[activeSelectedPhase]
                    if (selCoords != null) {
                        val selColor = Color(activeSelectedPhase.colorHex)
                        BlenderRenderer.drawIsoFloorRing(
                            drawScope = this,
                            originX = originX,
                            originY = originY,
                            scale = scale,
                            centerX = selCoords.first,
                            centerY = selCoords.second,
                            z = 0.04f,
                            radius = 1.45f + sin(washerSpin * PI.toFloat() / 180f) * 0.08f,
                            color = selColor,
                            strokeWidth = 2.6f,
                            fillAlpha = 0.12f
                        )
                    }

                    if (tappedStation != null && tapPulseAnim.value > 0.02f) {
                        val tapCoords = stationIsoCenters[tappedStation]
                        if (tapCoords != null) {
                            val progress = 1f - tapPulseAnim.value
                            val ringColor = Color(tappedStation!!.colorHex)
                            BlenderRenderer.drawIsoFloorRing(
                                drawScope = this,
                                originX = originX,
                                originY = originY,
                                scale = scale,
                                centerX = tapCoords.first,
                                centerY = tapCoords.second,
                                z = 0.06f,
                                radius = 0.7f + progress * 1.9f,
                                color = ringColor.copy(alpha = tapPulseAnim.value),
                                strokeWidth = 4.5f * tapPulseAnim.value,
                                fillAlpha = 0.22f * tapPulseAnim.value
                            )
                        }
                    }

                    // D. 6 MULTI-PART 3D STATIONS IN PAINTER'S BACK-TO-FRONT DEPTH ORDER
                    val cuciKg = engine.ordersList.filter { it.currentPhase == StationPhase.CUCI }.sumOf { it.weightKg }
                    val keringKg = engine.ordersList.filter { it.currentPhase == StationPhase.JEMUR_KERING }.sumOf { it.weightKg }
                    val setrikaKg = engine.ordersList.filter { it.currentPhase == StationPhase.SETRIKA }.sumOf { it.weightKg }
                    val masukKg = engine.ordersList.filter { it.currentPhase == StationPhase.MASUK }.sumOf { it.weightKg }
                    val packingKg = engine.ordersList.filter { it.currentPhase == StationPhase.PACKING }.sumOf { it.weightKg }
                    val selesaiKg = engine.ordersList.filter { it.currentPhase == StationPhase.SELESAI }.sumOf { it.weightKg }

                    val isWasherRunning = engine.equipmentList.any { it.isRunning } || cuciKg > 0.0
                    val washerCount = engine.equipmentList.count {
                        it.type == AssetType.MESIN_CUCI_7KG || it.type == AssetType.MESIN_CUCI_14KG
                    }.coerceAtLeast(1)
                    val hasCommercialWasher = engine.equipmentList.any { it.type == AssetType.MESIN_CUCI_14KG }
                    val hasGasDryer = engine.equipmentList.any { it.type == AssetType.DRYER_GAS_10KG }
                    val hasBoiler = engine.equipmentList.any { it.type == AssetType.SETRIKA_UAP_BOILER }

                    fun stationBounceZ(phase: StationPhase): Float {
                        return if (tappedStation == phase) sin(tapPulseAnim.value * PI.toFloat()) * 0.42f else 0f
                    }

                    // 1. Back-Left: Stasiun 2 (CUCI)
                    draw3DStationCuci(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        zBounce = stationBounceZ(StationPhase.CUCI),
                        isRunning = isWasherRunning,
                        washerCount = washerCount,
                        hasCommercialWasher = hasCommercialWasher,
                        spinAngle = washerSpin,
                        steamFloat = steamFloat,
                        ordersKg = cuciKg,
                        progressFrac = engine.getPhaseProgressFraction(StationPhase.CUCI),
                        isSelected = activeSelectedPhase == StationPhase.CUCI,
                        isBottleneck = bottleneck.phase == StationPhase.CUCI && cuciKg >= 12.0,
                        textMeasurer = textMeasurer
                    )

                    // 2. Back-Right: Stasiun 3 (KERING)
                    draw3DStationKering(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        zBounce = stationBounceZ(StationPhase.JEMUR_KERING),
                        hasGasDryer = hasGasDryer,
                        clothesSway = clothesSway,
                        spinAngle = washerSpin,
                        ordersKg = keringKg,
                        progressFrac = engine.getPhaseProgressFraction(StationPhase.JEMUR_KERING),
                        isSelected = activeSelectedPhase == StationPhase.JEMUR_KERING,
                        isBottleneck = bottleneck.phase == StationPhase.JEMUR_KERING && keringKg >= 12.0,
                        textMeasurer = textMeasurer
                    )

                    // 3. Center Island: Stasiun 4 (SETRIKA)
                    draw3DStationSetrika(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        zBounce = stationBounceZ(StationPhase.SETRIKA),
                        hasBoiler = hasBoiler,
                        steamFloat = steamFloat,
                        ironGlide = if (setrikaKg > 0.0) ironGlide else 0f,
                        ordersKg = setrikaKg,
                        progressFrac = engine.getPhaseProgressFraction(StationPhase.SETRIKA),
                        isSelected = activeSelectedPhase == StationPhase.SETRIKA,
                        isBottleneck = bottleneck.phase == StationPhase.SETRIKA && setrikaKg >= 12.0,
                        textMeasurer = textMeasurer
                    )

                    // 4. Front-Left: Stasiun 1 (MASUK)
                    draw3DStationMasuk(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        zBounce = stationBounceZ(StationPhase.MASUK),
                        ordersKg = masukKg,
                        progressFrac = engine.getPhaseProgressFraction(StationPhase.MASUK),
                        isSelected = activeSelectedPhase == StationPhase.MASUK,
                        isBottleneck = bottleneck.phase == StationPhase.MASUK && masukKg >= 12.0,
                        textMeasurer = textMeasurer
                    )

                    // 5. Front-Right: Stasiun 5 (PACKING)
                    draw3DStationPacking(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        zBounce = stationBounceZ(StationPhase.PACKING),
                        ordersKg = packingKg,
                        progressFrac = engine.getPhaseProgressFraction(StationPhase.PACKING),
                        isSelected = activeSelectedPhase == StationPhase.PACKING,
                        isBottleneck = bottleneck.phase == StationPhase.PACKING && packingKg >= 12.0,
                        textMeasurer = textMeasurer
                    )

                    // 6. Front-Center Pickup: Stasiun 6 (SELESAI)
                    draw3DStationSelesai(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        zBounce = stationBounceZ(StationPhase.SELESAI),
                        ordersKg = selesaiKg,
                        completedTodayKg = engine.completedKgToday,
                        isSelected = activeSelectedPhase == StationPhase.SELESAI,
                        textMeasurer = textMeasurer
                    )

                    // E. ACTIVE 3D CHARACTERS WITH SWINGING ARMS
                    draw3DIsoCharacters(
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        workers = engine.workerList,
                        bob = workerBob,
                        armSwing = ironGlide,
                        hasIncomingCustomer = masukKg > 0.0,
                        textMeasurer = textMeasurer
                    )

                    // F. FLOATING REWARD PARTICLES
                    drawFloatingParticlesClean(w, h, engine.floatingParticles, textMeasurer)
                }

                // Top-Left Clean Pastel Pill: Store Tier & Weather
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .shadow(3.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏪", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "${engine.growthTier.title} • Lv.${engine.tycoonLevel}",
                            style = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp)
                        )
                        Text(
                            text = "${engine.weatherToday.icon} Cuaca ${engine.weatherToday.label} • Ketuk alat 3D untuk bantu!",
                            style = TextStyle(color = Color(0xFF0284C7), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }
                }

                // Top-Right Interactive 3D Camera & Combo Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (engine.taskComboCount > 1) {
                        Box(
                            modifier = Modifier
                                .shadow(3.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFF43F5E), Color(0xFFF59E0B))))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔥x${engine.taskComboCount}",
                                style = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                            )
                        }
                    }

                    // Camera Angle Toggle Button
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(10.dp))
                            .clickable {
                                cameraViewIndex = when (cameraViewIndex) {
                                    0 -> 1
                                    1 -> -1
                                    else -> 0
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (cameraViewIndex) {
                                -1 -> "🎥 Kiri"
                                1 -> "🎥 Kanan"
                                else -> "🎥 Tengah"
                            },
                            style = TextStyle(color = OceanBlueDeep, fontWeight = FontWeight.ExtraBold, fontSize = 9.5.sp)
                        )
                    }

                    // Zoom Toggle Button
                    Box(
                        modifier = Modifier
                            .shadow(3.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isZoomedIn) OceanBluePrimary else Color.White.copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(10.dp))
                            .clickable { isZoomedIn = !isZoomedIn }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isZoomedIn) "🔍 1.2x" else "🔍 1.0x",
                            style = TextStyle(
                                color = if (isZoomedIn) Color.White else OceanBlueDeep,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

            // 2. DEDICATED 6-STATION STEP SELECTOR BAR (Never overlaps 3D Canvas, never wraps vertically!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StationPhase.values().forEach { phase ->
                        val isSel = activeSelectedPhase == phase
                        val phaseKg = engine.ordersList.filter { it.currentPhase == phase }.sumOf { it.weightKg }
                        val pillColor = Color(phase.colorHex)
                        val shortName = when (phase) {
                            StationPhase.MASUK -> "Masuk"
                            StationPhase.CUCI -> "Cuci"
                            StationPhase.JEMUR_KERING -> "Kering"
                            StationPhase.SETRIKA -> "Setrika"
                            StationPhase.PACKING -> "Packing"
                            StationPhase.SELESAI -> "Selesai"
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) pillColor else Color.White)
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) pillColor else pillColor.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { triggerStationBoost(phase) }
                                .padding(vertical = 5.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${phase.step}.${phase.icon}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSel) Color.White else pillColor,
                                maxLines = 1
                            )
                            Text(
                                text = shortName,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSel) Color.White else Slate800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${phaseKg.toInt()}kg",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFFFEF08A) else Slate500,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. LIVE SELECTED STATION REACTIVE CONTROL STRIP
                val selKg = engine.ordersList.filter { it.currentPhase == activeSelectedPhase }.sumOf { it.weightKg }
                val selProgress = engine.getPhaseProgressFraction(activeSelectedPhase)
                val selWorkerNames = engine.workerList
                    .filter { it.assignedPhase == activeSelectedPhase }
                    .joinToString(", ") { it.name.substringBefore(" ") }
                    .ifEmpty { "Belum ada staf tetap" }
                val selColor = Color(activeSelectedPhase.colorHex)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(selColor.copy(alpha = 0.10f))
                        .border(1.dp, selColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tahap ${activeSelectedPhase.step}: ${activeSelectedPhase.icon} ${activeSelectedPhase.title}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(selColor)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${selKg.toInt()} kg",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "👷 Staf: $selWorkerNames",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600,
                            maxLines = 1
                        )
                        if (selKg > 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { selProgress },
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = selColor,
                                trackColor = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Two clear action buttons: Boost 3D Station OR Open Station Inspector Modal
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { triggerStationBoost(activeSelectedPhase) },
                            colors = ButtonDefaults.buttonColors(containerColor = selColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (activeSelectedPhase == StationPhase.MASUK && selKg == 0.0) "➕ Order" else "⚡ Bantu!",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onOpenInspector?.invoke(activeSelectedPhase) ?: onSelectPhase(activeSelectedPhase)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, selColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "🔧 Atur",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = selColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawIsoCutawayRoom3D(
    originX: Float,
    originY: Float,
    scale: Float,
    weather: Weather,
    rainAnim: Float,
    tycoonLevel: Int
) {
    val gridN = 9f
    // Soft ground shadow beneath the 3D isometric building
    val shadowPath = Path().apply {
        val s0 = BlenderRenderer.isoPoint(originX, originY + 7f, scale, 0.2f, 0.2f, -0.55f)
        val s1 = BlenderRenderer.isoPoint(originX, originY + 7f, scale, gridN + 0.3f, 0.2f, -0.55f)
        val s2 = BlenderRenderer.isoPoint(originX, originY + 7f, scale, gridN + 0.3f, gridN + 0.3f, -0.55f)
        val s3 = BlenderRenderer.isoPoint(originX, originY + 7f, scale, 0.2f, gridN + 0.3f, -0.55f)
        moveTo(s0.x, s0.y)
        lineTo(s1.x, s1.y)
        lineTo(s2.x, s2.y)
        lineTo(s3.x, s3.y)
        close()
    }
    drawPath(shadowPath, color = Color(0x220284C7))

    // 1A. 3D Floor Foundation Slab (x=0..9, y=0..9, z=-0.48..0)
    BlenderRenderer.drawIsoBox(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        x = 0f, y = 0f, z = -0.48f,
        dx = gridN, dy = gridN, dz = 0.48f,
        topColor = Color(0xFFF8FAFC),
        leftColor = Color(0xFF38BDF8),
        rightColor = Color(0xFF0284C7),
        strokeColor = Color.White.copy(alpha = 0.5f)
    )

    // 1B. 3D Isometric Checkerboard Ceramic Tiles (9x9 grid)
    val tileLight = Color(0xFFFFFFFF)
    val tileAccent = if (tycoonLevel >= 2) Color(0xFFD1FAE5) else Color(0xFFE0F2FE)
    val tileBorder = Color(0xFFBAE6FD).copy(alpha = 0.45f)

    for (ix in 0 until 9) {
        for (iy in 0 until 9) {
            val p0 = BlenderRenderer.isoPoint(originX, originY, scale, ix.toFloat(), iy.toFloat(), 0f)
            val p1 = BlenderRenderer.isoPoint(originX, originY, scale, ix + 1f, iy.toFloat(), 0f)
            val p2 = BlenderRenderer.isoPoint(originX, originY, scale, ix + 1f, iy + 1f, 0f)
            val p3 = BlenderRenderer.isoPoint(originX, originY, scale, ix.toFloat(), iy + 1f, 0f)
            val tPath = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }
            val color = if ((ix + iy) % 2 == 0) tileLight else tileAccent
            drawPath(tPath, color = color)
            drawPath(tPath, color = tileBorder, style = Stroke(width = 0.7f))
        }
    }

    val wallH = 3.1f
    val wallThick = 0.32f

    // Left Cutaway Wall (runs along Y axis at X = 0)
    BlenderRenderer.drawIsoBox(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        x = -wallThick, y = -wallThick, z = 0f,
        dx = wallThick, dy = gridN + wallThick, dz = wallH,
        topColor = Color(0xFFBAE6FD),
        leftColor = Color(0xFF7DD3FC),
        rightColor = Color(0xFFF0F9FF),
        strokeColor = Color(0xFF93C5FD)
    )
    // Left Wall Mint Baseboard Trim
    BlenderRenderer.drawIsoBox(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        x = 0f, y = 0f, z = 0f,
        dx = 0.08f, dy = gridN, dz = 0.30f,
        topColor = Color(0xFF34D399),
        leftColor = Color(0xFF10B981),
        rightColor = Color(0xFF10B981),
        strokeColor = null
    )

    // Right Cutaway Wall (runs along X axis at Y = 0)
    BlenderRenderer.drawIsoBox(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        x = 0f, y = -wallThick, z = 0f,
        dx = gridN, dy = wallThick, dz = wallH,
        topColor = Color(0xFFBAE6FD),
        leftColor = Color(0xFFE0F2FE),
        rightColor = Color(0xFF7DD3FC),
        strokeColor = Color(0xFF93C5FD)
    )
    // Right Wall Mint Baseboard Trim
    BlenderRenderer.drawIsoBox(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        x = 0f, y = 0f, z = 0f,
        dx = gridN, dy = 0.08f, dz = 0.30f,
        topColor = Color(0xFF34D399),
        leftColor = Color(0xFF059669),
        rightColor = Color(0xFF10B981),
        strokeColor = null
    )

    // 3D Isometric Window on Right Wall
    val w0 = BlenderRenderer.isoPoint(originX, originY, scale, 4.4f, 0.06f, 1.15f)
    val w1 = BlenderRenderer.isoPoint(originX, originY, scale, 8.0f, 0.06f, 1.15f)
    val w2 = BlenderRenderer.isoPoint(originX, originY, scale, 8.0f, 0.06f, 2.75f)
    val w3 = BlenderRenderer.isoPoint(originX, originY, scale, 4.4f, 0.06f, 2.75f)
    val winPath = Path().apply {
        moveTo(w0.x, w0.y)
        lineTo(w1.x, w1.y)
        lineTo(w2.x, w2.y)
        lineTo(w3.x, w3.y)
        close()
    }
    val skyColors = when (weather) {
        Weather.CERAH -> listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD))
        Weather.MENDUNG -> listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1))
        Weather.HUJAN -> listOf(Color(0xFF64748B), Color(0xFF94A3B8))
    }
    drawPath(winPath, brush = Brush.verticalGradient(skyColors, startY = w3.y, endY = w0.y))
    if (weather == Weather.CERAH) {
        val sunCenter = BlenderRenderer.isoPoint(originX, originY, scale, 7.1f, 0.06f, 2.25f)
        drawCircle(Color(0xFFFFE066), radius = scale * 0.40f, center = sunCenter)
    } else if (weather == Weather.HUJAN) {
        for (r in 0..5) {
            val rx = 4.8f + r * 0.55f
            val rz = 2.6f - ((rainAnim + r * 0.18f) % 1.0f) * 1.25f
            val rs = BlenderRenderer.isoPoint(originX, originY, scale, rx, 0.06f, rz)
            val re = BlenderRenderer.isoPoint(originX, originY, scale, rx - 0.14f, 0.06f, rz - 0.30f)
            drawLine(Color.White.copy(alpha = 0.85f), rs, re, strokeWidth = 2f)
        }
    }
    drawPath(winPath, color = Color.White, style = Stroke(width = 2.6f))

    // 3D Detergent Wall Shelf & Cylindrical Bottles on Left Wall
    BlenderRenderer.drawIsoBox(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        x = 0.05f, y = 1.2f, z = 1.85f,
        dx = 0.40f, dy = 2.5f, dz = 0.15f,
        topColor = Color(0xFFFDE68A),
        leftColor = Color(0xFFF59E0B),
        rightColor = Color(0xFFD97706)
    )
    val bottleColors = listOf(Color(0xFFF43F5E), Color(0xFF0EA5E9), Color(0xFF10B981), Color(0xFF8B5CF6))
    for (b in 0..3) {
        val bc = bottleColors[b]
        BlenderRenderer.drawIsoVerticalCylinder(
            drawScope = this,
            originX = originX,
            originY = originY,
            scale = scale,
            centerX = 0.25f,
            centerY = 1.55f + b * 0.56f,
            baseZ = 2.0f,
            radius = 0.14f,
            height = 0.44f,
            topColor = BlenderLighting.highlightColor(bc, 0.35f),
            bodyColor = bc
        )
    }
}

private fun DrawScope.drawConveyorNetworkAndBundles3D(
    originX: Float,
    originY: Float,
    scale: Float,
    engine: SimulationEngine,
    flowProgress: Float,
    stationCenters: Map<StationPhase, Pair<Float, Float>>
) {
    val links = listOf(
        Triple(StationPhase.MASUK, StationPhase.CUCI, Color(0xFF10B981)),
        Triple(StationPhase.CUCI, StationPhase.JEMUR_KERING, Color(0xFF0EA5E9)),
        Triple(StationPhase.JEMUR_KERING, StationPhase.SETRIKA, Color(0xFFF59E0B)),
        Triple(StationPhase.SETRIKA, StationPhase.PACKING, Color(0xFFF43F5E)),
        Triple(StationPhase.PACKING, StationPhase.SELESAI, Color(0xFF8B5CF6))
    )

    for ((fromPhase, toPhase, color) in links) {
        val c1 = stationCenters[fromPhase] ?: continue
        val c2 = stationCenters[toPhase] ?: continue
        BlenderRenderer.drawIsoConveyorTrack(
            drawScope = this,
            originX = originX,
            originY = originY,
            scale = scale,
            x1 = c1.first,
            y1 = c1.second,
            x2 = c2.first,
            y2 = c2.second,
            flowProgress = flowProgress,
            trackColor = color
        )

        // Only render a moving 3D Laundry Basket on this track when the origin station actively has laundry flowing forward!
        val activeLoad = engine.ordersList.any { !it.isCompleted && it.currentPhase == fromPhase }
        if (activeLoad) {
            val bx = c1.first + (c2.first - c1.first) * flowProgress
            val by = c1.second + (c2.second - c1.second) * flowProgress
            val hopZ = abs(sin(flowProgress * PI.toFloat() * 2f)) * 0.14f
            BlenderRenderer.drawIsoBox(
                drawScope = this,
                originX = originX,
                originY = originY,
                scale = scale,
                x = bx - 0.22f,
                y = by - 0.22f,
                z = 0.06f + hopZ,
                dx = 0.44f,
                dy = 0.44f,
                dz = 0.30f,
                topColor = BlenderLighting.highlightColor(color, 0.35f),
                leftColor = color,
                rightColor = BlenderLighting.shadeColor(color, 0.80f)
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DStationCuci(
    originX: Float,
    originY: Float,
    scale: Float,
    zBounce: Float,
    isRunning: Boolean,
    washerCount: Int,
    hasCommercialWasher: Boolean,
    spinAngle: Float,
    steamFloat: Float,
    ordersKg: Double,
    progressFrac: Float,
    isSelected: Boolean,
    isBottleneck: Boolean,
    textMeasurer: TextMeasurer
) {
    draw3DStationPad(
        originX, originY, scale,
        x = 0.7f, y = 0.7f, dx = 3.2f, dy = 2.2f,
        padColor = if (isBottleneck) Color(0xFFFFE4E6) else Color(0xFFE0F2FE),
        borderColor = if (isBottleneck) Color(0xFFF43F5E) else if (isSelected) Color(0xFF0EA5E9) else Color(0xFF7DD3FC)
    )

    // Reactive machine vibration when washing
    val vibX = if (isRunning) sin(spinAngle * PI.toFloat() / 45f) * 0.04f else 0f
    val vibZ = zBounce + (if (isRunning) abs(cos(spinAngle * PI.toFloat() / 45f)) * 0.05f else 0f)

    val activeSlots = washerCount.coerceIn(1, 2)
    val washerPositions = listOf(0.95f, 2.35f)
    washerPositions.forEachIndexed { idx, baseWx ->
        val wy = 0.90f
        if (idx < activeSlots) {
            val wx = baseWx + vibX * (if (idx == 0) 1f else -1f)
            // Pedestal
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = wx, y = wy, z = vibZ,
                dx = 1.18f, dy = 1.18f, dz = 0.20f,
                topColor = Color(0xFFCBD5E1),
                leftColor = Color(0xFF94A3B8),
                rightColor = Color(0xFF64748B)
            )
            // Main Cabinet
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = wx, y = wy, z = vibZ + 0.20f,
                dx = 1.18f, dy = 1.18f, dz = 1.35f,
                topColor = Color(0xFFFFFFFF),
                leftColor = Color(0xFFF8FAFC),
                rightColor = Color(0xFFE2E8F0),
                strokeColor = Color(0xFFBAE6FD)
            )
            // Top Control Fascia (Gold/Amber trim if upgraded to Commercial 14kg Washer!)
            val topAccent = if (idx == 1 && hasCommercialWasher) {
                Color(0xFFF59E0B)
            } else if (idx == 0) {
                Color(0xFF0EA5E9)
            } else {
                Color(0xFF06B6D4)
            }
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = wx, y = wy, z = vibZ + 1.55f,
                dx = 1.18f, dy = 1.18f, dz = 0.28f,
                topColor = BlenderLighting.highlightColor(topAccent, 0.35f),
                leftColor = topAccent,
                rightColor = BlenderLighting.shadeColor(topAccent, 0.82f)
            )

            // Blinking LED indicator dot on control fascia
            val ledPt = BlenderRenderer.isoPoint(originX, originY, scale, wx + 0.92f, wy + 1.19f, vibZ + 1.68f)
            drawCircle(
                color = if (isRunning) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                radius = scale * 0.08f,
                center = ledPt
            )

            // True 3D Foreshortened Isometric Drum Porthole on the +Y Front Face
            BlenderRenderer.drawIsoPortholeOnYFace(
                drawScope = this,
                originX = originX,
                originY = originY,
                scale = scale,
                centerX = wx + 0.59f,
                faceY = wy + 1.18f,
                centerZ = vibZ + 0.88f,
                radius = 0.39f,
                extrudeY = 0.13f,
                rimColor = topAccent,
                glassColor = if (isRunning) Color(0xFF0284C7) else Color(0xFF334155),
                swirlColor = if (isRunning) Color(0xFF7DD3FC) else null,
                spinDeg = spinAngle + idx * 90f
            )

            if (isRunning) {
                val bubblePos = BlenderRenderer.isoPoint(originX, originY, scale, wx + 0.55f, wy + 0.55f, vibZ + 1.95f + steamFloat * 0.65f)
                drawCircle(Color(0xCC7DD3FC), radius = 5.5f, center = bubblePos)
                drawCircle(Color.White, radius = 2.2f, center = Offset(bubblePos.x - 1.5f, bubblePos.y - 1.5f))
            }
        } else {
            // Empty 2nd Washer Expansion Slot Pad (shows clearly where a new bought washer will appear!)
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = baseWx, y = wy, z = 0.02f,
                dx = 1.18f, dy = 1.18f, dz = 0.12f,
                topColor = Color(0xFFE0F2FE),
                leftColor = Color(0xFFBAE6FD),
                rightColor = Color(0xFF7DD3FC),
                strokeColor = Color(0xFF38BDF8)
            )
        }
    }

    val badgePos = BlenderRenderer.isoPoint(originX, originY, scale, 1.9f, 0.7f, 2.50f + zBounce)
    drawClean3DBadge(
        centerX = badgePos.x,
        centerY = badgePos.y,
        stepNum = 2,
        title = "Cuci",
        value = "${ordersKg.toInt()}kg",
        progress = if (ordersKg > 0) progressFrac else 0f,
        accentColor = if (isBottleneck) Color(0xFFF43F5E) else Color(0xFF0EA5E9),
        isSelected = isSelected,
        textMeasurer = textMeasurer
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DStationKering(
    originX: Float,
    originY: Float,
    scale: Float,
    zBounce: Float,
    hasGasDryer: Boolean,
    clothesSway: Float,
    spinAngle: Float,
    ordersKg: Double,
    progressFrac: Float,
    isSelected: Boolean,
    isBottleneck: Boolean,
    textMeasurer: TextMeasurer
) {
    draw3DStationPad(
        originX, originY, scale,
        x = 4.6f, y = 0.7f, dx = 3.6f, dy = 2.2f,
        padColor = if (isBottleneck) Color(0xFFFFE4E6) else Color(0xFFFEF3C7),
        borderColor = if (isBottleneck) Color(0xFFF43F5E) else if (isSelected) Color(0xFFF59E0B) else Color(0xFFFDE68A)
    )

    if (hasGasDryer) {
        listOf(4.9f, 6.4f).forEachIndexed { idx, dxPos ->
            val dyPos = 0.90f
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = dxPos, y = dyPos, z = zBounce,
                dx = 1.22f, dy = 1.22f, dz = 1.60f,
                topColor = Color(0xFFFFFBEB),
                leftColor = Color(0xFFFEF3C7),
                rightColor = Color(0xFFFDE68A),
                strokeColor = Color(0xFFF59E0B)
            )
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = dxPos, y = dyPos, z = zBounce + 1.60f,
                dx = 1.22f, dy = 1.22f, dz = 0.30f,
                topColor = Color(0xFFFBBF24),
                leftColor = Color(0xFFF59E0B),
                rightColor = Color(0xFFD97706)
            )
            BlenderRenderer.drawIsoPortholeOnYFace(
                drawScope = this,
                originX = originX,
                originY = originY,
                scale = scale,
                centerX = dxPos + 0.61f,
                faceY = dyPos + 1.22f,
                centerZ = zBounce + 0.88f,
                radius = 0.40f,
                extrudeY = 0.13f,
                rimColor = Color(0xFFD97706),
                glassColor = Color(0xFFF59E0B),
                swirlColor = Color(0xFFFEF08A),
                spinDeg = -spinAngle + idx * 60f
            )
        }
    } else {
        // 3D Sun-Lit Clothes Drying Rack with Swaying Garments
        BlenderRenderer.drawIsoBox(
            this, originX, originY, scale,
            x = 4.9f, y = 1.45f, z = zBounce,
            dx = 0.22f, dy = 0.22f, dz = 1.95f,
            topColor = Color(0xFFFBBF24), leftColor = Color(0xFFD97706), rightColor = Color(0xFFB45309)
        )
        BlenderRenderer.drawIsoBox(
            this, originX, originY, scale,
            x = 7.6f, y = 1.45f, z = zBounce,
            dx = 0.22f, dy = 0.22f, dz = 1.95f,
            topColor = Color(0xFFFBBF24), leftColor = Color(0xFFD97706), rightColor = Color(0xFFB45309)
        )
        BlenderRenderer.drawIsoBox(
            this, originX, originY, scale,
            x = 4.85f, y = 1.43f, z = zBounce + 1.95f,
            dx = 3.0f, dy = 0.26f, dz = 0.18f,
            topColor = Color(0xFFFDE68A), leftColor = Color(0xFFF59E0B), rightColor = Color(0xFFD97706)
        )
        val shirtColors = listOf(Color(0xFFF43F5E), Color(0xFF0EA5E9), Color(0xFF10B981), Color(0xFF8B5CF6))
        val swayMult = if (ordersKg > 0) 1.4f else 0.7f
        for (i in 0..3) {
            val sc = shirtColors[i]
            val sx = 5.25f + i * 0.58f
            BlenderRenderer.drawIsoBox(
                this, originX, originY, scale,
                x = sx, y = 1.50f + clothesSway * swayMult * (if (i % 2 == 0) 1f else -1f), z = zBounce + 0.75f,
                dx = 0.40f, dy = 0.15f, dz = 1.15f,
                topColor = BlenderLighting.highlightColor(sc, 0.3f),
                leftColor = sc,
                rightColor = BlenderLighting.shadeColor(sc, 0.8f)
            )
        }
    }

    val badgePos = BlenderRenderer.isoPoint(originX, originY, scale, 6.7f, 0.6f, 2.50f + zBounce)
    drawClean3DBadge(
        centerX = badgePos.x,
        centerY = badgePos.y,
        stepNum = 3,
        title = if (hasGasDryer) "Dryer" else "Kering",
        value = "${ordersKg.toInt()}kg",
        progress = if (ordersKg > 0) progressFrac else 0f,
        accentColor = if (isBottleneck) Color(0xFFF43F5E) else Color(0xFFF59E0B),
        isSelected = isSelected,
        textMeasurer = textMeasurer
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DStationSetrika(
    originX: Float,
    originY: Float,
    scale: Float,
    zBounce: Float,
    hasBoiler: Boolean,
    steamFloat: Float,
    ironGlide: Float,
    ordersKg: Double,
    progressFrac: Float,
    isSelected: Boolean,
    isBottleneck: Boolean,
    textMeasurer: TextMeasurer
) {
    draw3DStationPad(
        originX, originY, scale,
        x = 3.6f, y = 3.5f, dx = 2.5f, dy = 2.2f,
        padColor = if (isBottleneck) Color(0xFFFFE4E6) else Color(0xFFFFF1F2),
        borderColor = if (isBottleneck || isSelected) Color(0xFFF43F5E) else Color(0xFFFECDD3)
    )

    // Table Legs
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 3.95f, y = 4.05f, z = zBounce,
        dx = 0.20f, dy = 0.80f, dz = 0.90f,
        topColor = Color(0xFFCBD5E1), leftColor = Color(0xFF94A3B8), rightColor = Color(0xFF64748B)
    )
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 5.25f, y = 4.05f, z = zBounce,
        dx = 0.20f, dy = 0.80f, dz = 0.90f,
        topColor = Color(0xFFCBD5E1), leftColor = Color(0xFF94A3B8), rightColor = Color(0xFF64748B)
    )

    // Ironing Board Top
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 3.80f, y = 3.90f, z = zBounce + 0.90f,
        dx = 1.80f, dy = 1.05f, dz = 0.22f,
        topColor = Color(0xFFF43F5E),
        leftColor = Color(0xFFE11D48),
        rightColor = Color(0xFFBE123C)
    )
    // Garment on Ironing Board
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 4.05f, y = 4.08f, z = zBounce + 1.12f,
        dx = 1.10f, dy = 0.68f, dz = 0.09f,
        topColor = Color(0xFFFEF08A),
        leftColor = Color(0xFFFACC15),
        rightColor = Color(0xFFEAB308)
    )
    // Active Gliding 3D Steam Iron (glides back and forth across garment!)
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 4.45f + ironGlide, y = 4.20f, z = zBounce + 1.21f,
        dx = 0.38f, dy = 0.32f, dz = 0.26f,
        topColor = Color.White,
        leftColor = Color(0xFF38BDF8),
        rightColor = Color(0xFF0284C7)
    )

    // True 3D Cylindrical Steam Boiler Tank beside table
    val boilerColor = if (hasBoiler) Color(0xFFF59E0B) else Color(0xFFFB7185)
    BlenderRenderer.drawIsoVerticalCylinder(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        centerX = 5.80f,
        centerY = 4.05f,
        baseZ = zBounce,
        radius = 0.25f,
        height = if (hasBoiler) 1.35f else 0.95f,
        topColor = BlenderLighting.highlightColor(boilerColor, 0.35f),
        bodyColor = boilerColor
    )

    // Rising Steam Puffs
    val s1 = BlenderRenderer.isoPoint(originX, originY, scale, 4.6f + ironGlide, 4.3f, zBounce + 1.55f + steamFloat * 0.65f)
    val s2 = BlenderRenderer.isoPoint(originX, originY, scale, 4.9f, 4.3f, zBounce + 1.80f + steamFloat * 0.85f)
    drawCircle(Color.White.copy(alpha = (1f - steamFloat) * 0.85f), radius = scale * 0.24f, center = s1)
    drawCircle(Color.White.copy(alpha = (1f - steamFloat) * 0.65f), radius = scale * 0.32f, center = s2)

    val badgePos = BlenderRenderer.isoPoint(originX, originY, scale, 4.8f, 3.8f, 2.30f + zBounce)
    drawClean3DBadge(
        centerX = badgePos.x,
        centerY = badgePos.y,
        stepNum = 4,
        title = "Setrika",
        value = "${ordersKg.toInt()}kg",
        progress = if (ordersKg > 0) progressFrac else 0f,
        accentColor = Color(0xFFF43F5E),
        isSelected = isSelected,
        textMeasurer = textMeasurer
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DStationMasuk(
    originX: Float,
    originY: Float,
    scale: Float,
    zBounce: Float,
    ordersKg: Double,
    progressFrac: Float,
    isSelected: Boolean,
    isBottleneck: Boolean,
    textMeasurer: TextMeasurer
) {
    draw3DStationPad(
        originX, originY, scale,
        x = 0.7f, y = 4.8f, dx = 2.5f, dy = 2.8f,
        padColor = if (isBottleneck) Color(0xFFFFE4E6) else Color(0xFFD1FAE5),
        borderColor = if (isBottleneck) Color(0xFFF43F5E) else if (isSelected) Color(0xFF10B981) else Color(0xFFA7F3D0)
    )

    // Reception Counter Base
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 0.9f, y = 5.1f, z = zBounce,
        dx = 1.9f, dy = 1.15f, dz = 0.95f,
        topColor = Color(0xFFFFFFFF),
        leftColor = Color(0xFF10B981),
        rightColor = Color(0xFF059669)
    )
    // Counter Top Countertop
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 0.85f, y = 5.05f, z = zBounce + 0.95f,
        dx = 2.0f, dy = 1.25f, dz = 0.16f,
        topColor = Color(0xFFFFFBEB),
        leftColor = Color(0xFFFDE68A),
        rightColor = Color(0xFFF59E0B)
    )
    // 3D Digital Scale on Counter
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 1.10f, y = 5.30f, z = zBounce + 1.11f,
        dx = 0.65f, dy = 0.65f, dz = 0.24f,
        topColor = Color(0xFF34D399),
        leftColor = Color(0xFF1E293B),
        rightColor = Color(0xFF0F172A)
    )

    // True 3D Cylindrical Laundry Hamper (height scales with incoming ordersKg!)
    val hamperH = (0.65f + (ordersKg.toFloat() * 0.04f).coerceAtMost(0.45f))
    BlenderRenderer.drawIsoVerticalCylinder(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        centerX = 2.35f,
        centerY = 6.85f,
        baseZ = zBounce,
        radius = 0.46f,
        height = hamperH,
        topColor = if (ordersKg > 0) Color(0xFFFDA4AF) else Color(0xFFBAE6FD),
        bodyColor = Color(0xFF0EA5E9)
    )

    val badgePos = BlenderRenderer.isoPoint(originX, originY, scale, 1.1f, 6.7f, 2.15f + zBounce)
    drawClean3DBadge(
        centerX = badgePos.x,
        centerY = badgePos.y,
        stepNum = 1,
        title = "Masuk",
        value = "${ordersKg.toInt()}kg",
        progress = if (ordersKg > 0) progressFrac else 0f,
        accentColor = if (isBottleneck) Color(0xFFF43F5E) else Color(0xFF10B981),
        isSelected = isSelected,
        textMeasurer = textMeasurer
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DStationPacking(
    originX: Float,
    originY: Float,
    scale: Float,
    zBounce: Float,
    ordersKg: Double,
    progressFrac: Float,
    isSelected: Boolean,
    isBottleneck: Boolean,
    textMeasurer: TextMeasurer
) {
    draw3DStationPad(
        originX, originY, scale,
        x = 6.3f, y = 3.9f, dx = 2.3f, dy = 2.4f,
        padColor = Color(0xFFEDE9FE),
        borderColor = if (isSelected) Color(0xFF8B5CF6) else Color(0xFFDDD6FE)
    )

    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 6.5f, y = 4.2f, z = zBounce,
        dx = 1.9f, dy = 1.4f, dz = 0.92f,
        topColor = Color(0xFFFFFFFF),
        leftColor = Color(0xFF8B5CF6),
        rightColor = Color(0xFF7C3AED)
    )
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 6.45f, y = 4.15f, z = zBounce + 0.92f,
        dx = 2.0f, dy = 1.5f, dz = 0.16f,
        topColor = Color(0xFFF5F3FF),
        leftColor = Color(0xFFDDD6FE),
        rightColor = Color(0xFFC4B5FD)
    )
    val stackColors = listOf(Color(0xFFEC4899), Color(0xFF38BDF8), Color(0xFFFBBF24))
    stackColors.forEachIndexed { idx, sc ->
        BlenderRenderer.drawIsoBox(
            this, originX, originY, scale,
            x = 6.65f + idx * 0.54f, y = 4.45f, z = zBounce + 1.08f,
            dx = 0.45f, dy = 0.58f, dz = 0.30f + (idx % 2) * 0.14f + (ordersKg.toFloat() * 0.02f).coerceAtMost(0.25f),
            topColor = BlenderLighting.highlightColor(sc, 0.3f),
            leftColor = sc,
            rightColor = BlenderLighting.shadeColor(sc, 0.82f)
        )
    }

    val badgePos = BlenderRenderer.isoPoint(originX, originY, scale, 7.9f, 5.1f, 2.15f + zBounce)
    drawClean3DBadge(
        centerX = badgePos.x,
        centerY = badgePos.y,
        stepNum = 5,
        title = "Packing",
        value = "${ordersKg.toInt()}kg",
        progress = if (ordersKg > 0) progressFrac else 0f,
        accentColor = Color(0xFF8B5CF6),
        isSelected = isSelected,
        textMeasurer = textMeasurer
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DStationSelesai(
    originX: Float,
    originY: Float,
    scale: Float,
    zBounce: Float,
    ordersKg: Double,
    completedTodayKg: Double,
    isSelected: Boolean,
    textMeasurer: TextMeasurer
) {
    draw3DStationPad(
        originX, originY, scale,
        x = 3.8f, y = 6.3f, dx = 2.6f, dy = 2.2f,
        padColor = Color(0xFFCCFBF1),
        borderColor = if (isSelected) Color(0xFF14B8A6) else Color(0xFF99F6E4)
    )

    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 4.1f, y = 6.6f, z = zBounce,
        dx = 2.0f, dy = 1.4f, dz = 0.42f,
        topColor = Color(0xFF99F6E4),
        leftColor = Color(0xFF14B8A6),
        rightColor = Color(0xFF0D9488)
    )
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 4.2f, y = 7.0f, z = zBounce + 0.42f,
        dx = 0.15f, dy = 0.15f, dz = 1.40f,
        topColor = Color.White, leftColor = Color(0xFFCBD5E1), rightColor = Color(0xFF94A3B8)
    )
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 5.8f, y = 7.0f, z = zBounce + 0.42f,
        dx = 0.15f, dy = 0.15f, dz = 1.40f,
        topColor = Color.White, leftColor = Color(0xFFCBD5E1), rightColor = Color(0xFF94A3B8)
    )
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = 4.18f, y = 6.98f, z = zBounce + 1.82f,
        dx = 1.80f, dy = 0.18f, dz = 0.14f,
        topColor = Color(0xFFFDE68A), leftColor = Color(0xFFF59E0B), rightColor = Color(0xFFD97706)
    )
    val bagColors = listOf(Color(0xFF10B981), Color(0xFF0EA5E9), Color(0xFF8B5CF6))
    for (b in 0..2) {
        val bc = bagColors[b]
        BlenderRenderer.drawIsoBox(
            this, originX, originY, scale,
            x = 4.42f + b * 0.44f, y = 6.85f, z = zBounce + 0.58f,
            dx = 0.34f, dy = 0.44f, dz = 1.15f,
            topColor = BlenderLighting.highlightColor(bc, 0.35f),
            leftColor = bc,
            rightColor = BlenderLighting.shadeColor(bc, 0.82f)
        )
    }

    val displayKg = max(ordersKg, completedTodayKg).toInt()
    val badgePos = BlenderRenderer.isoPoint(originX, originY, scale, 5.2f, 8.2f, 2.05f + zBounce)
    drawClean3DBadge(
        centerX = badgePos.x,
        centerY = badgePos.y,
        stepNum = 6,
        title = "Selesai",
        value = "${displayKg}kg",
        progress = if (displayKg > 0) 1f else 0f,
        accentColor = Color(0xFF0D9488),
        isSelected = isSelected,
        textMeasurer = textMeasurer
    )
}

private fun DrawScope.draw3DStationPad(
    originX: Float,
    originY: Float,
    scale: Float,
    x: Float,
    y: Float,
    dx: Float,
    dy: Float,
    padColor: Color,
    borderColor: Color
) {
    val p0 = BlenderRenderer.isoPoint(originX, originY, scale, x, y, 0.02f)
    val p1 = BlenderRenderer.isoPoint(originX, originY, scale, x + dx, y, 0.02f)
    val p2 = BlenderRenderer.isoPoint(originX, originY, scale, x + dx, y + dy, 0.02f)
    val p3 = BlenderRenderer.isoPoint(originX, originY, scale, x, y + dy, 0.02f)
    val path = Path().apply {
        moveTo(p0.x, p0.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        close()
    }
    drawPath(path, color = padColor.copy(alpha = 0.85f))
    drawPath(path, color = borderColor, style = Stroke(width = 2.0f))
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.draw3DIsoCharacters(
    originX: Float,
    originY: Float,
    scale: Float,
    workers: List<WorkerStaff>,
    bob: Float,
    armSwing: Float,
    hasIncomingCustomer: Boolean,
    textMeasurer: TextMeasurer
) {
    if (hasIncomingCustomer) {
        drawSingle3DIsoCharacter(
            originX = originX,
            originY = originY,
            scale = scale,
            x = 1.45f,
            y = 7.45f,
            zBob = bob * 0.6f,
            armSwing = armSwing * 0.5f,
            outfitColor = Color(0xFFEC4899),
            capColor = Color(0xFFF472B6),
            label = "Pelanggan",
            textMeasurer = textMeasurer
        )
    }

    workers.forEachIndexed { index, worker ->
        val (wx, wy) = when (worker.assignedPhase) {
            StationPhase.MASUK -> Pair(2.5f, 5.7f)
            StationPhase.CUCI -> Pair(2.1f + (index % 2) * 1.0f, 3.0f)
            StationPhase.JEMUR_KERING -> Pair(5.8f + (index % 2) * 1.0f, 3.0f)
            StationPhase.SETRIKA -> Pair(4.5f + (index % 2) * 0.9f, 5.5f)
            StationPhase.PACKING -> Pair(7.1f, 6.2f)
            StationPhase.SELESAI -> Pair(5.0f, 6.1f)
        }
        val zAnim = if (index % 2 == 0) bob else (0.22f - bob)
        val outfit = Color(worker.avatarColor)
        val statusBadge = if (worker.energyPercent <= 45) "😴Kopi!" else worker.moodEmoji
        drawSingle3DIsoCharacter(
            originX = originX,
            originY = originY,
            scale = scale,
            x = wx,
            y = wy,
            zBob = zAnim,
            armSwing = armSwing,
            outfitColor = outfit,
            capColor = BlenderLighting.highlightColor(outfit, 0.25f),
            label = "${worker.name.substringBefore(" ")} $statusBadge",
            textMeasurer = textMeasurer
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawSingle3DIsoCharacter(
    originX: Float,
    originY: Float,
    scale: Float,
    x: Float,
    y: Float,
    zBob: Float,
    armSwing: Float,
    outfitColor: Color,
    capColor: Color,
    label: String,
    textMeasurer: TextMeasurer
) {
    val groundPt = BlenderRenderer.isoPoint(originX, originY, scale, x + 0.22f, y + 0.22f, 0.03f)
    drawOval(
        color = Color(0x330F172A),
        topLeft = Offset(groundPt.x - scale * 0.38f, groundPt.y - scale * 0.18f),
        size = Size(scale * 0.76f, scale * 0.36f)
    )

    // 1. 3D Shoes/Legs
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = x, y = y, z = zBob,
        dx = 0.38f, dy = 0.38f, dz = 0.32f,
        topColor = Color(0xFF475569),
        leftColor = Color(0xFF334155),
        rightColor = Color(0xFF1E293B)
    )
    // 2. 3D Cylindrical Torso & Apron
    BlenderRenderer.drawIsoVerticalCylinder(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        centerX = x + 0.19f,
        centerY = y + 0.19f,
        baseZ = zBob + 0.32f,
        radius = 0.25f,
        height = 0.56f,
        topColor = BlenderLighting.highlightColor(outfitColor, 0.28f),
        bodyColor = outfitColor
    )
    // 2B. Active Swinging 3D Arms
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = x - 0.12f, y = y + 0.10f + armSwing * 0.25f, z = zBob + 0.52f,
        dx = 0.14f, dy = 0.24f, dz = 0.28f,
        topColor = Color(0xFFFFE4C4),
        leftColor = outfitColor,
        rightColor = BlenderLighting.shadeColor(outfitColor, 0.82f)
    )
    BlenderRenderer.drawIsoBox(
        this, originX, originY, scale,
        x = x + 0.36f, y = y + 0.10f - armSwing * 0.25f, z = zBob + 0.52f,
        dx = 0.14f, dy = 0.24f, dz = 0.28f,
        topColor = Color(0xFFFFE4C4),
        leftColor = outfitColor,
        rightColor = BlenderLighting.shadeColor(outfitColor, 0.82f)
    )
    // 3. 3D Stylized Head
    BlenderRenderer.drawIsoVerticalCylinder(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        centerX = x + 0.19f,
        centerY = y + 0.19f,
        baseZ = zBob + 0.88f,
        radius = 0.22f,
        height = 0.42f,
        topColor = Color(0xFFFFE4C4),
        bodyColor = Color(0xFFFFDAB9)
    )
    // 4. 3D Cap / Hair
    BlenderRenderer.drawIsoVerticalCylinder(
        drawScope = this,
        originX = originX,
        originY = originY,
        scale = scale,
        centerX = x + 0.19f,
        centerY = y + 0.19f,
        baseZ = zBob + 1.30f,
        radius = 0.24f,
        height = 0.14f,
        topColor = capColor,
        bodyColor = outfitColor
    )

    val headTop = BlenderRenderer.isoPoint(originX, originY, scale, x + 0.19f, y + 0.19f, zBob + 1.66f)
    val textResult = textMeasurer.measure(
        AnnotatedString(label),
        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
    )
    val bw = textResult.size.width + 10f
    val bh = textResult.size.height + 4f
    val bx = headTop.x - bw / 2f
    val by = headTop.y - bh / 2f

    drawRoundRect(
        color = Color.White.copy(alpha = 0.94f),
        topLeft = Offset(bx, by),
        size = Size(bw, bh),
        cornerRadius = CornerRadius(7f, 7f)
    )
    drawRoundRect(
        color = outfitColor,
        topLeft = Offset(bx, by),
        size = Size(bw, bh),
        cornerRadius = CornerRadius(7f, 7f),
        style = Stroke(width = 1.1f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(bx + 5f, by + 2f),
        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawClean3DBadge(
    centerX: Float,
    centerY: Float,
    stepNum: Int,
    title: String,
    value: String,
    progress: Float,
    accentColor: Color,
    isSelected: Boolean,
    textMeasurer: TextMeasurer
) {
    val label = "$stepNum.$title $value"
    val textResult = textMeasurer.measure(
        AnnotatedString(label),
        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    )
    val bw = textResult.size.width + 14f
    val bh = textResult.size.height + (if (progress > 0f) 10f else 6f)
    val bx = centerX - bw / 2f
    val by = centerY - bh / 2f

    drawRoundRect(
        color = Color(0x330F172A),
        topLeft = Offset(bx + 1.5f, by + 2f),
        size = Size(bw, bh),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(bx, by),
        size = Size(bw, bh),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        color = if (isSelected) Color(0xFFFEF08A) else Color.White.copy(alpha = 0.85f),
        topLeft = Offset(bx, by),
        size = Size(bw, bh),
        cornerRadius = CornerRadius(9f, 9f),
        style = Stroke(width = if (isSelected) 2.2f else 1.2f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(bx + 7f, by + 2.5f),
        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    )

    // Embedded mini progress bar inside the 3D station badge when active
    if (progress > 0f) {
        val barW = bw - 12f
        val barX = bx + 6f
        val barY = by + bh - 4.5f
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(barX, barY),
            size = Size(barW, 2.8f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFFFEF08A),
            topLeft = Offset(barX, barY),
            size = Size(barW * progress.coerceIn(0.1f, 1f), 2.8f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawFloatingParticlesClean(
    w: Float,
    h: Float,
    particles: List<FloatingParticle>,
    textMeasurer: TextMeasurer
) {
    val now = System.currentTimeMillis()
    particles.forEach { p ->
        val elapsed = (now - p.timestamp).toFloat().coerceIn(0f, 2500f)
        val progress = elapsed / 2500f
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val floatUpY = (p.yRatio * h) - (progress * 55f)
        val posX = (p.xRatio * w).coerceIn(70f, w - 70f)

        val textResult = textMeasurer.measure(
            AnnotatedString(p.text),
            style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = alpha))
        )
        val bw = textResult.size.width + 14f
        val bh = textResult.size.height + 7f

        drawRoundRect(
            color = Color(p.colorHex).copy(alpha = alpha * 0.95f),
            topLeft = Offset(posX - bw / 2f, floatUpY - bh / 2f),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = alpha),
            topLeft = Offset(posX - bw / 2f, floatUpY - bh / 2f),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 1.4f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = p.text,
            topLeft = Offset(posX - textResult.size.width / 2f, floatUpY - textResult.size.height / 2f),
            style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = alpha))
        )
    }
}
