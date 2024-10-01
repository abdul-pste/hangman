package com.example.hangman

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangman.ui.theme.HangmanTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HangmanTheme {
                GameScreen()
            }
        }
    }
}


val wordBank = listOf(
    "APPLE", "BALL", "CAT", "DOG", "EAGLE", "FROG", "GOAT", "HAT", "IGLOO", "JAR",
    "KITE", "LION", "MOON", "NEST", "OWL", "PEN", "QUEEN", "RAT", "SUN", "TREE"
)


val hints = listOf(
    "A fruit", "Used in sports", "A small pet", "Man's best friend", "A large bird", "A hopping animal",
    "A farm animal", "Worn on the head", "A cold shelter", "Used to store things",
    "Flies in the air", "The king of the jungle", "Seen at night", "A bird's home", "A nocturnal bird",
    "Used for writing", "A female monarch", "A small rodent", "Seen during the day", "Grows tall in forests"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen() {
    var selectedIndex by remember { mutableStateOf(Random.nextInt(wordBank.size)) }
    var currentWord by remember { mutableStateOf(wordBank[selectedIndex]) }
    var guessedLetters by remember { mutableStateOf(mutableSetOf<Char>()) }
    var correctLetters by remember { mutableStateOf(mutableSetOf<Char>()) }
    var incorrectGuesses = remember { mutableStateOf(0) }
    var remainingTurns = remember { mutableStateOf(6) }
    var hintStage by remember { mutableStateOf(0) }
    var currentHint by remember { mutableStateOf(hints[selectedIndex]) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT


    LaunchedEffect(correctLetters) {
        if (currentWord.all { correctLetters.contains(it) }) {
            Toast.makeText(context, "You won!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(remainingTurns.value) {
        if (remainingTurns.value <= 0) {
            Toast.makeText(context, "Game Over! The word was $currentWord", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Guess the Word") },
                actions = {
                    Button(onClick = {

                        guessedLetters.clear()
                        correctLetters.clear()
                        incorrectGuesses.value = 0
                        remainingTurns.value = 6
                        hintStage = 0
                        selectedIndex = Random.nextInt(wordBank.size)
                        currentWord = wordBank[selectedIndex]
                        currentHint = hints[selectedIndex]
                    }) {
                        Text("New Game")
                    }
                }
            )
        },
        content = {
            if (isPortrait) {
                PortraitLayout(
                    currentWord = currentWord,
                    guessedLetters = guessedLetters,
                    correctLetters = correctLetters,
                    incorrectGuesses = incorrectGuesses,
                    remainingTurns = remainingTurns,
                    currentHint = currentHint,
                    onLetterGuess = { letter ->
                        handleGuess(letter, currentWord, guessedLetters, correctLetters, remainingTurns, incorrectGuesses, context)
                    },
                    onHintClick = {
                        handleHint(remainingTurns, guessedLetters, currentWord, currentHint, hintStage, context)
                        hintStage++
                    }
                )
            } else {
                LandscapeLayout(
                    currentWord = currentWord,
                    guessedLetters = guessedLetters,
                    correctLetters = correctLetters,
                    incorrectGuesses = incorrectGuesses,
                    remainingTurns = remainingTurns,
                    hintStage = hintStage,
                    currentHint = currentHint,
                    onLetterGuess = { letter ->
                        handleGuess(letter, currentWord, guessedLetters, correctLetters, remainingTurns, incorrectGuesses, context)
                    },
                    onHintClick = {
                        handleHint(remainingTurns, guessedLetters, currentWord, currentHint, hintStage, context)
                        hintStage++
                    }
                )
            }
        }
    )
}

fun handleGuess(
    letter: Char,
    word: String,
    guessedLetters: MutableSet<Char>,
    correctLetters: MutableSet<Char>,
    remainingTurns: MutableState<Int>,
    incorrectGuesses: MutableState<Int>,
    context: android.content.Context
) {
    guessedLetters.add(letter)
    if (word.contains(letter)) {
        correctLetters.add(letter)
        Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
    } else {
        remainingTurns.value--
        incorrectGuesses.value++
        Toast.makeText(context, "Incorrect!", Toast.LENGTH_SHORT).show()
    }
}

fun handleHint(
    remainingTurns: MutableState<Int>,
    guessedLetters: MutableSet<Char>,
    currentWord: String,
    currentHint: String,
    hintStage: Int,
    context: android.content.Context
) {
    when (hintStage) {
        0 -> {

            Toast.makeText(context, "Hint: $currentHint", Toast.LENGTH_SHORT).show()
        }
        1 -> {

            val unguessedLetters = ('A'..'Z').filterNot { guessedLetters.contains(it) }
            val disableLetters = unguessedLetters.filterNot { currentWord.contains(it) }.take(unguessedLetters.size / 2)
            guessedLetters.addAll(disableLetters)
            remainingTurns.value--
        }
        2 -> {

            guessedLetters.addAll(setOf('A', 'E', 'I', 'O', 'U'))
            remainingTurns.value--
        }
        else -> {
            Toast.makeText(context, "No more hints available", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun PortraitLayout(
    currentWord: String,
    guessedLetters: MutableSet<Char>,
    correctLetters: MutableSet<Char>,
    incorrectGuesses: MutableState<Int>,
    remainingTurns: MutableState<Int>,
    currentHint: String,
    onLetterGuess: (Char) -> Unit,
    onHintClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        WordDisplay(currentWord, correctLetters)
        Spacer(modifier = Modifier.height(16.dp))

        CorrectLettersDisplay(currentWord, correctLetters)
        Spacer(modifier = Modifier.height(16.dp))

        LetterSelection(guessedLetters, onLetterGuess)
        Spacer(modifier = Modifier.height(16.dp))

        HangmanDisplay(incorrectGuesses.value)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onHintClick) {
            Text("Hint")
        }
    }
}

@Composable
fun LandscapeLayout(
    currentWord: String,
    guessedLetters: MutableSet<Char>,
    correctLetters: MutableSet<Char>,
    incorrectGuesses: MutableState<Int>,
    remainingTurns: MutableState<Int>,
    hintStage: Int,
    currentHint: String,
    onLetterGuess: (Char) -> Unit,
    onHintClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier.weight(1f)) {
            LetterSelection(guessedLetters, onLetterGuess)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WordDisplay(currentWord, correctLetters)
            Spacer(modifier = Modifier.height(16.dp))

            CorrectLettersDisplay(currentWord, correctLetters)
            Spacer(modifier = Modifier.height(16.dp))
            HangmanDisplay(incorrectGuesses.value)
        }

        Column(modifier = Modifier.weight(1f)) {
            Button(onClick = onHintClick) {
                Text("Hint")
            }
        }
    }
}


@Composable
fun GuessedLettersDisplay(guessedLetters: MutableSet<Char>) {
    Text(
        text = "Guessed Letters: ${guessedLetters.joinToString(", ")}",
        fontSize = 18.sp,
        modifier = Modifier.padding(8.dp)
    )
}


@Composable
fun WordDisplay(currentWord: String, correctLetters: MutableSet<Char>) {
    Row {
        currentWord.forEach { letter ->
            Text(
                text = if (correctLetters.contains(letter)) letter.toString() else "_",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}


@Composable
fun CorrectLettersDisplay(currentWord: String, correctLetters: MutableSet<Char>) {
    Row {
        currentWord.forEach { letter ->
            Text(
                text = if (correctLetters.contains(letter)) letter.toString() else "_",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}


@Composable
fun LetterSelection(guessedLetters: MutableSet<Char>, onLetterGuess: (Char) -> Unit) {
    val alphabet = ('A'..'Z').toList()

    Column {
        alphabet.chunked(7).forEach { row ->
            Row {
                row.forEach { letter ->
                    Button(
                        onClick = { onLetterGuess(letter) },
                        enabled = !guessedLetters.contains(letter),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(text = letter.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun HangmanDisplay(incorrectGuesses: Int) {
    Canvas(modifier = Modifier.size(200.dp)) {
        drawHangman(incorrectGuesses)
    }
}

fun DrawScope.drawHangman(incorrectGuesses: Int) {
    val baseX = size.width / 2
    val baseY = size.height


    drawLine(Color.Black, start = Offset(baseX, baseY), end = Offset(baseX, baseY - 150), strokeWidth = 8f) // Vertical pole
    drawLine(Color.Black, start = Offset(baseX, baseY - 150), end = Offset(baseX + 50, baseY - 150), strokeWidth = 8f) // Top horizontal pole
    drawLine(Color.Black, start = Offset(baseX + 50, baseY - 150), end = Offset(baseX + 50, baseY - 130), strokeWidth = 8f) // Short rope

    if (incorrectGuesses > 0) {

        drawCircle(Color.Black, radius = 20f, center = Offset(baseX + 50, baseY - 100))
    }
    if (incorrectGuesses > 1) {

        drawLine(Color.Black, start = Offset(baseX + 50, baseY - 80), end = Offset(baseX + 50, baseY - 40), strokeWidth = 8f)
    }
    if (incorrectGuesses > 2) {

        drawLine(Color.Black, start = Offset(baseX + 50, baseY - 70), end = Offset(baseX + 30, baseY - 60), strokeWidth = 8f)
    }
    if (incorrectGuesses > 3) {

        drawLine(Color.Black, start = Offset(baseX + 50, baseY - 70), end = Offset(baseX + 70, baseY - 60), strokeWidth = 8f)
    }
    if (incorrectGuesses > 4) {

        drawLine(Color.Black, start = Offset(baseX + 50, baseY - 40), end = Offset(baseX + 30, baseY - 20), strokeWidth = 8f)
    }
    if (incorrectGuesses > 5) {

        drawLine(Color.Black, start = Offset(baseX + 50, baseY - 40), end = Offset(baseX + 70, baseY - 20), strokeWidth = 8f)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HangmanTheme {
        GameScreen()
    }
}
