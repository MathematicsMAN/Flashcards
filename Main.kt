package flashcards

import java.io.File
import kotlin.random.Random

private const val TERM_TEXT = "term"
private const val DEFINITION_TEXT = "definition"

private lateinit var myCards: Cards
private val logs = Log(mutableListOf())

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        detectFileNames(args)
    } else {
        myCards = Cards()
    }
    startWithMenu()
}

fun detectFileNames(args: Array<String>) {
    var inputFileName: String? = null
    var outputFileName: String? = null

    for (i in args.indices) {
        if (args[i] == "-import") {
            inputFileName = args[i + 1]
        } else if (args[i] == "-export") {
            outputFileName = args[i + 1]
        }
    }
    myCards = Cards(inputFileName, outputFileName)
}

fun startWithMenu() {

    val menuActions = mapOf(
        "add" to { myCards.add() },
        "remove" to { myCards.remove() },
        "import" to { myCards.import() },
        "export" to { myCards.export() },
        "ask" to { myCards.ask() },
        "exit" to {  },
        "log" to { myCards.log() },
        "hardest card" to { myCards.hardestCard() },
        "reset stats" to { myCards.resetStarts() }
    )

    val mainMenu = MainMenu(menuActions)

    while (true) {
        logs.outputMessage("Input the action $mainMenu:")
        val action = logs.inputMessage()
        if (action == "exit") {
            myCards.exit()
            break
        }
        if (mainMenu.items.containsKey(action)) {
            mainMenu.items[action]?.invoke()
        }
//        else {
//            println("$action - There is no such action.")
//        }
        logs.outputMessage("")
    }
    logs.outputMessage("Bye bye!")
}

data class Card(val term: String, val definition: String, var mistakes: Int)

class Cards(private val inputFileName: String? = null, private val exportFileName: String? = null) {

    private var cards: MutableSet<Card> = mutableSetOf()

    fun add() {
        logs.outputMessage("The card:")
        val term = inputText(TERM_TEXT) ?: return

        logs.outputMessage("The definition of the card:")
        val definition = inputText(DEFINITION_TEXT) ?: return

        cards.add(Card(term, definition, 0))
        logs.outputMessage("The pair (\"$term\":\"$definition\") has been added.")
    }

    private fun inputText(typeText: String): String? {
        val result: String = logs.inputMessage()
        if (typeText == TERM_TEXT && containsTerm(result)) {
            logs.outputMessage("The card \"$result\" already exists.")
            return null
        }
        if (typeText == DEFINITION_TEXT && containsDefinition(result)) {
            logs.outputMessage("The definition \"$result\" already exists.")
            return null
        }
        return result
    }

    private fun containsTerm(term: String, withDelete: Boolean = false): Boolean {
        for (card in cards) {
            if (card.term == term) {
                if (withDelete) {
                    cards.remove(card)
                }
                return true
            }
        }
        return false
    }

    private fun containsDefinition(definition: String): Boolean {
        for (card in cards) {
            if (card.definition == definition) {
                return true
            }
        }
        return false
    }

    fun remove() {
        logs.outputMessage("Which card?")
        val cardTerm = logs.inputMessage()
        for (card in cards) {
            if (card.term == cardTerm) {
                cards.remove(card)
                logs.outputMessage("The card has been removed.")
                return
            }
        }
        logs.outputMessage("Can't remove \"$cardTerm\": there is no such card.")
    }

    fun import() {
        val file = File(getFileName())

        loadFromFile(file)
    }

    private fun loadFromFile(file: File) {
        if (!file.exists()) {
            logs.outputMessage("File not found.")
            return
        }
        val lines = file.readLines()
        var count = 0
        for (i in 0 until lines.size / 3) {
            val card = Card(lines[i * 3], lines[i * 3 + 1], lines[i * 3 + 2].toInt())
            containsTerm(card.term, withDelete = true)
            cards.add(card)
            count++
        }
        logs.outputMessage("$count cards have been loaded.")
    }

    fun export() {
        val file = File(getFileName())
        exportToFile(file)
    }

    private fun exportToFile(file: File) {
        file.writeText("")

        for (card in cards) {
            file.appendText("${card.term}\n")
            file.appendText("${card.definition}\n")
            file.appendText("${card.mistakes}\n")
        }
        logs.outputMessage("${cards.size} cards have been saved.")
    }

    private fun getFileName(): String {
        logs.outputMessage("File name:")
        return logs.inputMessage()
    }

    fun ask() {
        logs.outputMessage("How many times to ask?")
        val num: Int = logs.inputMessage().toInt()
        repeat(num) {
            testingForCards()
        }
    }

    private fun testingForCards() {
        val position = Random.nextInt(cards.size)
        val card = cards.elementAt(position)
        logs.outputMessage("Print the definition of \"${card.term}\":")
        val answer = logs.inputMessage()
        if (answer == card.definition) {
            logs.outputMessage("Correct!")
        } else {
            cards.elementAt(position).mistakes++
            var outMessage = "Wrong. The right answer is \"${card.definition}\""
            if (containsDefinition(answer)) {
                outMessage += ", but your definition is correct for \"${getTerm(answer)}\""
            }
            logs.outputMessage("$outMessage.")
        }
    }

    private fun getTerm(definition: String): String {
        for ((term, cardDefinition) in cards) {
            if (cardDefinition == definition) {
                return term
            }
        }
        return ""
    }

    fun log() {
        val file = File(getFileName())
        file.writeText("")

        for (log in logs.lines) {
            file.appendText("$log\n")
        }
        logs.outputMessage("The log has been saved.")
    }

    fun hardestCard() {
        val hardestCardsList = mutableListOf<Card>()
        var maxMistakes = 0

        for (card in cards) {
            if (maxMistakes != 0 && maxMistakes == card.mistakes) {
                hardestCardsList.add(card)
            } else if (maxMistakes < card.mistakes) {
                hardestCardsList.clear()
                hardestCardsList.add(card)
                maxMistakes = card.mistakes
            }
        }
        logs.outputMessage(
            when (hardestCardsList.size) {
                0 -> {
                    "There are no cards with errors."
                }
                1 -> {
                    "The hardest card is \"${hardestCardsList[0].term}\"." +
                            " You have ${hardestCardsList[0].mistakes} errors answering it."
                }
                else -> {
                    var listCards = "\""
                    for (i in 0 until hardestCardsList.size - 1) {
                        listCards += "${hardestCardsList[i].term}\", \""
                    }
                    listCards += "${hardestCardsList.last().term}\""

                    "The hardest cards are $listCards." +
                            " You have $maxMistakes errors answering them."
                }
            }
        )
    }

    fun resetStarts() {
        cards.forEach { card ->
            card.mistakes = 0
        }
        logs.outputMessage("Card statistics have been reset.")
    }

    fun exit() {
        if (exportFileName != null) {
            exportToFile(File(exportFileName))
        }
    }

    init {
        if (inputFileName != null) {
            loadFromFile(File(inputFileName))
        }
    }
}

