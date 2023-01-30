package com.example.mapactivity.trackfragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.mapactivity.MainActivity
import com.example.mapactivity.R
import com.example.mapactivity.databinding.FragmentNotificationBinding


class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



//        binding.showNotification.setOnClickListener {
//            if (Build.VERSION.SDK_INT >= 33) {
//                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
//            } else {
//                hasNotificationPermissionGranted = true
//            }
//        }
        binding.showNotification.setOnClickListener {
//            if (requireActivity().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
//                showNotification()
//            }
            oneMore()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addNotification() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(requireActivity())
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Notifications Example")
            .setContentText("This is a test notification")
        val notificationIntent = Intent(requireActivity(), MainActivity::class.java)

        val contentIntent = PendingIntent.getActivity(
            requireActivity(), 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(contentIntent)

        // Add as notification
        val manager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        manager!!.notify(0, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun oneMore(){
        var NOTIFICATION_ID = 234
        val notificationManager1: NotificationManager =
            requireActivity().getSystemService(NotificationManager::class.java)
        var CHANNEL_ID: String = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CHANNEL_ID = "my_channel_01"
            val name: CharSequence = "my_channel"
            val Description = "This is my channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(false)

            notificationManager1.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Notifications Example")
            .setContentText("This is a test notification")

        val resultIntent = Intent(requireActivity(), MainActivity::class.java)
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(requireActivity())
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent: PendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
        notificationManager1.notify(NOTIFICATION_ID, builder.build())
    }

//    private val notificationPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            hasNotificationPermissionGranted = isGranted
//            if (!isGranted) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (Build.VERSION.SDK_INT >= 33) {
//                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
//                            showNotificationPermissionRationale()
//                        } else {
//                            showSettingDialog()
//                        }
//                    }
//                }
//            } else {
//                Toast.makeText(requireActivity(), "notification permission granted", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//
//    private fun showNotificationPermissionRationale() {
//
//        MaterialAlertDialogBuilder(requireActivity(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
//            .setTitle("Alert")
//            .setMessage("Notification permission is required, to show notification")
//            .setPositiveButton("Ok") { _, _ ->
//                if (Build.VERSION.SDK_INT >= 33) {
//                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun showSettingDialog() {
//        MaterialAlertDialogBuilder(requireActivity(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
//            .setTitle("Notification Permission")
//            .setMessage("Notification permission is required, Please allow notification permission from setting")
//            .setPositiveButton("Ok") { _, _ ->
//                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
//                intent.data = Uri.parse("package:${requireActivity().packageName}")
//                startActivity(intent)
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun showNotification() {
//
//        val channelId = "12345"
//        val description = "Test Notification"
//
//        val notificationManager =
//            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationChannel =
//                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
//            notificationChannel.lightColor = Color.BLUE
//
//            notificationChannel.enableVibration(true)
//            notificationManager.createNotificationChannel(notificationChannel)
//
//        }
//
//        val  builder = NotificationCompat.Builder(requireActivity(), channelId)
//            .setContentTitle("Hello World")
//            .setContentText("Test Notification")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setLargeIcon(
//                BitmapFactory.decodeResource(
//                    this.resources, R.drawable
//                        .ic_launcher_background
//                )
//            )
//        notificationManager.notify(12345, builder.build())
//    }


}