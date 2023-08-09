package me.xiaozhang.slime.shining

import io.github.sunshinewzy.shining.api.addon.ShiningAddon
import io.github.sunshinewzy.shining.api.event.guide.ShiningGuideOpenEvent
import io.github.sunshinewzy.shining.api.event.guide.ShiningGuideTeamGetAsyncEvent
import io.github.sunshinewzy.shining.core.guide.team.GuideTeam
import io.github.sunshinewzy.shining.core.guide.team.GuideTeams
import io.github.sunshinewzy.shining.objects.ShiningDispatchers
import me.xiaozhangup.slimecargo.manager.IslandManager
import me.xiaozhangup.slimecargo.objects.Island
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object ShiningSlimeTeam : ShiningAddon() {

    private val namespacedKey: NamespacedKey = NamespacedKey("shining", "team")

    override fun onEnable() {
        addonManager.registerListener(ShiningGuideTeamGetAsyncEvent::class.java) {
            val island = it.player.getIsland()!!

            ShiningDispatchers.launchDB {
                var isTeam: GuideTeam? = null
                newSuspendedTransaction {
                    val pre = GuideTeam.findById(island)
                    if (pre != null) {
                        isTeam = pre
                    } else {
                        isTeam = GuideTeam.create(it.player, it.player.island().landId.toString(), ItemStack(Material.GRASS_BLOCK))
                        it.player.world.setTeam(isTeam!!.id.value)
                    }
                }
                it.team = isTeam
            }
        }

        addonManager.registerListener(ShiningGuideOpenEvent::class.java) {
            if (!it.player.inIsland()) { it.isCancelled = true }
        }
    }

    private fun Player.getIsland(): Int? {
        val id = world.persistentDataContainer.getOrDefault(namespacedKey, PersistentDataType.INTEGER, -1)
        return if (id == -1) null
        else id
    }

    private fun Player.inIsland(): Boolean {
        return IslandManager.getLand(world.name).isPresent
    }

    private fun Player.island(): Island {
        return IslandManager.getLand(world.name).get()
    }

    private fun World.setTeam(id: Int) {
        persistentDataContainer.set(namespacedKey, PersistentDataType.INTEGER, id)
    }
}