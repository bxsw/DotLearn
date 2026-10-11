package com.dotlearn.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotlearn.app.ui.AppRoot
import com.dotlearn.app.ui.AppViewModel
import com.dotlearn.app.ui.theme.DotLearnTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModel.Factory)
            val themeMode by vm.themeMode.collectAsState()
            DotLearnTheme(themeMode = themeMode) {
                AppRoot(vm)
            }
        }
    }
}
