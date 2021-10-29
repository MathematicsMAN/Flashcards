package flashcards

data class MainMenu(var items: Map<String, () -> Unit>) {

    override fun toString(): String {
        val itemNames: MutableList<String> = mutableListOf()
        for (item in items) {
            itemNames += item.key
        }
        return itemNames.joinToString(separator = ", ", prefix = "(", postfix = ")")
    }
}