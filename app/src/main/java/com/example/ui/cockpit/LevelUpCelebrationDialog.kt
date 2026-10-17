package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.LevelUpCelebration
import com.example.sim.formatMoney
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LevelUpCelebrationDialog(
    celebration: LevelUpCelebration,
    onClaim: () -> Unit
) {
    // Entrance scale animation with spring
    val scaleAnim = remember { Animatable(0.2f) }
    LaunchedEffect(celebration) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Infinite rotating sunburst rays
    val infiniteTransition = rememberInfiniteTransition(label = "sunburst_transition")
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst_rotation"
    )

    // Pulse for glow
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Dialog(onDismissRequest = onClaim) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFF38BDF8), Color(0xFF10B981))), RoundedCornerShape(24.dp))
                .testTag("level_up_celebration_dialog")
        ) {
            // Background Canvas: Sunburst Rays & Confetti
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                val center = Offset(size.width / 2f, size.height * 0.28f)
                val rayCount = 16
                val rayLength = size.width.coerceAtLeast(size.height) * 1.2f

                // Draw sunburst rays
                for (i in 0 until rayCount) {
                    val angleDeg = (i * (360f / rayCount)) + rotationDegrees
                    val angleRad1 = (angleDeg - 7f) * (PI / 180f).toFloat()
                    val angleRad2 = (angleDeg + 7f) * (PI / 180f).toFloat()

                    val p1 = Offset(
                        center.x + (cos(angleRad1) * rayLength),
                        center.y + (sin(angleRad1) * rayLength)
                    )
                    val p2 = Offset(
                        center.x + (cos(angleRad2) * rayLength),
                        center.y + (sin(angleRad2) * rayLength)
                    )

                    val rayPath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }

                    drawPath(
                        path = rayPath,
                        color = Color(0xFFF59E0B).copy(alpha = 0.08f)
                    )
                }

                // Decorative celebratory confetti dots
                val confettiColors = listOf(
                    Color(0xFFFBBF24), Color(0xFF38BDF8), Color(0xFF34D399),
                    Color(0xFFF472B6), Color(0xFFA78BFA), Color(0xFFFB923C)
                )
                for (k in 0 until 24) {
                    val cx = (size.width * ((k * 17) % 100) / 100f)
                    val cy = (size.height * ((k * 23) % 100) / 100f)
                    val dotColor = confettiColors[k % confettiColors.size].copy(alpha = 0.55f)
                    drawCircle(color = dotColor, radius = 4f, center = Offset(cx, cy))
                }
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Badge: LEVEL UP!
                Box(
                    modifier = Modifier
                        .scale(glowPulse)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFBBF24))))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LEVEL UP! TINGKAT ${celebration.newLevelNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Banner: Tier Progression
                Text(
                    text = celebration.newTierTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDE68A)
                    )
                )
                Text(
                    text = "Naik kelas dari ${celebration.oldTierTitle}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // DYNAMIC 3D WORKSHOP EXPANSION STAGE & TRANSITION
                CapacityUpgrade3DTransition(
                    celebration = celebration,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // HIGHLIGHT: THROUGHPUT CAPACITY EXPANSION CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KAPASITAS THROUGHPUT PRODUKSI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Capacity Numbers with Arrow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Old Capacity
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Lama", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(
                                    "${celebration.oldCapacityKg.toInt()} kg",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text("per hari", fontSize = 9.sp, color = Color(0xFF64748B))
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            Text("➔", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(16.dp))

                            // New Capacity
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Kapasitas Baru", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                                Text(
                                    "${celebration.newCapacityKg.toInt()} kg",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399)
                                )
                                Text("per hari", fontSize = 9.sp, color = Color(0xFF34D399))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val increasePercent = (((celebration.newCapacityKg - celebration.oldCapacityKg) / celebration.oldCapacityKg) * 100).toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF065F46))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🚀 +$increasePercent% LONJAKAN KAPASITAS HARIAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFA7F3D0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // UNLOCKED PERKS & REWARDS
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎁 FASILITAS & BONUS TERBUKA:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        celebration.unlockedPerks.forEach { perk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = perk,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // BIG GAMING CLAIM BUTTON
                Button(
                    onClick = {
                        com.example.audio.GameSoundManager.playKaChing()
                        onClaim()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_claim_level_up")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "KLAIM BONUS & GASPOLL! 🚀",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}
