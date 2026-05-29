package com.codestream.tetrak.screens.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codestream.tetrak.R
import com.codestream.tetrak.adapter.NoteColorAdapter
import com.codestream.tetrak.adapter.NoteHistoryAdapter
import com.codestream.tetrak.databinding.DialogColorPickerBinding
import com.codestream.tetrak.databinding.DialogNoteHistoryBinding
import com.codestream.tetrak.databinding.FragmentDetailBinding
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.premium.PremiumManager
import com.codestream.tetrak.util.NoteEditorHistory
import com.codestream.tetrak.util.NoteEditorSearch
import com.codestream.tetrak.utils.AppConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private var currentNote: NoteModel? = null
    private var selectedColor: Int = NoteModel.DEFAULT_NOTE_COLOR
    private var colorAdapter: NoteColorAdapter? = null
    private var isEditMode = false
    private var isSaving = false
    private var latestHistory: List<NoteHistoryModel> = emptyList()
    private var editorHistory: NoteEditorHistory? = null
    private var editorSearch: NoteEditorSearch? = null
    private var isGeneratingTitle = false

    private val colorOptions = listOf(
        Color.parseColor("#5B6CFF"),
        Color.parseColor("#FFB84D"),
        Color.parseColor("#24C6A1"),
        Color.parseColor("#FF6B8A"),
        Color.parseColor("#A162F7"),
        Color.parseColor("#4DB6FF"),
        Color.parseColor("#FF8A3D")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        currentNote = readNoteArgument()
        selectedColor = currentNote?.color ?: NoteModel.DEFAULT_NOTE_COLOR
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackHandling()
        setupToolbar()
        setupColorPicker()
        setupEditorTools()
        setupAiTitleGenerator()
        bindNote()
        observeHistory()
        animateIntro()
        setupClicks()
    }

    private fun readNoteArgument(): NoteModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("note", NoteModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("note") as? NoteModel
        }
    }

    private fun setupBackHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleExitRequest()
                }
            }
        )
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationOnClickListener { handleExitRequest() }
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    enterEditMode()
                    true
                }
                R.id.action_history -> {
                    showHistoryDialog()
                    true
                }
                else -> false
            }
        }
    }


    private fun setupAiTitleGenerator() {
        binding.titleInput.isEndIconVisible = AppConstants.HAS_PREMIUM_FEATURES
        if (!AppConstants.HAS_PREMIUM_FEATURES) return

        binding.titleInput.setEndIconOnClickListener {
            animateToolTap(binding.titleInput)
            generateTitleWithAi()
        }
    }

    private fun generateTitleWithAi() {
        if (isGeneratingTitle) return
        if (!requirePremiumForAiTitle()) return
        if (!isEditMode) enterEditMode()
        val description = binding.editDescription.text?.toString().orEmpty().trim()
        if (description.isBlank()) {
            Snackbar.make(binding.root, R.string.ai_title_description_required, Snackbar.LENGTH_SHORT).show()
            binding.editDescription.requestFocus()
            return
        }

        isGeneratingTitle = true
        binding.titleInput.isEndIconVisible = false
        binding.titleInput.helperText = getString(R.string.ai_title_generating)
        binding.titleInput.animate().scaleX(1.01f).scaleY(1.01f).setDuration(140L).withEndAction {
            binding.titleInput.animate().scaleX(1f).scaleY(1f).setDuration(160L).start()
        }.start()

        viewModel.generateTitle(
            description = description,
            onSuccess = { title ->
                isGeneratingTitle = false
                binding.titleInput.isEndIconVisible = true
                binding.titleInput.helperText = null
                if (title.isBlank()) {
                    Snackbar.make(binding.root, R.string.ai_title_generation_failed, Snackbar.LENGTH_SHORT).show()
                    return@generateTitle
                }
                binding.editTitle.setText(title)
                binding.editTitle.setSelection(binding.editTitle.text?.length ?: 0)
                binding.titleInput.error = null
                binding.titleInput.animate().translationX(6f).setDuration(55L).withEndAction {
                    binding.titleInput.animate().translationX(0f).setDuration(120L).start()
                }.start()
                Snackbar.make(binding.root, R.string.ai_title_generated, Snackbar.LENGTH_SHORT).show()
            },
            onError = { message ->
                isGeneratingTitle = false
                binding.titleInput.isEndIconVisible = true
                binding.titleInput.helperText = null
                Snackbar.make(
                    binding.root,
                    message.takeIf { it.isNotBlank() } ?: getString(R.string.ai_title_generation_failed),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun requirePremiumForAiTitle(): Boolean {
        if (!AppConstants.HAS_PREMIUM_FEATURES) return false
        val clickCount = PremiumManager.recordAiTitleClick(requireContext())
        if (PremiumManager.isPremium(requireContext())) {
            Snackbar.make(
                binding.root,
                getString(R.string.premium_ai_title_notice, clickCount),
                Snackbar.LENGTH_LONG
            ).show()
            return true
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.premium_required_title)
            .setMessage(getString(R.string.premium_ai_title_required_message, clickCount))
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.premium_restore) { _, _ ->
                PremiumBillingManager.restorePurchases(requireContext()) { _, message ->
                    if (isAdded) Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
            }
            .setPositiveButton(R.string.premium_upgrade) { _, _ ->
                findNavController().navigate(R.id.settingsFragment)
            }
            .show()
        return false
    }

    private fun setupColorPicker() {
        colorAdapter = NoteColorAdapter(
            presetColors = colorOptions,
            selectedColor = selectedColor,
            onColorSelected = { color ->
                selectedColor = color
                updateColorUi(animate = true)
            },
            onCustomColorClick = { showColorPickerDialog() }
        )
        binding.colorPicker.adapter = colorAdapter
    }

    private fun setupEditorTools() {
        editorHistory = NoteEditorHistory(
            fields = listOf(binding.editTitle, binding.editDescription),
            onStateChanged = { updateEditorToolState() }
        ).also { it.attach() }
        editorSearch = NoteEditorSearch(listOf(binding.editTitle, binding.editDescription))

        binding.toolCopyBtn.setOnClickListener {
            animateToolTap(it)
            copySelectionOrNoteText()
        }
        binding.toolUndoBtn.setOnClickListener {
            animateToolTap(it)
            editorHistory?.undo()
        }
        binding.toolRedoBtn.setOnClickListener {
            animateToolTap(it)
            editorHistory?.redo()
        }
        binding.toolSearchBtn.setOnClickListener {
            animateToolTap(it)
            toggleSearchPanel(show = binding.searchContainer.visibility != View.VISIBLE)
        }
        binding.searchNextBtn.setOnClickListener {
            animateToolTap(it)
            findNextSearchMatch()
        }
        binding.searchCloseBtn.setOnClickListener { toggleSearchPanel(show = false) }
        binding.searchInput.setOnEditorActionListener { _, _, _ ->
            findNextSearchMatch()
            true
        }
        updateEditorToolState()
    }

    private fun updateEditorToolState() {
        val history = editorHistory
        binding.toolUndoBtn.isEnabled = history?.canUndo() == true
        binding.toolRedoBtn.isEnabled = history?.canRedo() == true
    }

    private fun toggleSearchPanel(show: Boolean) = with(binding.searchContainer) {
        if (show) {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = -12f
            animate().alpha(1f).translationY(0f).setDuration(220L).start()
            binding.searchInput.post {
                binding.searchInput.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            animate().alpha(0f).translationY(-12f).setDuration(160L).withEndAction {
                visibility = View.GONE
                binding.searchInput.setText("")
                binding.searchInputLayout.error = null
            }.start()
        }
    }

    private fun findNextSearchMatch() {
        val found = editorSearch?.findNext(binding.searchInput.text?.toString().orEmpty()) == true
        binding.searchInputLayout.error = if (found) null else getString(R.string.search_no_results)
    }

    private fun copySelectionOrNoteText() {
        val fields = listOf(binding.editTitle, binding.editDescription)
        val focused = fields.firstOrNull { it.hasFocus() }
        val selectedText = focused?.let { field ->
            val start = minOf(field.selectionStart, field.selectionEnd).coerceAtLeast(0)
            val end = maxOf(field.selectionStart, field.selectionEnd).coerceAtLeast(0)
            if (end > start) field.text?.substring(start, end) else null
        }
        val textToCopy = selectedText ?: focused?.text?.toString()?.takeIf { it.isNotBlank() }
            ?: listOf(binding.editTitle.text?.toString().orEmpty(), binding.editDescription.text?.toString().orEmpty())
                .filter { it.isNotBlank() }
                .joinToString(separator = "\n\n")

        if (textToCopy.isBlank()) {
            Snackbar.make(binding.root, R.string.nothing_to_copy, Snackbar.LENGTH_SHORT).show()
            return
        }
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), textToCopy))
        Snackbar.make(binding.root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
    }

    private fun animateToolTap(view: View) {
        view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(70L).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
        }.start()
    }

    private fun observeHistory() {
        val noteId = currentNote?.id ?: return
        viewModel.getHistory(noteId).observe(viewLifecycleOwner) { history ->
            latestHistory = history
        }
    }

    private fun bindNote() = with(binding) {
        val note = currentNote ?: return@with
        selectedColor = note.color
        title.text = note.title
        description.text = note.description.ifBlank { getString(R.string.no_description) }
        editTitle.setText(note.title)
        editDescription.setText(note.description)
        editedLabel.visibility = if (note.edited) View.VISIBLE else View.GONE
        toolbar.menu.findItem(R.id.action_history)?.isVisible = note.edited
        colorAdapter?.select(selectedColor)
        updateColorUi(animate = false)
        editorHistory?.reset()
    }

    private fun updateColorUi(animate: Boolean) = with(binding) {
        detailCard.strokeColor = selectedColor
        noteIcon.setColorFilter(selectedColor)
        noteAccent.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(selectedColor)
        }
        if (animate) {
            detailCard.animate().scaleX(1.01f).scaleY(1.01f).setDuration(90L).withEndAction {
                detailCard.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
            }.start()
        }
    }

    private fun setupClicks() {
        binding.editTitle.doAfterTextChanged { binding.titleInput.error = null }
        binding.deleteBtn.setOnClickListener {
            currentNote?.let { note ->
                binding.deleteBtn.isEnabled = false
                viewModel.delete(note) { findNavController().navigateUp() }
            }
        }
        binding.cancelEditBtn.setOnClickListener { handleCancelEdit() }
        binding.saveEditBtn.setOnClickListener { saveEditedNote() }
    }

    private fun enterEditMode() {
        if (isEditMode) return
        isEditMode = true
        binding.toolbar.menu.findItem(R.id.action_edit)?.isVisible = false
        binding.title.visibility = View.GONE
        binding.description.visibility = View.GONE
        binding.deleteBtn.visibility = View.GONE
        binding.editContainer.visibility = View.VISIBLE
        binding.editorToolsCard.visibility = View.VISIBLE
        binding.cancelEditBtn.visibility = View.VISIBLE
        binding.saveEditBtn.visibility = View.VISIBLE
        binding.editContainer.alpha = 0f
        binding.editContainer.translationY = 24f
        binding.editContainer.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        binding.editorToolsCard.alpha = 0f
        binding.editorToolsCard.translationY = 16f
        binding.editorToolsCard.animate().alpha(1f).translationY(0f).setDuration(260L).setStartDelay(90L).start()
        binding.actionRow.animate().translationY(0f).alpha(1f).setDuration(220L).start()
        binding.saveEditBtn.scaleX = 0.96f
        binding.saveEditBtn.scaleY = 0.96f
        binding.saveEditBtn.animate().scaleX(1f).scaleY(1f).setDuration(180L).start()
        binding.editTitle.requestFocus()
    }

    private fun exitEditMode(resetFields: Boolean) {
        if (resetFields) {
            currentNote?.let { note ->
                selectedColor = note.color
                binding.editTitle.setText(note.title)
                binding.editDescription.setText(note.description)
                colorAdapter?.select(selectedColor)
                updateColorUi(animate = true)
            }
        }
        isEditMode = false
        binding.toolbar.menu.findItem(R.id.action_edit)?.isVisible = true
        binding.title.visibility = View.VISIBLE
        binding.description.visibility = View.VISIBLE
        binding.deleteBtn.visibility = View.VISIBLE
        binding.editContainer.visibility = View.GONE
        binding.editorToolsCard.visibility = View.GONE
        binding.cancelEditBtn.visibility = View.GONE
        binding.saveEditBtn.visibility = View.GONE
        binding.searchContainer.visibility = View.GONE
        binding.searchInput.setText("")
        binding.searchInputLayout.error = null
        editorHistory?.reset()
    }

    private fun handleCancelEdit() {
        if (!hasEditChanges()) {
            exitEditMode(resetFields = true)
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.discard_changes_title)
            .setMessage(R.string.discard_changes_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.discard) { _, _ -> exitEditMode(resetFields = true) }
            .show()
    }

    private fun handleExitRequest() {
        if (!isEditMode) {
            findNavController().navigateUp()
            return
        }
        if (!hasEditChanges()) {
            exitEditMode(resetFields = true)
            return
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.unsaved_changes_title)
            .setMessage(R.string.unsaved_changes_message)
            .setNegativeButton(R.string.discard) { _, _ -> findNavController().navigateUp() }
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (saveEditedNote(afterSave = { findNavController().navigateUp() })) dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun hasEditChanges(): Boolean {
        val note = currentNote ?: return false
        return binding.editTitle.text?.toString().orEmpty().trim() != note.title ||
            binding.editDescription.text?.toString().orEmpty().trim() != note.description ||
            selectedColor != note.color
    }

    private fun saveEditedNote(afterSave: (() -> Unit)? = null): Boolean {
        val note = currentNote ?: return false
        if (isSaving) return false
        val title = binding.editTitle.text?.toString().orEmpty().trim()
        val description = binding.editDescription.text?.toString().orEmpty().trim()
        if (title.isBlank()) {
            binding.titleInput.error = getString(R.string.title_required)
            binding.editTitle.requestFocus()
            return false
        }
        if (!hasEditChanges()) {
            exitEditMode(resetFields = false)
            return true
        }
        isSaving = true
        binding.saveEditBtn.isEnabled = false
        val updatedNote = note.copy(
            title = title,
            description = description,
            color = selectedColor,
            edited = true
        )
        viewModel.update(note, updatedNote) {
            currentNote = updatedNote.copy(updatedAt = System.currentTimeMillis(), edited = true)
            isSaving = false
            binding.saveEditBtn.isEnabled = true
            bindNote()
            exitEditMode(resetFields = false)
            afterSave?.invoke()
        }
        return true
    }

    private fun showHistoryDialog() {
        val dialogBinding = DialogNoteHistoryBinding.inflate(layoutInflater)
        val adapter = NoteHistoryAdapter()
        dialogBinding.historyList.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.historyList.adapter = adapter
        adapter.submitList(latestHistory)
        dialogBinding.historyEmpty.visibility = if (latestHistory.isEmpty()) View.VISIBLE else View.GONE
        dialogBinding.historyList.visibility = if (latestHistory.isEmpty()) View.GONE else View.VISIBLE

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_history)
            .setMessage(R.string.edit_history_subtitle)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun showColorPickerDialog() {
        val dialogBinding = DialogColorPickerBinding.inflate(layoutInflater)
        var dialogColor = selectedColor
        var isUpdatingHexFromPicker = false

        fun updatePreview(color: Int, animate: Boolean) = with(dialogBinding) {
            colorPreview.background = roundedColorDrawable(color, 16f)
            colorValue.text = color.toHexColor()
            selectedColorCard.strokeColor = color
            if (animate) {
                colorPreview.animate().scaleX(1.08f).scaleY(1.08f).setDuration(90L).withEndAction {
                    colorPreview.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
                }.start()
            }
        }

        dialogBinding.advancedColorPicker.setColor(selectedColor, animate = false)
        updatePreview(selectedColor, animate = false)
        dialogBinding.hexInput.setText(selectedColor.toHexColor().removePrefix("#"))

        dialogBinding.advancedColorPicker.setOnColorChangedListener { color ->
            dialogColor = color
            updatePreview(color, animate = true)
            isUpdatingHexFromPicker = true
            dialogBinding.hexInput.setText(color.toHexColor().removePrefix("#"))
            dialogBinding.hexInput.setSelection(dialogBinding.hexInput.text?.length ?: 0)
            dialogBinding.hexInputLayout.error = null
            isUpdatingHexFromPicker = false
        }

        dialogBinding.hexInput.doAfterTextChanged { editable ->
            if (isUpdatingHexFromPicker) return@doAfterTextChanged
            val typedColor = editable?.toString().orEmpty().toColorOrNull()
            if (typedColor != null) {
                dialogBinding.hexInputLayout.error = null
                dialogColor = typedColor
                dialogBinding.advancedColorPicker.setColor(typedColor, animate = true)
                updatePreview(typedColor, animate = true)
            } else if (!editable.isNullOrBlank()) {
                dialogBinding.hexInputLayout.error = getString(R.string.invalid_hex_color)
            } else {
                dialogBinding.hexInputLayout.error = null
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.custom_note_color)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.apply, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val typedColor = dialogBinding.hexInput.text?.toString().orEmpty().toColorOrNull()
                if (typedColor == null) {
                    dialogBinding.hexInputLayout.error = getString(R.string.invalid_hex_color)
                    return@setOnClickListener
                }
                selectedColor = dialogColor
                colorAdapter?.select(selectedColor)
                updateColorUi(animate = true)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun animateIntro() = with(binding.detailCard) {
        scaleX = 0.96f
        scaleY = 0.96f
        alpha = 0f
        animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(360L).start()
    }

    private fun roundedColorDrawable(color: Int, radiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * resources.displayMetrics.density
            setColor(color)
        }
    }

    private fun Int.toHexColor(): String = String.format("#%06X", 0xFFFFFF and this)

    private fun String.toColorOrNull(): Int? {
        val normalized = trim().removePrefix("#")
        if (!Regex("^[0-9A-Fa-f]{6}$").matches(normalized)) return null
        return runCatching { Color.parseColor("#$normalized") }.getOrNull()
    }

    override fun onDestroyView() {
        binding.colorPicker.adapter = null
        colorAdapter = null
        editorHistory = null
        editorSearch = null
        _binding = null
        super.onDestroyView()
    }
}
