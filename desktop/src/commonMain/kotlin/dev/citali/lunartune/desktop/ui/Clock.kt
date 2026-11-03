/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

/**
 * The greeting on the home screen depends on the wall-clock hour. java.time is
 * not available in commonMain, so the platform supplies the hour through this
 * expect; desktopMain implements it with java.time.LocalTime.
 */
expect fun currentHourOfDay(): Int
