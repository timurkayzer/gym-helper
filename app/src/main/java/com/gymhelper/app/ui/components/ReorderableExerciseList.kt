package com.gymhelper.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun <T> ReorderableExerciseList(
    items: List<T>,
    key: (T) -> Any,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T, Modifier) -> Unit,
) {
    val mutableItems = remember { mutableStateListOf<T>() }

    LaunchedEffect(items) {
        val currentKeys = mutableItems.map { key(it) }
        val newKeys = items.map { key(it) }
        if (currentKeys != newKeys) {
            mutableItems.clear()
            mutableItems.addAll(items)
        }
    }

    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        mutableItems.add(to.index, mutableItems.removeAt(from.index))
        onReorder(mutableItems.toList())
    })

    LazyColumn(
        state = reorderState.listState,
        modifier = modifier.reorderable(reorderState),
    ) {
        items(mutableItems, key = { key(it) }) { item ->
            ReorderableItem(reorderState, key(item)) { _ ->
                itemContent(
                    item,
                    Modifier
                        .fillMaxWidth()
                        .detectReorderAfterLongPress(reorderState),
                )
            }
        }
    }
}

@Composable
fun ReorderDragHandle(modifier: Modifier = Modifier) {
    Icon(
        Icons.Default.DragHandle,
        contentDescription = "Drag to reorder",
        modifier = modifier.padding(end = 8.dp),
    )
}

@Composable
fun ReorderableTextRow(
    title: String,
    subtitle: String?,
    dragModifier: Modifier,
    trailing: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = dragModifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReorderDragHandle()
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
        trailing()
    }
}
