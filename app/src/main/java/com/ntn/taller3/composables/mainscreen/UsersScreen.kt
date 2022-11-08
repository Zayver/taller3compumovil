package com.ntn.taller3.composables.mainscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ntn.taller3.composables.users.UsersViewModel
import com.ntn.taller3.data.UserDetails

@Composable
fun UsersScreen(_viewModel: MainScreenViewModel = viewModel()) {
    val otherUsers by _viewModel.users.collectAsState()
    Scaffold(topBar = { TopBar() }) {
        LazyColumn(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            itemsIndexed(otherUsers) { index, item ->
                UserCard(emp = item)
            }
        }
    }


}

@Composable
private fun TopBar(_viewModel: MainScreenViewModel = viewModel()) {
    TopAppBar(title = { Text(text = "Usuarios en línea") }, navigationIcon = {
        IconButton(onClick = { _viewModel.onUIStateChange(UIState.Map) }) {
            Icon(Icons.Default.ArrowBack, "")
        }
    })

}


@Composable
private fun UserCard(emp: UserDetails) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = androidx.compose.ui.graphics.Color.Blue,
        shape = RoundedCornerShape(corner = CornerSize(16.dp))

    ) {

        Row(modifier = Modifier.padding(20.dp)) {
            Column(
                modifier = Modifier.weight(1f),
                Arrangement.Center
            ) {
                Text(
                    text = emp.username,
                    style = TextStyle(
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = "Latitude :- " + emp.latitude,
                    style = TextStyle(
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "Logitude :- " + emp.longitude,
                    style = TextStyle(
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 15.sp
                    )
                )

            }
            /*
            Image(bitmap = emp.image!!.asImageBitmap(), contentDescription = "Imagen de perfil",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .padding(8.dp)
                    .size(110.dp)
                    .clip((CircleShape)  ))
*/
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    UsersScreen()
}


