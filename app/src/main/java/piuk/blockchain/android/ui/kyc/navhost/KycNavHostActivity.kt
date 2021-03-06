package piuk.blockchain.android.ui.kyc.navhost

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.view.Menu
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment.findNavController
import piuk.blockchain.android.ui.kyc.complete.ApplicationCompleteFragment
import piuk.blockchain.android.campaign.CampaignType
import piuk.blockchain.android.ui.kyc.navhost.models.KycStep
import piuk.blockchain.android.ui.kyc.splash.KycSplashFragment
import com.blockchain.swap.nabu.StartKyc
import com.blockchain.swap.nabu.StartKycForBuySell
import org.koin.android.ext.android.inject
import piuk.blockchain.android.KycNavXmlDirections
import piuk.blockchain.android.R
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.invisibleIf
import piuk.blockchain.androidcoreui.utils.extensions.toast
import kotlinx.android.synthetic.main.activity_kyc_nav_host.frame_layout_fragment_wrapper as fragmentWrapper
import kotlinx.android.synthetic.main.activity_kyc_nav_host.nav_host as navHostFragment
import kotlinx.android.synthetic.main.activity_kyc_nav_host.progress_bar_kyc as progressIndicator
import kotlinx.android.synthetic.main.activity_kyc_nav_host.progress_bar_loading_user as progressLoadingUser
import kotlinx.android.synthetic.main.activity_kyc_nav_host.toolbar_kyc as toolBar

internal class KycStarterBuySell : StartKycForBuySell {
    override fun startKycActivity(context: Any) {
        KycNavHostActivity.start(context as Context, CampaignType.BuySell)
    }
}

internal class KycStarter : StartKyc {
    override fun startKycActivity(context: Any) {
        KycNavHostActivity.start(context as Context, CampaignType.Swap, true)
    }
}

class KycNavHostActivity : BaseMvpActivity<KycNavHostView, KycNavHostPresenter>(),
    KycProgressListener, KycNavHostView {

    private val presenter: KycNavHostPresenter by inject()
    private var navInitialDestination: NavDestination? = null
    private val navController by unsafeLazy { findNavController(navHostFragment) }
    private val currentFragment: Fragment?
        get() = navHostFragment.childFragmentManager.findFragmentById(R.id.nav_host)

    override val campaignType by unsafeLazy {
        intent.getSerializableExtra(EXTRA_CAMPAIGN_TYPE) as CampaignType
    }
    override val showTiersLimitsSplash by unsafeLazy {
        intent.getBooleanExtra(EXTRA_SHOW_TIERS_LIMITS_SPLASH, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyc_nav_host)
        val title = when (campaignType) {
            CampaignType.BuySell -> R.string.buy_sell_splash_title
            CampaignType.Swap -> R.string.kyc_splash_title
            CampaignType.Sunriver,
            CampaignType.SimpleBuy,
            CampaignType.Blockstack,
            CampaignType.Resubmission -> R.string.sunriver_splash_title
        }
        setupToolbar(toolBar, title)

        navController
            .setGraph(R.navigation.kyc_nav, intent.extras)

        onViewReady()
    }

    override fun setHostTitle(title: Int) {
        toolBar.title = getString(title)
    }

    override fun displayLoading(loading: Boolean) {
        fragmentWrapper.invisibleIf(loading)
        progressLoadingUser.invisibleIf(!loading)
    }

    override fun showErrorToastAndFinish(message: Int) {
        toast(message, ToastCustom.TYPE_ERROR)
        finish()
    }

    override fun navigate(directions: NavDirections) {
        navController.navigate(directions)
        navInitialDestination = navController.currentDestination
    }

    override fun navigateToKycSplash() {
        navController.navigate(KycNavXmlDirections.actionDisplayKycSplash())
        navInitialDestination = navController.currentDestination
    }

    override fun navigateToResubmissionSplash() {
        navController.navigate(KycNavXmlDirections.actionDisplayResubmissionSplash())
        navInitialDestination = navController.currentDestination
    }

    override fun incrementProgress(kycStep: KycStep) {
        val progress =
            100 * (
                    KycStep.values()
                        .takeWhile { it != kycStep }
                        .sumBy { it.relativeValue } + kycStep.relativeValue
                    ) / KycStep.values().sumBy { it.relativeValue }

        updateProgressBar(progress)
    }

    override fun decrementProgress(kycStep: KycStep) {
        val progress =
            100 * KycStep.values()
                .takeWhile { it != kycStep }
                .sumBy { it.relativeValue } / KycStep.values().sumBy { it.relativeValue }

        updateProgressBar(progress)
    }

    private fun updateProgressBar(progress: Int) {
        ObjectAnimator.ofInt(progressIndicator, "progress", progress).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun hideBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.fragments.forEach { fragment ->
            fragment.childFragmentManager.fragments.forEach {
                it.onActivityResult(
                    requestCode,
                    resultCode,
                    data
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = consume {

        if (flowShouldBeClosedAfterBackAction() || !navController.navigateUp()) {
            finish()
        }
    }

    override fun onBackPressed() {
        if (flowShouldBeClosedAfterBackAction()) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun flowShouldBeClosedAfterBackAction() =
        // If on final page, close host Activity on navigate up
        currentFragment is ApplicationCompleteFragment ||
                // If coming from buy/sell, we want the intro/splash screen to be the 1st screen in the stack
                (currentFragment is KycSplashFragment && campaignType == CampaignType.BuySell) ||
                // If not coming from settings, we want the 1st launched screen to be the 1st screen in the stack
                (navInitialDestination != null && navInitialDestination?.id == navController.currentDestination?.id)

    override fun createPresenter(): KycNavHostPresenter = presenter

    override fun getView(): KycNavHostView = this

    override fun startLogoutTimer() = Unit

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.help, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                showHelpDialog(this)
                return true
            }
            else -> false
        }
    }

    companion object {

//        const val RESULT_KYC_STX_COMPLETE = 5

        private const val EXTRA_CAMPAIGN_TYPE = "piuk.blockchain.android.EXTRA_CAMPAIGN_TYPE"
        const val EXTRA_SHOW_TIERS_LIMITS_SPLASH = "piuk.blockchain.android.EXTRA_SHOW_TIERS_LIMITS_SPLASH"

        @JvmStatic
        fun start(context: Context, campaignType: CampaignType) {
            intentArgs(context, campaignType)
                .run { context.startActivity(this) }
        }

        @JvmStatic
        fun start(context: Context, campaignType: CampaignType, showLimits: Boolean) {
            intentArgs(context, campaignType, showLimits)
                .run { context.startActivity(this) }
        }

        @JvmStatic
        fun startForResult(activity: Activity, campaignType: CampaignType, requestCode: Int) {
            intentArgs(activity, campaignType)
                .run { activity.startActivityForResult(this, requestCode) }
        }

        @JvmStatic
        fun startForResult(fragment: Fragment, campaignType: CampaignType, requestCode: Int) {
            intentArgs(fragment.requireContext(), campaignType)
                .run { fragment.startActivityForResult(this, requestCode) }
        }

        @JvmStatic
        private fun intentArgs(
            context: Context,
            campaignType: CampaignType,
            showTiersLimitsSplash: Boolean = false
        ): Intent =
            Intent(context, KycNavHostActivity::class.java)
                .apply {
                    putExtra(EXTRA_CAMPAIGN_TYPE, campaignType)
                    putExtra(EXTRA_SHOW_TIERS_LIMITS_SPLASH, showTiersLimitsSplash)
                }
    }
}

interface KycProgressListener {

    val campaignType: CampaignType

    fun setHostTitle(@StringRes title: Int)

    fun incrementProgress(kycStep: KycStep)

    fun decrementProgress(kycStep: KycStep)

    fun hideBackButton()
}