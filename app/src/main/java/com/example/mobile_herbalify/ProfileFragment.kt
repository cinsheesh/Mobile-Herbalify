package com.example.mobile_herbalify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvNamaProfil = view.findViewById<TextView>(R.id.tvNamaProfil)
        val tvEmailProfil = view.findViewById<TextView>(R.id.tvEmailProfil)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnEditProfil = view.findViewById<LinearLayout>(R.id.btnMenuEditProfil)
        val btnGantiPassword = view.findViewById<LinearLayout>(R.id.btnMenuGantiPassword)

        val sharedPref = requireActivity().getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)
        val namaUser = sharedPref.getString("USER_NAME", "Pengguna")
        val emailUser = sharedPref.getString("USER_EMAIL", "email@default.com")

        tvNamaProfil.text = namaUser
        tvEmailProfil.text = emailUser

        btnEditProfil.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🚀 INI YANG TADI BELUM DIGANTI: Sekarang sudah mengarah ke ChangePasswordFragment
        btnGantiPassword.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogout.setOnClickListener {
            val prefToClear = requireActivity().getSharedPreferences("HerbalifyPrefs", Context.MODE_PRIVATE)
            prefToClear.edit().clear().apply()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}