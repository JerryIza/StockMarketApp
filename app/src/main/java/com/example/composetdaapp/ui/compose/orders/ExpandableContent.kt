package com.example.composetdaapp.ui.compose.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composetdaapp.data.entities.orders.get.GetOrderItem
import com.example.composetdaapp.utils.COLLAPSE_ANIMATION_DURATION
import com.example.composetdaapp.utils.EXPAND_ANIMATION_DURATION
import com.example.composetdaapp.utils.FADE_IN_ANIMATION_DURATION
import com.example.composetdaapp.utils.FADE_OUT_ANIMATION_DURATION


@ExperimentalAnimationApi
@Composable
fun ExpandableContent(
    visible: Boolean = true,
    initialVisibility: Boolean = false,
    orders: GetOrderItem
) {

    val orderInfo = "@ a " + orders.orderType + " of $" + orders.price

    val enterFadeIn = remember {
        fadeIn(
            animationSpec = TweenSpec(
                durationMillis = FADE_IN_ANIMATION_DURATION,
                easing = FastOutLinearInEasing
            )
        )
    }
    val enterExpand = remember {
        expandVertically(animationSpec = tween(EXPAND_ANIMATION_DURATION))
    }
    val exitFadeOut = remember {
        fadeOut(
            animationSpec = TweenSpec(
                durationMillis = FADE_OUT_ANIMATION_DURATION,
                easing = LinearOutSlowInEasing
            )
        )
    }
    val exitCollapse = remember {
        shrinkVertically(animationSpec = tween(COLLAPSE_ANIMATION_DURATION))
    }
    AnimatedVisibility(
        visible = visible,
        initiallyVisible = initialVisibility,
        enter = enterExpand + enterFadeIn,
        exit = exitCollapse + exitFadeOut
    ) {
        Column(
            /* modifier = Modifier
                 .background(color = Color.Red),*/
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(
                text = orderInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center,

                )
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp),
            )
            {
                OrderBtn(text = "Edit")
                OrderBtn(text = "Delete")
            }
        }


    }
}