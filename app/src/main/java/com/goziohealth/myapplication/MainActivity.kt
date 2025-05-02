package com.goziohealth.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import com.goziohealth.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Comment out SharedTransitionLayout or downgrade to constraintlayout-compose to 1.1.0 to see the correct behavior.
                SharedTransitionLayout {
                    Content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun Content() {
    val scrollBehavior = exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Toolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            scrollBehavior.state.heightOffset += delta
                        }
                    ),
                progress = scrollBehavior.state.collapsedFraction,
                onCollapsingContentSizeChange = {
                    /* After the disappearing content has been calculated at it's peak height,
                    in PX, we can set the heightOffsetLimit to the negation of that value.
                    This tells the scrollState that this is the max value, in PX, the toolbar
                    can collapse. */
                    scrollBehavior.state.heightOffsetLimit = -it
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(padding)
                .padding(padding)
                .navigationBarsPadding()
        ) {
            items(count = 100) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = "Item $it", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMotionApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun Toolbar(
    modifier: Modifier = Modifier,
    progress: Float,
    onCollapsingContentSizeChange: (px: Float) -> Unit,
) {
    val context = LocalContext.current
    val motionScene = with(context) {
        remember {
            resources.openRawResource(R.raw.motion_scene_detail)
                .readBytes()
                .decodeToString()
        }
    }

    val rating: String? by remember { mutableStateOf<String?>("4") }

    // Total height of the content that will disappear during animation
    var totalHeightPx by remember { mutableFloatStateOf(0f) }

    var imageHeightPx by remember { mutableFloatStateOf(0f) }
    var specialtiesHeightPx by remember { mutableFloatStateOf(0f) }
    var ratingsHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(imageHeightPx, specialtiesHeightPx, ratingsHeightPx) {
        val calculatedHeight = imageHeightPx + specialtiesHeightPx + ratingsHeightPx
        if (calculatedHeight > totalHeightPx) {
            // We only want to take the max height of all the content
            totalHeightPx = calculatedHeight
            onCollapsingContentSizeChange(totalHeightPx)
        }
    }

    Surface(
        modifier = modifier,
    ) {
        MotionLayout(
            motionScene = MotionScene(motionScene),
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .padding(4.dp)
                    .layoutId("nav_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .padding(
                        top = 12.dp,
                        bottom = 8.dp
                    )
                    .size(78.dp)
                    .clip(CircleShape)
                    .onSizeChanged {
                        val newHeight = it.height.toFloat()
                        if (newHeight > imageHeightPx) {
                            imageHeightPx = newHeight
                        }
                    }
                    .layoutId("user_image"),
            )
            Text(
                text = "Username",
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .layoutId("user_name")
            )
            Text(
                text = "Extra text describing person",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier
                    .padding(
                        vertical = 8.dp,
                        horizontal = 16.dp
                    )
                    .onSizeChanged {
                        val newHeight = it.height.toFloat()
                        if (newHeight > imageHeightPx) {
                            specialtiesHeightPx = newHeight
                        }
                    }
                    .layoutId("extra_text")
            )
            rating?.let { rating ->
                Text(
                    text = "rating",
                    modifier = Modifier
                        .onSizeChanged {
                            val newHeight = it.height.toFloat()
                            if (newHeight > imageHeightPx) {
                                ratingsHeightPx = newHeight
                            }
                        }
                        .layoutId("rating_text")
                )
            }

            LazyRow(
                modifier = Modifier
                    .padding(8.dp)
                    .layoutId("button_bar")
            ) {
                items(count = 4) {
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Item $it", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}
