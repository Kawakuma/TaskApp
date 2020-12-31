//このクラスがデータを保存するクラス⇒モデルクラスとなる。

package jp.techacademy.kentaro.kawamura.taskapp

import java.io.Serializable  //データを丸ごと保存できるようになる。
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Task : RealmObject(), Serializable {
    var title: String = ""
    var contents: String = ""
    var date: Date = Date()

    var category: String = ""

    @PrimaryKey
    var id: Int = 0 //タスクひとつひとつに割り当てられたID
}