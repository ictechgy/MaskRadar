package com.jh.mask_radar.ui.about

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.jh.mask_radar.R

class TermsFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_terms, container, false)
        val termsWebView : WebView = v.findViewById(R.id.terms_web_view)
        //termsWebView.webChromeClient = WebChromeClient()

        val progressBar : ProgressBar = v.findViewById(R.id.webProgress)
        termsWebView.webViewClient = object:WebViewClient(){

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.INVISIBLE
            }
        }


        val webViewSettings = termsWebView.settings
        webViewSettings.setSupportMultipleWindows(false)
        webViewSettings.javaScriptCanOpenWindowsAutomatically = false
        webViewSettings.javaScriptEnabled = false
        webViewSettings.loadWithOverviewMode = true
        webViewSettings.useWideViewPort = true
        webViewSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        termsWebView.loadUrl(getString(R.string.terms_URL))

        return v
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TermsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
                TermsFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}