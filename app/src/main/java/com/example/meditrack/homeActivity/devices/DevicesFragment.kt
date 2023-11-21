package com.example.meditrack.homeActivity.devices

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meditrack.R
import com.example.meditrack.adapter.RecyclerViewAdapter
import com.example.meditrack.dataModel.dataClasses.UserSessionData2
import com.example.meditrack.dataModel.dataClasses.UserSessionData2.Companion.toUserSessionData
import com.example.meditrack.dataModel.enumClasses.others.SessionStatus
import com.example.meditrack.databinding.FragmentDevicesBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.firebase.FirestorePaginationManager
import com.example.meditrack.homeActivity.HomeActivity
import com.example.meditrack.mainActivity.MainActivity
import com.example.meditrack.userSession.SessionSharedPreferencesManager
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange

class DevicesFragment : Fragment() {

    companion object {
        fun newInstance() = DevicesFragment()
    }

    private lateinit var viewModel: DevicesViewModel
    private lateinit var binding: FragmentDevicesBinding
    private lateinit var progressDialog: CustomProgressDialog
    private lateinit var firestorePaginationManager: FirestorePaginationManager
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var rowsArrayList = ArrayList<UserSessionData2?>()
    private lateinit var sessionCollectionRef:CollectionReference
    private lateinit var currectSessionID : String
    private var currentSessionData: UserSessionData2?=null
    private var isLoading = false
    private val tAG = "DevicesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_devices, container, false)
        binding = FragmentDevicesBinding.bind(view)
        viewModel = ViewModelProvider(this)[DevicesViewModel::class.java]
        progressDialog = CustomProgressDialog(requireContext())
        firestorePaginationManager = FirestorePaginationManager()
        currectSessionID = SessionSharedPreferencesManager.fetchSessionId(requireContext())!!
        sessionCollectionRef = FBase.getUsersSessionsDataCollection()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            sessionCollectionRef.document(currectSessionID).get().addOnSuccessListener {
                try{
                    if(it.data==null)
                    {
                        return@addOnSuccessListener
                    }
                    currentSessionData = (it.data)!!.toUserSessionData(it.id)
                    currentSessionData!!.apply {
                        txtDeviceName.text = deviceName
                        txtLoginTimeStamp.text = loginTimestamp
                        if(status!=SessionStatus.LOGGED_IN){
                            btnSignOut.setTextAppearance(R.style.DevicesFragmentSignOut)
                            btnSignOut.isEnabled=false
                        }
                        txtDeviceVersion.text = "${osVersion} (${apiLevel})"
                        val locationList = mutableListOf<String>()
                        if(country!=null && country!="" && country!="null"){
                            locationList.add(0,country)
                        }
                        if(state!=null && state!="" && state!="null"){
                            locationList.add(0,state)
                        }
                        if(city!=null && city!="" && city!="null"){
                            locationList.add(0,city)
                        }
                        if(area!=null && area!="" && area!="null"){
                            locationList.add(0,area)
                        }
                        txtLocation.text = locationList.joinToString(", ")
                    }

                }
                catch (ex:java.lang.Exception){
                    ex.printStackTrace()
                }

            }.addOnFailureListener {}
            populateData(firestorePaginationManager)
            sessionCollectionRef.addSnapshotListener { snapshot, e ->

                if (e != null) {
                    // Handle errors
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (change in snapshot.documentChanges) {
                        when (change.type) {
                            // Document added
                            DocumentChange.Type.ADDED -> {
                                val addedDocument = change.document
                                val addedData = addedDocument.data
                                if(rowsArrayList.isEmpty()){
                                    return@addSnapshotListener
                                }
                                rowsArrayList.add(0,addedData.toUserSessionData(addedDocument.id))
                                recyclerViewAdapter.notifyItemInserted(0)
                                return@addSnapshotListener
                                // Handle added document
                            }
                            // Document modified
                            DocumentChange.Type.MODIFIED -> {
                                val modifiedDocument = change.document
                                val modifiedData = modifiedDocument.data
                                if(rowsArrayList.isEmpty()){
                                    return@addSnapshotListener
                                }
                                for (i in 0 until rowsArrayList.size)
                                {
                                    if(rowsArrayList[i]!!.sessionId==modifiedDocument.id)
                                    {
                                        rowsArrayList[i]=modifiedData.toUserSessionData(modifiedDocument.id)
                                        recyclerViewAdapter.notifyItemChanged(i)
                                        return@addSnapshotListener
                                    }
                                }

                                // Handle modified document
                            }
                            // Document removed
                            DocumentChange.Type.REMOVED -> {
                                val removedDocument = change.document
                                //val removedData = removedDocument.data
                                // Handle removed document
                                if(rowsArrayList.isEmpty()){
                                    return@addSnapshotListener
                                }
                                for (i in 0 until rowsArrayList.size)
                                {
                                    if(rowsArrayList[i]!!.sessionId==removedDocument.id)
                                    {
                                        rowsArrayList.removeAt(i)
                                        recyclerViewAdapter.notifyItemRemoved(i)
                                        return@addSnapshotListener
                                    }
                                }
                            }
                        }
                    }
                }
            }



            btnSignOut.setOnClickListener {
                showSignOutConfirmationDialog()
                /*val sessionDocRef = FBase.getUsersSessionsDataCollection()
                    .document(currectSessionID)

                val updates = mapOf(
                    "status" to SessionStatus.LOGGED_OUT.name,
                    "logoutTimestamp" to com.example.meditrack.userSession.TimeUtils.getLogoutTimestamp()
                )

                sessionDocRef.update(updates)
                    .addOnSuccessListener {
                        FBase.getFireBaseAuth().signOut()
                        SessionSharedPreferencesManager.deleteSharedPreferences(requireContext())
                        Intent(requireContext(), MainActivity::class.java).apply {
                            startActivity(this)
                        }
                        requireActivity().finish()
                    }
                    .addOnFailureListener {

                    }*/
            }
        }
    }


    private fun showSignOutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to sign out your account?")

        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            // Handle deletion logic here
            val sessionDocRef = FBase.getUsersSessionsDataCollection()
                .document(currectSessionID)

            val updates = mapOf(
                "status" to SessionStatus.LOGGED_OUT.name,
                "logoutTimestamp" to com.example.meditrack.userSession.TimeUtils.getLogoutTimestamp()
            )
            FBase.getFireBaseAuth().signOut()
            SessionSharedPreferencesManager.deleteSharedPreferences(requireContext())
            sessionDocRef.update(updates)
                .addOnSuccessListener {
                    Intent(requireContext(), MainActivity::class.java).apply {
                        startActivity(this)
                    }
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    Intent(requireContext(), MainActivity::class.java).apply {
                        startActivity(this)
                    }
                    requireActivity().finish()
                }
        }

        builder.setNegativeButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showSignOutConfirmationDialogOther(position:Int) {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure you want to sign out your account?")

        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            // Handle deletion logic here
            try{
                val sessionDocRef = FBase.getUsersSessionsDataCollection()
                    .document(rowsArrayList[position]!!.sessionId)

                val updates = mapOf(
                    "status" to SessionStatus.LOGGED_OUT.name,
                    "logoutTimestamp" to com.example.meditrack.userSession.TimeUtils.getLogoutTimestamp()
                )

                sessionDocRef.update(updates)
                    .addOnSuccessListener {
                        rowsArrayList[position]!!.status = SessionStatus.LOGGED_OUT
                        recyclerViewAdapter.notifyItemChanged(position)
                    }
                    .addOnFailureListener {

                    }
            }
            catch (ex:Exception){
                ex.printStackTrace()
            }
        }

        builder.setNegativeButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun populateData(firestorePaginationManager:FirestorePaginationManager) {
        firestorePaginationManager.fetchNextBatch(currectSessionID,
            onSuccess = { dataList ->
                rowsArrayList.addAll(dataList)
                initAdapter()
                initScrollListener()
            },
            onFailure = { exception ->
            }
        )
    }

    private val signOutClickListener = object : RecyclerViewAdapter.SignOutClickListener{
        override fun onClick(position:Int) {
            showSignOutConfirmationDialogOther(position)
        }

    }

    private fun initAdapter() {
        recyclerViewAdapter = RecyclerViewAdapter(rowsArrayList,signOutClickListener)
        binding.rvDevices.adapter = recyclerViewAdapter
    }

    private fun initScrollListener() {
        binding.rvDevices.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size - 1) {
                        //bottom of list!

                        firestorePaginationManager.fetchNextBatch(currectSessionID,
                            onSuccess = { dataList ->

                                if(dataList.isNotEmpty()){
                                    Log.i(tAG,"$dataList")
                                    isLoading = true
                                    loadMore(dataList)
                                }
                            },
                            onFailure = { exception ->
                            }
                        )

                    }
                }
            }
        })
    }

    private fun loadMore(dataList:List<UserSessionData2>) {

        rowsArrayList.addAll(dataList)
        rowsArrayList.add(null)
        recyclerViewAdapter.notifyItemInserted(rowsArrayList.size - 1)

        binding.rvDevices.postDelayed({
            rowsArrayList.removeAt(rowsArrayList.size - 1)
            val scrollPosition = rowsArrayList.size
            recyclerViewAdapter.notifyItemRemoved(scrollPosition)
            /*val rowsArrayListSize = rowsArrayList.size
            for (i in dataList.indices)
            {
                recyclerViewAdapter.notifyItemInserted(rowsArrayListSize+i)
            }*/
            recyclerViewAdapter.notifyDataSetChanged()
            isLoading = false
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.setNavigationDrawerSelection(R.id.nav_devices)
    }

    override fun onPause() {
        super.onPause()
        (activity as? HomeActivity)?.clearNavigationDrawerSelection(R.id.nav_devices)
    }

}