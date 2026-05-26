package com.codestream.tetrak.screens.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentPremiumBinding
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.premium.PremiumConfig
import com.codestream.tetrak.premium.PremiumManager
import com.codestream.tetrak.premium.PremiumPlan
import com.google.android.material.snackbar.Snackbar

class PremiumFragment : Fragment() {
    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!
    private var monthlyPlan: PremiumPlan? = null
    private var yearlyPlan: PremiumPlan? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupStaticUi()
        setupClicks()
        loadPlans()
        animateIntro()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupStaticUi() = with(binding) {
        refreshStatus()
        monthlyPrice.text = PremiumConfig.DEFAULT_MONTHLY_PRICE
        yearlyPrice.text = PremiumConfig.DEFAULT_YEARLY_PRICE
    }

    private fun setupClicks() = with(binding) {
        monthlyCard.setOnClickListener { buy(monthlyPlan?.productId ?: PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID) }
        yearlyCard.setOnClickListener { buy(yearlyPlan?.productId ?: PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) }
        monthlyButton.setOnClickListener { buy(monthlyPlan?.productId ?: PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID) }
        yearlyButton.setOnClickListener { buy(yearlyPlan?.productId ?: PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) }
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
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
        binding.root.postDelayed({ if (isAdded) { setLoading(false); refreshStatus() } }, 1200L)
    }

    private fun refreshStatus() = with(binding) {
        val premium = PremiumManager.isPremium(requireContext())
        premiumStatus.setText(if (premium) R.string.premium_status_active else R.string.premium_status_free)
        premiumStatus.alpha = if (premium) 1f else 0.82f
        monthlyButton.isEnabled = !premium
        yearlyButton.isEnabled = !premium
    }

    private fun setLoading(loading: Boolean) = with(binding) {
        loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        monthlyButton.isEnabled = !loading && !PremiumManager.isPremium(requireContext())
        yearlyButton.isEnabled = !loading && !PremiumManager.isPremium(requireContext())
        restoreButton.isEnabled = !loading
    }

    private fun animateTap(view: View) {
        view.animate().scaleX(0.985f).scaleY(0.985f).setDuration(80L).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
        }.start()
    }

    private fun animateIntro() = with(binding) {
        heroCard.alpha = 0f
        monthlyCard.alpha = 0f
        yearlyCard.alpha = 0f
        featuresCard.alpha = 0f
        heroCard.translationY = 28f
        monthlyCard.translationY = 28f
        yearlyCard.translationY = 28f
        featuresCard.translationY = 28f
        heroCard.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        monthlyCard.animate().alpha(1f).translationY(0f).setStartDelay(90L).setDuration(260L).start()
        yearlyCard.animate().alpha(1f).translationY(0f).setStartDelay(160L).setDuration(260L).start()
        featuresCard.animate().alpha(1f).translationY(0f).setStartDelay(230L).setDuration(260L).start()
    }


    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
