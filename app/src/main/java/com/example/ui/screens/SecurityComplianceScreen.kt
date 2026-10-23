package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogSeverity
import com.example.data.model.SecurityLogEntry
import com.example.ui.theme.MonochromeBlack
import com.example.ui.theme.MonochromeCardBorder
import com.example.ui.theme.MonochromeCardSurface
import com.example.ui.theme.MonochromeDarkSurface
import com.example.ui.theme.MonochromeLightGrey
import com.example.ui.theme.MonochromeMediumGrey
import com.example.ui.theme.MonochromeMutedText
import com.example.ui.theme.MonochromeWhite
import com.example.ui.viewmodels.IsoComplianceItem
import com.example.ui.viewmodels.SecurityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityComplianceScreen(
    securityViewModel: SecurityViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val checklist = remember { securityViewModel.getComplianceChecklist() }
    val logs by securityViewModel.logs.collectAsState()

    Scaffold(
        containerColor = MonochromeBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MonochromeWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seguridad ISO/IEC 27400", style = MaterialTheme.typography.titleMedium, color = MonochromeWhite)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MonochromeWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MonochromeBlack
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MonochromeBlack)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MonochromeBlack,
                contentColor = MonochromeWhite,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MonochromeWhite
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Cláusulas ISO/IEC 27400", color = if (selectedTab == 0) MonochromeWhite else MonochromeMutedText) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Auditoría en Vivo (${logs.size})", color = if (selectedTab == 1) MonochromeWhite else MonochromeMutedText) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Cumplimiento Estándar Internacional",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MonochromeWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "La norma ISO/IEC 27400 establece directrices de ciberseguridad y privacidad para sistemas IoT del consumidor. Esta aplicación implementa controles de extremo a extremo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MonochromeLightGrey
                                )
                            }
                        }
                    }

                    items(checklist) { item ->
                        ComplianceItemCard(item)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registro de Eventos Criptográficos y de Red",
                            style = MaterialTheme.typography.labelMedium,
                            color = MonochromeMutedText
                        )

                        Row {
                            IconButton(onClick = {
                                val textLogs = logs.joinToString("\n") {
                                    "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it.timestamp))}] [${it.severity}] ${it.eventType}: ${it.description} (Op: ${it.operator})"
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("IoT Security Logs", textLogs)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Logs copiados al portapapeles", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar logs", tint = MonochromeWhite)
                            }

                            IconButton(onClick = { securityViewModel.clearAuditLogs() }) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar logs", tint = MonochromeMutedText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay eventos registrados aún.", style = MaterialTheme.typography.bodyMedium, color = MonochromeMutedText)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(logs) { log ->
                                SecurityLogCard(log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComplianceItemCard(item: IsoComplianceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Cumplido",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                    Text(
                        text = item.standardRef,
                        style = MaterialTheme.typography.labelSmall,
                        color = MonochromeLightGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MonochromeLightGrey
            )

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MonochromeDarkSurface)
                    .border(1.dp, MonochromeCardBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Implementación: ${item.implementationDetails}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MonochromeWhite
                )
            }
        }
    }
}

@Composable
fun SecurityLogCard(log: SecurityLogEntry) {
    val timeStr = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (log.severity == LogSeverity.CRITICAL) MonochromeWhite else MonochromeMutedText)
                    .padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.eventType,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MonochromeMutedText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Operador: ${log.operator}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MonochromeMutedText
                )
            }
        }
    }
}
