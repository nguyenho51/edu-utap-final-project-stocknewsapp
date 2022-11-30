package edu.utap.stocknewsapp.ui


import android.os.Bundle
import android.util.Log

import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import edu.utap.stocknewsapp.MainActivity
import edu.utap.stocknewsapp.R
import edu.utap.stocknewsapp.databinding.FragmentAccountBinding
import edu.utap.stocknewsapp.usermetadata.AuthInit

class AccountFragment: Fragment(R.layout.fragment_account) {

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun displayAndChangeName() {
        binding.changeNameET.visibility = INVISIBLE
        viewModel.getDisplayName().observe(viewLifecycleOwner) {
            binding.userName.text = viewModel.getDisplayName().value
        }
        binding.changeSaveNameBut.setOnClickListener {
            if (binding.changeSaveNameBut.text == "Change") {
                binding.changeSaveNameBut.text = "Save"
                binding.userName.visibility = INVISIBLE
                binding.changeNameET.visibility = VISIBLE
                binding.cancelNameBut.visibility = VISIBLE
            } else {
                val nameET = binding.changeNameET.text.toString()
                if (nameET.length < 4) {
                    Toast.makeText(context,"Username must contain 4 to 14 characters",
                        Toast.LENGTH_SHORT).show()
                } else {
                    (requireActivity() as MainActivity).hideKeyboard()
                    binding.changeSaveNameBut.text = "Change"
                    binding.userName.visibility = VISIBLE
                    binding.cancelNameBut.visibility = INVISIBLE
                    binding.changeNameET.visibility = INVISIBLE
                    AuthInit.setDisplayName(nameET,viewModel)
                    binding.changeNameET.text.clear()
                }
            }
        }
        binding.cancelNameBut.setOnClickListener {
            (requireActivity() as MainActivity).hideKeyboard()
            binding.changeSaveNameBut.text = "Change"
            binding.userName.visibility = VISIBLE
            binding.cancelNameBut.visibility = INVISIBLE
            binding.changeNameET.visibility = INVISIBLE
            binding.changeNameET.text.clear()
        }
    }
    private fun displayAndChangePassword() {
        binding.changePasswordET.visibility = INVISIBLE
        binding.changeSavePwBut.setOnClickListener {
            if (binding.changeSavePwBut.text == "Change") {
                binding.changeSavePwBut.text = "Save"
                binding.userPW.visibility = INVISIBLE
                binding.changePasswordET.visibility = VISIBLE
                binding.cancelPwBut.visibility = VISIBLE
            } else {
                val passwordET = binding.changePasswordET.text.toString()
                if (passwordET.length < 5) {
                    Toast.makeText( context, "Password must contain 6 to 20 characters",
                        Toast.LENGTH_SHORT).show()
                } else {
                    binding.changeSavePwBut.text = "Change"
                    (requireActivity() as MainActivity).hideKeyboard()
                    binding.userPW.visibility = VISIBLE
                    binding.changePasswordET.visibility = INVISIBLE
                    binding.cancelPwBut.visibility = INVISIBLE
                    AuthInit.changeUserPassword(passwordET)
                    binding.changePasswordET.text.clear()
                }
            }
        }
        binding.cancelPwBut.setOnClickListener {
            (requireActivity() as MainActivity).hideKeyboard()
            binding.changeSavePwBut.text = "Change"
            binding.cancelPwBut.visibility = INVISIBLE
            binding.userPW.visibility = VISIBLE
            binding.changePasswordET.visibility = INVISIBLE
            binding.changePasswordET.text.clear()
        }
    }
    private fun displayEmail() {
        viewModel.getDisplayEmail().observe(viewLifecycleOwner) {
            binding.userEmail.text = viewModel.getDisplayEmail().value
        }
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                viewModel.setIsLoggedIn(true)
                viewModel.loadUserInfo()
            }
        }

    private fun initLogInOut() {
        viewModel.observeSignIn().observe(viewLifecycleOwner) {
            val isSignIn = viewModel.observeSignIn().value
            Log.d("XXX isSignIn", "Current State: $isSignIn")
            if (isSignIn == true) {
                binding.logOutBut.visibility = VISIBLE
                binding.logInBut.visibility = INVISIBLE
            } else {
                binding.logOutBut.visibility = INVISIBLE
                binding.logInBut.visibility = VISIBLE
            }
        }
        binding.logOutBut.setOnClickListener {
            viewModel.resetNameAndEmail()
            try {
                viewModel.signOut()
            } catch (e: java.lang.NullPointerException) {
                viewModel.setIsLoggedIn(false)

            }
        }
        binding.logInBut.setOnClickListener {
            //val isSignIn = viewModel.observeSignIn().value
            AuthInit(viewModel,signInLauncher)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)
        Log.d(javaClass.simpleName, "Account Frag Created")
        //Log.d("XXX", "Current State: ${viewModel.observeSignIn().value}")
        displayAndChangeName()
        displayAndChangePassword()
        displayEmail()
        initLogInOut()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
