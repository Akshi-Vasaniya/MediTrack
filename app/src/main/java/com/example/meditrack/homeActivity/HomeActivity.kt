package com.example.meditrack.homeActivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.example.meditrack.R
import com.example.meditrack.dataModel.dataClasses.UserData
import com.example.meditrack.databinding.ActivityHomeBinding
import com.example.meditrack.firebase.fBase
import com.example.meditrack.mainActivity.MainActivity
import com.example.meditrack.utility.UtilityFunction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var binding: ActivityHomeBinding
    lateinit var drawer_layout:DrawerLayout
    lateinit var nav_view:NavigationView
    lateinit var navHostFragment:NavHostFragment
    private val viewModel: HomeActivityViewModel by viewModels()

    /*val myToolbarImage: ImageView
        get() = findViewById(R.id.toolbar_profile_image)*/

    fun getToolbarMenuLayout(): ConstraintLayout {
        return findViewById(R.id.fragment_home_toolbar_menu_layout)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navigationView = this.findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView?.getHeaderView(0)
        val userName = headerView?.findViewById<TextView>(R.id.user_name_menu_header)
        val userEmail = headerView?.findViewById<TextView>(R.id.user_email_menu_header)
        val userImage = headerView?.findViewById<ImageView>(R.id.user_image_menu_header)



        /*val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val headerTextView: TextView = headerView.findViewById(R.id.user_name_menu_header)
        headerTextView.text = "Your Text Here"*/

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container_home) as NavHostFragment


        headerView!!.setOnClickListener {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            navHostFragment.findNavController().navigate(R.id.userProfileFragment)
        }
        setSupportActionBar(findViewById(R.id.toolbarHome))

        drawer_layout = findViewById(R.id.drawer_layout)
        nav_view = findViewById(R.id.nav_view)

        findViewById<ConstraintLayout>(R.id.fragment_home_toolbar_menu_layout).setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, findViewById(R.id.toolbarHome), R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        displayScreen(-1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        loadFragment()

        // Creating Channel to show notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "1",
                "Medicine Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        MainScope().launch(Dispatchers.IO) {
            val userQuery = fBase.getUserDataQuery()
            userQuery.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        viewModel.setUserData(
                            UserData(
                                it.child("name").value.toString(),
                                it.child("surname").value.toString(),
                                it.child("email").value.toString(),
                                it.child("profileImage").value.toString())
                        )
                    }

                    //Log.i("Name Updated: ", "${viewModel._userData.value!!.name}")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
        viewModel._userData.observe(this){
            MainScope().launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main)
                    {
                        userName!!.text = resources.getString(R.string.full_name,it.name,it.surname)
                        userEmail!!.text = it.email

                        /*binding.menuLayout.usernameTxt.text=requireActivity().getString(R.string.full_name,it.name,it.surname)
                        binding.menuLayout.profileImage.setImageBitmap(null)*/
                    }

                    if(it?.profileImage != null && it.profileImage!!.isNotEmpty() && it.profileImage!!.isNotBlank() && it.profileImage!="null")
                    {
                        //val bitmap = UtilityFunction.decodeBase64ToBitmap(it?.profileImage!!)

                        /*bitmap = getCircularBitmap(bitmap)*/
                        Log.i("Profile Image URL: ","${it.profileImage}")
                        withContext(Dispatchers.Main)
                        {
                            /*val parentView = binding.menuLayout.profileImage.parent as View
                            binding.menuLayout.profileImage.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            parentView.requestLayout()
                            binding.menuLayout.profileImage.setImageBitmap(bitmap)*/
                            Glide.with(this@HomeActivity)
                                .load(it.profileImage!!)
                                .into(userImage!!)
                            //userImage!!.setImageBitmap(bitmap)
                        }
                    }
                    else{
                        val drawableResourceId = resources.getIdentifier("profilepic", "drawable", packageName)
                        userImage!!.setImageResource(drawableResourceId)
                    }
                }
                catch (ex:Exception)
                {
                    Log.e("Home Activity","$ex")
                }

            }
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        return true
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun loadFragment()
    {
        appBarConfiguration = AppBarConfiguration.Builder(
            setOf(
                R.id.homeFragment,
                R.id.OCRFragment,
                R.id.searchFragment,
                R.id.addMedicineFragment,
                R.id.medicineStockFragment,
                R.id.userProfileFragment,
                R.id.aboutUsFragment,
                R.id.updateProfileImageFragment
            )
        ).build()

        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    fun displayScreen(id: Int){

        // val fragment =  when (id){

        when (id){
            R.id.nav_home -> {
                navHostFragment.findNavController().popBackStack(R.id.homeFragment,false)
//                Toast.makeText(this, "Clicked Home", Toast.LENGTH_SHORT).show()

            }

//            R.id.nav_photos -> {
//                Toast.makeText(this, "Clicked Photos", Toast.LENGTH_SHORT).show()
//            }
//
//            R.id.nav_movies -> {
//                Toast.makeText(this, "Clicked Movies", Toast.LENGTH_SHORT).show()
//            }

            R.id.nav_notifications -> {
                navController.navigate(R.id.notificationFragment)
            }

//            R.id.nav_settings -> {
//                Toast.makeText(this, "Clicked Settings", Toast.LENGTH_SHORT).show()
//            }

            R.id.nav_aboutUs -> {
                navHostFragment.findNavController().navigate(R.id.aboutUsFragment)
                //Toast.makeText(this, "Clicked About Us", Toast.LENGTH_SHORT).show()
            }

//            R.id.nav_privacyPolicy -> {
//                Toast.makeText(this, "Clicked Privacy Policy", Toast.LENGTH_SHORT).show()
//            }

            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()
                Intent(this, MainActivity::class.java).apply {
                    startActivity(this)
                }
                this.finish()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayScreen(item.itemId)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}