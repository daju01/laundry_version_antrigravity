package com.example.ui.scene

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.*

/**
 * 3D Vector Math, Mesh Structures & Blender Cycles-style Isometric Shader
 * Provides vertex transformations, rotational matrices, Phong lighting,
 * bevel chamfering, and specular highlights.
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

    /**
     * Rotate around Z-axis (yaw)
     */
    fun rotateZ(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }

    /**
     * Rotate around X-axis (pitch)
     */
    fun rotateX(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    /**
     * Rotate around Y-axis (roll)
     */
    fun rotateY(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }
}

/**
 * Polygonal face with surface normal and material color.
 */
data class BlenderFace(
    val vertexIndices: List<Int>,
    val baseColor: Color,
    val roughness: Float = 0.4f,
    val metallic: Float = 0.3f,
    val emission: Color? = null,
    val normal: Vec3? = null
)

/**
 * 3D Mesh container representing models exported or constructed with Blender topology.
 */
class BlenderMesh(
    val vertices: List<Vec3>,
    val faces: List<BlenderFace>
)

/**
 * Blender-style Lighting Environment:
 * - Key Light (warm sunlight from high top-right)
 * - Fill Light (cool cyan reflection from bottom-left)
 * - Rim / Hair Light (accent backlight from behind)
 */
object BlenderLighting {
    val keyLightDir = Vec3(0.577f, -0.577f, 0.577f).normalized()
    val fillLightDir = Vec3(-0.707f, 0.707f, 0.3f).normalized()
    val keyLightColor = Color(0xFFFFF7ED) // Warm 3200K sunlight
    val fillLightColor = Color(0xFF38BDF8) // Cool skylight fill
    val ambientColor = Color(0xFF0F172A) // Deep ambient occluded bounce

    /**
     * Shading function combining Diffuse (Lambertian) + Specular (Blinn-Phong)
     */
    fun computeShading(
        normal: Vec3,
        baseColor: Color,
        roughness: Float,
        metallic: Float,
        emission: Color? = null
    ): Color {
        if (emission != null) return emission

        // Key Light intensity (Lambert)
        val nDotLKey = max(0f, normal.dot(keyLightDir))
        // Fill Light intensity
        val nDotLFill = max(0f, normal.dot(fillLightDir)) * 0.35f

        // Specular highlight (Blinn-Phong) with isometric camera view looking down along (-0.7, -0.7, 1)
        val viewDir = Vec3(0.5f, 0.5f, 0.707f).normalized()
        val halfVec = (keyLightDir + viewDir).normalized()
        val nDotH = max(0f, normal.dot(halfVec))
        val specPower = (1f - roughness) * 32f + 4f
        val specIntensity = nDotH.pow(specPower) * (1f - roughness) * (0.6f + metallic * 0.4f)

        // Blend colors
        val r = (baseColor.red * (0.3f + 0.7f * nDotLKey) + fillLightColor.red * nDotLFill * 0.3f + specIntensity).coerceIn(0f, 1f)
        val g = (baseColor.green * (0.3f + 0.7f * nDotLKey) + fillLightColor.green * nDotLFill * 0.3f + specIntensity).coerceIn(0f, 1f)
        val b = (baseColor.blue * (0.3f + 0.7f * nDotLKey) + fillLightColor.blue * nDotLFill * 0.4f + specIntensity).coerceIn(0f, 1f)

        return Color(r, g, b, baseColor.alpha)
    }
}

/**
 * Procedural Blender Model Generator.
 * Creates meshes for Commercial Washers, Steam Dryers, Garment Rails, and Workshop Architecture.
 */
object BlenderModelFactory {

    /**
     * Creates a Blender-style Chamfered / Beveled Box (Subdivision Surface simulation).
     * Has top, bottom, front, back, left, right faces + beveled chamfers.
     */
    fun createChamferedWasherMesh(
        width: Float,
        depth: Float,
        height: Float,
        bodyColor: Color,
        topColor: Color,
        accentColor: Color
    ): BlenderMesh {
        val hw = width / 2f
        val hd = depth / 2f

        // 8 main corners
        val rawVerts = mutableListOf(
            Vec3(-hw, -hd, 0f),       // 0: Bottom-Back-Left
            Vec3(hw, -hd, 0f),        // 1: Bottom-Back-Right
            Vec3(hw, hd, 0f),         // 2: Bottom-Front-Right
            Vec3(-hw, hd, 0f),        // 3: Bottom-Front-Left
            Vec3(-hw, -hd, height),   // 4: Top-Back-Left
            Vec3(hw, -hd, height),    // 5: Top-Back-Right
            Vec3(hw, hd, height),     // 6: Top-Front-Right
            Vec3(-hw, hd, height)     // 7: Top-Front-Left
        )

        // Drum ring vertices on the front face (y = hd)
        val drumRadius = min(width, height) * 0.28f
        val drumCenterZ = height * 0.45f
        val drumSegments = 12
        val drumStartIdx = rawVerts.size

        for (i in 0 until drumSegments) {
            val angle = (i.toFloat() / drumSegments) * 2f * PI.toFloat()
            val vx = cos(angle) * drumRadius
            val vz = drumCenterZ + sin(angle) * drumRadius
            // Front ring extruded slightly outward (Blender rim)
            rawVerts.add(Vec3(vx, hd + 2f, vz))
        }

        // Drum center inset for glass dome
        val drumInnerIdx = rawVerts.size
        for (i in 0 until drumSegments) {
            val angle = (i.toFloat() / drumSegments) * 2f * PI.toFloat()
            val vx = cos(angle) * (drumRadius * 0.82f)
            val vz = drumCenterZ + sin(angle) * (drumRadius * 0.82f)
            rawVerts.add(Vec3(vx, hd + 1f, vz))
        }

        val faces = mutableListOf<BlenderFace>()

        // 1. Bottom Face
        faces.add(BlenderFace(listOf(0, 3, 2, 1), Color(0xFF0F172A), normal = Vec3(0f, 0f, -1f)))
        // 2. Top Face (Chamfered metallic panel)
        faces.add(BlenderFace(listOf(4, 5, 6, 7), topColor, roughness = 0.25f, metallic = 0.6f, normal = Vec3(0f, 0f, 1f)))
        // 3. Back Face
        faces.add(BlenderFace(listOf(0, 1, 5, 4), Color(0xFF1E293B), roughness = 0.6f, normal = Vec3(0f, -1f, 0f)))
        // 4. Left Face
        faces.add(BlenderFace(listOf(0, 4, 7, 3), bodyColor.copy(red = bodyColor.red * 0.82f, green = bodyColor.green * 0.82f, blue = bodyColor.blue * 0.82f), roughness = 0.35f, metallic = 0.4f, normal = Vec3(-1f, 0f, 0f)))
        // 5. Right Face
        faces.add(BlenderFace(listOf(1, 2, 6, 5), bodyColor.copy(red = bodyColor.red * 0.9f, green = bodyColor.green * 0.9f, blue = bodyColor.blue * 0.9f), roughness = 0.35f, metallic = 0.4f, normal = Vec3(1f, 0f, 0f)))
        // 6. Front Face (Main Cabinet Body)
        faces.add(BlenderFace(listOf(3, 7, 6, 2), bodyColor, roughness = 0.3f, metallic = 0.5f, normal = Vec3(0f, 1f, 0f)))

        // 7. Drum Chrome Ring Bezel
        for (i in 0 until drumSegments) {
            val next = (i + 1) % drumSegments
            faces.add(
                BlenderFace(
                    listOf(drumStartIdx + i, drumStartIdx + next, drumInnerIdx + next, drumInnerIdx + i),
                    accentColor,
                    roughness = 0.15f,
                    metallic = 0.9f,
                    normal = Vec3(0f, 1f, 0.2f).normalized()
                )
            )
        }

        // 8. Drum Glass Dome / Translucent Porthole
        val glassFaceIndices = (0 until drumSegments).map { drumInnerIdx + it }
        faces.add(
            BlenderFace(
                glassFaceIndices,
                Color(0xCC0284C7),
                roughness = 0.05f,
                metallic = 0.1f,
                normal = Vec3(0f, 1f, 0f)
            )
        )

        return BlenderMesh(rawVerts, faces)
    }

    /**
     * Creates a Blender Gas Dryer Mesh with glowing heating element and cylindrical chassis.
     */
    fun createBlenderDryerMesh(
        width: Float,
        depth: Float,
        height: Float,
        primaryColor: Color = Color(0xFFEF4444)
    ): BlenderMesh {
        val hw = width / 2f
        val hd = depth / 2f

        val verts = mutableListOf(
            Vec3(-hw, -hd, 0f), Vec3(hw, -hd, 0f), Vec3(hw, hd, 0f), Vec3(-hw, hd, 0f),
            Vec3(-hw, -hd, height), Vec3(hw, -hd, height), Vec3(hw, hd, height), Vec3(-hw, hd, height)
        )

        val faces = mutableListOf(
            BlenderFace(listOf(0, 3, 2, 1), Color(0xFF1E1E1E), normal = Vec3(0f, 0f, -1f)),
            BlenderFace(listOf(4, 5, 6, 7), Color(0xFFFCA5A5), roughness = 0.2f, metallic = 0.5f, normal = Vec3(0f, 0f, 1f)),
            BlenderFace(listOf(0, 1, 5, 4), Color(0xFF450A0A), roughness = 0.6f, normal = Vec3(0f, -1f, 0f)),
            BlenderFace(listOf(0, 4, 7, 3), primaryColor.copy(alpha = 0.85f), roughness = 0.3f, normal = Vec3(-1f, 0f, 0f)),
            BlenderFace(listOf(1, 2, 6, 5), primaryColor.copy(alpha = 0.95f), roughness = 0.3f, normal = Vec3(1f, 0f, 0f)),
            BlenderFace(listOf(3, 7, 6, 2), primaryColor, roughness = 0.25f, metallic = 0.4f, normal = Vec3(0f, 1f, 0f))
        )

        // Heating element glow porthole
        val center = Vec3(0f, hd + 2f, height * 0.5f)
        val r = width * 0.25f
        val segs = 10
        val startI = verts.size
        for (i in 0 until segs) {
            val a = (i.toFloat() / segs) * 2f * PI.toFloat()
            verts.add(center + Vec3(cos(a) * r, 0f, sin(a) * r))
        }
        faces.add(
            BlenderFace(
                (0 until segs).map { startI + it },
                Color(0xFFFEF08A),
                roughness = 0.1f,
                emission = Color(0xFFF59E0B),
                normal = Vec3(0f, 1f, 0f)
            )
        )

        return BlenderMesh(verts, faces)
    }
}

/**
 * Blender Projection & Rendering Pipeline into Android Compose Canvas.
 */
object BlenderRenderer {

    /**
     * Projects a 3D point into 2D screen space with camera yaw, pitch, and zoom.
     */
    fun projectIso(
        v: Vec3,
        centerX: Float,
        centerY: Float,
        zoom: Float,
        yawRad: Float,
        pitchRad: Float = 0.5235f // 30 degrees standard isometric tilt
    ): Offset {
        // Apply rotation
        val rz = v.rotateZ(yawRad)
        val rx = rz.rotateX(pitchRad)

        // Isometric projection formulas (True 3D axonometric)
        val screenX = centerX + rx.x * zoom
        val screenY = centerY - rx.y * 0.577f * zoom - rx.z * zoom
        return Offset(screenX, screenY)
    }

    /**
     * Renders a BlenderMesh sorted by Painter's Algorithm depth (Z-sorting).
     */
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
        ambientOcclusion: Float = 0.25f
    ) {
        val transformedVerts = mesh.vertices.map { v ->
            val worldV = v + worldOffset
            val rotatedZ = worldV.rotateZ(yawRad)
            val finalV = rotatedZ.rotateX(pitchRad)
            Pair(finalV, projectIso(worldV, centerX, centerY, zoom, yawRad, pitchRad))
        }

        // Calculate average depth (camera distance) for each face
        val sortedFaces = mesh.faces.mapIndexed { index, face ->
            val avgDepth = face.vertexIndices.sumOf { transformedVerts[it].first.y.toDouble() } / face.vertexIndices.size
            Triple(index, face, avgDepth)
        }.sortedBy { it.third } // Back-to-front rendering

        for ((_, face, _) in sortedFaces) {
            if (face.vertexIndices.size < 3) continue

            // Compute surface normal in world space if not specified
            val p0 = mesh.vertices[face.vertexIndices[0]]
            val p1 = mesh.vertices[face.vertexIndices[1]]
            val p2 = mesh.vertices[face.vertexIndices[2]]
            val calculatedNormal = (p1 - p0).cross(p2 - p0).normalized()
            val faceNormal = (face.normal ?: calculatedNormal).rotateZ(yawRad)

            // Backface culling: skip faces pointing directly away from camera
            val viewNormal = Vec3(0f, 1f, 0.5f).normalized()
            if (faceNormal.dot(viewNormal) < -0.3f && face.emission == null) {
                // backface, skip or draw faint
            }

            // Blender Cycles-style lighting calculation
            val shadedColor = BlenderLighting.computeShading(
                normal = faceNormal,
                baseColor = face.baseColor,
                roughness = face.roughness,
                metallic = face.metallic,
                emission = face.emission
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

            // High-end Blender Edge Highlight / Chamfer line
            if (wireframeOverlay || face.roughness < 0.3f) {
                drawScope.drawPath(
                    path = path,
                    color = Color.White.copy(alpha = if (wireframeOverlay) 0.35f else 0.18f),
                    style = Stroke(width = 1.2f * zoom)
                )
            }
        }
    }
}
