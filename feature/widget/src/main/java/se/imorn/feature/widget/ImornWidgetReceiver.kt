package se.imorn.feature.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ImornWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() {
            val ctx = context ?: throw IllegalStateException("no context")
            return ImornWidget.create(ctx)
        }
}
