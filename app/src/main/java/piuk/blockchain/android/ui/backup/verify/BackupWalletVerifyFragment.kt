package piuk.blockchain.android.ui.backup.verify

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.fragment_backup_wallet_verify.*
import org.koin.android.ext.android.inject
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.backup.completed.BackupWalletCompletedFragment
import piuk.blockchain.android.ui.backup.start.BackupWalletStartingFragment
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import com.blockchain.ui.dialog.MaterialProgressDialog
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.androidcoreui.utils.extensions.toast

class BackupWalletVerifyFragment : BaseFragment<BackupVerifyView, BackupVerifyPresenter>(),
    BackupVerifyView {

    private val backupVerifyPresenter: BackupVerifyPresenter by inject()

    private var progressDialog: MaterialProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_backup_wallet_verify)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewReady()

        button_verify.setOnClickListener {
            presenter.onVerifyClicked(
                edittext_first_word.text.toString(),
                edittext_second_word.text.toString(),
                edittext_third_word.text.toString()
            )
        }
    }

    override fun showWordHints(hints: List<Int>) {
        val mnemonicRequestHint = resources.getStringArray(R.array.mnemonic_word_requests)
        edittext_first_word.hint = mnemonicRequestHint[hints[0]]
        edittext_second_word.hint = mnemonicRequestHint[hints[1]]
        edittext_third_word.hint = mnemonicRequestHint[hints[2]]
    }

    override fun getPageBundle(): Bundle? = arguments

    override fun createPresenter() = backupVerifyPresenter

    override fun getMvpView() = this

    override fun showProgressDialog() {
        progressDialog = MaterialProgressDialog(
            requireContext()
        ).apply {
            setMessage("${getString(R.string.please_wait)}…")
            setCancelable(false)
            show()
        }
    }

    override fun hideProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog?.dismiss()
        }
    }

    override fun showCompletedFragment() {
        popAllAndStartFragment(
            BackupWalletCompletedFragment.newInstance(true),
            BackupWalletCompletedFragment.TAG
        )
    }

    override fun showStartingFragment() {
        popAllAndStartFragment(BackupWalletStartingFragment(), BackupWalletStartingFragment.TAG)
    }

    override fun showToast(message: Int, toastType: String) {
        toast(message, toastType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.run {
            val view = currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    private fun popAllAndStartFragment(fragment: Fragment, tag: String) {
        fragmentManager?.run {
            popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(tag)
                .commit()
        }
    }
}