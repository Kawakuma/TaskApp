package jp.techacademy.kentaro.kawamura.taskapp

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import io.realm.Realm

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // SDKバージョンが26以上の場合、チャネルを設定する必要がある
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("default",   //第１引数にチャネルのID、第２にユーザーが認識できる名前、第３に重要度
                "Channel name",                         //IMPORTANCE_DEFAULTなら音が鳴る。
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Channel description"       //システム設定内でユーザーに表示する説明を設定
            notificationManager.createNotificationChannel(channel) //　()にチャネルを渡して登録
        }

        // 通知の設定を行う
        val builder = NotificationCompat.Builder(context, "default")
        builder.setSmallIcon(R.drawable.small_icon)
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.large_icon)) //◆Largeだけbitmap使う
        builder.setWhen(System.currentTimeMillis())   //いつの時刻が設定されてるの？
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setAutoCancel(true)

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        val taskId = intent!!.getIntExtra(EXTRA_TASK, -1) //◆inputActivity165行目のresultIntentの中に入ってるEXTRA_TASKを取り出している
        val realm = Realm.getDefaultInstance()
        val task = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        // タスクの情報を設定する
        builder.setTicker(task!!.title)   // ステータスバーに流れる文字を設定する。5.0以降は表示されない
        builder.setContentTitle(task.title)
        builder.setContentText(task.contents)

        // 通知をタップしたらアプリを起動するようにする
        val startAppIntent = Intent(context, MainActivity::class.java)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)//タップしたら画面の一番上にMainActivityを持ってきてね。                                //?
        val pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0)
        builder.setContentIntent(pendingIntent)  //フラグを何も入れないなら０

        // 通知を表示する
        notificationManager.notify(task!!.id, builder.build())
        realm.close()
    }
}