package nurgling.widgets.bots

import haven.*
import nurgling.actions.bots.BotAction
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.FileReader
import java.io.IOException

object BotLoaderHelper {


    fun loadBot(path: String) : List<BotAction>{
        val loadedActions = mutableListOf<BotAction>()
        val file = File("bots/bot", "$path.json")

        if (!file.exists() || !file.isFile) {
            println("File not found: $path")
            return loadedActions
        }

        try {
            FileReader(file).use { reader ->
                val content = StringBuilder()
                var character: Int
                while (reader.read().also { character = it } != -1) {
                    content.append(character.toChar())
                }

                val jsonArray = JSONArray(content.toString())
                var i = 0
                while (i < jsonArray.length()) {
                    val action = jsonArray.getJSONObject(i)
                    val type = action.getString("type")
                    when (type) {
                        "Path" -> {
                            val path = action.getString("path")
                            val forageables = action.getJSONArray("forageables").getString(0)
                            val avoidables = action.getJSONArray("avoidables").getString(0)
                            loadedActions.add(BotAction.Path(path, forageables, avoidables))
                        }
                        "TraverseGate" -> {
                            loadedActions.add(BotAction.TraverseGate(action.getLong("grid"), action.getDouble("x"), action.getDouble("y")))
                        }
                        "ChooseRoad" -> {
                            val signpost = action.getJSONArray("signpost").getString(0)
                            val road = action.getString("road")
                            loadedActions.add(BotAction.ChooseRoad(null, road))
                        }
                        "GenericInteract" -> {
                            val target = action.getJSONArray("target").getString(0)
                            loadedActions.add(BotAction.GenericInteract(null))
                        }
                        "HearthBack" -> {
                            loadedActions.add(BotAction.HearthBack)
                        }
                    }
                    i++
                }

                println("Bot loaded successfully!")
            }
        } catch (e: IOException) {
            println("Error loading bot: ${e.message}")
        } catch (e: JSONException) {
            println("Error loading bot: ${e.message}")
        }

        return loadedActions
    }
    fun loadForageables(path: String): List<String> {
        val loadedForageables = mutableListOf<String>()
        val file = File("bots/forageables", path)

        if (!file.exists() || !file.isFile) {
            println("File not found: $path")
            return loadedForageables
        }

        try {
            FileReader(file).use { reader ->
                val content = StringBuilder()
                var character: Int
                while (reader.read().also { character = it } != -1) {
                    content.append(character.toChar())
                }

                val jsonArray = JSONArray(content.toString())
                var i = 0
                while (i < jsonArray.length()) {
                    loadedForageables.add(jsonArray.getString(i))
                    i++
                }

                println("Forageables loaded successfully!")
            }
        } catch (e: IOException) {
            println("Error loading forageables: ${e.message}")
        } catch (e: JSONException) {
            println("Error loading forageables: ${e.message}")
        }

        return loadedForageables
    }

    fun loadAvoidables(path: String): List<String> {
        val loadedAvoidables = mutableListOf<String>()
        val file = File("bots/avoidables", path)

        if (!file.exists() || !file.isFile) {
            println("File not found: $path")
            return loadedAvoidables
        }

        try {
            FileReader(file).use { reader ->
                val content = StringBuilder()
                var character: Int
                while (reader.read().also { character = it } != -1) {
                    content.append(character.toChar())
                }

                val jsonArray = JSONArray(content.toString())
                var i = 0
                while (i < jsonArray.length()) {
                    loadedAvoidables.add(jsonArray.getString(i))
                    i++
                }

                println("Avoidables loaded successfully!")
            }
        } catch (e: IOException) {
            println("Error loading avoidables: ${e.message}")
        } catch (e: JSONException) {
            println("Error loading avoidables: ${e.message}")
        }

        return loadedAvoidables
    }

    fun loadCoordinates(path: String): List<Coord2d> {
        val loadedCoordinates = mutableListOf<Coord2d>()
        val file = File("bots/paths", path)

        if (!file.exists() || !file.isFile) {
            println("File not found: $path")
            return loadedCoordinates
        }

        try {
            FileReader(file).use { reader ->
                val content = StringBuilder()
                var character: Int
                while (reader.read().also { character = it } != -1) {
                    content.append(character.toChar())
                }

                val jsonArray = JSONArray(content.toString())
                var i = 0
                while (i < jsonArray.length()) {
                    val x = jsonArray.getString(i).replace("(", "")
                    val y = jsonArray.getString(i + 1).replace(")", "")
                    loadedCoordinates.add(Coord2d(x.toDouble(), y.toDouble()))
                    i += 2
                }

                println("Coordinates loaded successfully!")
            }
        } catch (e: IOException) {
            println("Error loading coordinates: ${e.message}")
        } catch (e: JSONException) {
            println("Error loading coordinates: ${e.message}")
        }

        return loadedCoordinates
    }
}