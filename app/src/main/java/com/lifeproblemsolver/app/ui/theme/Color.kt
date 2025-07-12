package com.lifeproblemsolver.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Color Palette for Executive App
val PrimaryBlue = Color(0xFF1E3A8A)
val PrimaryBlueLight = Color(0xFF3B82F6)
val SecondaryTeal = Color(0xFF0F766E)
val SecondaryTealLight = Color(0xFF14B8A6)
val AccentGold = Color(0xFFD97706)
val AccentGoldLight = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF059669)
val SuccessGreenLight = Color(0xFF10B981)
val WarningOrange = Color(0xFFEA580C)
val WarningOrangeLight = Color(0xFFF97316)
val ErrorRed = Color(0xFFDC2626)
val ErrorRedLight = Color(0xFFEF4444)

// Neutral Colors
val Neutral900 = Color(0xFF111827)
val Neutral800 = Color(0xFF1F2937)
val Neutral700 = Color(0xFF374151)
val Neutral600 = Color(0xFF4B5563)
val Neutral500 = Color(0xFF6B7280)
val Neutral400 = Color(0xFF9CA3AF)
val Neutral300 = Color(0xFFD1D5DB)
val Neutral200 = Color(0xFFE5E7EB)
val Neutral100 = Color(0xFFF3F4F6)
val Neutral50 = Color(0xFFF9FAFB)

// Background Colors
val BackgroundPrimary = Color(0xFFFAFAFA)
val BackgroundSecondary = Color(0xFFF5F5F5)
val BackgroundTertiary = Color(0xFFEFEFEF)
val BackgroundDark = Color(0xFF1A1A1A)
val BackgroundDarkSecondary = Color(0xFF2D2D2D)

// Premium Gradients
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(PrimaryBlue, PrimaryBlueLight)
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(SecondaryTeal, SecondaryTealLight)
)

val AccentGradient = Brush.linearGradient(
    colors = listOf(AccentGold, AccentGoldLight)
)

val SuccessGradient = Brush.linearGradient(
    colors = listOf(SuccessGreen, SuccessGreenLight)
)

val PremiumGradient = Brush.linearGradient(
    colors = listOf(PrimaryBlue, SecondaryTeal, AccentGold)
)

val GlassGradient = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.1f),
        Color.White.copy(alpha = 0.05f)
    )
)

// Legacy colors for compatibility
val Purple80 = PrimaryBlueLight
val PurpleGrey80 = SecondaryTealLight
val Pink80 = AccentGoldLight

val Purple40 = PrimaryBlue
val PurpleGrey40 = SecondaryTeal
val Pink40 = AccentGold 