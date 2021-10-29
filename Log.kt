package flashcards

data class Log(val lines: MutableList<String>) {

    fun outputMessage(line: String) {
        println(line)
        lines.add(line)
    }

    fun inputMessage(): String {
        val line = readLine()!!
        lines.add(line)
        return line
    }
}