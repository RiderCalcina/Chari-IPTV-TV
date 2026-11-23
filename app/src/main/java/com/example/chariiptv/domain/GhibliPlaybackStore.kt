package com.example.chariiptv.domain

import android.content.Context

/**
 * Gestor de persistencia de progreso y reanudación exclusiva para las 25 películas de Studio Ghibli.
 * Permite almacenar y consultar la posición exacta (en ms) cuando el usuario sale de la reproducción.
 */
object GhibliPlaybackStore {

    private const val PREFS_NAME = "ghibli_playback_store"
    private const val KEY_PREFIX = "ghibli_pos_"
    const val MIN_RESUME_THRESHOLD_MS = 15_000L // Mínimo 15 segundos reproducidos para ofrecer reanudar
    private const val END_THRESHOLD_MS = 60_000L // Si quedan menos de 60s, se asume finalizada

    /**
     * Guarda la posición actual de reproducción en SharedPreferences.
     */
    fun savePosition(context: Context, movieName: String, positionMs: Long, durationMs: Long) {
        val movieInfo = GhibliData.getMovieInfo(movieName)
        val key = buildKey(movieInfo.title)

        // Si ya está terminando la película (últimos 60 segundos o >95%), reiniciar progreso
        if (durationMs > 0L && positionMs >= (durationMs - END_THRESHOLD_MS)) {
            clearPosition(context, movieName)
            return
        }

        // Si no superó el umbral mínimo de 15 segundos, no guardar
        if (positionMs < MIN_RESUME_THRESHOLD_MS) return

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("${key}_pos", positionMs)
                .putLong("${key}_dur", durationMs)
                .putLong("${key}_updated", System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {}
    }

    /**
     * Obtiene la posición guardada (posMs, durMs) si supera el umbral mínimo.
     * Retorna null si no hay progreso previo o si fue finalizada.
     */
    fun getSavedPosition(context: Context, movieName: String): Pair<Long, Long>? {
        val movieInfo = GhibliData.getMovieInfo(movieName)
        val key = buildKey(movieInfo.title)

        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pos = prefs.getLong("${key}_pos", 0L)
            val dur = prefs.getLong("${key}_dur", 0L)
            if (pos >= MIN_RESUME_THRESHOLD_MS) {
                Pair(pos, dur)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Borra la posición guardada para reiniciar desde 0.
     */
    fun clearPosition(context: Context, movieName: String) {
        val movieInfo = GhibliData.getMovieInfo(movieName)
        val key = buildKey(movieInfo.title)

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove("${key}_pos")
                .remove("${key}_dur")
                .remove("${key}_updated")
                .apply()
        } catch (_: Exception) {}
    }

    private fun buildKey(title: String): String {
        return KEY_PREFIX + title.lowercase()
            .replace("á", "a")
            .replace("ä", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("ñ", "n")
            .replace(Regex("[^a-z0-9]"), "_")
    }
}
