package com.xunao.testlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.uniondrug.udlib.web.activity.UDWebFragment

class TestFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_fragment)
//        val map: MutableMap<String, String> = HashMap()
//        map["token"] = "6c0f60fb-d999-4547-b9b2-a1d49c434b4b"
//        map["assistantId"] = "17962"
//        val fragment = UDWebFragment.newInstance("uniondrugshop://app/udweb?mpProject=education",map)
//        supportFragmentManager.beginTransaction()
//            .add(R.id.fragment, fragment)
//            .addToBackStack("p")
//            .commit()
    }
}