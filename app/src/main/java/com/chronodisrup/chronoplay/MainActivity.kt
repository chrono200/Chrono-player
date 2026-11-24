package com.chronodisrup.chronoplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chronodisrup.chronoplay.core.designsystem.ChronoOrange
import com.chronodisrup.chronoplay.core.designsystem.ChronoTheme

private enum class Destination(val label: String) {
    HOME("Home"), DISCOVER("Discover"), SEARCH("Search"), LIBRARY("Library")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChronoTheme { ChronoRoot() } }
    }
}

@Composable
private fun ChronoRoot() {
    var selected by remember { mutableStateOf(Destination.HOME) }
    var showPlayer by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                MiniPlayer(onClick = { showPlayer = true })
                NavigationBar(containerColor = Color(0xFF101010), tonalElevation = 0.dp) {
                    Destination.entries.forEach { destination ->
                        val icon = when (destination) {
                            Destination.HOME -> Icons.Default.Home
                            Destination.DISCOVER -> Icons.Default.Explore
                            Destination.SEARCH -> Icons.Default.Search
                            Destination.LIBRARY -> Icons.Default.LibraryMusic
                        }
                        NavigationBarItem(
                            selected = selected == destination,
                            onClick = { selected = destination },
                            icon = { Icon(icon, contentDescription = destination.label) },
                            label = { Text(destination.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ChronoOrange,
                                selectedTextColor = ChronoOrange,
                                indicatorColor = Color(0xFF2B1712),
                                unselectedIconColor = Color(0xFF777777),
                                unselectedTextColor = Color(0xFF777777)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                Destination.HOME -> HomeScreen()
                Destination.DISCOVER -> SimpleScreen("Discover", "Find your next obsession.")
                Destination.SEARCH -> SimpleScreen("Search", "Search will connect to your Muriel server in the next build.")
                Destination.LIBRARY -> SimpleScreen("Your library", "Your saved music will live here.")
            }
            if (showPlayer) {
                PlayerSheet(onClose = { showPlayer = false })
            }
        }
    }
}

@Composable
private fun HomeScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("CHRONO", color = ChronoOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("PLAY", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                IconButton(onClick = {}) { Icon(Icons.Default.Settings, "Settings", tint = Color.LightGray) }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YOUR SOUND. YOUR TIME.", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp)
                Text("A world of music, tuned to you.", color = Color(0xFF929292), fontSize = 14.sp)
            }
        }
        item {
            Surface(
                color = Color(0xFF1C1715),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MADE FOR THE MOMENT", color = ChronoOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("Press play.
Leave the noise behind.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
                    Text("Your next favourite track is one search away.", color = Color(0xFFB4A7A2), fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = ChronoOrange, shape = RoundedCornerShape(50), modifier = Modifier.clickable { }.padding(0.dp)) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Start listening", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("Your rotation", "Made for your taste") }
        item { EmptyMusicCard("Your daily mix is taking shape", "Play a few tracks and CHRONO will learn your sound.") }
        item { SectionTitle("Explore", "Find something new") }
        item { EmptyMusicCard("Discover your next favourite", "Trending music and recommendations arrive in the next build.") }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color(0xFF858585), fontSize = 12.sp)
    }
}

@Composable
private fun EmptyMusicCard(title: String, subtitle: String) {
    Surface(color = Color(0xFF141414), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(Color(0xFF29201D), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.GraphicEq, null, tint = ChronoOrange, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Color(0xFF858585), fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun SimpleScreen(title: String, description: String) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(description, color = Color(0xFF929292), fontSize = 14.sp)
    }
}

@Composable
private fun MiniPlayer(onClick: () -> Unit) {
    Surface(color = Color(0xFF1B1B1B), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(Color(0xFF30201B), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MusicNote, null, tint = ChronoOrange)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Nothing playing yet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Pick a track to get started", color = Color(0xFF858585), fontSize = 11.sp)
            }
            Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun PlayerSheet(onClose: () -> Unit) {
    Surface(color = Color(0xFF090909).copy(alpha = 0.98f), modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, "Close player", tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Text("NOW PLAYING", color = Color(0xFF999999), fontSize = 10.sp, letterSpacing = 2.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) { Icon(Icons.Default.MoreHoriz, "More", tint = Color.White) }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF211713), RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.GraphicEq, null, tint = ChronoOrange, modifier = Modifier.size(100.dp))
                }
                Text("No track selected", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Your music starts here", color = Color(0xFF999999), fontSize = 14.sp)
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth(), color = ChronoOrange, trackColor = Color(0xFF333333))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0:00", color = Color.Gray, fontSize = 11.sp)
                    Text("0:00", color = Color.Gray, fontSize = 11.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shuffle, "Shuffle", tint = Color.Gray)
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                    Surface(color = ChronoOrange, shape = RoundedCornerShape(50), modifier = Modifier.size(64.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(36.dp)) }
                    }
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                    Icon(Icons.Default.Repeat, "Repeat", tint = Color.Gray)
                }
            }
        }
    }
}
