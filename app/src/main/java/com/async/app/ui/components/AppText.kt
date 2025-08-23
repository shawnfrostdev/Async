package com.async.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Centralized text components with consistent styling
 */
object AppText {
    
    @Composable
    fun TitleLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun TitleMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun TitleSmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun BodyLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun BodyMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun BodySmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun LabelLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun LabelMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
    
    @Composable
    fun LabelSmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
        fontWeight: FontWeight? = null,
        textAlign: TextAlign? = null,
        maxLines: Int = Int.MAX_VALUE,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
    }
} 