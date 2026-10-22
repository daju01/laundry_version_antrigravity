package com.example.ui.cockpit

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sim.LevelUpCelebration
import com.example.ui.theme.*

@Composable
fun LevelUpCelebrationDialog(
    celebration: LevelUpCelebration,
    onClaim: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.3f) }
    LaunchedEffect(celebration) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(onDismissRequest = onClaim) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(SunnyAmber, OceanBluePrimary, MintEmerald)),
                width = 2.5.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value)
                .testTag("level_up_celebration_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Brush.horizontalGradient(listOf(SunnyAmber, CoralRose)))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
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
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = celebration.newTierTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Slate900
                    )
                )
                Text(
                    text = "Naik kelas dari ${celebration.oldTierTitle}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate500,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3D Isometric Expansion Diorama Stage
                CapacityUpgrade3DTransition(
                    celebration = celebration,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Capacity Expansion Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MintTintBg),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MintTintBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = MintEmeraldDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KAPASITAS THROUGHPUT HARIAN",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MintEmeraldDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${celebration.oldCapacityKg.toInt()} kg",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("➔", fontSize = 20.sp, fontWeight = FontWeight.Black, color = SunnyAmberDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${celebration.newCapacityKg.toInt()} kg/hari",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                color = MintEmeraldDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unlocked Perks
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate50),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎁 FASILITAS & BONUS TERBUKA:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlueDeep
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        celebration.unlockedPerks.forEach { perk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = perk,
                                    fontSize = 11.sp,
                                    color = Slate800,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        com.example.audio.GameSoundManager.playKaChing()
                        onClaim()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_claim_level_up")
                ) {
                    Text(
                        text = "KLAIM BONUS & LANJUTKAN! 🚀",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
