package com.example.dolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Ink = Color(0xFF202A35)
private val Muted = Color(0xFF84909B)
private val Canvas = Color(0xFFF5F7F8)
private val Green = Color(0xFF4E9B82)
private val PaleGreen = Color(0xFFE5F2ED)
private val White = Color.White

data class Todo(val id: Long, val title: String, val done: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TodoApp() }
    }
}

@Composable
fun TodoApp() {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf(loadTasks(context)) }
    var newTask by remember { mutableStateOf("") }
    var showDone by remember { mutableStateOf(false) }

    fun update(next: List<Todo>) {
        tasks = next
        saveTasks(context, next)
    }

    val remaining = tasks.count { !it.done }
    val completed = tasks.count { it.done }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            background = Canvas,
            surface = White,
            onSurface = Ink
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text("برنامه‌ریزی روزانه", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(7.dp))
                Text(
                    "کارهای من",
                    color = Ink,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = PaleGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("قدم‌های کوچک، نتیجه‌های بزرگ ✨", color = Ink, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("$remaining", color = Green, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                                Text("کار باقی‌مانده", color = Muted, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$completed", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("انجام‌شده", color = Muted, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size },
                            modifier = Modifier.fillMaxWidth().height(7.dp),
                            color = Green,
                            trackColor = Color(0xFFD3E5DD)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("فهرست امروز", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("چه کاری باید انجام بدی؟", fontSize = 13.sp) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = Color(0xFFE1E6E9),
                            focusedContainerColor = White,
                            unfocusedContainerColor = White
                        )
                    )
                    Spacer(Modifier.width(9.dp))
                    FilledIconButton(
                        onClick = {
                            val title = newTask.trim()
                            if (title.isNotEmpty()) {
                                update(tasks + Todo(System.currentTimeMillis(), title))
                                newTask = ""
                            }
                        },
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Green)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن", tint = White)
                    }
                }

                Spacer(Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (showDone) "کارهای انجام‌شده" else "کارهای باقی‌مانده",
                        color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    TextButton(onClick = { showDone = !showDone }) {
                        Text(if (showDone) "نمایش باقی‌مانده‌ها" else "نمایش انجام‌شده‌ها", color = Green, fontSize = 12.sp)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Green)
                    }
                }

                val visibleTasks = tasks.filter { it.done == showDone }
                if (visibleTasks.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (showDone) "هنوز کاری انجام نشده" else "همه‌چیز مرتبه! 🎉",
                                color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(if (showDone) "بعد از انجام کارها اینجا می‌بینیشون" else "یک کار جدید به فهرستت اضافه کن",
                                color = Muted, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visibleTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onToggle = {
                                    update(tasks.map { if (it.id == task.id) it.copy(done = !it.done) else it })
                                },
                                onDelete = { update(tasks.filterNot { it.id == task.id }) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .background(if (task.done) Green else Color.Transparent, CircleShape)
                    .then(if (!task.done) Modifier.background(Color.Transparent, CircleShape) else Modifier)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.done) {
                    Icon(Icons.Default.Check, contentDescription = "انجام شد", tint = White, modifier = Modifier.size(17.dp))
                } else {
                    Box(Modifier.size(21.dp).background(Color.Transparent, CircleShape))
                    Surface(shape = CircleShape, color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB9C4C9))) {
                        Box(Modifier.size(21.dp))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                task.title,
                modifier = Modifier.weight(1f),
                color = if (task.done) Muted else Ink,
                fontSize = 14.sp,
                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFADB7BC), modifier = Modifier.size(19.dp))
            }
        }
    }
}

private fun loadTasks(context: android.content.Context): List<Todo> {
    return try {
        val raw = context.getSharedPreferences("todos", 0).getString("items", "[]") ?: "[]"
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Todo(obj.getLong("id"), obj.getString("title"), obj.getBoolean("done"))
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveTasks(context: android.content.Context, tasks: List<Todo>) {
    val array = JSONArray()
    tasks.forEach { task ->
        array.put(JSONObject().apply {
            put("id", task.id)
            put("title", task.title)
            put("done", task.done)
        })
    }
    context.getSharedPreferences("todos", 0).edit().putString("items", array.toString()).apply()
}
