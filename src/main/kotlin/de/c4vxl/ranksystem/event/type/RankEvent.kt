package de.c4vxl.ranksystem.event.type

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Basic event for rank-system events
 */
open class RankEvent : Event() {
    companion object {
        private val HANDLER_LIST: HandlerList = HandlerList()

         @JvmStatic
         fun getHandlerList(): HandlerList = HANDLER_LIST
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST
}