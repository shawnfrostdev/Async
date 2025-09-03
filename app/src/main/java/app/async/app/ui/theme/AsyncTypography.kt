package app.async.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.async.R

// Inter Font Family
val InterFontFamily = FontFamily(
    Font(R.font.inter_18pt_regular, FontWeight.Normal),
    Font(R.font.inter_18pt_medium, FontWeight.Medium),
    Font(R.font.inter_18pt_semibold, FontWeight.SemiBold),
    Font(R.font.inter_18pt_bold, FontWeight.Bold),
    Font(R.font.inter_18pt_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_18pt_black, FontWeight.Black),
    Font(R.font.inter_18pt_light, FontWeight.Light),
    Font(R.font.inter_18pt_extralight, FontWeight.ExtraLight)
)

// Typography system based on documentation specifications
val AsyncTypography = Typography(
    // Headline - 24sp, Bold, for main titles
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = (24 * 1.4).sp, // 1.4x line height for readability
        color = AsyncColors.TextPrimary
    ),
    
    // Title - 18sp, Medium, for section titles
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = (18 * 1.4).sp,
        color = AsyncColors.TextPrimary
    ),
    
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = (18 * 1.4).sp,
        color = AsyncColors.TextPrimary
    ),
    
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = (16 * 1.4).sp,
        color = AsyncColors.TextPrimary
    ),
    
    // Body Text - 16sp, Regular, for main content
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.4).sp,
        color = AsyncColors.TextPrimary
    ),
    
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.4).sp,
        color = AsyncColors.TextPrimary
    ),
    
    // Secondary Text - 14sp, Regular, for descriptions
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = (14 * 1.4).sp,
        color = AsyncColors.TextSecondary
    ),
    
    // Caption/Label - 12sp, Regular, for metadata
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * 1.4).sp,
        color = AsyncColors.TextSecondary
    ),
    
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * 1.4).sp,
        color = AsyncColors.TextSecondary
    ),
    
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = (12 * 1.4).sp,
        color = AsyncColors.TextSecondary
    )
) 
