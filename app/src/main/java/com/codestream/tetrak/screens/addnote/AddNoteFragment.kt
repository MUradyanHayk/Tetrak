package com.codestream.tetrak.screens.addnote

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
    private var saveButtonBottomAnimator: ValueAnimator? = null
    private var lastSaveButtonBottomMargin: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackHandling()
        setupToolbar()
        setupKeyboardAwareSaveButton()
        setupColorPicker()
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
        binding.toolbar.setNavigationOnClickListener { handleExitRequest() }
    }


    private fun setupKeyboardAwareSaveButton() {
        val defaultBottomMargin = binding.addNoteBtn.resources.getDimensionPixelSize(R.dimen.save_button_bottom_margin)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardHeight = (imeInsets.bottom - systemBarInsets.bottom).coerceAtLeast(0)
            val targetBottomMargin = defaultBottomMargin + keyboardHeight
            val keyboardVisible = keyboardHeight > 0

            animateSaveButtonForKeyboard(targetBottomMargin, keyboardVisible)
            if (keyboardVisible) {
                keepFocusedFieldVisible()
            }
            insets
        }
    }

    private fun animateSaveButtonForKeyboard(targetBottomMargin: Int, keyboardVisible: Boolean) {
        if (lastSaveButtonBottomMargin == targetBottomMargin) return

        val params = binding.addNoteBtn.layoutParams as ConstraintLayout.LayoutParams
        val startBottomMargin = if (lastSaveButtonBottomMargin == -1) params.bottomMargin else lastSaveButtonBottomMargin
        lastSaveButtonBottomMargin = targetBottomMargin

        saveButtonBottomAnimator?.cancel()
        saveButtonBottomAnimator = ValueAnimator.ofInt(startBottomMargin, targetBottomMargin).apply {
            duration = if (keyboardVisible) 280L else 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val animatedMargin = animator.animatedValue as Int
                val animatedParams = binding.addNoteBtn.layoutParams as ConstraintLayout.LayoutParams
                animatedParams.bottomMargin = animatedMargin
                binding.addNoteBtn.layoutParams = animatedParams
            }
            start()
        }

        binding.addNoteBtn.animate()
            .scaleX(if (keyboardVisible) 0.985f else 1f)
            .scaleY(if (keyboardVisible) 0.985f else 1f)
            .alpha(1f)
            .setDuration(180L)
            .withEndAction {
                binding.addNoteBtn.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(140L)
                    .start()
            }
            .start()
    }

    private fun keepFocusedFieldVisible() {
        binding.editorScroll.postDelayed({
            val focusedView = binding.root.findFocus() ?: return@postDelayed
            val focusedBottom = focusedView.bottom + binding.editorScroll.paddingBottom + 32
            if (focusedBottom > binding.editorScroll.scrollY + binding.editorScroll.height) {
                binding.editorScroll.smoothScrollTo(0, focusedBottom - binding.editorScroll.height)
            }
        }, 120L)
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

    private fun setupValidation() {
        binding.edAddTitle.doAfterTextChanged { binding.titleInput.error = null }
    }

    private fun setupClicks() {
        binding.addNoteBtn.setOnClickListener {
            val title = binding.edAddTitle.text?.toString().orEmpty().trim()
            val description = binding.edAddDesc.text?.toString().orEmpty().trim()

            if (title.isBlank()) {
                binding.titleInput.error = getString(R.string.title_required)
                return@setOnClickListener
            }

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
                if (title.isBlank()) {
                    binding.titleInput.error = getString(R.string.title_required)
                    binding.edAddTitle.requestFocus()
                    return@setOnClickListener
                }
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
        saveButtonBottomAnimator?.cancel()
        saveButtonBottomAnimator = null
        binding.addNoteBtn.animate().cancel()
        binding.colorPicker.adapter = null
        colorAdapter = null
        _binding = null
        super.onDestroyView()
    }
}
