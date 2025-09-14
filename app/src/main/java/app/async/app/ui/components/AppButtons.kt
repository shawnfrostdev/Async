package app.async.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import app.async.app.ui.theme.DISABLED_ALPHA
import app.async.app.ui.theme.padding
import app.async.app.ui.theme.iconSizing
import app.async.app.ui.theme.componentSizing
import app.async.app.ui.theme.ComponentSizing
import app.async.app.ui.theme.IconSizing

/**
 * Mihon-style Button Components as per UI guide
 */

/**
 * Custom TextButton following Mihon specifications
 */
@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape = ButtonDefaults.textShape,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA),
    ),
    contentPadding: PaddingValues = ComponentSizing.ButtonPadding.standard, // 24dp horizontal, 8dp vertical
    content: @Composable RowScope.() -> Unit,
) = Button(
    onClick = onClick,
    modifier = modifier.heightIn(min = ComponentSizing.Button.standard), // 40dp height as per Mihon guide
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    content = content,
)

/**
 * Mihon-style Action Button as per UI guide
 */
@Composable
fun ActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall), // 4dp spacing
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(IconSizing.default) // 24dp as per Mihon guide
            )
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge // 14sp Medium as per Mihon guide
            )
        }
    }
}

/**
 * Mihon-style Standard Button as per UI guide
 */
@Composable
fun StandardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ComponentSizing.ButtonPadding.standard,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ComponentSizing.Button.standard), // 40dp height
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Mihon-style Large Button as per UI guide
 */
@Composable
fun LargeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ComponentSizing.ButtonPadding.large,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ComponentSizing.Button.large), // 48dp height
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Mihon-style Icon Button with proper touch target
 */
@Composable
fun StandardIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    contentDescription: String? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(ComponentSizing.Button.large), // 48dp touch target
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(IconSizing.default) // 24dp icon
        )
    }
}

// Extended FAB Constants as per Mihon UI guide - updated with proper sizing
private val ExtendedFabMinimumWidth = 80.dp
private val FabContainerWidth = 56.dp // FAB size as per Mihon guide
private val ExtendedFabIconPadding = 12.dp // Icon padding 
private val ExtendedFabTextPadding = 20.dp // Text padding

// Extended FAB Animations as per Mihon UI guide
private val ExtendedFabExpandAnimation = fadeIn(animationSpec = tween(200)) + 
    slideInHorizontally(animationSpec = tween(200)) { -it / 2 }
private val ExtendedFabCollapseAnimation = fadeOut(animationSpec = tween(200)) + 
    slideOutHorizontally(animationSpec = tween(200)) { -it / 2 }

// Emphasized Cubic Bezier easing as per Material specs
private val EasingEmphasizedCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

/**
 * Mihon-style Extended FAB with Animation as per UI guide
 */
@Composable
fun ExtendedFloatingActionButton(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = FloatingActionButtonDefaults.extendedFabShape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
) {
    androidx.compose.material3.FloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
    ) {
        val minWidth by animateDpAsState(
            targetValue = if (expanded) ExtendedFabMinimumWidth else FabContainerWidth,
            animationSpec = tween(
                durationMillis = 500,
                easing = EasingEmphasizedCubicBezier,
            ),
            label = "minWidth",
        )

        Row(
            modifier = Modifier.sizeIn(minWidth = minWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            icon()
            AnimatedVisibility(
                visible = expanded,
                enter = ExtendedFabExpandAnimation,
                exit = ExtendedFabCollapseAnimation,
            ) {
                Box(modifier = Modifier.padding(start = ExtendedFabIconPadding, end = ExtendedFabTextPadding)) {
                    text()
                }
            }
        }
    }
} 
