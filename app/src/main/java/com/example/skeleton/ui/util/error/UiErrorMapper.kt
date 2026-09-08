package com.example.skeleton.ui.util.error

import android.content.Context
import com.example.skeleton.R
import io.ktor.client.plugins.*
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toUiMessage(context: Context): String =
    when (this) {
        is SocketTimeoutException ->
            context.getString(R.string.request_timed_out)

        is IOException ->
            context.getString(R.string.please_check_your_internet_connection)

        is ClientRequestException ->
            context.getString(R.string.please_check_your_internet_connection)

        is ServerResponseException ->
            context.getString(R.string.the_server_is_busy)

        else ->
            context.getString(R.string.we_are_sorry)
    }
