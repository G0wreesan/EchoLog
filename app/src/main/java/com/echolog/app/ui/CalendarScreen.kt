package com.echolog.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.data.LogEntity
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.viewmodel.LogViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    viewModel: LogViewModel,
    onAddLog: (LocalDate) -> Unit
) {
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()
    val logsByDate by viewModel.calendarLogs.collectAsState()

    val yearMonth = YearMonth.from(selectedDate)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Text(
            text = "${yearMonth.month.name} ${yearMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(280.dp)
        ) {
            items(firstOfMonth) {
                Spacer(Modifier.fillMaxSize())
            }

            items(daysInMonth) { day ->
                val date = yearMonth.atDay(day + 1)
                val isSelected = date == selectedDate

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xCC3FC1FD) else Color.Transparent)
                        .clickable { viewModel.updateSelectedDate(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (day + 1).toString(),
                        color = if (isSelected) Color.White else Color.Black,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${selectedDate.dayOfMonth} ${selectedDate.month.name}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        text = selectedDate.dayOfWeek.name,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = { onAddLog(selectedDate) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xCC3FC1FD), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Log", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (logsByDate.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No entries for this day.", color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(logsByDate) { log ->
                        HomeLogCard(log = log, onClick = {})
                    }
                }
            }
        }
    }
}
