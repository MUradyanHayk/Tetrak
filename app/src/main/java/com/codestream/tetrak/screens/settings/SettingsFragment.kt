package com.codestream.tetrak.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentSettingsBinding
import com.codestream.tetrak.model.DeletedNoteModel
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.utils.AppConstants
import com.codestream.tetrak.utils.AppSettings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private var latestDeletedNotes: List<DeletedNoteModel> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        bindCurrentSettings()
        setupThemePicker()
        setupLanguagePicker()
        setupPremiumControls()
        setupDeletedNotesControls()
        animateIntro()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun bindCurrentSettings() = with(binding) {
        themeGroup.check(
            when (viewModel.theme()) {
                AppSettings.THEME_LIGHT -> R.id.theme_light
                AppSettings.THEME_DARK -> R.id.theme_dark
                else -> R.id.theme_system
            }
        )
        languageGroup.check(
            when (viewModel.language()) {
                AppSettings.LANGUAGE_ENGLISH -> R.id.language_english
                AppSettings.LANGUAGE_ARMENIAN -> R.id.language_armenian
                AppSettings.LANGUAGE_RUSSIAN -> R.id.language_russian
                AppSettings.LANGUAGE_ARABIC -> R.id.language_arabic
                AppSettings.LANGUAGE_PERSIAN -> R.id.language_persian
                else -> R.id.language_system
            }
        )
    }

    private fun setupThemePicker() {
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.theme_light -> AppSettings.THEME_LIGHT
                R.id.theme_dark -> AppSettings.THEME_DARK
                else -> AppSettings.THEME_SYSTEM
            }
            viewModel.updateTheme(mode)
        }
    }

    private fun setupLanguagePicker() {
        binding.languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val language = when (checkedId) {
                R.id.language_english -> AppSettings.LANGUAGE_ENGLISH
                R.id.language_armenian -> AppSettings.LANGUAGE_ARMENIAN
                R.id.language_russian -> AppSettings.LANGUAGE_RUSSIAN
                R.id.language_arabic -> AppSettings.LANGUAGE_ARABIC
                R.id.language_persian -> AppSettings.LANGUAGE_PERSIAN
                else -> AppSettings.LANGUAGE_SYSTEM
            }
            viewModel.updateLanguage(language)
        }
    }

    private fun setupPremiumControls() = with(binding) {
        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            premiumCard.visibility = View.GONE
            return@with
        }

        refreshPremiumUi()

        premiumUpgradeBtn.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70L).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
            }.start()
            findNavController().navigate(R.id.action_settingsFragment_to_premiumFragment)
        }

        premiumRestoreBtn.setOnClickListener {
            PremiumBillingManager.restorePurchases(requireContext()) { _, message ->
                refreshPremiumUi()
                Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
            }
        }

        premiumDebugSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPremiumForDevelopment(isChecked)
            refreshPremiumUi()
            Snackbar.make(
                root,
                if (isChecked) R.string.premium_dev_enabled else R.string.premium_dev_disabled,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun refreshPremiumUi() = with(binding) {
        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            premiumCard.visibility = View.GONE
            return@with
        }
        val premium = viewModel.isPremium()
        premiumStatus.setText(if (premium) R.string.premium_status_active else R.string.premium_status_free)
        premiumDebugSwitch.isChecked = premium
        adsCard.alpha = if (premium) 0.55f else 1f
        premiumUpgradeBtn.isEnabled = true
        premiumUpgradeBtn.setText(if (premium) R.string.premium_manage_plan else R.string.premium_view_plans)
    }

    private fun animateIntro() = with(binding) {
        themeCard.translationY = 32f
        languageCard.translationY = 32f
        if (AppConstants.HAS_PREMIUM_FEATURES) premiumCard.translationY = 32f
        adsCard.translationY = 32f
        themeCard.alpha = 0f
        languageCard.alpha = 0f
        if (AppConstants.HAS_PREMIUM_FEATURES) premiumCard.alpha = 0f
        adsCard.alpha = 0f
        themeCard.animate().translationY(0f).alpha(1f).setDuration(280L).start()
        languageCard.animate().translationY(0f).alpha(1f).setStartDelay(90L).setDuration(280L).start()
        if (AppConstants.HAS_PREMIUM_FEATURES) {
            premiumCard.animate().translationY(0f).alpha(1f).setStartDelay(180L).setDuration(280L).start()
        } else {
            premiumCard.visibility = View.GONE
        }
        deletedNotesCard.translationY = 32f
        deletedNotesCard.alpha = 0f
        adsCard.animate().translationY(0f).alpha(if (viewModel.isPremium()) 0.55f else 1f).setStartDelay(if (AppConstants.HAS_PREMIUM_FEATURES) 270L else 180L).setDuration(280L).start()
        deletedNotesCard.animate().translationY(0f).alpha(1f).setStartDelay(if (AppConstants.HAS_PREMIUM_FEATURES) 360L else 270L).setDuration(280L).start()
    }

    private fun setupDeletedNotesControls() = with(binding) {
        deletedNotesRetention.text = getString(R.string.deleted_notes_retention, AppConstants.DELETED_NOTES_RETENTION_DAYS)
        deletedNotesOpenBtn.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70L).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
            }.start()
            findNavController().navigate(R.id.action_settingsFragment_to_deletedNotesFragment)
        }
        viewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
            latestDeletedNotes = notes
            deletedNotesCount.text = resources.getQuantityString(
                R.plurals.deleted_notes_count,
                notes.size,
                notes.size
            )
            deletedNotesOpenBtn.isEnabled = true
        }
    }

    private fun showDeletedNotesDialog() {
        if (latestDeletedNotes.isEmpty()) {
            Snackbar.make(binding.root, R.string.deleted_notes_empty, Snackbar.LENGTH_SHORT).show()
            return
        }
        val dateFormat = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
        val items = latestDeletedNotes.map { note ->
            val deleted = dateFormat.format(Date(note.deletedAt))
            "${note.title.ifBlank { getString(R.string.untitled_note) }}\n${getString(R.string.deleted_on_value, deleted)}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.deleted_notes)
            .setMessage(getString(R.string.deleted_notes_dialog_message, AppConstants.DELETED_NOTES_RETENTION_DAYS))
            .setItems(items) { _, index -> showDeletedNoteActionDialog(latestDeletedNotes[index]) }
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun showDeletedNoteActionDialog(note: DeletedNoteModel) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
        val message = buildString {
            appendLine(note.description.ifBlank { getString(R.string.no_description) })
            appendLine()
            append(getString(R.string.deleted_note_expires_value, dateFormat.format(Date(note.expiresAt))))
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(note.title.ifBlank { getString(R.string.untitled_note) })
            .setMessage(message)
            .setNegativeButton(R.string.delete_forever) { _, _ ->
                viewModel.permanentlyDeleteDeletedNote(note) {
                    Snackbar.make(binding.root, R.string.deleted_note_removed, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setPositiveButton(R.string.restore) { _, _ ->
                viewModel.restoreDeletedNote(note) {
                    Snackbar.make(binding.root, R.string.deleted_note_restored, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
