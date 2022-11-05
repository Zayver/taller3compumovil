package com.ntn.taller3.composables.mainscreen

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.maps.android.compose.GoogleMap

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(){
    Scaffold( topBar = { TopBar() }) {

    }
}

@Composable
private fun TopBar(){
    TopAppBar() {

    }
}

@Composable
private fun Map(){
    GoogleMap(){

    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview(){
    MainScreen()
}
