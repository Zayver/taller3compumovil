package com.ntn.taller3.composables.users

import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ntn.taller3.composables.auth.LoginViewModel

@Composable
fun UsersScreen(_viewModel: UsersViewModel = viewModel()){
    val users by _viewModel.users.collectAsState()

    _viewModel.getData()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {_viewModel.pushData()})
        { Text("Add") }
        LazyColumn( contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            itemsIndexed(users) { index, item ->
                UserCard(emp = item)
            }
        }
    }
}



@Composable
fun UserCard(emp: UserDetails) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = androidx.compose.ui.graphics.Color.Blue,
        shape = RoundedCornerShape(corner = CornerSize(16.dp))

    ) {

        Row(modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.weight(1f),
                Arrangement.Center) {
                Text(
                    text = emp.username,
                    style = TextStyle(
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = "Latitude :- "+emp.latitude,
                    style = TextStyle(
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "Logitude :- "+emp.longitude,
                    style = TextStyle(
                        color = androidx.compose.ui.graphics.Color.Black,
                        fontSize = 15.sp
                    )
                )

            }

            Image(bitmap = emp.image!!.asImageBitmap(), contentDescription = "Imagen de perfil",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .padding(8.dp)
                    .size(110.dp)
                    .clip((CircleShape)  ))

        }
    }
}


