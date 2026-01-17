package com.android.sun.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Tema principală a aplicației SUN
 * Suportă Light și Dark mode
 */

	private val LightColorScheme = lightColorScheme(
		primary = Primary,
		onPrimary = OnPrimary,
		primaryContainer = PrimaryContainer,
		onPrimaryContainer = OnPrimaryContainer,
		
		secondary = Secondary,
		onSecondary = OnSecondary,
		secondaryContainer = SecondaryContainer,
		onSecondaryContainer = OnSecondaryContainer,
		
		tertiary = Tertiary,
		onTertiary = OnTertiary,
		tertiaryContainer = TertiaryContainer,
		onTertiaryContainer = OnTertiaryContainer,
		
		error = Error,
		onError = OnError,
		errorContainer = ErrorContainer,
		onErrorContainer = OnErrorContainer,
		
		background = Background,
		onBackground = OnBackground,
		surface = Surface,
		onSurface = OnSurface,
		
		surfaceVariant = SurfaceVariant,
		onSurfaceVariant = OnSurfaceVariant,
		outline = Outline
	)

	private val DarkColorScheme = darkColorScheme(
		primary = PrimaryDark,
		onPrimary = OnPrimaryDark,
		primaryContainer = PrimaryContainerDark,
		onPrimaryContainer = OnPrimaryContainerDark,
		
		secondary = SecondaryDark,
		onSecondary = OnSecondaryDark,
		secondaryContainer = SecondaryContainerDark,
		onSecondaryContainer = OnSecondaryContainerDark,
		
		tertiary = TertiaryDark,
		onTertiary = OnTertiaryDark,
		tertiaryContainer = TertiaryContainerDark,
		onTertiaryContainer = OnTertiaryContainerDark,
		
		error = ErrorDark,
		onError = OnErrorDark,
		errorContainer = ErrorContainerDark,
		onErrorContainer = OnErrorContainerDark,
		
		background = BackgroundDark,
		onBackground = OnBackgroundDark,
		surface = SurfaceDark,
		onSurface = OnSurfaceDark,
		
		surfaceVariant = SurfaceVariantDark,
		onSurfaceVariant = OnSurfaceVariantDark,
		outline = OutlineDark
	)

	/**
	 * Tema principală pentru aplicație
	 * 
	 * @param darkTheme Activează dark mode (default = setare sistem)
	 * @param dynamicColor Activează Material You colors (Android 12+)
	 */
	@Composable
	fun SunTheme(
		darkTheme: Boolean = isSystemInDarkTheme(),
		dynamicColor: Boolean = false,  // Dezactivat pentru culori consistente Tattva
		content: @Composable () -> Unit
	) {
		val colorScheme = when {
			// Dynamic color pe Android 12+ (opțional)
			dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
				val context = LocalContext.current
				if (darkTheme) dynamicDarkColorScheme(context) 
				else dynamicLightColorScheme(context)
			}
			
			// Tema standard
			darkTheme -> DarkColorScheme
			else -> LightColorScheme
		}
		
		val view = LocalView.current
		if (!view.isInEditMode) {
			
			SideEffect {
				val window = (view.context as Activity).window
				
				// Status bar transparent
				window.statusBarColor = android.graphics.Color.TRANSPARENT
				
				// Navigation bar COMPLET TRANSPARENTA
				window.navigationBarColor = android.graphics.Color.TRANSPARENT
				
				// Iconite status bar - dark icons for light theme, light icons for dark theme
				WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
				
				// Iconite navigation bar
				WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
			}
		
		
		
		
		
		
		
		}

		MaterialTheme(
			colorScheme = colorScheme,
			typography = Typography,
			content = content
		)
	}