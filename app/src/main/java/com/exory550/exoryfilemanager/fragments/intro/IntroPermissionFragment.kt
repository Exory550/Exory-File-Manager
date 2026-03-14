package com.exory550.exoryfilemanager.fragments.intro

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.exory550.exoryfilemanager.R
import com.exory550.exoryfilemanager.databinding.FragmentIntroPermissionBinding

class IntroPermissionFragment : Fragment() {

    private var _binding: FragmentIntroPermissionBinding? = null
    private val binding get() = _binding!!

    private lateinit var ivIcon: ImageView
    private lateinit var ivStorage: ImageView
    private lateinit var ivManageStorage: ImageView
    private lateinit var ivNotification: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvStorageStatus: TextView
    private lateinit var tvManageStorageStatus: TextView
    private lateinit var tvNotificationStatus: TextView
    private lateinit var btnGrantStorage: Button
    private lateinit var btnGrantManageStorage: Button
    private lateinit var btnGrantNotification: Button
    private lateinit var btnContinue: Button

    private var storageGranted = false
    private var manageStorageGranted = false
    private var notificationGranted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        setupAnimations()
        checkPermissions()
        setupClickListeners()
    }

    private fun initViews() {
        ivIcon = binding.ivIcon
        ivStorage = binding.ivStorage
        ivManageStorage = binding.ivManageStorage
        ivNotification = binding.ivNotification
        tvTitle = binding.tvTitle
        tvDescription = binding.tvDescription
        tvStorageStatus = binding.tvStorageStatus
        tvManageStorageStatus = binding.tvManageStorageStatus
        tvNotificationStatus = binding.tvNotificationStatus
        btnGrantStorage = binding.btnGrantStorage
        btnGrantManageStorage = binding.btnGrantManageStorage
        btnGrantNotification = binding.btnGrantNotification
        btnContinue = binding.btnContinue

        tvTitle.text = getString(R.string.intro_permission_title)
        tvDescription.text = getString(R.string.intro_permission_description)

        btnContinue.isEnabled = false
    }

    private fun setupAnimations() {
        val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        ivIcon.startAnimation(pulseAnimation)

        val floatAnimation = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.floating)
        ivStorage.startAnimation(floatAnimation)
        ivManageStorage.startAnimation(floatAnimation)
        ivNotification.startAnimation(floatAnimation)

        val scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivStorage,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f)
        )
        scaleAnimator.duration = 2000
        scaleAnimator.repeatCount = ObjectAnimator.INFINITE
        scaleAnimator.start()

        val rotateAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivManageStorage,
            PropertyValuesHolder.ofFloat("rotation", 0f, 10f, -10f, 0f)
        )
        rotateAnimator.duration = 3000
        rotateAnimator.repeatCount = ObjectAnimator.INFINITE
        rotateAnimator.start()

        val bounceAnimator = ObjectAnimator.ofPropertyValuesHolder(
            ivNotification,
            PropertyValuesHolder.ofFloat("translationY", 0f, -20f, 0f)
        )
        bounceAnimator.duration = 1500
        bounceAnimator.repeatCount = ObjectAnimator.INFINITE
        bounceAnimator.start()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            updateNotificationStatus()
        } else {
            notificationGranted = true
            tvNotificationStatus.text = getString(R.string.permission_not_required)
            btnGrantNotification.visibility = View.GONE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manageStorageGranted = android.os.Environment.isExternalStorageManager()
            updateManageStorageStatus()
        } else {
            manageStorageGranted = true
            tvManageStorageStatus.text = getString(R.string.permission_not_required)
            btnGrantManageStorage.visibility = View.GONE
        }

        storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        updateStorageStatus()

        updateContinueButton()
    }

    private fun setupClickListeners() {
        btnGrantStorage.setOnClickListener {
            requestPermissions(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    )
                } else {
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                },
                REQUEST_STORAGE
            )
        }

        btnGrantManageStorage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:${requireContext().packageName}")
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE)
            }
        }

        btnGrantNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION
                )
            }
        }

        btnContinue.setOnClickListener {
            (activity as? com.exory550.exoryfilemanager.activities.IntroActivity)?.completeIntro()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_STORAGE -> {
                storageGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                updateStorageStatus()
            }
            REQUEST_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationGranted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
                    updateNotificationStatus()
                }
            }
        }
        updateContinueButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manageStorageGranted = android.os.Environment.isExternalStorageManager()
                updateManageStorageStatus()
                updateContinueButton()
            }
        }
    }

    private fun updateStorageStatus() {
        if (storageGranted) {
            tvStorageStatus.text = getString(R.string.permission_granted)
            tvStorageStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_color))
            btnGrantStorage.visibility = View.GONE
        } else {
            tvStorageStatus.text = getString(R.string.permission_required)
            tvStorageStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_color))
            btnGrantStorage.visibility = View.VISIBLE
        }
    }

    private fun updateManageStorageStatus() {
        if (manageStorageGranted) {
            tvManageStorageStatus.text = getString(R.string.permission_granted)
            tvManageStorageStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_color))
            btnGrantManageStorage.visibility = View.GONE
        } else {
            tvManageStorageStatus.text = getString(R.string.permission_required)
            tvManageStorageStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_color))
            btnGrantManageStorage.visibility = View.VISIBLE
        }
    }

    private fun updateNotificationStatus() {
        if (notificationGranted) {
            tvNotificationStatus.text = getString(R.string.permission_granted)
            tvNotificationStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_color))
            btnGrantNotification.visibility = View.GONE
        } else {
            tvNotificationStatus.text = getString(R.string.permission_required)
            tvNotificationStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_color))
            btnGrantNotification.visibility = View.VISIBLE
        }
    }

    private fun updateContinueButton() {
        btnContinue.isEnabled = storageGranted && manageStorageGranted && notificationGranted
        if (btnContinue.isEnabled) {
            btnContinue.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction {
                    btnContinue.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_STORAGE = 1001
        private const val REQUEST_MANAGE_STORAGE = 1002
        private const val REQUEST_NOTIFICATION = 1003

        fun newInstance(): IntroPermissionFragment {
            return IntroPermissionFragment()
        }
    }
}
