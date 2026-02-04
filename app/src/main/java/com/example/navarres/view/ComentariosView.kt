package com.example.navarres.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.navarres.model.data.Comentario
import com.example.navarres.viewmodel.CommentThread
import com.example.navarres.viewmodel.ComentariosViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosView(
    restaurantId: String,
    restaurantName: String,
    onBack: () -> Unit,
    onCommentAdded: () -> Unit = {},
    viewModel: ComentariosViewModel = viewModel()
) {
    val threads by viewModel.threads.collectAsState()
    //ESto hace que sea asíncrona, si detecta algo actualiza la pantalla en otro hilo.
    val isLoading by viewModel.isLoading.collectAsState()

    var showWriteDialog by remember { mutableStateOf(false) }
    var commentToReply by remember { mutableStateOf<Comentario?>(null) }

    LaunchedEffect(restaurantId) {
        viewModel.cargarComentarios(restaurantId)
    }
    //Función que se ejecuta cuando se crea la pantalla, para traer todo s los comentarios de firebase

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reseñas", fontWeight = FontWeight.Bold)
                        Text(restaurantName, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    commentToReply = null
                    showWriteDialog = true
                },
                containerColor = Color(0xFFB30000),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Edit, null) },
                text = { Text("Escribir reseña") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFB30000))
            } else if (threads.isEmpty()) {
                Text("Sé el primero en opinar", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(threads) { thread ->
                        CommentThreadItem(
                            thread = thread,
                            onReplyClick = { parent ->
                                commentToReply = parent
                                showWriteDialog = true
                            },
                            onLikeClick = { comment -> viewModel.toggleLike(comment) }
                        )
                    }
                }
            }
        }
    }

    if (showWriteDialog) {
        WriteReviewDialog(
            replyingToName = commentToReply?.userName,
            onDismiss = { showWriteDialog = false },
            onSubmit = { text, rating ->
                // --- AQUÍ ESTÁ EL CAMBIO CLAVE PARA QUE SE ACTUALICE AL MOMENTO ---
                viewModel.enviarComentario(
                    restauranteId = restaurantId,
                    textoInput = text,
                    valoracionInput = rating,
                    parentId = commentToReply?.id,
                    onSuccess = {
                        // Solo actualizamos la media si NO es una respuesta
                        if (commentToReply == null) {
                            onCommentAdded()
                        }
                    }
                )

                showWriteDialog = false
                commentToReply = null
            }
        )
    }
}



@Composable
fun CommentThreadItem(
    thread: CommentThread,
    onReplyClick: (Comentario) -> Unit,
    onLikeClick: (Comentario) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        ReviewItemCard(
            comentario = thread.parent,
            isReply = false,
            replyCount = thread.replies.size,
            isExpanded = isExpanded,
            onReplyClick = { onReplyClick(thread.parent) },
            onLikeClick = { onLikeClick(thread.parent) },
            onExpandClick = { isExpanded = !isExpanded }
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                Row {
                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.LightGray.copy(alpha = 0.5f)))
                    Column {
                        thread.replies.forEach { reply ->
                            Spacer(modifier = Modifier.height(8.dp))
                            ReviewItemCard(
                                comentario = reply,
                                isReply = true,
                                onReplyClick = { },
                                onLikeClick = { onLikeClick(reply) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItemCard(
    comentario: Comentario,
    isReply: Boolean,
    replyCount: Int = 0,
    isExpanded: Boolean = false,
    onReplyClick: () -> Unit,
    onLikeClick: () -> Unit,
    onExpandClick: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val fechaStr = remember(comentario.date) { dateFormat.format(comentario.date) }

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val isLikedByMe = remember(comentario.likedBy, currentUserId) {
        comentario.likedBy.contains(currentUserId)
    }

    val bgColor = if (isReply) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    val cardElevation = if (isReply) 0.dp else 2.dp

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(cardElevation),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!comentario.userPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = comentario.userPhotoUrl, contentDescription = null,
                        modifier = Modifier.size(if (isReply) 28.dp else 36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(if (isReply) 28.dp else 36.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        val inicial = if (comentario.userName.isNotEmpty()) comentario.userName.first().toString() else "U"
                        Text(inicial, fontWeight = FontWeight.Bold, color = Color.White, fontSize = if(isReply) 12.sp else 14.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(comentario.userName, fontWeight = FontWeight.Bold, style = if (isReply) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium)
                    Text(fechaStr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                if (!isReply) {
                    Row {
                        repeat(5) { i ->
                            Icon(Icons.Rounded.Star, null, tint = if (i < comentario.rating) Color(0xFFFFC107) else Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(comentario.text, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onLikeClick() }.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLikedByMe) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        modifier = Modifier.size(16.dp),
                        tint = if(isLikedByMe) Color(0xFFB30000) else Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    if (comentario.likesCount > 0) {
                        Text("${comentario.likesCount}", style = MaterialTheme.typography.labelSmall, color = if(isLikedByMe) Color(0xFFB30000) else Color.Gray)
                    }
                }

                Spacer(Modifier.width(16.dp))

                if (!isReply) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onReplyClick() }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Reply, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text("Responder", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                if (!isReply && replyCount > 0) {
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onExpandClick() }.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isExpanded) "Ocultar" else "$replyCount respuestas", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065FD4), fontWeight = FontWeight.Bold)
                        Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFF065FD4), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewDialog(
    replyingToName: String? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (replyingToName != null) {
                Text("Respondiendo a $replyingToName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFB30000))
            } else {
                Text("¿Qué te ha parecido?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            if (replyingToName == null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        Icon(if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline, null, tint = Color(0xFFFFC107), modifier = Modifier.size(48.dp).clickable { rating = i }.padding(4.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = text, onValueChange = { text = it },
                label = { Text(if (replyingToName != null) "Respuesta..." else "Tu experiencia...") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                maxLines = 5, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFB30000), cursorColor = Color(0xFFB30000))
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                Button(onClick = { onSubmit(text, rating) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB30000)), enabled = text.isNotBlank(), shape = RoundedCornerShape(8.dp)) { Text("Publicar") }
            }
        }
    }
}