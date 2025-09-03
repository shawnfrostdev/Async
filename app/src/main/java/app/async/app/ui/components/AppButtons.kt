package app.async.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.async.app.ui.theme.AsyncColors

/**
 * Centralized button components following design system specifications
 * 
 * Button Types:
 * - Primary: #F54B5D background, #E0E0E0 text - For main actions
 * - Secondary: Transparent background, #F54B5D bordered text - For alternative actions  
 * - Disabled: #555555 background, #898989 text - For inactive states
 * 
 * Standards:
 * - Corner radius: 8dp
 * - Minimum height: 48dp
 * - Label: 16sp, Medium font weight
 */
object AppButtons {
    
    @Composable
    fun Primary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AsyncColors.Primary,
                contentColor = AsyncColors.TextPrimary,
                disabledContainerColor = AsyncColors.Disabled,
                disabledContentColor = AsyncColors.TextSecondary
            )
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    @Composable
    fun Secondary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AsyncColors.Primary,
                disabledContentColor = AsyncColors.TextSecondary
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (enabled) AsyncColors.Primary else AsyncColors.Disabled
            )
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    @Composable
    fun Text(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        color: Color = AsyncColors.Primary
    ) {
        TextButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = color,
                disabledContentColor = AsyncColors.TextSecondary
            )
        ) {
            androidx.compose.material3.Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    @Composable
    fun Icon(
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        containerColor: Color = AsyncColors.Primary,
        contentColor: Color = AsyncColors.TextPrimary
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = AsyncColors.Disabled,
                disabledContentColor = AsyncColors.TextSecondary
            )
        ) {
            icon()
        }
    }
    
    @Composable
    fun FloatingAction(
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: Color = AsyncColors.Primary,
        contentColor: Color = AsyncColors.TextPrimary
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            icon()
        }
    }
} 
