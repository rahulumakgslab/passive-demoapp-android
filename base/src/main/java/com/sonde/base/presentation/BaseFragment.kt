package com.sonde.base.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.sonde.base.presentation.utils.extensions.showAlertDialog
import com.sonde.base.presentation.utils.extensions.toGone
import com.sonde.base.presentation.utils.extensions.toVisible

abstract class BaseFragment<B : ViewDataBinding, M : ViewModel>(
    @LayoutRes
    private val layoutId: Int
) : Fragment() {

    lateinit var viewModel: M
    lateinit var viewBinding: B


    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = initViewModel()

        initDependencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        viewBinding.lifecycleOwner = viewLifecycleOwner
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDataBinding()
    }


    abstract fun initViewModel(): M

    abstract fun initDependencies()

    abstract fun initDataBinding()

    fun showProgressbar(loadingView: View) {
        loadingView.toVisible()
    }

    fun hideProgressbar(loadingView: View) {
        loadingView.toGone()
    }

    fun showNoInternetDialog(titleResId: Int, messageResId: Int) {
        activity?.showAlertDialog(
            titleResId,
            messageResId
        )
    }

    fun showErrorMessage(messageResId: Int) {
        activity?.showAlertDialog(
            messageResId = messageResId
        )
    }

}