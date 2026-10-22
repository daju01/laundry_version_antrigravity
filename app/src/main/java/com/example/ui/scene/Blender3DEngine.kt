package com.example.ui.scene

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*

/**
 * True 3D Isometric Voxel / Low-Poly Diorama Engine & Stylized Toon/PBR Shader.
 * Supports multi-box 3D primitives, true isometric foreshortened cylindrical drums,
 * vertical cylinders, isometric cutaway rooms, and global painter's algorithm depth sorting.
 */
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vec3 {
        val l = length()
        return if (l > 0.0001f) Vec3(x / l, y / l, z / l) else Vec3(0f, 0f, 1f)
    }

    fun rotateZ(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }

    fun rotateX(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }
}

data class BlenderFace(
    val vertexIndices: List<Int>,
    val baseColor: Color,
    val roughness: Float = 0.35f,
    val metallic: Float = 0.2f,
    val emission: Color? = null,
    val normal: Vec3? = null
)

class BlenderMesh(
    val vertices: List<Vec3>,
    val faces: List<BlenderFace>
) {
    fun mergeWith(other: BlenderMesh, offset: Vec3 = Vec3(0f, 0f, 0f)): BlenderMesh {
        val baseIdx = vertices.size
        val combinedVerts = vertices + other.vertices.map { it + offset }
        val combinedFaces = faces + other.faces.map { f ->
            f.copy(vertexIndices = f.vertexIndices.map { it + baseIdx })
        }
        return BlenderMesh(combinedVerts, combinedFaces)
    }
}

object BlenderLighting {
    val keyLightDir = Vec3(-0.45f, -0.60f, 0.66f).normalized()
    val fillLightDir = Vec3(0.65f, 0.45f, 0.60f).normalized()

    fun shadeColor(base: Color, brightnessFactor: Float): Color {
        val r = (base.red * brightnessFactor).coerceIn(0f, 1f)
        val g = (base.green * brightnessFactor).coerceIn(0f, 1f)
        val b = (base.blue * brightnessFactor).coerceIn(0f, 1f)
        return Color(r, g, b, base.alpha)
    }

    fun highlightColor(base: Color, mixWhite: Float): Color {
        val m = mixWhite.coerceIn(0f, 1f)
        return Color(
            red = (base.red * (1f - m) + m).coerceIn(0f, 1f),
            green = (base.green * (1f - m) + m).coerceIn(0f, 1f),
            blue = (base.blue * (1f - m) + m).coerceIn(0f, 1f),
            alpha = base.alpha
        )
    }

    fun computeShading(
        normal: Vec3,
        baseColor: Color,
        roughness: Float,
        metallic: Float,
        emission: Color? = null,
        aoFactor: Float = 1f
    ): Color {
        if (emission != null) return emission

        val n = normal.normalized()
        val keyDot = max(0f, n.dot(keyLightDir))
        val fillDot = max(0f, n.dot(fillLightDir))
        val upDot = max(0f, n.z)

        val viewDir = Vec3(0.0f, 0.707f, 0.707f).normalized()
        val halfVec = (keyLightDir + viewDir).normalized()
        val specDot = max(0f, n.dot(halfVec))
        val specPower = (1f - roughness) * 24f + 6f
        val specular = specDot.pow(specPower) * (1f - roughness) * (0.25f + metallic * 0.25f)

        val lightLevel = (0.68f + 0.26f * keyDot + 0.14f * fillDot + 0.12f * upDot) * aoFactor.coerceIn(0.75f, 1.05f)

        val r = (baseColor.red * lightLevel + specular).coerceIn(0f, 1f)
        val g = (baseColor.green * lightLevel + specular).coerceIn(0f, 1f)
        val b = (baseColor.blue * lightLevel + specular * 1.05f).coerceIn(0f, 1f)

        return Color(r, g, b, baseColor.alpha)
    }
}

object BlenderModelFactory {

    fun createBoxMesh(
        width: Float,
        depth: Float,
        height: Float,
        topColor: Color,
        frontColor: Color,
        sideColor: Color = BlenderLighting.shadeColor(frontColor, 0.86f),
        roughness: Float = 0.3f,
        metallic: Float = 0.15f,
        emission: Color? = null
    ): BlenderMesh {
        val hw = width / 2f
        val hd = depth / 2f
        val verts = listOf(
            Vec3(-hw, -hd, 0f),
            Vec3(hw, -hd, 0f),
            Vec3(hw, hd, 0f),
            Vec3(-hw, hd, 0f),
            Vec3(-hw, -hd, height),
            Vec3(hw, -hd, height),
            Vec3(hw, hd, height),
            Vec3(-hw, hd, height)
        )
        val faces = listOf(
            BlenderFace(listOf(0, 3, 2, 1), BlenderLighting.shadeColor(sideColor, 0.6f), roughness = 0.8f, normal = Vec3(0f, 0f, -1f)),
            BlenderFace(listOf(4, 5, 6, 7), topColor, roughness = roughness, metallic = metallic, emission = emission, normal = Vec3(0f, 0f, 1f)),
            BlenderFace(listOf(0, 1, 5, 4), sideColor, roughness = roughness, metallic = metallic, normal = Vec3(0f, -1f, 0f)),
            BlenderFace(listOf(0, 4, 7, 3), sideColor, roughness = roughness, metallic = metallic, normal = Vec3(-1f, 0f, 0f)),
            BlenderFace(listOf(1, 2, 6, 5), sideColor, roughness = roughness, metallic = metallic, normal = Vec3(1f, 0f, 0f)),
            BlenderFace(listOf(3, 7, 6, 2), frontColor, roughness = roughness, metallic = metallic, emission = emission, normal = Vec3(0f, 1f, 0f))
        )
        return BlenderMesh(verts, faces)
    }

    fun createChamferedWasherMesh(
        width: Float,
        depth: Float,
        height: Float,
        bodyColor: Color,
        topColor: Color,
        accentColor: Color
    ): BlenderMesh {
        val pedestalH = height * 0.10f
        val mainH = height * 0.74f
        val headerH = height * 0.16f

        var mesh = createBoxMesh(
            width = width * 0.96f,
            depth = depth * 0.96f,
            height = pedestalH,
            topColor = Color(0xFFCBD5E1),
            frontColor = Color(0xFF94A3B8),
            sideColor = Color(0xFF64748B)
        )

        val bodyMesh = createBoxMesh(
            width = width,
            depth = depth,
            height = mainH,
            topColor = topColor,
            frontColor = bodyColor,
            sideColor = BlenderLighting.shadeColor(bodyColor, 0.88f),
            roughness = 0.22f,
            metallic = 0.25f
        )
        mesh = mesh.mergeWith(bodyMesh, Vec3(0f, 0f, pedestalH))

        val fasciaMesh = createBoxMesh(
            width = width,
            depth = depth,
            height = headerH,
            topColor = BlenderLighting.highlightColor(accentColor, 0.25f),
            frontColor = accentColor,
            sideColor = BlenderLighting.shadeColor(accentColor, 0.85f),
            roughness = 0.2f,
            metallic = 0.3f
        )
        mesh = mesh.mergeWith(fasciaMesh, Vec3(0f, 0f, pedestalH + mainH))

        val hd = depth / 2f
        val drumRadius = min(width, height) * 0.30f
        val drumCenterZ = pedestalH + mainH * 0.52f
        val segs = 16

        val drumVerts = mutableListOf<Vec3>()
        val drumFaces = mutableListOf<BlenderFace>()

        for (i in 0 until segs) {
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            drumVerts.add(Vec3(cos(a) * drumRadius, hd + 0.5f, drumCenterZ + sin(a) * drumRadius))
        }
        for (i in 0 until segs) {
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            drumVerts.add(Vec3(cos(a) * drumRadius * 0.95f, hd + 3.5f, drumCenterZ + sin(a) * drumRadius * 0.95f))
        }
        for (i in 0 until segs) {
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            drumVerts.add(Vec3(cos(a) * drumRadius * 0.72f, hd + 2.5f, drumCenterZ + sin(a) * drumRadius * 0.72f))
        }

        for (i in 0 until segs) {
            val next = (i + 1) % segs
            drumFaces.add(
                BlenderFace(
                    listOf(i, next, segs + next, segs + i),
                    accentColor,
                    roughness = 0.15f,
                    metallic = 0.7f,
                    normal = Vec3(cos(i * 2f * PI.toFloat() / segs), 0.5f, sin(i * 2f * PI.toFloat() / segs)).normalized()
                )
            )
            drumFaces.add(
                BlenderFace(
                    listOf(segs + i, segs + next, 2 * segs + next, 2 * segs + i),
                    Color(0xFFF8FAFC),
                    roughness = 0.1f,
                    metallic = 0.85f,
                    normal = Vec3(0f, 1f, 0.15f).normalized()
                )
            )
        }

        drumFaces.add(
            BlenderFace(
                (0 until segs).map { 2 * segs + it },
                Color(0xFF0EA5E9),
                roughness = 0.05f,
                metallic = 0.1f,
                emission = Color(0xFF38BDF8),
                normal = Vec3(0f, 1f, 0f)
            )
        )

        return mesh.mergeWith(BlenderMesh(drumVerts, drumFaces))
    }

    fun createBlenderDryerMesh(
        width: Float,
        depth: Float,
        height: Float,
        primaryColor: Color = Color(0xFFF59E0B)
    ): BlenderMesh {
        var mesh = createChamferedWasherMesh(
            width = width,
            depth = depth,
            height = height,
            bodyColor = Color(0xFFFFFBEB),
            topColor = Color(0xFFFEF3C7),
            accentColor = primaryColor
        )
        val duct = createBoxMesh(
            width = width * 0.45f,
            depth = depth * 0.45f,
            height = height * 0.14f,
            topColor = Color(0xFFFDE68A),
            frontColor = primaryColor,
            sideColor = Color(0xFFD97706)
        )
        mesh = mesh.mergeWith(duct, Vec3(0f, -depth * 0.15f, height))
        return mesh
    }
}

object BlenderRenderer {

    var cameraSkewX: Float = 0f

    fun projectIso(
        v: Vec3,
        centerX: Float,
        centerY: Float,
        zoom: Float,
        yawRad: Float,
        pitchRad: Float = 0.5235f
    ): Offset {
        val rz = v.rotateZ(yawRad)
        val rx = rz.rotateX(pitchRad)
        val screenX = centerX + rx.x * zoom
        val screenY = centerY - rx.y * 0.577f * zoom - rx.z * zoom
        return Offset(screenX, screenY)
    }

    fun isoPoint(originX: Float, originY: Float, scale: Float, x: Float, y: Float, z: Float): Offset {
        val sy = originY + (x + y) * 0.5f * scale - z * scale
        val sx = originX + (x - y) * 0.866f * scale + (sy - originY) * cameraSkewX
        return Offset(sx, sy)
    }

    fun drawIsoFloorRing(
        drawScope: DrawScope,
        originX: Float,
        originY: Float,
        scale: Float,
        centerX: Float,
        centerY: Float,
        z: Float,
        radius: Float,
        color: Color,
        strokeWidth: Float,
        fillAlpha: Float = 0f
    ) {
        val segments = 24
        val path = Path()
        for (i in 0..segments) {
            val angle = (i * 2.0 * PI / segments).toFloat()
            val px = centerX + radius * cos(angle)
            val py = centerY + radius * sin(angle)
            val pt = isoPoint(originX, originY, scale, px, py, z)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        path.close()
        if (fillAlpha > 0f) {
            drawScope.drawPath(path, color = color.copy(alpha = fillAlpha), style = Fill)
        }
        if (strokeWidth > 0f) {
            drawScope.drawPath(path, color = color, style = Stroke(width = strokeWidth))
        }
    }

    fun drawIsoConveyorTrack(
        drawScope: DrawScope,
        originX: Float,
        originY: Float,
        scale: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        flowProgress: Float,
        trackColor: Color
    ) {
        val pStart = isoPoint(originX, originY, scale, x1, y1, 0.03f)
        val pEnd = isoPoint(originX, originY, scale, x2, y2, 0.03f)

        // Draw track shadow/outer rail
        drawScope.drawLine(
            color = trackColor.copy(alpha = 0.28f),
            start = pStart,
            end = pEnd,
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Draw core animated dashed belt conveyor
        drawScope.drawLine(
            color = trackColor.copy(alpha = 0.75f),
            start = pStart,
            end = pEnd,
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), flowProgress * 20f),
            cap = StrokeCap.Round
        )

        // Pulse dot moving along the track
        val dotX = x1 + (x2 - x1) * flowProgress
        val dotY = y1 + (y2 - y1) * flowProgress
        val dotPt = isoPoint(originX, originY, scale, dotX, dotY, 0.05f)
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 3.5f,
            center = dotPt
        )
    }

    /**
     * Draws a 3D Isometric Prism/Box directly in world coordinates (x, y, z)
     * with automatic top, left-front (+Y), and right-front (+X) shading and bevel edges.
     */
    fun drawIsoBox(
        drawScope: DrawScope,
        originX: Float,
        originY: Float,
        scale: Float,
        x: Float,
        y: Float,
        z: Float,
        dx: Float,
        dy: Float,
        dz: Float,
        topColor: Color,
        leftColor: Color,
        rightColor: Color,
        strokeColor: Color? = Color.White.copy(alpha = 0.40f),
        strokeWidth: Float = 1.1f
    ) {
        if (dx <= 0f || dy <= 0f || dz <= 0f) return

        val p000 = isoPoint(originX, originY, scale, x, y, z)
        val p100 = isoPoint(originX, originY, scale, x + dx, y, z)
        val p110 = isoPoint(originX, originY, scale, x + dx, y + dy, z)
        val p010 = isoPoint(originX, originY, scale, x, y + dy, z)

        val p001 = isoPoint(originX, originY, scale, x, y, z + dz)
        val p101 = isoPoint(originX, originY, scale, x + dx, y, z + dz)
        val p111 = isoPoint(originX, originY, scale, x + dx, y + dy, z + dz)
        val p011 = isoPoint(originX, originY, scale, x, y + dy, z + dz)

        // Left-Front Face (facing +Y: p010 -> p110 -> p111 -> p011)
        val leftPath = Path().apply {
            moveTo(p010.x, p010.y)
            lineTo(p110.x, p110.y)
            lineTo(p111.x, p111.y)
            lineTo(p011.x, p011.y)
            close()
        }
        drawScope.drawPath(leftPath, color = leftColor)

        // Right-Front Face (facing +X: p100 -> p110 -> p111 -> p101)
        val rightPath = Path().apply {
            moveTo(p100.x, p100.y)
            lineTo(p110.x, p110.y)
            lineTo(p111.x, p111.y)
            lineTo(p101.x, p101.y)
            close()
        }
        drawScope.drawPath(rightPath, color = rightColor)

        // Top Face (facing +Z: p001 -> p101 -> p111 -> p011)
        val topPath = Path().apply {
            moveTo(p001.x, p001.y)
            lineTo(p101.x, p101.y)
            lineTo(p111.x, p111.y)
            lineTo(p011.x, p011.y)
            close()
        }
        drawScope.drawPath(topPath, color = topColor)

        if (strokeColor != null) {
            drawScope.drawPath(topPath, color = strokeColor, style = Stroke(width = strokeWidth))
            drawScope.drawPath(leftPath, color = strokeColor.copy(alpha = strokeColor.alpha * 0.65f), style = Stroke(width = strokeWidth * 0.85f))
            drawScope.drawPath(rightPath, color = strokeColor.copy(alpha = strokeColor.alpha * 0.65f), style = Stroke(width = strokeWidth * 0.85f))
        }
    }

    /**
     * Draws a true 3D Isometric Extruded Circular Porthole Drum on a +Y facing appliance front face.
     * The circle is foreshortened in true 30° isometric perspective along the X-Z plane!
     */
    fun drawIsoPortholeOnYFace(
        drawScope: DrawScope,
        originX: Float,
        originY: Float,
        scale: Float,
        centerX: Float,
        faceY: Float,
        centerZ: Float,
        radius: Float,
        extrudeY: Float = 0.14f,
        rimColor: Color,
        glassColor: Color,
        swirlColor: Color? = null,
        spinDeg: Float = 0f
    ) {
        val segs = 20
        // 1. Extruded outer rim cylinder sides
        val backPts = (0 until segs).map { i ->
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            isoPoint(originX, originY, scale, centerX + cos(a) * radius, faceY, centerZ + sin(a) * radius)
        }
        val frontPts = (0 until segs).map { i ->
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            isoPoint(originX, originY, scale, centerX + cos(a) * radius, faceY + extrudeY, centerZ + sin(a) * radius)
        }
        val innerPts = (0 until segs).map { i ->
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            isoPoint(originX, originY, scale, centerX + cos(a) * (radius * 0.74f), faceY + extrudeY * 0.8f, centerZ + sin(a) * (radius * 0.74f))
        }

        for (i in 0 until segs) {
            val next = (i + 1) % segs
            val quad = Path().apply {
                moveTo(backPts[i].x, backPts[i].y)
                lineTo(backPts[next].x, backPts[next].y)
                lineTo(frontPts[next].x, frontPts[next].y)
                lineTo(frontPts[i].x, frontPts[i].y)
                close()
            }
            val shade = if (sin((i.toFloat() / segs) * 2f * PI.toFloat()) > 0f) {
                BlenderLighting.highlightColor(rimColor, 0.2f)
            } else {
                BlenderLighting.shadeColor(rimColor, 0.82f)
            }
            drawScope.drawPath(quad, color = shade)
        }

        // 2. Front Bevel Ring
        val outerRingPath = Path().apply {
            moveTo(frontPts[0].x, frontPts[0].y)
            for (i in 1 until segs) lineTo(frontPts[i].x, frontPts[i].y)
            close()
        }
        drawScope.drawPath(outerRingPath, color = Color.White)
        drawScope.drawPath(outerRingPath, color = rimColor, style = Stroke(width = 1.5f))

        // 3. Inner Porthole Glass Dome
        val glassPath = Path().apply {
            moveTo(innerPts[0].x, innerPts[0].y)
            for (i in 1 until segs) lineTo(innerPts[i].x, innerPts[i].y)
            close()
        }
        drawScope.drawPath(glassPath, color = glassColor)

        // 4. Optional Animated 3D Isometric Swirl inside Drum
        if (swirlColor != null) {
            val startRad = (spinDeg * PI / 180f).toFloat()
            val swirlPath = Path().apply {
                val centerPt = isoPoint(originX, originY, scale, centerX, faceY + extrudeY * 0.8f, centerZ)
                moveTo(centerPt.x, centerPt.y)
                val arcSteps = 10
                for (k in 0..arcSteps) {
                    val a = startRad + (k.toFloat() / arcSteps) * PI.toFloat() * 1.15f
                    val pt = isoPoint(
                        originX, originY, scale,
                        centerX + cos(a) * (radius * 0.65f),
                        faceY + extrudeY * 0.8f,
                        centerZ + sin(a) * (radius * 0.65f)
                    )
                    lineTo(pt.x, pt.y)
                }
                close()
            }
            drawScope.drawPath(swirlPath, color = swirlColor)
        }

        // Specular glass highlight crescent
        val specPt = isoPoint(originX, originY, scale, centerX - radius * 0.25f, faceY + extrudeY, centerZ + radius * 0.28f)
        drawScope.drawCircle(color = Color.White.copy(alpha = 0.75f), radius = scale * radius * 0.18f, center = specPt)
    }

    /**
     * Draws a true 3D Vertical Cylinder in isometric space (e.g. for Steam Boilers, Round Hampers, Characters).
     */
    fun drawIsoVerticalCylinder(
        drawScope: DrawScope,
        originX: Float,
        originY: Float,
        scale: Float,
        centerX: Float,
        centerY: Float,
        baseZ: Float,
        radius: Float,
        height: Float,
        topColor: Color,
        bodyColor: Color
    ) {
        val segs = 16
        val bottomPts = (0 until segs).map { i ->
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            isoPoint(originX, originY, scale, centerX + cos(a) * radius, centerY + sin(a) * radius, baseZ)
        }
        val topPts = (0 until segs).map { i ->
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            isoPoint(originX, originY, scale, centerX + cos(a) * radius, centerY + sin(a) * radius, baseZ + height)
        }

        // Draw front-facing cylinder segments
        for (i in 0 until segs) {
            val next = (i + 1) % segs
            val midAngle = ((i + 0.5f) / segs) * 2f * PI.toFloat()
            // In isometric view, front-facing segments have cos(a) + sin(a) > -0.2
            if (cos(midAngle) + sin(midAngle) > -0.35f) {
                val quad = Path().apply {
                    moveTo(bottomPts[i].x, bottomPts[i].y)
                    lineTo(bottomPts[next].x, bottomPts[next].y)
                    lineTo(topPts[next].x, topPts[next].y)
                    lineTo(topPts[i].x, topPts[i].y)
                    close()
                }
                val light = (0.82f + 0.18f * (sin(midAngle) - cos(midAngle))).coerceIn(0.72f, 1.05f)
                drawScope.drawPath(quad, color = BlenderLighting.shadeColor(bodyColor, light))
            }
        }

        // Top Cap Ellipse
        val topPath = Path().apply {
            moveTo(topPts[0].x, topPts[0].y)
            for (i in 1 until segs) lineTo(topPts[i].x, topPts[i].y)
            close()
        }
        drawScope.drawPath(topPath, color = topColor)
        drawScope.drawPath(topPath, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 1.1f))
    }

    fun renderMesh(
        drawScope: DrawScope,
        mesh: BlenderMesh,
        worldOffset: Vec3,
        centerX: Float,
        centerY: Float,
        zoom: Float,
        yawRad: Float,
        pitchRad: Float = 0.5235f,
        wireframeOverlay: Boolean = false,
        ambientOcclusion: Float = 0.15f
    ) {
        val transformedVerts = mesh.vertices.map { v ->
            val worldV = v + worldOffset
            val rotatedZ = worldV.rotateZ(yawRad)
            val finalV = rotatedZ.rotateX(pitchRad)
            Pair(finalV, projectIso(worldV, centerX, centerY, zoom, yawRad, pitchRad))
        }

        val sortedFaces = mesh.faces.mapIndexed { index, face ->
            val avgDepth = face.vertexIndices.sumOf {
                val fv = transformedVerts[it].first
                (fv.y - fv.z * 0.35f).toDouble()
            } / face.vertexIndices.size
            Triple(index, face, avgDepth)
        }.sortedBy { it.third }

        for ((_, face, _) in sortedFaces) {
            if (face.vertexIndices.size < 3) continue

            val p0 = mesh.vertices[face.vertexIndices[0]]
            val p1 = mesh.vertices[face.vertexIndices[1]]
            val p2 = mesh.vertices[face.vertexIndices[2]]
            val calculatedNormal = (p1 - p0).cross(p2 - p0).normalized()
            val faceNormal = (face.normal ?: calculatedNormal).rotateZ(yawRad).rotateX(pitchRad)

            val avgLocalZ = face.vertexIndices.map { mesh.vertices[it].z }.average().toFloat()
            val aoFactor = 1f - (ambientOcclusion * (1f - (avgLocalZ / 42f).coerceIn(0f, 1f)))

            val shadedColor = BlenderLighting.computeShading(
                normal = faceNormal,
                baseColor = face.baseColor,
                roughness = face.roughness,
                metallic = face.metallic,
                emission = face.emission,
                aoFactor = aoFactor
            )

            val path = Path().apply {
                val firstPt = transformedVerts[face.vertexIndices[0]].second
                moveTo(firstPt.x, firstPt.y)
                for (i in 1 until face.vertexIndices.size) {
                    val pt = transformedVerts[face.vertexIndices[i]].second
                    lineTo(pt.x, pt.y)
                }
                close()
            }

            drawScope.drawPath(path = path, color = shadedColor)
            drawScope.drawPath(
                path = path,
                color = if (wireframeOverlay) Color(0xFF0EA5E9).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.28f),
                style = Stroke(width = (if (wireframeOverlay) 1.5f else 1.0f) * zoom)
            )
        }
    }
}
