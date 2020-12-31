package jp.techacademy.kentaro.kawamura.taskapp

import android.view.View              //viewひとつひとつのUI　
import android.view.ViewGroup         //viewをまとめたもの
import android.widget.BaseAdapter
import android.view.LayoutInflater    //他のxmlリソースのViewを取り扱うための仕組み　　　
                                      //kotlinx.android.synthetic.main.activity_main.*ではviewを呼び出せない
import android.widget.TextView
import android.content.Context//

import java.text.SimpleDateFormat
import java.util.* //ここではLocaleを使うため


class TaskAdapter(context:Context):BaseAdapter(){
    //Contextはアプリの情報を管理しているクラス
    // LayoutInflaterでファイルをとってくるときにcontextが必要になるから

    private val mLayoutInflater: LayoutInflater
    var taskList= mutableListOf<Task>()

    //val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
    //mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
    //上の作業によってtitle、content、dateなどのデータはtaskListへ渡される。
    //これらのはデータは、ひとまとまりになってListへ渡されている。taskList(A,B,C)があったとして、Aの中にtitleやcontent、dateが入っているイメージ。

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return  taskList.size
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
        //各タスクの位置を返す。
    }

    override fun getItemId(position: Int): Long {
        //各タスクの枠に割り当てられているIdである。
        // このメソッドで出した値は、TaskクラスのIdに渡される。
        return 0
    }


    //getViewはセルを入れる枠をつくる。次にデータがあればもう枠を作る。リストにデータがある分呼ばれる関数。
    //convertViewは枠１個分。  //parentは枠ひとつひとつの集まり。
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view:View=convertView ?:mLayoutInflater.inflate(android.R.layout.simple_expandable_list_item_2,null)
        //最初はnullが入ってるため右辺が実行され、枠が１個作られる。
        // 枠が画面いっぱいになったら、新たに枠を作れない、つまりnullがなくなるため↑の文はスルーされる。


        //android.R.から始まるものはandroidStudio側が用意してくれているファイル
        //simple_expandable_list_item_2をCtrlを押しながら右クリックでtext1などの詳細を見れる。
        //これを読み込むためにinflaterを使っている
        //UI部品をとってくるメソッドがfindViewById　　// android:idと書かれているものもandroid側が作ってくれているview
        //だから呼び出すときはandroid.R.idで呼び出す

        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        //simple_expandable_list_item_2に入ってるtext1というセルをtextView1に渡す

        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        textView1.text = taskList[position].title

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        // yyyy年　MM月　dd日　HH:mm時間　, ◆Locale 日本の時刻で設定？
        val date = taskList[position].date  //MainActivityのDateで設定した日付時刻をdateに渡して・・・
        textView2.text = simpleDateFormat.format(date) //dateをyyyy-MM-dd HH:mmの形にしてtextView2へ渡す。



        return view
    }
}