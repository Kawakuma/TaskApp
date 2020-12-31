package jp.techacademy.kentaro.kawamura.taskapp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import java.util.*
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.view.View
import android.widget.EditText
import io.realm.RealmQuery

const val EXTRA_TASK ="jp.techacademy.kentaro.kawamura.taskapp"//ほかのアプリのExtraと間違えないように
//◆const？


class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    //OnCreateが始まるまでlateinit変数ので初期化を遅らせる。型によってはライフサイクル内でなければ初期化できないものがあるから。

    private val mRealmListener = object : RealmChangeListener<Realm> {   //ﾃﾞｰﾀﾍﾞｰｽへの追加更新があるとき呼ばれる。◆この形は無名関数？
            override fun onChange(element: Realm) { //変更された情報が入っているのがelement
                reloadListView()
            }
        }
    private lateinit var mTaskAdapter: TaskAdapter

    private val searchClickListener = View.OnClickListener {              ////
        searchReloadView()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_button.setOnClickListener(searchClickListener)              ////



        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)//floatingButtonでinput画面へ飛ぶ
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance() //Realmクラスのオブジェクト取得。
        mRealm.addChangeListener(mRealmListener)//◆この括弧内に設定することにより追加更新があればonChangeメソッドが動く


        mTaskAdapter = TaskAdapter(this@MainActivity)
        //◆Layoutinflaterを使うためにcontextを渡している


        //下の処理によってタスク(枠)１個ををタップした時、入力画面へ遷移する。
        listView1.setOnItemClickListener { parent, view, position, id ->   //◆TaskList(ListView1)のタスク1個分をタップ。その位置とID
            val task = parent.adapter.getItem(position) as Task
            //parent (つまりlistView1)から、アダプターを介して、タップした項目を取り出す。それはタスク型のデータ。
            //それをval taskに渡す。

            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id) //遷移先にEXTRA_TASKというキーでtask.idを送り込む
            startActivity(intent)

        }


        //下の処理によって、タスク１個を長押ししたとき、
        listView1.setOnItemLongClickListener { parent, view, position, id ->

            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ -> //　第2引数にはwhich(どれが押されたか)
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()
                //?
                mRealm.beginTransaction()
                results.deleteAllFromRealm() //削除
                mRealm.commitTransaction()



                //以下の処理でタスク削除と同時にアラームを削除する
                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)

                val resultPendingIntent = PendingIntent.getBroadcast( //特定の時刻になったときにintentを発生させるための設定
                    this@MainActivity,
                    task.id,//ここでどのﾍﾟﾝﾃﾞｨﾝｸﾞインテントか判定している
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )//セットした時と同じintentとPendingIntentを作成して・・・
                // 下の処理でアラームをキャンセルする。

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)


                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)//◆nullでなにもおこなわない？

            val dialog = builder.create()
            dialog.show()

            true   //これらの処理が正しく終わったときtrueを返す
        }

        reloadListView()
    }


    private fun reloadListView() {
        val taskRealmResults =
            mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
        //mRealmの中の、Ｔａｓｋのテーブル(保存してる領域)からとってきてくださいね。(　where(Task::class.java)　)
        //findAllで全件とってきて、それらをdateの情報を用いて降順に並び替える。
        //これらはタスク型のデータである。


        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        //mRealm.copyFromRealm(taskRealmResults) でコピーしてアダプターに渡す。
        // Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は、直接渡すのではなく、
        // このようにコピーして渡す必要がある。


        listView1.adapter = mTaskAdapter
        //mTaskAdapterのデータをadapterを介してlistView1に渡す。

        mTaskAdapter.notifyDataSetChanged()
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
    }


    //アプリを終了させた時、Realmも一緒に閉じる
    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }





    private fun searchReloadView() {
        val searchKey =search_text
        val searchWord =searchKey.text.toString()     //edit.textの変換は.textで取り出してから

        val categoryRealmResults =
            mRealm.where(Task::class.java).equalTo("category",searchWord).findAll().sort("date", Sort.DESCENDING)

        mTaskAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)
        listView1.adapter = mTaskAdapter

        mTaskAdapter.notifyDataSetChanged()
    }
}





