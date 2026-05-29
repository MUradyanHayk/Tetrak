package com.codestream.tetrak.screens.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentPremiumBinding
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.premium.PremiumConfig
import com.codestream.tetrak.premium.PremiumManager
import com.codestream.tetrak.premium.PremiumPlan
import androidx.core.content.ContextCompat
import com.codestream.tetrak.utils.AppConstants
import com.google.android.material.snackbar.Snackbar

class PremiumFragment : Fragment() {
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private var monthlyPlan: PremiumPlan? = null
    private var yearlyPlan: PremiumPlan? = null
    private var selectedProductId: String = PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            binding.root.visibility = View.GONE
            closePremiumScreen()
            return
        }
        setupBackNavigation()
        setupToolbar()
        setupStaticUi()
        setupClicks()
        loadPlans()
        animateIntro()
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closePremiumScreen()
        })
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { closePremiumScreen() }
    }

    private fun closePremiumScreen() {
        val navController = findNavController()
        val returnedToSettings = navController.popBackStack(R.id.settingsFragment, false)
        if (!returnedToSettings) {
            val popped = navController.popBackStack()
            if (!popped && isAdded) navController.navigate(R.id.startFragment)
        }
    }

    private fun setupStaticUi() = with(binding) {
        refreshStatus()
        monthlyPrice.text = PremiumConfig.DEFAULT_MONTHLY_PRICE
        yearlyPrice.text = PremiumConfig.DEFAULT_YEARLY_PRICE
        updatePlanSelection(animate = false)
    }

    private fun setupClicks() = with(binding) {
        monthlyCard.setOnClickListener { selectPlan(PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID) }
        yearlyCard.setOnClickListener { selectPlan(PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) }
        monthlyButton.setOnClickListener { selectPlan(PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID) }
        yearlyButton.setOnClickListener { selectPlan(PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) }
        continueButton.setOnClickListener { buy(selectedProductId) }
        restoreButton.setOnClickListener {
            setLoading(true)
            PremiumBillingManager.restorePurchases(requireContext()) { _, message ->
                if (!isAdded) return@restorePurchases
                setLoading(false)
                refreshStatus()
                Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadPlans() {
        setLoading(true)
        PremiumBillingManager.loadPlans(requireContext()) { plans, warning ->
            if (!isAdded) return@loadPlans
            setLoading(false)
            monthlyPlan = plans.firstOrNull { it.productId == PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID }
            yearlyPlan = plans.firstOrNull { it.productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID }
            bindPlanTexts()
            updatePlanSelection(animate = false)
            if (!warning.isNullOrBlank()) Snackbar.make(binding.root, warning, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun bindPlanTexts() = with(binding) {
        monthlyPlan?.let {
            monthlyTitle.text = it.title
            monthlySubtitle.text = it.subtitle
            monthlyPrice.text = it.formattedPrice
        }
        yearlyPlan?.let {
            yearlyTitle.text = it.title
            yearlySubtitle.text = it.subtitle
            yearlyPrice.text = it.formattedPrice
        }
    }

    private fun selectPlan(productId: String) {
        if (selectedProductId == productId) {
            animateTap(if (productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) binding.yearlyCard else binding.monthlyCard)
            return
        }
        selectedProductId = productId
        updatePlanSelection(animate = true)
    }

    private fun updatePlanSelection(animate: Boolean) = with(binding) {
        val monthlySelected = selectedProductId == PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID
        val yearlySelected = selectedProductId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)
        val outline = ContextCompat.getColor(requireContext(), R.color.surface_variant)
        val selectedSurface = ContextCompat.getColor(requireContext(), R.color.primary_container)
        val normalSurface = ContextCompat.getColor(requireContext(), R.color.surface)

        monthlyCard.strokeColor = if (monthlySelected) primary else outline
        yearlyCard.strokeColor = if (yearlySelected) primary else outline
        monthlyCard.strokeWidth = dp(if (monthlySelected) 3 else 1)
        yearlyCard.strokeWidth = dp(if (yearlySelected) 3 else 1)
        monthlyCard.setCardBackgroundColor(if (monthlySelected) selectedSurface else normalSurface)
        yearlyCard.setCardBackgroundColor(if (yearlySelected) selectedSurface else normalSurface)
        monthlyButton.setText(if (monthlySelected) R.string.selected_plan else R.string.choose)
        yearlyButton.setText(if (yearlySelected) R.string.selected_plan else R.string.choose)
        continueButton.text = getString(
            R.string.continue_with_plan,
            if (yearlySelected) yearlyPrice.text else monthlyPrice.text
        )

        if (animate) {
            animateSelection(if (monthlySelected) monthlyCard else yearlyCard)
        }
    }

    private fun buy(productId: String) {
        if (PremiumManager.isPremium(requireContext())) {
            Snackbar.make(binding.root, R.string.premium_already_active, Snackbar.LENGTH_SHORT).show()
            return
        }
        animateTap(if (productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) binding.yearlyCard else binding.monthlyCard)
        setLoading(true)
        PremiumBillingManager.launchPremiumPurchase(requireActivity(), productId) { message ->
            if (!isAdded) return@launchPremiumPurchase
            setLoading(false)
            refreshStatus()
            updatePlanSelection(animate = false)
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
        binding.root.postDelayed({ if (isAdded) { setLoading(false); refreshStatus(); updatePlanSelection(animate = false) } }, 1200L)
    }

    private fun refreshStatus() = with(binding) {
        val premium = PremiumManager.isPremium(requireContext())
        premiumStatus.setText(if (premium) R.string.premium_status_active else R.string.premium_status_free)
        premiumStatus.alpha = if (premium) 1f else 0.82f
        monthlyButton.isEnabled = !premium
        yearlyButton.isEnabled = !premium
        continueButton.isEnabled = !premium
    }

    private fun setLoading(loading: Boolean) = with(binding) {
        val premium = PremiumManager.isPremium(requireContext())
        loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        monthlyButton.isEnabled = !loading && !premium
        yearlyButton.isEnabled = !loading && !premium
        continueButton.isEnabled = !loading && !premium
        restoreButton.isEnabled = !loading
    }

    private fun animateTap(view: View) {
        view.animate().scaleX(0.985f).scaleY(0.985f).setDuration(80L).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
        }.start()
    }

    private fun animateSelection(view: View) {
        view.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(120L)
            .withEndAction { view.animate().scaleX(1f).scaleY(1f).setDuration(150L).start() }
            .start()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun animateIntro() = with(binding) {
        heroCard.alpha = 0f
        monthlyCard.alpha = 0f
        yearlyCard.alpha = 0f
        continueButton.alpha = 0f
        featuresCard.alpha = 0f
        heroCard.translationY = 28f
        monthlyCard.translationY = 28f
        yearlyCard.translationY = 28f
        continueButton.translationY = 28f
        featuresCard.translationY = 28f
        heroCard.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        monthlyCard.animate().alpha(1f).translationY(0f).setStartDelay(90L).setDuration(260L).start()
        yearlyCard.animate().alpha(1f).translationY(0f).setStartDelay(160L).setDuration(260L).start()
        continueButton.animate().alpha(1f).translationY(0f).setStartDelay(210L).setDuration(260L).start()
        featuresCard.animate().alpha(1f).translationY(0f).setStartDelay(260L).setDuration(260L).start()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        if (_binding != null) updatePlanSelection(animate = false)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
