package me.xiaozhang.slime.shining

import io.github.sunshinewzy.shining.api.event.guide.ShiningGuideTeamGetAsyncEvent
import io.github.sunshinewzy.shining.api.event.guide.ShiningGuideTeamSetupEvent
import io.github.sunshinewzy.shining.core.addon.ShiningAddon
import io.github.sunshinewzy.shining.core.guide.team.GuideTeam
import io.github.sunshinewzy.shining.core.guide.team.GuideTeams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.xiaozhangup.slimecargo.manager.IslandManager
import me.xiaozhangup.slimecargo.objects.Island
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object ShiningSlimeTeam : ShiningAddon() {

    private val namespacedKey: NamespacedKey = NamespacedKey("shining", "team")

    override fun onEnable() {
        addonManager.registerListener(ShiningGuideTeamGetAsyncEvent::class.java) {
            it.isCancelled = true
            val player = it.player
            transaction {
                player.island()?.let { island ->
                    player.getTeam()?.let { guideTeam ->
                        it.team = guideTeam
                    } ?: kotlin.run {
                        val newTeam = runBlocking(Dispatchers.IO) {
                            GuideTeam.create(
                                island.landId,
                                island.landId.toString(),
                                ItemStack(Material.GRASS_BLOCK)
                            ) ?: newSuspendedTransaction { 
                                GuideTeam.find { GuideTeams.captain eq island.landId }.first()
                            }
                        }

                        player.world.setTeam(newTeam.id.value)
                        it.team = newTeam
                    }
                }
            }
        }

        addonManager.registerListener(ShiningGuideTeamSetupEvent::class.java) {
            it.isCancelled = true
        }
    }

    private fun Player.island(): Island? {
        return IslandManager.getLand(world.name).orElse(null)
    }

    private fun World.setTeam(id: Int) {
        persistentDataContainer.set(namespacedKey, PersistentDataType.INTEGER, id)
    }

    private fun Player.getTeam(): GuideTeam? {
        val id = world.persistentDataContainer.getOrDefault(namespacedKey, PersistentDataType.INTEGER, -1)
        return if (id == -1) null
        else transaction { GuideTeam.findById(id) }
    }

}