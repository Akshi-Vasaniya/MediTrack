package com.example.meditrack.homeActivity.userprofile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.meditrack.R
import com.example.meditrack.dataModel.dataClasses.UserData
import com.example.meditrack.databinding.FragmentUserProfileBinding
import com.example.meditrack.firebase.FBase
import com.example.meditrack.utility.ownDialogs.UserInfoUpdateDialog
import com.example.meditrack.utility.ownDialogs.CustomProgressDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileFragment : Fragment(), UserInfoUpdateDialog.CustomDialogListener {

    companion object {
        fun newInstance() = UserProfileFragment()
    }

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var progressDialog: CustomProgressDialog
    private var userProfileImgUrl:String = ""

    override fun onUpdateButtonClicked(text: String,fieldName:String) {
        // Perform your actions here with the received text
        // For example, update the user profile with the provided text
        val userRef = FBase.getUserReference().child(FBase.getCurrentUser()!!.uid)
        val updates: MutableMap<String, Any> = HashMap()
        updates[fieldName] = text
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                // The name has been updated successfully
                Toast.makeText(requireContext(),"Success",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle any errors that may occur
                Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
            }
    }

//    override fun onPrepareOptionsMenu(menu: Menu) {
//        for(i in 0 until menu.size()){
//            menu.getItem(i).isVisible = false
//        }
//
//        super.onPrepareOptionsMenu(menu)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        binding = FragmentUserProfileBinding.bind(view)

//        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        progressDialog= CustomProgressDialog(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            editNameButton.setOnClickListener {
                val userInfoUpdateDialog = UserInfoUpdateDialog(requireContext(), this@UserProfileFragment,fragmentUserProfileNameTextInputEditText.text.toString(),"name")
                userInfoUpdateDialog.show()
            }
            editSurnameButton.setOnClickListener {
                val userInfoUpdateDialog = UserInfoUpdateDialog(requireContext(), this@UserProfileFragment,fragmentUserProfileSurnameTextInputEditText.text.toString(),"surname")
                userInfoUpdateDialog.show()
            }
            userProfileImage.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("profileImageUrl", userProfileImgUrl)
                findNavController().navigate(R.id.updateProfileImageFragment,bundle)
            }
            userProfileImageFloatingBtn.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("profileImageUrl", userProfileImgUrl)
                findNavController().navigate(R.id.updateProfileImageFragment,bundle)
            }
        }


        MainScope().launch(Dispatchers.IO) {
            withContext(Dispatchers.Main)
            {
                progressDialog.start("Loading Details...")
            }
            val userQuery = FBase.getUserDataQuery()
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
                    progressDialog.stop()
                    //Log.i("Name Updated: ", "${viewModel._userData.value!!.name}")
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.stop()
                    TODO("Not yet implemented")
                }

            })
        }
        viewModel.userData.observe(viewLifecycleOwner){
            MainScope().launch(Dispatchers.IO) {
                try {
                    userProfileImgUrl = it.profileImage!!
                    withContext(Dispatchers.Main)
                    {
                        binding.fragmentUserProfileNameTextInputEditText.setText(it.name)
                        binding.fragmentUserProfileSurnameTextInputEditText.setText(it.surname)
                        binding.fragmentUserProfileEmailTextInputEditText.setText(it.email)
                    }

                    if(it?.profileImage != null && it.profileImage!!.isNotEmpty() && it.profileImage!!.isNotBlank() && it.profileImage!="null")
                    {
                        //val bitmap = UtilityFunction.decodeBase64ToBitmap(it?.profileImage!!)

                        withContext(Dispatchers.Main)
                        {
                            Glide.with(requireActivity())
                                .load(userProfileImgUrl)
                                .into(binding.fragmentUserProfileProfileImage)
                            //binding.fragmentUserProfileProfileImage.setImageBitmap(bitmap)
                        }
                    }
                    else{
                        //val drawableResourceId = resources.getIdentifier("profilepic", "drawable", requireContext().packageName)
                        val drawableResourceId = R.drawable.profilepic
                        binding.fragmentUserProfileProfileImage.setImageResource(drawableResourceId)
                    }
                }
                catch (ex:Exception)
                {
                    Log.e("User Profile Fragment","$ex")
                }

            }
        }
    }


}