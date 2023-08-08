package me.xiaozhang.slime.shining

import io.github.sunshinewzy.shining.api.addon.ShiningAddon
import io.github.sunshinewzy.shining.api.event.guide.ShiningGuideTeamSetupEvent

object ShiningSlimeTeam : ShiningAddon() {

    override fun onInit() {
        
    }

    override fun onEnable() {
        addonManager.registerListener(ShiningGuideTeamSetupEvent::class.java) {
            it.isCancelled = true
            it.player.sendMessage("Setting up teams...")
        }
    }

    override fun onActive() {

    }

    override fun onDisable() {
        
    }
    
}