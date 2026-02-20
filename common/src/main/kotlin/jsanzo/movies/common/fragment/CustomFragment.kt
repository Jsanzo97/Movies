package jsanzo.movies.common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import jsanzo.movies.common.DIALOG_FRAGMENT_TAG
import jsanzo.movies.common.R
import com.google.android.material.textview.MaterialTextView

abstract class CustomFragment(private val contentLayoutId: Int) : Fragment() {

    private lateinit var progressView: View
    private lateinit var progressDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(contentLayoutId, container, false)
    }

    fun showError(message: String) {
        hideProgressDialog()
        CustomDialogFragment(
            message = message,
            title = getString(R.string.error)
        ).show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
    }

    fun showProgressDialog(title: String = getString(R.string.loading)) {
        if (!::progressDialog.isInitialized) {
            progressView = layoutInflater.inflate(R.layout.custom_progress_dialog, null)
            progressView.findViewById<MaterialTextView>(R.id.title_progress_dialog).text = title
            progressDialog = AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setView(progressView)
                .create()
        }

        progressDialog.show()
    }

    fun hideProgressDialog() {
        if (::progressDialog.isInitialized) {
            progressDialog.dismiss()
        }
    }

}
