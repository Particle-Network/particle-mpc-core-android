package com.particle.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.SPUtils
import com.particle.auth.AuthCore
import com.particle.auth.data.AuthCoreServiceCallback
import com.particle.auth.data.req.CodeReq
import com.particle.base.data.ErrorInfo
import com.particle.base.model.UserInfo
import com.particle.sample.databinding.FragmentLoginBinding
import com.particle.sample.utils.CountdownUtils
import com.particle.sample.utils.toast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private var countDownJob: Job? = null

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
        private const val REGEX_EMAIL =
            "^([a-z0-9A-Z]+[-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDetach() {
        countDownJob?.cancel()
        super.onDetach()
    }

    private fun initView() {
        binding.loginTypeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.emailLoginLayout.visibility = View.GONE
                binding.jwtLoginLayout.visibility = View.VISIBLE
                binding.loginTypeSwitch.text = "JWT"
            } else {
                binding.emailLoginLayout.visibility = View.VISIBLE
                binding.jwtLoginLayout.visibility = View.GONE
                binding.loginTypeSwitch.text = "Email"
            }
        }
        binding.dynamicTheme.isChecked = SPUtils.getInstance().getBoolean("isDynamicColor", true)
        binding.dynamicTheme.setOnCheckedChangeListener { buttonView, isChecked ->
            SPUtils.getInstance().put("isDynamicColor", isChecked)
            requireActivity().recreate()
        }

        binding.sendCode.setOnClickListener {
            sendCode()
        }

        binding.loginEmail.setOnClickListener {
            loginWithEmail()
        }

        binding.loginJwt.setOnClickListener {
            loginWithJWT()
        }

        SPUtils.getInstance().getString("login_email").let {
            binding.email.setText(it)
        }
        SPUtils.getInstance().getString("login_jwt").let {
            binding.jwt.setText(it)
        }
    }

    private fun sendCode() {
        val email = binding.email.text!!.trim().toString()
        if (Pattern.matches(REGEX_EMAIL, email)) {
            binding.sendCode.isEnabled = false
            lifecycleScope.launch {
                try {
                    AuthCore.sendCode(CodeReq(email))
                    toast("Send code success")
                    countDownJob = CountdownUtils.countDownCoroutines(60, lifecycleScope, onTick = { second ->
                        binding.sendCode.text = "Send Code(${second}s)"
                    }, onFinish = {
                        binding.sendCode.isEnabled = true
                        binding.sendCode.text = "Send Code"
                    })
                } catch (e: Exception) {
                    toast(e.message ?: "Please try again later")
                    binding.sendCode.isEnabled = true
                    binding.sendCode.text = "Send Code"
                }
            }
        } else {
            toast("Please enter the correct email address")
        }
    }

    private fun loginWithEmail() {
        val email = binding.email.text!!.trim().toString()
        val code = binding.code.text!!.trim().toString()
        if (!Pattern.matches(REGEX_EMAIL, email)) {
            toast("Please enter the correct email address")
            return
        }
        if (code.length != 6) {
            toast("Please enter the correct code")
            return
        }

        binding.loginLoading.visibility = View.VISIBLE
        binding.loginEmail.visibility = View.INVISIBLE


    }

    private fun loginWithJWT() {
        val jwt = binding.jwt.text!!.trim().toString()

        if (jwt.isEmpty()) {
            toast("Please enter the correct jwt")
            return
        }

        binding.loginLoading.visibility = View.VISIBLE
        binding.loginJwt.visibility = View.INVISIBLE

        AuthCore.connect(jwt, object : AuthCoreServiceCallback<UserInfo> {
            override fun success(output: UserInfo) {
                SPUtils.getInstance().put("login_jwt", jwt)
                toast("Connect Success")
                replaceWithMain()
            }

            override fun failure(errMsg: ErrorInfo) {
                toast("Connect Error: ${errMsg.message}")
                binding.loginLoading.visibility = View.GONE
                binding.loginJwt.visibility = View.VISIBLE
            }
        })
    }

    private fun replaceWithMain() {
        (requireActivity() as MainActivity).replaceWithMain()
    }
}