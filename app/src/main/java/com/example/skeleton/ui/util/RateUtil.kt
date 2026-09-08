package com.example.skeleton.ui.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.skeleton.R
import com.google.android.play.core.review.ReviewManagerFactory

object RateUtil {
    /********************************
     * send Feedback To Google Play Store
     */
    fun sendFeedbackToGooglePlay(
        activity: Activity,
    ) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) { // open in-app review
                val reviewInfo = request.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.thanks_for_your_feedback),
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    it.printStackTrace()
                }
            } else { // open Google Play Store to review
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${activity.packageName}")
                    )
                )
                Toast.makeText(
                    activity,
                    activity.getString(R.string.thanks_for_your_feedback),
                    Toast.LENGTH_SHORT
                ).show()
            }
            LogUtil.logcat(message = "addOnCompleteListener")
        }.addOnCanceledListener {
            LogUtil.logcat(message = "addOnCanceledListener")
        }.addOnFailureListener {
            LogUtil.logcat(message = "addOnFailureListener")
        }.addOnSuccessListener {
            LogUtil.logcat(message = "addOnSuccessListener")
        }
    }

    /********************************
     * compose email
     */
    fun composeEmail(
        context: Context,
        feedbackStar: Int,
        feedbackMessage: String,
    ) {
        try {
            val subject = "[Android Compose Skeleton] Feedback"
            val body =
                "• Application Name: ${context.getString(R.string.app_name)}" + "\n\n• Rate star: $feedbackStar star / 5 star" + "\n\n• Customer feedback: ${feedbackMessage.trim()}"
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("po@astronex.ai"))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}