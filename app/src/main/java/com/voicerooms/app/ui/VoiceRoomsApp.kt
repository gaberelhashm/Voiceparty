package com.voicerooms.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import com.voicerooms.app.AgoraVoiceManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// ==================== Models ====================

data class VoiceRoom(
    val id: String,
    val name: String,
    val description: String,
    val speakers: Int,
    val listeners: Int,
    val isLive: Boolean = true,
    val category: String = "عام",
    val hostName: String = "مضيف"
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val time: String,
    val isMe: Boolean = false
)

data class Participant(
    val id: String,
    val name: String,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val isHost: Boolean = false,
    val isRaisedHand: Boolean = false,
    val role: String = "مستمع"
)

// ==================== Main App ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRoomsApp(
    currentUser: com.voicerooms.app.data.AppUser? = null,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentRoom by remember { mutableStateOf<VoiceRoom?>(null) }
    var showCreateRoom by remember { mutableStateOf(false) }

    val rooms = remember {
        mutableStateListOf(
            VoiceRoom("1", "غرفة السهرة", "دردشة عامة وموسيقى ومرح", 4, 128, true, "ترفيه", "أحمد"),
            VoiceRoom("2", "تعلم الإنجليزية", "ممارسة اللغة الإنجليزية يومياً", 2, 45, true, "تعليم", "سارة"),
            VoiceRoom("3", "ألعاب جماعية", "مناقشات ألعاب الفيديو", 6, 89, true, "ألعاب", "محمد"),
            VoiceRoom("4", "نقاش تقني", "برمجة وتطوير تطبيقات", 3, 67, true, "تقنية", "يوسف"),
            VoiceRoom("5", "موسيقى عربية", "أغاني وطلبات حية", 5, 210, true, "موسيقى", "ليلى"),
            VoiceRoom("6", "كاريوكي", "غنِّ معنا الآن!", 8, 156, true, "ترفيه", "نور")
        )
    }

    if (currentRoom != null) {
        RoomScreen(
            room = currentRoom!!,
            onLeave = { currentRoom = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VidorVoice",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "إشعارات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val tabs = listOf("الغرف", "الدردشة", "الألعاب", "الملف")
                val icons = listOf(
                    Icons.Filled.Home to Icons.Outlined.Home,
                    Icons.Filled.Email to Icons.Outlined.Email,
                    Icons.Filled.Star to Icons.Outlined.Star,
                    Icons.Filled.Person to Icons.Outlined.Person
                )
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) icons[index].first else icons[index].second,
                                contentDescription = title
                            )
                        },
                        label = { Text(title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> RoomsScreen(
                    rooms = rooms,
                    onJoinRoom = { room -> currentRoom = room },
                    onCreateRoom = { showCreateRoom = true }
                )
                1 -> GlobalChatScreen()
                2 -> GamesScreen()
                3 -> ProfileScreen(currentUser = currentUser, onLogout = onLogout)
            }
        }
    }

    if (showCreateRoom) {
        CreateRoomDialog(
            onDismiss = { showCreateRoom = false },
            onCreate = { name, desc, category ->
                val newRoom = VoiceRoom(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = desc,
                    speakers = 1,
                    listeners = 1,
                    isLive = true,
                    category = category,
                    hostName = "أنت"
                )
                rooms.add(0, newRoom)
                showCreateRoom = false
                currentRoom = newRoom
            }
        )
    }
}

// ==================== Rooms List ====================

@Composable
fun RoomsScreen(
    rooms: List<VoiceRoom>,
    onJoinRoom: (VoiceRoom) -> Unit,
    onCreateRoom: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onCreateRoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إنشاء غرفة جديدة", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "الغرف النشطة (${rooms.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(rooms, key = { it.id }) { room ->
                RoomCard(room = room, onClick = { onJoinRoom(room) })
            }
        }
    }
}

@Composable
fun RoomCard(room: VoiceRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${room.category} • ${room.hostName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (room.isLive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4ADE80).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "مباشر",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF4ADE80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = room.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.speakers} متحدث",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.listeners} مستمع",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==================== Create Room Dialog ====================

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ترفيه") }
    val categories = listOf("ترفيه", "تعليم", "ألعاب", "تقنية", "موسيقى", "عام")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء غرفة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الغرفة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("الوصف") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Text("التصنيف", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name.trim(), desc.trim().ifBlank { "غرفة صوتية جديدة" }, category)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("إنشاء ودخول")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

// ==================== Room Screen ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(room: VoiceRoom, onLeave: () -> Unit) {
    var isMuted by remember { mutableStateOf(true) }
    var isHandRaised by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf("جاري الاتصال...") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val participants = remember {
        mutableStateListOf(
            Participant("1", room.hostName, isSpeaking = true, isHost = true, role = "متحدث"),
            Participant("2", "سارة", isSpeaking = false, role = "متحدث"),
            Participant("3", "محمد", isMuted = true, role = "متحدث"),
            Participant("4", "ليلى", role = "متحدث"),
            Participant("5", "يوسف", role = "مستمع"),
            Participant("6", "نور", role = "مستمع"),
            Participant("7", "كريم", role = "مستمع"),
            Participant("me", "أنت", isMuted = true, role = "مستمع")
        )
    }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(sender = "أحمد", text = "مرحباً بالجميع في الغرفة 👋", time = "الآن"),
            ChatMessage(sender = "سارة", text = "يلا نبدأ السهرة!", time = "الآن")
        )
    }

    // الانضمام للقناة الصوتية عند الدخول
    DisposableEffect(room.id) {
        AgoraVoiceManager.onJoinSuccess = { channel, uid ->
            connectionStatus = "متصل ✓"
        }
        AgoraVoiceManager.onError = { code, msg ->
            connectionStatus = "خطأ: $code"
        }
        AgoraVoiceManager.onUserJoined = { uid ->
            // مستخدم جديد انضم
        }

        // اسم القناة = id الغرفة (كل اللي يدخلوا نفس الـ id يتكلموا مع بعض)
        val joined = AgoraVoiceManager.joinChannel(
            channelName = room.id,
            token = null, // للتجربة - لو فشل استخدم Temporary Token من Console
            asSpeaker = false
        )
        if (!joined) {
            connectionStatus = "فشل الاتصال"
        }

        onDispose {
            AgoraVoiceManager.leaveChannel()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val speakers = participants.filter { it.role == "متحدث" && !it.isMuted }
            if (speakers.isNotEmpty()) {
                val random = speakers.random()
                val index = participants.indexOfFirst { it.id == random.id }
                if (index >= 0) {
                    participants[index] = participants[index].copy(isSpeaking = true)
                    delay(1500)
                    if (index < participants.size) {
                        participants[index] = participants[index].copy(isSpeaking = false)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = room.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${participants.count { it.role == "متحدث" }} متحدث • ${participants.size} إجمالي",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeave) {
                        Icon(Icons.Default.Close, contentDescription = "مغادرة")
                    }
                },
                actions = {
                    IconButton(onClick = { showChat = !showChat }) {
                        Icon(
                            if (showChat) Icons.Default.Mic else Icons.Default.Email,
                            contentDescription = "دردشة",
                            tint = if (showChat) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = {
                                isMuted = !isMuted
                                // التحكم الحقيقي في المايك عبر Agora
                                AgoraVoiceManager.muteLocalAudio(isMuted)
                                val meIndex = participants.indexOfFirst { it.id == "me" }
                                if (meIndex >= 0) {
                                    participants[meIndex] = participants[meIndex].copy(
                                        isMuted = isMuted,
                                        role = if (!isMuted) "متحدث" else "مستمع"
                                    )
                                }
                            },
                            containerColor = if (isMuted) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isMuted) "إلغاء الكتم" else "كتم"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isMuted) "مكتوم" else "يتحدث",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = {
                                isHandRaised = !isHandRaised
                                val meIndex = participants.indexOfFirst { it.id == "me" }
                                if (meIndex >= 0) {
                                    participants[meIndex] = participants[meIndex].copy(
                                        isRaisedHand = isHandRaised
                                    )
                                }
                            },
                            containerColor = if (isHandRaised) Color(0xFFF59E0B)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isHandRaised) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.PanTool, contentDescription = "رفع اليد")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isHandRaised) "يد مرفوعة" else "رفع اليد",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = {
                                AgoraVoiceManager.leaveChannel()
                                onLeave()
                            },
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                            contentColor = Color(0xFFEF4444),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "مغادرة")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "مغادرة",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val speakers = participants.filter { it.role == "متحدث" }
            val listeners = participants.filter { it.role == "مستمع" }

            Text(
                text = "المتحدثون",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                items(speakers, key = { it.id }) { participant ->
                    ParticipantCard(participant)
                }
            }

            if (listeners.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "المستمعون (${listeners.size})",
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 160.dp)
                ) {
                    items(listeners, key = { it.id }) { participant ->
                        ListenerCard(participant)
                    }
                }
            }

            AnimatedVisibility(
                visible = showChat,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "دردشة الغرفة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            RoomChatBubble(msg)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("اكتب رسالة...") },
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    chatMessages.add(
                                        ChatMessage(
                                            sender = "أنت",
                                            text = messageText.trim(),
                                            time = "الآن",
                                            isMe = true
                                        )
                                    )
                                    messageText = ""
                                    scope.launch {
                                        delay(100)
                                        listState.animateScrollToItem(chatMessages.lastIndex)
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantCard(participant: Participant) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (participant.isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))
                            ),
                            shape = CircleShape
                        )
                )
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.name.first().toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (participant.isMuted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MicOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            if (participant.isHost) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = participant.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (participant.isSpeaking) {
            Text(
                text = "يتحدث...",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ListenerCard(participant: Participant) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.name.first().toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (participant.isRaisedHand) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PanTool,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = participant.name,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RoomChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (message.isMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!message.isMe) {
                    Text(
                        text = message.sender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (message.isMe) Color.White
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ==================== Global Chat ====================

@Composable
fun GlobalChatScreen() {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(sender = "أحمد", text = "مرحباً بالجميع! 👋", time = "10:30"),
            ChatMessage(sender = "سارة", text = "مين معانا النهاردة؟", time = "10:31"),
            ChatMessage(sender = "محمد", text = "أنا هنا، جاهز للدردشة", time = "10:32"),
            ChatMessage(sender = "ليلى", text = "يلا نبدأ السهرة 🔥", time = "10:33"),
            ChatMessage(sender = "يوسف", text = "من يريد يلعب ألعاب؟", time = "10:35")
        )
    }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "الدردشة العامة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("اكتب رسالة...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (text.isNotBlank()) {
                        messages.add(
                            ChatMessage(
                                sender = "أنت",
                                text = text.trim(),
                                time = "الآن",
                                isMe = true
                            )
                        )
                        text = ""
                        scope.launch {
                            delay(100)
                            listState.animateScrollToItem(messages.lastIndex)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.sender.first().toString(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.sender,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message.time,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

// ==================== Games ====================

@Composable
fun GamesScreen() {
    val games = listOf(
        Triple("لعبة الكلمات", Icons.Default.Edit, "خمّن الكلمة قبل الوقت"),
        Triple("تخمين الأغنية", Icons.Default.Star, "اعرف الأغنية من اللحن"),
        Triple("أسئلة عامة", Icons.Default.Info, "ثقافة عامة وتحديات"),
        Triple("رسم وتخمين", Icons.Default.Brush, "ارسم وخليهم يخمنوا"),
        Triple("السرعة", Icons.Default.PlayArrow, "أسرع إجابة تفوز"),
        Triple("حرب الأرقام", Icons.Default.Star, "حسابات سريعة")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "الألعاب الجماعية",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "قريباً سيتم تفعيل الألعاب داخل الغرف الصوتية",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(games) { (name, icon, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==================== Profile ====================

@Composable
fun ProfileScreen(
    currentUser: com.voicerooms.app.data.AppUser? = null,
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentUser?.displayName ?: "مستخدم",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = currentUser?.role?.name ?: "USER",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (currentUser != null) {
            Text(
                text = "ID: ${currentUser.uid.take(12)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("12", "غرف")
            StatItem("48", "ساعات")
            StatItem("5", "أصدقاء")
        }

        Spacer(modifier = Modifier.height(32.dp))

        ProfileMenuItem(Icons.Default.Settings, "الإعدادات")
        ProfileMenuItem(Icons.Default.Star, "VIP")
        ProfileMenuItem(Icons.Default.Info, "المساعدة")
        ProfileMenuItem(Icons.Default.Info, "حول التطبيق")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تسجيل الخروج")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
