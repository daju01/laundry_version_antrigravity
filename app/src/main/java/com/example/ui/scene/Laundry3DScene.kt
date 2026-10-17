package com.example.ui.scene

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.sim.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun Laundry3DScene(
    engine: SimulationEngine,
    selectedPhase: StationPhase?,
    onSelectPhase: (StationPhase) -> Unit,
    onInteractStation: ((StationPhase) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Smooth ambient animations
    val infiniteTransition = rememberInfiniteTransition(label = "tycoon_scene_anim")
    val washerSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "washer_spin"
    )
    val workerBob by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "worker_bob"
    )
    val steamFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam"
    )
    val clothesSway by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )
    val rainDropAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    val bottleneck = remember(engine.currentMinuteOfDay, engine.ordersList.size) {
        engine.diagnoseBottleneck()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp) // Much more prominent & immersive game view!
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF090D16))
            .testTag("laundry_3d_scene")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val nx = offset.x / width
                        val ny = offset.y / height

                        val tappedPhase = when {
                            // Masuk / Counter (Front left: x: 0.04-0.38, y: 0.52-0.96)
                            nx in 0.04f..0.38f && ny in 0.52f..0.96f -> StationPhase.MASUK
                            // Cuci / Bay (Back left: x: 0.04-0.38, y: 0.12-0.50)
                            nx in 0.04f..0.38f && ny in 0.12f..0.50f -> StationPhase.CUCI
                            // Jemur / Kering (Back middle-right: x: 0.40-0.74, y: 0.08-0.48)
                            nx in 0.40f..0.74f && ny in 0.08f..0.48f -> StationPhase.JEMUR_KERING
                            // Setrika (Center: x: 0.38-0.72, y: 0.50-0.90)
                            nx in 0.38f..0.72f && ny in 0.50f..0.90f -> StationPhase.SETRIKA
                            // Packing (Bottom right: x: 0.72-0.98, y: 0.52-0.96)
                            nx in 0.72f..0.98f && ny in 0.52f..0.96f -> StationPhase.PACKING
                            // Selesai (Top right: x: 0.74-0.98, y: 0.12-0.52)
                            nx in 0.74f..0.98f && ny in 0.12f..0.52f -> StationPhase.SELESAI
                            else -> null
                        }

                        if (tappedPhase != null) {
                            onInteractStation?.invoke(tappedPhase) ?: engine.interactWithStation(tappedPhase)
                            onSelectPhase(tappedPhase)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. ISOMETRIC ROOM & AMBIENT GAME LIGHTING
            drawIsometricTycoonRoom(
                w = w,
                h = h,
                weather = engine.weatherToday,
                rainAnim = rainDropAnim,
                growthTier = engine.growthTier,
                tycoonLevel = engine.tycoonLevel,
                textMeasurer = textMeasurer
            )

            // 2. STATIONS
            // STASIUN 1: MASUK (Counter, Timbangan, Pelanggan Menunggu)
            drawStationMasukGaming(
                w = w, h = h,
                ordersKg = engine.ordersList.filter { it.currentPhase == StationPhase.MASUK }.sumOf { it.weightKg },
                isSelected = selectedPhase == StationPhase.MASUK,
                isBottleneck = bottleneck.phase == StationPhase.MASUK,
                textMeasurer = textMeasurer
            )

            // STASIUN 2: CUCI (Mesin Cuci Bersih dengan Door Glow & Wave)
            val isWasherRunning = engine.equipmentList.any { it.isRunning }
            drawStationCuciGaming(
                w = w, h = h,
                isRunning = isWasherRunning,
                spinAngle = if (isWasherRunning) washerSpin else 0f,
                ordersKg = engine.ordersList.filter { it.currentPhase == StationPhase.CUCI }.sumOf { it.weightKg },
                isSelected = selectedPhase == StationPhase.CUCI,
                isBottleneck = bottleneck.phase == StationPhase.CUCI,
                textMeasurer = textMeasurer
            )

            // STASIUN 3: JEMUR / DRYER
            val hasGasDryer = engine.equipmentList.any { it.type == AssetType.DRYER_GAS_10KG }
            drawStationJemurGaming(
                w = w, h = h,
                hasGasDryer = hasGasDryer,
                weather = engine.weatherToday,
                clothesSway = clothesSway,
                ordersKg = engine.ordersList.filter { it.currentPhase == StationPhase.JEMUR_KERING }.sumOf { it.weightKg },
                isSelected = selectedPhase == StationPhase.JEMUR_KERING,
                isBottleneck = bottleneck.phase == StationPhase.JEMUR_KERING,
                textMeasurer = textMeasurer
            )

            // STASIUN 4: SETRIKA (Steam particle puffs, pile of clothes)
            drawStationSetrikaGaming(
                w = w, h = h,
                steamOffset = steamFloat,
                ordersKg = engine.ordersList.filter { it.currentPhase == StationPhase.SETRIKA }.sumOf { it.weightKg },
                isSelected = selectedPhase == StationPhase.SETRIKA,
                isBottleneck = bottleneck.phase == StationPhase.SETRIKA,
                textMeasurer = textMeasurer
            )

            // STASIUN 5: PACKING (Lipatan rapi, plastik roll)
            drawStationPackingGaming(
                w = w, h = h,
                ordersKg = engine.ordersList.filter { it.currentPhase == StationPhase.PACKING }.sumOf { it.weightKg },
                isSelected = selectedPhase == StationPhase.PACKING,
                isBottleneck = bottleneck.phase == StationPhase.PACKING,
                textMeasurer = textMeasurer
            )

            // STASIUN 6: SELESAI (Gantungan rack baju bersih)
            drawStationSelesaiGaming(
                w = w, h = h,
                ordersKg = engine.ordersList.filter { it.currentPhase == StationPhase.SELESAI }.sumOf { it.weightKg },
                isSelected = selectedPhase == StationPhase.SELESAI,
                isBottleneck = bottleneck.phase == StationPhase.SELESAI,
                textMeasurer = textMeasurer
            )

            // 3. WORKERS & CUSTOMER NPCS (Animated with bobbing & speech bubbles)
            drawWorkersAndCustomers(w, h, engine.workerList, workerBob, textMeasurer)

            // 4. FLOATING PARTICLES (+Rp 45.000, +25 XP, Cuci Selesai)
            drawFloatingParticles(w, h, engine.floatingParticles, textMeasurer)
        }

        // Top Left Tycoon Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xEE1E293B))
                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏪", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "${engine.growthTier.title} (Lv. ${engine.tycoonLevel})",
                    style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                )
                Text(
                    text = "${engine.weatherToday.icon} ${engine.weatherToday.label}",
                    style = TextStyle(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                )
            }
        }

        // Top Right: Combo Flame & Throughput Capacity Pill
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Combo Badge
            if (engine.taskComboCount > 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFDC2626))))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 COMBO x${engine.taskComboCount}!",
                        style = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                    )
                }
            }

            // Throughput Capacity
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE1E293B))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🚀 Kapasitas: ${engine.growthTier.maxDailyCapacityTargetKg.toInt()} kg/hari",
                    style = TextStyle(color = Color(0xFFA7F3D0), fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                )
            }
        }

        // Tap Hint / Station Quick Jump Badge (Bottom Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xDD0284C7))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "⚡ Ketuk Stasiun = Upgrade",
                style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
        }
    }
}

// -------------------------------------------------------------
// ISOMETRIC TYCOON ROOM DRAWING
// -------------------------------------------------------------
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawIsometricTycoonRoom(
    w: Float,
    h: Float,
    weather: Weather,
    rainAnim: Float,
    growthTier: GrowthTier,
    tycoonLevel: Int,
    textMeasurer: TextMeasurer
) {
    // Wall Background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
            startY = 0f, endY = h * 0.44f
        ),
        topLeft = Offset(0f, 0f),
        size = Size(w, h * 0.44f)
    )

    // Back Wall Neon Tycoon Branding Plaque (Reflects expanded tier!)
    val signW = w * 0.42f
    val signH = 26f
    val signX = w * 0.04f
    val signY = h * 0.04f
    drawRoundRect(
        color = if (tycoonLevel >= 2) Color(0xFF0284C7).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.35f),
        topLeft = Offset(signX - 4f, signY - 4f),
        size = Size(signW + 8f, signH + 8f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(
            if (tycoonLevel >= 2) listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF38BDF8))
            else listOf(Color(0xFF1E293B), Color(0xFF334155))
        ),
        topLeft = Offset(signX, signY),
        size = Size(signW, signH),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = if (tycoonLevel >= 2) Color(0xFFFDE68A) else Color(0xFF64748B),
        topLeft = Offset(signX, signY),
        size = Size(signW, signH),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.2f)
    )
    val signText = textMeasurer.measure(
        AnnotatedString("⚡ ${growthTier.title.uppercase()} • ${growthTier.maxDailyCapacityTargetKg.toInt()} KG/HARI"),
        style = TextStyle(color = if (tycoonLevel >= 2) Color(0xFFFDE68A) else Color(0xFFCBD5E1), fontSize = 7.5.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textLayoutResult = signText,
        topLeft = Offset(signX + (signW - signText.size.width) / 2f, signY + (signH - signText.size.height) / 2f)
    )

    // Window with Sky view
    val winX = w * 0.48f
    val winY = h * 0.04f
    val winW = w * 0.32f
    val winH = h * 0.24f

    val skyColors = when (weather) {
        Weather.CERAH -> listOf(Color(0xFF38BDF8), Color(0xFF7DD3FC))
        Weather.MENDUNG -> listOf(Color(0xFF64748B), Color(0xFF94A3B8))
        Weather.HUJAN -> listOf(Color(0xFF1E293B), Color(0xFF334155))
    }
    drawRoundRect(
        brush = Brush.verticalGradient(skyColors, startY = winY, endY = winY + winH),
        topLeft = Offset(winX, winY),
        size = Size(winW, winH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Sunlight glow if sunny
    if (weather == Weather.CERAH) {
        drawCircle(Color(0x66FDE047), radius = winH * 0.35f, center = Offset(winX + winW * 0.8f, winY + winH * 0.3f))
    }

    // Window white frame
    drawRoundRect(
        color = Color(0xFFF8FAFC),
        topLeft = Offset(winX, winY),
        size = Size(winW, winH),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 3.5f)
    )
    drawLine(Color(0xFFE2E8F0), Offset(winX + winW / 2, winY), Offset(winX + winW / 2, winY + winH), strokeWidth = 2.5f)
    drawLine(Color(0xFFE2E8F0), Offset(winX, winY + winH / 2), Offset(winX + winW, winY + winH / 2), strokeWidth = 2.5f)

    // Animated rain lines if rainy
    if (weather == Weather.HUJAN) {
        for (i in 0 until 8) {
            val rx = winX + 8f + (i * (winW - 16f) / 8f)
            val ry = winY + ((rainAnim + (i * 24)) % winH)
            drawLine(
                color = Color(0xCCBAE6FD),
                start = Offset(rx, ry),
                end = Offset(rx - 5f, ry + 16f),
                strokeWidth = 2f
            )
        }
    }

    // Isometric Wood & Tile Floor with shiny gradient (Upgraded ceramic colors if Lv 2+)
    val floorPath = Path().apply {
        moveTo(0f, h * 0.44f)
        lineTo(w, h * 0.44f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = floorPath,
        brush = Brush.verticalGradient(
            colors = if (tycoonLevel >= 2) listOf(Color(0xFF0F2537), Color(0xFF1E293B), Color(0xFF0B132B))
                     else listOf(Color(0xFF334155), Color(0xFF1E293B)),
            startY = h * 0.44f, endY = h
        )
    )

    // Shiny isometric tile grid lines (Refined when upgraded)
    val lines = 8
    val gridColor = if (tycoonLevel >= 2) Color(0x4038BDF8) else Color(0x2838BDF8)
    for (i in 0..lines) {
        val y = h * 0.44f + (i * (h * 0.56f) / lines)
        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = if (tycoonLevel >= 2) 1.5f else 1.2f)
    }
    for (i in 0..lines) {
        val x = (i * w) / lines
        drawLine(gridColor, Offset(x, h * 0.44f), Offset(x, h), strokeWidth = if (tycoonLevel >= 2) 1.5f else 1.2f)
    }

    // Expansion Wing Divider Accent Line (Highlights expanded factory space)
    if (tycoonLevel >= 2) {
        drawLine(
            color = Color(0xFFF59E0B).copy(alpha = 0.5f),
            start = Offset(w * 0.52f, h * 0.44f),
            end = Offset(w * 0.52f, h),
            strokeWidth = 2f
        )
    }
}

// -------------------------------------------------------------
// STATIONS DRAWINGS (GAMIFIED)
// -------------------------------------------------------------

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStationMasukGaming(
    w: Float, h: Float, ordersKg: Double, isSelected: Boolean, isBottleneck: Boolean, textMeasurer: TextMeasurer
) {
    val x = w * 0.06f
    val y = h * 0.60f
    val sw = w * 0.30f
    val sh = h * 0.30f

    // Glow pulse if selected or bottleneck
    if (isSelected || isBottleneck) {
        drawRoundRect(
            color = if (isBottleneck) Color(0x99EF4444) else Color(0x8838BDF8),
            topLeft = Offset(x - 8f, y - 8f),
            size = Size(sw + 16f, sh + 16f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    // Wooden counter desk
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309))),
        topLeft = Offset(x, y + 14f),
        size = Size(sw, sh - 14f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = Color(0xFFFDE68A),
        topLeft = Offset(x, y),
        size = Size(sw, sh * 0.38f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    // Digital scale with glowing LED screen
    drawRoundRect(Color(0xFF334155), Offset(x + 8f, y + 6f), Size(sw * 0.45f, 16f), CornerRadius(4f, 4f))
    drawRect(Color(0xFF22C55E), Offset(x + 12f, y + 10f), Size(sw * 0.22f, 8f))

    // Big Laundry Basket with colorful clothes
    val basketX = x + sw * 0.52f
    val basketY = y + 4f
    drawRoundRect(Color(0xFF0284C7), Offset(basketX, basketY), Size(sw * 0.42f, sh * 0.48f), CornerRadius(8f, 8f))
    // Clothes spilling over top
    drawCircle(Color(0xFFEF4444), radius = 7f, center = Offset(basketX + 10f, basketY - 2f))
    drawCircle(Color(0xFFFBBF24), radius = 8f, center = Offset(basketX + 22f, basketY - 4f))
    drawCircle(Color(0xFF8B5CF6), radius = 6f, center = Offset(basketX + 32f, basketY - 1f))

    drawGameStationBadge(x + sw / 2, y - 14f, "🧺 Masuk", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFF10B981), textMeasurer)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStationCuciGaming(
    w: Float, h: Float, isRunning: Boolean, spinAngle: Float, ordersKg: Double, isSelected: Boolean, isBottleneck: Boolean, textMeasurer: TextMeasurer
) {
    val x = w * 0.06f
    val y = h * 0.16f
    val sw = w * 0.30f
    val sh = h * 0.36f

    // Blender Ambient Shadow cast on floor
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(x - 4f, y + sh - 8f),
        size = Size(sw + 8f, 16f)
    )

    if (isSelected || isBottleneck) {
        drawRoundRect(
            color = if (isBottleneck) Color(0x99EF4444) else Color(0x8838BDF8),
            topLeft = Offset(x - 8f, y - 8f),
            size = Size(sw + 16f, sh + 16f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    // High-tech Blender-modeled washing machine cabinet (Chamfered with 3D Depth)
    // Left shading facet (3D side depth)
    val sideWidth = 8f
    val sidePath = Path().apply {
        moveTo(x, y + 14f)
        lineTo(x + sideWidth, y)
        lineTo(x + sideWidth, y + sh)
        lineTo(x, y + sh)
        close()
    }
    drawPath(sidePath, brush = Brush.verticalGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B))))

    // Front Cabinet Face
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFCBD5E1), Color(0xFF94A3B8))),
        topLeft = Offset(x + sideWidth, y),
        size = Size(sw - sideWidth, sh),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // Blender Specular Rim Light (Chamfer Edge)
    drawLine(
        color = Color.White.copy(alpha = 0.85f),
        start = Offset(x + sideWidth, y + 2f),
        end = Offset(x + sw - 6f, y + 2f),
        strokeWidth = 2f
    )

    // Control panel with brushed metal & glowing LED digital display
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))),
        topLeft = Offset(x + sideWidth, y),
        size = Size(sw - sideWidth, sh * 0.22f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawCircle(if (isRunning) Color(0xFF22C55E) else Color(0xFFEF4444), radius = 5f, center = Offset(x + sideWidth + 14f, y + sh * 0.11f))
    drawCircle(Color(0xFF38BDF8), radius = 4f, center = Offset(x + sw - 14f, y + sh * 0.11f))

    // Drum Glass with Blender Chrome Bezel Rim
    val drumCenter = Offset(x + sideWidth + (sw - sideWidth) / 2, y + sh * 0.62f)
    val drumRadius = (sw - sideWidth) * 0.35f

    // Outer Chrome Rim with Bevel Shading
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFFF8FAFC), Color(0xFF475569), Color(0xFFE2E8F0)),
            center = drumCenter
        ),
        radius = drumRadius,
        center = drumCenter
    )
    // Dark Inner Drum Chamber
    drawCircle(
        color = if (isRunning) Color(0xFF0369A1) else Color(0xFF0B132B),
        radius = drumRadius - 4.5f,
        center = drumCenter
    )

    // Animated water swirl, detergent bubbles, and vortex if running
    if (isRunning) {
        drawArc(
            brush = Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF60A5FA), Color(0xFF0284C7))),
            startAngle = spinAngle,
            sweepAngle = 240f,
            useCenter = true,
            topLeft = Offset(drumCenter.x - drumRadius + 6f, drumCenter.y - drumRadius + 6f),
            size = Size((drumRadius - 6f) * 2, (drumRadius - 6f) * 2)
        )
        // Blender-style curved glass reflection highlight
        drawArc(
            color = Color.White.copy(alpha = 0.5f),
            startAngle = 210f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(drumCenter.x - drumRadius + 7f, drumCenter.y - drumRadius + 7f),
            size = Size((drumRadius - 7f) * 2, (drumRadius - 7f) * 2),
            style = Stroke(width = 3f)
        )
        // Bubble particles
        drawCircle(Color.White, radius = 4f, center = Offset(drumCenter.x + 8f, drumCenter.y - 6f))
        drawCircle(Color.White, radius = 3f, center = Offset(drumCenter.x - 10f, drumCenter.y + 8f))
    }

    drawGameStationBadge(x + sw / 2, y - 14f, "🫧 Cuci", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFF0284C7), textMeasurer)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStationJemurGaming(
    w: Float, h: Float, hasGasDryer: Boolean, weather: Weather, clothesSway: Float, ordersKg: Double, isSelected: Boolean, isBottleneck: Boolean, textMeasurer: TextMeasurer
) {
    val x = w * 0.42f
    val y = h * 0.14f
    val sw = w * 0.32f
    val sh = h * 0.34f

    if (isSelected || isBottleneck) {
        drawRoundRect(
            color = if (isBottleneck) Color(0x99EF4444) else Color(0x88F59E0B),
            topLeft = Offset(x - 8f, y - 8f),
            size = Size(sw + 16f, sh + 16f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    if (hasGasDryer) {
        // Futuristic Gas Dryer
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8))),
            topLeft = Offset(x, y),
            size = Size(sw, sh),
            cornerRadius = CornerRadius(14f, 14f)
        )
        // Warm glowing orange drum
        val center = Offset(x + sw / 2, y + sh * 0.58f)
        drawCircle(Color(0xFF78350F), radius = sw * 0.32f, center = center)
        drawCircle(Color(0xFFF59E0B), radius = sw * 0.28f, center = center)
        drawCircle(Color(0xFFFEF3C7), radius = sw * 0.14f, center = center)
        drawGameStationBadge(x + sw / 2, y - 14f, "🔥 Dryer Gas", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFFF59E0B), textMeasurer)
    } else {
        // Outdoor Clothesline Rack with fluttering clothes
        drawLine(Color(0xFF64748B), Offset(x + 6f, y + 10f), Offset(x + 6f, y + sh), strokeWidth = 5f)
        drawLine(Color(0xFF64748B), Offset(x + sw - 6f, y + 10f), Offset(x + sw - 6f, y + sh), strokeWidth = 5f)
        drawLine(Color(0xFFE2E8F0), Offset(x + 6f, y + 20f), Offset(x + sw - 6f, y + 20f), strokeWidth = 2.5f)

        // Hanging shirts swaying with wind
        val colors = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B))
        for (i in 0 until 4) {
            val sx = x + 16f + (i * (sw - 40f) / 3f)
            val sy = y + 22f
            drawRoundRect(
                color = colors[i],
                topLeft = Offset(sx + (clothesSway * (i + 1) * 0.25f), sy),
                size = Size(16f, sh * 0.50f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        drawGameStationBadge(x + sw / 2, y - 14f, "👕 Jemuran", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFFF59E0B), textMeasurer)
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStationSetrikaGaming(
    w: Float, h: Float, steamOffset: Float, ordersKg: Double, isSelected: Boolean, isBottleneck: Boolean, textMeasurer: TextMeasurer
) {
    val x = w * 0.40f
    val y = h * 0.56f
    val sw = w * 0.32f
    val sh = h * 0.32f

    if (isSelected || isBottleneck) {
        drawRoundRect(
            color = if (isBottleneck) Color(0x99EF4444) else Color(0x88EF4444),
            topLeft = Offset(x - 8f, y - 8f),
            size = Size(sw + 16f, sh + 16f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    // Padded blue ironing board
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
        topLeft = Offset(x, y + 16f),
        size = Size(sw, sh * 0.40f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    // Metallic X legs
    drawLine(Color(0xFF94A3B8), Offset(x + 12f, y + 16f + sh * 0.40f), Offset(x + sw - 12f, y + sh), strokeWidth = 3.5f)
    drawLine(Color(0xFF94A3B8), Offset(x + sw - 12f, y + 16f + sh * 0.40f), Offset(x + 12f, y + sh), strokeWidth = 3.5f)

    // Steam Iron unit
    drawRoundRect(Color(0xFFF59E0B), Offset(x + sw * 0.58f, y + 20f), Size(24f, 14f), CornerRadius(4f, 4f))

    // Animated Steam Puffs
    drawCircle(Color(0x99FFFFFF), radius = 5f, center = Offset(x + sw * 0.68f, y + 12f - steamOffset * 0.6f))
    drawCircle(Color(0x66FFFFFF), radius = 8f, center = Offset(x + sw * 0.72f, y + 2f - steamOffset))

    drawGameStationBadge(x + sw / 2, y - 12f, "💨 Setrika", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFFEF4444), textMeasurer)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStationPackingGaming(
    w: Float, h: Float, ordersKg: Double, isSelected: Boolean, isBottleneck: Boolean, textMeasurer: TextMeasurer
) {
    val x = w * 0.74f
    val y = h * 0.58f
    val sw = w * 0.24f
    val sh = h * 0.30f

    if (isSelected || isBottleneck) {
        drawRoundRect(
            color = if (isBottleneck) Color(0x99EF4444) else Color(0x888B5CF6),
            topLeft = Offset(x - 8f, y - 8f),
            size = Size(sw + 16f, sh + 16f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    // Packing table
    drawRoundRect(Color(0xFF475569), Offset(x, y + 12f), Size(sw, sh - 12f), CornerRadius(10f, 10f))
    drawRoundRect(Color(0xFFE2E8F0), Offset(x, y), Size(sw, sh * 0.40f), CornerRadius(8f, 8f))

    // Folded clothes with purple plastic wrap
    drawRoundRect(Color(0xDD8B5CF6), Offset(x + 8f, y + 6f), Size(sw * 0.70f, 16f), CornerRadius(4f, 4f))
    drawCircle(Color(0xFFFBBF24), radius = 4f, center = Offset(x + sw * 0.78f, y + 14f))

    drawGameStationBadge(x + sw / 2, y - 12f, "📦 Packing", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFF8B5CF6), textMeasurer)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStationSelesaiGaming(
    w: Float, h: Float, ordersKg: Double, isSelected: Boolean, isBottleneck: Boolean, textMeasurer: TextMeasurer
) {
    val x = w * 0.74f
    val y = h * 0.16f
    val sw = w * 0.24f
    val sh = h * 0.36f

    if (isSelected || isBottleneck) {
        drawRoundRect(
            color = if (isBottleneck) Color(0x99EF4444) else Color(0x880D9488),
            topLeft = Offset(x - 8f, y - 8f),
            size = Size(sw + 16f, sh + 16f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    }

    // High garment rolling rack
    drawRoundRect(Color(0xFF64748B), Offset(x, y), Size(sw, sh), CornerRadius(8f, 8f), style = Stroke(width = 3.5f))
    // Hanging clean bags (Translucent teal & green)
    drawRoundRect(Color(0xCC10B981), Offset(x + 6f, y + 12f), Size(14f, sh - 22f), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xCC0D9488), Offset(x + 24f, y + 12f), Size(14f, sh - 22f), CornerRadius(4f, 4f))

    drawGameStationBadge(x + sw / 2, y - 14f, "✨ Selesai", "${ordersKg.toInt()} kg", if (isBottleneck) Color(0xFFEF4444) else Color(0xFF0D9488), textMeasurer)
}

// -------------------------------------------------------------
// WORKERS & CUSTOMER NPCS WITH MOOD EMOJIS & WALKING ANIMATION
// -------------------------------------------------------------
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawWorkersAndCustomers(
    w: Float, h: Float, workers: List<WorkerStaff>, bob: Float, textMeasurer: TextMeasurer
) {
    // 1. Draw Customer NPC at Counter
    val custX = w * 0.08f
    val custY = h * 0.88f
    drawCircle(Color(0xFFFFCC80), radius = 9f, center = Offset(custX, custY - 14f))
    drawRoundRect(Color(0xFFEC4899), Offset(custX - 8f, custY - 5f), Size(16f, 18f), CornerRadius(4f, 4f))
    drawSpeechBubble(custX, custY - 32f, "Bawa Cucian 🧺", textMeasurer)

    // 2. Draw Workers at stations with bobbing footsteps
    workers.forEachIndexed { index, worker ->
        val (baseX, baseY) = when (worker.assignedPhase) {
            StationPhase.MASUK -> Pair(w * 0.28f, h * 0.82f)
            StationPhase.CUCI -> Pair(w * 0.30f, h * 0.38f)
            StationPhase.JEMUR_KERING -> Pair(w * 0.62f, h * 0.36f)
            StationPhase.SETRIKA -> Pair(w * 0.54f, h * 0.78f)
            StationPhase.PACKING -> Pair(w * 0.86f, h * 0.82f)
            StationPhase.SELESAI -> Pair(w * 0.86f, h * 0.42f)
        }

        val yPos = baseY + (bob * (if (index % 2 == 0) 1f else -1f))

        // Shadow under worker
        drawOval(Color(0x44000000), Offset(baseX - 10f, baseY + 12f), Size(20f, 6f))

        // Worker Head & Hat
        drawCircle(Color(0xFFFFDBAC), radius = 10f, center = Offset(baseX, yPos - 14f))
        drawCircle(Color(worker.avatarColor), radius = 6f, center = Offset(baseX, yPos - 21f)) // Headband/Cap

        // Apron body
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(worker.avatarColor), Color(0xFF1E293B))),
            topLeft = Offset(baseX - 9f, yPos - 4f),
            size = Size(18f, 20f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Mood Speech Bubble
        drawSpeechBubble(baseX, yPos - 38f, "${worker.name.substringBefore(" ")} ${worker.moodEmoji}", textMeasurer)
    }
}

// -------------------------------------------------------------
// FLOATING PARTICLES (REWARDS, +Rp, +XP)
// -------------------------------------------------------------
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawFloatingParticles(
    w: Float, h: Float, particles: List<FloatingParticle>, textMeasurer: TextMeasurer
) {
    val now = System.currentTimeMillis()
    particles.forEach { p ->
        val elapsed = (now - p.timestamp).toFloat().coerceIn(0f, 2500f)
        val progress = elapsed / 2500f
        val alpha = (1f - progress).coerceIn(0f, 1f)
        val floatUpY = (p.yRatio * h) - (progress * 50f)
        val posX = p.xRatio * w

        val textResult = textMeasurer.measure(
            AnnotatedString(p.text),
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(p.colorHex).copy(alpha = alpha))
        )
        val bw = textResult.size.width + 12f
        val bh = textResult.size.height + 6f

        drawRoundRect(
            color = Color(0xCC0F172A).copy(alpha = alpha),
            topLeft = Offset(posX - bw / 2, floatUpY - bh / 2),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = p.text,
            topLeft = Offset(posX - textResult.size.width / 2, floatUpY - textResult.size.height / 2),
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(p.colorHex).copy(alpha = alpha))
        )
    }
}

// Speech Bubble Helper
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawSpeechBubble(x: Float, y: Float, text: String, textMeasurer: TextMeasurer) {
    val textResult = textMeasurer.measure(
        AnnotatedString(text),
        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    )
    val bw = textResult.size.width + 10f
    val bh = textResult.size.height + 4f
    val bx = x - bw / 2
    val by = y - bh / 2

    drawRoundRect(Color.White, Offset(bx, by), Size(bw, bh), CornerRadius(6f, 6f))
    drawText(textMeasurer, text, Offset(bx + 5f, by + 2f), TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
}

// Station Badge Helper
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawGameStationBadge(
    centerX: Float, centerY: Float, title: String, value: String, badgeColor: Color, textMeasurer: TextMeasurer
) {
    val label = "$title: $value"
    val textResult = textMeasurer.measure(
        AnnotatedString(label),
        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    )
    val bw = textResult.size.width + 14f
    val bh = textResult.size.height + 6f
    val bx = centerX - bw / 2
    val by = centerY - bh / 2

    // Shadow
    drawRoundRect(Color(0x88000000), Offset(bx + 2f, by + 2f), Size(bw, bh), CornerRadius(8f, 8f))
    // Badge pill
    drawRoundRect(badgeColor, Offset(bx, by), Size(bw, bh), CornerRadius(8f, 8f))
    drawText(textMeasurer, label, Offset(bx + 7f, by + 3f), TextStyle(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
}
