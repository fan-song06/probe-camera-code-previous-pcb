package com.lightresearch.probecamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import java.util.regex.Pattern

@Composable
fun VideoPlayer(uri: String, modifier: Modifier = Modifier) {
    val videoCompletionLiveData = MutableLiveData(false)

//    val videoCompleted by videoCompletionLiveData.observeAsState(initial = false)
//    if (videoCompleted) onCompletion()
    AndroidView(modifier = modifier, factory = { context ->
        VideoView(context).apply {
            setVideoURI(uri.toUri())
            setOnPreparedListener {
                it.isLooping = true
            }
            setOnCompletionListener {
                videoCompletionLiveData.value = true
            }
            start()

        }
    })

}

@Composable
fun FrescoImage(
    url: String,
    modifier: Modifier = Modifier,
    scaleType: ScalingUtils.ScaleType = ScalingUtils.ScaleType.CENTER_CROP
) {
    AndroidView(modifier = modifier, factory = { context ->
        SimpleDraweeView(context).apply {
            hierarchy = GenericDraweeHierarchyBuilder(context.resources).setActualImageScaleType(
                scaleType
            ).setRoundingParams(RoundingParams.fromCornersRadius(8f)).build()
        }
    }, update = { view ->
        view.setImageURI(url)
    })
}

fun extractUnixTimestamp(filename: String): Long {
    // Use a regular expression to find a sequence of digits in the filename.
    val pattern = Pattern.compile("(\\d+)")
    val matcher = pattern.matcher(filename)

    if (matcher.find()) {
        return matcher.group(1).toLong()
    }
    return 0
}

fun sortFilesByTimestamp(files: List<File>): List<File> {

    // Return the sorted list of files based on extracted Unix timestamps.
    return files.sortedBy { file -> extractUnixTimestamp(file.name) }.reversed()
}


class GalleryActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadAndDisplayImages()
        } else {
            // Handle permission denied scenario
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val directoryPath = intent.getStringExtra("directory_path")
        if (directoryPath == null) {
            finish()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadAndDisplayImages()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadAndDisplayImages() {
        val directoryPath = intent.getStringExtra("directory_path")


        lifecycleScope.launch {

            setContent {
                if (!directoryPath.isNullOrEmpty()) {
                    Gallery(directoryPath)
                } else {
                    Text("Missing directory path for gallery", Modifier.padding(8.dp))
                }
            }
        }
    }

    private fun loadMediaFromDirectory(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        val unsorted = directory.listFiles { _, name ->
            name.endsWith(".jpg", ignoreCase = true) || name.endsWith(
                ".png", ignoreCase = true
            ) || name.endsWith(".mp4", ignoreCase = true)
        }?.toList() ?: emptyList()
        return sortFilesByTimestamp(unsorted)
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Gallery(directoryPath: String) {
        val imageIcon = painterResource(R.drawable.baseline_image_45)
        val videoIcon = painterResource(R.drawable.baseline_play_arrow_24)
        var viewedItem by remember { mutableStateOf<File?>(null) }
        var selectedItems by remember { mutableStateOf<List<String>>(listOf()) }
        var isSelecting = remember(selectedItems) { selectedItems.isNotEmpty() }
        var mediaFiles by remember {mutableStateOf<List<File>>(listOf())}
        fun loadMediaFiles(): List<File> {
            return loadMediaFromDirectory(directoryPath ?: "")
        }

        LaunchedEffect(directoryPath){
            mediaFiles = loadMediaFiles()
        }
        BackHandler(viewedItem != null) {
            viewedItem = null
        }
        val selectedIsVideo = remember(viewedItem) {
            viewedItem != null && viewedItem?.extension.equals(
                "mp4", ignoreCase = true
            )
        }

        fun deleteSelected() {
            selectedItems.forEach { File(it).delete() }
            mediaFiles = loadMediaFiles()
            selectedItems = listOf()
        }

        if (viewedItem == null) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(60.dp)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Probe Images", color = Color.Gray, fontSize = 20.sp)
                    if (isSelecting) {
                        Row(
                            Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${selectedItems.size} item${if (selectedItems.size == 1) "" else "s"} selected",
                                    color = Color.Gray
                                )
                                IconButton(
                                    modifier = Modifier.size(30.dp),
                                    onClick = { deleteSelected() }) {
                                    Icon(
                                        painterResource(R.drawable.baseline_delete_24),
                                        "Delete",
                                        tint = Color(0xffdd2222),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Checkbox(
                                    checked = true,
                                    onCheckedChange = {
                                        selectedItems = listOf()
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xff999999)),
                                    modifier = Modifier.scale(1.3f)
                                )
                            }
                        }
                    }
                }
                Divider()
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(if (isSelecting) 0xffdedede else 0xffefefef)),
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(mediaFiles.size) { imageIdx ->
                        val mediaFile = mediaFiles[imageIdx]
                        val isSelected = selectedItems.contains(mediaFile.absolutePath)
                        val isVideo = mediaFile.extension.equals("mp4", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .shadow(if (isSelected) 8.dp else 0.dp)
                                .border(
                                    2.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(Color.Gray) // this will be visible during image load or if an image has transparency
                                .combinedClickable(onClick = {
                                    if (isSelecting) {
                                        val isInList =
                                            selectedItems.contains(mediaFile.absolutePath)
                                        if (isInList) {
                                            val newList = selectedItems.toMutableList()
                                            newList.remove(mediaFile.absolutePath)
                                            selectedItems = newList
                                        } else {
                                            selectedItems =
                                                selectedItems + listOf(mediaFile.absolutePath)
                                        }
                                    } else viewedItem = mediaFile
                                }, onLongClick = {
                                    if (!isSelecting) selectedItems = listOf(mediaFile.absolutePath)
                                })

                        ) {
                            FrescoImage(
                                url = mediaFile.toUri().toString(),
                                modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    if (isVideo) videoIcon else imageIcon,
                                    contentDescription = "Image",
                                    modifier = Modifier.alpha(0.5f)
                                )
                            }
                            if (isSelecting) {
                                Row(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xeeffffff),
                                            checkmarkColor = Color.Gray,
                                            uncheckedColor = Color(0x88ffffff)
                                        )
                                    )
                                }
                            }

                        }
                    }
                }

            }
        }

        viewedItem?.let { file ->
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { viewedItem = null }  // Reset on click
            ) {
                if (selectedIsVideo) {
                    VideoPlayer(uri = file.toUri().toString(), Modifier.fillMaxSize())
                } else {
                    FrescoImage(
                        url = file.toUri().toString(),
                        modifier = Modifier.fillMaxSize(),
                        ScalingUtils.ScaleType.FIT_CENTER
                    )
                }
            }
        }
    }

}