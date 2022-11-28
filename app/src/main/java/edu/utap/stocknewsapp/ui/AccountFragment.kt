package edu.utap.stocknewsapp.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import edu.utap.stocknewsapp.MainActivity
import edu.utap.stocknewsapp.R
import edu.utap.stocknewsapp.databinding.FragmentAccountBinding
import edu.utap.stocknewsapp.usermetadata.AuthInit

class AccountFragment: Fragment(R.layout.fragment_account) {

    companion object {
        private const val titleKey = "Account Setting"
        fun newInstance(title: String): AccountFragment {
            val frag = AccountFragment()
            val bundle = Bundle()
            // XXX set the fragment's arguments
            bundle.putString(titleKey, title)
            frag.arguments = bundle
            return frag
        }
    }

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
                    Toast.makeText(context,"Username must contain 4 to 30 characters",
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
                    Toast.makeText( context, "Password must contain 6 to 30 characters",
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
            viewModel.loadUserInfo()
        }

    private fun initLogInOut() {
        binding.logInOutBut.setOnClickListener {
            viewModel.signOut()
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
