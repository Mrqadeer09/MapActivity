package com.example.mapactivity

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mapactivity.trackfragment.GeoFragment

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

  /*      val fragment = GeoFragment()
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()*/

      /*  if (savedInstanceState == null) {
            val fragmentManager: FragmentManager = supportFragmentManager
            val newFragment: GeoFragment = GeoFragment()
            val ft: FragmentTransaction = fragmentManager.beginTransaction()
            ft.add(R.id.fragment_container, newFragment).commit()
        }*/

        val fragment= GeoFragment();

        // Open fragment
        getSupportFragmentManager()
            .beginTransaction().replace(R.id.fragment_container,fragment)
            .commit()

    }


}