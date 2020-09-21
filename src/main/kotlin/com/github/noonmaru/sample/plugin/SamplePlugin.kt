package com.github.noonmaru.sample.plugin

import com.github.noonmaru.tap.util.GitHubSupport
import com.github.noonmaru.tap.util.UpToDateException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * @author Noonmaru
 */
open class SamplePlugin : JavaPlugin() {

    // Update example
    fun update(sender: CommandSender) {
        update {
            onSuccess { url ->
                sender.sendMessage("Updated successfully. Applies after the server restarts.")
                sender.sendMessage(url)
            }
            onFailure { t ->
                if (t is UpToDateException) sender.sendMessage("Up to date!")
                else {
                    sender.sendMessage("Update failed. Check the console.")
                    t.printStackTrace()
                }
            }
        }
    }

    fun update(callback: (Result<String>.() -> Unit)? = null) {
        GlobalScope.launch {
            val file = file
            val updateFile = File(file.parentFile, "update/${file.name}")
            GitHubSupport.downloadUpdate(updateFile, "noonmaru", "kotlin-plugin", description.version, callback)
        }
    }
}