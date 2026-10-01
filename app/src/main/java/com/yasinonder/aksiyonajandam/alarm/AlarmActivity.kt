package com.yasinonder.aksiyonajandam.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.yasinonder.aksiyonajandam.data.ActionDatabase
import com.yasinonder.aksiyonajandam.ui.theme.AksiyonAjandamTheme

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra(AlarmReceiver.EXTRA_ID, -1L)

        setContent {
            AksiyonAjandamTheme {
                val item = remember(id) { ActionDatabase.get(this).byId(id) }

                Column(
                    modifier = Modifier.fillMaxSize().padding(28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aksiyon zamanı", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        item?.title ?: "Hatırlatma",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (!item?.subject.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(item?.subject.orEmpty())
                    }
                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            ActionDatabase.get(this@AlarmActivity).setCompleted(id, true)
                            AlarmScheduler.cancel(this@AlarmActivity, id)
                            NotificationManagerCompat.from(this@AlarmActivity).cancel(id.toInt())
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tamamlandı")
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            AlarmScheduler.snooze(this@AlarmActivity, id, 10)
                            NotificationManagerCompat.from(this@AlarmActivity).cancel(id.toInt())
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("10 dakika ertele")
                    }

                    OutlinedButton(
                        onClick = {
                            AlarmScheduler.snooze(this@AlarmActivity, id, 30)
                            NotificationManagerCompat.from(this@AlarmActivity).cancel(id.toInt())
                            finish()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("30 dakika ertele")
                    }
                }
            }
        }
    }
}
