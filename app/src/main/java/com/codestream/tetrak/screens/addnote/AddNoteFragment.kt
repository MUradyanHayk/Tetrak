package com.codestream.tetrak.screens.addnote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.ViewTreeObserver
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.adapter.NoteColorAdapter
import com.codestream.tetrak.ads.AdMobManager
import com.codestream.tetrak.databinding.DialogColorPickerBinding
import com.codestream.tetrak.databinding.FragmentAddNoteBinding
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.premium.PremiumManager
import com.codestream.tetrak.util.NoteEditorHistory
import com.codestream.tetrak.util.NoteEditorSearch
import com.codestream.tetrak.utils.AppConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddNoteViewModel by viewModels()
    private val colorOptions = listOf(
        Color.parseColor("#5B6CFF"),
        Color.parseColor("#FFB84D"),
        Color.parseColor("#24C6A1"),
        Color.parseColor("#FF6B8A"),
        Color.parseColor("#A162F7"),
        Color.parseColor("#4DB6FF"),
        Color.parseColor("#FF8A3D")
    )
    private var colorAdapter: NoteColorAdapter? = null
    private var selectedColor: Int = NoteModel.DEFAULT_NOTE_COLOR
    private var isSaving = false
    private var editorHistory: NoteEditorHistory? = null
    private var editorSearch: NoteEditorSearch? = null
    private var isGeneratingTitle = false
    private val floatingHandler = Handler(Looper.getMainLooper())
    private val showFloatingRunnable = Runnable { showFloatingControls() }
    private var floatingKeyboardOffset = 0f
    private var isToolsTrayExpanded = false
    private val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener { updateFloatingKeyboardOffset() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackHandling()
        setupToolbar()
        setupColorPicker()
        setupEditorTools()
        setupFloatingEditorControls()
        setupAiTitleGenerator()
        animateIntro()
        setupValidation()
        setupClicks()
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

    private fun setupToolbar() {
        binding.topEditorBar.setNavigationOnClickListener { handleExitRequest() }
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
        val description = binding.edAddDesc.text?.toString().orEmpty().trim()
        if (description.isBlank()) {
            Snackbar.make(binding.root, R.string.ai_title_description_required, Snackbar.LENGTH_SHORT).show()
            binding.edAddDesc.requestFocus()
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
                binding.edAddTitle.setText(title)
                binding.edAddTitle.setSelection(binding.edAddTitle.text?.length ?: 0)
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
                updateSelectedColorUi(animate = true)
            },
            onCustomColorClick = { showColorPickerDialog() }
        )
        binding.colorPicker.adapter = colorAdapter
        updateSelectedColorUi(animate = false)
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
                updateSelectedColorUi(animate = true)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun updateSelectedColorUi(animate: Boolean) = with(binding) {
        selectedColorHex.text = getString(R.string.selected_color_value, selectedColor.toHexColor())
        editorCard.strokeColor = selectedColor
        if (animate) {
            colorPicker.animate().scaleX(1.01f).scaleY(1.01f).setDuration(90L).withEndAction {
                colorPicker.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
            }.start()
        }
    }

    private fun setupEditorTools() {
        editorHistory = NoteEditorHistory(
            fields = listOf(binding.edAddTitle, binding.edAddDesc),
            onStateChanged = { updateEditorToolState() }
        ).also { it.attach() }
        editorSearch = NoteEditorSearch(listOf(binding.edAddTitle, binding.edAddDesc))

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

    private fun setupFloatingEditorControls() = with(binding) {
        editorToolsCard.visibility = View.GONE
        editorToolsCard.alpha = 0f
        editorToolsCard.scaleX = 0.55f
        editorToolsCard.scaleY = 0.92f
        editorToolsCard.translationX = 24f
        editorToolsFab.setImageResource(R.drawable.ic_tools)
        editorToolsFab.setOnClickListener {
            toggleToolsTray(!isToolsTrayExpanded)
        }
        root.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
        showFloatingControls()
    }

    private fun toggleToolsTray(show: Boolean) {
        isToolsTrayExpanded = show
        val tray = binding.editorToolsCard
        tray.animate().cancel()
        animateToolsFabIcon(show)
        if (show) {
            tray.visibility = View.VISIBLE
            tray.pivotX = tray.width.takeIf { it > 0 }?.toFloat() ?: 206f
            tray.pivotY = tray.height / 2f
            tray.alpha = 0f
            tray.scaleX = 0.35f
            tray.scaleY = 0.86f
            tray.translationX = 28f
            tray.translationY = floatingKeyboardOffset + 6f
            setToolButtonsVisible(false)
            tray.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .translationY(floatingKeyboardOffset)
                .setDuration(260L)
                .withEndAction { animateToolButtonsIn() }
                .start()
        } else {
            animateToolButtonsOut()
            tray.animate()
                .alpha(0f)
                .scaleX(0.35f)
                .scaleY(0.86f)
                .translationX(28f)
                .translationY(floatingKeyboardOffset + 6f)
                .setDuration(190L)
                .withEndAction { tray.visibility = View.GONE }
                .start()
        }
    }

    private fun animateToolsFabIcon(open: Boolean) = with(binding.editorToolsFab) {
        animate().cancel()
        animate()
            .scaleX(0.78f)
            .scaleY(0.78f)
            .rotation(if (open) 90f else -90f)
            .setDuration(95L)
            .withEndAction {
                setImageResource(if (open) R.drawable.ic_close else R.drawable.ic_tools)
                contentDescription = getString(if (open) R.string.close else R.string.editor_tools)
                rotation = if (open) -90f else 90f
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotation(0f)
                    .setDuration(155L)
                    .start()
            }
            .start()
    }

    private fun toolButtons(): List<View> = listOf(
        binding.toolCopyBtn,
        binding.toolSearchBtn,
        binding.toolUndoBtn,
        binding.toolRedoBtn
    )

    private fun setToolButtonsVisible(visible: Boolean) {
        toolButtons().forEach { button ->
            button.alpha = if (visible) 1f else 0f
            button.translationX = if (visible) 0f else 16f
            button.scaleX = if (visible) 1f else 0.82f
            button.scaleY = if (visible) 1f else 0.82f
        }
    }

    private fun animateToolButtonsIn() {
        toolButtons().forEachIndexed { index, button ->
            button.animate().cancel()
            button.animate()
                .alpha(1f)
                .translationX(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(index * 38L)
                .setDuration(180L)
                .start()
        }
    }

    private fun animateToolButtonsOut() {
        toolButtons().forEachIndexed { index, button ->
            button.animate().cancel()
            button.animate()
                .alpha(0f)
                .translationX(14f)
                .scaleX(0.82f)
                .scaleY(0.82f)
                .setStartDelay(index * 18L)
                .setDuration(110L)
                .start()
        }
    }
    private fun hideFloatingControlsTemporarily() {
        floatingHandler.removeCallbacks(showFloatingRunnable)
        hideFloatingControls()
        floatingHandler.postDelayed(showFloatingRunnable, 650L)
    }

    private fun hideFloatingControls() {
        binding.editorToolsFab.animate().cancel()
        binding.editorToolsFab.animate()
            .alpha(0f)
            .scaleX(0.78f)
            .scaleY(0.78f)
            .translationY(floatingKeyboardOffset + 18f)
            .setDuration(140L)
            .start()
        if (isToolsTrayExpanded) {
            binding.editorToolsCard.animate().cancel()
            binding.editorToolsCard.animate()
                .alpha(0f)
                .scaleX(0.94f)
                .scaleY(0.94f)
                .translationY(floatingKeyboardOffset + 18f)
                .setDuration(140L)
                .start()
        }
    }

    private fun showFloatingControls() {
        if (_binding == null) return
        binding.editorToolsFab.animate().cancel()
        binding.editorToolsFab.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(floatingKeyboardOffset)
            .setDuration(190L)
            .start()
        if (isToolsTrayExpanded) {
            binding.editorToolsCard.animate().cancel()
            binding.editorToolsCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(floatingKeyboardOffset)
                .setDuration(190L)
                .start()
        }
    }

    private fun updateFloatingKeyboardOffset() {
        val binding = _binding ?: return
        val visibleFrame = Rect()
        binding.root.getWindowVisibleDisplayFrame(visibleFrame)
        val hiddenHeight = (binding.root.rootView.height - visibleFrame.bottom).coerceAtLeast(0)
        val newOffset = if (hiddenHeight > binding.root.height * 0.15f) -hiddenHeight.toFloat() else 0f
        if (kotlin.math.abs(newOffset - floatingKeyboardOffset) < 1f) return
        floatingKeyboardOffset = newOffset
        showFloatingControls()
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
        val fields = listOf(binding.edAddTitle, binding.edAddDesc)
        val focused = fields.firstOrNull { it.hasFocus() }
        val selectedText = focused?.let { field ->
            val start = minOf(field.selectionStart, field.selectionEnd).coerceAtLeast(0)
            val end = maxOf(field.selectionStart, field.selectionEnd).coerceAtLeast(0)
            if (end > start) field.text?.substring(start, end) else null
        }
        val textToCopy = selectedText ?: focused?.text?.toString()?.takeIf { it.isNotBlank() }
            ?: listOf(binding.edAddTitle.text?.toString().orEmpty(), binding.edAddDesc.text?.toString().orEmpty())
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

    private fun setupValidation() {
        // Title is optional. Empty titles are saved with a date-based default title.
    }

    private fun setupClicks() {
        binding.addNoteBtn.setOnClickListener {
            val title = binding.edAddTitle.text?.toString().orEmpty().trim()
            val description = binding.edAddDesc.text?.toString().orEmpty().trim()

            saveNote(title, description)
        }
    }


    private fun handleExitRequest() {
        if (isSaving) return
        if (!hasDraftChanges()) {
            findNavController().navigateUp()
            return
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.unsaved_note_title)
            .setMessage(R.string.unsaved_note_message)
            .setNegativeButton(R.string.discard) { _, _ -> findNavController().navigateUp() }
            .setNeutralButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = binding.edAddTitle.text?.toString().orEmpty().trim()
                val description = binding.edAddDesc.text?.toString().orEmpty().trim()
                dialog.dismiss()
                saveNote(title, description)
            }
        }
        dialog.show()
    }

    private fun hasDraftChanges(): Boolean {
        val title = binding.edAddTitle.text?.toString().orEmpty().trim()
        val description = binding.edAddDesc.text?.toString().orEmpty().trim()
        return title.isNotBlank() || description.isNotBlank() || selectedColor != NoteModel.DEFAULT_NOTE_COLOR
    }

    private fun saveNote(title: String, description: String) {
        if (isSaving) return
        isSaving = true
        binding.addNoteBtn.isEnabled = false
        viewModel.insert(NoteModel(title = title, description = description, color = selectedColor)) {
            AdMobManager.recordNoteSavedAndMaybeShowVideoAd(requireActivity()) {
                if (isAdded) {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun animateIntro() = with(binding.editorCard) {
        translationY = 48f
        alpha = 0f
        animate().translationY(0f).alpha(1f).setDuration(450L).start()
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
        floatingHandler.removeCallbacks(showFloatingRunnable)
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(keyboardLayoutListener)
        binding.addNoteBtn.animate().cancel()
        binding.editorToolsFab.animate().cancel()
        binding.editorToolsCard.animate().cancel()
        binding.colorPicker.adapter = null
        colorAdapter = null
        editorHistory = null
        editorSearch = null
        _binding = null
        super.onDestroyView()
    }
}
