package com.example.virtualtryon
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var drawerLayout: DrawerLayout

    private lateinit var loadingProgressBar: ProgressBar
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        loadingProgressBar = findViewById(R.id.progressBar)

//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, HomeFragment()).commit()
//            navigationView.setCheckedItem(R.id.nav_home)
//        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            isAdmin = it.email == "admin123@gmail.com"
        }

        if (savedInstanceState == null) {
            val email = intent.getStringExtra("user_email")
            if (email == "admin123@gmail.com") {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ClothUploadForAdmin()).commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()
            }
            navigationView.setCheckedItem(R.id.nav_home)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_save -> {
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, SaveFragment()).commit()
                if (isAdmin) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ClothFragment()).commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SaveFragment()).commit()
                }
            }
            R.id.nav_home -> {
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, HomeFragment()).commit()
                if (isAdmin) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ClothUploadForAdmin()).commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                }
            }

            R.id.nav_logout -> {
                // Perform logout action
                FirebaseAuth.getInstance().signOut()
                // Navigate to sign-in activity
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showProgressBar() {
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        loadingProgressBar.visibility = View.GONE
    }


}
