package nurgling.actions.bots

import com.google.gson.*
import haven.Gob
import nurgling.tools.NAlias
import java.lang.reflect.Type

sealed class BotAction {

    data class Path(
        val path: String,
        val forageables: String,
        val avoidables: String
    ) : BotAction()

    data class TraverseGate(
        val grid: Long,
        val x: Double,
        val y: Double
    ) : BotAction()

    data class ChooseRoad(val signpost: Gob?, val road: String?) : BotAction()

    data class GenericInteract(val target: Gob?) : BotAction()

    object HearthBack : BotAction()

    override fun toString(): String {
        return when (this) {
            is Path -> "Path to $path"
            is TraverseGate -> "Open gate"
            is ChooseRoad -> "Choose road $road"
            is GenericInteract -> "Interact with ${target?.ngob?.name}"
            is HearthBack -> "Hearth back"
        }
    }
}

class BotActionSerializer : JsonSerializer<BotAction> {
    override fun serialize(src: BotAction, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", src.javaClass.simpleName)
        when (src) {
            is BotAction.Path -> {
                jsonObject.addProperty("path", src.path)
                jsonObject.add("forageables", JsonArray().apply { add(src.forageables) })
                jsonObject.add("avoidables", JsonArray().apply { add(src.avoidables) })
            }

            is BotAction.ChooseRoad -> {
                jsonObject.add("signpost", src.signpost?.let { JsonArray().apply { add(it.id) } })
                jsonObject.addProperty("road", src.road)
            }
            is BotAction.GenericInteract ->{
                jsonObject.add("target", src.target?.let { JsonArray().apply { add(it.id) } })
            }
            is BotAction.TraverseGate -> {
                jsonObject.addProperty("grid", src.grid)
                jsonObject.addProperty("x", src.x)
                jsonObject.addProperty("y", src.y)
            }
            BotAction.HearthBack ->{
                // Do nothing
            }
        }
        return jsonObject
    }
}
