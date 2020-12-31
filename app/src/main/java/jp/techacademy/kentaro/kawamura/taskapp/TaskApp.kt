//◆モデルクラスで定義したデータを、このクラスで定義したデータベース(realm)に保存する？

package jp.techacademy.kentaro.kawamura.taskapp

import android.app.Application
import io.realm.Realm

class TaskApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)//ここでrealmを初期化する。
    }
}