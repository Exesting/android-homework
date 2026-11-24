package com.example.homework1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homework1.ui.theme.Homework1Theme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val message = intent.getStringExtra("student_info") ?: ""

        setContent {
            Homework1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CoroutineScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        studentInfo = message
                    )
                }
            }
        }
    }
}

@Suppress("LongMethod")
@androidx.compose.runtime.Composable
private fun CoroutineScreen(
    modifier: Modifier = Modifier,
    studentInfo: String
) {
    val scope = rememberCoroutineScope()
    var coroutineCount by remember { mutableIntStateOf(3) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isRunning by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Выберите количество корутин") }
    var workJob by remember { mutableStateOf<Job?>(null) }

    fun startWork(parallel: Boolean) {
        isRunning = true
        progress = 0f
        status = if (parallel) {
            "Корутины выполняются параллельно"
        } else {
            "Корутины выполняются последовательно"
        }

        workJob = scope.launch {
            try {
                var completedSteps = 0
                val totalSteps = coroutineCount * PROGRESS_STEPS

                suspend fun doWork() {
                    repeat(PROGRESS_STEPS) {
                        delay(STEP_DURATION_MS)
                        completedSteps++
                        progress = completedSteps.toFloat() / totalSteps
                    }
                }

                if (parallel) {
                    coroutineScope {
                        repeat(coroutineCount) {
                            launch { doWork() }
                        }
                    }
                } else {
                    repeat(coroutineCount) { doWork() }
                }
                status = "Выполнение завершено"
            } finally {
                isRunning = false
                workJob = null
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (studentInfo.isNotBlank()) Text(text = studentInfo)

        Text(text = "Количество корутин: $coroutineCount")
        Slider(
            value = coroutineCount.toFloat(),
            onValueChange = { coroutineCount = it.toInt() },
            valueRange = MIN_COROUTINES.toFloat()..MAX_COROUTINES.toFloat(),
            steps = MAX_COROUTINES - MIN_COROUTINES - 1,
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = status)

        Button(
            onClick = { startWork(parallel = false) },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Запустить последовательно") }

        Button(
            onClick = { startWork(parallel = true) },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Запустить параллельно") }

        Button(
            onClick = {
                scope.launch {
                    workJob?.cancelAndJoin()
                    progress = 0f
                    status = "Корутины отменены"
                }
            },
            enabled = isRunning,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Отменить все корутины") }
    }
}

private const val MIN_COROUTINES = 1
private const val MAX_COROUTINES = 10
private const val PROGRESS_STEPS = 10
private const val STEP_DURATION_MS = 100L
