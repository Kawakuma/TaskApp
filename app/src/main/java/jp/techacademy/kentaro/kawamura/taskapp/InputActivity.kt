package jp.techacademy.kentaro.kawamura.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent

class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->//_にviewは入れてもよい
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                //%02dで２桁表示させる。 注意点としてmonthは0月スタートである。
                date_button.text = dateString
            }, mYear, mMonth, mDay) //第3 4 5引数は初期値。
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false) //trueは24時間表記。falseはAM PMを選択するタイプ。
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()  //この関数でtitleやcontentのデータを保存する
        finish()   // finish() を呼ぶことでInputActivityを閉じて前の画面（MainActivity）に戻る。
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        //アクションバーにツールバーを付け加えるための一文。ツールバーを加えることでカスタムできる。 アクションバーは設定ボタンやタイトルが表示される。
        setSupportActionBar(toolbar)//ツールバーをアクションバーとして使えるようにしている。
        if (supportActionBar != null) {  //supportActionBarがnullでないことを証明している
            supportActionBar!!.setDisplayHomeAsUpEnabled(true) //setDisplayHomeAsUpEnabledによってActionBarに戻るボタンをつけている。
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        val intent = intent //この文は必要なし。
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)// EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        //◆EXTRA_TASKにデータが入っていなければ、taskIdには-1が代入される？
        //Mainの５３、５８行目のidを渡している//

        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        //タスクをひとつだけ取り出すためにfindFirstを使う。タスク入力画面にはタスク一つだけでいいため。

        //realm内でtaskIdが検索されて、最初に見つかったデータのインスタンスが渡される。 //
        //taskIdが-1の場合、nullとなる。つまりmTask=null。
        realm.close()

        if (mTask == null) {
            // 新規作成の場合　つまりフローティングボタンからここに飛んできたとき
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR) //◆このCalendarは現在の日時を設定している
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合 つまりタスクをタップしてここに飛んできたとき
            title_edit_text.setText(mTask!!.title)               //mTaskにtitle_edit_textデータを渡している//
            content_edit_text.setText(mTask!!.contents)
            InputCategory.setText(mTask!!.category)               ////

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date  //◆calendar.timeにdateを渡すことで、dateに入っている日時をmYEARなどに設定する？
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction() //今からデータベースに変更を加えます

        if (mTask == null) {//?
            //EXTRA_TASKにデータが渡ってきていなければ、Id-1となりmTask == null。つまり新規作成。
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =

                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1         //id作り
                } else {
                    0
                }

            //タスクが０件の時に備えてnullチェック


            mTask!!.id = identifier
        }


        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val inputCategory2 = InputCategory.text.toString()

        mTask!!.title = title   //◆mTaskがnull許容型なので
        mTask!!.contents = content
        mTask!!.category =inputCategory2   ////

        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time //calendar.timeにmYear, mMonth, mDay, mHour, mMinuteが入ってる
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!) //データを更新します
        realm.commitTransaction()          //データを確定します

        realm.close()




        //addTaskでタスクを追加したと同時に、以下の処理でアラームをセット。これによりTaskAlarmReceiverが指定時間に起動する
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        //TaskAlarmReceiver起動のためのIntent作成
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)  // resultIntentに遷移先のTaskAlarmReceiverが入ってる。このTaskAlarmReceiverに
                                                       // putExtraでEXTRA_TASK, mTask!!.idの値を渡している。

                                                       // TaskAlarmReceiverがタスクのタイトルなどを表示するためにmTask!!.idの情報が必要
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,  //タスク削除の際、アラームも同時に削除するためidを設定する
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT //既存のPendingIntentがあればextraのデータだけを置き換えるよ　という設定
        )//設定を立てるときフラグを立てる。

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager   //AlarmManagerはActivityのgetSystemServiceメソッドに引数ALARM_SERVICEを与えて取得する。
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
        // setメソッドの第一引数のRTC_WAKEUPは「UTC時間を指定する。画面スリープ中でもアラームを発行する」という指定。
        // 第二引数でタスクの時間をUTC時間で指定している。
    }


}